package com.udacity.shoestore.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.shoestore.models.Shoe

class ShoeListViewModel : ViewModel() {

    private var _shoes: MutableLiveData<List<Shoe>> = MutableLiveData<List<Shoe>>()
    val listOfShoes: LiveData<List<Shoe>>
        get() = _shoes


    fun setShoes() {
        val listOfShoes: MutableList<Shoe> = mutableListOf(
            Shoe(
                name = "Nike",
                size = 10.0,
                company = "Nike",
                description = "Description of this Nike"
            ),
            Shoe(
                name = "Adidas",
                size = 11.0,
                company = "Adidas",
                description = "Description of this Adidas"
            ),
        )

        _shoes.value = listOfShoes
    }

    fun addNewShoe(newShoe: Shoe) {
        // Add the new shoe to the prexisting list
        _shoes.value = _shoes.value?.plus(newShoe) ?: listOf(newShoe)
    }
}