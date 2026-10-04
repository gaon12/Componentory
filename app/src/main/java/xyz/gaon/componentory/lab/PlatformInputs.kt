package xyz.gaon.componentory.lab

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.MultiAutoCompleteTextView
import android.widget.SearchView
import android.widget.Spinner
import xyz.gaon.componentory.R

private val inputOptions = listOf("Alpha", "Beta", "Gamma")

internal fun createPlatformInput(context: Context, component: LabComponent): View =
    when (component) {
        LabComponent.AUTOCOMPLETE ->
            AutoCompleteTextView(context).apply {
                hint = context.getString(R.string.sample_item_hint)
                threshold = 1
                setSingleLine(true)
                setAdapter(
                    ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, inputOptions)
                )
            }
        LabComponent.MULTI_AUTOCOMPLETE ->
            MultiAutoCompleteTextView(context).apply {
                hint = context.getString(R.string.sample_multi_item_hint)
                threshold = 1
                setSingleLine(true)
                setTokenizer(MultiAutoCompleteTextView.CommaTokenizer())
                setAdapter(
                    ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, inputOptions)
                )
            }
        LabComponent.SPINNER ->
            Spinner(context).apply {
                adapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_item, inputOptions)
                        .apply {
                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        }
            }
        LabComponent.SEARCH_VIEW ->
            SearchView(context).apply {
                isIconifiedByDefault = false
                isSubmitButtonEnabled = true
                queryHint = context.getString(R.string.sample_search_hint)
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }

internal fun updatePlatformInput(
    view: View,
    component: LabComponent,
    enabled: Boolean,
    state: SampleState,
) {
    when (component) {
        LabComponent.AUTOCOMPLETE,
        LabComponent.MULTI_AUTOCOMPLETE ->
            (view as AutoCompleteTextView).apply {
                updateEditableInput(this, state)
                if (!enabled) dismissDropDown()
            }
        LabComponent.SPINNER ->
            (view as Spinner).apply {
                onItemSelectedListener = null
                if (selectedItemPosition != state.value) setSelection(state.value)
                onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            selected: View?,
                            position: Int,
                            id: Long,
                        ) {
                            state.value = position
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                    }
            }
        LabComponent.SEARCH_VIEW ->
            (view as SearchView).apply {
                setOnQueryTextListener(null)
                if (query.toString() != state.text) setQuery(state.text, false)
                setOnQueryTextListener(
                    object : SearchView.OnQueryTextListener {
                        override fun onQueryTextChange(newText: String): Boolean {
                            state.text = newText
                            return false
                        }

                        override fun onQueryTextSubmit(query: String): Boolean {
                            if (enabled) state.value++
                            clearFocus()
                            return true
                        }
                    }
                )
                // SearchView's enabled flag does not disable its editable children.
                setInputEnabled(this, enabled)
                if (!enabled) clearFocus()
            }
        else -> error("Unsupported components must be handled by SamplePanel.")
    }
}

internal fun updateEditableInput(view: EditText, state: SampleState) {
    (view.tag as? TextWatcher)?.let { view.removeTextChangedListener(it) }
    if (view.text.toString() != state.text) {
        if (view is AutoCompleteTextView) view.setText(state.text, false)
        else view.setText(state.text)
        view.setSelection(state.text.length)
    }
    val watcher =
        object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                state.text = s?.toString().orEmpty()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        }
    view.tag = watcher
    view.addTextChangedListener(watcher)
}

private fun setInputEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
    if (view is ViewGroup) {
        for (index in 0 until view.childCount) setInputEnabled(view.getChildAt(index), enabled)
    }
}
