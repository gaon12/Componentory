package xyz.gaon.componentory.lab.inline

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.ComponentName
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.Dataset
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi

@RequiresApi(30)
class InlineDemoAutofillService : AutofillService() {
    private var attribution: PendingIntent? = null

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
    ) {
        val structure = request.fillContexts.lastOrNull()?.structure
        // Reject other Activities before traversing their trees. No user text is read or saved.
        if (
            structure == null ||
                structure.activityComponent !=
                    ComponentName(this, InlineDemoActivity::class.java) ||
                cancellationSignal.isCanceled
        ) {
            callback.onSuccess(null)
            return
        }
        val id = findDemoId(structure)
        if (id == null) {
            callback.onSuccess(null)
            return
        }
        val presentation =
            RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
                setTextViewText(android.R.id.text1, InlineDemoSession.DEMO_VALUE)
            }
        val dataset = Dataset.Builder(presentation)
        val spec = request.inlineSuggestionsRequest?.inlinePresentationSpecs?.firstOrNull()
        if (spec != null) {
            attribution?.cancel()
            val intent =
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, InlineDemoActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            attribution = intent
            val content: UiVersions.Content =
                InlineSuggestionUi.newContentBuilder(intent)
                    .setTitle(InlineDemoSession.DEMO_VALUE)
                    .setContentDescription(InlineDemoSession.DEMO_VALUE)
                    .build()

            dataset.setValue(
                id,
                AutofillValue.forText(InlineDemoSession.DEMO_VALUE),
                presentation,
                InlinePresentation(content.slice, spec, false),
            )
        } else
            dataset.setValue(id, AutofillValue.forText(InlineDemoSession.DEMO_VALUE), presentation)
        if (cancellationSignal.isCanceled) callback.onSuccess(null)
        else callback.onSuccess(FillResponse.Builder().addDataset(dataset.build()).build())
    }

    private fun findDemoId(structure: AssistStructure): AutofillId? {
        for (index in 0 until structure.windowNodeCount) {
            findDemoId(structure.getWindowNodeAt(index).rootViewNode)?.let {
                return it
            }
        }
        return null
    }

    private fun findDemoId(node: AssistStructure.ViewNode): AutofillId? {
        if (node.idEntry == "inline_sample_input") return node.autofillId
        for (index in 0 until node.childCount) findDemoId(node.getChildAt(index))?.let {
            return it
        }
        return null
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }

    override fun onDestroy() {
        attribution?.cancel()
        attribution = null
        super.onDestroy()
    }
}
