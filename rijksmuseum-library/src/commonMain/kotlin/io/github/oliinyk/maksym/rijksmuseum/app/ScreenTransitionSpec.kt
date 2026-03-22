package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.navigation3.scene.Scene

private const val DEFAULT_TRANSITION_DURATION_MILLISECOND = 500
private val TransitionEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

internal fun <T : Any> AppTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND, easing = TransitionEasing),
        ),
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            targetOffset = { it / 4 },
            animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND, easing = TransitionEasing),
        ),
    )
}

internal fun <T : Any> AppPopTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            initialOffset = { it / 4 },
            animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND, easing = TransitionEasing),
        ),
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(DEFAULT_TRANSITION_DURATION_MILLISECOND, easing = TransitionEasing),
        ),
    )
}
