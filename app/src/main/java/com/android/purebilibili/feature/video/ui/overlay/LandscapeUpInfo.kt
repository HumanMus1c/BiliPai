// 文件路径: feature/video/ui/overlay/LandscapeUpInfo.kt
package com.android.purebilibili.feature.video.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.components.AppSurface

/**
 *  横屏 UP 主信息组件
 * 
 * 显示在横屏左上角，包含：
 * - UP 主头像（圆形）
 * - UP 主名字
 */
@Composable
fun LandscapeUpInfo(
    avatarUrl: String,
    upName: String,
    modifier: Modifier = Modifier
) {
    AppSurface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // UP 主头像
            AsyncImage(
                model = avatarUrl,
                contentDescription = "UP主头像",
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // UP 主名字
            Text(
                text = upName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
        }
    }
}

/**
 *  横屏观看人数组件
 */
@Composable
fun LandscapeViewerCount(
    count: String,
    modifier: Modifier = Modifier
) {
    if (count.isEmpty()) return
    
    AppSurface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // 观看图标（使用文字代替）
            Text(
                text = "👁",
                fontSize = 10.sp
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = count,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
