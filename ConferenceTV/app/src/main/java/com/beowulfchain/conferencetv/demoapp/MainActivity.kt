package com.beowulfchain.conferencetv.demoapp
import android.Manifest
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.beowulfchain.conferencetv.demoapp.ui.theme.DemoAppTheme

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize

import androidx.tv.material3.Text
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    TVDemoScreen()
                }
            }
        }

        // 1. Create flutter engine
        val flutterEngine = FlutterEngine(this)

        try {
            GeneratedPluginRegistrant.registerWith(flutterEngine)
        } catch (e: Exception) {
            Log.e("SDK", "Cannot register plugin", e)
        }
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "quickom/conference")
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "onConferenceConnecting" -> {
                        Log.d("ConferenceScreen", "onConferenceConnecting")
                        result.success(null)
                    }
                    "onConferenceConnected" -> {
                        Log.d("ConferenceScreen", "onConferenceConnected")
                        result.success(null)
                    }
                    "onEndConference" -> {
                        val reason = call.argument<String>("reason") ?: ""
                        Log.d("ConferenceScreen", "onEndConference with reason = $reason")
                        finishActivityFromFlutter()
                        result.success(true)
                    }
                    "onShowConference" -> {
                        Log.d("ConferenceScreen", "onShowConference")
                        result.success(null)
                    }
                    "onHideConference" -> {
                        Log.d("ConferenceScreen", "onHideConference")
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
                    "onRequestFriendList" -> {
                        Log.d("ConferenceScreen", "onRequestFriendList")
                        result.success(null)

                        // Giả lập sau khi xử lý xong hoặc lấy data từ Server về:
                        val friendList = listOf(
                            mapOf("name" to "Jenny", "avatar" to "https://i.pravatar.cc/400?img=65", "id" to "123"),
                            mapOf("name" to "Võ Nam", "avatar" to "https://i.pravatar.cc/400?img=47", "id" to "124"),
                            mapOf("name" to "Ngọc Lan", "avatar" to "https://i.pravatar.cc/400?img=34", "id" to "125")
                        )

                        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
                        engine?.let {
                            MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
                                "onResponseFriendList",
                                friendList
                            )
                        }
                    }
                    "onAddParticipant" -> {
                        val friendId = call.argument<Map<String,Any>>("friend")
                        Log.d("ConferenceScreen", "onAddParticipant friendId = $friendId")
                        result.success(null)
                    }
                    else -> result.notImplemented()
                }
            }
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        // 2. Cache flutterEngine with quickom_engine_id
        FlutterEngineCache.getInstance().put("quickom_engine_id", flutterEngine)
    }

    private fun finishActivityFromFlutter() {
        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
        engine?.navigationChannel?.setInitialRoute("/")
    }

    @Composable
    fun TVDemoScreen() {
        val focusManager = LocalFocusManager.current

        var alias by remember { mutableStateOf("NtSYP") }
        var name by remember { mutableStateOf("Kim Yến") }
        var token by remember { mutableStateOf("0004") }

        val interactionHost = remember { MutableInteractionSource() }
        val isHostFocused by interactionHost.collectIsFocusedAsState()

        val interactionJoin = remember { MutableInteractionSource() }
        val isJoinFocused by interactionJoin.collectIsFocusedAsState()

        val joinLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                onJoinButtonClicked(alias = alias, name = name);
            }
        }

        val hostLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.all { it }) {
                onHostButtonClicked(alias = alias, name = name, token = token);
            }
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Demo Conference TV",
                style = MaterialTheme.typography.headlineLarge, // Large font for TV
                modifier = Modifier.padding(bottom = 40.dp)   // Space below the title
            )

            // Input Fields
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                label = { Text("Alias") },
                modifier = Modifier.fillMaxWidth(0.6f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), // Hiển thị nút "Next" trên bàn phím
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) } // Nhấn enter/next sẽ nhảy xuống dưới
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    // Màu chữ khi người dùng nhập
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue,

                    // Màu của đường viền (Border)
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,

                    // Màu của nhãn (Label) khi được chọn và không được chọn
                    focusedLabelColor = Color.Blue,
                    unfocusedLabelColor = Color.Gray,

                    // Màu của con trỏ (Cursor)
                    cursorColor = Color.Blue
                )

            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(0.6f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    // Màu chữ khi người dùng nhập
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue,

                    // Màu của đường viền (Border)
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,

                    // Màu của nhãn (Label) khi được chọn và không được chọn
                    focusedLabelColor = Color.Blue,
                    unfocusedLabelColor = Color.Gray,

                    // Màu của con trỏ (Cursor)
                    cursorColor = Color.Blue
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Token") },
                modifier = Modifier.fillMaxWidth(0.6f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // Hiển thị nút "Done" (Tích chọn hoàn thành)
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() } // Ẩn bàn phím và bỏ focus
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    // Màu chữ khi người dùng nhập
                    focusedTextColor = Color.Blue,
                    unfocusedTextColor = Color.Blue,

                    // Màu của đường viền (Border)
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,

                    // Màu của nhãn (Label) khi được chọn và không được chọn
                    focusedLabelColor = Color.Blue,
                    unfocusedLabelColor = Color.Gray,

                    // Màu của con trỏ (Cursor)
                    cursorColor = Color.Blue
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        hostLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red, // Màu nền nút
                        contentColor = Color.White  // Màu chữ/icon bên trong nút
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        .then(
                            if (isHostFocused) Modifier.border(10.dp, Color.Blue, RoundedCornerShape(20.dp))
                            else Modifier
                        )
                ) {
                    Text("Host")
                }
                Button(
                    onClick = {
                        joinLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green, // Màu nền nút
                        contentColor = Color.Black  // Màu chữ/icon bên trong nút
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .width(100.dp)
                        .height(40.dp)
                        .then(
                            if (isJoinFocused) Modifier.border(10.dp, Color.Blue, RoundedCornerShape(20.dp))
                            else Modifier
                        )
                ) {
                    Text("Join")
                }
            }
        }
    }

    fun onHostButtonClicked(alias: String, name: String, token: String) {
        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
//        val testAlias = "8ob37";
//        val testName = "KinhHost";
//        val testToken = "SFMyNTY.ZDRhNGJmNDMtNDZlOS00ZDU4LTgzMmUtNDA1ZjdjMzI3NWU1.Lk4Cm0d87gwD6hsSZ14Ycsv4EwrS1CdzxqzcHsmx7K0";
        val conferenceDomain = "https://realtime-staging.api.datagram.network";
        val storageDomain = "https://storage.beowulfchain.com";
//        val conferenceDomain = "https://signal-mytv.quickom.com";
//        val storageDomain = "https://storage.beowulfchain.com";

        val locale = "vi";

        // For testing purpose, we use jsonbin (https://jsonbin.io/) to fetch token from code
        val testAlias = alias;
        val testName = name;

        val tokenService = JsonBinService()

        MainScope().launch {
            val testToken = tokenService.fetchToken(token)
//            val testToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhbGlhcyI6IjM1dzVoIiwidXNlcl9pZCI6ImY2ZDYxMmIzLWZhYzctNDViMC04MGU2LTFiMTZlY2I3MTI1NCIsImhvc3QiOnRydWUsImV4cGlyZXNfaW4iOjYwfQ.hxqQfACEKTXeuNgGCsXlSUUqToQcIBZ5J2ACD267AyM"
            if (testToken != null && testAlias.isNotEmpty()) {
                engine?.let {
                    // Send data to Flutter before hand
                    // "quickom/conference" must match with channel in Flutter side
                    MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
                        "openConference",
                        mapOf(
                            "alias" to testAlias,
                            "name" to testName,
                            "token" to testToken,
                            "conferenceDomain" to conferenceDomain,
                            "storageDomain" to storageDomain,
                            "locale" to locale,
                            "avatar" to "https://i.pravatar.cc/400?img=36",
                            "remoteName" to "Hoàng Hà",
                            "remoteAvatar" to "https://i.pravatar.cc/400?img=14"
                        )
                    )
                }

                // Open FlutterActivity using engine with data
                startActivity(
                    FlutterActivity
                        .withCachedEngine("quickom_engine_id")
                        .build(this@MainActivity)
                )
            } else {
                Toast.makeText(this@MainActivity, "Token and alias is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onJoinButtonClicked(alias: String, name: String) {
        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
//        val testAlias = "088zv";
//        val testName = "KinhChen";
        val conferenceDomain = "https://realtime-staging.api.datagram.network";
        val storageDomain = "https://storage.beowulfchain.com";
        val locale = "vi";

        val testAlias = alias;
        val testName = name;

        if (testAlias.isNotEmpty()) {
            engine?.let {
                // Send data to Flutter before hand
                // "quickom/conference" must match with channel in Flutter side
                MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
                    "openConference",
                    mapOf(
                        "alias" to testAlias,
                        "name" to testName,
                        "conferenceDomain" to conferenceDomain,
                        "storageDomain" to storageDomain,
                        "locale" to locale,
                        "avatar" to "https://i.pravatar.cc/400?img=14",
                        "remoteName" to "Kim Yến",
                        "remoteAvatar" to "https://i.pravatar.cc/400?img=36"
                    )
                )
            }

            // Open FlutterActivity using engine with data
            startActivity(
                FlutterActivity
                    .withCachedEngine("quickom_engine_id")
                    .build(this)
            )
        }
        else {
            Toast.makeText(this@MainActivity, "Alias is required", Toast.LENGTH_SHORT).show();
        }
    }
}


class JsonBinService {
    private val client = OkHttpClient()

    // Thay bằng thông tin từ tài khoản JSONbin của bạn
    private val binId = "6a02ae07adc21f119a88e73c"
    private val apiKey = "\$2a\$10\$qCQqFLr1PHPdys8PfcD8VePPddlez/Sy.siIhIthT3jRmG0HxrS5u";

    suspend fun fetchToken(shortCode: String): String? = withContext(Dispatchers.IO) {
        val url = "https://api.jsonbin.io/v3/b/$binId/latest"

        val request = Request.Builder()
            .url(url)
            .addHeader("X-Master-Key", apiKey)
            .addHeader("X-Bin-Meta", "false") // Chỉ lấy nội dung record, không lấy meta data
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val jsonData = response.body?.string()
                    if (jsonData != null) {
                        val jsonObject = JSONObject(jsonData)
                        // Trả về token tương ứng với mã code, nếu không có trả về null
                        return@withContext if (jsonObject.has(shortCode)) {
                            jsonObject.getString(shortCode)
                        } else null
                    }
                }
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}