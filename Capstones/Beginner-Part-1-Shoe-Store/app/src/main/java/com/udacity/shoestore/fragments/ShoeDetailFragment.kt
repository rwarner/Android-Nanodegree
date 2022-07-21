package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeDetailBinding
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.viewmodels.ShoeListViewModel

class ShoeDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate databinding against the shoe detail fragment
        val binding: FragmentShoeDetailBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_shoe_detail, container, false
        );

        // Required to initialize a blank shoe into the variable of the layout for databinding
        binding.shoe = Shoe(name = "",
        0.0,
        "",
        "")

        // Opt to use the activity view models accessor with Kotlin
        val activityShoeViewModel: ShoeListViewModel by activityViewModels()
//        val activityShoeViewModel: ShoeListViewModel = (activity as MainActivity).viewModel

        binding.cancelButton.setOnClickListener {
            it.findNavController()
                .navigate(ShoeDetailFragmentDirections.actionShoeDetailFragmentToShoeListFragment().actionId)
        }

        binding.saveButton.setOnClickListener {

            if (binding.shoeNameEditText.text.isEmpty() || binding.shoeNameEditText.text.isBlank()) {
                binding.shoeNameEditText.setError("Must not be blank")
            } else {
                activityShoeViewModel.addNewShoe(binding.shoe!!)

                // Navigate back to the list
                it.findNavController()
                    .navigate(ShoeDetailFragmentDirections.actionShoeDetailFragmentToShoeListFragment().actionId)

            }

        }

        return binding.root
    }
}