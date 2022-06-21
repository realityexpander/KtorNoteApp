package com.realityexpander.ktornoteapp.data.remote

import com.realityexpander.ktornoteapp.data.local.entities.NoteEntity
import com.realityexpander.ktornoteapp.data.remote.requests.AccountRequest
import com.realityexpander.ktornoteapp.data.remote.requests.AddOwnerIdToNoteIdRequest
import com.realityexpander.ktornoteapp.data.remote.requests.DeleteNoteRequest
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponse
import com.realityexpander.ktornoteapp.data.remote.responses.SimpleResponseWithData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface NotesApi {

    @POST("/register")
    suspend fun register(
        @Body registerRequest: AccountRequest,  // @Body means the registerRequest is the body of the request and will be converted to a JSON object string
    ): Response<SimpleResponse>

    @POST("/login")
    suspend fun login(
        @Body loginRequest: AccountRequest,  // @Body means the loginRequest is the body of the request and will be converted to a JSON object string
    ): Response<SimpleResponse>

    @POST("/saveNote")
    suspend fun addNote(
        @Body note: NoteEntity,  // @Body means the note is the body of the request and will be converted to a JSON object string
    ): Response<SimpleResponseWithData<NoteEntity>>

    @POST("/deleteNote")
    suspend fun deleteNote(
        @Body deleteNote: DeleteNoteRequest,  // @Body means the deleteNote is the body of the request and will be converted to a JSON object string
    ): Response<SimpleResponseWithData<NoteEntity>>

    @POST("/addOwnerIdToNoteId")
    suspend fun addOwnerIdToNoteId(
        @Body addOwnerIdToNoteId: AddOwnerIdToNoteIdRequest,  // @Body means the addOwnerIdToNoteId is the body of the request and will be converted to a JSON object string
    ): Response<SimpleResponseWithData<NoteEntity>>

    @GET("/getNotes")
    suspend fun getNotes(): Response<SimpleResponseWithData<List<NoteEntity>>>
}