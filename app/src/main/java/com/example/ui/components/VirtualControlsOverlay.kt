package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionSlot
import com.example.model.ControlProfile
import com.example.model.GTA_QUICK_COMMANDS
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.HudGlass
import com.example.ui.theme.LosSantosGold
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ViceMagenta
import com.example.viewmodel.FivePlayUiState

@Composable
fun VirtualControlsOverlay(
    uiState: FivePlayUiState,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float, Float, Float) -> Unit,
    onCursorTapClick: () -> Unit,
    onWheelScroll: (Int) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleMoveStyle: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleHud: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onToggleOrientation: () -> Unit,
    onOpenQuickChat: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onOpenCommands: (() -> Unit)? = null,
    onSwitchServer: (() -> Unit)? = null,
    onToggleLowRam: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val runtime = uiState.runtime
    val scale = prefs.controlScale
    val opacity = prefs.controlOpacity
    val haptics = prefs.hapticFeedback

    var showToolboxDrawer by remember { mutableStateOf(false) }
    var showPhoneDpadPopup by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // 1. Virtual Mouse & 3D Camera Trackpad Surface
        // In TOUCH_ONLY mode, covers full screen; otherwise covers right 65% of screen
        Row(modifier = Modifier.fillMaxSize()) {
            if (runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY) {
                Spacer(modifier = Modifier.weight(0.35f))
            }
            MouseLookTouchpad(
                sensitivity = prefs.mouseSensitivity,
                invertY = prefs.invertYAxis,
                hapticsEnabled = haptics,
                onMouseDelta = onMouseDelta,
                onTapClick = onCursorTapClick,
                modifier = Modifier
                    .weight(if (runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY) 0.65f else 1f)
                    .fillMaxHeight()
            )
        }

        // 2. Crisp Virtual PC Mouse Pointer Arrow & 3D Crosshair Layer
        VirtualMouseCursorOverlay(
            cursorNormX = runtime.cursorNormX,
            cursorNormY = runtime.cursorNormY,
            isClicking = runtime.isCursorClicking,
            mouseMode = prefs.mouseControlMode,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Profile-Specific Ergonomic Virtual Controls
        AnimatedVisibility(
            visible = runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            when (runtime.activeProfile) {
                ControlProfile.ON_FOOT -> SystematicOnFootControls(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    showPhoneDpad = showPhoneDpadPopup,
                    onTogglePhoneDpad = { showPhoneDpadPopup = !showPhoneDpadPopup },
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = { dx, dy -> onMouseDelta(dx, dy, 1000f, 600f) },
                    onStickVector = onStickVector
                )

                ControlProfile.DRIVING -> SystematicDrivingControls(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged
                )

                ControlProfile.XBOX_GAMEPAD -> XboxGamepadLayout(
                    uiState = uiState,
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = { dx, dy -> onMouseDelta(dx, dy, 1000f, 600f) },
                    onStickVector = onStickVector
                )

                ControlProfile.TOUCH_ONLY -> Unit
            }
        }

        // 4. Compact Systematic Top-Center Control Bar (Unified, slim & non-intrusive)
        SystematicTopPillBar(
            uiState = uiState,
            onCycleProfile = onCycleProfile,
            onToggleMouseMode = onToggleMouseMode,
            onToggleControls = onToggleControls,
            onOpenToolbox = { showToolboxDrawer = true },
            onToggleMiniPcKeyboard = onToggleMiniPcKeyboard,
            onToggleOrientation = onToggleOrientation,
            onReloadPage = onReloadPage,
            onExitToHub = onExitToHub,
            onToggleLowRam = onToggleLowRam,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
        )

        // 5. Quick Toolbox Drawer (Bottom Sheet Card for GTA Weapons, F1-F8, Menu & Quick Commands)
        AnimatedVisibility(
            visible = showToolboxDrawer,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        ) {
            GtaToolboxDrawer(
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onOpenChat = {
                    showToolboxDrawer = false
                    onOpenQuickChat()
                },
                onOpenCommands = {
                    showToolboxDrawer = false
                    onOpenCommands?.invoke()
                },
                onSwitchServer = {
                    showToolboxDrawer = false
                    onSwitchServer?.invoke()
                },
                onWheelScroll = onWheelScroll,
                onClose = { showToolboxDrawer = false }
            )
        }

        // 6. Full On-Screen Mini PC Keyboard Overlay (when explicitly requested)
        AnimatedVisibility(
            visible = runtime.showMiniPcKeyboard,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            MiniPcKeyboardOverlay(
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onClose = onToggleMiniPcKeyboard
            )
        }
    }
}

/**
 * Compact, modern, non-intrusive Top Bar.
 * Takes minimal screen real estate and keeps 95% of the display clear for GTA 5.
 */
@Composable
private fun SystematicTopPillBar(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleControls: () -> Unit,
    onOpenToolbox: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onToggleOrientation: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onToggleLowRam: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val runtime = uiState.runtime
    val prefs = uiState.prefs

    Surface(
        modifier = modifier.testTag("floating_cyber_hud"),
        color = HudGlass,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Live FPS & Server Status Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurface)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (runtime.currentFps >= 45) NeonEmerald else LosSantosGold)
                )
                Text(
                    text = "${runtime.currentFps} FPS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // ⚡ 1-Tap RAM Saver Pill (Low RAM 720p 60FPS)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleLowRam?.invoke() }
                    .testTag("hud_low_ram_btn"),
                color = if (prefs.lowRamMode) NeonEmerald.copy(alpha = 0.18f) else CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (prefs.lowRamMode) NeonEmerald.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Giảm RAM",
                        tint = if (prefs.lowRamMode) NeonEmerald else LosSantosGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (prefs.lowRamMode) "RAM: 720p" else "RAM: Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (prefs.lowRamMode) NeonEmerald else Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // Mode Selector: Đi Bộ / Lái Xe / Xbox (1-tap switch)
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCycleProfile)
                    .testTag("hud_cycle_profile_btn"),
                color = LosSantosGold.copy(alpha = 0.18f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LosSantosGold.copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = when (runtime.activeProfile) {
                            ControlProfile.ON_FOOT -> Icons.AutoMirrored.Filled.DirectionsRun
                            ControlProfile.DRIVING -> Icons.Default.DirectionsCar
                            ControlProfile.XBOX_GAMEPAD -> Icons.Default.Gamepad
                            ControlProfile.TOUCH_ONLY -> Icons.Default.VisibilityOff
                        },
                        contentDescription = "Chế độ điều khiển",
                        tint = LosSantosGold,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = when (runtime.activeProfile) {
                            ControlProfile.ON_FOOT -> "Đi Bộ"
                            ControlProfile.DRIVING -> "Lái Xe"
                            ControlProfile.XBOX_GAMEPAD -> "Xbox"
                            ControlProfile.TOUCH_ONLY -> "Chỉ Chuột"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                }
            }

            // Mouse Mode: Chuột PC vs Tâm 3D
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onToggleMouseMode)
                    .testTag("hud_mouse_mode_btn"),
                color = NeonEmerald.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = if (prefs.mouseControlMode == MouseControlMode.VIRTUAL_CURSOR) {
                            Icons.Default.Mouse
                        } else {
                            Icons.Default.AdsClick
                        },
                        contentDescription = "Chế độ chuột",
                        tint = NeonEmerald,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = prefs.mouseControlMode.badgeVi,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // HỘP PHÍM (Toolbox Drawer) Button: 1-click opens full weapons, F-keys, and commands
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenToolbox)
                    .testTag("hud_toolbox_btn"),
                color = ElectricBlue.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.8f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Hộp phím",
                        tint = ElectricBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Hộp Phím",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            // ẨN / HIỆN Phím (1-click to clear screen)
            IconButton(
                onClick = onToggleControls,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("hud_visibility_btn")
            ) {
                Icon(
                    imageVector = if (runtime.controlsVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Ẩn/Hiện Phím",
                    tint = if (runtime.controlsVisible) NeonEmerald else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Full QWERTY PC Keyboard toggle
            IconButton(
                onClick = onToggleMiniPcKeyboard,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("hud_pc_keyboard_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "Bàn phím PC",
                    tint = if (runtime.showMiniPcKeyboard) ViceMagenta else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Rotate Screen
            IconButton(
                onClick = onToggleOrientation,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("hud_rotate_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = "Xoay Màn Hình",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Reload Page
            IconButton(
                onClick = onReloadPage,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("hud_reload_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tải lại trang",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Settings / Exit to Hub
            IconButton(
                onClick = onExitToHub,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("hud_settings_hub_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt & Sảnh",
                    tint = ViceMagenta,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Systematic On-Foot Controls:
 * - Left zone: Smooth Analog Stick + compact 2-button vertical pill (Shift + Ctrl) + Phone Icon.
 * - Right zone: Curved tactical combat arc (Fire, Aim, Jump, Enter Vehicle, Interact, Reload, Cover).
 * Zero random buttons cluttering the center of the display!
 */
@Composable
private fun SystematicOnFootControls(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    showPhoneDpad: Boolean,
    onTogglePhoneDpad: () -> Unit,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float) -> Unit,
    onStickVector: (Float, Float) -> Unit
) {
    val prefs = uiState.prefs

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // --- 1. LEFT HAND ERGONOMIC MOVEMENT ZONE ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Analog Joystick or WASD buttons
                if (prefs.movementStyle == MovementStyle.ANALOG_STICK) {
                    VirtualAnalogStick(
                        sizeDp = (128 * scale).dp,
                        opacity = opacity,
                        autoSprint = prefs.autoSprintOnEdge,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        onVectorChanged = onStickVector
                    )
                } else {
                    WasdKeyCluster(
                        scale = scale,
                        opacity = opacity,
                        haptics = haptics,
                        onKeyStateChanged = onKeyStateChanged
                    )
                }

                // Ergonomic 2-Button Pill immediately next to thumbstick: Shift (Sprint) & Ctrl (Crouch)
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TacticalKeyButton(
                        label = "SHIFT",
                        subLabel = "Chạy",
                        keySpec = uiState.resolveKeySpec(ActionSlot.SPRINT),
                        sizeDp = (48 * scale).dp,
                        opacity = opacity,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "btn_onfoot_shift"
                    )

                    TacticalKeyButton(
                        label = "CTRL",
                        subLabel = "Ngồi",
                        keySpec = uiState.resolveKeySpec(ActionSlot.CROUCH),
                        sizeDp = (44 * scale).dp,
                        opacity = opacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "btn_onfoot_ctrl"
                    )
                }
            }

            // Discreet Phone Button (📱) positioned neatly above the thumbstick
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(bottom = (134 * scale).dp)
            ) {
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePhoneDpad)
                        .testTag("btn_toggle_phone"),
                    color = CyberSurface.copy(alpha = opacity),
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, if (showPhoneDpad) NeonEmerald else LosSantosGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Điện thoại GTA",
                            tint = if (showPhoneDpad) NeonEmerald else LosSantosGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (showPhoneDpad) "Đóng" else "ĐT (↑)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Temporary Phone D-Pad Popup (Only appears when user taps the Phone icon, leaving screen 100% clean otherwise!)
        AnimatedVisibility(
            visible = showPhoneDpad,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp, bottom = 40.dp)
        ) {
            Surface(
                color = HudGlass,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(0.22f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ĐIỆN THOẠI GTA",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonEmerald,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable(onClick = onTogglePhoneDpad)
                        )
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    PhoneArrowCluster(
                        scale = scale * 0.95f,
                        opacity = opacity,
                        haptics = haptics,
                        onKeyStateChanged = onKeyStateChanged
                    )
                }
            }
        }

        // --- 2. RIGHT HAND ERGONOMIC COMBAT & ACTION CLUSTER ---
        // Grouped systematically into 2 compact curved rows along the thumb arc
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upper Action Row: Cover (Q), Reload (R), Enter Vehicle (F), Aim (RMB)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TacticalKeyButton(
                    label = "Q",
                    subLabel = "Nấp",
                    keySpec = uiState.resolveKeySpec(ActionSlot.COVER),
                    sizeDp = (44 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_cover"
                )

                TacticalKeyButton(
                    label = "R",
                    subLabel = "Nạp Đạn",
                    keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                    sizeDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_reload"
                )

                TacticalKeyButton(
                    label = "VÀO XE",
                    subLabel = "Phím F",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_vehicle"
                )

                TacticalKeyButton(
                    label = "NGẮM",
                    subLabel = "Chuột Phải",
                    keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    allowDragAim = true,
                    sensitivity = prefs.mouseSensitivity,
                    invertY = prefs.invertYAxis,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = onMouseDelta,
                    testTagName = "btn_onfoot_aim"
                )
            }

            // Lower Action Row: Interact (E), Jump (Space), Fire / Left Click (LMB)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                TacticalKeyButton(
                    label = "E",
                    subLabel = "Tương tác",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_interact"
                )

                TacticalKeyButton(
                    label = "NHẢY",
                    subLabel = "Space",
                    keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_onfoot_jump"
                )

                TacticalKeyButton(
                    label = "BẮN / CLICK",
                    subLabel = "Chuột Trái",
                    keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                    sizeDp = (70 * scale).dp,
                    opacity = opacity,
                    accentColor = DangerRed,
                    hapticsEnabled = haptics,
                    allowDragAim = true,
                    sensitivity = prefs.mouseSensitivity,
                    invertY = prefs.invertYAxis,
                    onKeyStateChanged = onKeyStateChanged,
                    onMouseDelta = onMouseDelta,
                    testTagName = "btn_onfoot_fire"
                )
            }
        }
    }
}

/**
 * Systematic Driving Controls (Cockpit Ergonomics):
 * - Left zone: Steering pedals (◄ Left / Right ►) + Exit car (F) + Horn (E).
 * - Right zone: Gas (W), Brake/Reverse (S), Handbrake (Space), Nitro (Shift).
 */
@Composable
private fun SystematicDrivingControls(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Left Side: Exit Car (F) + Horn (E) + Steering Left/Right
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TacticalKeyButton(
                    label = "RA XE",
                    subLabel = "Phím F",
                    keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_exit"
                )
                TacticalKeyButton(
                    label = "CÒI XE",
                    subLabel = "Phím E",
                    keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                    sizeDp = (48 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_horn"
                )
                TacticalKeyButton(
                    label = "GÓC CAM",
                    subLabel = "Phím V",
                    keySpec = uiState.resolveKeySpec(ActionSlot.CAMERA),
                    sizeDp = (46 * scale).dp,
                    opacity = opacity,
                    accentColor = ElectricBlue,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_cam"
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PedalButton(
                    label = "◄ TRÁI",
                    subLabel = "Lái A",
                    keySpec = PcKeyCatalog.KEY_A,
                    widthDp = (78 * scale).dp,
                    heightDp = (70 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_left"
                )
                PedalButton(
                    label = "PHẢI ►",
                    subLabel = "Lái D",
                    keySpec = PcKeyCatalog.KEY_D,
                    widthDp = (78 * scale).dp,
                    heightDp = (70 * scale).dp,
                    opacity = opacity,
                    accentColor = NeonEmerald,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_right"
                )
            }
        }

        // Right Side: Nitro, Drift/Handbrake, Brake/Reverse (S), Gas (W)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TacticalKeyButton(
                    label = "NITRO",
                    subLabel = "Shift",
                    keySpec = PcKeyCatalog.KEY_SHIFT,
                    sizeDp = (50 * scale).dp,
                    opacity = opacity,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_nitro"
                )
                TacticalKeyButton(
                    label = "DRIFT",
                    subLabel = "Phanh Tay",
                    keySpec = PcKeyCatalog.KEY_SPACE,
                    sizeDp = (54 * scale).dp,
                    opacity = opacity,
                    accentColor = ViceMagenta,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    testTagName = "btn_drive_handbrake"
                )
            }

            PedalButton(
                label = "PHANH",
                subLabel = "Lùi (S)",
                keySpec = PcKeyCatalog.KEY_S,
                widthDp = (64 * scale).dp,
                heightDp = (86 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_brake"
            )

            PedalButton(
                label = "GA",
                subLabel = "Tăng tốc (W)",
                keySpec = PcKeyCatalog.KEY_W,
                widthDp = (70 * scale).dp,
                heightDp = (118 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_drive_gas"
            )
        }
    }
}

/**
 * Standard Xbox Gamepad layout with left stick & d-pad, right ABXY diamond & triggers.
 */
@Composable
private fun XboxGamepadLayout(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onMouseDelta: (Float, Float) -> Unit,
    onStickVector: (Float, Float) -> Unit
) {
    val prefs = uiState.prefs
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Left Triggers LT & LB
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalKeyButton(
                label = "LT",
                subLabel = "Ngắm",
                keySpec = PcKeyCatalog.MOUSE_RIGHT,
                sizeDp = (50 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lt"
            )
            TacticalKeyButton(
                label = "LB",
                subLabel = "Kho Súng",
                keySpec = PcKeyCatalog.KEY_TAB,
                sizeDp = (46 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_lb"
            )
        }

        // Right Triggers RB & RT
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TacticalKeyButton(
                label = "RB",
                subLabel = "Ẩn Nấp",
                keySpec = PcKeyCatalog.KEY_Q,
                sizeDp = (46 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_rb"
            )
            TacticalKeyButton(
                label = "RT",
                subLabel = "Bắn / Click",
                keySpec = PcKeyCatalog.MOUSE_LEFT,
                sizeDp = (54 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                allowDragAim = true,
                sensitivity = prefs.mouseSensitivity,
                invertY = prefs.invertYAxis,
                onKeyStateChanged = onKeyStateChanged,
                onMouseDelta = onMouseDelta,
                testTagName = "btn_xbox_rt"
            )
        }

        // Left Stick + D-Pad
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VirtualAnalogStick(
                sizeDp = (124 * scale).dp,
                opacity = opacity,
                autoSprint = prefs.autoSprintOnEdge,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onVectorChanged = onStickVector
            )
            PhoneArrowCluster(
                scale = scale * 0.9f,
                opacity = opacity,
                haptics = haptics,
                onKeyStateChanged = onKeyStateChanged
            )
        }

        // Right ABXY Diamond
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size((154 * scale).dp)
        ) {
            TacticalKeyButton(
                label = "Y",
                subLabel = "Vào Xe (F)",
                keySpec = PcKeyCatalog.KEY_F,
                sizeDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = LosSantosGold,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_y",
                modifier = Modifier.align(Alignment.TopCenter)
            )
            TacticalKeyButton(
                label = "X",
                subLabel = "Nạp (R)",
                keySpec = PcKeyCatalog.KEY_R,
                sizeDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = ElectricBlue,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_x",
                modifier = Modifier.align(Alignment.CenterStart)
            )
            TacticalKeyButton(
                label = "B",
                subLabel = "Dùng (E)",
                keySpec = PcKeyCatalog.KEY_E,
                sizeDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = DangerRed,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_b",
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            TacticalKeyButton(
                label = "A",
                subLabel = "Nhảy",
                keySpec = PcKeyCatalog.KEY_SPACE,
                sizeDp = (48 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "btn_xbox_a",
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * Modern Quick GTA Toolbox Drawer:
 * Opens smoothly on-demand when user taps "Hộp Phím".
 * Groups all supplementary keys into clean categories so the main game screen is never cluttered!
 */
@Composable
private fun GtaToolboxDrawer(
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onOpenChat: () -> Unit,
    onOpenCommands: () -> Unit,
    onSwitchServer: () -> Unit,
    onWheelScroll: (Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .testTag("gta_toolbox_drawer"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFA0B0F19)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, NeonEmerald.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "HỘP CÔNG CỤ & PHÍM GTA 5",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Chat Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenChat),
                        color = LosSantosGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, LosSantosGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = LosSantosGold, modifier = Modifier.size(12.dp))
                            Text("Chat (T)", color = LosSantosGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Commands Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenCommands),
                        color = ElectricBlue.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, ElectricBlue)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(12.dp))
                            Text("Lệnh", color = ElectricBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Close Drawer Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DangerRed.copy(alpha = 0.25f))
                            .border(1.dp, DangerRed, RoundedCornerShape(8.dp))
                            .clickable(onClick = onClose)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Đóng ✕", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category 1: Vũ khí & Bánh xe súng (1 - 5, TAB)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vũ Khí:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )

                listOf(
                    PcKeyCatalog.KEY_TAB to "TAB (Kho)",
                    PcKeyCatalog.KEY_1 to "1 (Nắm đấm)",
                    PcKeyCatalog.KEY_2 to "2 (Súng lục)",
                    PcKeyCatalog.KEY_3 to "3 (SMG/Shotgun)",
                    PcKeyCatalog.KEY_4 to "4 (Rifle)",
                    PcKeyCatalog.KEY_5 to "5 (Sniper/Lựu)"
                ).forEach { (spec, label) ->
                    MiniSquareKeyButton(
                        label = label,
                        keySpec = spec,
                        sizeDp = 30.dp,
                        widthDp = 52.dp,
                        opacity = 0.95f,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "toolbox_key_${spec.id}"
                    )
                }

                // Scroll Wheel Buttons
                MiniSquareKeyButton(
                    label = "LĂN ↑",
                    keySpec = PcKeyCatalog.WHEEL_UP,
                    sizeDp = 30.dp,
                    widthDp = 46.dp,
                    opacity = 0.95f,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = { spec, down ->
                        onKeyStateChanged(spec, down)
                        if (down) onWheelScroll(-120)
                    },
                    testTagName = "toolbox_wheel_up"
                )
                MiniSquareKeyButton(
                    label = "LĂN ↓",
                    keySpec = PcKeyCatalog.WHEEL_DOWN,
                    sizeDp = 30.dp,
                    widthDp = 46.dp,
                    opacity = 0.95f,
                    accentColor = LosSantosGold,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = { spec, down ->
                        onKeyStateChanged(spec, down)
                        if (down) onWheelScroll(120)
                    },
                    testTagName = "toolbox_wheel_down"
                )
            }

            // Category 2: Menu GTA & Chức Năng (ESC, M, I, P, F1, F2, F3, F8, V, N, X, G, C)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu & Phím F:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp
                )

                listOf(
                    PcKeyCatalog.KEY_ESC to "ESC",
                    PcKeyCatalog.KEY_M to "M (Bản đồ)",
                    PcKeyCatalog.KEY_I to "I (Túi đồ)",
                    PcKeyCatalog.KEY_P to "P (Pause)",
                    PcKeyCatalog.KEY_F1 to "F1",
                    PcKeyCatalog.KEY_F2 to "F2",
                    PcKeyCatalog.KEY_F3 to "F3",
                    PcKeyCatalog.KEY_F8 to "F8",
                    PcKeyCatalog.KEY_V to "V (Cam)",
                    PcKeyCatalog.KEY_N to "N (Mic)",
                    PcKeyCatalog.KEY_X to "X (Giơ tay)",
                    PcKeyCatalog.KEY_G to "G (Đai/Lựu)",
                    PcKeyCatalog.KEY_C to "C (Nhìn sau)"
                ).forEach { (spec, label) ->
                    MiniSquareKeyButton(
                        label = label,
                        keySpec = spec,
                        sizeDp = 30.dp,
                        widthDp = if (label.length >= 4) 48.dp else 36.dp,
                        opacity = 0.95f,
                        accentColor = if (spec.id.startsWith("KEY_F") || spec.id == "KEY_ESC") ViceMagenta else ElectricBlue,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "toolbox_key_${spec.id}"
                    )
                }
            }
        }
    }
}
