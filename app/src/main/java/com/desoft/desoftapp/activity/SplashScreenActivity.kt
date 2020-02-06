package com.desoft.desoftapp.activity

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.content.Intent
import android.view.animation.Animation.AnimationListener
import android.R.anim
import android.view.Window
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.desoft.desoftapp.R
import kotlinx.android.synthetic.main.activity_slash_screen.*


class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_slash_screen)
        val bluetoothAdapter=BluetoothAdapter.getDefaultAdapter()
        if(!bluetoothAdapter.isEnabled)
            bluetoothAdapter.enable()
        cargarPermiso()
    }

    private fun cargarPermiso() {
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ) == 0 && ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) == 0 && ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_PHONE_STATE"
            ) == 0 && ContextCompat.checkSelfPermission(
                this,
                "android.permission.CAMERA"
            ) == 0 && ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) == 0 && ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_FINE_LOCATION"
            ) == 0
        ) {
            val animation =
                AnimationUtils.loadAnimation(applicationContext,anim.fade_in)
            animation.duration = 1050
            imageView.startAnimation(animation)
            animation.setAnimationListener(object : AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    val mainintent=Intent(this@SplashScreenActivity,
                        MainActivity::class.java)
                    startActivity(mainintent)
                    this@SplashScreenActivity.finish()
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return
        }
        val permissions = ArrayList<String>()
        permissions.add("android.permission.WRITE_EXTERNAL_STORAGE")
        permissions.add("android.permission.READ_EXTERNAL_STORAGE")
        permissions.add("android.permission.READ_PHONE_STATE")
        permissions.add("android.permission.CAMERA")
        permissions.add("android.permission.ACCESS_COARSE_LOCATION")
        permissions.add("android.permission.ACCESS_FINE_LOCATION")
        val perm = arrayOfNulls<String>(permissions.size)
        permissions.toArray(perm)
        ActivityCompat.requestPermissions(this, perm, 20)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            20 -> {
                val animation =
                    AnimationUtils.loadAnimation(applicationContext,anim.fade_in)
                animation.duration = 1050
                this.imageView.startAnimation(animation)
                animation.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}

                    override fun onAnimationEnd(animation: Animation) {
                        val mainintent=Intent(this@SplashScreenActivity,
                            MainActivity::class.java)
                        startActivity(mainintent)
                        this@SplashScreenActivity.finish()
                    }

                    override fun onAnimationRepeat(animation: Animation) {}
                })
                return
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                return
            }
        }
    }
}
