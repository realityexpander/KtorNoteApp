package com.realityexpander.ktornoteapp.ui.auth

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.realityexpander.ktornoteapp.R
import com.realityexpander.ktornoteapp.common.Status
import com.realityexpander.ktornoteapp.databinding.FragmentAuthBinding
import com.realityexpander.ktornoteapp.ui.BaseFragment
import com.realityexpander.ktornoteapp.ui.common.onImeDone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment: BaseFragment(R.layout.fragment_auth) {

    private val viewModel: AuthViewModel by viewModels()

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

        requireActivity().requestedOrientation = SCREEN_ORIENTATION_PORTRAIT

        subscribeToObservers()

        binding.btnLogin.setOnClickListener {
            viewModel.getNotesFromApi()
//            viewModel.deleteNoteFromApi()

            //findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToNotesListFragment())
        }

        binding.etRegisterPasswordConfirm.onImeDone {
            binding.btnRegister.performClick()
        }

        binding.btnRegister.setOnClickListener {
            viewModel.register(
                binding.etRegisterEmail.text.toString(),
                binding.etRegisterPassword.text.toString(),
                binding.etRegisterPasswordConfirm.text.toString(),
            )
            // findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToRegisterFragment())
        }

    }

    private fun subscribeToObservers() {
        viewModel.registerStatus.observe(viewLifecycleOwner, Observer { result ->
            result?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        binding.registerProgressBar.visibility = View.GONE
                        showSnackbar(resource.data ?: resource.message ?: "Register Status error: No message sent from server")
                        //findNavController().navigate(AuthFragmentDirections.actionAuthFragmentToNotesListFragment())
                    }
                    Status.ERROR -> {
                        binding.registerProgressBar.visibility = View.GONE
                        showSnackbar(resource.message ?: "registerStatus error")
                    }
                    Status.LOADING -> {
                        binding.registerProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}