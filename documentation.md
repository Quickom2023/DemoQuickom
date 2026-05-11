# Quickom Conference SDK Documentation

## 1. Introduction / Giới thiệu
**EN:** The Quickom Conference SDK allows developers to integrate high-quality video conferencing into Android applications using a Flutter-based engine. It supports two primary roles: **Host** and **Joiner**.

**VI:** Quickom Conference SDK cho phép nhà phát triển tích hợp tính năng hội nghị truyền hình chất lượng cao vào ứng dụng Android thông qua nền tảng Flutter. SDK hỗ trợ hai vai trò chính: **Chủ phòng (Host)** và **Người tham gia (Joiner)**.

---

## 2. Installation / Cài đặt

### A. Settings Gradle Configuration
**EN:** Add the following repositories to your `settings.gradle.kts` file to allow the project to resolve SDK dependencies.

**VI:** Thêm các repository sau vào file `settings.gradle.kts` để project có thể tìm thấy các thư viện cần thiết.

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("[https://storage.googleapis.com/download.flutter.io](https://storage.googleapis.com/download.flutter.io)") }
        maven { url = uri("[https://jitpack.io](https://jitpack.io)") }

        // Repository for SDK artifacts
        maven { url = uri("../SDK/repo") }
    }
}
```

---

### B. Build Gradle Configuration
**EN:** Add the SDK dependencies to your app-level build.gradle.kts. Use debugImplementation for development and releaseImplementation for production builds.

**VI:** Thêm các dependency của SDK vào file build.gradle.kts cấp app. Sử dụng debugImplementation cho bản debug và releaseImplementation cho bản release.

```kotlin
dependencies {
    debugImplementation("com.beowulfchain.flutter_sdk_packer:flutter_debug:1.0")
    releaseImplementation("com.beowulfchain.flutter_sdk_packer:flutter_release:1.0")
}
```

## 3. Core Functions / Các chức năng chính

### A. Host Conference (Tổ chức cuộc họp)
**EN:** Used when a user wants to start and manage a room. This requires a `token` for authentication.

**VI:** Sử dụng khi người dùng muốn bắt đầu và quản lý một phòng họp. Chế độ này yêu cầu một mã `token` để xác thực quyền chủ phòng.

**Required Parameters / Tham số bắt buộc:**
* `alias`: Room unique identifier (ID phòng).
* `name`: Host display name (Tên hiển thị chủ phòng).
* `token`: Security token for hosting rights (Mã xác thực chủ phòng).
* `conferenceDomain`: Conference API endpoint (Server điều phối).
* `storageDomain`: Asset storage endpoint (Server lưu trữ).

### B. Join Conference (Tham gia cuộc họp)
**EN:** Used for participants entering an existing room. No token is required.

**VI:** Sử dụng cho người tham gia vào phòng họp đã có sẵn. Không yêu cầu mã token.

**Required Parameters / Tham số bắt buộc:**
* `alias`: Existing room identifier (ID phòng hiện có).
* `name`: Participant display name (Tên hiển thị người tham gia).
* `conferenceDomain`: Conference API endpoint.
* `storageDomain`: Asset storage endpoint.

---

## 4. Implementation Example / Ví dụ triển khai (Android/Kotlin)

### Setup / Cấu hình
**EN:** The `FlutterEngine` must be pre-warmed and cached with the ID `"quickom_engine_id"`.

**VI:** `FlutterEngine` cần được khởi tạo sẵn và lưu vào cache với ID `"quickom_engine_id"`.

### Method Channel
* **Channel Name:** `quickom/conference`
* **Method Name:** `openConference`

#### Code Snippet (Kotlin):

```kotlin
/**
 * Host a conference room
 */
fun onHostButtonClicked(alias: String, name: String, token: String) {
    val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
    
    // Configuration Domains
    val conferenceDomain = "[https://realtime-staging.api.datagram.network](https://realtime-staging.api.datagram.network)"
    val storageDomain = "[https://storage.beowulfchain.com](https://storage.beowulfchain.com)"

    // 1. Launch the Flutter Activity
    startActivity(
        FlutterActivity.withCachedEngine("quickom_engine_id").build(this)
    )

    // 2. Pass data to Flutter via MethodChannel
    engine?.let {
        MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
            "openConference",
            mapOf(
                "alias" to alias,
                "name" to name,
                "token" to token, // Required for Host
                "conferenceDomain" to conferenceDomain,
                "storageDomain" to storageDomain
            )
        )
    }
}

/**
 * Join an existing conference room
 */
fun onJoinButtonClicked(alias: String, name: String) {
    val engine = FlutterEngineCache.getInstance().get("quickom_engine_id")
    
    val conferenceDomain = "[https://realtime-staging.api.datagram.network](https://realtime-staging.api.datagram.network)"
    val storageDomain = "[https://storage.beowulfchain.com](https://storage.beowulfchain.com)"

    startActivity(
        FlutterActivity.withCachedEngine("quickom_engine_id").build(this)
    )

    engine?.let {
        MethodChannel(it.dartExecutor.binaryMessenger, "quickom/conference").invokeMethod(
            "openConference",
            mapOf(
                "alias" to alias,
                "name" to name,
                "conferenceDomain" to conferenceDomain,
                "storageDomain" to storageDomain
            )
        )
    }
}
```
