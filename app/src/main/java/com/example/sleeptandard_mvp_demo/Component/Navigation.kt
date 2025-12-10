package com.example.sleeptandard_mvp_demo.Component

import android.util.Log
import com.example.sleeptandard_mvp_demo.Screen.HomeScreen
import com.example.sleeptandard_mvp_demo.Screen.SettingAlarmScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Screen.ReviewAlarmScreen
import com.example.sleeptandard_mvp_demo.Screen.SettedAlarmScreen
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

sealed class Screen(val route:String){
    object Home: Screen("home")
    /* Not using: 폐기
    object SettingAlarm: Screen("settingAlarm")*/
    object ReviewAlarm: Screen("reviewAlarm")
    object SettedAlarm: Screen("settedAlarm")
}

@Composable
fun AppNav(
    scheduler: AlarmScheduler,
    startDestination: String = Screen.Home.route,
    initialAlarm: Alarm? = null   // ✨ 추가
){
    val rememberNavController = rememberNavController()
    val alarmViewModel: AlarmViewModel = viewModel()

    // 🔥 앱 시작 시, initialAlarm이 있으면 ViewModel에 세팅
    LaunchedEffect(initialAlarm) {
        if (initialAlarm != null) {
            alarmViewModel.copyAlarm(initialAlarm)
        }
    }

    val navGraph = rememberNavController.createGraph(startDestination = startDestination){
        composable(Screen.Home.route){
            HomeScreen(
                alarmViewModel = alarmViewModel,
                scheduler = scheduler,
                onClickSetting = {
                rememberNavController.navigate(Screen.SettedAlarm.route)
                },
            )
        }
        composable(Screen.SettedAlarm.route){
            SettedAlarmScreen(
                alarmViewModel = alarmViewModel,
                scheduler = scheduler,
                onTurnAlarmOff = {
                    rememberNavController.navigate(Screen.Home.route){
                        popUpTo(Screen.SettedAlarm.route){inclusive = true}
                    }

                }
            )
        }
        /* Not using: 폐기
        composable(Screen.SettingAlarm.route){
            SettingAlarmScreen(
                viewModel = alarmViewModel,
                scheduler = scheduler,
                onClickConfirm = {rememberNavController.popBackStack()})
        }*/

        composable(Screen.ReviewAlarm.route){
            ReviewAlarmScreen(
                onSubmit = {
                    rememberNavController.navigate(Screen.Home.route){
                        // 네비 스택 초기화
                        popUpTo(Screen.ReviewAlarm.route){inclusive = true}
                    }

                    }
            )
        }

    }

    NavHost(
        navController = rememberNavController,
        graph = navGraph)


}