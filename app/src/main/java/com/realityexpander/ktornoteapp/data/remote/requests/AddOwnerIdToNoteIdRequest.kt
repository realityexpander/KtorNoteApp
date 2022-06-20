package com.realityexpander.ktornoteapp.data.remote.requests

data class AddOwnerIdToNoteIdRequest(
    val noteId: String,
    val ownerIdToAdd: String,
)
