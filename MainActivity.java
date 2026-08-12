package com.jdrlunaria.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    private static final String WEB_URL = "https://foxydreyard-dotcom.github.io/JDR/";
    private static final String RELEASE_API = "https://api.github.com/repos/foxydreyard-dotcom/JDR/releases/latest";
    private static final int FILE_CHOOSER_REQUEST = 9001;

    private WebView webView;
    private ProgressBar loading;
    private ValueCallback<Uri[]> fileChooserCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jdrlunaria.app.R.layout.activity_main);

        webView = findViewById(R.id.webView);
        loading = findViewById(R.id.loading);

        configureWebView();
        webView.loadUrl(WEB_URL);

        checkForNativeUpdate();
    }

    private void configureWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setUserAgentString(s.getUserAgentString() + " JDRLunariaNative/" + BuildConfig.VERSION_NAME);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                loading.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();

                if ("foxydreyard-dotcom.github.io".equalsIgnoreCase(host)) {
                    return false;
                }

                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (ActivityNotFoundException ignored) {}
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (fileChooserCallback != null) {
                    fileChooserCallback.onReceiveValue(null);
                }
                fileChooserCallback = filePathCallback;

                Intent intent;
                try {
                    intent = fileChooserParams.createIntent();
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileChooserCallback = null;
                    return false;
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException ignored) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            if (fileChooserCallback != null) {
                fileChooserCallback.onReceiveValue(result);
                fileChooserCallback = null;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void checkForNativeUpdate() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(RELEASE_API);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "JDR-Lunaria-Android");

                int code = connection.getResponseCode();
                if (code < 200 || code >= 300) return;

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();

                JSONObject release = new JSONObject(json.toString());
                String tag = release.optString("tag_name", "");
                String latestVersion = tag.replaceFirst("^android-v", "").trim();

                if (latestVersion.isEmpty() || !isNewerVersion(latestVersion, BuildConfig.VERSION_NAME)) return;

                JSONArray assets = release.optJSONArray("assets");
                String apkUrl = "";
                if (assets != null) {
                    for (int i = 0; i < assets.length(); i++) {
                        JSONObject asset = assets.getJSONObject(i);
                        String name = asset.optString("name", "");
                        if (name.toLowerCase().endsWith(".apk")) {
                            apkUrl = asset.optString("browser_download_url", "");
                            break;
                        }
                    }
                }

                final String downloadUrl = apkUrl;
                final String version = latestVersion;

                if (!downloadUrl.isEmpty()) {
                    runOnUiThread(() -> showUpdateDialog(version, downloadUrl));
                }
            } catch (Exception ignored) {
                // Une panne du contrôle de mise à jour ne doit jamais empêcher l'ouverture du JDR.
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }

    private void showUpdateDialog(String version, String apkUrl) {
        new AlertDialog.Builder(this)
                .setTitle("Mise à jour JDR Lunaria")
                .setMessage("Une nouvelle version de l'application est disponible : " + version +
                        "\n\nLa partie JDR web se met à jour automatiquement. Cette mise à jour concerne l'application Android elle-même (icône, fonctions natives, etc.).")
                .setPositiveButton("Télécharger", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl)));
                    } catch (ActivityNotFoundException ignored) {}
                })
                .setNegativeButton("Plus tard", null)
                .show();
    }

    private boolean isNewerVersion(String remote, String local) {
        String cleanLocal = local.replace("-debug", "");
        String[] r = remote.split("\\.");
        String[] l = cleanLocal.split("\\.");
        int max = Math.max(r.length, l.length);

        for (int i = 0; i < max; i++) {
            int rv = i < r.length ? safeInt(r[i]) : 0;
            int lv = i < l.length ? safeInt(l[i]) : 0;
            if (rv > lv) return true;
            if (rv < lv) return false;
        }
        return false;
    }

    private int safeInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
