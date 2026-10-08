package xyz.gaon.componentory.eastereggs

private const val TAP_WINDOW_MILLIS = 1_000L
private const val TAPS_TO_OPEN = 3

internal class EasterEggTapSequence {
    private var firstTapAt = 0L
    private var tapCount = 0

    fun registerTap(uptimeMillis: Long): Boolean {
        if (tapCount == 0 || uptimeMillis - firstTapAt !in 0..TAP_WINDOW_MILLIS) {
            firstTapAt = uptimeMillis
            tapCount = 0
        }
        tapCount++
        if (tapCount == TAPS_TO_OPEN) {
            tapCount = 0
            return true
        }
        return false
    }
}
