package com.realityexpander.ktornoteapp.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.ktornoteapp.common.Resource
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _registerStatus = MutableLiveData<Resource<String>>()
    val registerStatus: LiveData<Resource<String>> = _registerStatus

    fun register(email: String, password: String, repeatedPassword: String) {
        _registerStatus.postValue(Resource.loading())

        if(email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
            _registerStatus.postValue(Resource.error(
                "Please enter email and password"
            ))
            return
        }

        if(password != repeatedPassword) {
            _registerStatus.postValue(Resource.error(
                "Passwords do not match",
            ))
            return
        }

//        _registerStatus.value = Resource.loading("Registering user...")
        _registerStatus.postValue( Resource.loading("Registering user...") )

        viewModelScope.launch {

            val result = repository.register(email, password)

            _registerStatus.postValue(
                Resource(
                    status = result.status,
                    message = result.message ?: "Unknown error",
                    data = result.data?.message ?: "Unknown error"
                )
            )

        }
    }

    fun getNotesFromApi() {
        viewModelScope.launch {
            val result = repository.getNotesFromApi()

            when(result.status) {
                Status.SUCCESS -> {
                    println(result.data)
                }
                Status.ERROR -> {
                    println(result.message +", "+ result.statusCode)
                }
                else -> {
                    println("Unknown error")
                }
            }

        }
    }

}