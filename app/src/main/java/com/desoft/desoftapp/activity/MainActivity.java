package com.desoft.desoftapp.activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.desoft.desoftapp.R;
import com.desoft.desoftapp.utils.ConectionWithPrint;
import com.desoft.desoftapp.utils.RedAyuda;
import com.desoft.desoftapp.utils.UtilAyuda;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public ArrayList<ColaImpresion> colaImpresions = new ArrayList<>();
    public int countDown = 0;
    public int countUp = 0;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeLayout;
    WebView webView;

    private class AndroidInterfaces {
        static final int PRINT_FEEDPAPER = 2;
        static final int PRINT_TAGGEDTEXT = 1;

        AndroidInterfaces() {
        }

        @JavascriptInterface
        public void OpenPrinter(String printername) {
            if (printername == null || printername.isEmpty()) {
                printername = getSharedPreferences("Config", 0).getString("printer", "");
            }
            Imprimir(printername);
        }

        @JavascriptInterface
        public void PrintTaggedText(String taggedtext) {
            PrintTaggedText(taggedtext, "CP858");
        }

        @JavascriptInterface
        public void PrintTaggedText(String taggedtext, String charset) {
            colaImpresions.add(new ColaImpresion(taggedtext, charset, 0, 1));
        }

        @JavascriptInterface
        public void FeedPaper(int lines) {
            colaImpresions.add(new ColaImpresion("", "", lines, 2));
        }

        @JavascriptInterface
        public void resetPrint() {
            PrintTaggedText("{reset}", "CP858");
        }

        @JavascriptInterface
        public void ClearHistorial() {
            ClearHistorialWebView();
        }

        @JavascriptInterface
        public void ChangeColorStatusBar(final String colorHex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Window window = getWindow();
                        if (Build.VERSION.SDK_INT >= 19) {
                            window.clearFlags(67108864);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            window.addFlags(Integer.MIN_VALUE);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            window.setStatusBarColor(Color.parseColor(colorHex));
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            });
        }

        @JavascriptInterface
        public void OcultarTeclado() {
            runOnUiThread(new Runnable() {
                public void run() {
                    UtilAyuda.OcultarTeclado(MainActivity.this);
                }
            });
        }

        @JavascriptInterface
        public String getIdDispositivo() {
            return UtilAyuda.getIdDispositivo(MainActivity.this);
        }

        @JavascriptInterface
        public void AplicarUrlsDefault(final String UrlLocal, final String UrlRemota, final boolean NavWeb) {
            runOnUiThread(new Runnable() {
                public void run() {
                    SharedPreferences.Editor editor = getSharedPreferences("Config", 0).edit();
                    editor.putBoolean("cache", false);
                    editor.putBoolean("zoom", false);
                    editor.putBoolean("navweb", NavWeb);
                    editor.putString("UrlLocal", UrlLocal.trim());
                    editor.putString("UrlRemoto", UrlRemota.trim());
                    editor.putString("printer", "");
                    editor.apply();
                    ClearHistorialWebView();
                    RestarConfigWebView();
                    DetectaConexion();
                }
            });
        }
    }

    private class ColaImpresion {
        String CharSet;
        String Text;
        int TipoImpresion;
        int linesfeed;

        ColaImpresion(String Text2, String CharSet2, int linesfeed2, int TipoImpresion2) {
            this.Text = Text2;
            this.CharSet = CharSet2;
            this.linesfeed = linesfeed2;
            this.TipoImpresion = TipoImpresion2;
        }
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"SetJavaScriptEnabled"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CreateDir();
        this.webView = (WebView) findViewById(R.id.webview);
        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        this.swipeLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
        this.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                webView.reload();
            }
        });
        this.swipeLayout.setEnabled(false);
        this.webView.getSettings().setLoadsImagesAutomatically(true);
        this.webView.getSettings().setJavaScriptEnabled(true);
        if (getSharedPreferences("Config", 0).getBoolean("zoom", false)) {
            this.webView.getSettings().setBuiltInZoomControls(true);
            this.webView.getSettings().setSupportZoom(true);
            this.webView.getSettings().setDisplayZoomControls(false);
        }
        this.webView.addJavascriptInterface(new AndroidInterfaces(), "AndroidInterfaces");
        this.webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.webView.getSettings().setSaveFormData(true);
        this.webView.clearSslPreferences();
        this.webView.setCertificate(this.webView.getCertificate());
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setAllowFileAccess(true);
        if (getSharedPreferences("Config", 0).getBoolean("cache", false)) {
            this.webView.getSettings().setAppCacheMaxSize(PlaybackStateCompat.ACTION_PLAY_FROM_URI);
            this.webView.getSettings().setAppCacheEnabled(true);
        }
        this.webView.getSettings().setUseWideViewPort(true);
        this.webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                if (swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                super.onPageFinished(view, url);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode == -2) {
                    findViewById(R.id.error).setVisibility(View.VISIBLE);
                    webView.loadData("<h2 id=\"hola\">ERROR DE CONEXION</h2> <script>hola.style.textAlign=\"center\"</script>", "text/html", "utf-8");
                    return;
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }
        });
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                progressBar.incrementProgressBy(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
        this.webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Uri source = Uri.parse(url);
                    DownloadManager.Request request = new DownloadManager.Request(source);
                    if (Build.VERSION.SDK_INT >= 11) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(1);
                    }
                    request.setDestinationUri(Uri.fromFile(new File(new File(Environment.getExternalStorageDirectory() + "/Desoftinf/"), Objects.requireNonNull(source.getLastPathSegment()).replaceAll("_", " "))));
                    ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
                    Toast.makeText(getApplicationContext(), "Descarga iniciada!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Por favor intentalo nuevamente!", Toast.LENGTH_LONG).show();
                }
            }
        });
        DetectaConexion();
    }

    public void RestarConfigWebView() {
        if (getSharedPreferences("Config", 0).getBoolean("zoom", false)) {
            this.webView.getSettings().setBuiltInZoomControls(true);
            this.webView.getSettings().setSupportZoom(true);
            this.webView.getSettings().setDisplayZoomControls(false);
        } else {
            this.webView.getSettings().setBuiltInZoomControls(false);
            this.webView.getSettings().setSupportZoom(false);
            this.webView.getSettings().setDisplayZoomControls(false);
        }
        if (getSharedPreferences("Config", 0).getBoolean("cache", false)) {
            this.webView.getSettings().setAppCacheMaxSize(PlaybackStateCompat.ACTION_PLAY_FROM_URI);
            this.webView.getSettings().setAppCacheEnabled(true);
            return;
        }
        this.webView.getSettings().setAppCacheMaxSize(0);
        this.webView.getSettings().setAppCacheEnabled(false);
    }

    @SuppressLint({"RtlHardcoded"})
    public void DetectaConexion() {
        boolean contieneDosPunto;
        boolean contieneSlash;
        SharedPreferences sharedPreferences = getSharedPreferences("Config", 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (RedAyuda.getHaveNetworkMobile(this)) {
            editor.putBoolean("conexionRemoto", true);
            editor.apply();
            CargarUrl();
            return;
        }
        String url = Objects.requireNonNull(sharedPreferences.getString("UrlRemoto", "")).trim().replaceAll("(?i)http://", "").replaceAll("(?i)https://", "");
        contieneDosPunto = !(url.indexOf(":") <= 0 || url.indexOf(":") >= url.length());
        contieneSlash = !(url.indexOf("/") <= 0 || url.indexOf("/") >= url.length());
        int length = contieneDosPunto ? url.indexOf(":") : contieneSlash ? url.indexOf("/") : url.length();
        String url2 = url.substring(0, length);
        if (url2.length() <= 0 || Objects.requireNonNull(sharedPreferences.getString("UrlRemoto", "")).equalsIgnoreCase(sharedPreferences.getString("UrlLocal", ""))) {
            editor.putBoolean("conexionRemoto", false);
            editor.apply();
            CargarUrl();
            return;
        }
        RedAyuda.ValidarConexion(url2, new RedAyuda.Valida() {
            public String OnConexionLocal() {
                editor.putBoolean("conexionRemoto", false);
                editor.apply();
                CargarUrl();
                return "";
            }

            public String OnConexionRemota() {
                editor.putBoolean("conexionRemoto", true);
                editor.apply();
                CargarUrl();
                return "";
            }
        });
    }

    public void CargarUrl() {
        findViewById(R.id.error).setVisibility(View.GONE);
        SharedPreferences preferences = getSharedPreferences("Config", 0);
        String url = preferences.getBoolean("conexionRemoto", false) ? preferences.getString("UrlRemoto", "") : preferences.getString("UrlLocal", "");
        if (Objects.requireNonNull(url).trim().isEmpty()) {
            this.webView.loadUrl("http://desoftinf.com/printersdk.php");
        } else {
            this.webView.loadUrl(url);
        }
    }

    public void Error(View view) {
        if (!RedAyuda.getHaveNetworkConnection(this)) {
            findViewById(R.id.error).setVisibility(View.VISIBLE);
            this.webView.loadData("<h2 id=\"hola\">ERROR DE CONEXION</h2> <script>hola.style.textAlign=\"center\"</script>", "text/html", "utf-8");
            Toast.makeText(getApplicationContext(), "Por favor intentalo nuevamente!", Toast.LENGTH_LONG).show();
            return;
        }
        findViewById(R.id.error).setVisibility(View.GONE);
        DetectaConexion();
    }

    public void CreateDir() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Desoftinf/");
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView").getMethod("onPause", null).invoke(this.webView, null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        try {
            Class.forName("android.webkit.WebView").getMethod("onResume", null).invoke(this.webView, null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.webView.destroy();
        this.webView = null;
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean z = true;
        if (keyCode == 82) {
            this.webView.loadUrl("javascript:(function () { try { openMenu(); }catch(erro){/*alert(erro.message);*/}})();");
        }
        if (event.getAction() == 0 && keyCode == 24) {
            if (this.countUp == 2) {
                new CountDownTimer(3000, 500) {
                    public void onTick(long l) {
                    }

                    public void onFinish() {
                        countUp = 0;
                        countDown = 0;
                    }
                }.start();
            } else {
                this.countUp++;
            }
            if (this.countUp < 2) {
                return true;
            }
            return false;
        } else if (event.getAction() != 0 || keyCode != 25) {
            return super.onKeyDown(keyCode, event);
        } else {
            if (this.countDown == 3) {
                Config_Dialog();
                this.countDown = 0;
                this.countUp = 0;
            } else if (this.countUp >= 2) {
                this.countDown++;
            }
            if (this.countUp < 2 || this.countDown >= 3) {
                z = false;
            }
            return z;
        }
    }

    private void Config_Dialog() {
        final SharedPreferences preferences = getSharedPreferences("Config", 0);
        View view = getLayoutInflater().inflate(R.layout.prompt_config, null);
        final EditText etUrlLocal = (EditText) view.findViewById(R.id.etUrlLocal);
        final EditText etUrlRemoto = (EditText) view.findViewById(R.id.etUrlRemoto);
        final EditText etPrinter = (EditText) view.findViewById(R.id.etPrinter);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cbCache);
        final CheckBox cbZoom = (CheckBox) view.findViewById(R.id.cbZoom);
        final CheckBox cbBack = (CheckBox) view.findViewById(R.id.cbBack);
        etUrlLocal.setText(preferences.getString("UrlLocal", ""));
        etUrlRemoto.setText(preferences.getString("UrlRemoto", ""));
        etPrinter.setText(preferences.getString("printer", ""));
        cbCache.setChecked(preferences.getBoolean("cache", false));
        cbZoom.setChecked(preferences.getBoolean("zoom", false));
        cbBack.setChecked(preferences.getBoolean("navweb", false));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Configuración")
                .setIcon(R.mipmap.configuracion_small)
                .setView(view)
                .setNegativeButton("CANCELAR",null)
                .setNeutralButton("DEFAULT", null)
                .setPositiveButton("APLICAR", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(-2).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                dialog.getButton(-3).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("cache", false);
                        editor.putBoolean("zoom", false);
                        editor.putBoolean("navweb", false);
                        editor.putString("UrlLocal", "");
                        editor.putString("UrlRemoto", "");
                        editor.putString("printer", "");
                        editor.putBoolean("conexionRemoto", false);
                        editor.apply();
                        dialog.cancel();
                        ClearHistorialWebView();
                        RestarConfigWebView();
                        DetectaConexion();
                    }
                });
                dialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (etUrlLocal.getText().toString().trim().isEmpty() || (!etUrlLocal.getText().toString().toLowerCase().contains("http://") && !etUrlLocal.getText().toString().toLowerCase().contains("https://"))) {
                            etUrlLocal.setError("Datos invalidos");
                            etUrlLocal.requestFocus();
                        } else if (etUrlRemoto.getText().toString().trim().isEmpty() || (!etUrlRemoto.getText().toString().toLowerCase().contains("http://") && !etUrlRemoto.getText().toString().toLowerCase().contains("https://"))) {
                            etUrlRemoto.setError("Datos invalidos");
                            etUrlRemoto.requestFocus();
                        } else {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putBoolean("cache", cbCache.isChecked());
                            editor.putBoolean("zoom", cbZoom.isChecked());
                            editor.putBoolean("navweb", cbBack.isChecked());
                            editor.putString("UrlLocal", etUrlLocal.getText().toString().trim());
                            editor.putString("UrlRemoto", etUrlRemoto.getText().toString().trim());
                            editor.putString("printer", etPrinter.getText().toString().trim());
                            editor.apply();
                            dialog.cancel();
                            ClearHistorialWebView();
                            RestarConfigWebView();
                            DetectaConexion();
                        }
                    }
                });
            }
        });
        dialog.show();
    }

    public void onBackPressed() {
        if (getSharedPreferences("Config", 0).getBoolean("navweb", false) || !this.webView.canGoBack()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setIcon(R.mipmap.applogo)
                    .setTitle(R.string.app_name)
                    .setMessage( "Desea salir de la aplicacion?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).setPositiveButton("Si",new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).create();
            dialog.show();
            return;
        }
        this.webView.goBack();
    }

    public void ClearHistorialWebView() {
        this.webView.clearFormData();
        this.webView.clearHistory();
        this.webView.clearView();
    }

    public void Imprimir(String printername) {
        new ConectionWithPrint(this, printername) {
            public void conexionStatus(boolean status) {
                if (status) {
                    Toast.makeText(getApplication(), "Conexión establecida.", Toast.LENGTH_SHORT).show();
                    for (ColaImpresion colaImpresion : colaImpresions) {
                        try {
                            switch (colaImpresion.TipoImpresion) {
                                case 1:
                                    synchronized (this) {
                                        this.printer.printTaggedText(colaImpresion.Text, colaImpresion.CharSet);
                                        break;
                                    }
                                case 2:
                                    synchronized (this) {
                                        this.printer.feedPaper(colaImpresion.linesfeed);
                                        break;
                                    }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplication(), "Error al imprimir el recibo en la impresora: " + this.printerName, Toast.LENGTH_SHORT).show();
                        }
                    }
                    try {
                        synchronized (this) {
                            this.printer.flush();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    closeBlutoothConnection();
                } else {
                    Toast.makeText(getApplication(), "No se pudo, establecer conexión con la Impresora.", Toast.LENGTH_SHORT).show();
                    closeBlutoothConnection();
                }
                colaImpresions.clear();
            }

            public void closeConexionStatus(boolean status) {
                if (status) {
                    Toast.makeText(getApplication(), "Conexión cerrada.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplication(), "No se pudo, cerrar la conexión.", Toast.LENGTH_SHORT).show();
                }
            }
        }.establishBluetoothConnection();
    }
}
