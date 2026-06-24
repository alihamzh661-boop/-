package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val name: String,
    val role: String, // CLIENT, STORE, CAPTAIN, ADMIN
    val status: String = "ACTIVE", // ACTIVE, DISABLED
    val earnings: Double = 0.0
)

@Entity(tableName = "stores")
data class Store(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ownerId: Int,
    val name: String,
    val category: String, // مطاعم, بقالة, صيدلية, مرسول خاص, غاز
    val phone: String,
    val status: String = "ACTIVE", // ACTIVE, DISABLED
    val address: String = "الشعيب",
    val imageUrl: String = ""
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeId: Int,
    val name: String,
    val price: Double,
    val category: String = "عمومي",
    val imageUrl: String = "",
    val status: String = "AVAILABLE" // AVAILABLE, UNAVAILABLE
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientId: Int,
    val clientName: String,
    val clientPhone: String,
    val storeId: Int,
    val storeName: String,
    val driverId: Int? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val status: String = "PENDING", // PENDING, PREPARING, READY_FOR_PICKUP, ON_THE_WAY, DELIVERED, REJECTED
    val deliveryFee: Double = 1000.0,
    val itemsAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "كاش عند الاستلام",
    val addressText: String = "",
    val gpsCoordinates: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ratingStore: Float = 0.0f,
    val ratingDriver: Float = 0.0f,
    val reviewText: String = ""
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val price: Double,
    val quantity: Int
)

@Entity(tableName = "delivery_settings")
data class DeliverySetting(
    @PrimaryKey val key: String,
    val value: String
)
