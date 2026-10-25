package com.example.bridge

import android.os.SystemClock
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.model.PcKeySpec

class FivePlayNativeCallback(
    private val onFpsReported: (Int) -> Unit,
    private val onCanvasDetected: (Boolean) -> Unit
) {
    @JavascriptInterface
    fun onFpsUpdate(fps: Int) {
        onFpsReported(fps.coerceIn(0, 144))
    }

    @JavascriptInterface
    fun onGameCanvasStatus(hasCanvas: Boolean) {
        onCanvasDetected(hasCanvas)
    }
}

object FivePlayJsBridge {

    fun buildBootstrapScript(enableWebGlBoost: Boolean, lowRamMode: Boolean): String = """
        (function() {
            if (window.__fivePlayInjected) return;
            window.__fivePlayInjected = true;

            const isLowRam = $lowRamMode;
            const isBoost = $enableWebGlBoost;
            const targetDpr = isLowRam ? 1.0 : (isBoost ? 1.25 : Math.min(window.devicePixelRatio || 1.5, 1.5));

            // 1. Persistent Cache & Storage Quota Optimization (Fixes cache failing to load)
            try {
                if (navigator.storage && navigator.storage.persist) {
                    navigator.storage.persist().then(function(granted) {
                        console.log('[FivePlay Cache] Storage persistent mode:', granted);
                    }).catch(function() {});
                }
            } catch (e) {}

            // 2. Clamp Device Pixel Ratio to prevent 2K/3K/4K raster buffers eating 1.5GB+ RAM
            try {
                Object.defineProperty(window, 'devicePixelRatio', {
                    get: function() { return targetDpr; },
                    configurable: true
                });
            } catch (e) {}

            // 3. Canvas Buffer Resolution Clamp (720p in Low-RAM cuts VRAM & RAM by 70%)
            const MAX_CANVAS_WIDTH = isLowRam ? 1280 : 1600;
            const MAX_CANVAS_HEIGHT = isLowRam ? 720 : 900;

            try {
                const origWidthDesc = Object.getOwnPropertyDescriptor(HTMLCanvasElement.prototype, 'width');
                const origHeightDesc = Object.getOwnPropertyDescriptor(HTMLCanvasElement.prototype, 'height');

                if (origWidthDesc && origWidthDesc.set) {
                    Object.defineProperty(HTMLCanvasElement.prototype, 'width', {
                        get: function() { return origWidthDesc.get.call(this); },
                        set: function(val) {
                            const clamped = Math.min(val, MAX_CANVAS_WIDTH);
                            origWidthDesc.set.call(this, clamped);
                        },
                        configurable: true
                    });
                }

                if (origHeightDesc && origHeightDesc.set) {
                    Object.defineProperty(HTMLCanvasElement.prototype, 'height', {
                        get: function() { return origHeightDesc.get.call(this); },
                        set: function(val) {
                            const clamped = Math.min(val, MAX_CANVAS_HEIGHT);
                            origHeightDesc.set.call(this, clamped);
                        },
                        configurable: true
                    });
                }
            } catch (e) {}

            // 4. WebGL High-Performance & Zero-MSAA (Drastically reduces RAM and input latency)
            try {
                const origGetContext = HTMLCanvasElement.prototype.getContext;
                HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                    if (type === 'webgl' || type === 'webgl2' || type === 'experimental-webgl') {
                        attrs = Object.assign({}, attrs || {}, {
                            powerPreference: 'high-performance',
                            desynchronized: true,
                            preserveDrawingBuffer: false,
                            failIfMajorPerformanceCaveat: false
                        });
                        if (isLowRam) {
                            attrs.antialias = false; // Disabling MSAA saves huge VRAM
                        }
                    }
                    return origGetContext.call(this, type, attrs);
                };
            } catch (e) {}

            window.addEventListener('webglcontextlost', function(e) {
                e.preventDefault();
            }, false);

            try {
                const style = document.createElement('style');
                style.id = 'fiveplay-injected-style';
                style.innerHTML = `
                    * {
                        -webkit-touch-callout: none !important;
                        overscroll-behavior: none !important;
                    }
                    html, body {
                        touch-action: none !important;
                        overflow: hidden !important;
                        background-color: #070A12;
                        margin: 0;
                        padding: 0;
                    }
                    canvas {
                        touch-action: none !important;
                    }
                `;
                if (document.head) {
                    document.head.appendChild(style);
                } else {
                    document.addEventListener('DOMContentLoaded', () => {
                        if (document.head && !document.getElementById('fiveplay-injected-style')) {
                            document.head.appendChild(style);
                        }
                    });
                }
            } catch (e) {}

            window.addEventListener('contextmenu', e => e.preventDefault(), { passive: false });

            let lockedElement = null;
            function findGameTarget(x, y) {
                if (typeof x === 'number' && typeof y === 'number') {
                    const el = document.elementFromPoint(x, y);
                    if (el) return el;
                }
                return document.querySelector('canvas') || document.body || document.documentElement;
            }

            try {
                Object.defineProperty(document, 'pointerLockElement', {
                    get: function() { return lockedElement || document.querySelector('canvas') || document.body; },
                    configurable: true
                });
                Object.defineProperty(document, 'mozPointerLockElement', {
                    get: function() { return lockedElement || document.querySelector('canvas') || document.body; },
                    configurable: true
                });
                Object.defineProperty(document, 'webkitPointerLockElement', {
                    get: function() { return lockedElement || document.querySelector('canvas') || document.body; },
                    configurable: true
                });

                Element.prototype.requestPointerLock = function() {
                    lockedElement = this;
                    setTimeout(() => {
                        document.dispatchEvent(new Event('pointerlockchange', { bubbles: true }));
                        document.dispatchEvent(new Event('mozpointerlockchange', { bubbles: true }));
                        document.dispatchEvent(new Event('webkitpointerlockchange', { bubbles: true }));
                    }, 5);
                    return Promise.resolve();
                };

                Document.prototype.exitPointerLock = function() {
                    lockedElement = null;
                    document.dispatchEvent(new Event('pointerlockchange', { bubbles: true }));
                };
            } catch (e) {}

            const virtualGamepad = {
                id: "Xbox 360 Controller (XInput STANDARD GAMEPAD)",
                index: 0,
                connected: true,
                timestamp: performance.now(),
                mapping: "standard",
                axes: [0.0, 0.0, 0.0, 0.0],
                buttons: Array.from({ length: 17 }, () => ({ pressed: false, touched: false, value: 0.0 }))
            };

            try {
                navigator.getGamepads = function() {
                    virtualGamepad.timestamp = performance.now();
                    return [virtualGamepad, null, null, null];
                };
                navigator.webkitGetGamepads = navigator.getGamepads;
                setTimeout(() => {
                    try {
                        const gpEvent = new Event('gamepadconnected');
                        gpEvent.gamepad = virtualGamepad;
                        window.dispatchEvent(gpEvent);
                    } catch (e) {}
                }, 400);
            } catch (e) {}

            let virtualCursorX = window.innerWidth / 2;
            let virtualCursorY = window.innerHeight / 2;
            let activeMouseButtonsMask = 0;
            const heldKeys = new Set();

            function createKeyboardEvent(type, key, code, keyCode) {
                const ev = new KeyboardEvent(type, {
                    key: key,
                    code: code,
                    keyCode: keyCode,
                    which: keyCode,
                    bubbles: true,
                    cancelable: true,
                    composed: true,
                    view: window
                });
                try {
                    Object.defineProperty(ev, 'keyCode', { get: () => keyCode });
                    Object.defineProperty(ev, 'which', { get: () => keyCode });
                } catch (e) {}
                return ev;
            }

            function patchMovement(ev, dx, dy) {
                try {
                    Object.defineProperty(ev, 'movementX', { get: () => dx });
                    Object.defineProperty(ev, 'movementY', { get: () => dy });
                    Object.defineProperty(ev, 'mozMovementX', { get: () => dx });
                    Object.defineProperty(ev, 'mozMovementY', { get: () => dy });
                    Object.defineProperty(ev, 'webkitMovementX', { get: () => dx });
                    Object.defineProperty(ev, 'webkitMovementY', { get: () => dy });
                } catch (e) {}
                return ev;
            }

            window.__fivePlayBridge = {
                sendKey: function(key, code, keyCode, isDown, gpIndex) {
                    if (typeof gpIndex === 'number' && gpIndex >= 0 && gpIndex < 17) {
                        virtualGamepad.buttons[gpIndex].pressed = isDown;
                        virtualGamepad.buttons[gpIndex].touched = isDown;
                        virtualGamepad.buttons[gpIndex].value = isDown ? 1.0 : 0.0;
                    }
                    if (isDown) {
                        if (heldKeys.has(code)) return;
                        heldKeys.add(code);
                    } else {
                        heldKeys.delete(code);
                    }
                    const type = isDown ? 'keydown' : 'keyup';
                    const target = findGameTarget(virtualCursorX, virtualCursorY);
                    const ev = createKeyboardEvent(type, key, code, keyCode);
                    if (target) target.dispatchEvent(ev);
                    document.dispatchEvent(createKeyboardEvent(type, key, code, keyCode));
                    window.dispatchEvent(createKeyboardEvent(type, key, code, keyCode));
                },

                sendMouseButtonAt: function(normX, normY, button, isDown, gpIndex) {
                    if (typeof normX === 'number' && typeof normY === 'number' && normX >= 0 && normY >= 0) {
                        virtualCursorX = Math.max(0, Math.min(window.innerWidth - 1, normX * window.innerWidth));
                        virtualCursorY = Math.max(0, Math.min(window.innerHeight - 1, normY * window.innerHeight));
                    }
                    if (typeof gpIndex === 'number' && gpIndex >= 0 && gpIndex < 17) {
                        virtualGamepad.buttons[gpIndex].pressed = isDown;
                        virtualGamepad.buttons[gpIndex].touched = isDown;
                        virtualGamepad.buttons[gpIndex].value = isDown ? 1.0 : 0.0;
                    }
                    const bit = (button === 0) ? 1 : (button === 2 ? 2 : 4);
                    if (isDown) {
                        activeMouseButtonsMask |= bit;
                    } else {
                        activeMouseButtonsMask &= ~bit;
                    }
                    const target = findGameTarget(virtualCursorX, virtualCursorY);
                    if (target && !lockedElement) {
                        lockedElement = target;
                    }
                    const type = isDown ? 'mousedown' : 'mouseup';
                    const mouseInit = {
                        bubbles: true,
                        cancelable: true,
                        view: window,
                        button: button,
                        buttons: activeMouseButtonsMask,
                        clientX: virtualCursorX,
                        clientY: virtualCursorY,
                        screenX: virtualCursorX,
                        screenY: virtualCursorY
                    };
                    if (target) {
                        target.dispatchEvent(new MouseEvent(type, mouseInit));
                        try {
                            target.dispatchEvent(new PointerEvent(isDown ? 'pointerdown' : 'pointerup', Object.assign({ pointerId: 1, isPrimary: true }, mouseInit)));
                        } catch (e) {}
                        if (!isDown && button === 0) {
                            target.dispatchEvent(new MouseEvent('click', mouseInit));
                        }
                    }
                    document.dispatchEvent(new MouseEvent(type, mouseInit));
                    window.dispatchEvent(new MouseEvent(type, mouseInit));
                },

                sendMouseMoveAt: function(normX, normY, dx, dy) {
                    if (typeof normX === 'number' && typeof normY === 'number' && normX >= 0 && normY >= 0) {
                        virtualCursorX = Math.max(0, Math.min(window.innerWidth - 1, normX * window.innerWidth));
                        virtualCursorY = Math.max(0, Math.min(window.innerHeight - 1, normY * window.innerHeight));
                    } else {
                        virtualCursorX = Math.max(0, Math.min(window.innerWidth - 1, virtualCursorX + dx));
                        virtualCursorY = Math.max(0, Math.min(window.innerHeight - 1, virtualCursorY + dy));
                    }
                    const target = findGameTarget(virtualCursorX, virtualCursorY);
                    const moveInit = {
                        bubbles: true,
                        cancelable: true,
                        view: window,
                        clientX: virtualCursorX,
                        clientY: virtualCursorY,
                        screenX: virtualCursorX,
                        screenY: virtualCursorY,
                        movementX: dx,
                        movementY: dy,
                        buttons: activeMouseButtonsMask
                    };
                    if (target) {
                        target.dispatchEvent(patchMovement(new MouseEvent('mousemove', moveInit), dx, dy));
                        try {
                            target.dispatchEvent(patchMovement(new PointerEvent('pointermove', Object.assign({ pointerId: 1, isPrimary: true }, moveInit)), dx, dy));
                        } catch (e) {}
                    }
                    document.dispatchEvent(patchMovement(new MouseEvent('mousemove', moveInit), dx, dy));
                    window.dispatchEvent(patchMovement(new MouseEvent('mousemove', moveInit), dx, dy));
                },

                sendWheel: function(deltaY) {
                    const target = findGameTarget(virtualCursorX, virtualCursorY);
                    const wheelEv = new WheelEvent('wheel', {
                        deltaY: deltaY,
                        clientX: virtualCursorX,
                        clientY: virtualCursorY,
                        bubbles: true,
                        cancelable: true
                    });
                    if (target) target.dispatchEvent(wheelEv);
                    document.dispatchEvent(wheelEv);
                },

                setGamepadAxis: function(axisIndex, value) {
                    if (axisIndex >= 0 && axisIndex < 4) {
                        virtualGamepad.axes[axisIndex] = value;
                    }
                },

                sendChatText: function(text) {
                    const active = document.activeElement;
                    if (active && (active.tagName === 'INPUT' || active.tagName === 'TEXTAREA')) {
                        active.value = text;
                        active.dispatchEvent(new Event('input', { bubbles: true }));
                        active.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    this.sendKey('Enter', 'Enter', 13, true, -1);
                    setTimeout(() => this.sendKey('Enter', 'Enter', 13, false, -1), 60);
                },

                releaseAll: function() {
                    heldKeys.forEach(code => {
                        document.dispatchEvent(new KeyboardEvent('keyup', { code: code, bubbles: true }));
                        window.dispatchEvent(new KeyboardEvent('keyup', { code: code, bubbles: true }));
                    });
                    heldKeys.clear();
                    activeMouseButtonsMask = 0;
                    for (let i = 0; i < 17; i++) {
                        virtualGamepad.buttons[i].pressed = false;
                        virtualGamepad.buttons[i].value = 0.0;
                    }
                    virtualGamepad.axes = [0.0, 0.0, 0.0, 0.0];
                },

                applyMemoryOptimization: function(lowRam) {
                    const maxW = lowRam ? 1280 : 1600;
                    const maxH = lowRam ? 720 : 900;
                    const canvases = document.querySelectorAll('canvas');
                    canvases.forEach(function(c) {
                        if (c.width > maxW) c.width = maxW;
                        if (c.height > maxH) c.height = maxH;
                    });
                }
            };

            let frameCount = 0;
            let lastTime = performance.now();
            function tickFps(now) {
                frameCount++;
                if (now - lastTime >= 1000) {
                    const fps = Math.round((frameCount * 1000) / (now - lastTime));
                    frameCount = 0;
                    lastTime = now;
                    if (window.FivePlayNative && window.FivePlayNative.onFpsUpdate) {
                        window.FivePlayNative.onFpsUpdate(fps);
                        window.FivePlayNative.onGameCanvasStatus(!!document.querySelector('canvas'));
                    }
                }
                requestAnimationFrame(tickFps);
            }
            requestAnimationFrame(tickFps);
        })();
    """.trimIndent()

    fun dispatchKeySpec(
        webView: WebView?,
        spec: PcKeySpec,
        isDown: Boolean,
        cursorNormX: Float = 0.5f,
        cursorNormY: Float = 0.5f,
        sendNativeTapOnCursorClick: Boolean = false
    ) {
        val wv = webView ?: return
        if (spec.wheelDelta != 0) {
            if (isDown) {
                dispatchWheel(wv, spec.wheelDelta)
            }
            return
        }
        if (spec.isMouse) {
            wv.evaluateJavascript(
                "window.__fivePlayBridge && window.__fivePlayBridge.sendMouseButtonAt($cursorNormX, $cursorNormY, ${spec.mouseButton}, $isDown, ${spec.gamepadButtonIndex});",
                null
            )
            if (sendNativeTapOnCursorClick && spec.mouseButton == 0 && wv.width > 0 && wv.height > 0) {
                val px = (cursorNormX * wv.width).coerceIn(1f, (wv.width - 1).toFloat())
                val py = (cursorNormY * wv.height).coerceIn(1f, (wv.height - 1).toFloat())
                val now = SystemClock.uptimeMillis()
                val action = if (isDown) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_UP
                val ev = MotionEvent.obtain(now, now, action, px, py, 0)
                wv.dispatchTouchEvent(ev)
                ev.recycle()
            }
        } else {
            val escapedKey = spec.key.replace("'", "\\'")
            wv.evaluateJavascript(
                "window.__fivePlayBridge && window.__fivePlayBridge.sendKey('$escapedKey', '${spec.code}', ${spec.keyCode}, $isDown, ${spec.gamepadButtonIndex});",
                null
            )
        }
    }

    fun dispatchMouseMoveAt(
        webView: WebView?,
        normX: Float,
        normY: Float,
        dx: Float,
        dy: Float
    ) {
        val wv = webView ?: return
        val rx = (dx * 10f).toInt() / 10f
        val ry = (dy * 10f).toInt() / 10f
        if (rx == 0f && ry == 0f) return
        wv.evaluateJavascript(
            "window.__fivePlayBridge && window.__fivePlayBridge.sendMouseMoveAt($normX, $normY, $rx, $ry);",
            null
        )
    }

    fun dispatchGamepadAxes(webView: WebView?, leftX: Float, leftY: Float, rightX: Float, rightY: Float) {
        val wv = webView ?: return
        wv.evaluateJavascript(
            """
            if (window.__fivePlayBridge) {
                window.__fivePlayBridge.setGamepadAxis(0, $leftX);
                window.__fivePlayBridge.setGamepadAxis(1, $leftY);
                window.__fivePlayBridge.setGamepadAxis(2, $rightX);
                window.__fivePlayBridge.setGamepadAxis(3, $rightY);
            }
            """.trimIndent(),
            null
        )
    }

    fun dispatchWheel(webView: WebView?, deltaY: Int) {
        val wv = webView ?: return
        wv.evaluateJavascript(
            "window.__fivePlayBridge && window.__fivePlayBridge.sendWheel($deltaY);",
            null
        )
    }

    fun dispatchChatMessage(webView: WebView?, message: String) {
        val wv = webView ?: return
        val safe = message.replace("\\", "\\\\").replace("'", "\\'")
        wv.evaluateJavascript(
            "window.__fivePlayBridge && window.__fivePlayBridge.sendChatText('$safe');",
            null
        )
    }

    fun triggerCheat(webView: WebView?, cheat: String) {
        val wv = webView ?: return
        val safe = cheat.replace("'", "\\'")
        wv.evaluateJavascript(
            "if (typeof applyCheat === 'function') { applyCheat('$safe'); }",
            null
        )
    }

    fun releaseAllInputs(webView: WebView?) {
        val wv = webView ?: return
        wv.evaluateJavascript(
            "window.__fivePlayBridge && window.__fivePlayBridge.releaseAll();",
            null
        )
    }

    fun applyMemoryOptimization(webView: WebView?, lowRam: Boolean) {
        val wv = webView ?: return
        wv.evaluateJavascript(
            "window.__fivePlayBridge && window.__fivePlayBridge.applyMemoryOptimization($lowRam);",
            null
        )
    }
}

