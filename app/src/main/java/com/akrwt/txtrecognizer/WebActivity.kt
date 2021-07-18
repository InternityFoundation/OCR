package com.akrwt.txtrecognizer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity : AppCompatActivity(), MaterialSearchBar.OnSearchActionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        val url = intent.getStringExtra("url")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.visibility = View.VISIBLE
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
        webView.webViewClient = WebViewClient()

        webView.loadUrl("https://www.google.com/search?q=$url")
        webView.settings.javaScriptEnabled = true
        webView.settings.allowContentAccess = true
        webView.settings.domStorageEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.setAppCacheEnabled(true)

        btnShare.setOnClickListener {
            sharePage()
        }
        search()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
            progressBar.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    private fun sharePage() {
        val view1 = window.decorView.rootView
        view1.isDrawingCacheEnabled = true

        val bitmap = Bitmap.createBitmap(view1.getDrawingCache())
        view1.isDrawingCacheEnabled = false

        val bitmPath =
            MediaStore.Images.Media.insertImage(contentResolver, bitmap, "ImageShare", null)

        val uri = Uri.parse(bitmPath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.setPackage("com.whatsapp")
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Share Using"))

    }

    private fun search() {
        searchBar.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                webView.findAllAsync(s.toString())
                if (s != "") {
                    btnNext.visibility = View.VISIBLE
                    btnNext.setOnClickListener {
                        webView.findNext(true)
                    }
                } else {
                    btnNext.visibility = View.GONE
                }
            }
        })

    }

    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_BACK -> searchBar.clearFocus()
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
    }

    override fun onSearchConfirmed(text: CharSequence?) {
    }

}
