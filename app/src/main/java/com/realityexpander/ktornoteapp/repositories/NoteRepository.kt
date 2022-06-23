package com.realityexpander.ktornoteapp.repositories

import android.app.Application
import com.google.gson.Gson
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.common.isInternetConnected
import com.realityexpander.ktornoteapp.common.networkBoundResource
import com.realityexpander.ktornoteapp.data.local.NotesDao
import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.remote.NotesApi
import com.realityexpander.ktornoteapp.data.remote.requests.AccountRequest
import com.realityexpander.ktornoteapp.data.remote.requests.DeleteNoteRequest
import com.realityexpander.ktornoteapp.data.remote.responses.BaseSimpleResponse
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponse
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

    /// CACHED
    ///  ≈ Api -> Database --> UI
    ///  ≈ Database -> Api -> Database --> UI (as Flow of Resource)

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
                    notesDao.upsertNote(note.apply { isSynced = true; id = noteId })
                }
            }
        } else {
            // If the server failed to add the note, then attempt to update the local database
            // and set the note as not synced.
            notesDao.upsertNote(note.apply { isSynced = false })
        }
    }

    // Api -> Database --> UI
    suspend fun upsertNotesCached(notes: List<NoteEntity>) {
        notes.forEach { note ->
            upsertNoteCached(note)
        }
    }

    // Database -> Api -> Database --> UI (as Flow of Resource)
    fun getAllNotesCached(): Flow<Resource<List<NoteEntity>>> {
        return networkBoundResource(
            queryDb = {
                notesDao.getAllNotes()
            },
            fetchFromNetwork = {
                notesApi.getNotes()
            },
            saveFetchResponseToDb = { response ->
                if(response.isSuccessful && response.body() != null) {
                    upsertNotesCached(response.body()!!.data ?: emptyList())

                    return@networkBoundResource
                }

                throw Exception("Error getting notes from API, response: code="
                        + response.code() + ", "
                        + response.message() + ", "
                        + response.errorBody()?.string()
                )
            },
            shouldFetch = { _->
                isInternetConnected(context)
            },
            debugNetworkResponseType = { response ->
                println(response)
            },
            debugDatabaseResultType = { result ->
                println(result)
            }
        )
    }



    /// REMOTE = to/from API Only ///

    suspend fun registerApi(email: String, password: String): Resource<SimpleResponse> =
        callApi {
            notesApi.register(AccountRequest(email, password))
        }

    suspend fun loginApi(email: String, password: String): Resource<SimpleResponse> =
        callApi {
            notesApi.login(AccountRequest(email, password))
        }

    // Gets all notes for the authenticated user
    suspend fun getAllNotesApi() =
        callApi {
            notesApi.getNotes()
        }

    suspend fun deleteNoteApi(deleteNoteId: String) =
        callApi {
            notesApi.deleteNote(DeleteNoteRequest(deleteNoteId))
        }

    suspend fun getOwnerIdForEmail(email: String?): String? {
        if(email.isNullOrBlank()) return null

        val response = callApi {
            notesApi.getOwnerIdForEmail(email)
        }
        if(response.status != Status.SUCCESS) return null

        return response.data?.data
    }

    // Standardized call to the API returns a `Resource.<status>` object
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


    /// LOCAL DATABASE = to/from local database ONLY ///

    //suspend fun getNotes() = notesDao.getAllNotes()
    suspend fun getNoteByIdDb(noteId: String) = notesDao.getNoteById(noteId)


    /// Tests ///

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
            saveFetchResponseToDb = { response ->
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