// Modified for Componentory: Retain the used inset mask and remove unused calls to private AndroidX methods.
package androidx.activity;

import androidx.core.view.WindowInsetsCompat;

/** Insets used by the imported game layouts. */
public final class EdgeToEdgeCompat {
    public static final int EDGE_INSETS_MASK =
            WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    private EdgeToEdgeCompat() {}
}
