package com.example.data

import com.example.model.Extra
import com.example.model.MenuItem
import com.example.service.Pizzeria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MenuRepository(val pizzeria: Pizzeria, val extras: List<Extra>) {

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    fun findItem(id: Int): MenuItem? = pizzeria.findItem(id)

    fun toggleFavorite(itemId: Int) = _favorites.update { ids ->
        if (itemId in ids) ids - itemId else ids + itemId
    }
}
