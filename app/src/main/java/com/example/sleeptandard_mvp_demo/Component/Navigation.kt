package com.example.sleeptandard_mvp_demo.Component

import com.example.sleeptandard_mvp_demo.Screen.HomeScreen
import com.example.sleeptandard_mvp_demo.Screen.SettingAlarmScreen

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

sealed class Screen(val route:String){
    object Home: Screen("home")
    object SettingAlarm: Screen("settingAlarm")
}

@Composable
fun AppNav(scheduler: AlarmScheduler){
    val rememberNavController = rememberNavController()
    val alarmViewModel: AlarmViewModel = viewModel()

    val navGraph = rememberNavController.createGraph(startDestination = Screen.Home.route){
        composable(Screen.Home.route){
            HomeScreen(
                alarmViewModel = alarmViewModel,
                onClickSetting = {
                rememberNavController.navigate(Screen.SettingAlarm.route)
                })
        }
        composable(Screen.SettingAlarm.route){
            SettingAlarmScreen(
                viewModel = alarmViewModel,
                scheduler = scheduler,
                onClickConfirm = {rememberNavController.navigate(Screen.Home.route)})
        }
    }

    NavHost(
        navController = rememberNavController,
        graph = navGraph)


}