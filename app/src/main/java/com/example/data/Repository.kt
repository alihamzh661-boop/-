package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val storeDao = db.storeDao()
    val productDao = db.productDao()
    val orderDao = db.orderDao()
    val settingDao = db.settingDao()

    // Users
    suspend fun getUserByPhone(phone: String): User? = userDao.getUserByPhone(phone)
    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    fun getAllCaptains(): Flow<List<User>> = userDao.getAllCaptains()
    fun getAllClients(): Flow<List<User>> = userDao.getAllClients()

    // Stores
    fun getAllStores(): Flow<List<Store>> = storeDao.getAllStores()
    fun getStoresByCategory(category: String): Flow<List<Store>> = storeDao.getStoresByCategory(category)
    suspend fun getStoreById(id: Int): Store? = storeDao.getStoreById(id)
    suspend fun insertStore(store: Store): Long = storeDao.insertStore(store)
    suspend fun updateStore(store: Store) = storeDao.updateStore(store)

    // Products
    fun getProductsByStore(storeId: Int): Flow<List<Product>> = productDao.getProductsByStore(storeId)
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(id: Int) = productDao.deleteProduct(id)

    // Orders
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()
    fun getOrdersByClient(clientId: Int): Flow<List<Order>> = orderDao.getOrdersByClient(clientId)
    fun getOrdersByStore(storeId: Int): Flow<List<Order>> = orderDao.getOrdersByStore(storeId)
    fun getOrdersByDriver(driverId: Int): Flow<List<Order>> = orderDao.getOrdersByDriver(driverId)
    fun getAvailableDeliveryOrders(): Flow<List<Order>> = orderDao.getAvailableDeliveryOrders()
    suspend fun getOrderById(id: Int): Order? = orderDao.getOrderById(id)
    suspend fun getItemsForOrder(orderId: Int): List<OrderItem> = orderDao.getItemsForOrder(orderId)

    suspend fun placeOrder(order: Order, items: List<OrderItem>): Long {
        val orderId = orderDao.insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId.toInt()) }
        orderDao.insertOrderItems(itemsWithId)
        return orderId
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }

    // Settings
    suspend fun insertSetting(key: String, value: String) {
        settingDao.insertSetting(DeliverySetting(key, value))
    }
    suspend fun getSettingValue(key: String, default: String): String {
        return settingDao.getSetting(key)?.value ?: default
    }
    fun getAllSettings(): Flow<List<DeliverySetting>> = settingDao.getAllSettings()

    // Seed Data
    suspend fun initializeDataIfEmpty() {
        val storesList = storeDao.getAllStores().first()
        if (storesList.isEmpty()) {
            // 1. Seed standard users
            val clientOwnerId = userDao.insertUser(User(phone = "777777777", name = "علي الشعيبي", role = "CLIENT"))
            val storeOwnerId1 = userDao.insertUser(User(phone = "771111111", name = "مالك العوابل", role = "STORE"))
            val storeOwnerId2 = userDao.insertUser(User(phone = "771111112", name = "صيدلي الشعيب", role = "STORE"))
            val storeOwnerId3 = userDao.insertUser(User(phone = "771111113", name = "أبو غازي الشعيبي", role = "STORE"))
            val storeOwnerId4 = userDao.insertUser(User(phone = "771111114", name = "بقالة العاصمة", role = "STORE"))
            val storeOwnerId5 = userDao.insertUser(User(phone = "771111115", name = "مكتب مرسول الشعيب", role = "STORE"))
            val driverOwnerId = userDao.insertUser(User(phone = "772222222", name = "صالح الكابتن", role = "CAPTAIN"))
            
            // Seed Admin
            userDao.insertUser(User(phone = "admin", name = "مدير المنصة", role = "ADMIN"))

            // 2. Seed Stores
            val storeId1 = storeDao.insertStore(Store(ownerId = storeOwnerId1.toInt(), name = "مطعم العوابل", category = "مطاعم", phone = "771111111", address = "العوابل - الشارع العام"))
            val storeId2 = storeDao.insertStore(Store(ownerId = storeOwnerId2.toInt(), name = "صيدلية الشعيب", category = "صيدلية", phone = "771111112", address = "العوابل - بجانب المستشفى"))
            val storeId3 = storeDao.insertStore(Store(ownerId = storeOwnerId3.toInt(), name = "محطة الشعيب للغاز", category = "غاز", phone = "771111113", address = "الشعيب - المخرج الجنوبي"))
            val storeId4 = storeDao.insertStore(Store(ownerId = storeOwnerId4.toInt(), name = "سوبر ماركت العاصمة", category = "بقالة", phone = "771111114", address = "مريس - مفرق الشعيب"))
            val storeId5 = storeDao.insertStore(Store(ownerId = storeOwnerId5.toInt(), name = "مرسول الشعيب الخاص", category = "مرسول خاص", phone = "771111115", address = "الشعيب - خدمة عامة"))

            // 3. Seed Products
            // Restaurant Items
            productDao.insertProduct(Product(storeId = storeId1.toInt(), name = "نصف حبة مندي دجاج مع الأرز", price = 3500.0, category = "وجبات رئيسية"))
            productDao.insertProduct(Product(storeId = storeId1.toInt(), name = "برمة لحم بلدي فاخر", price = 8000.0, category = "وجبات رئيسية"))
            productDao.insertProduct(Product(storeId = storeId1.toInt(), name = "سلتة شعيبية حامية", price = 1500.0, category = "شعبيات"))
            productDao.insertProduct(Product(storeId = storeId1.toInt(), name = "مخبازة روتي ساخن (5 حبات)", price = 500.0, category = "مخبوزات"))

            // Pharmacy Items
            productDao.insertProduct(Product(storeId = storeId2.toInt(), name = "بندول اكسترا مسكن آلام", price = 800.0, category = "أدوية عامة"))
            productDao.insertProduct(Product(storeId = storeId2.toInt(), name = "فيتامين سي فوار مستورد", price = 2500.0, category = "مكملات"))
            productDao.insertProduct(Product(storeId = storeId2.toInt(), name = "صندوق إسعافات أولية متكامل", price = 12000.0, category = "مستلزمات"))

            // Gas Items
            productDao.insertProduct(Product(storeId = storeId3.toInt(), name = "إسطوانة غاز منزلي ممتلئة", price = 7500.0, category = "غاز منزلي"))

            // Grocery Items
            productDao.insertProduct(Product(storeId = storeId4.toInt(), name = "كيس أرز بسمتي 5 كيلو", price = 6500.0, category = "مواد غذائية"))
            productDao.insertProduct(Product(storeId = storeId4.toInt(), name = "حليب ممتز علبة كبيرة", price = 1200.0, category = "البان"))
            productDao.insertProduct(Product(storeId = storeId4.toInt(), name = "علبة تونة درجة أولى", price = 900.0, category = "معلبات"))

            // Special Messenger
            productDao.insertProduct(Product(storeId = storeId5.toInt(), name = "توصيل طرد أو وثيقة مستعجلة", price = 3000.0, category = "خدمات شخصية"))
            productDao.insertProduct(Product(storeId = storeId5.toInt(), name = "قضاء معاملة حكومية سريعة", price = 10000.0, category = "خدمات شخصية"))

            // 4. Seed Settings
            settingDao.insertSetting(DeliverySetting("standard_delivery_fee", "1500"))
            settingDao.insertSetting(DeliverySetting("distance_per_km_fee", "300"))
            settingDao.insertSetting(DeliverySetting("commission_rate", "10")) // 10% commission
        }
    }
}
