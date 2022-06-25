package com.realityexpander.ktornoteapp.repositories

import android.app.Application
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

// All functions for local database and remote API
class NoteRepository @Inject constructor(
    private val notesDao: NotesDao,
    private val context: Application,  // for connectivity check
    private val notesApi: NotesApi,
    private val gson: Gson
) {

    private var curNotesResponse: Response<SimpleResponseWithData<List<NoteEntity>>>? = null

    ////////////////////////////////////////////////////
    /// CACHED = uses Api and local database
    ///  ≈ Api -> Database --> UI
    ///  ≈ Database -> Api -> Database --> UI (as Flow of Resource)

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
    fun getAllNotesCached(): Flow<Resource<List<NoteEntity>>> {
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
    /// REMOTE = to/from Api Only

    suspend fun registerApi(email: String, password: String): Resource<SimpleResponse> =
        callApi {
            notesApi.register(AccountRequest(email, password))
        }

    suspend fun loginApi(email: String, password: String): Resource<SimpleResponse> =
        callApi {
            notesApi.login(AccountRequest(email, password))
        }

    // Gets all notes for the authenticated user, wrapped in a Resource
    suspend fun getAllNotesResourceApi() =
        callApi {
            notesApi.getNotes()
        }

    // Gets all notes for the authenticated user
    suspend fun getAllNotesApi() =
            notesApi.getNotes()

    suspend fun deleteNoteApi(deleteNoteId: String) =
        callApi {
            notesApi.deleteNoteId(DeleteNoteIdRequest(deleteNoteId))
        }

    suspend fun getOwnerIdForEmailApi(email: String?): String? {
        if(email.isNullOrBlank()) return null

        val response = callApi {
            notesApi.getOwnerIdForEmail(email)
        }
        if(response.status != Status.SUCCESS) return null

        return response.data?.data
    }

    // Standardized call to the API returns a `Resource.<status>` object, possibly with Data
    private suspend fun <T : BaseSimpleResponse> callApi(
        call: suspend () -> Response<out T>
    ): Resource<T> = withContext(Dispatchers.IO) {
        try {
            val retrofitResponse = call()

            if (retrofitResponse.isSuccessful) {
                return@withContext retrofitResponse.body()?.let { apiResponse ->
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
    suspend fun getNoteIdDb(noteId: String) = notesDao.getNoteId(noteId)

    // Delete all notes
    suspend fun deleteAllNotesDb() = notesDao.deleteAllNotes()

    // Get all notes
    fun getAllNotesDb() = notesDao.getAllNotes()

    // Get all unsynced notes
    suspend fun getAllUnsyncedNotesDb() = notesDao.getAllUnsyncedNotes()

    suspend fun upsertNoteDb(note: NoteEntity) = notesDao.upsertNote(note)

    suspend fun upsertNotesDb(notes: List<NoteEntity>) {
        notes.forEach { note ->
            upsertNoteDb(note)
        }
    }

    fun observeNoteIdDb(noteId: String) = notesDao.observeNoteId(noteId)


    /// LOCALLY_DELETED_NOTE_ID = uses local database only ///

    // Get all locally_deleted noteIds
    private suspend fun getAllLocallyDeletedNoteIdsDb() =
        notesDao.getAllLocallyDeletedNoteIds()

    // Delete a locally_deleted noteId
    suspend fun deleteLocallyDeletedNoteIdDb(locallyDeleteNoteId: String) =
        notesDao.deleteLocallyDeletedNoteId(locallyDeleteNoteId)

    // Insert a locally_deleted noteId
    private suspend fun insertLocallyDeletedNoteIdDb(locallyDeleteNoteId: String) =
        notesDao.insertLocallyDeletedNoteId(LocallyDeletedNoteId(locallyDeleteNoteId))







    ////////////////////////////////////////////////////
    /// Testing only

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