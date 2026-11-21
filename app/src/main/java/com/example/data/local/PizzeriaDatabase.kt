package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MenuItemEntity::class,
        IngredientEntity::class,
        RecipeItemEntity::class,
        StockTransactionEntity::class,
        CartLineEntity::class,
        OrderEntity::class,
        OrderLineEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PizzeriaDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao

    companion object {
        private const val NAME = "pizzeria.db"

        /** [onCreated] runs once, when the database file is created on the first launch. */
        fun build(context: Context, onCreated: () -> Unit): PizzeriaDatabase =
            Room.databaseBuilder(context, PizzeriaDatabase::class.java, NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) = onCreated()
                })
                .build()
    }
}
