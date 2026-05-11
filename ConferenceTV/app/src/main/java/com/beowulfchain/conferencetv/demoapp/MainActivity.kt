package com.beowulfchain.conferencetv.demoapp
import android.Manifest
import com.beowulfchain.conferencetv.demoapp.ui.theme.DemoAppTheme

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts

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

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant


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
                    "closeConference" -> {
                        finishActivityFromFlutter()
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
        var alias by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var token by remember { mutableStateOf("") }

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

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("Token") },
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = {
                    hostLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) {
                    Text("Host")
                }
                Button(onClick = {
                    joinLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) {
                    Text("Join")
                }
            }
        }
    }

    fun onHostButtonClicked(alias: String, name: String, token: String) {
        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
        val testAlias = "8ob37";
        val testName = "KinhHost";
        val testToken = "SFMyNTY.ZDRhNGJmNDMtNDZlOS00ZDU4LTgzMmUtNDA1ZjdjMzI3NWU1.Lk4Cm0d87gwD6hsSZ14Ycsv4EwrS1CdzxqzcHsmx7K0";
        val conferenceDomain = "https://realtime-staging.api.datagram.network";
        val storageDomain = "https://storage.beowulfchain.com";

        // Open FlutterActivity using engine with data
        startActivity(
            FlutterActivity
                .withCachedEngine("quickom_engine_id")
                .build(this)
        )

        engine?.let {
            // Send data to Flutter before hand
            // "quickom/conference" must match with channel in Flutter side
            MethodChannel(it.dartExecutor.binaryMessenger, "quickom/channel").invokeMethod(
                "openConference",
                mapOf(
                    "alias" to testAlias,
                    "name" to testName,
                    "token" to testToken,
                    "conferenceDomain" to conferenceDomain,
                    "storageDomain" to storageDomain
                )
            )
        }
    }

    fun onJoinButtonClicked(alias: String, name: String) {
        val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
        val testAlias = "088zv";
        val testName = "KinhChen";
        val conferenceDomain = "https://realtime-staging.api.datagram.network";
        val storageDomain = "https://storage.beowulfchain.com";

        // Open FlutterActivity using engine with data
        startActivity(
            FlutterActivity
                .withCachedEngine("quickom_engine_id")
                .build(this)
        )

        engine?.let {
            // Send data to Flutter before hand
            // "quickom/conference" must match with channel in Flutter side
            MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
                "openConference",
                mapOf(
                    "alias" to testAlias,
                    "name" to testName,
                    "conferenceDomain" to conferenceDomain,
                    "storageDomain" to storageDomain
                )
            )
        }
    }
}