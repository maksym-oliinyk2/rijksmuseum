package io.github.oliinyk.maksym.rijksmuseum.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.navigation3.scene.Scene

private const val DefaultTransitionDuration = 500

@Suppress("MagicNumber")
private val TransitionEasing = CubicBezierEasing(0.2833f, 0.99f, 0.31833f, 0.99f)

internal fun <T : Any> AppTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(DefaultTransitionDuration, easing = TransitionEasing),
        ),
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            targetOffset = { it / 4 },
            animationSpec = tween(DefaultTransitionDuration, easing = TransitionEasing),
        ),
    )
}

internal fun <T : Any> AppPopTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            initialOffset = { it / 4 },
            animationSpec = tween(DefaultTransitionDuration, easing = TransitionEasing),
        ),
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(DefaultTransitionDuration, easing = TransitionEasing),
        ),
    )
}
