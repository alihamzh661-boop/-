package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = Repository(database)

    // Current Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow("GUEST") // GUEST, CLIENT, STORE, CAPTAIN, ADMIN
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Loaded Data Flows
    val allStores = repository.getAllStores().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCaptains = repository.getAllCaptains().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allClients = repository.getAllClients().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allOrders = repository.getAllOrders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val availableDeliveryOrders = repository.getAvailableDeliveryOrders().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart State: Map of Product to Quantity
    private val _cart = MutableStateFlow<Map<Product, Int>>(emptyMap())
    val cart: StateFlow<Map<Product, Int>> = _cart.asStateFlow()

    // Nav / View State
    private val _selectedCategory = MutableStateFlow("مطاعم")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedStore = MutableStateFlow<Store?>(null)
    val selectedStore: StateFlow<Store?> = _selectedStore.asStateFlow()

    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    // Simulated Tracking State
    private val _driverLocationProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val driverLocationProgress: StateFlow<Float> = _driverLocationProgress.asStateFlow()

    // Registration Temp State
    var tempPhone = ""
    var tempName = ""
    var tempRole = ""
    var tempStoreCategory = ""
    private val _smsCodeSent = MutableStateFlow<String?>(null)
    val smsCodeSent: StateFlow<String?> = _smsCodeSent.asStateFlow()

    // Delivery settings cache
    private val _deliverySettings = MutableStateFlow<Map<String, String>>(emptyMap())
    val deliverySettings: StateFlow<Map<String, String>> = _deliverySettings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDataIfEmpty()
            // Observe settings
            repository.getAllSettings().collect { list ->
                _deliverySettings.value = list.associate { it.key to it.value }
            }
        }
    }

    // Role Switcher (For convenient testing across Client, Store, Captain, Admin)
    fun switchRole(role: String, autoLoginPhone: String? = null) {
        _currentRole.value = role
        _cart.value = emptyMap()
        _selectedStore.value = null
        _activeOrder.value = null
        if (autoLoginPhone != null) {
            viewModelScope.launch {
                val user = repository.getUserByPhone(autoLoginPhone)
                _currentUser.value = user
            }
        } else if (role == "ADMIN") {
            _currentUser.value = User(phone = "admin", name = "مدير المنصة", role = "ADMIN")
        } else if (role == "GUEST") {
            _currentUser.value = null
        }
    }

    // Authentication Actions
    fun login(phone: String, role: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone)
            if (user != null) {
                if (user.status == "DISABLED") {
                    onError("هذا الحساب معطل من قبل الإدارة")
                    return@launch
                }
                if (user.role == role) {
                    _currentUser.value = user
                    _currentRole.value = role
                    onSuccess()
                } else {
                    onError("عذراً، هذا الحساب مسجل كـ ${translateRole(user.role)}")
                }
            } else {
                onError("الرقم غير مسجل. يرجى إنشاء حساب جديد")
            }
        }
    }

    fun startRegister(phone: String, name: String, role: String, storeCategory: String = "") {
        tempPhone = phone
        tempName = name
        tempRole = role
        tempStoreCategory = storeCategory
        // Simulate sending verification code via WhatsApp
        _smsCodeSent.value = "5544" // Fixed activation code for testing convenience
    }

    fun verifySmsCode(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (code == "5544" || code == "1234") {
            viewModelScope.launch {
                val existing = repository.getUserByPhone(tempPhone)
                if (existing != null) {
                    onError("هذا الرقم مسجل بالفعل!")
                    return@launch
                }

                val newUser = User(phone = tempPhone, name = tempName, role = tempRole)
                val newUserId = repository.insertUser(newUser)
                val createdUser = newUser.copy(id = newUserId.toInt())
                
                _currentUser.value = createdUser
                _currentRole.value = tempRole

                // If registering a store owner, automatically create their store
                if (tempRole == "STORE") {
                    repository.insertStore(
                        Store(
                            ownerId = newUserId.toInt(),
                            name = if (tempName.contains("متجر") || tempName.contains("مطعم")) tempName else "متجر $tempName",
                            category = if (tempStoreCategory.isNotEmpty()) tempStoreCategory else "مطاعم",
                            phone = tempPhone
                        )
                    )
                }

                _smsCodeSent.value = null
                onSuccess()
            }
        } else {
            onError("رمز التحقق غير صحيح، يرجى إدخال 5544 للتفعيل")
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentRole.value = "GUEST"
        _cart.value = emptyMap()
        _selectedStore.value = null
        _activeOrder.value = null
    }

    // Cart Actions
    fun addToCart(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val count = currentMap[product] ?: 0
        currentMap[product] = count + 1
        _cart.value = currentMap
    }

    fun removeFromCart(product: Product) {
        val currentMap = _cart.value.toMutableMap()
        val count = currentMap[product] ?: 0
        if (count > 1) {
            currentMap[product] = count - 1
        } else {
            currentMap.remove(product)
        }
        _cart.value = currentMap
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun updateCartQuantity(product: Product, quantity: Int) {
        val currentMap = _cart.value.toMutableMap()
        if (quantity <= 0) {
            currentMap.remove(product)
        } else {
            currentMap[product] = quantity
        }
        _cart.value = currentMap
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        _selectedStore.value = null
    }

    fun selectStore(store: Store?) {
        _selectedStore.value = store
    }

    // Checkout & Order Actions
    fun confirmOrder(addressText: String, gpsCoordinates: String, paymentMethod: String, onSuccess: (Order) -> Unit) {
        val client = _currentUser.value ?: return
        val store = _selectedStore.value ?: return
        val cartItems = _cart.value
        if (cartItems.isEmpty()) return

        viewModelScope.launch {
            val baseFee = getSettingValue("standard_delivery_fee", "1500").toDouble()
            val itemsSum = cartItems.entries.sumOf { it.key.price * it.value }
            val total = itemsSum + baseFee

            val newOrder = Order(
                clientId = client.id,
                clientName = client.name,
                clientPhone = client.phone,
                storeId = store.id,
                storeName = store.name,
                deliveryFee = baseFee,
                itemsAmount = itemsSum,
                totalAmount = total,
                paymentMethod = paymentMethod,
                addressText = addressText,
                gpsCoordinates = gpsCoordinates,
                status = "PENDING"
            )

            val orderItems = cartItems.map {
                OrderItem(
                    orderId = 0,
                    productId = it.key.id,
                    productName = it.key.name,
                    price = it.key.price,
                    quantity = it.value
                )
            }

            val orderId = repository.placeOrder(newOrder, orderItems)
            val savedOrder = newOrder.copy(id = orderId.toInt())
            
            _activeOrder.value = savedOrder
            _cart.value = emptyMap()
            _selectedStore.value = null
            onSuccess(savedOrder)
        }
    }

    // Store Actions
    fun updateOrderStatus(orderId: Int, nextStatus: String) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(status = nextStatus)
            repository.updateOrder(updated)
            
            // Sync current active view order if it matches
            if (_activeOrder.value?.id == orderId) {
                _activeOrder.value = updated
            }

            // Simulate Driver movement coordination
            if (nextStatus == "ON_THE_WAY") {
                startSimulatedTracking()
            }
        }
    }

    // Captain Actions
    fun acceptDeliveryOrder(orderId: Int) {
        val driver = _currentUser.value ?: return
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(
                driverId = driver.id,
                driverName = driver.name,
                driverPhone = driver.phone,
                status = "READY_FOR_PICKUP"
            )
            repository.updateOrder(updated)
            
            if (_activeOrder.value?.id == orderId) {
                _activeOrder.value = updated
            }
        }
    }

    // Live Simulated Tracking
    private fun startSimulatedTracking() {
        _driverLocationProgress.value = 0f
        viewModelScope.launch {
            for (i in 1..20) {
                delay(2000) // update every 2 seconds
                _driverLocationProgress.value = i / 20f
            }
        }
    }

    // Complete review & credit captain
    fun submitReviewAndClose(orderId: Int, ratingStore: Float, ratingDriver: Float, reviewText: String) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(
                ratingStore = ratingStore,
                ratingDriver = ratingDriver,
                reviewText = reviewText
            )
            repository.updateOrder(updated)
            _activeOrder.value = null
        }
    }

    fun selectActiveOrder(order: Order?) {
        _activeOrder.value = order
        if (order?.status == "ON_THE_WAY") {
            startSimulatedTracking()
        }
    }

    // Admin Actions
    fun toggleUserStatus(userId: Int) {
        viewModelScope.launch {
            val user = repository.getUserById(userId) ?: return@launch
            val nextStatus = if (user.status == "ACTIVE") "DISABLED" else "ACTIVE"
            repository.updateUser(user.copy(status = nextStatus))
        }
    }

    fun toggleStoreStatus(storeId: Int) {
        viewModelScope.launch {
            val store = repository.getStoreById(storeId) ?: return@launch
            val nextStatus = if (store.status == "ACTIVE") "DISABLED" else "ACTIVE"
            repository.updateStore(store.copy(status = nextStatus))
        }
    }

    fun updateStoreInfo(store: Store) {
        viewModelScope.launch {
            repository.updateStore(store)
        }
    }

    fun addNewStore(name: String, category: String, phone: String, address: String) {
        viewModelScope.launch {
            // Create dummy owner first
            val ownerId = repository.insertUser(User(phone = phone, name = "مالك $name", role = "STORE"))
            repository.insertStore(Store(ownerId = ownerId.toInt(), name = name, category = category, phone = phone, address = address))
        }
    }

    fun updateSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.insertSetting(key, value)
        }
    }

    fun addProduct(storeId: Int, name: String, price: Double, category: String) {
        viewModelScope.launch {
            repository.insertProduct(Product(storeId = storeId, name = name, price = price, category = category))
        }
    }

    fun editProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    private fun getSettingValue(key: String, default: String): String {
        return _deliverySettings.value[key] ?: default
    }

    fun translateRole(role: String): String {
        return when (role) {
            "CLIENT" -> "عميل"
            "STORE" -> "متجر"
            "CAPTAIN" -> "كابتن توصيل"
            "ADMIN" -> "مدير الإدارة"
            else -> "زائر"
        }
    }
}
