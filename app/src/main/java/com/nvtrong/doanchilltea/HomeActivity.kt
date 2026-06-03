package com.nvtrong.doanchilltea

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.nvtrong.doanchilltea.model.CartManager
import com.nvtrong.doanchilltea.model.Product
import com.nvtrong.doanchilltea.network.ProductsResponse
import com.nvtrong.doanchilltea.network.RetrofitClient
import com.nvtrong.doanchilltea.network.StatsResponse
import com.nvtrong.doanchilltea.network.WeatherResponse
import com.nvtrong.doanchilltea.network.WeatherRetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.Normalizer

class HomeActivity : AppCompatActivity() {

    companion object {
        private const val THAI_NGUYEN_LAT = 21.5942
        private const val THAI_NGUYEN_LON = 105.8482
        private const val WEATHER_CACHE_TTL_MS = 10 * 60 * 1000L

        private var cachedWeather: CachedWeather? = null
    }

    private data class CachedWeather(
        val temp: Int,
        val condition: String?,
        val cityName: String?,
        val savedAtMillis: Long
    )

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tvWeather: TextView
    private lateinit var tvCartBadge: TextView

    private var userName = "Khách hàng"
    private var userId = -1
    private var currentTemp: Int? = null
    private var currentWeatherCondition: String? = null
    private var allProducts: List<Product> = emptyList()
    private var topProductName: String? = null
    private var aiSuggestedProductIds: List<Int> = emptyList()
    private var weatherRequestInFlight = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) getDeviceLocation() else useDefaultLocation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val session = SessionManager.getUser(this)
        userName = intent.getStringExtra("USER_NAME") ?: session?.name ?: "Khách hàng"
        userId = intent.getIntExtra("USER_ID", session?.id ?: -1)
        CartManager.setCurrentUser(userId)

        findViewById<TextView>(R.id.tvName).text = userName
        tvWeather = findViewById(R.id.tvWeather)
        tvCartBadge = findViewById(R.id.tvCartBadge)

        setupBottomNavigation()
        updateCartBadge()
        loadProductsFromApi()
        loadTodayTopProduct()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        checkAndRequestLocationPermissions()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        if (!applyCachedWeatherIfFresh()) {
            getDeviceLocation()
        }
    }

    private fun loadProductsFromApi() {
        RetrofitClient.instance.getProducts().enqueue(object : Callback<ProductsResponse> {
            override fun onResponse(call: Call<ProductsResponse>, response: Response<ProductsResponse>) {
                allProducts = if (response.isSuccessful && response.body()?.success == true) {
                    response.body()!!.products.map {
                        Product(
                            id = it.id,
                            name = it.name,
                            price = "%,d đ".format(it.price),
                            imageUrl = it.imageUrl,
                            categoryId = it.categoryId,
                            categoryName = it.categoryName
                        )
                    }
                } else {
                    dummyProducts()
                }

                setupAllProductsRecyclerView(allProducts)
                refreshSuggestions()
            }

            override fun onFailure(call: Call<ProductsResponse>, t: Throwable) {
                Log.e("Home", "Load products failed: ${t.message}")
                allProducts = dummyProducts()
                setupAllProductsRecyclerView(allProducts)
                refreshSuggestions()
            }
        })
    }

    private fun loadTodayTopProduct() {
        RetrofitClient.instance.getStats("today").enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    topProductName = response.body()!!.topProducts.minByOrNull { it.rank }?.name
                }
                refreshSuggestions()
            }

            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {
                Log.e("Home", "Load top product failed: ${t.message}")
                refreshSuggestions()
            }
        })
    }

    private fun refreshSuggestions() {
        if (allProducts.isEmpty()) return

        val topProduct = topProductName
            ?.let { name -> allProducts.firstOrNull { it.name.equals(name, ignoreCase = true) } }
        val aiProducts = aiSuggestedProductIds
            .mapNotNull { id -> allProducts.firstOrNull { it.id == id } }

        val preferredCategories = preferredCategoryKeywords(currentTemp)
        val categoryMatches = allProducts
            .filter { product ->
                val category = normalize(product.categoryName.orEmpty())
                preferredCategories.any { keyword -> category.contains(keyword) }
            }
            .filterNot { it.id == topProduct?.id }

        val fallback = allProducts.filterNot { it.id == topProduct?.id }
        val suggestions = buildList {
            if (topProduct != null) add(topProduct)
            addAll(categoryMatches)
            addAll(aiProducts.filterNot { aiProduct ->
                categoryMatches.any { it.id == aiProduct.id }
            })
            addAll(fallback.filterNot { fallbackProduct ->
                categoryMatches.any { it.id == fallbackProduct.id } ||
                    aiProducts.any { it.id == fallbackProduct.id }
            })
        }.distinctBy { it.id }.take(10)

        setupSuggestRecyclerView(suggestions)
    }

    private fun preferredCategoryKeywords(temp: Int?): List<String> {
        return when {
            temp == null -> listOf("ngay binh thuong", "ngay am u")
            temp <= 18 -> listOf("ngay lanh")
            temp <= 23 -> listOf("ngay am u")
            temp <= 28 -> listOf("ngay binh thuong")
            temp <= 33 -> listOf("ngay nong", "ngay nang")
            else -> listOf("ngay rat nong", "ngay nong", "ngay nang")
        }
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace("đ", "d")
    }

    private fun dummyProducts() = listOf(
        Product(1, "Trà sữa Ô Long", "35,000 đ", null, categoryName = "Ngày bình thường"),
        Product(2, "Trà đào cam sả", "39,000 đ", null, categoryName = "Ngày nắng"),
        Product(3, "Matcha Latte", "45,000 đ", null, categoryName = "Ngày lạnh"),
        Product(4, "Hồng trà sữa", "35,000 đ", null, categoryName = "Ngày bình thường"),
        Product(5, "Cà phê muối", "29,000 đ", null, categoryName = "Ngày âm u"),
        Product(6, "Sinh tố bơ", "45,000 đ", null, categoryName = "Ngày nắng")
    )

    private fun setupSuggestRecyclerView(data: List<Product>) {
        val rv = findViewById<RecyclerView>(R.id.rvSuggestToday)
        rv.adapter = SuggestAdapter(data) { onAddToCart(it) }
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv.visibility = View.VISIBLE
    }

    private fun setupAllProductsRecyclerView(data: List<Product>) {
        val list = findViewById<LinearLayout>(R.id.llAllProducts)
        val inflater = LayoutInflater.from(this)
        list.removeAllViews()

        data.forEach { product ->
            val itemView = inflater.inflate(R.layout.item_product_all, list, false)
            ProductImageLoader.load(itemView.findViewById<ImageView>(R.id.imgProduct), product.imageUrl)
            itemView.findViewById<TextView>(R.id.tvProductName).text = product.name
            itemView.findViewById<TextView>(R.id.tvProductPrice).text = product.price
            itemView.findViewById<MaterialButton>(R.id.btnAddProduct).setOnClickListener {
                onAddToCart(product)
            }
            list.addView(itemView)
        }

        list.visibility = View.VISIBLE
    }

    private fun onAddToCart(product: Product) {
        CartManager.addItem(product)
        updateCartBadge()
        Toast.makeText(this, "Đã thêm: ${product.name}", Toast.LENGTH_SHORT).show()
    }

    fun updateCartBadge() {
        val count = CartManager.totalCount()
        tvCartBadge.text = if (count > 9) "9+" else count.toString()
        tvCartBadge.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
    }

    private fun setupBottomNavigation() {
        val nav = findViewById<LinearLayout>(R.id.bottomNavLayout)
        (nav.getChildAt(0) as LinearLayout).setOnClickListener { }
        (nav.getChildAt(1) as LinearLayout).setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("USER_NAME", userName)
            })
        }
        (nav.getChildAt(2) as LinearLayout).setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("USER_NAME", userName)
                currentTemp?.let { putExtra("WEATHER_TEMP", it) }
                putExtra("WEATHER_CONDITION", currentWeatherCondition)
            })
        }
        (nav.getChildAt(3) as LinearLayout).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("USER_NAME", userName)
            })
        }
    }

    private fun checkAndRequestLocationPermissions() {
        val hasFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

        if (hasFineLocation || hasCoarseLocation) {
            getDeviceLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        if (applyCachedWeatherIfFresh() || weatherRequestInFlight) return

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            useDefaultLocation()
            return
        }

        weatherRequestInFlight = true
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeather(location.latitude, location.longitude)
                } else {
                    requestCurrentLocationOrDefault()
                }
            }
            .addOnFailureListener {
                requestCurrentLocationOrDefault()
            }
    }

    @SuppressLint("MissingPermission")
    private fun requestCurrentLocationOrDefault() {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    fetchWeather(location.latitude, location.longitude)
                } else {
                    useDefaultLocation()
                }
            }
            .addOnFailureListener {
                useDefaultLocation()
            }
    }

    private fun useDefaultLocation() {
        fetchWeather(THAI_NGUYEN_LAT, THAI_NGUYEN_LON)
    }

    private fun fetchWeather(lat: Double, lon: Double) {
        weatherRequestInFlight = true
        WeatherRetrofitClient.instance.getWeather(lat, lon, "e2dca3181e1966bc3a710ca645803f6a")
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(call: Call<WeatherResponse>, r: Response<WeatherResponse>) {
                    weatherRequestInFlight = false
                    val weather = r.body()
                    if (r.isSuccessful && weather != null) {
                        val temp = weather.main.temp.toInt()
                        val condition = weather.weather
                            ?.firstOrNull()
                            ?.let { it.description ?: it.main }
                        cachedWeather = CachedWeather(
                            temp = temp,
                            condition = condition,
                            cityName = weather.name,
                            savedAtMillis = SystemClock.elapsedRealtime()
                        )
                        applyWeather(temp, condition, weather.name)
                        loadAiSuggestions()
                    }
                    refreshSuggestions()
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    weatherRequestInFlight = false
                    currentTemp = null
                    currentWeatherCondition = null
                    tvWeather.text = "--°C"
                    refreshSuggestions()
                }
            })
    }

    private fun applyCachedWeatherIfFresh(): Boolean {
        val weather = cachedWeather ?: return false
        if (SystemClock.elapsedRealtime() - weather.savedAtMillis > WEATHER_CACHE_TTL_MS) return false

        applyWeather(weather.temp, weather.condition, weather.cityName)
        refreshSuggestions()
        return true
    }

    private fun applyWeather(temp: Int, condition: String?, cityName: String?) {
        currentTemp = temp
        currentWeatherCondition = condition
        tvWeather.text = if (cityName.isNullOrBlank()) "${temp}°C" else "${temp}°C, $cityName"
    }

    private fun loadAiSuggestions() {
        if (userId <= 0) return

        RetrofitClient.instance.getAiSuggestions(userId, currentTemp, currentWeatherCondition)
            .enqueue(object : Callback<ProductsResponse> {
                override fun onResponse(call: Call<ProductsResponse>, response: Response<ProductsResponse>) {
                    aiSuggestedProductIds = if (response.isSuccessful && response.body()?.success == true) {
                        response.body()!!.products.map { it.id }
                    } else {
                        emptyList()
                    }
                    refreshSuggestions()
                }

                override fun onFailure(call: Call<ProductsResponse>, t: Throwable) {
                    aiSuggestedProductIds = emptyList()
                    refreshSuggestions()
                }
            })
    }
}
