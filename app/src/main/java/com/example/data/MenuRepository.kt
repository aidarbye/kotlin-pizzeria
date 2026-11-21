package com.example.data

import com.example.data.local.MenuDao
import com.example.data.local.toEntity
import com.example.data.local.toModel
import com.example.model.Extra
import com.example.model.MenuItem
import com.example.model.RecipeEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenuRepository(
    private val menuDao: MenuDao,
    val extras: List<Extra>,
    private val externalScope: CoroutineScope
) {
    private val entities = menuDao.observeAll()

    val menu: Flow<List<MenuItem>> = entities.map { list -> list.map { it.toModel() } }

    val favorites: Flow<Set<Int>> = entities.map { list -> list.filter { it.isFavorite }.map { it.id }.toSet() }

    /** Recipes of all dishes, grouped by dish id. */
    val recipes: Flow<Map<Int, List<RecipeEntry>>> =
        menuDao.observeRecipes().map { list -> list.map { it.toModel() }.groupBy { it.menuItemId } }

    fun item(id: Int): Flow<MenuItem?> = menuDao.observeById(id).map { it?.toModel() }

    suspend fun getItem(id: Int): MenuItem? = menuDao.getById(id)?.toModel()

    suspend fun getRecipe(id: Int): List<RecipeEntry> = menuDao.getRecipe(id).map { it.toModel() }

    /** Creates the dish when its id is 0, otherwise updates it. Returns the id of the saved dish. */
    suspend fun save(item: MenuItem, recipe: List<RecipeEntry>): Int = externalScope.runDetached {
        menuDao.saveWithRecipe(item.toEntity(), recipe.map { it.toEntity() })
    }

    suspend fun delete(id: Int) = externalScope.runDetached { menuDao.deleteById(id) }

    suspend fun toggleFavorite(id: Int) = externalScope.runDetached { menuDao.toggleFavorite(id) }
}
