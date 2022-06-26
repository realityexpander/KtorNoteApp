package com.realityexpander.ktornoteapp.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.common.isInternetConnected
import com.realityexpander.ktornoteapp.common.networkBoundResource
import com.realityexpander.ktornoteapp.data.local.NotesDao
import com.realityexpander.ktornoteapp.data.local.entities.LocallyDeletedNoteId
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.remote.NotesApi
import com.realityexpander.ktornoteapp.data.remote.requests.AccountRequest
import com.realityexpander.ktornoteapp.data.remote.requests.AddOwnerIdToNoteIdRequest
import com.realityexpander.ktornoteapp.data.remote.requests.DeleteNoteIdRequest
import com.realityexpander.ktornoteapp.data.remote.responses.BaseSimpleResponse
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponse
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponseWithData
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.fromValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

// All functions for local database and remote API
class NoteRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val context: Application,  // for connectivity check
    @Named("NotesApi_accept_all_certs_for_development") private val notesApi: NotesApi,
    private val gson: Gson
) {

    private var curNotesResponse: Response<SimpleResponseWithData<List<NoteEntity>>>? = null

    ////////////////////////////////////////////////////
    /// CACHED = uses Api and local database
    ///  * UI is updated via LiveData or Flow subscriptions to database changes.
    ///  ≈ Api -> Database --> UI
    ///  ≈ Database -> Api -> Database --> UI

    // Update or insert a note
    // Api -> Database --> UI
    suspend fun upsertNoteCached(note: NoteEntity) {

        // Attempt upsert via Api first
        val response = try {
            notesApi.addNote(note)
        } catch (e: Exception) {
            null
        }

        // Attempt upsert on the local Database
        if (response != null && response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                // force the note Id to match the one from the server
                val noteId = body.data?.id

                if (noteId != null) {
                    upsertNoteDb(note.apply { isSynced = true; id = noteId })
                }
            }
        } else {
            // If the server failed to add the note, then attempt to update the local database
            // and set the note as not synced.
            upsertNoteDb(note.apply { isSynced = false })
        }
    }

    // Update or insert a List of notes
    // Api -> Database --> UI
    suspend fun upsertNotesCached(notes: List<NoteEntity>) {
        notes.forEach { note ->
            upsertNoteCached(note)
        }
    }

    // Get all notes for the authenticated user
    // Database -> Api -> Database --> UI (as Flow of Resource)
    fun getAllNotesCached():
            Flow<Resource<List<NoteEntity>>> {
        return networkBoundResource(
            queryDb = {
                getAllNotesDb()
            },
            fetchFromNetwork = {
                syncAllNotesCached()
                curNotesResponse
            },
            saveFetchedResponseToDb = { response ->
                if(response?.isSuccessful == true && response.body() != null) {
                    val freshNotes = response.body()?.data ?: emptyList()

                    upsertNotesDb(freshNotes.onEach{ it.isSynced = true })

                    return@networkBoundResource
                }

                throw Exception("Error getting notes from API, " +
                        "response: code=" + response?.code() + ", "
                        + response?.message() + ", "
                        + response?.errorBody()?.string()
                )
            },
            shouldFetch = { _->
                isInternetConnected(context)
            },
            debugNetworkResponseType = { response ->
                println("debugNetworkResponseType = $response")
            },
            debugDatabaseResultType = { result ->
                println("debugDatabaseResultType = $result")
            }
        )
    }

    // Delete a noteId
    // Api -> Database --> UI
    suspend fun deleteNoteIdCached(deleteNoteId: String) {

        // Attempt delete via Api first
        val response = try {
            notesApi.deleteNoteId(DeleteNoteIdRequest(deleteNoteId))
        } catch (e: Exception) {
            null
        }

        // Delete locally no matter what the server says
        notesDao.deleteNoteId(deleteNoteId)

        // Check if the server returned a successful response
        if (response != null && response.isSuccessful) {
            // if the server succeeded, delete the deleteNoteId from
            // the table list of locally_deleted_noteIds (if it exists)
            deleteLocallyDeletedNoteIdDb(deleteNoteId)
        } else {
            // if the server failed, insert the deleteNoteId into
            // the table list of locally_deleted_noteIds
            insertLocallyDeletedNoteIdDb(deleteNoteId)
        }

    }

    // Sync all notes for the authenticated user
    // Api -> Database --> UI
    // Delete notes on the server that are not in the local database
    //   via the list of locally_deleted_noteIds.
    suspend fun syncAllNotesCached() {

        // Check if there are any locally deleted notes (deleted while offline)
        val locallyDeletedNoteIds = getAllLocallyDeletedNoteIdsDb()
        if (locallyDeletedNoteIds.isNotEmpty()) {
            // Attempt to Delete (on the server) the locally_deleted notes
            locallyDeletedNoteIds.forEach { locallyDeletedNoteId ->
                deleteNoteIdCached(locallyDeletedNoteId)
            }
        }

        // Check if there are any unsynced notes (added/edited while offline)
        val unsyncedNotes = getAllUnsyncedNotesDb()
        if(unsyncedNotes.isNotEmpty()) {
            // Attempt to add (on the server) the unsynced notes
            unsyncedNotes.forEach { unsyncedNote ->
                upsertNoteCached(unsyncedNote)
            }
        }

        // Get all current notes from the server for this user
        curNotesResponse = try {
            getAllNotesApi()
        } catch (e: Exception) {
            null
        }

        if (curNotesResponse != null && curNotesResponse!!.isSuccessful) {
            val body = curNotesResponse!!.body()
            if (body != null) {

                val freshNotes = body.data ?: emptyList()

                // Delete all notes from local database (to start fresh)
                deleteAllNotesDb()

                // Insert all notes from the server into local database
                // and indicate they are successfully synced (isSynced = true)
                upsertNotesDb(freshNotes.onEach { it.isSynced = true })
            }
        }
    }


    ////////////////////////////////////////////////////
    /// REMOTE = to/from Api ONLY

    suspend fun registerApi(email: String, password: String):
            Resource<SimpleResponse> =
        callApi {
            notesApi.register(AccountRequest(email, password))
        }

    suspend fun loginApi(email: String, password: String):
            Resource<SimpleResponse> =
        callApi {
            notesApi.login(AccountRequest(email, password))
        }

    // Gets all notes for the authenticated user, wrapped in a Resource
    suspend fun getAllNotesResourceApi():
            Resource<SimpleResponseWithData<List<NoteEntity>>> =
        callApi {
            notesApi.getNotes()
        }

    // Gets all notes for the authenticated user
    suspend fun getAllNotesApi():
            Response<SimpleResponseWithData<List<NoteEntity>>> =
            notesApi.getNotes()

    // Delete a noteId
    suspend fun deleteNoteApi(deleteNoteId: String):
            Resource<SimpleResponseWithData<NoteEntity>> =
        callApi {
            notesApi.deleteNoteId(DeleteNoteIdRequest(deleteNoteId))
        }

    // Get an ownerId for a given email address
    suspend fun getOwnerIdForEmailApi(email: String?):
            String? {
        if(email.isNullOrBlank()) return null

        val response = callApi {
            notesApi.getOwnerIdForEmail(email)
        }
        if(response.status != Status.SUCCESS) return null

        return response.data?.data
    }

    // Get an email address for a given ownerId
    suspend fun getEmailForOwnerIdApi(ownerId: String?):
            String? {
        if(ownerId.isNullOrBlank()) return null

        val response = callApi {
            notesApi.getEmailForOwnerId(ownerId)
        }
        if(response.status != Status.SUCCESS) return null

        return response.data?.data
    }

    // Add an ownerId to a note using their email address
    suspend fun addOwnerByEmailToNoteIdApi(email: String, noteId: String):
            Resource<SimpleResponseWithData<NoteEntity>> {

        if(noteId.isBlank()) {
            return Resource.error("noteId is blank")
        }

        if(email.isBlank()) {
            return Resource.error("Owner Email can't be blank")
        }

        // Lookup the ownerId for the email on the server
        val ownerId = getOwnerIdForEmailApi(email)
            ?: return Resource.error("OwnerId is not found for email: $email")

        return callApi {
            notesApi.addOwnerIdToNoteId(AddOwnerIdToNoteIdRequest(noteId, ownerId))
        }
    }

    // Standardized call to the API returns a Resource wrapped object,
    // possibly with Data as a subclass of BaseSimpleResponse.
    private suspend fun <T: BaseSimpleResponse> callApi (
        call: suspend () -> Response<out T>
    ): Resource<T> = withContext(Dispatchers.IO) {
        try {
            val retrofitResponse = call()

            if (retrofitResponse.isSuccessful) {
                return@withContext retrofitResponse.body()?.let { apiResponse: T ->
                    if (apiResponse.isSuccessful) {
                        Resource.success(apiResponse.message, apiResponse, apiResponse.statusCode)
                    } else {
                        Resource.error(
                            apiResponse.message,
                            fromValue(retrofitResponse.code()),
                            apiResponse
                        )
                    }
                } ?: Resource.error(
                    "Error - missing api response body",
                    fromValue(retrofitResponse.code()),
                    null
                )
            }

            // Retrofit call or API call was not successful. (network error?)
            // Try to get the error message string from the error body
            val errorMessageFromServer = try {
                val errorMsg = gson.fromJson(
                    retrofitResponse.errorBody()?.string(),
                    SimpleResponse::class.java // error messages are always SimpleResponse
                ).message

                if (errorMsg.isBlank()) {
                    "Server error: ${retrofitResponse.message()}"
                }

                errorMsg
            } catch (e: Exception) {
                e.printStackTrace()

                "Server Error: ${retrofitResponse.message()}"
            }

            Resource.error(errorMessageFromServer,
                fromValue(retrofitResponse.code()), null
            )
        } catch (e: Exception) {
            e.printStackTrace()

            Resource.error(e.message ?: "Unknown error",
                InternalServerError, null
            )
        }
    }



    ////////////////////////////////////////////////////
    /// LOCAL DATABASE = to/from local database ONLY

    //suspend fun getNotes() = notesDao.getAllNotes()
    suspend fun getNoteIdDb(noteId: String): NoteEntity? =
        notesDao.getNoteId(noteId)

    // Delete all notes
    suspend fun deleteAllNotesDb() =
        notesDao.deleteAllNotes()

    // Get all notes
    fun getAllNotesDb(): Flow<List<NoteEntity>> =
        notesDao.getAllNotes()

    // Get all unsynced notes
    suspend fun getAllUnsyncedNotesDb(): List<NoteEntity> =
        notesDao.getAllUnsyncedNotes()

    suspend fun upsertNoteDb(note: NoteEntity) =
        notesDao.upsertNote(note)

    suspend fun upsertNotesDb(notes: List<NoteEntity>) {
        notes.forEach { note ->
            upsertNoteDb(note)
        }
    }

    fun observeNoteIdDb(noteId: String): LiveData<NoteEntity> =
        notesDao.observeNoteId(noteId)


    /// LOCALLY DELETED NOTE IDs = uses local database only ///

    // Get all locally_deleted noteIds
    private suspend fun getAllLocallyDeletedNoteIdsDb(): List<String> =
        notesDao.getAllLocallyDeletedNoteIds()

    // Delete a locally_deleted noteId
    suspend fun deleteLocallyDeletedNoteIdDb(locallyDeleteNoteId: String) =
        notesDao.deleteLocallyDeletedNoteId(locallyDeleteNoteId)

    // Insert a locally_deleted noteId
    private suspend fun insertLocallyDeletedNoteIdDb(locallyDeleteNoteId: String) =
        notesDao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(locallyDeleteNoteId))







    ////////////////////////////////////////////////////
    /// Testing

    fun testNetworkBoundResource(): Flow<Resource<Pair<String, String>>> {  // always returns a Flow of Resource of a type
        // <currentValue, previousValue>
        var dbEntity = Pair("stale DB value", "[no previous value]")

        return networkBoundResource(
            queryDb = {
                println("querying db...")

                flow { emit(dbEntity) }  // simulate a query emission from db
            },
            fetchFromNetwork = {
                println("fetching from network...")

                "fresh value"  // simulate network response
            },
            debugNetworkResponseType = { response ->
                println("debugNwResponseType: '$response'")
            },
            debugDatabaseResultType = { result ->
                println("debugDbResultType: '$result'")
            },
            saveFetchedResponseToDb = { response ->
                println("Saving to DB: '$response'")

                dbEntity = Pair(response, dbEntity.first)
            }
        )
    }

    fun runTestNetworkBoundResource() {
        runBlocking {
            val flow = testNetworkBoundResource()
            flow.collect {
                println(it)
            }
        }
    }

}