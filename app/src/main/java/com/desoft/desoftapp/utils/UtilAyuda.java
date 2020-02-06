package com.desoft.desoftapp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

public class UtilAyuda {
    @SuppressLint({"HardwareIds"})
    public static String getIdDispositivo(Context context) {
        String imei = "";
        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE") != 0) {
            return imei;
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            imei = tm.getDeviceId();
        }
        if (imei == null || imei.length() == 0) {
            imei = Secure.getString(context.getContentResolver(), "android_id");
        }
        return imei;
    }

    public static void OcultarTeclado(Activity context) {
        View view = context.getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
