package com.realityexpander.ktornoteapp.ui.auth

import android.content.SharedPreferences
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.data.remote.BasicAuthInterceptor
import com.realityexpander.ktornoteapp.databinding.FragmentAuthBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.common.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthFragment: BaseFragment(R.layout.fragment_auth) {

    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var sharedPref: SharedPreferences

    @Inject
    lateinit var basicAuthInterceptor: BasicAuthInterceptor

    private var curEmail: String? = null
    private var curPassword: String? = null

    private var _binding: FragmentAuthBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(isLoggedIn(sharedPref)) {
            setApiCredentials(basicAuthInterceptor,
                getLoggedInEmail(sharedPref)!!,
                getLoggedInPassword(sharedPref)!!
            )

            navigateToNoteList()
        }

        requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        subscribeToObservers()

        //viewModel.testCrossinline()

        binding.etLoginPassword.onImeDone {
            binding.btnLogin.performClick()
        }

        binding.btnLogin.setOnClickListener {
            curEmail = binding.etLoginEmail.text.toString()
            curPassword = binding.etLoginPassword.text.toString()

            viewModel.login(
                curEmail ?: "",
                curPassword ?: ""
            )

            //findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToNotesListFragment())
        }

        binding.etRegisterPasswordConfirm.onImeDone {
            binding.btnRegister.performClick()
        }

        binding.btnRegister.setOnClickListener {
            curEmail = binding.etRegisterEmail.text.toString()
            curPassword = binding.etRegisterPassword.text.toString()

            viewModel.register(
                curEmail ?: "",
                curPassword ?: "",
                binding.etRegisterPasswordConfirm.text.toString(),
            )
            // findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToRegisterFragment())
        }

    }

    private fun subscribeToObservers() {
        viewModel.authenticationStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        binding.registerProgressBar.visibility = View.GONE
                        binding.loginProgressBar.visibility = View.GONE
                        showSnackbar(resource.data ?: resource.message
                            ?: "Successful Auth Status, but no message sent from server"
                        )

                        // Get the userId of the logged in user
                        val curUserId = viewModel.getOwnerIdForEmail(curEmail)

                        if(saveAuthCredentialsToPrefs(sharedPref,
                                basicAuthInterceptor,
                                curEmail,
                                curPassword,
                                curUserId)
                        ) {
                            // showSnackbar("Credentials saved to shared prefs")
                            navigateToNoteList()
                        } else {
                            viewModel.showSavingCredentialsFailed()
                        }
                    }
                    Status.ERROR -> {
                        binding.registerProgressBar.visibility = View.GONE
                        binding.loginProgressBar.visibility = View.GONE
                        showSnackbar(resource.message ?: "Authentication Status error")
                    }
                    Status.LOADING -> {
                        binding.registerProgressBar.visibility = View.VISIBLE
                        binding.loginProgressBar.visibility = View.VISIBLE
                        showToast(resource.message)
                    }
                }
            }
        })
    }

    private fun navigateToNoteList() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.authFragment, true)  // removes this fragment from the back stack
            .build()

        findNavController().navigate(
            AuthFragmentDirections.actionAuthFragmentToNotesListFragment(),
            navOptions
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}