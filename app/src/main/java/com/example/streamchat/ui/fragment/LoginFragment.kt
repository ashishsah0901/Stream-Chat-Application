package com.example.streamchat.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.streamchat.ui.viewmodels.LoginViewModel
import com.example.streamchat.R
import com.example.streamchat.databinding.FragmentLoginBinding
import com.example.streamchat.util.navigateSafely
import com.example.streamchat.util.Util
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding
        get() = _binding!!

    private val viewModel : LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm.setOnClickListener {
            setUpConnectingUIState()
            viewModel.connectUser(binding.etUsername.text.toString())
        }
        binding.etUsername.addTextChangedListener {
            binding.etUsername.error = null
        }
        subscribeToEvents()
        if(viewModel.getCurrentUser() != null) {
            findNavController().navigateSafely(R.id.action_loginFragment_to_channelFragment)
        }
        if(sharedPref.getString("Name","") != "") {
            showLoggingIn()
            viewModel.connectUser(sharedPref.getString("Name","").toString())
        }else{
            showLogin()
        }
    }

    private fun subscribeToEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginEvent.collect { event ->
                when(event) {
                    is LoginViewModel.LoginEvent.ErrorInputTooShort -> {
                        setUpIdleUIState()
                        binding.etUsername.error = getString(R.string.error_username_too_short, Util.MINIMUM_USERNAME_LENGTH)
                    }
                    is LoginViewModel.LoginEvent.ErrorLogin -> {
                        setUpIdleUIState()
                        Toast.makeText(requireContext(), event.error, Toast.LENGTH_LONG).show()
                    }
                    is LoginViewModel.LoginEvent.Success -> {
                        setUpIdleUIState()
                        sharedPref.edit().putString("Name",binding.etUsername.text.toString()).apply()
                        findNavController().navigateSafely(R.id.action_loginFragment_to_channelFragment)
                    }
                }
            }
        }
    }

    private fun showLoggingIn() {
        binding.progressBar.isVisible = true
        binding.btnConfirm.isVisible = true
        binding.tilUsername.isVisible = false
        binding.tvLogin.isVisible = false
        binding.btnConfirm.text = getString(R.string.loggingIn)
    }

    private fun showLogin() {
        binding.progressBar.isVisible = false
        binding.btnConfirm.isVisible = true
        binding.tilUsername.isVisible = true
        binding.tvLogin.isVisible = true
        binding.btnConfirm.text = getString(R.string.confirm)
    }

    private fun setUpConnectingUIState() {
        binding.progressBar.isVisible = true
        binding.btnConfirm.isEnabled = false
    }

    private fun setUpIdleUIState() {
        binding.progressBar.isVisible = false
        binding.btnConfirm.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}