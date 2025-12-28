package com.example.sleeptandard_mvp_demo.Component

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import com.example.sleeptandard_mvp_demo.Screen.HomeScreen
import com.example.sleeptandard_mvp_demo.Screen.SettingAlarmScreen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.Screen.ExperimentScreen
import com.example.sleeptandard_mvp_demo.Screen.JournalScreen
import com.example.sleeptandard_mvp_demo.Screen.ReviewAlarmScreen
import com.example.sleeptandard_mvp_demo.Screen.SettedAlarmScreen
import com.example.sleeptandard_mvp_demo.Screen.SettingsScreen
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel


sealed class Screen(val route: String, val showBottomBar: Boolean = true) {
    object Home : Screen("home", showBottomBar = true)
    object Journal : Screen("journal", showBottomBar = true)
    object Settings : Screen("settings", showBottomBar = true)

    object SettedAlarm : Screen("settedAlarm", showBottomBar = false)
    object ReviewAlarm : Screen("reviewAlarm", showBottomBar = false)
    object Experiment : Screen("experiment", showBottomBar = false)
}

@Composable
fun AppNav(
    scheduler: AlarmScheduler,
    // 실험중
    startDestination: String = Screen.Home.route,
    initialAlarm: Alarm? = null   // ✨ 추가
){

    /*** 기존에 있던 코드 ***/
    val rememberNavController = rememberNavController()
    val alarmViewModel: AlarmViewModel = viewModel()

    // 앱 시작 시, initialAlarm이 있으면 ViewModel에 세팅
    LaunchedEffect(initialAlarm) {
        if (initialAlarm != null) {
            alarmViewModel.copyAlarm(initialAlarm)
        }
    }

    // AlarmPreference를 위한 컨텍스트
    val context = LocalContext.current
    val alarmPrefs = AlarmPreferences(context)
    val isAlarmSetted = alarmPrefs.isAlarmSet()

    val navGraph = rememberNavController.createGraph(startDestination = startDestination){
        composable(Screen.Home.route){
            HomeScreen(
                alarmViewModel = alarmViewModel,
                scheduler = scheduler,
                onClickSetting = {
                    rememberNavController.navigate(Screen.SettedAlarm.route){
                        popUpTo(Screen.Home.route){inclusive = true}
                    }
                },
                goExperimentScreen = {
                    rememberNavController.navigate(Screen.Experiment.route)
                },
                onClickJournal = { rememberNavController.navigate(Screen.Journal.route) },
                onClickSettingTab = { /* 네가 원하는 설정 화면 route로 */ }
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

        composable(Screen.Journal.route) {
            JournalScreen()
        }

        composable(Screen.Settings.route){
            SettingsScreen()
        }

        /** 실험장 **/
        composable(Screen.Experiment.route){
            ExperimentScreen()
        }

    }


    /* 전에 쓰던 코드 잠시 비활성화
    NavHost(
        navController = rememberNavController,
        graph = navGraph)
    */

    /**** navigation 공사중 ****/
    val navBackStackEntry by rememberNavController.currentBackStackEntryAsState()   // 최신 스택을 가져옴 (현재 위치한 경로)
    val currentRoute = navBackStackEntry?.destination?.route    // 최신 스택의 route를 가져옴 (현재 위치한 경로)

    Scaffold(
        bottomBar = {
            // 지금 라우트가 홈, 일지, 설정, 설정완료화면 이면 바텀네비바를 띄우기 위한 Boolean값임.
            val showBottom = when (currentRoute) {
                Screen.Home.route,
                Screen.Journal.route,
                Screen.Settings.route,
                Screen.SettedAlarm.route -> true
                else -> false
            }

            // 위에 조건에 부합하면 바텀네바바를 띄움
            if (showBottom) {
                AlarmBottomNavBar(
                    selectedIndex = when (currentRoute) {
                        Screen.Home.route -> 0
                        Screen.Journal.route -> 1
                        Screen.Settings.route -> 2
                        // SettedAlarmScreen이 else에 들어가겠지
                        else -> 0
                    },
                    onSelect = { idx ->
                        val target = when (idx) {
                            // 시발 이렇게 하는게 맞냐?
                            0 -> if(isAlarmSetted) Screen.SettedAlarm.route else Screen.Home.route
                            1 -> Screen.Journal.route
                            2 -> Screen.Settings.route
                            else -> Screen.Home.route
                        }
                        rememberNavController.navigate(target) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Screen.Home.route) { saveState = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = rememberNavController,
            modifier = Modifier.padding(innerPadding),
            graph = navGraph
        )
    }





}