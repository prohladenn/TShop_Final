package com.example.tshop.t_shop;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AquiringActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aquiring);
        String url = getIntent().getStringExtra("url");
        WebView webView = findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);

        SimpleWebViewClientImpl webViewClient = new SimpleWebViewClientImpl(this,getIntent().getStringExtra("basketPath"),getIntent().getStringExtra("shopPath"),getIntent().getStringExtra("orderPath") , getIntent().getLongExtra("number", 0L)  );
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);
    }
}
