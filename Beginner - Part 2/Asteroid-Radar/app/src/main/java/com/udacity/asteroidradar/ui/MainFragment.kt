package com.udacity.asteroidradar.ui

import android.os.Bundle
import android.view.*
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.AsteroidItemBinding
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.viewmodels.AsteroidViewModel


class MainFragment : Fragment() {

    /**
     * One way to delay creation of the viewModel until an appropriate lifecycle method is to use
     * lazy. This requires that viewModel not be referenced before onViewCreated(), which we
     * do in this Fragment.
     */
    private val viewModel: AsteroidViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated()"
        }

        ViewModelProvider(
            this,
            AsteroidViewModel.Factory(activity.application)
        )[AsteroidViewModel::class.java]

    }

    /**
     * RecyclerView Adapter for converting a list of Asteroids to cards.
     */
    private var viewModelAdapter: AsteroidAdapter? = null


    /**
     * Called immediately after onCreateView() has returned, and fragment's
     * view hierarchy has been created.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.listOfAsteroids.observe(
            viewLifecycleOwner,
            Observer<List<Asteroid>> { asteroids ->
                asteroids?.apply {
                    viewModelAdapter?.asteroids = asteroids
                }
            })
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Return the View for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentMainBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_main,
            container,
            false
        )
        // Set the lifecycleOwner so DataBinding can observe LiveData
        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModelAdapter = AsteroidAdapter(AsteroidClick {
            // When an asteroid is clicked this block will be called by AsteroidAdapter
            if ( it != null ) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
            }

        })

        binding.root.findViewById<RecyclerView>(R.id.asteroid_recycler).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = viewModelAdapter
        }

        return binding.root
    }
}

/**
 * Click listener for Asteroids. By giving the block a name it helps a reader understand what it does.
 *
 */
class AsteroidClick(val block: (Asteroid) -> Unit) {
    /**
     * Called when a video is clicked
     *
     * @param video the video that was clicked
     */
    fun onClick(asteroid: Asteroid) = block(asteroid)
}

/**
 * RecyclerView Adapter for setting up data binding on the items in the list.
 */
class AsteroidAdapter(val callback: AsteroidClick) : RecyclerView.Adapter<AsteroidViewHolder>() {

    /**
     * The asteroids that our Adapter will show
     */
    var asteroids: List<Asteroid> = emptyList()
        set(value) {
            field = value
            // For an extra challenge, update this to use the paging library.

            // Notify any registered observers that the data set has changed. This will cause every
            // element in our RecyclerView to be invalidated.
            notifyDataSetChanged()
        }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsteroidViewHolder {
        val withDataBinding: AsteroidItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            AsteroidViewHolder.LAYOUT,
            parent,
            false)
        return AsteroidViewHolder(withDataBinding)
    }

    override fun getItemCount() = asteroids.size

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     */
    override fun onBindViewHolder(holder: AsteroidViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.asteroid = asteroids[position]
            it.asteroidCallback = callback
        }
    }

}

/**
 * ViewHolder for Asteroids. All work is done by data binding.
 */
class AsteroidViewHolder(val viewDataBinding: AsteroidItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.asteroid_item
    }
}