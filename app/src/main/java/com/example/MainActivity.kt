package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Force RTL Layout to natively support Arabic flow as requested
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color(0xFFF2F4F7) // Geometric Slate-Light Background
                    ) { innerPadding ->
                        AppContent(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val cart by viewModel.cart.collectAsState()
    
    val context = LocalContext.current

    // Navigation sub-tab inside roles
    var clientTab by remember { mutableStateOf("HOME") } // HOME, CART, ORDERS, PROFILE
    var storeTab by remember { mutableStateOf("QUEUE") } // QUEUE, PRODUCTS
    var captainTab by remember { mutableStateOf("DELIVERIES") } // DELIVERIES, MAP, EARNINGS
    var adminTab by remember { mutableStateOf("STATS") } // STATS, STORES, CAPTAINS, SETTINGS

    Column(modifier = modifier.fillMaxSize()) {
        // 1. Role Quick-Switcher & Brand Header
        HeaderBar(
            currentRole = currentRole,
            currentUser = currentUser,
            onRoleChange = { targetRole ->
                // Auto login sample users based on selected role for fast testing
                when (targetRole) {
                    "CLIENT" -> viewModel.switchRole("CLIENT", "777777777")
                    "STORE" -> viewModel.switchRole("STORE", "771111111")
                    "CAPTAIN" -> viewModel.switchRole("CAPTAIN", "772222222")
                    "ADMIN" -> viewModel.switchRole("ADMIN", "admin")
                    else -> viewModel.switchRole("GUEST")
                }
                Toast.makeText(context, "تم التبديل إلى واجهة: ${viewModel.translateRole(targetRole)}", Toast.LENGTH_SHORT).show()
            },
            onLogout = { viewModel.logout() }
        )

        // 2. Main Workspace depending on current state
        Box(modifier = Modifier.weight(1f)) {
            when (currentRole) {
                "GUEST" -> GuestAuthScreen(viewModel)
                "CLIENT" -> ClientScreen(viewModel, clientTab, onChangeTab = { clientTab = it })
                "STORE" -> StoreScreen(viewModel, storeTab, onChangeTab = { storeTab = it })
                "CAPTAIN" -> CaptainScreen(viewModel, captainTab, onChangeTab = { captainTab = it })
                "ADMIN" -> AdminScreen(viewModel, adminTab, onChangeTab = { adminTab = it })
            }
        }

        // 3. Dynamic Bottom Navigation Bar matching current role
        if (currentRole != "GUEST") {
            BottomNavBar(
                role = currentRole,
                activeTab = when (currentRole) {
                    "CLIENT" -> clientTab
                    "STORE" -> storeTab
                    "CAPTAIN" -> captainTab
                    "ADMIN" -> adminTab
                    else -> ""
                },
                cartCount = cart.values.sum(),
                onTabSelect = { tab ->
                    when (currentRole) {
                        "CLIENT" -> clientTab = tab
                        "STORE" -> storeTab = tab
                        "CAPTAIN" -> captainTab = tab
                        "ADMIN" -> adminTab = tab
                    }
                }
            )
        }
    }
}

// ==================== COMPONENTS ====================

@Composable
fun HeaderBar(
    currentRole: String,
    currentUser: User?,
    onRoleChange: (String) -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title & Brand info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF002D62), Color(0xFF1E3A8A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalShipping,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الشعيب إكسبريس",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF002D62)
                        )
                        Text(
                            text = "AL-SHUAIB EXPRESS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Logged-in profile badge
                if (currentUser != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "User",
                            tint = Color(0xFF002D62),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentUser.name.take(12),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002D62)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ExitToApp,
                                contentDescription = "logout",
                                tint = Color.Red,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fast Role Switcher bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF2F4F7), RoundedCornerShape(16.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val roles = listOf("CLIENT", "STORE", "CAPTAIN", "ADMIN")
                roles.forEach { role ->
                    val isSelected = currentRole == role
                    val btnBg = if (isSelected) Color(0xFF002D62) else Color.Transparent
                    val btnFg = if (isSelected) Color.White else Color(0xFF64748B)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(btnBg)
                            .clickable { onRoleChange(role) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (role) {
                                "CLIENT" -> "العميل"
                                "STORE" -> "المتجر"
                                "CAPTAIN" -> "الكابتن"
                                "ADMIN" -> "الإدارة"
                                else -> ""
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = btnFg
                        )
                    }
                }
            }
        }
    }
}

// ==================== AUTHENTICATION SCREENS ====================

@Composable
fun GuestAuthScreen(viewModel: AppViewModel) {
    var isRegister by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("CLIENT") }
    var storeCategory by remember { mutableStateOf("مطاعم") }
    var smsInput by remember { mutableStateOf("") }
    
    val smsCodeSent by viewModel.smsCodeSent.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Big Logo
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF002D62), Color(0xFF1E3A8A))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalShipping,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "مرحباً بك في الشعيب إكسبريس",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF002D62)
        )
        Text(
            text = "توصيل وخدمات سريعة وموثوقة لقرى مديرية الشعيب",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (smsCodeSent == null) {
                    // Step 1: Input details
                    Text(
                        text = if (isRegister) "إنشاء حساب جديد" else "تسجيل الدخول",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002D62)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (77xxxxxxx)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (isRegister) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("الاسم الكامل") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "نوع الحساب:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf("CLIENT" to "عميل", "STORE" to "متجر", "CAPTAIN" to "كابتن").forEach { (r, label) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = role == r, onClick = { role = r })
                                    Text(text = label, fontSize = 12.sp)
                                }
                            }
                        }

                        if (role == "STORE") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "تصنيف المتجر:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val cats = listOf("مطاعم", "بقالة", "صيدلية", "مرسول خاص", "غاز")
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                                cats.forEach { c ->
                                    FilterChip(
                                        selected = storeCategory == c,
                                        onClick = { storeCategory = c },
                                        label = { Text(c) },
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (phone.isEmpty()) {
                                Toast.makeText(context, "الرجاء إدخال رقم الهاتف", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (isRegister) {
                                if (name.isEmpty()) {
                                    Toast.makeText(context, "الرجاء إدخال الاسم", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.startRegister(phone, name, role, storeCategory)
                                Toast.makeText(context, "تم إرسال رمز التفعيل للواتساب", Toast.LENGTH_LONG).show()
                            } else {
                                viewModel.login(phone, "CLIENT", 
                                    onSuccess = { Toast.makeText(context, "مرحباً بك مجدداً!", Toast.LENGTH_SHORT).show() },
                                    onError = { 
                                        // Try auto login for store/driver/admin also as fallback
                                        viewModel.login(phone, "STORE", 
                                            onSuccess = { Toast.makeText(context, "مرحباً بك كشريك متجر", Toast.LENGTH_SHORT).show() },
                                            onError = {
                                                viewModel.login(phone, "CAPTAIN",
                                                    onSuccess = { Toast.makeText(context, "مرحباً بك يا كابتن الشعيب", Toast.LENGTH_SHORT).show() },
                                                    onError = { err -> Toast.makeText(context, err, Toast.LENGTH_LONG).show() }
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                    ) {
                        Text(text = if (isRegister) "تأكيد وإرسال رمز واتساب" else "تسجيل الدخول")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { isRegister = !isRegister },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isRegister) "لديك حساب بالفعل؟ سجل دخولك" else "ليس لديك حساب؟ سجل الآن",
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp
                        )
                    }

                } else {
                    // Step 2: SMS Verification Simulation
                    Text(
                        text = "كود تأكيد الرقم (الواتساب)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002D62)
                    )
                    Text(
                        text = "وصلك رمز التحقق عبر الواتس آب إلى الرقم $phone. يرجى إدخال (5544) للتفعيل السريع.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = smsInput,
                        onValueChange = { smsInput = it },
                        label = { Text("رمز التحقق (مثال: 5544)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.verifySmsCode(smsInput,
                                onSuccess = { Toast.makeText(context, "تم تسجيل حسابك بنجاح!", Toast.LENGTH_SHORT).show() },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                    ) {
                        Text(text = "تفعيل الحساب")
                    }
                }
            }
        }
    }
}

// ==================== CLIENT SCREEN SYSTEM ====================

@Composable
fun ClientScreen(viewModel: AppViewModel, tab: String, onChangeTab: (String) -> Unit) {
    when (tab) {
        "HOME" -> ClientHomeScreen(viewModel)
        "CART" -> ClientCartScreen(viewModel, onOrderConfirmed = { onChangeTab("ORDERS") })
        "ORDERS" -> ClientOrdersScreen(viewModel)
        "PROFILE" -> ClientProfileScreen(viewModel)
    }
}

@Composable
fun ClientHomeScreen(viewModel: AppViewModel) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val allStores by viewModel.allStores.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()

    val filteredStores = allStores.filter { it.category == selectedCategory && it.status == "ACTIVE" }

    if (selectedStore == null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // 1. Promo Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF002D62), Color(0xFF1E3A8A))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Column {
                            Text(
                                text = "توصيل سريع لكل قرى الشعيب",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "أطلب وجبتك، دواءك، أو أغراضك المنزلية بأمان وسرعة",
                                color = Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Button(
                                onClick = { /* Action */ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF94A3B8)),
                                shape = RoundedCornerShape(50.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("تصفح المتاجر", fontSize = 10.sp, color = Color(0xFF002D62), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Services / Category grid
            item {
                Text(
                    text = "الفئات الخدمية الشعيبية",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF002D62),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                val categories = listOf(
                    Triple("مطاعم", Icons.Rounded.Restaurant, Color(0xFFF97316)),
                    Triple("بقالة", Icons.Rounded.ShoppingCart, Color(0xFF22C55E)),
                    Triple("صيدلية", Icons.Rounded.LocalPharmacy, Color(0xFFEF4444)),
                    Triple("مرسول خاص", Icons.Rounded.LocalShipping, Color(0xFF3B82F6)),
                    Triple("غاز", Icons.Rounded.LocalGasStation, Color(0xFF71717A))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (cat, icon, color) ->
                        val isSel = selectedCategory == cat
                        val bg = if (isSel) Color(0xFF002D62) else Color.White
                        val fg = if (isSel) Color.White else Color(0xFF002D62)
                        
                        Card(
                            onClick = { viewModel.selectCategory(cat) },
                            modifier = Modifier
                                .width(90.dp)
                                .height(85.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = bg),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = cat,
                                    tint = if (isSel) Color.White else color,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
                            }
                        }
                    }
                }
            }

            // 3. Stores List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "متاجر قسم ($selectedCategory)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF002D62)
                    )
                    Text(
                        text = "${filteredStores.size} متاح حالياً",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (filteredStores.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "لا توجد متاجر نشطة في هذا القسم حالياً", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    }
                }
            } else {
                items(filteredStores) { store ->
                    Card(
                        onClick = { viewModel.selectStore(store) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Store,
                                    contentDescription = null,
                                    tint = Color(0xFF002D62),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = store.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = store.address, fontSize = 11.sp, color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = store.phone, fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color(0xFF002D62),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Store Product View Screen
        val productsFlow = viewModel.repository.getProductsByStore(selectedStore!!.id).collectAsState(initial = emptyList())
        val cartItems by viewModel.cart.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header for specific store
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectStore(null) }) {
                    Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = selectedStore!!.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                    Text(text = "موقع المتجر: ${selectedStore!!.address}", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                item {
                    Text(
                        text = "المنتجات والخدمات المعروضة",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                if (productsFlow.value.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("لا توجد منتجات متوفرة حالياً في هذا المتجر", color = Color(0xFF94A3B8), fontSize = 12.sp)
                        }
                    }
                } else {
                    items(productsFlow.value) { product ->
                        val qtyInCart = cartItems[product] ?: 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                    Text(text = "السعر: ${product.price.toInt()} ريال يمني", fontSize = 12.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                                    Text(text = "التصنيف: ${product.category}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (qtyInCart > 0) {
                                        IconButton(
                                            onClick = { viewModel.removeFromCart(product) },
                                            modifier = Modifier.size(30.dp).background(Color(0xFFE2E8F0), CircleShape)
                                        ) {
                                            Text(text = "-", fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                        }
                                        Text(
                                            text = qtyInCart.toString(),
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.addToCart(product) },
                                        modifier = Modifier.size(30.dp).background(Color(0xFF002D62), CircleShape)
                                    ) {
                                        Text(text = "+", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientCartScreen(viewModel: AppViewModel, onOrderConfirmed: () -> Unit) {
    val cart by viewModel.cart.collectAsState()
    val deliverySettings by viewModel.deliverySettings.collectAsState()
    val selectedStore by viewModel.selectedStore.collectAsState()
    val context = LocalContext.current

    var addressText by remember { mutableStateOf("الشعيب - العوابل") }
    var gpsCoordinates by remember { mutableStateOf("13.8824, 44.8213") }
    var paymentMethod by remember { mutableStateOf("كاش عند الاستلام") }

    val baseFee = (deliverySettings["standard_delivery_fee"] ?: "1500").toDouble()
    val itemsAmount = cart.entries.sumOf { it.key.price * it.value }
    val totalAmount = itemsAmount + baseFee

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "سلة المشتريات",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF002D62),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (cart.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Rounded.ShoppingCart, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "سلتك فارغة حالياً. تصفح المتاجر واطلب الآن", color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text(
                        text = "المنتجات المحددة في السلة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                items(cart.entries.toList()) { entry ->
                    val product = entry.key
                    val qty = entry.value
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = "${product.price.toInt()} ريال x $qty", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Text(text = "${(product.price * qty).toInt()} ريال", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "بيانات التوصيل والدفع:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = addressText,
                        onValueChange = { addressText = it },
                        label = { Text("عنوان التوصيل (مثال: قرية القضاة / العوابل)") },
                        modifier = Modifier.fillMaxWidth().testTag("address_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = gpsCoordinates,
                        onValueChange = { gpsCoordinates = it },
                        label = { Text("إحداثيات GPS (اختياري)") },
                        modifier = Modifier.fillMaxWidth().testTag("gps_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "طريقة الدفع التفضيلية:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("كاش عند الاستلام", "تواصل واتساب للدفع الإلكتروني").forEach { method ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = paymentMethod == method, onClick = { paymentMethod = method })
                                Text(text = method, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary Bill card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("قيمة المشتريات:", fontSize = 12.sp)
                                Text("${itemsAmount.toInt()} ريال يمني", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رسوم التوصيل:", fontSize = 12.sp)
                                Text("${baseFee.toInt()} ريال يمني", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الإجمالي الكلي للطلب:", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF002D62))
                                Text("${totalAmount.toInt()} ريال يمني", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF002D62))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (addressText.isEmpty()) {
                                Toast.makeText(context, "الرجاء تحديد موقع التوصيل", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.confirmOrder(addressText, gpsCoordinates, paymentMethod) { order ->
                                Toast.makeText(context, "تم إرسال طلبك بنجاح!", Toast.LENGTH_SHORT).show()
                                onOrderConfirmed()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("place_order_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                    ) {
                        Text("تأكيد وإرسال الطلب الآن", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    // Contact WhatsApp backup button
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val msg = "مرحباً الشعيب إكسبريس، أريد طلب خدمة من التطبيق لموقع $addressText"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967777777777?text=${Uri.encode(msg)}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF22C55E))
                    ) {
                        Icon(imageVector = Icons.Rounded.Phone, contentDescription = null, tint = Color(0xFF22C55E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الطلب والتواصل المباشر عبر الواتس آب", color = Color(0xFF22C55E), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ClientOrdersScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()
    val trackingProgress by viewModel.driverLocationProgress.collectAsState()

    val clientOrders = allOrders.filter { it.clientId == currentUser?.id }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (activeOrder != null) {
            // Live order tracker
            Text(
                text = "تتبع طلبك اللحظي (${activeOrder!!.storeName})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF002D62)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "حالة الطلب الحالي:", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(
                            text = when (activeOrder!!.status) {
                                "PENDING" -> "قيد المراجعة والقبول"
                                "PREPARING" -> "جاري التجهيز بالمتجر"
                                "READY_FOR_PICKUP" -> "بانتظار استلام الكابتن"
                                "ON_THE_WAY" -> "في الطريق إليك الآن"
                                "DELIVERED" -> "تم التوصيل بنجاح"
                                else -> "مرفوض"
                            },
                            fontWeight = FontWeight.Black,
                            color = when (activeOrder!!.status) {
                                "DELIVERED" -> Color(0xFF22C55E)
                                "REJECTED" -> Color.Red
                                else -> Color(0xFF002D62)
                            },
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress indicators
                    LinearProgressIndicator(
                        progress = when (activeOrder!!.status) {
                            "PENDING" -> 0.15f
                            "PREPARING" -> 0.40f
                            "READY_FOR_PICKUP" -> 0.65f
                            "ON_THE_WAY" -> 0.65f + (trackingProgress * 0.30f)
                            "DELIVERED" -> 1.0f
                            else -> 0f
                        },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = Color(0xFF002D62),
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeOrder!!.status == "ON_THE_WAY") {
                        // Simulated map path
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalShipping,
                                    contentDescription = "Captain Delivery Vehicle",
                                    tint = Color(0xFF002D62),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text("تتبع كابتن التوصيل على الخريطة...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Spacer(modifier = Modifier.height(4.dp))
                                // Text indicating animated progress
                                Text("الكابتن على بعد ${(1.0f - trackingProgress) * 4} كم من موقعك", fontSize = 11.sp, color = Color(0xFF1E3A8A))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeOrder!!.driverName != null) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(32.dp).background(Color(0xFF002D62), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("الكابتن: ${activeOrder!!.driverName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("رقم الهاتف: ${activeOrder!!.driverPhone}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }

                    if (activeOrder!!.status == "DELIVERED") {
                        // Rating option
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "قيم تجربتك وخدمتنا:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                        
                        var storeStars by remember { mutableStateOf(5f) }
                        var driverStars by remember { mutableStateOf(5f) }
                        var commentText by remember { mutableStateOf("") }

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("تقييم المتجر: ", fontSize = 11.sp, modifier = Modifier.width(80.dp))
                            Row {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = if (star <= storeStars) Color(0xFFEAB308) else Color(0xFFCBD5E1),
                                        modifier = Modifier.clickable { storeStars = star.toFloat() }.size(24.dp)
                                    )
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("تقييم الكابتن: ", fontSize = 11.sp, modifier = Modifier.width(80.dp))
                            Row {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = if (star <= driverStars) Color(0xFFEAB308) else Color(0xFFCBD5E1),
                                        modifier = Modifier.clickable { driverStars = star.toFloat() }.size(24.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            label = { Text("اكتب رأيك هنا (اختياري)") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.submitReviewAndClose(activeOrder!!.id, storeStars, driverStars, commentText)
                                Toast.makeText(viewModel.getApplication(), "شكراً لك على تقييمك!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text("إرسال التقييم وإغلاق")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.selectActiveOrder(null) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                        ) {
                            Text("الرجوع للقائمة")
                        }
                    }
                }
            }
        }

        Text(
            text = "سجل الطلبات السابقة",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF002D62),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (clientOrders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد طلبات سابقة في سجلك حتى الآن.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(clientOrders) { order ->
                    Card(
                        onClick = { viewModel.selectActiveOrder(order) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = order.storeName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = "${order.totalAmount.toInt()} ريال", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = order.addressText, fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    text = when (order.status) {
                                        "DELIVERED" -> "تم التوصيل"
                                        "REJECTED" -> "ملغي / مرفوض"
                                        else -> "قيد العمل"
                                    },
                                    fontSize = 11.sp,
                                    color = if (order.status == "DELIVERED") Color(0xFF22C55E) else Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientProfileScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF002D62), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Rounded.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = currentUser?.name ?: "زائر الشعيب", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
        Text(text = currentUser?.phone ?: "لا يوجد هاتف", fontSize = 14.sp, color = Color(0xFF64748B))

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "معلومات المحفظة والنقاط", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("نقاط الولاء المكتسبة:", fontSize = 12.sp)
                    Text("350 نقطة", fontWeight = FontWeight.Bold, color = Color(0xFFEAB308))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الرصيد الإلكتروني:", fontSize = 12.sp)
                    Text("0.00 ريال", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تسجيل الخروج من الحساب")
        }
    }
}

// ==================== MERCHANT SCREEN SYSTEM ====================

@Composable
fun StoreScreen(viewModel: AppViewModel, tab: String, onChangeTab: (String) -> Unit) {
    when (tab) {
        "QUEUE" -> StoreOrdersScreen(viewModel)
        "PRODUCTS" -> StoreProductManagerScreen(viewModel)
    }
}

@Composable
fun StoreOrdersScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allStores by viewModel.allStores.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    // Find store owned by current user
    val myStore = allStores.find { it.ownerId == currentUser?.id }
    val storeOrders = if (myStore != null) allOrders.filter { it.storeId == myStore.id } else emptyList()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (myStore == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("يرجى إعداد بيانات المتجر أولاً في لوحة التحكم")
            }
            return
        }

        Text(
            text = "طلبات متجر: ${myStore.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF002D62)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (storeOrders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد طلبات مرسلة لمتجرك حتى الآن.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(storeOrders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "طلب رقم #${order.id}", fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(
                                    text = when (order.status) {
                                        "PENDING" -> "طلب جديد"
                                        "PREPARING" -> "قيد التجهيز"
                                        "READY_FOR_PICKUP" -> "جاهز للتوصيل"
                                        "ON_THE_WAY" -> "في الطريق"
                                        "DELIVERED" -> "تم التوصيل"
                                        else -> "مرفوض"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "العميل: ${order.clientName} (${order.clientPhone})", fontSize = 12.sp)
                            Text(text = "العنوان: ${order.addressText}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "الإجمالي المطلوب: ${order.totalAmount.toInt()} ريال", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                if (order.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.id, "PREPARING") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("قبول وتجهيز")
                                    }
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.id, "REJECTED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("رفض")
                                    }
                                } else if (order.status == "PREPARING") {
                                    Button(
                                        onClick = { viewModel.updateOrderStatus(order.id, "READY_FOR_PICKUP") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                                    ) {
                                        Text("تجهيز واطلب الكابتن")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreProductManagerScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allStores by viewModel.allStores.collectAsState()
    val myStore = allStores.find { it.ownerId == currentUser?.id }

    var showAddDialog by remember { mutableStateOf(false) }
    var prodName by remember { mutableStateOf("") }
    var prodPrice by remember { mutableStateOf("") }
    var prodCat by remember { mutableStateOf("عام") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (myStore == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("يرجى إنشاء متجر أولاً")
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "إدارة منتجات متجرك", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                Text("إضافة منتج")
            }
        }

        val productsFlow = viewModel.repository.getProductsByStore(myStore.id).collectAsState(initial = emptyList())

        Spacer(modifier = Modifier.height(12.dp))

        if (productsFlow.value.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد منتجات معروضة حالياً. اضغط إضافة منتج للبدء", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(productsFlow.value) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = "${product.price.toInt()} ريال يمني", fontSize = 12.sp, color = Color(0xFF1E3A8A))
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                Icon(imageVector = Icons.Rounded.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Add Product Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("إضافة منتج أو خدمة جديدة") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = prodName,
                            onValueChange = { prodName = it },
                            label = { Text("اسم المنتج") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = prodPrice,
                            onValueChange = { prodPrice = it },
                            label = { Text("السعر بالريال اليمني") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = prodCat,
                            onValueChange = { prodCat = it },
                            label = { Text("التصنيف (مثال: رئيسي، جانبي)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val price = prodPrice.toDoubleOrNull() ?: 0.0
                            if (prodName.isNotEmpty() && price > 0) {
                                viewModel.addProduct(myStore.id, prodName, price, prodCat)
                                prodName = ""
                                prodPrice = ""
                                prodCat = "عام"
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("إضافة")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
                }
            )
        }
    }
}

// ==================== CAPTAIN SCREEN SYSTEM ====================

@Composable
fun CaptainScreen(viewModel: AppViewModel, tab: String, onChangeTab: (String) -> Unit) {
    when (tab) {
        "DELIVERIES" -> CaptainDeliveriesScreen(viewModel, onAccept = { onChangeTab("MAP") })
        "MAP" -> CaptainNavigationScreen(viewModel)
        "EARNINGS" -> CaptainEarningsScreen(viewModel)
    }
}

@Composable
fun CaptainDeliveriesScreen(viewModel: AppViewModel, onAccept: () -> Unit) {
    val availableDeliveryOrders by viewModel.availableDeliveryOrders.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "طلبات التوصيل المتاحة في الشعيب",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF002D62)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (availableDeliveryOrders.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد طلبات جاهزة للتوصيل حالياً. انتظر طلبات جديدة...", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(availableDeliveryOrders) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "طلب #${order.id}", fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = "رسوم التوصيل: ${order.deliveryFee.toInt()} ريال", fontWeight = FontWeight.Black, color = Color(0xFF22C55E))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "المتجر: ${order.storeName}", fontSize = 12.sp)
                            Text(text = "العميل في: ${order.addressText}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "طريقة الدفع: ${order.paymentMethod}", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    viewModel.acceptDeliveryOrder(order.id)
                                    onAccept()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                            ) {
                                Text("قبول وتوصيل الطلب")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaptainNavigationScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val trackingProgress by viewModel.driverLocationProgress.collectAsState()

    // Find if the captain has any active order being delivered
    val activeDelivery = allOrders.find {
        it.driverId == currentUser?.id && it.status != "DELIVERED" && it.status != "REJECTED"
    }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        if (activeDelivery == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("ليس لديك طلب نشط حالياً لتوصيله. يرجى قبول طلب أولاً")
            }
            return
        }

        Text(
            text = "مسار التوصيل النشط",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002D62)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "المتجر: ${activeDelivery.storeName}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = "العميل: ${activeDelivery.clientName} (${activeDelivery.clientPhone})", fontSize = 12.sp)
                Text(text = "عنوان التسليم: ${activeDelivery.addressText}", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(text = "المبلغ الكلي للتحصيل: ${activeDelivery.totalAmount.toInt()} ريال", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E3A8A))

                Spacer(modifier = Modifier.height(16.dp))

                // Map Simulation Graphic
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = "Navigation Path",
                            tint = Color(0xFF002D62),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "إحداثيات العميل: ${activeDelivery.gpsCoordinates.ifEmpty { "العوابل، الشعيب" }}", fontSize = 10.sp)
                        Text(text = "المسافة التقريبية: 4.8 كم", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        
                        // Progress slider/simulation bar
                        LinearProgressIndicator(
                            progress = trackingProgress,
                            modifier = Modifier.fillMaxWidth(0.8f).padding(top = 8.dp),
                            color = Color(0xFF002D62)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Status Update Buttons
                when (activeDelivery.status) {
                    "READY_FOR_PICKUP" -> {
                        Button(
                            onClick = { viewModel.updateOrderStatus(activeDelivery.id, "ON_THE_WAY") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
                        ) {
                            Text("استلمت الشحنة من المتجر (في الطريق للعميل)")
                        }
                    }
                    "ON_THE_WAY" -> {
                        Button(
                            onClick = { viewModel.updateOrderStatus(activeDelivery.id, "DELIVERED") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text("تم تسليم الطلب للعميل واستلام المبلغ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CaptainEarningsScreen(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    val myDeliveries = allOrders.filter { it.driverId == currentUser?.id && it.status == "DELIVERED" }
    val totalEarnings = myDeliveries.sumOf { it.deliveryFee }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "إحصائيات أرباح الكابتن",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF002D62)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF002D62))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "إجمالي أرباح التوصيل الخاصة بك", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "${totalEarnings.toInt()} ريال يمني", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "عدد التوصيلات الناجحة: ${myDeliveries.size}", color = Color(0xFFCBD5E1), fontSize = 11.sp)
            }
        }

        Text(text = "سجل التوصيلات المكتملة", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))

        if (myDeliveries.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("لا توجد توصيلات مكتملة مسجلة بعد.", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(myDeliveries) { order ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(text = "طلب #${order.id} - ${order.storeName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "تاريخ التوصيل: العوابل الشعيب", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Text(text = "+${order.deliveryFee.toInt()} ريال", fontWeight = FontWeight.Bold, color = Color(0xFF22C55E), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== ADMINISTRATIVE SCREEN SYSTEM ====================

@Composable
fun AdminScreen(viewModel: AppViewModel, tab: String, onChangeTab: (String) -> Unit) {
    when (tab) {
        "STATS" -> AdminStatsScreen(viewModel)
        "STORES" -> AdminStoresScreen(viewModel)
        "CAPTAINS" -> AdminCaptainsScreen(viewModel)
        "SETTINGS" -> AdminSettingsScreen(viewModel)
    }
}

@Composable
fun AdminStatsScreen(viewModel: AppViewModel) {
    val allOrders by viewModel.allOrders.collectAsState()
    val allStores by viewModel.allStores.collectAsState()
    val allCaptains by viewModel.allCaptains.collectAsState()
    val allClients by viewModel.allClients.collectAsState()

    val completedOrders = allOrders.filter { it.status == "DELIVERED" }
    val grossSales = completedOrders.sumOf { it.totalAmount }
    val totalCommissions = grossSales * 0.10 // 10% Platform fee

    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        item {
            Text(
                text = "مراقبة العمليات - الإدارة العامة الشعيب",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF002D62)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Stats boxes grid
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("الطلبات المكتملة", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${completedOrders.size} طلب", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF002D62))
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("المبيعات الكلية", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${grossSales.toInt()} ريال", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF22C55E))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("عمولة المنصة المقدرة", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${totalCommissions.toInt()} ريال", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFEAB308))
                    }
                }
                Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("الشركاء والعملاء", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${allStores.size} متجر / ${allCaptains.size} كابتن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "مراقبة الطلبات المباشرة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (allOrders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد عمليات جارية حالياً")
                }
            }
        } else {
            items(allOrders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "طلب #${order.id} - ${order.storeName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "للعميل: ${order.clientName} | ${order.addressText}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(text = "كابتن: ${order.driverName ?: "بانتظار القبول"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                        }
                        Text(
                            text = order.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = when (order.status) {
                                "DELIVERED" -> Color(0xFF22C55E)
                                "REJECTED" -> Color.Red
                                else -> Color(0xFFEAB308)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStoresScreen(viewModel: AppViewModel) {
    val allStores by viewModel.allStores.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var sName by remember { mutableStateOf("") }
    var sPhone by remember { mutableStateOf("") }
    var sCat by remember { mutableStateOf("مطاعم") }
    var sAddr by remember { mutableStateOf("العوابل") }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "إدارة المتاجر المسجلة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62))
            ) {
                Text("إضافة متجر", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allStores) { store ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = store.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                            Text(text = "الفئة: ${store.category} | ${store.address}", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(text = "الحالة: ${if (store.status == "ACTIVE") "نشط" else "معطل"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.toggleStoreStatus(store.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (store.status == "ACTIVE") Color.Red else Color(0xFF22C55E)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(text = if (store.status == "ACTIVE") "تعطيل" else "تفعيل", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("إضافة متجر جديد") },
                text = {
                    Column {
                        OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("اسم المتجر") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("هاتف الشريك") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = sCat, onValueChange = { sCat = it }, label = { Text("التصنيف (مطاعم / صيدلية / بقالة)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = sAddr, onValueChange = { sAddr = it }, label = { Text("العنوان بالتفصيل") }, modifier = Modifier.fillMaxWidth())
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (sName.isNotEmpty() && sPhone.isNotEmpty()) {
                                viewModel.addNewStore(sName, sCat, sPhone, sAddr)
                                sName = ""
                                sPhone = ""
                                showAddDialog = false
                            }
                        }
                    ) { Text("حفظ") }
                }
            )
        }
    }
}

@Composable
fun AdminCaptainsScreen(viewModel: AppViewModel) {
    val allCaptains by viewModel.allCaptains.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(text = "إدارة ومراقبة كباتن الشعيب", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
        Spacer(modifier = Modifier.height(12.dp))

        if (allCaptains.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا يوجد كباتن مسجلين حالياً.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(allCaptains) { captain ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = captain.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
                                Text(text = "رقم الهاتف: ${captain.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(text = "حالة الحساب: ${if (captain.status == "ACTIVE") "نشط" else "موقوف"}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.toggleUserStatus(captain.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (captain.status == "ACTIVE") Color.Red else Color(0xFF22C55E)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(text = if (captain.status == "ACTIVE") "حظر" else "تفعيل", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(viewModel: AppViewModel) {
    val settings by viewModel.deliverySettings.collectAsState()
    val context = LocalContext.current

    var baseDeliveryFee by remember { mutableStateOf("1500") }
    var commissionRate by remember { mutableStateOf("10") }

    LaunchedEffect(settings) {
        if (settings.isNotEmpty()) {
            baseDeliveryFee = settings["standard_delivery_fee"] ?: "1500"
            commissionRate = settings["commission_rate"] ?: "10"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "إعدادات منصة الشعيب إكسبريس", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002D62))
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = baseDeliveryFee,
            onValueChange = { baseDeliveryFee = it },
            label = { Text("سعر التوصيل الموحد لقرى الشعيب (ريال)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = commissionRate,
            onValueChange = { commissionRate = it },
            label = { Text("نسبة عمولة التطبيق من قيمة التوصيل (%)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.updateSetting("standard_delivery_fee", baseDeliveryFee)
                viewModel.updateSetting("commission_rate", commissionRate)
                Toast.makeText(context, "تم حفظ الإعدادات بنجاح!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF002D62)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("حفظ وتحديث الإعدادات العامة")
        }
    }
}

// ==================== NAV BAR SYSTEM ====================

@Composable
fun BottomNavBar(
    role: String,
    activeTab: String,
    cartCount: Int,
    onTabSelect: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(80.dp)
    ) {
        when (role) {
            "CLIENT" -> {
                NavigationBarItem(
                    selected = activeTab == "HOME",
                    onClick = { onTabSelect("HOME") },
                    icon = { Icon(imageVector = Icons.Rounded.Home, contentDescription = null) },
                    label = { Text("الرئيسية", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "CART",
                    onClick = { onTabSelect("CART") },
                    icon = {
                        Box {
                            Icon(imageVector = Icons.Rounded.ShoppingCart, contentDescription = null)
                            if (cartCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cartCount.toString(),
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text("السلة", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "ORDERS",
                    onClick = { onTabSelect("ORDERS") },
                    icon = { Icon(imageVector = Icons.Rounded.List, contentDescription = null) },
                    label = { Text("طلباتي", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "PROFILE",
                    onClick = { onTabSelect("PROFILE") },
                    icon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = null) },
                    label = { Text("حسابي", fontSize = 10.sp) }
                )
            }
            "STORE" -> {
                NavigationBarItem(
                    selected = activeTab == "QUEUE",
                    onClick = { onTabSelect("QUEUE") },
                    icon = { Icon(imageVector = Icons.Rounded.List, contentDescription = null) },
                    label = { Text("طلبات المتجر", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "PRODUCTS",
                    onClick = { onTabSelect("PRODUCTS") },
                    icon = { Icon(imageVector = Icons.Rounded.Store, contentDescription = null) },
                    label = { Text("المنتجات", fontSize = 10.sp) }
                )
            }
            "CAPTAIN" -> {
                NavigationBarItem(
                    selected = activeTab == "DELIVERIES",
                    onClick = { onTabSelect("DELIVERIES") },
                    icon = { Icon(imageVector = Icons.Rounded.LocalShipping, contentDescription = null) },
                    label = { Text("الطلبات", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "MAP",
                    onClick = { onTabSelect("MAP") },
                    icon = { Icon(imageVector = Icons.Rounded.LocalShipping, contentDescription = null) },
                    label = { Text("خريطة التوصيل", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "EARNINGS",
                    onClick = { onTabSelect("EARNINGS") },
                    icon = { Icon(imageVector = Icons.Rounded.Star, contentDescription = null) },
                    label = { Text("الأرباح", fontSize = 10.sp) }
                )
            }
            "ADMIN" -> {
                NavigationBarItem(
                    selected = activeTab == "STATS",
                    onClick = { onTabSelect("STATS") },
                    icon = { Icon(imageVector = Icons.Rounded.Home, contentDescription = null) },
                    label = { Text("العمليات", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "STORES",
                    onClick = { onTabSelect("STORES") },
                    icon = { Icon(imageVector = Icons.Rounded.Store, contentDescription = null) },
                    label = { Text("المتاجر", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "CAPTAINS",
                    onClick = { onTabSelect("CAPTAINS") },
                    icon = { Icon(imageVector = Icons.Rounded.Person, contentDescription = null) },
                    label = { Text("الكباتن", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "SETTINGS",
                    onClick = { onTabSelect("SETTINGS") },
                    icon = { Icon(imageVector = Icons.Rounded.Settings, contentDescription = null) },
                    label = { Text("الإعدادات", fontSize = 10.sp) }
                )
            }
        }
    }
}
