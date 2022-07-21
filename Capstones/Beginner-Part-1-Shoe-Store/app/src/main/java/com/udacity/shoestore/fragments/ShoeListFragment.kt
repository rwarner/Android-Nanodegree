package com.udacity.shoestore.fragments

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.udacity.shoestore.MainActivity
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeListBinding
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.viewmodels.ShoeListViewModel
import kotlinx.android.synthetic.main.listitem_shoe.view.*


class ShoeListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Specifically using DataBinding for LiveData
        val binding: FragmentShoeListBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_shoe_list, container, false
        );

        // Set the floating action button to navigate using the navigation graph action
        binding.fragmentShoeListFloatingActionButton.setOnClickListener {
            it.findNavController()
                .navigate(ShoeListFragmentDirections.actionShoeListFragmentToShoeDetailFragment())
        }
        // Enables having an overflow menu at all
        setHasOptionsMenu(true)


        val activityShoeViewModel: ShoeListViewModel = (activity as MainActivity).viewModel
        binding.shoeViewModel = activityShoeViewModel
        binding.lifecycleOwner = this


        activityShoeViewModel.listOfShoes.observe(this.viewLifecycleOwner, {

            for (curShoe: Shoe in it) {
                // Inflate up a Shoe List Item view into the parent linear layout inside the scroll view
                var shoeItemView = layoutInflater.inflate(
                    R.layout.listitem_shoe,
                    binding.fragmentShoeListLinearInsideScrollView,
                    false
                )

                // Set the text view for each item
                shoeItemView.listitem_shoe_textView.text = curShoe.name

                // Set the shoe size
                shoeItemView.listitem_size_actual_size.text = curShoe.size.toString()

                // Set the text view for each item
                shoeItemView.listitem_shoe_actual_company.text = curShoe.company

                // Add the view to the linear layout inside the scroll view
                binding.fragmentShoeListLinearInsideScrollView.addView(shoeItemView)

            }
        })

        return binding.root
    }

    /**
     * Decides what the overflow items do on selection. Since the menu item ID
     * has the fragment name, it is able to navigate to the selected fragment.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Normally we would match the menu item to the navigation for convenience
//        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController()) || super.onOptionsItemSelected(item)

        // However, onNavDestination overrides all NavOptions
        // So we need to manually navigate to avoid going back to the
        // list with the back button and respect NavOptions by
        // using the Action created
        if (item.itemId == R.id.overflow_logout) {
            requireView().findNavController()
                .navigate(ShoeListFragmentDirections.actionShoeListFragmentToLoginFragment())
        } else {
            return NavigationUI.onNavDestinationSelected(
                item,
                requireView().findNavController()
            ) || super.onOptionsItemSelected(item)
        }
        return true

    }

    /**
     * Enable the overflow options menu utilizing the overflow_menu.xml file
     *
     * on selection of an item, we go to -> onOptionsItemSelected()
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.overflow_menu, menu)
    }
}