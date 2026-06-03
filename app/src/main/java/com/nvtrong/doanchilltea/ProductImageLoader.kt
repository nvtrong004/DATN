package com.nvtrong.doanchilltea

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nvtrong.doanchilltea.network.RetrofitClient

object ProductImageLoader {
    private const val TAG = "ProductImageLoader"

    fun load(target: ImageView, imageUrl: String?) {
        val resolvedUrl = resolveImageUrl(imageUrl)
        if (resolvedUrl == null) {
            target.setImageDrawable(null)
            return
        }

        Log.d(TAG, "Loading image: $resolvedUrl")
        Glide.with(target)
            .load(resolvedUrl)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .centerCrop()
            .into(target)
    }

    private fun resolveImageUrl(imageUrl: String?): String? {
        val value = imageUrl?.trim()?.replace("\\", "/")?.takeIf { it.isNotEmpty() && it != "null" }
            ?: return null

        if (value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
        ) {
            val parsedUrl = Uri.parse(value)
            if (isLocalHost(parsedUrl.host)) {
                val currentBase = Uri.parse(RetrofitClient.BASE_URL)
                val path = parsedUrl.encodedPath.orEmpty()
                val query = parsedUrl.encodedQuery?.let { "?$it" }.orEmpty()
                return "${currentBase.scheme}://${currentBase.authority}$path$query"
            }
            return value
        }

        val baseUrl = RetrofitClient.BASE_URL.trimEnd('/')
        val origin = baseUrl.substringBefore("/chilltea")

        return when {
            value.startsWith("/") -> origin + value
            value.startsWith("chilltea/", ignoreCase = true) -> "$origin/$value"
            else -> "$baseUrl/${value.trimStart('/')}"
        }
    }

    private fun isLocalHost(host: String?): Boolean {
        val value = host?.lowercase() ?: return false
        if (value == "localhost" || value == "127.0.0.1" || value == "10.0.2.2") return true
        if (value.startsWith("192.168.")) return true
        if (value.startsWith("10.")) return true

        val parts = value.split(".")
        if (parts.size == 4 && parts[0] == "172") {
            val second = parts[1].toIntOrNull()
            return second != null && second in 16..31
        }

        return false
    }
}
