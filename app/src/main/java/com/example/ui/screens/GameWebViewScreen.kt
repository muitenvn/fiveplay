package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bridge.FivePlayJsBridge
import com.example.bridge.FivePlayNativeCallback
import com.example.model.DEFAULT_FIVEPLAY_URL
import com.example.model.DEFAULT_SERVER_PRESETS
import com.example.model.GTA_QUICK_COMMANDS
import com.example.model.MouseControlMode
import com.example.model.PcKeyCatalog
import com.example.ui.components.VirtualControlsOverlay
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ViceMagenta
import com.example.viewmodel.FivePlayUiState
import com.example.viewmodel.FivePlayViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DESKTOP_CHROME_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GameWebViewScreen(
    uiState: FivePlayUiState,
    viewModel: FivePlayViewModel,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val runtime = uiState.runtime
    val coroutineScope = rememberCoroutineScope()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var chatInputText by remember { mutableStateOf("") }
    var showCommandsDialog by remember { mutableStateOf(false) }
    var showServerSwitchDialog by remember { mutableStateOf(false) }
    var networkErrorMsg by remember { mutableStateOf<String?>(null) }

    BackHandler {
        FivePlayJsBridge.releaseAllInputs(webViewRef)
        viewModel.exitToHub()
    }

    DisposableEffect(Unit) {
        onDispose {
            FivePlayJsBridge.releaseAllInputs(webViewRef)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBackground)
            .testTag("game_webview_screen")
    ) {
        // 1. Hardware-Accelerated WebView specifically optimized for fiveplay.lol
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fiveplay_webview"),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.parseColor("#070A12"))
                    isFocusable = true
                    isFocusableInTouchMode = true

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        try {
                            val swController = ServiceWorkerController.getInstance()
                            swController.serviceWorkerWebSettings.apply {
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowContentAccess = true
                                allowFileAccess = true
                                blockNetworkLoads = false
                            }
                            swController.setServiceWorkerClient(object : ServiceWorkerClient() {
                                override fun shouldInterceptRequest(request: WebResourceRequest?): WebResourceResponse? {
                                    return super.shouldInterceptRequest(request)
                                }
                            })
                        } catch (_: Exception) {}
                    }

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        allowContentAccess = true
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = true
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        loadsImagesAutomatically = true
                        blockNetworkImage = false
                        offscreenPreRaster = true
                        saveFormData = false
                        setGeolocationEnabled(false)
                        @Suppress("DEPRECATION")
                        setRenderPriority(WebSettings.RenderPriority.HIGH)
                        if (prefs.desktopUserAgent) {
                            userAgentString = DESKTOP_CHROME_UA
                        }
                    }

                    addJavascriptInterface(
                        FivePlayNativeCallback(
                            onFpsReported = { fps ->
                                viewModel.onTelemetryUpdate(fps, runtime.hasGameCanvas)
                            },
                            onCanvasDetected = { hasCanvas ->
                                viewModel.onTelemetryUpdate(runtime.currentFps, hasCanvas)
                            }
                        ),
                        "FivePlayNative"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            networkErrorMsg = null
                            viewModel.onPageLoadProgress(15, true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            CookieManager.getInstance().flush()
                            viewModel.onPageLoadProgress(100, false)
                            view?.evaluateJavascript(
                                FivePlayJsBridge.buildBootstrapScript(prefs.webGlFpsBoost, prefs.lowRamMode),
                                null
                            )
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                networkErrorMsg = "Không thể tải máy chủ fiveplay.lol (${error?.description ?: "Vui lòng kiểm tra kết nối mạng"})"
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean = false
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            viewModel.onPageLoadProgress(newProgress, newProgress < 100)
                        }
                    }

                    loadUrl(prefs.serverUrl.ifBlank { DEFAULT_FIVEPLAY_URL })
                    webViewRef = this
                }
            },
            update = { webView ->
                webViewRef = webView
                val desiredUa = if (prefs.desktopUserAgent) {
                    DESKTOP_CHROME_UA
                } else {
                    WebSettings.getDefaultUserAgent(webView.context)
                }
                if (webView.settings.userAgentString != desiredUa) {
                    webView.settings.userAgentString = desiredUa
                }
                FivePlayJsBridge.applyMemoryOptimization(webView, prefs.lowRamMode)
            }
        )

        // 2. Sleek Non-intrusive Ambient Loading HUD (Never blocks game screen or canvas)
        AnimatedVisibility(
            visible = runtime.isPageLoading && !runtime.hasGameCanvas,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                color = CyberSurface.copy(alpha = 0.90f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f)),
                modifier = Modifier
                    .testTag("game_loading_card")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                    )
                    Text(
                        text = "Tải GTA 5: ${runtime.pageLoadingProgress}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { (runtime.pageLoadingProgress / 100f).coerceIn(0.1f, 1f) },
                        modifier = Modifier
                            .size(width = 70.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = NeonEmerald,
                        trackColor = Color(0xFF131A2A)
                    )
                    TextButton(
                        onClick = { viewModel.onPageLoadProgress(100, false) },
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Bỏ qua", color = LosSantosGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Network Disconnect Banner / Retry
        if (networkErrorMsg != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberSurface.copy(alpha = 0.95f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DangerRed),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(0.85f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "KHÔNG THỂ KẾT NỐI FIVEPLAY.LOL",
                        style = MaterialTheme.typography.titleMedium,
                        color = DangerRed
                    )
                    Text(
                        text = networkErrorMsg ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                networkErrorMsg = null
                                webViewRef?.reload()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Tải Lại Trang", color = Color.Black)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.exitToHub()
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LosSantosGold)
                        ) {
                            Text("Về Sảnh", color = LosSantosGold)
                        }
                    }
                }
            }
        }

        // 4. Multi-touch Virtual Gamepad, GTA 5 Keys & Virtual PC Mouse Cursor Overlay
        VirtualControlsOverlay(
            uiState = uiState,
            onKeyStateChanged = { spec, isDown ->
                viewModel.recordInputEvent(spec, isDown)
                FivePlayJsBridge.dispatchKeySpec(
                    webView = webViewRef,
                    spec = spec,
                    isDown = isDown,
                    cursorNormX = runtime.cursorNormX,
                    cursorNormY = runtime.cursorNormY,
                    sendNativeTapOnCursorClick = prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR
                )
            },
            onMouseDelta = { dx, dy, padW, padH ->
                if (prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR) {
                    val (nx, ny) = viewModel.moveVirtualCursorByDelta(dx, dy, padW, padH)
                    FivePlayJsBridge.dispatchMouseMoveAt(webViewRef, nx, ny, dx, dy)
                } else {
                    viewModel.updateTesterCamera(dx, dy)
                    FivePlayJsBridge.dispatchMouseMoveAt(webViewRef, 0.5f, 0.5f, dx, dy)
                }
            },
            onCursorTapClick = {
                coroutineScope.launch {
                    viewModel.setCursorClicking(true)
                    FivePlayJsBridge.dispatchKeySpec(
                        webView = webViewRef,
                        spec = PcKeyCatalog.MOUSE_LEFT,
                        isDown = true,
                        cursorNormX = runtime.cursorNormX,
                        cursorNormY = runtime.cursorNormY,
                        sendNativeTapOnCursorClick = true
                    )
                    delay(55)
                    FivePlayJsBridge.dispatchKeySpec(
                        webView = webViewRef,
                        spec = PcKeyCatalog.MOUSE_LEFT,
                        isDown = false,
                        cursorNormX = runtime.cursorNormX,
                        cursorNormY = runtime.cursorNormY,
                        sendNativeTapOnCursorClick = true
                    )
                    viewModel.setCursorClicking(false)
                }
            },
            onWheelScroll = { deltaY ->
                FivePlayJsBridge.dispatchWheel(webViewRef, deltaY)
            },
            onStickVector = { nx, ny ->
                viewModel.updateTesterStick(nx, ny)
                FivePlayJsBridge.dispatchGamepadAxes(webViewRef, nx, ny, 0f, 0f)
            },
            onCycleProfile = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.cycleControlProfile()
            },
            onToggleMouseMode = {
                viewModel.toggleMouseControlMode()
            },
            onToggleMoveStyle = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.toggleMovementStyle()
            },
            onToggleControls = { viewModel.toggleControlsVisibility() },
            onToggleHud = { viewModel.toggleHudExpanded() },
            onToggleMiniPcKeyboard = { viewModel.toggleMiniPcKeyboard() },
            onToggleOrientation = { viewModel.toggleOrientationLock() },
            onOpenQuickChat = {
                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_T, true)
                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_T, false)
                viewModel.setShowQuickChat(true)
            },
            onReloadPage = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                webViewRef?.reload()
            },
            onExitToHub = {
                FivePlayJsBridge.releaseAllInputs(webViewRef)
                viewModel.exitToHub()
            },
            onOpenCommands = { showCommandsDialog = true },
            onSwitchServer = { showServerSwitchDialog = true },
            onToggleLowRam = { viewModel.toggleLowRamMode() },
            onCycleControlStyle = { viewModel.cycleControlStyle() },
            onSelectControlStyle = { viewModel.saveInGameControlStyle(it) },
            onCycleTopHudStyle = { viewModel.cycleTopHudStyle() },
            onSelectTopHudStyle = { viewModel.saveTopHudStyle(it) },
            onToggleLayoutSwitcher = { viewModel.setShowLayoutSwitcher(!runtime.showLayoutSwitcherDialog) }
        )

        // 6. Quick Chat / Keyboard Command Modal
        if (runtime.showQuickChatDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowQuickChat(false) },
                containerColor = CyberSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = NeonEmerald)
                        Text(
                            text = "Gõ Chat / Nhập Lệnh (T & Enter)",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonEmerald
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Nhập nội dung tin nhắn chat hoặc nickname gửi thẳng vào fiveplay.lol:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Ví dụ: /help hoặc Xin chào phòng chơi!") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_chat_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Enter", "Esc", "Backspace", "F1").forEach { quickKey ->
                                OutlinedButton(
                                    onClick = {
                                        val spec = when (quickKey) {
                                            "Enter" -> PcKeyCatalog.KEY_ENTER
                                            "Esc" -> PcKeyCatalog.KEY_ESC
                                            "Backspace" -> PcKeyCatalog.KEY_BACKSPACE
                                            else -> PcKeyCatalog.KEY_F1
                                        }
                                        FivePlayJsBridge.dispatchKeySpec(webViewRef, spec, true)
                                        FivePlayJsBridge.dispatchKeySpec(webViewRef, spec, false)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(quickKey, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (chatInputText.isNotBlank()) {
                                FivePlayJsBridge.dispatchChatMessage(webViewRef, chatInputText)
                                chatInputText = ""
                            } else {
                                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_ENTER, true)
                                FivePlayJsBridge.dispatchKeySpec(webViewRef, PcKeyCatalog.KEY_ENTER, false)
                            }
                            viewModel.setShowQuickChat(false)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text("Gửi & Enter", color = Color.Black)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowQuickChat(false) }) {
                        Text("Đóng", color = LosSantosGold)
                    }
                }
            )
        }

        // 7. GTA Quick Commands Modal
        if (showCommandsDialog) {
            AlertDialog(
                onDismissRequest = { showCommandsDialog = false },
                containerColor = CyberSurface,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = LosSantosGold)
                        Text(
                            text = "LỆNH NHANH FIVEPLAY.LOL",
                            style = MaterialTheme.typography.titleLarge,
                            color = LosSantosGold
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        GTA_QUICK_COMMANDS.forEach { cmd ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        FivePlayJsBridge.dispatchChatMessage(webViewRef, cmd.command)
                                        showCommandsDialog = false
                                    },
                                color = CyberBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cmd.nameVi,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White
                                        )
                                        Text(
                                            text = cmd.descriptionVi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = cmd.command,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonEmerald
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCommandsDialog = false }) {
                        Text("Đóng", color = Color.White)
                    }
                }
            )
        }

        // 8. Server Switch Modal
        if (showServerSwitchDialog) {
            AlertDialog(
                onDismissRequest = { showServerSwitchDialog = false },
                containerColor = CyberSurface,
                title = {
                    Text(
                        text = "CHỌN MÁY CHỦ FIVEPLAY",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonEmerald
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DEFAULT_SERVER_PRESETS.forEach { preset ->
                            val isSelected = prefs.serverUrl == preset.url
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        networkErrorMsg = null
                                        viewModel.saveServerUrl(preset.url)
                                        webViewRef?.loadUrl(preset.url)
                                        showServerSwitchDialog = false
                                    },
                                color = if (isSelected) NeonEmerald.copy(alpha = 0.2f) else CyberBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonEmerald else Color.Transparent
                                )
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = preset.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White
                                        )
                                        Text(
                                            text = preset.badge,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) NeonEmerald else LosSantosGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showServerSwitchDialog = false }) {
                        Text("Đóng", color = LosSantosGold)
                    }
                }
            )
        }
    }
}
