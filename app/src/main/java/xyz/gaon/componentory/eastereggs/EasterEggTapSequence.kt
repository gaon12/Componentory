package xyz.gaon.componentory.eastereggs

private const val TAPS_TO_OPEN = 7

internal class EasterEggTapSequence {
    private var remainingTaps = TAPS_TO_OPEN

    // Zero tells the caller to open the screen; the next visit starts at seven again.
    fun registerTap(): Int {
        remainingTaps--
        val remaining = remainingTaps
        if (remaining == 0) reset()
        return remaining
    }

    fun reset() {
        remainingTaps = TAPS_TO_OPEN
    }
}
