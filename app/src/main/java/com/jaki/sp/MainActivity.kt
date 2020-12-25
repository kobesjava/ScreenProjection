package com.jaki.sp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jaki.sp.recive.ReciveActivity
import com.jaki.sp.send.SendManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 10000
            )
            return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10000) {
            startMediaProject()
        } else if (requestCode == 10001) {
            data?.let {
                val mediaProjectionManager =
                    getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val mediaProjection: MediaProjection? =
                    mediaProjectionManager.getMediaProjection(resultCode, data)
                mediaProjection?.let {
                    val sendManager = SendManager(it)
                    lifecycle.addObserver(sendManager)
                    sendManager.start()
                }
            }
        }
    }

    private fun startMediaProject() {
        val mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent: Intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, 10001)
    }

    fun send(view: View) {
        if (checkPermission()) {
            startMediaProject()
        }
    }

    fun recive(view: View) {
        startActivity(Intent(this, ReciveActivity::class.java))
    }
}