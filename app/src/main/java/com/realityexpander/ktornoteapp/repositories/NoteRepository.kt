package com.realityexpander.ktornoteapp.repositories

import android.app.Application
import com.google.gson.Gson
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.data.local.NotesDao
import com.realityexpander.ktornoteapp.data.remote.NotesApi
import com.realityexpander.ktornoteapp.data.remote.requests.AccountRequest
import com.realityexpander.ktornoteapp.data.remote.requests.DeleteNoteRequest
import com.realityexpander.ktornoteapp.data.remote.responses.BaseSimpleResponse
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponse
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.fromValue
import kotlinx.coroutines.Dispatchers
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
    /// Remote API ///

    suspend fun register(email: String, password: String) =
        callApi {
            notesApi.register(AccountRequest(email, password))
        }

    suspend fun login(email: String, password: String) =
        callApi {
            notesApi.login(AccountRequest(email, password))
        }

    suspend fun getNotesFromApi() =
        callApi {
            notesApi.getNotes()
        }

    suspend fun deleteNoteFromApi(deleteNoteId: String) =
        callApi {
            notesApi.deleteNote(DeleteNoteRequest(deleteNoteId))
        }

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


    /// Local Database ///

    //suspend fun getNotes() = notesDao.getAllNotes()
    suspend fun getNote(id: String) = notesDao.getNoteById(id)
}