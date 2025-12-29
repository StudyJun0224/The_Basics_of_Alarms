package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons

@Preview
@Composable
fun SettingsScreen(){

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp)

    ){

        Spacer(Modifier.height(32.dp))

        Text("설정",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 20.sp
            ),
            modifier = Modifier
                .padding(start = 12.dp)
        )

        Spacer(Modifier.height(34.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth( )
                .height(132.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x26F1F1F1),   // ✅ 여기로 이동
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ){
            Row(
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .fillMaxHeight()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = Color(0xFF465467), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            modifier = Modifier
                                .size(18.dp),
                            painter = painterResource(AppIcons.SettingsMail),
                            contentDescription = "고객 지원"
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = Color(0xFF465467), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            modifier = Modifier
                                .size(18.dp),
                            painter = painterResource(AppIcons.SettingsTool),
                            contentDescription = "고객 지원"
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            "고객 지원",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            "문의하기",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                lineHeight = 20.sp,
                                color = Color(0x99F1F1F1),
                            )
                        )

                    }

                    HorizontalDivider(Modifier.height(0.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ){
                        Text(
                            "시스템 접근권한",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            "설정",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                lineHeight = 20.sp,
                                color = Color(0x99F1F1F1),
                            )
                        )

                    }
                }
            }

        }

        Surface(
            modifier = Modifier
                .fillMaxWidth( )
                .height(71.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x26F1F1F1),   // ✅ 여기로 이동
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ){
                Column(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .fillMaxHeight()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = Color(0xFF465467), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(15.dp),
                            painter = painterResource(AppIcons.SettingsQuestion),
                            contentDescription = "튜토리얼"
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "튜토리얼",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        "튜토리얼",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            lineHeight = 20.sp,
                            color = Color(0x99F1F1F1),
                        )
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth( )
                .height(71.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0x26F1F1F1),   // ✅ 여기로 이동
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 20.dp)
            ){
                Column(
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .fillMaxHeight()
                        .background(Color.Transparent),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = Color(0xFF465467), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(18.dp),
                            painter = painterResource(AppIcons.SettingsActivity),
                            contentDescription = "수면데이터"
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        "수면데이터 보내기",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        "수면데이터 보내기",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            lineHeight = 20.sp,
                            color = Color(0x99F1F1F1),
                        )
                    )
                }
            }
        }

    }
}