package com.example.jardin.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jardin.adapters.PlantAdapter
import com.example.jardin.databinding.FragmentHomeBinding
import com.example.jardin.models.PlantModel
import com.example.jardin.repository.FirebaseRepository
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: FirebaseRepository
    private lateinit var plantAdapter: PlantAdapter
    private val plantsList = mutableListOf<PlantModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = FirebaseRepository()

        setupRecyclerView()
        loadPlants()

        binding.swipeRefresh.setOnRefreshListener {
            loadPlants()
        }
    }

    private fun setupRecyclerView() {
        plantAdapter = PlantAdapter(plantsList) { _ ->
            // Handle plant item click (open details)
            // You can navigate to a detail fragment/activity here
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = plantAdapter
        }
    }

    private fun loadPlants() {
        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val plants = repository.getUserPlants()

                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false

                if (plants.isEmpty()) {
                    binding.tvNoPlants.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.tvNoPlants.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    plantsList.clear()
                    plantsList.addAll(plants)
                    plantAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}