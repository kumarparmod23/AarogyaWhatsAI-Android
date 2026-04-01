package com.aarogya.whatsai

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : Activity() {

    // ──────────────────────────────────────────────
    //  CHANGE THIS TO YOUR DEPLOYED URL
    // ──────────────────────────────────────────────
    private val webUrl = "https://aarogya-whatsai.vercel.app"
    // For local testing use: "http://10.0.2.2:3000"

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var offlineView: RelativeLayout

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        offlineView = findViewById(R.id.offlineView)

        // Pull-to-refresh
        swipeRefresh.setColorSchemeColors(0xFF25D366.toInt())
        swipeRefresh.setOnRefreshListener {
            if (isOnline()) {
                webView.reload()
            } else {
                swipeRefresh.isRefreshing = false
                showOffline()
            }
        }

        // Retry button
        findViewById<View>(R.id.btnRetry).setOnClickListener {
            if (isOnline()) {
                hideOffline()
                webView.loadUrl(webUrl)
            }
        }

        setupWebView()

        if (isOnline()) {
            webView.loadUrl(webUrl)
        } else {
            showOffline()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            mediaPlaybackRequiresUserGesture = false

            // Enable app cache for offline support
            val cachePath = applicationContext.cacheDir.absolutePath
            setGeolocationDatabasePath(cachePath)
        }

        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest?, error: WebResourceError?
            ) {
                // Show offline screen only for main frame
                if (request?.isForMainFrame == true) {
                    showOffline()
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false

                // Open external links (WhatsApp, phone, email) in external apps
                return when {
                    url.startsWith("tel:") || url.startsWith("mailto:") ||
                    url.startsWith("whatsapp:") || url.startsWith("intent:") -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    }
                    // Keep internal navigation inside WebView
                    url.contains(webUrl.replace("https://", "").replace("http://", "")) -> false
                    // Open other external URLs in browser
                    else -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        true
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }

            // Handle file upload from patients
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                val intent = fileChooserParams?.createIntent()
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST)
                    this@MainActivity.filePathCallback = filePathCallback
                } catch (e: Exception) {
                    filePathCallback?.onReceiveValue(null)
                }
                return true
            }
        }
    }

    // File upload handling
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            val result = if (resultCode == RESULT_OK && data != null) {
                data.data?.let { arrayOf(it) }
            } else null
            filePathCallback?.onReceiveValue(result)
            filePathCallback = null
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOffline() {
        webView.visibility = View.GONE
        swipeRefresh.isEnabled = false
        offlineView.visibility = View.VISIBLE
    }

    private fun hideOffline() {
        webView.visibility = View.VISIBLE
        swipeRefresh.isEnabled = true
        offlineView.visibility = View.GONE
    }

    // Handle back button — go back in WebView history
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private companion object {
        const val FILE_CHOOSER_REQUEST = 1001
    }
}
