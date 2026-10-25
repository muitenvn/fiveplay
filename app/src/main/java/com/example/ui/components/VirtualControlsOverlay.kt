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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.InGameControlStyle
import com.example.model.MouseControlMode
import com.example.model.MovementStyle
import com.example.model.PcKeyCatalog
import com.example.model.PcKeySpec
import com.example.model.TopHudStyle
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
    onCycleControlStyle: () -> Unit = {},
    onSelectControlStyle: (InGameControlStyle) -> Unit = {},
    onCycleTopHudStyle: () -> Unit = {},
    onSelectTopHudStyle: (TopHudStyle) -> Unit = {},
    onToggleLayoutSwitcher: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val runtime = uiState.runtime
    val scale = prefs.controlScale
    val opacity = prefs.controlOpacity
    val haptics = prefs.hapticFeedback

    var showToolboxDrawer by remember { mutableStateOf(false) }
    var showPhoneDpadPopup by remember { mutableStateOf(false) }
    var localShowLayoutSwitcher by remember { mutableStateOf(false) }
    val isSwitcherOpen = runtime.showLayoutSwitcherDialog || localShowLayoutSwitcher

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

        // 3. User-Selected In-Game Control Layout (4 Distinct Full Presets)
        AnimatedVisibility(
            visible = runtime.controlsVisible && runtime.activeProfile != ControlProfile.TOUCH_ONLY,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            when (runtime.activeProfile) {
                ControlProfile.ON_FOOT -> {
                    when (prefs.inGameControlStyle) {
                        InGameControlStyle.MINIMAL_TRANSPARENT -> MinimalTransparentControls(
                            uiState = uiState,
                            scale = scale,
                            opacity = opacity,
                            haptics = haptics,
                            onKeyStateChanged = onKeyStateChanged,
                            onStickVector = onStickVector
                        )
                        InGameControlStyle.FLOATING_ARC -> FloatingArcControls(
                            uiState = uiState,
                            scale = scale,
                            opacity = opacity,
                            haptics = haptics,
                            onKeyStateChanged = onKeyStateChanged,
                            onStickVector = onStickVector
                        )
                        InGameControlStyle.GESTURE_IMMERSIVE -> GestureImmersiveControls(
                            uiState = uiState,
                            haptics = haptics,
                            onKeyStateChanged = onKeyStateChanged,
                            onStickVector = onStickVector
                        )
                        InGameControlStyle.CLASSIC_GAMEPAD -> ClassicConsoleGamepadControls(
                            uiState = uiState,
                            scale = scale,
                            opacity = opacity,
                            haptics = haptics,
                            onKeyStateChanged = onKeyStateChanged,
                            onStickVector = onStickVector
                        )
                    }
                }

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

        // 4. User-Selected Top HUD Style (4 Distinct Display Styles)
        when (prefs.topHudStyle) {
            TopHudStyle.FLOATING_FAB_DRAWER -> FloatingFabTopHud(
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
                onToggleLayoutSwitcher = { localShowLayoutSwitcher = true },
                modifier = Modifier.align(Alignment.TopEnd)
            )

            TopHudStyle.SLIM_PINNED_PILL -> SlimPinnedPillTopHud(
                uiState = uiState,
                onCycleProfile = onCycleProfile,
                onToggleMouseMode = onToggleMouseMode,
                onToggleControls = onToggleControls,
                onOpenToolbox = { showToolboxDrawer = true },
                onToggleMiniPcKeyboard = onToggleMiniPcKeyboard,
                onReloadPage = onReloadPage,
                onExitToHub = onExitToHub,
                onToggleLowRam = onToggleLowRam,
                onToggleLayoutSwitcher = { localShowLayoutSwitcher = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )

            TopHudStyle.SLIDE_SIDE_PANEL -> SlideSidePanelTopHud(
                uiState = uiState,
                onCycleProfile = onCycleProfile,
                onToggleMouseMode = onToggleMouseMode,
                onToggleControls = onToggleControls,
                onToggleMiniPcKeyboard = onToggleMiniPcKeyboard,
                onReloadPage = onReloadPage,
                onExitToHub = onExitToHub,
                onToggleLowRam = onToggleLowRam,
                onToggleLayoutSwitcher = { localShowLayoutSwitcher = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            )

            TopHudStyle.IMMERSIVE_HIDDEN -> ImmersiveHiddenTopHud(
                uiState = uiState,
                onCycleProfile = onCycleProfile,
                onToggleControls = onToggleControls,
                onToggleMiniPcKeyboard = onToggleMiniPcKeyboard,
                onReloadPage = onReloadPage,
                onExitToHub = onExitToHub,
                onToggleLayoutSwitcher = { localShowLayoutSwitcher = true },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // 5. In-Game Layout Switcher Modal (1-tap preview & switch between 4 layouts and 4 HUD styles)
        if (isSwitcherOpen) {
            LayoutSwitcherModal(
                currentControlStyle = prefs.inGameControlStyle,
                currentTopHudStyle = prefs.topHudStyle,
                onSelectControlStyle = {
                    onSelectControlStyle(it)
                },
                onSelectTopHudStyle = {
                    onSelectTopHudStyle(it)
                },
                onDismiss = {
                    localShowLayoutSwitcher = false
                    if (runtime.showLayoutSwitcherDialog) {
                        onToggleLayoutSwitcher()
                    }
                }
            )
        }

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

/**
 * Layout Style 1: Minimalist Transparent HUD.
 * Extreme transparency, only core keys (Analog Stick, Fire, Aim, Jump, Interact, Vehicle).
 * Keeps >90% of screen unblocked.
 */
@Composable
private fun MinimalTransparentControls(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs
    val subtleOpacity = (opacity * 0.42f).coerceIn(0.22f, 0.65f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Left side: Sleek transparent analog stick
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 4.dp)
        ) {
            VirtualAnalogStick(
                sizeDp = (116 * scale).dp,
                opacity = subtleOpacity,
                autoSprint = prefs.autoSprintOnEdge,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onVectorChanged = onStickVector
            )
        }

        // Right side: Clean, compact 4-button cluster + vehicle pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Secondary small actions (Vehicle F + Interact E)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactMiniKeyButton(
                        label = "F",
                        keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                        sizeDp = (38 * scale).dp,
                        opacity = subtleOpacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "minimal_btn_f"
                    )
                    CompactMiniKeyButton(
                        label = "E",
                        keySpec = uiState.resolveKeySpec(ActionSlot.INTERACT),
                        sizeDp = (38 * scale).dp,
                        opacity = subtleOpacity,
                        accentColor = ViceMagenta,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "minimal_btn_e"
                    )
                }

                // Primary combat keys: Aim, Jump, Fire
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactMiniKeyButton(
                        label = "NGẮM",
                        keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                        sizeDp = (44 * scale).dp,
                        opacity = subtleOpacity,
                        accentColor = ElectricBlue,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "minimal_btn_aim"
                    )
                    CompactMiniKeyButton(
                        label = "NHẢY",
                        keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                        sizeDp = (48 * scale).dp,
                        opacity = subtleOpacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "minimal_btn_jump"
                    )
                    CompactMiniKeyButton(
                        label = "BẮN",
                        keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                        sizeDp = (58 * scale).dp,
                        opacity = (opacity * 0.65f).coerceIn(0.35f, 0.85f),
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "minimal_btn_fire"
                    )
                }
            }
        }
    }
}

/**
 * Layout Style 2: Floating Stick & Compact Combat Arc.
 * Left: Touch anywhere dynamically summons the analog joystick.
 * Right: Buttons wrap naturally around the thumb's radial arc.
 */
@Composable
private fun FloatingArcControls(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs

    Box(modifier = modifier.fillMaxSize()) {
        // Left Half: Dynamic Floating Joystick
        Box(
            modifier = Modifier
                .fillMaxWidth(0.48f)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
        ) {
            FloatingDynamicJoystick(
                opacity = opacity,
                autoSprint = prefs.autoSprintOnEdge,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                onVectorChanged = onStickVector
            )
        }

        // Right side: Radial Combat Arc (Centered on comfortable thumb rest)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Outer Arc: Vehicle F, Reload R, Aim RMB
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompactMiniKeyButton(
                        label = "XE",
                        keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                        sizeDp = (42 * scale).dp,
                        opacity = opacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_vehicle"
                    )
                    CompactMiniKeyButton(
                        label = "NẠP",
                        keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                        sizeDp = (42 * scale).dp,
                        opacity = opacity,
                        accentColor = ElectricBlue,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_reload"
                    )
                    CompactMiniKeyButton(
                        label = "NGẮM",
                        keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                        sizeDp = (46 * scale).dp,
                        opacity = opacity,
                        accentColor = ViceMagenta,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_aim"
                    )
                }

                // Inner Arc: Sprint Shift, Jump Space, Fire LMB
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    CompactMiniKeyButton(
                        label = "CHẠY",
                        keySpec = uiState.resolveKeySpec(ActionSlot.SPRINT),
                        sizeDp = (46 * scale).dp,
                        opacity = opacity,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_sprint"
                    )
                    CompactMiniKeyButton(
                        label = "NHẢY",
                        keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                        sizeDp = (52 * scale).dp,
                        opacity = opacity,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_jump"
                    )
                    CompactMiniKeyButton(
                        label = "BẮN",
                        keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                        sizeDp = (62 * scale).dp,
                        opacity = opacity,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "arc_btn_fire"
                    )
                }
            }
        }
    }
}

/**
 * Layout Style 3: Ultra-Clean Gesture Mode.
 * 100% buttonless screen!
 * Left zone: Swipe to Move & Sprint.
 * Right zone: Swipe for Camera, Double Tap to Fire, Swipe Up to Jump.
 */
@Composable
private fun GestureImmersiveControls(
    uiState: FivePlayUiState,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGestureHint by remember { mutableStateOf(true) }
    var showMiniActions by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(3500)
        showGestureHint = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Left 50%: Gesture Movement (Dynamic Drag)
        FloatingDynamicJoystick(
            opacity = 0.35f,
            autoSprint = true,
            hapticsEnabled = haptics,
            onKeyStateChanged = onKeyStateChanged,
            onVectorChanged = onStickVector,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
        )

        // Transient gesture guide pill
        AnimatedVisibility(
            visible = showGestureHint,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            Surface(
                color = CyberSurface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.7f))
            ) {
                Text(
                    text = "CHẾ ĐỘ CỬ CHỈ: Nửa trái vuốt di chuyển | Nửa phải xoay góc nhìn | Chạm đúp để Bắn",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonEmerald,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Tiny discreet edge badge for secondary GTA keys (Vehicle, Phone, Reload)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showMiniActions) {
                    CompactMiniKeyButton(
                        label = "XE",
                        keySpec = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                        sizeDp = 38.dp,
                        opacity = 0.85f,
                        accentColor = LosSantosGold,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "gesture_btn_vehicle"
                    )
                    CompactMiniKeyButton(
                        label = "NHẢY",
                        keySpec = uiState.resolveKeySpec(ActionSlot.JUMP),
                        sizeDp = 38.dp,
                        opacity = 0.85f,
                        accentColor = NeonEmerald,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "gesture_btn_jump"
                    )
                    CompactMiniKeyButton(
                        label = "NẠP",
                        keySpec = uiState.resolveKeySpec(ActionSlot.RELOAD),
                        sizeDp = 38.dp,
                        opacity = 0.85f,
                        accentColor = ElectricBlue,
                        hapticsEnabled = haptics,
                        onKeyStateChanged = onKeyStateChanged,
                        testTagName = "gesture_btn_reload"
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable { showMiniActions = !showMiniActions }
                        .testTag("gesture_toggle_mini_actions"),
                    color = CyberSurface.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (showMiniActions) "✕" else "•••",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layout Style 4: Classic Console Gamepad Controls (ABXY + D-Pad + Bumpers)
 */
@Composable
private fun ClassicConsoleGamepadControls(
    uiState: FivePlayUiState,
    scale: Float,
    opacity: Float,
    haptics: Boolean,
    onKeyStateChanged: (PcKeySpec, Boolean) -> Unit,
    onStickVector: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val prefs = uiState.prefs

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Top Shoulder Bumpers: L1 (Aim) & R1 (Fire)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 2.dp)
        ) {
            CompactMiniKeyButton(
                label = "L1 (NGẮM)",
                keySpec = uiState.resolveKeySpec(ActionSlot.AIM),
                sizeDp = (54 * scale).dp,
                opacity = opacity,
                accentColor = ViceMagenta,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "bumper_l1"
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 2.dp)
        ) {
            CompactMiniKeyButton(
                label = "R1 (BẮN)",
                keySpec = uiState.resolveKeySpec(ActionSlot.FIRE),
                sizeDp = (54 * scale).dp,
                opacity = opacity,
                accentColor = NeonEmerald,
                hapticsEnabled = haptics,
                onKeyStateChanged = onKeyStateChanged,
                testTagName = "bumper_r1"
            )
        }

        // Bottom Left: D-Pad or Analog Stick
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 6.dp)
        ) {
            if (prefs.movementStyle == MovementStyle.ANALOG_STICK) {
                VirtualAnalogStick(
                    sizeDp = (124 * scale).dp,
                    opacity = opacity,
                    autoSprint = prefs.autoSprintOnEdge,
                    hapticsEnabled = haptics,
                    onKeyStateChanged = onKeyStateChanged,
                    onVectorChanged = onStickVector
                )
            } else {
                ClassicGamepadDpad(
                    scale = scale,
                    opacity = opacity,
                    haptics = haptics,
                    onKeyStateChanged = onKeyStateChanged
                )
            }
        }

        // Bottom Right: Classic ABXY Diamond
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 6.dp)
        ) {
            ClassicGamepadAbxy(
                scale = scale,
                opacity = opacity,
                haptics = haptics,
                keyA = uiState.resolveKeySpec(ActionSlot.JUMP),
                keyB = uiState.resolveKeySpec(ActionSlot.CROUCH),
                keyX = uiState.resolveKeySpec(ActionSlot.RELOAD),
                keyY = uiState.resolveKeySpec(ActionSlot.VEHICLE),
                onKeyStateChanged = onKeyStateChanged
            )
        }
    }
}

/**
 * Top HUD Style 1: Floating FAB Quick Drawer.
 * Auto-dims after 3s of inactivity, single tap expands tool drawer.
 */
@Composable
private fun FloatingFabTopHud(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleControls: () -> Unit,
    onOpenToolbox: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onToggleOrientation: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onToggleLowRam: (() -> Unit)?,
    onToggleLayoutSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var isIdle by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (!expanded) {
            delay(3200)
            isIdle = true
        } else {
            isIdle = false
        }
    }

    val alpha by animateFloatAsState(targetValue = if (isIdle && !expanded) 0.35f else 1f, label = "fab_alpha")

    Box(modifier = modifier.padding(top = 4.dp, end = 8.dp)) {
        Column(horizontalAlignment = Alignment.End) {
            // The Mini Circular FAB
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        isIdle = false
                        expanded = !expanded
                    }
                    .testTag("hud_fab_btn"),
                color = HudGlass.copy(alpha = alpha),
                shape = CircleShape,
                border = BorderStroke(1.2.dp, NeonEmerald.copy(alpha = 0.8f * alpha))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Tune,
                        contentDescription = "Menu Công Cụ",
                        tint = NeonEmerald.copy(alpha = alpha),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded Quick Drawer
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                Surface(
                    color = CyberSurface.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .testTag("fab_quick_drawer")
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Layout Switcher shortcut
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    expanded = false
                                    onToggleLayoutSwitcher()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = LosSantosGold, modifier = Modifier.size(16.dp))
                            Text("Đổi Bố Cục Nút (1-4)", color = LosSantosGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Profile Switcher (Đi Bộ / Xe / Xbox)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onCycleProfile)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Gamepad, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                            Text(
                                text = when (uiState.runtime.activeProfile) {
                                    ControlProfile.ON_FOOT -> "Chế độ: Đi Bộ"
                                    ControlProfile.DRIVING -> "Chế độ: Lái Xe"
                                    ControlProfile.XBOX_GAMEPAD -> "Chế độ: Xbox"
                                    ControlProfile.TOUCH_ONLY -> "Chế độ: Chuột"
                                },
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }

                        // Virtual Keyboard
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    expanded = false
                                    onToggleMiniPcKeyboard()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            Text("Bàn Phím Mini PC", color = Color.White, fontSize = 11.sp)
                        }

                        // Reload & Exit
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onReloadPage)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                color = CyberSurface
                            ) {
                                Text("Tải Lại", color = Color.White, fontSize = 11.sp)
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(onClick = onExitToHub)
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                color = DangerRed.copy(alpha = 0.2f)
                            ) {
                                Text("Về Sảnh", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top HUD Style 2: Slim Pinned Pill Bar (Ultra-compact 28dp height).
 */
@Composable
private fun SlimPinnedPillTopHud(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleControls: () -> Unit,
    onOpenToolbox: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onToggleLowRam: (() -> Unit)?,
    onToggleLayoutSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runtime = uiState.runtime
    val prefs = uiState.prefs

    Surface(
        modifier = modifier.testTag("slim_pinned_pill_hud"),
        color = HudGlass,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // FPS badge
            Text(
                text = "${runtime.currentFps} FPS",
                style = MaterialTheme.typography.labelSmall,
                color = NeonEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Layout Switcher icon
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onToggleLayoutSwitcher)
                    .testTag("slim_hud_switcher_btn"),
                color = LosSantosGold.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = LosSantosGold, modifier = Modifier.size(12.dp))
                    Text("Layout", color = LosSantosGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Low RAM toggle
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleLowRam?.invoke() },
                color = if (prefs.lowRamMode) NeonEmerald.copy(alpha = 0.18f) else CyberSurface
            ) {
                Text(
                    text = if (prefs.lowRamMode) "RAM 720p" else "RAM Max",
                    color = if (prefs.lowRamMode) NeonEmerald else Color.White,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                )
            }

            // Keyboard icon
            IconButton(onClick = onToggleMiniPcKeyboard, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Keyboard, contentDescription = "Phím", tint = ElectricBlue, modifier = Modifier.size(13.dp))
            }

            // Reload
            IconButton(onClick = onReloadPage, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Tải lại", tint = Color.White, modifier = Modifier.size(13.dp))
            }

            // Exit
            IconButton(onClick = onExitToHub, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Về sảnh", tint = DangerRed, modifier = Modifier.size(13.dp))
            }
        }
    }
}

/**
 * Top HUD Style 3: Slide-out Side Panel.
 * Top of the screen is 100% empty! Pinned tab on screen edge slides panel in.
 */
@Composable
private fun SlideSidePanelTopHud(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleMouseMode: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onToggleLowRam: (() -> Unit)?,
    onToggleLayoutSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPanelOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) {
        // Tab handle at edge
        if (!isPanelOpen) {
            Surface(
                modifier = Modifier
                    .size(width = 24.dp, height = 54.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .clickable { isPanelOpen = true }
                    .testTag("side_panel_pull_tab"),
                color = CyberSurface.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.6f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Mở bảng công cụ",
                        tint = NeonEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Sliding Panel
        AnimatedVisibility(
            visible = isPanelOpen,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(160.dp)
                    .testTag("side_panel_drawer"),
                color = CyberSurface.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CÔNG CỤ", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        IconButton(onClick = { isPanelOpen = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Đóng", tint = Color.White)
                        }
                    }

                    // FPS
                    Text("${uiState.runtime.currentFps} FPS", color = LosSantosGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    // Layout Switcher
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isPanelOpen = false
                                onToggleLayoutSwitcher()
                            },
                        color = LosSantosGold.copy(alpha = 0.18f)
                    ) {
                        Text("Đổi Layout (1-4)", color = LosSantosGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }

                    // Profile
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onCycleProfile),
                        color = NeonEmerald.copy(alpha = 0.15f)
                    ) {
                        Text("Đổi Profile", color = NeonEmerald, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }

                    // Mini PC Keyboard
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isPanelOpen = false
                                onToggleMiniPcKeyboard()
                            },
                        color = ElectricBlue.copy(alpha = 0.15f)
                    ) {
                        Text("Bàn Phím PC", color = ElectricBlue, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }

                    // Reload
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onReloadPage),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Text("Tải Lại", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Exit
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onExitToHub),
                        color = DangerRed.copy(alpha = 0.25f)
                    ) {
                        Text("Về Sảnh", color = DangerRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Top HUD Style 4: Immersive Hidden (100% full screen game).
 * Transient bar shows for 4.5 seconds on swipe down / back.
 */
@Composable
private fun ImmersiveHiddenTopHud(
    uiState: FivePlayUiState,
    onCycleProfile: () -> Unit,
    onToggleControls: () -> Unit,
    onToggleMiniPcKeyboard: () -> Unit,
    onReloadPage: () -> Unit,
    onExitToHub: () -> Unit,
    onToggleLayoutSwitcher: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            delay(4500)
            isRevealed = false
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        // Invisible top edge touch target
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .clickable { isRevealed = true }
        )

        // The transient micro-bar
        AnimatedVisibility(
            visible = isRevealed,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                color = CyberSurface.copy(alpha = 0.94f),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                border = BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.7f)),
                modifier = Modifier.padding(top = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${uiState.runtime.currentFps} FPS", color = NeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onToggleLayoutSwitcher) {
                        Text("Đổi Layout", color = LosSantosGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onToggleMiniPcKeyboard) {
                        Text("Phím PC", color = ElectricBlue, fontSize = 10.sp)
                    }
                    TextButton(onClick = onReloadPage) {
                        Text("Tải lại", color = Color.White, fontSize = 10.sp)
                    }
                    TextButton(onClick = onExitToHub) {
                        Text("Thoát", color = DangerRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Layout Switcher Modal:
 * Allows 1-tap switching between all 4 Control Styles and all 4 Top HUD Styles!
 */
@Composable
private fun LayoutSwitcherModal(
    currentControlStyle: InGameControlStyle,
    currentTopHudStyle: TopHudStyle,
    onSelectControlStyle: (InGameControlStyle) -> Unit,
    onSelectTopHudStyle: (TopHudStyle) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CyberSurface,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Layers, contentDescription = null, tint = NeonEmerald)
                Text("TÙY CHỈNH GIAO DIỆN IN-GAME", color = NeonEmerald, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: 4 Control Layouts
                Text("1. BỐ CỤC NÚT BẤM (CONTROL LAYOUT):", color = LosSantosGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                InGameControlStyle.entries.forEach { style ->
                    val isSelected = style == currentControlStyle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectControlStyle(style) }
                            .testTag("select_control_style_${style.name}"),
                        color = if (isSelected) NeonEmerald.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) NeonEmerald else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Gamepad,
                                contentDescription = null,
                                tint = if (isSelected) NeonEmerald else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(style.titleVi, color = if (isSelected) NeonEmerald else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(style.subtitleVi, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
                            }
                        }
                    }
                }

                // Section 2: 4 Top HUD Styles
                Text("2. PHONG CÁCH THANH CÔNG CỤ (TOP HUD):", color = LosSantosGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                TopHudStyle.entries.forEach { hud ->
                    val isSelected = hud == currentTopHudStyle
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectTopHudStyle(hud) }
                            .testTag("select_top_hud_${hud.name}"),
                        color = if (isSelected) LosSantosGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.05f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) LosSantosGold else Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Tune,
                                contentDescription = null,
                                tint = if (isSelected) LosSantosGold else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(hud.titleVi, color = if (isSelected) LosSantosGold else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(hud.subtitleVi, color = Color.White.copy(alpha = 0.65f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Xong", color = NeonEmerald, fontWeight = FontWeight.Bold)
            }
        }
    )
}
