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
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _authenticationStatus = MutableLiveData<Resource<String>>()
    val authenticationStatus: LiveData<Resource<String>> = _authenticationStatus

    fun register(email: String, password: String, repeatedPassword: String) {

        if(email.isEmpty() || password.isEmpty() || repeatedPassword.isEmpty()) {
            _authenticationStatus.postValue(Resource.error(
                "Please enter email and password"
            ))
            return
        }

        if(password != repeatedPassword) {
            _authenticationStatus.postValue(Resource.error(
                "Passwords do not match",
            ))
            return
        }

        _authenticationStatus.postValue( Resource.loading("Registering user...") )
        viewModelScope.launch {
            val result = repository.registerApi(email, password)

            _authenticationStatus.postValue(
                Resource(
                    status = result.status,
                    message = result.message ?: "Unknown error",
                    data = result.data?.message ?: "Unknown error"
                )
            )
        }
    }

    fun login(email: String, password: String) {
        _authenticationStatus.postValue(Resource.loading())

        if(email.isEmpty() || password.isEmpty() ) {
            _authenticationStatus.postValue(Resource.error(
                "Please enter email and password"
            ))
            return
        }

        _authenticationStatus.postValue( Resource.loading(message = "Logging in user...") )
        viewModelScope.launch {
            val result = repository.loginApi(email, password)

            _authenticationStatus.postValue(
                Resource(
                    status = result.status,
                    message = result.message ?: "Unknown error",
                    data = result.data?.message ?: "Unknown error"
                )
            )
        }
    }

    fun showSavingCredentialsFailed() {
        _authenticationStatus.postValue(Resource.error(
            "Saving Credentials failed, please try again."
        ))
    }

    fun getOwnerIdForEmail(authEmail: String?): String? {
        return runBlocking {
            repository.getOwnerIdForEmailApi(authEmail)
        }
    }







    /////// TESTS ///////

    fun testCrossinline() {
        val result = repository.testNetworkBoundResource()
        repository.runTestNetworkBoundResource()
    }

    fun getNotesFromApi() {
        viewModelScope.launch {
            val result = repository.getAllNotesResourceApi()

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