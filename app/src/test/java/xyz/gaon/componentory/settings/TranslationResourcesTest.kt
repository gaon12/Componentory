package xyz.gaon.componentory.settings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.w3c.dom.Element

class TranslationResourcesTest {
    @Test
    fun everySupportedLanguageHasAllStringsAndMatchingFormatArguments() {
        val base = strings("values")
        listOf("values-ko", "values-ja", "values-b+zh+Hans", "values-b+zh+Hant").forEach { folder ->
            val translated = strings(folder)
            assertEquals(
                "Missing or extra resources in $folder",
                base.keys.map { it.substringBefore(':') }.toSet(),
                translated.keys.map { it.substringBefore(':') }.toSet(),
            )
            base.forEach { (key, text) ->
                val localText =
                    requireNotNull(
                        translated[key] ?: translated[key.substringBefore(':') + ":other"]
                    ) {
                        "Missing text or plural fallback in $folder/$key"
                    }
                assertFalse("Empty $folder/$key", localText.isBlank())
                assertEquals(
                    "Format arguments in $folder/$key",
                    placeholders(text),
                    placeholders(localText),
                )
            }
        }
    }

    @Test
    fun languageTagsDistinguishChineseScriptsAndSystemFallback() {
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromTag("zh-CN"))
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromTag("zh-Hans"))
        assertEquals(AppLanguage.SIMPLIFIED_CHINESE, AppLanguage.fromTag("zh-Hans-HK"))
        assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.fromTag("zh-TW"))
        assertEquals(AppLanguage.TRADITIONAL_CHINESE, AppLanguage.fromTag("zh-Hant"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"))
        assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(""))
    }

    private fun placeholders(text: String) =
        Regex("%[0-9]+\\$[ds]").findAll(text).map { it.value }.sorted().toList()

    private fun strings(folder: String): Map<String, String> {
        val document =
            DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(File("src/main/res/$folder/strings.xml"))
        val nodes = document.getElementsByTagName("string")
        val rows =
            (0 until nodes.length)
                .map { nodes.item(it) as Element }
                .filter { it.getAttribute("translatable") != "false" }
        val strings = rows.associate { it.getAttribute("name") to it.textContent }.toMutableMap()
        assertEquals("Duplicate string names in $folder", rows.size, strings.size)
        val plurals = document.getElementsByTagName("plurals")
        for (index in 0 until plurals.length) {
            val plural = plurals.item(index) as Element
            val items = plural.getElementsByTagName("item")
            for (itemIndex in 0 until items.length) {
                val item = items.item(itemIndex) as Element
                val key = plural.getAttribute("name") + ":" + item.getAttribute("quantity")
                assertEquals(
                    "Duplicate plural item in $folder/$key",
                    null,
                    strings.put(key, item.textContent),
                )
            }
        }
        return strings
    }
}
