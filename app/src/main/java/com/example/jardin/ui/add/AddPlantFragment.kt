package com.example.jardin.ui.add

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.jardin.R
import com.example.jardin.databinding.FragmentAddPlantBinding
import com.example.jardin.models.PlantModel
import com.example.jardin.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AddPlantFragment : Fragment() {

    private var _binding: FragmentAddPlantBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: FirebaseRepository
    private var selectedImageUri: Uri? = null

    // Replace deprecated startActivityForResult with ActivityResultLauncher
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null && result.data?.data != null) {
            selectedImageUri = result.data?.data
            binding.ivPlantImage.setImageURI(selectedImageUri)
            binding.ivPlantImage.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FirebaseRepository()

        // Setup sunlight needs dropdown
        val sunlightOptions = arrayOf("Full Sun", "Partial Sun", "Shade")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sunlightOptions)
        binding.autoCompleteSunlight.setAdapter(adapter)

        // Setup click listeners
        binding.btnAddImage.setOnClickListener {
            openImagePicker()
        }

        binding.btnSave.setOnClickListener {
            savePlant()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(intent)
    }

    private fun savePlant() {
        val name = binding.etPlantName.text.toString()
        val type = binding.etPlantType.text.toString()
        val description = binding.etDescription.text.toString()
        val wateringFrequencyStr = binding.etWateringFrequency.text.toString()
        val sunlightNeeds = binding.autoCompleteSunlight.text.toString()

        if (name.isEmpty() || type.isEmpty() || wateringFrequencyStr.isEmpty() || sunlightNeeds.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val wateringFrequency = wateringFrequencyStr.toIntOrNull() ?: 0

        val plant = PlantModel(
            name = name,
            type = type,
            description = description,
            wateringFrequency = wateringFrequency,
            sunlightNeeds = sunlightNeeds
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val success = repository.addPlant(plant, selectedImageUri)

                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Plant added successfully", Toast.LENGTH_SHORT).show()
                    resetForm()
                } else {
                    Toast.makeText(requireContext(), "Failed to add plant", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetForm() {
        binding.etPlantName.text?.clear()
        binding.etPlantType.text?.clear()
        binding.etDescription.text?.clear()
        binding.etWateringFrequency.text?.clear()
        binding.autoCompleteSunlight.text?.clear()
        binding.ivPlantImage.setImageURI(null)
        binding.ivPlantImage.visibility = View.GONE
        selectedImageUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}