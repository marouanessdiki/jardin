package com.example.jardin.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jardin.R
import com.example.jardin.databinding.ItemPlantBinding
import com.example.jardin.models.PlantModel

class PlantAdapter(
    private val plants: List<PlantModel>,
    private val onPlantClick: (PlantModel) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val binding = ItemPlantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        holder.bind(plants[position])
    }

    override fun getItemCount(): Int = plants.size

    inner class PlantViewHolder(private val binding: ItemPlantBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPlantClick(plants[position])
                }
            }
        }

        fun bind(plant: PlantModel) {
            binding.tvPlantName.text = plant.name
            binding.tvPlantType.text = plant.type
            binding.tvWateringFrequency.text = "Water every ${plant.wateringFrequency} days"

            // Load image if available
            if (plant.imageUrl.isNotEmpty()) {
                Glide.with(binding.ivPlant.context)
                    .load(plant.imageUrl)
                    .placeholder(R.drawable.ic_plant_placeholder)
                    .into(binding.ivPlant)
            } else {
                binding.ivPlant.setImageResource(R.drawable.ic_plant_placeholder)
            }
        }
    }
}