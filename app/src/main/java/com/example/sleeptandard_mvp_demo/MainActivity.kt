package com.example.sleeptandard_mvp_demo

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Component.AppNav
import com.example.sleeptandard_mvp_demo.Component.Screen
import com.example.sleeptandard_mvp_demo.ui.theme.Sleeptandard_MVP_DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 설정 여부 확인. 안되어 있으면 설정 창으로
        var scheduler = AlarmScheduler(applicationContext)
        scheduler.confirmSetExactAlarms()
        checkFullScreenIntentPermission()

        val startDestination =
            intent.getStringExtra("startDestination") ?: Screen.Home.route

        enableEdgeToEdge()
        setContent {
            Sleeptandard_MVP_DemoTheme {
                AppNav(scheduler, startDestination)
            }
        }
    }

    // TODO: 이거를 다른곳으로 치워버리고 싶어요.
    // FSI 권환 확인 함수... 기본적으로 권한 설정이 되어있긴함.
    private fun checkFullScreenIntentPermission() {
        Log.d("checkFSI", "enter the checking permission fun")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Log.d("checkFSI", "version check complete")

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            Log.d("checkFSI", "can use FSI? : ${!nm.canUseFullScreenIntent()}")
            // 권한 없으면 설정 페이지로 이동
            if (!nm.canUseFullScreenIntent()) {
                Log.d("checkFSI", "can't use FSI")
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    // ⚠️ 이 data 넣어줘야 ActivityNotFoundException 안 남
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }

    }
}
