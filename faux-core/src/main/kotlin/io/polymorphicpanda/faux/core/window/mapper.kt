package io.polymorphicpanda.faux.core.window

import io.polymorphicpanda.faux.core.util.PlatformTypeMapper
import io.polymorphicpanda.faux.event.Event
import io.polymorphicpanda.faux.event.MouseButtonEvent
import io.polymorphicpanda.faux.event.MouseMoveEvent
import io.polymorphicpanda.faux.input.InputAction
import io.polymorphicpanda.faux.input.MouseButton
import mu.KotlinLogging
import org.lwjgl.glfw.GLFW


class GlfwEventMapper(private val processor: (Event) -> Unit) {
    private val logger = KotlinLogging.logger {}

    fun mouseMove(x: Double, y: Double) {
        processor(MouseMoveEvent(x, y))
    }

    fun mouseButton(x: Double, y: Double, button: Int, action: Int, mods: Int) {
        val platformButton = GlfwMouseButtonMapper.fromPlatformType(button)
        val platformAction = GlfwInputActionMapper.fromPlatformType(action)
        if (platformButton != null && platformAction != null) {
            processor(MouseButtonEvent(x, y, platformButton, platformAction))
        } else {
            logger.warn {
                "Unsupported mouse action: (button=$button, action=$action, mods=$mods)"
            }
        }
    }
}

object GlfwMouseButtonMapper: PlatformTypeMapper<Int, MouseButton>(
    mapOf(
        GLFW.GLFW_MOUSE_BUTTON_LEFT to MouseButton.LEFT,
        GLFW.GLFW_MOUSE_BUTTON_RIGHT to MouseButton.RIGHT,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE to MouseButton.MIDDLE,
        GLFW.GLFW_MOUSE_BUTTON_1 to MouseButton.BUTTON_1,
        GLFW.GLFW_MOUSE_BUTTON_2 to MouseButton.BUTTON_2,
        GLFW.GLFW_MOUSE_BUTTON_3 to MouseButton.BUTTON_3,
        GLFW.GLFW_MOUSE_BUTTON_4 to MouseButton.BUTTON_4,
        GLFW.GLFW_MOUSE_BUTTON_5 to MouseButton.BUTTON_5,
        GLFW.GLFW_MOUSE_BUTTON_6 to MouseButton.BUTTON_6,
        GLFW.GLFW_MOUSE_BUTTON_7 to MouseButton.BUTTON_7,
        GLFW.GLFW_MOUSE_BUTTON_8 to MouseButton.BUTTON_8
    )
)

object GlfwInputActionMapper: PlatformTypeMapper<Int, InputAction>(
    mapOf(
        GLFW.GLFW_PRESS to InputAction.PRESS,
        GLFW.GLFW_RELEASE to InputAction.RELEASE
    )
)
