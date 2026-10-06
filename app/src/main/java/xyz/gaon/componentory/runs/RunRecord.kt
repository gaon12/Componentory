package xyz.gaon.componentory.runs

// One saved experiment: which component ran, which providers drove each panel,
// the inputs the panels held, and the environment the run happened in. All
// values are strings so every state field survives without a schema version.
data class RunRecord(
    val id: String,
    val createdAtEpochMillis: Long,
    val component: String,
    val leftFamily: String,
    val rightFamily: String,
    val leftInputs: Map<String, String>,
    val rightInputs: Map<String, String>,
    val environment: Map<String, String>,
) {
    fun toJson(): String = buildString {
        append("{")
        field("id", id)
        field("createdAt", createdAtEpochMillis.toString())
        field("component", component)
        field("leftFamily", leftFamily)
        field("rightFamily", rightFamily)
        append("\"leftInputs\":")
        map(leftInputs)
        append(",\"rightInputs\":")
        map(rightInputs)
        append(",\"environment\":")
        map(environment)
        append("}")
    }

    private fun StringBuilder.field(name: String, value: String) {
        if (isNotEmpty() && last() != '{') append(',')
        append('"').append(name).append("\":\"").append(escape(value)).append('"')
    }

    private fun StringBuilder.map(entries: Map<String, String>) {
        append("{")
        entries.forEach { (key, value) ->
            if (last() != '{') append(',')
            append('"').append(escape(key)).append("\":\"").append(escape(value)).append('"')
        }
        append("}")
    }

    companion object {
        fun fromJson(line: String): RunRecord? {
            val fields = parseObject(line.trim()) ?: return null
            val id = fields["id"] ?: return null
            val created = fields["createdAt"]?.toLongOrNull() ?: return null
            val component = fields["component"] ?: return null
            val leftFamily = fields["leftFamily"] ?: return null
            val rightFamily = fields["rightFamily"] ?: return null
            val leftInputs = parseObject(fields["leftInputs"] ?: return null) ?: return null
            val rightInputs = parseObject(fields["rightInputs"] ?: return null) ?: return null
            val environment = parseObject(fields["environment"] ?: return null) ?: return null
            return RunRecord(
                id,
                created,
                component,
                leftFamily,
                rightFamily,
                leftInputs,
                rightInputs,
                environment,
            )
        }

        private fun escape(value: String): String = buildString {
            for (char in value) {
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
        }

        private fun unescape(value: String): String = buildString {
            var index = 0
            while (index < value.length) {
                val char = value[index]
                if (char == '\\' && index + 1 < value.length) {
                    index++
                    when (value[index]) {
                        'n' -> append('\n')
                        'r' -> append('\r')
                        't' -> append('\t')
                        else -> append(value[index])
                    }
                } else append(char)
                index++
            }
        }

        // Reads a flat {"key":"value"} object into ordered pairs; nested braces
        // mark the value raw so caller code can parse it as another object.
        private fun parseObject(text: String): Map<String, String>? {
            val trimmed = text.trim()
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null
            val fields = linkedMapOf<String, String>()
            var index = 1
            val end = trimmed.length - 1
            while (index < end) {
                val keyStart = trimmed.indexOf('"', index)
                if (keyStart < 0 || keyStart >= end) break
                val keyEnd = findStringEnd(trimmed, keyStart) ?: return null
                val key = unescape(trimmed.substring(keyStart + 1, keyEnd))
                var cursor = keyEnd + 1
                if (cursor >= end || trimmed[cursor] != ':') return null
                cursor++
                if (cursor < end && trimmed[cursor] == '{') {
                    var depth = 0
                    var valueEnd = cursor
                    var inString = false
                    var escaped = false
                    while (valueEnd <= end) {
                        val char = trimmed[valueEnd]
                        when {
                            escaped -> escaped = false
                            inString && char == '\\' -> escaped = true
                            char == '"' -> inString = !inString
                            !inString && char == '{' -> depth++
                            !inString && char == '}' -> {
                                depth--
                                if (depth == 0) break
                            }
                        }
                        valueEnd++
                    }
                    if (valueEnd > end) return null
                    fields[key] = trimmed.substring(cursor, valueEnd + 1)
                    cursor = valueEnd + 1
                } else if (cursor < end && trimmed[cursor] == '"') {
                    val valueEnd = findStringEnd(trimmed, cursor) ?: return null
                    fields[key] = unescape(trimmed.substring(cursor + 1, valueEnd))
                    cursor = valueEnd + 1
                } else return null
                if (cursor < end && trimmed[cursor] == ',') cursor++
                index = cursor
            }
            return fields
        }

        private fun findStringEnd(text: String, quoteStart: Int): Int? {
            var index = quoteStart + 1
            while (index < text.length) {
                when (text[index]) {
                    '\\' -> index++
                    '"' -> return index
                }
                index++
            }
            return null
        }
    }
}
