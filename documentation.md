# Quickom Conference SDK Documentation

## 1. Introduction

The Quickom Conference SDK allows developers to integrate high-quality video conferencing into Android applications using a Flutter-based engine. It supports two primary roles: **Host** and **Joiner**.

---

## 2. Installation

### A. Settings Gradle Configuration

Copy SDK/repo/ folder to your project and add the following repositories to your `settings.gradle.kts` file to allow the project to resolve SDK dependencies.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
        maven { url = uri("https://jitpack.io") }

        // Repository for SDK artifacts
        maven { url = uri("../SDK/repo") }
    }
}

```

---

### B. Build Gradle Configuration

Add the SDK dependencies to your app-level build.gradle.kts. Use debugImplementation for development and releaseImplementation for production builds.

```kotlin
dependencies {
    debugImplementation("com.beowulfchain.flutter_sdk_packer:flutter_debug:1.0")
    releaseImplementation("com.beowulfchain.flutter_sdk_packer:flutter_release:1.0")
}

```

---

### C. Application Manifest

Add following permission to <manifest> in AndroidMainifest.xml
```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE />
```

Add following feature to <manifest> in AndroidManifest.xml
```xml
<uses-feature android:name="android.hardware.camera"
    android:required="false" />
<uses-feature android:name="android.hardware.camera.autofocus"
    android:required="false" />
<uses-feature android:name="android.hardware.microphone"
    android:required="false" />
<uses-feature
    android:name="android.hardware.touchscreen"
    android:required="false" />
<uses-feature
    android:name="android.software.leanback"
    android:required="false" />

<uses-feature android:name="android.hardware.usb.host" />
```

---

## 3. Core Functions

### A. Host Conference

Used when a user wants to start and manage a room. This requires a `token` for authentication.

**Parameters:**

* `alias`: Room unique identifier.
* `name`: Host display name.
* `token`: Security token for hosting rights.
* `conferenceDomain`: Conference API endpoint.
* `storageDomain`: Asset storage endpoint.
* `locale`: Set desired language.
* `avatar`: URL to user avatar image.
* `remoteName`: Name of participant to call.
* `remoteAvatar`: URL to participant's avatar.

### B. Join Conference

Used for participants entering an existing room. No token is required.

**Parameters:**

* `alias`: Existing room identifier.
* `name`: Participant display name.
* `conferenceDomain`: Conference API endpoint.
* `storageDomain`: Asset storage endpoint.
* `locale`: Set desired language.
* `avatar`: URL to user avatar image.
* `remoteName`: Name of participant to call.
* `remoteAvatar`: URL to participant's avatar.

---

## 4. Implementation Example (Android/Kotlin)

### Setup

The `FlutterEngine` must be pre-warmed and cached with the ID `"quickom_engine_id"`.

```kotlin
 // 1. Create flutter engine
val flutterEngine = FlutterEngine(this)

try {
    GeneratedPluginRegistrant.registerWith(flutterEngine)
} catch (e: Exception) {
    Log.e("SDK", "Cannot register plugin", e)
}
 // 2. Handle call from method channel
MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "quickom/conference")
    .setMethodCallHandler { call, result ->
        when (call.method) {

        }
    }
 // 3. Execute flutter engine entry point
flutterEngine.dartExecutor.executeDartEntrypoint(
        DartExecutor.DartEntrypoint.createDefault()
    )
 // 4. Cache flutterEngine with quickom_engine_id
FlutterEngineCache.getInstance().put("quickom_engine_id", flutterEngine)
```

Initialize SDK (required), must call after FlutterEngine pre-warmed and cached.

```kotlin
startupSDK();
```

```kotlin
fun startupSDK() {
    val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
    engine?.let {
        MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
            "startupSDK",
            null
        )
    }
}
```

### Method Channel

* **Channel Name:** `quickom/conference`

---

### Methods (Native to Flutter)

#### 1. openConference

Joins the conference and simultaneously opens the conference user interface.

```kotlin
fun onHostButtonClicked(alias: String, name: String, token: String, avatar: String, remoteName: String, remoteAvatar: String) {
    val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
    val locale = "en"
    val conferenceDomain = "https://realtime-staging.api.datagram.network"
    val storageDomain = "https://storage.beowulfchain.com"

    // 1. Pass data to Flutter via MethodChannel
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
                "remoteAvatar" to "https://i.pravatar.cc/400?img=14",
                "videoOnStarted" to true
            )
        )
    }
    // 2. Launch the Flutter Activity
    startActivity(
        FlutterActivity
            .withCachedEngine("quickom_engine_id")
            .build(this@MainActivity)
    )
}

```

#### 2. startConference

Starts the conference in the background without opening the conference user interface immediately.

```kotlin
fun startConferenceInBackground(alias: String, name: String, token: String, avatar: String, remoteName: String, remoteAvatar: String) {
    val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
    val locale = "en"
    val conferenceDomain = "https://realtime-staging.api.datagram.network"
    val storageDomain = "https://storage.beowulfchain.com"

    engine?.let {
        MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
            "startConference",
            mapOf(
                "alias" to alias,
                "name" to name,
                "token" to token,
                "conferenceDomain" to conferenceDomain,
                "storageDomain" to storageDomain,
                "locale" to locale,
                "avatar" to avatar,
                "remoteName" to remoteName,
                "remoteAvatar" to remoteAvatar,
                "videoOnStarted" to true
            )
        )
    }
}

```

#### 3. nativeEndConference

Allows the native side to programmatically terminate the conference.

```kotlin
methodChannel?.invokeMethod("nativeEndConference", null)

```

#### 4. getMicrophoneStatus / getCameraStatus / getSpeakerStatus

Retrieves the current status of the hardware components. Returns a boolean value (`true` for ON, `false` for OFF).

```kotlin
methodChannel?.invokeMethod("getMicrophoneStatus", null, object : MethodChannel.Result {
    override fun success(result: Any?) {
        val isMuted = result as? Boolean ?: false
        Log.d("Conference", "Microphone enabled: $isMuted")
    }
    override fun error(code: String, msg: String?, details: Any?) {}
    override fun notImplemented() {}
})

```

#### 5. getConferenceDuration

Retrieves the total elapsed time of the current conference in milliseconds.

```kotlin
methodChannel?.invokeMethod("getConferenceDuration", null, object : MethodChannel.Result {
    override fun success(result: Any?) {
        val durationMs = result as? Long ?: 0L
        Log.d("Conference", "Duration: $durationMs ms")
    }
    override fun error(code: String, msg: String?, details: Any?) {}
    override fun notImplemented() {}
})

```

#### 6. setMicrophoneStatus

Sets the microphone status to either on or off.

```kotlin
val mapArgs = mapOf("enabled" to uiState.microStatus)
methodChannel?.invokeMethod("setMicrophoneStatus", mapArgs, null)

```

#### 7. setCameraStatus

Sets the camera status to either on or off.

```kotlin
val mapArgs = mapOf("enabled" to uiState.cameraStatus)
methodChannel?.invokeMethod("setCameraStatus", mapArgs, null)

```

#### 8. setSpeakerStatus

Sets the speaker status to either on or off.

```kotlin
val mapArgs = mapOf("enabled" to uiState.speakerStatus)
methodChannel?.invokeMethod("setSpeakerStatus", mapArgs, null)

```

#### 9. onResponseFriendList

Sends a list of friends to the conference to be displayed within the UI.

```kotlin
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

```

---

### Events (Flutter to Native Listener)

Native code can handle incoming events triggered from the conference engine via `MethodCallHandler`:

```kotlin
methodChannel.setMethodCallHandler { call, result ->
    when (call.method) {
        "onConferenceConnecting" -> {
            Log.d("ConferenceScreen", "Conference is connecting...")
            result.success(null)
        }

        "onConferenceJoined" -> {
            Log.d("ConferenceScreen", "Conference joined successfully. Everyone in conference can see you now.")
            result.success(null)
        }
        
        "onConferenceConnected" -> {
            Log.d("ConferenceScreen", "Conference connected successfully. At least one other participant has joined.")
            result.success(null)
        }
        
        "onEndConference" -> {
            val reason = call.argument<String>("reason") ?: ""
            Log.d("ConferenceScreen", "onEndConference with reason = $reason")
            finishActivityFromFlutter()
            result.success(null)
        }
        
        "onShowConference" -> {
            Log.d("ConferenceScreen", "Conference screen is now visible.")
            result.success(null)
        }
        
        "onHideConference" -> {
            Log.d("ConferenceScreen", "Conference screen hidden. Use 'openConference' to make it visible again.")
            result.success(null)
        }
        
        "onUpdateParticipant" -> {
            val participantList = call.argument<List<Map<String, Any>>>("participants")
            Log.d("ConferenceScreen", "onUpdateParticipant = $participantList")
            result.success(null)
        }
        
        "onChatReceived" -> {
            val chatInfo = call.argument<Map<String, Any>>("chat")
            Log.d("ConferenceScreen", "onChatReceived = $chatInfo")
            result.success(null)
        }
        
        "onRequestFriendList" -> {
            Log.d("ConferenceScreen", "onRequestFriendList")
            result.success(null)

            // Simulating fetching data from Server or local storage:
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
            val friendId = call.argument<Map<String, Any>>("friend")
            Log.d("ConferenceScreen", "onAddParticipant friendId = $friendId")
            result.success(null)
        }
        
        else -> result.notImplemented()
    }
}

```