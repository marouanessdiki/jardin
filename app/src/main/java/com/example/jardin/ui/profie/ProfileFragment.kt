package com.example.jardin.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.example.jardin.LoginActivity
import com.example.jardin.databinding.FragmentProfileBinding
import com.example.jardin.repository.FirebaseRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: FirebaseRepository
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FirebaseRepository()
        auth = FirebaseAuth.getInstance()

        loadUserProfile()

        binding.btnSignOut.setOnClickListener {
            signOut()
        }
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userProfile = repository.getUserProfile()

                binding.progressBar.visibility = View.GONE

                if (userProfile != null) {
                    binding.tvName.text = userProfile["name"] as? String ?: "N/A"
                    binding.tvEmail.text = userProfile["email"] as? String ?: "N/A"

                    // Set user email in header
                    binding.tvUserEmail.text = auth.currentUser?.email ?: "N/A"
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signOut() {
        repository.signOut()

        // Navigate to login screen
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}