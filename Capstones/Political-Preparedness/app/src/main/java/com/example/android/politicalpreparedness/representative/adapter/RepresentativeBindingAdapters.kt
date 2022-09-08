package com.example.android.politicalpreparedness.representative.adapter

import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.representative.model.Representative

/**
 * Instead of setting up a list binding in the fragment, we can execute one here and specify
 * the items in the list from the View Model in the XML file directly into the Recycler View
 */
@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Representative>?) {
    val adapter = recyclerView.adapter as RepresentativeListAdapter
    adapter.submitList(data)
}

@BindingAdapter("profileImage")
fun fetchImage(view: ImageView, src: String?) {
    src?.let {
        val uri = src.toUri().buildUpon().scheme("https").build()
        Glide.with(view.context)
            .load(uri)
            .transform(CircleCrop())
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .into(view)
    }
}

@BindingAdapter("stateValue")
fun Spinner.setNewValue(value: String?) {
    if(value != null && value.isNotBlank()) {
        val adapter = toTypedAdapter<String>(this.adapter as ArrayAdapter<*>)
        val position = when (adapter.getItem(0)) {
            is String -> adapter.getPosition(value)
            else -> this.selectedItemPosition
        }
        if (position >= 0) {
            setSelection(position)
        }
    }
}

inline fun <reified T> toTypedAdapter(adapter: ArrayAdapter<*>): ArrayAdapter<T>{
    return adapter as ArrayAdapter<T>
}
