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

    suspend fun register(email: String, password: String) = withContext(Dispatchers.IO) {

        callApi {
            notesApi.register(AccountRequest(email, password))
        }

//        try {
//            val retrofitResponse = notesApi.register(AccountRequest(email, password))
//
//            if (retrofitResponse.isSuccessful) {
//                retrofitResponse.body()?.let { apiResponse ->
//                    if (apiResponse.successful) {
//                        return@withContext Resource.success(apiResponse.message, apiResponse)
//                    } else {
//                        return@withContext Resource.error(apiResponse.message, apiResponse)
//                    }
//                } ?: return@withContext Resource.error(
//                    "Something went wrong - missing api response body",
//                    null
//                )
//            }
//
//            // retrofit call was not successful (network error?)
//            // try to get the error message string from the error body
//            var errorMessageFromServer = gson.fromJson(
//                retrofitResponse.errorBody()?.string(),
//                SimpleResponse::class.java
//            ).message
//            if(errorMessageFromServer.isBlank()) {
//                errorMessageFromServer = "Something went wrong - missing error message from server: ${retrofitResponse.message()}"
//            }
//            return@withContext Resource.error(errorMessageFromServer, null)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return@withContext Resource.error(e.message ?: "Unknown error", null)
//        }
    }

    suspend fun getNotesFromApi() = withContext(Dispatchers.IO) {
        callApi {
            notesApi.getNotes()
        }
    }

    suspend fun deleteNoteFromApi() = withContext(Dispatchers.IO) {
        callApi {
            notesApi.deleteNote(DeleteNoteRequest("11223344"))
        }
    }

    private suspend fun <T : BaseSimpleResponse> callApi(
        call: suspend () -> Response<out T>
    ): Resource<T> =
    try {
        val retrofitResponse = call()

        if (retrofitResponse.isSuccessful) {
            retrofitResponse.body()?.let { apiResponse ->
                return if (apiResponse.successful) {
                    Resource.success(apiResponse.message, apiResponse)
                } else {
                    Resource.error(apiResponse.message, apiResponse)
                }
            } ?: Resource.error(
                "Something went wrong - missing api response body",
                null
            )
        }

        // retrofit call was not successful (network error?)
        // try to get the error message string from the error body
        val errorMessageFromServer = try {
            val errorMsg = gson.fromJson(
                retrofitResponse.errorBody()?.string(),
                SimpleResponse::class.java // error messages are always SimpleResponse
            ).message

            if (errorMsg.isBlank()) {
                "Missing server error message: ${retrofitResponse.message()}"
            }

            errorMsg
        } catch (e: Exception) {
            e.printStackTrace()

            "Server Error message: ${retrofitResponse.message()}"
        }

        Resource.error(errorMessageFromServer, null)
    } catch (e: Exception) {
        e.printStackTrace()

        Resource.error(e.message ?: "Unknown error", null)
    }


    /// Local Database ///

    //suspend fun getNotes() = notesDao.getAllNotes()
    suspend fun getNote(id: String) = notesDao.getNoteById(id)
}