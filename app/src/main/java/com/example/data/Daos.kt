package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE role = 'CAPTAIN'")
    fun getAllCaptains(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE role = 'CLIENT'")
    fun getAllClients(): Flow<List<User>>
}

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: Store): Long

    @Update
    suspend fun updateStore(store: Store)

    @Query("SELECT * FROM stores")
    fun getAllStores(): Flow<List<Store>>

    @Query("SELECT * FROM stores WHERE category = :category")
    fun getStoresByCategory(category: String): Flow<List<Store>>

    @Query("SELECT * FROM stores WHERE id = :id LIMIT 1")
    suspend fun getStoreById(id: Int): Store?
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)

    @Query("SELECT * FROM products WHERE storeId = :storeId")
    fun getProductsByStore(storeId: Int): Flow<List<Product>>
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getOrdersByClient(clientId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getOrdersByStore(storeId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId ORDER BY timestamp DESC")
    fun getOrdersByDriver(driverId: Int): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = 'READY_FOR_PICKUP' OR status = 'PENDING' ORDER BY timestamp DESC")
    fun getAvailableDeliveryOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItem>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsForOrder(orderId: Int): List<OrderItem>
}

@Dao
interface SettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: DeliverySetting)

    @Query("SELECT * FROM delivery_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): DeliverySetting?

    @Query("SELECT * FROM delivery_settings")
    fun getAllSettings(): Flow<List<DeliverySetting>>
}
