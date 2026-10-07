package xyz.gaon.componentory.lab.recreation

import androidx.annotation.LayoutRes
import xyz.gaon.componentory.R
import xyz.gaon.componentory.lab.DesignFamily

/** Platform artwork associated with an era, rather than a Compose Toast API. */
internal object ResourceToasts {
    data class Release(
        @param:LayoutRes val layout: Int,
        val release: String,
        val commit: String,
        val hasIcon: Boolean = false,
        val libraryPalette: Boolean = false,
        val lineHeightSp: Float = 0f,
    ) {
        val sourcePath: String
            get() =
                if (hasIcon) "packages/SystemUI/res/layout/text_toast.xml"
                else "core/res/res/layout/transient_notification.xml"

        val sourceLabel: String
            get() = "AOSP · $release · $sourcePath"
    }

    private val classic =
        Release(
            R.layout.aosp_toast_classic_layout,
            "android-2.3.7_r1",
            "3f2821425f1ab6eddb76a8725e3f2c3edb5d8b07",
        )
    private val holo =
        Release(
            R.layout.aosp_toast_holo_layout,
            "android-4.4.4_r2",
            "63ade05d76785975fc3292ca030abbaa1dda8891",
        )
    private val material1 =
        Release(
            R.layout.aosp_toast_material1_layout,
            "android-5.0.2_r1",
            "0ac9664a9ee2e6d1d3b68fe00c264ad94fe98966",
        )
    private val material2 =
        Release(
            R.layout.aosp_toast_material2_layout,
            "android-9.0.0_r1",
            "6549309f6c473b792ec62d1aebadec62bcf07827",
        )
    private val material3 =
        Release(
            R.layout.aosp_toast_material3_layout,
            "android-12.0.0_r1",
            "cebf5c06997b64f4e47a1611edb5f97044509d76",
            hasIcon = true,
            libraryPalette = true,
            lineHeightSp = 20f,
        )
    private val expressive =
        Release(
            R.layout.aosp_toast_expressive_layout,
            "android-16.0.0_r1",
            "99b01a65cc4c104933788b3143285ab6bae65827",
            hasIcon = true,
            libraryPalette = true,
        )

    fun forFamily(family: DesignFamily): Release =
        when (family) {
            DesignFamily.CLASSIC -> classic
            DesignFamily.HOLO -> holo
            DesignFamily.MATERIAL -> material1
            DesignFamily.MATERIAL2 -> material2
            DesignFamily.MATERIAL3,
            DesignFamily.MATERIAL_YOU -> material3
            DesignFamily.EXPRESSIVE -> expressive
        }
}
