package xyz.gaon.componentory.lab

import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import xyz.gaon.componentory.R

@Composable
internal fun PlatformMediaSample(
    family: PlatformFamily,
    component: LabComponent,
    viewId: Int,
    enabled: Boolean,
    state: SampleState,
    modifier: Modifier,
) {
    AndroidView(
        factory = { context ->
            val themed = family.createContext(context)
            createMediaWidget(themed, component, state).apply { id = viewId }
        },
        update = { view -> view.isEnabled = enabled },
        onRelease = { view -> (view as? VideoView)?.suspend() },
        modifier = modifier.testTag("sample-${component.name.lowercase()}"),
    )
}

// Both samples play the same bundled two-second clip through the real media
// pipeline. VIDEO_VIEW toggles play/pause on taps and reports that flag as its
// copied state. MEDIA_CONTROLLER is the component under test: it lives on the
// VideoView's tag while the anchor carries the view id, and its transport
// buttons drive the player directly.
private fun createMediaWidget(
    themed: ContextThemeWrapper,
    component: LabComponent,
    state: SampleState,
): VideoView {
    val video = VideoView(themed)
    video.minimumHeight = (96 * themed.resources.displayMetrics.density).toInt()
    video.setVideoURI(Uri.parse("android.resource://${themed.packageName}/${R.raw.sample_clip}"))
    return if (component == LabComponent.MEDIA_CONTROLLER) {
        video.apply {
            val controller = MediaController(themed)
            setMediaController(controller)
            tag = controller
        }
    } else {
        video.apply {
            setOnPreparedListener { if (state.value == 1) start() }
            setOnCompletionListener { state.value = 0 }
            setOnClickListener {
                if (isPlaying) {
                    pause()
                    state.value = 0
                } else {
                    start()
                    state.value = 1
                }
            }
        }
    }
}
