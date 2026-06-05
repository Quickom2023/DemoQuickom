package com.beowulfchain.conferencetv.demoapp
import android.Manifest
import com.beowulfchain.conferencetv.demoapp.ui.theme.DemoAppTheme

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState

import android.content.Intent

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
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.text.ifEmpty

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ENGINE_ID = "quickom_engine_id"
        val flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )
        // Lưu vào bộ nhớ Cache
        FlutterEngineCache.getInstance().put(ENGINE_ID, flutterEngine)

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
    }

    @Composable
    fun TVDemoScreen() {
        val focusManager = LocalFocusManager.current

        var alias by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var token by remember { mutableStateOf("") }

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
                .verticalScroll(scrollState)
                .padding(32.dp),

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
        val testAlias = alias.ifEmpty { "8ob37" };
        val testName = name.ifEmpty { "Test Host" };
        val testRoomName = "Demo Room";

        val tokenService = JsonBinService()
        MainScope().launch {
            val testToken = tokenService.fetchToken(token.ifEmpty { "0001" })
            if (testToken != null && testAlias.isNotEmpty()) {
                val intent = Intent(this@MainActivity, ConferenceActivity::class.java).apply {
                    putExtra("ALIAS", testAlias.ifEmpty { "" })
                    putExtra("NAME", testName.ifEmpty { "Host name" })
                    putExtra("TOKEN", testToken.ifEmpty { "" })
                    putExtra("ROOM_NAME", testRoomName.ifEmpty { "Default Room Name" })
                    putExtra("ROLE", "host")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "Token and alias is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onJoinButtonClicked(alias: String, name: String) {
        val testAlias = alias.ifEmpty { "8ob37" };
        val testName = name.ifEmpty { "Test Participant" };
        val testRoomName = "Demo Room";
        val testToken = "";

        if (testAlias.isNotEmpty()) {
            val intent = Intent(this@MainActivity, ConferenceActivity::class.java).apply {
                putExtra("ALIAS", testAlias.ifEmpty { "" })
                putExtra("NAME", testName.ifEmpty { "Member" })
                putExtra("TOKEN", testToken.ifEmpty { "" })
                putExtra("ROOM_NAME", testRoomName.ifEmpty { "Default Room Name" })
                putExtra("ROLE", "participant")
            }
            startActivity(intent)
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