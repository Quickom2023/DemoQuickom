package com.beowulfchain.conferencetv.demoapp

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beowulfchain.conferencetv.demoapp.ui.theme.DemoAppTheme
import io.flutter.embedding.android.ExclusiveAppComponent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.delay

data class ConferenceUiState(
    val conferenceConnected: Boolean = false,
    val microStatus: Boolean = false,
    val speakerStatus: Boolean = false,
    val videoCallOn: Boolean = false,
    val duration: Int = 0 // tính bằng milliseconds
)

class ConferenceActivity : ComponentActivity() {
    private var mFlutterEngine: FlutterEngine? = null
    private val ENGINE_ID = "quickom_engine_id"

    private val uiState = mutableStateOf(ConferenceUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo và gán trực tiếp vào biến toàn cục của Class để tránh leak memory
//        val engine = FlutterEngine(this)
        val engine = FlutterEngineCache.getInstance().get(ENGINE_ID) ?: FlutterEngine(this)
        mFlutterEngine = engine

        // --- GIẢI PHÁP SỬA LỖI MATCH TYPE ---
        // Bọc 'this' vào ExclusiveAppComponent để làm hài lòng Flutter SDK
        val activityProvider = object : ExclusiveAppComponent<Activity> {
            override fun detachFromFlutterEngine() {

            }
            override fun getAppComponent(): Activity = this@ConferenceActivity
        }

        // Gắn kết chặt chẽ Activity và Vòng đời vào Engine để các Plugin phần cứng hoạt động được
        engine.activityControlSurface.attachToActivity(activityProvider, this.lifecycle)

        // Đăng ký Method Channel tiếp nhận event từ Flutter truyền lên
        MethodChannel(engine.dartExecutor.binaryMessenger, "quickom/conference")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "onConferenceConnecting" -> {
                        uiState.value = uiState.value.copy(conferenceConnected = false)
                        result.success(null)
                    }
                    "onConferenceConnected" -> {
                        uiState.value = uiState.value.copy(conferenceConnected = true)
                        result.success(null)
                    }
                    "onEndConference" -> {
                        val reason = call.argument<Boolean>("reason") ?: ""
                        Log.d("ConferenceScreen", "onEndConference with reason = $reason")
                        finishActivityFromFlutter()
                        result.success(null)
                    }
                    "onShowConference" -> {
                        uiState.value = uiState.value.copy(videoCallOn = true)
                        result.success(null)
                    }
                    "onHideConference" -> {
                        uiState.value = uiState.value.copy(videoCallOn = false)
                        result.success(null)
                    }
                    "onAudioLevelChanged" -> {
                        result.success(null)
                    }
                    "onUpdateMicroStatus" -> {
                        val isMute = call.argument<Boolean>("isMute") ?: false
                        // microStatus = true ứng với Mic mở (Not Mute)
                        uiState.value = uiState.value.copy(microStatus = !isMute)
                        result.success(null)
                    }
                    "onUpdateSpeakerStatus" -> {
                        val isSpeakerOn = call.argument<Boolean>("isSpeakerOn") ?: false
                        uiState.value = uiState.value.copy(speakerStatus = isSpeakerOn)
                        result.success(null)
                    }
                    "onUpdateParticipant" -> {
                        val participantList = call.argument<List<Map<String,Any>>>("participants");
                        Log.d("ConferenceScreen", "onUpdateParticipant = $participantList")
                        result.success(null)
                    }
                    "onChatReceived" -> {
                        val chatInfo = call.argument<Map<String,Any>>("chat");
                        Log.d("ConferenceScreen", "onChatReceived = $chatInfo")
                        result.success(null)
                    }
                    "onChatDeleted" -> {
                        val chatInfo = call.argument<Map<String,Any>>("chat");
                        Log.d("ConferenceScreen", "onChatDeleted = $chatInfo")
                        result.success(null)
                    }
                    "onAddParticipantRequest" -> {
                        result.success(null)
                    }
                    else -> {
                        result.notImplemented()
                    }
                }
            }
        engine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // 2. Cache flutterEngine
        FlutterEngineCache.getInstance().put(ENGINE_ID, engine)

        val name = intent.getStringExtra("NAME") ?: ""
        val alias = intent.getStringExtra("ALIAS") ?: ""
        val token = intent.getStringExtra("TOKEN") ?: ""
        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        val role = intent.getStringExtra("ROLE") ?: ""

        setContent {
            DemoAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    ConferenceScreen(
                        name = name,
                        onBackOrEnd = { finish() },
                        alias = alias,
                        token = token,
                        role = role,
                        roomName = roomName,
                        uiState = uiState.value,
                    )
                }
            }
        }
    }

    private fun finishActivityFromFlutter() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 3. Giải phóng chính xác Engine đã được cache
        if (isFinishing) {
            mFlutterEngine?.activityControlSurface?.detachFromActivity()

            FlutterEngineCache.getInstance().remove(ENGINE_ID)
            mFlutterEngine?.destroy()
            mFlutterEngine = null
        }
    }
}

@Composable
fun ConferenceScreen(
    alias: String,
    name: String,
    token: String,
    roomName: String,
    role: String,
    uiState: ConferenceUiState,
    onBackOrEnd: () -> Unit
) {
    val ENGINE_ID = "quickom_engine_id"

    val methodChannel = remember {
        val engine = FlutterEngineCache.getInstance().get(ENGINE_ID)
        engine?.let { MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference") }
    }

    val callArgs = remember(alias, name, token, roomName) {
        mapOf(
            "alias" to alias,
            "name" to name,
            "roomName" to roomName,
            "token" to token,
            "role" to role,
            "conferenceDomain" to "https://realtime-staging.api.datagram.network",
            "storageDomain" to "https://storage.beowulfchain.com",
            "locale" to "vi"
        )
    }

    // Tự động kích hoạt khi vào màn hình
    LaunchedEffect(Unit) {
        delay(2000)
        Log.d("ConferenceScreen", "Màn hình Native hiện lên -> Kích hoạt startConference ngầm")
        methodChannel?.invokeMethod("startConference", callArgs, object : MethodChannel.Result {
            override fun success(result: Any?) {
                Log.d("ConferenceScreen", "startConference phản hồi thành công: $result")
            }

            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                Log.e("ConferenceScreen", "Lỗi khi gọi startConference: $errorMessage")
            }

            override fun notImplemented() {
                Log.e("ConferenceScreen", "Hàm startConference chưa định nghĩa bên Flutter")
            }
        })
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = roomName.ifEmpty { name },
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (uiState.conferenceConnected) formatDuration(uiState.duration) else "Đang kết nối...",
                fontSize = 18.sp,
                color = if (uiState.conferenceConnected) Color(0xFF10B981) else Color(0xFF3B82F6),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(40.dp))

            Surface(
                modifier = Modifier.size(160.dp),
                shape = CircleShape,
                color = Color(0xFFD2E3FC),
                border = BorderStroke(1.dp, Color(0xFF3B82F6))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        modifier = Modifier.size(90.dp),
                        tint = Color(0xFF1A73E8)
                    )
                }
            }
        }

        // Thanh công cụ Bottom điều khiển cuộc gọi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // MICRO BUTTON
            CallActionButton(
                icon = if (uiState.microStatus) Icons.Default.Mic else Icons.Default.MicOff,
                label = if (uiState.microStatus) "TẮT MIC" else "BẬT MIC",
                iconColor = if (uiState.microStatus) Color(0xFF1A73E8) else Color.Gray,
                onClick = {
                    val mapArgs = mapOf("enabled" to uiState.microStatus)
                    methodChannel?.invokeMethod("setMicrophoneStatus", mapArgs, null)
                }
            )

            // SPEAKER BUTTON
            CallActionButton(
                icon = if (uiState.speakerStatus) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                label = if (uiState.speakerStatus) "LOA NGOÀI" else "LOA TRONG",
                iconColor = if (uiState.speakerStatus) Color(0xFF1A73E8) else Color.Gray,
                onClick = {
                    val mapArgs = mapOf("enabled" to uiState.speakerStatus)
                    methodChannel?.invokeMethod("setSpeakerStatus", mapArgs, null)
                }
            )

            val context = LocalContext.current
            // VIDEO CALL BUTTON
            CallActionButton(
                icon = if (uiState.videoCallOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                label = "VIDEO CALL",
                iconColor = if (uiState.videoCallOn) Color(0xFF1A73E8) else Color.Gray,
                onClick = {
                    // Chỉ cho bật video khi đã kết nối thành công.
                    if (uiState.conferenceConnected == true) {
                        Log.d("ConferenceScreen", "Người dùng click nút Video Call -> Gọi openConference")
                        val intent = FlutterActivity.withCachedEngine(ENGINE_ID)
                            .destroyEngineWithActivity(false)
                            .build(context)
                        context.startActivity(intent)

                        methodChannel?.invokeMethod("openConference", callArgs, object : MethodChannel.Result {
                            override fun success(result: Any?) {
                                Log.d("ConferenceScreen", "Mở openConference thành công")
                            }

                            override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                                Log.e("ConferenceScreen", "Lỗi khi gọi openConference: $errorMessage")
                            }

                            override fun notImplemented() {
                                Log.e("ConferenceScreen", "Hàm openConference chưa định nghĩa bên Flutter")
                            }
                        })
                    }
                }
            )

            // END CALL BUTTON
            CallActionButton(
                icon = Icons.Default.CallEnd,
                label = "KẾT THÚC",
                iconColor = Color.White,
                backgroundColor = Color(0xFFEA4335),
                onClick = {
                    Log.d("ConferenceScreen", "Người dùng click nút End -> Gọi nativeEndConference")
                    methodChannel?.invokeMethod("nativeEndConference", callArgs, object : MethodChannel.Result {
                        override fun success(result: Any?) {
                            Log.d("ConferenceScreen", "nativeEndConference thành công")
                            onBackOrEnd()
                        }

                        override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
                            Log.e("ConferenceScreen", "Lỗi khi gọi nativeEndConference: $errorMessage")
                        }

                        override fun notImplemented() {
                            Log.e("ConferenceScreen", "Hàm nativeEndConference chưa định nghĩa bên Flutter")
                        }
                    })
                }
            )
        }
    }
}

fun formatDuration(ms: Int): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
fun CallActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    backgroundColor: Color = Color.White,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
            modifier = Modifier.size(64.dp),
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            border = if (backgroundColor == Color.White) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}