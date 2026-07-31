//
//  ViewController.swift
//  Conference_iOS
//
//  Created by Kinh Tran on 30/7/26.
//

import UIKit
import SwiftUI
import Flutter
import FlutterPluginRegistrant

// MARK: - App Colors & Theme Setup
struct AppTheme {
    static let purple80 = Color(red: 0.816, green: 0.737, blue: 1.0)
    static let purpleGrey80 = Color(red: 0.8, green: 0.76, blue: 0.86)
    static let pink80 = Color(red: 0.937, green: 0.722, blue: 0.784)
    
    static let purple40 = Color(red: 0.4, green: 0.314, blue: 0.643)
    static let purpleGrey40 = Color(red: 0.384, green: 0.357, blue: 0.443)
    static let pink40 = Color(red: 0.49, green: 0.322, blue: 0.376)
}

// MARK: - Flutter Engine Manager
class FlutterManager {
    static let shared = FlutterManager()
    
    var flutterEngine: FlutterEngine?
    var channel: FlutterMethodChannel?
    
    private init() {}
    
    func setupEngine() {
        let engine = FlutterEngine(name: "quickom_engine_id")
        engine.run()
        
        // Đăng ký các plugin đã cài đặt
        GeneratedPluginRegistrant.register(with: engine)
        
        let binaryMessenger = engine.binaryMessenger
        let channel = FlutterMethodChannel(name: "quickom/conference", binaryMessenger: binaryMessenger)
        
        channel.setMethodCallHandler { [weak self] (call: FlutterMethodCall, result: @escaping FlutterResult) in
            switch call.method {
            case "onConferenceConnecting":
                print("[DemoApp] onConferenceConnecting")
                result(nil)
            case "onConferenceJoined":
                print("[DemoApp] onConferenceJoined")
                result(nil)
            case "onConferenceConnected":
                print("[DemoApp] onConferenceConnected")
                result(nil)
            case "onEndConference":
                let args = call.arguments as? [String: Any]
                let reason = args?["reason"] as? String ?? ""
                print("[DemoApp] onEndConference with reason = \(reason)")
                self?.finishActivityFromFlutter()
                result(true)
            case "onShowConference":
                print("[DemoApp] onShowConference")
                result(nil)
            case "onHideConference":
                print("[DemoApp] onHideConference")
                result(nil)
            case "onUpdateParticipant":
                let participantList = (call.arguments as? [String: Any])?["participants"] as? [[String: Any]]
                print("[DemoApp] onUpdateParticipant = \(String(describing: participantList))")
                result(nil)
            case "onChatReceived":
                let chatInfo = (call.arguments as? [String: Any])?["chat"] as? [String: Any]
                print("[DemoApp] onChatReceived = \(String(describing: chatInfo))")
                result(nil)
            case "onRequestFriendList":
                print("[DemoApp] onRequestFriendList")
                result(nil)
                
                let friendList: [[String: Any]] = [
                    ["name": "Jenny", "avatar": "https://i.pravatar.cc/400?img=65", "id": "18fcb3d0-ef4d-4084-853f-1f013ea858ca"],
                    ["name": "Võ Nam", "avatar": "https://i.pravatar.cc/400?img=47", "id": "f007df73-3715-43ff-bd41-1054cfe20630"],
                    ["name": "Ngọc Lan", "avatar": "https://i.pravatar.cc/400?img=34", "id": "9590941f-67b4-4d66-a851-2ba25338d47b"]
                ]
                
                self?.channel?.invokeMethod("onResponseFriendList", arguments: friendList)
            case "onAddParticipant":
                let friendId = (call.arguments as? [String: Any])?["friend"] as? String
                print("[DemoApp] onAddParticipant friendId = \(friendId ?? "")")
                result(nil)
            default:
                result(FlutterMethodNotImplemented)
            }
        }
        
        self.flutterEngine = engine
        self.channel = channel
        
        startupSDK()
    }
    
    func startupSDK() {
        channel?.invokeMethod("startupSDK", arguments: nil)
    }
    
    func finishActivityFromFlutter() {
        flutterEngine?.navigationChannel.invokeMethod("setInitialRoute", arguments: "/")
    }
}

// MARK: - JsonBin Network Service
class JsonBinService {
    private let binId = "6a02ae07adc21f119a88e73c"
    private let apiKey = "$2a$10$qCQqFLr1PHPdys8PfcD8VePPddlez/Sy.siIhIthT3jRmG0HxrS5u"
    
    func fetchToken(shortCode: String) async -> String? {
        guard let url = URL(string: "https://api.jsonbin.io/v3/b/\(binId)/latest") else { return nil }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.addValue(apiKey, forHTTPHeaderField: "X-Master-Key")
        request.addValue("false", forHTTPHeaderField: "X-Bin-Meta")
        
        do {
            let (data, response) = try await URLSession.shared.data(for: request)
            guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
                return nil
            }
            if let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] {
                return json[shortCode] as? String
            }
        } catch {
            print("Error fetching token: \(error)")
        }
        return nil
    }
}

// MARK: - SwiftUI Demo Screen
struct TVDemoScreen: View {
    @State private var alias: String = "NtSYP"
    @State private var name: String = "Kim Yến"
    @State private var token: String = "0004"
    
    @State private var userIndex = 0
    @State private var alertMessage: String?
    @State private var showAlert = false
    @State private var isLoading = false
    
    let presentingViewController: UIViewController
    
    private let mockRemoteUsers: [[String: String]] = [
        ["name": "Hoàng Hà", "avatar": "https://i.pravatar.cc/400?img=14"],
        ["name": "Jenny Phạm", "avatar": "https://i.pravatar.cc/400?img=65"],
        ["name": "Võ Nam", "avatar": "https://i.pravatar.cc/400?img=47"],
        ["name": "Ngọc Lan", "avatar": "https://i.pravatar.cc/400?img=34"],
        ["name": "Minh Trí", "avatar": "https://i.pravatar.cc/400?img=12"],
        ["name": "Thu Thảo", "avatar": "https://i.pravatar.cc/400?img=5"]
    ]
    
    private let tokenService = JsonBinService()
    
    @FocusState private var focusedField: Field?
    enum Field {
        case alias, name, token
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("Demo Conference TV")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .padding(.bottom, 40)
                
                // Alias Input
                VStack(alignment: .leading, spacing: 4) {
                    Text("Alias")
                        .font(.caption)
                        .foregroundColor(.blue)
                    TextField("", text: $alias)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .foregroundColor(.blue)
                        .focused($focusedField, equals: .alias)
                        .submitLabel(.next)
                        .onSubmit {
                            focusedField = .name
                        }
                }
                .frame(maxWidth: 300)
                
                // Name Input
                VStack(alignment: .leading, spacing: 4) {
                    Text("Name")
                        .font(.caption)
                        .foregroundColor(.blue)
                    TextField("", text: $name)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .foregroundColor(.blue)
                        .focused($focusedField, equals: .name)
                        .submitLabel(.next)
                        .onSubmit {
                            focusedField = .token
                        }
                }
                .frame(maxWidth: 300)
                
                // Token Input
                VStack(alignment: .leading, spacing: 4) {
                    Text("Token")
                        .font(.caption)
                        .foregroundColor(.blue)
                    TextField("", text: $token)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .foregroundColor(.blue)
                        .focused($focusedField, equals: .token)
                        .submitLabel(.done)
                        .onSubmit {
                            focusedField = nil
                        }
                }
                .frame(maxWidth: 300)
                
                Spacer().frame(height: 24)
                
                // Action Buttons
                HStack(spacing: 16) {
                    Button(action: {
                        onHostButtonClicked(alias: alias, name: name, token: token)
                    }) {
                        Text("Host")
                            .frame(width: 100, height: 40)
                            .background(Color.red)
                            .foregroundColor(.white)
                            .cornerRadius(20)
                    }
                    
                    Button(action: {
                        onJoinButtonClicked(alias: alias, name: name)
                    }) {
                        Text("Join")
                            .frame(width: 100, height: 40)
                            .background(Color.green)
                            .foregroundColor(.black)
                            .cornerRadius(20)
                    }
                }
            }
            .padding(32)
        }
        .alert(isPresented: $showAlert) {
            Alert(title: Text("Thông báo"), message: Text(alertMessage ?? ""), dismissButton: .default(Text("OK")))
        }
    }
    
    // MARK: - Host Action
    private func onHostButtonClicked(alias: String, name: String, token: String) {
        let conferenceDomain = "https://realtime-staging.api.datagram.network"
        let storageDomain = "https://storage.beowulfchain.com"
        let locale = "vi"
        
        let testAlias = alias
        let testName = name
        
        Task {
            let testToken = await tokenService.fetchToken(shortCode: token)
            
            if let testToken = testToken, !testAlias.isEmpty {
                let remoteUser = mockRemoteUsers[userIndex % mockRemoteUsers.count]
                userIndex += 1
                let remoteName = remoteUser["name"] ?? ""
                let remoteAvatar = remoteUser["avatar"] ?? ""
                
                let localUser = mockRemoteUsers[(userIndex + 2) % mockRemoteUsers.count]
                let localName = localUser["name"] ?? testName
                let localAvatar = localUser["avatar"] ?? ""
                
                print("[DemoApp] onHostButtonClicked, testAlias = \(testAlias), testName = \(testName), testToken = \(testToken)")
                
                let arguments: [String: Any] = [
                    "alias": testAlias,
                    "name": localName,
                    "token": testToken,
                    "conferenceDomain": conferenceDomain,
                    "storageDomain": storageDomain,
                    "locale": locale,
                    "avatar": localAvatar,
                    "remoteName": remoteName,
                    "remoteAvatar": remoteAvatar,
                    "videoOnStarted": true,
                    "theme": "light"
                ]
                
                FlutterManager.shared.channel?.invokeMethod("openConference", arguments: arguments)
                
                presentFlutterViewController()
            } else {
                alertMessage = "Token and alias is required"
                showAlert = true
            }
        }
    }
    
    // MARK: - Join Action
    private func onJoinButtonClicked(alias: String, name: String) {
        let conferenceDomain = "https://realtime-staging.api.datagram.network"
        let storageDomain = "https://storage.beowulfchain.com"
        let locale = "vi"
        
        let testAlias = alias
        let testName = name
        
        if !testAlias.isEmpty {
            print("[DemoApp] onJoinButtonClicked, testAlias = \(testAlias), testName = \(testName)")
            
            let arguments: [String: Any] = [
                "alias": testAlias,
                "name": testName,
                "conferenceDomain": conferenceDomain,
                "storageDomain": storageDomain,
                "locale": locale,
                "avatar": "https://i.pravatar.cc/400?img=14",
                "remoteName": "Kim Yến",
                "remoteAvatar": "https://i.pravatar.cc/400?img=36",
                "videoOnStarted": true,
                "theme": "light"
            ]
            
            FlutterManager.shared.channel?.invokeMethod("openConference", arguments: arguments)
            
            presentFlutterViewController()
        } else {
            alertMessage = "Alias is required"
            showAlert = true
        }
    }
    
    private func presentFlutterViewController() {
        guard let flutterEngine = FlutterManager.shared.flutterEngine else { return }
        let flutterViewController = FlutterViewController(engine: flutterEngine, nibName: nil, bundle: nil)
        flutterViewController.modalPresentationStyle = .fullScreen
        presentingViewController.present(flutterViewController, animated: true, completion: nil)
    }
}

// MARK: - Main UIViewController
class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 1. Khởi tạo Flutter Engine & MethodChannel
//        FlutterManager.shared.setupEngine()
        
        // 2. Nhúng SwiftUI View vào UIViewController
        let swiftUIView = TVDemoScreen(presentingViewController: self)
        let hostingController = UIHostingController(rootView: swiftUIView)
        
        addChild(hostingController)
        hostingController.view.frame = view.bounds
        hostingController.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        view.addSubview(hostingController.view)
        hostingController.didMove(toParent: self)
    }
}
