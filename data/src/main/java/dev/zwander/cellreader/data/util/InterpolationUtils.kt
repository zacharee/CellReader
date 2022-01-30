package dev.zwander.cellreader.data.util

private fun a(t: Float, s: Float): Float {
    return t * t * ((s + 1) * t - s)
}

private fun o(t: Float, s: Float): Float {
    return t * t * ((s + 1) * t + s)
}

fun anticipateDecelerateInterpolator(float: Float, tension: Float = 2.0f * 1.5f): Float {
    return if (float < 0.5f) 0.5f * a(
        float * 2.0f,
        tension
    ) else 0.5f * (o(float * 2.0f - 2.0f, 0f) + 2.0f)
}

fun overshootAccelerateInterpolator(float: Float, tension: Float = 2.0f * 1.5f): Float {
    return if (float < 0.5f) 0.5f * a(
        float * 2.0f,
        0f
    ) else 0.5f * (o(float * 2.0f - 2.0f, tension) + 2.0f)
}