package rubit.coreweb.validation

import jakarta.validation.ElementKind
import jakarta.validation.Path

object ConstraintPathNormalizer {
    fun normalize(path: Path): String {
        val propertyParts = mutableListOf<String>()
        var parameterName: String? = null
        var lastNodeName: String? = null

        for (node in path) {
            val name = node.name
            if (!name.isNullOrBlank()) {
                lastNodeName = name
            }
            when (node.kind) {
                ElementKind.PROPERTY -> {
                    if (!name.isNullOrBlank()) {
                        propertyParts.add(name)
                    }
                }
                ElementKind.PARAMETER -> {
                    if (!name.isNullOrBlank()) {
                        parameterName = name
                    }
                }
                else -> Unit
            }
        }

        if (propertyParts.isNotEmpty()) {
            return propertyParts.joinToString(".")
        }
        if (!parameterName.isNullOrBlank() && !parameterName!!.startsWith("arg")) {
            return parameterName!!
        }
        val fallback = lastNodeName?.takeUnless { it.startsWith("arg") }
        if (fallback != null) {
            return fallback
        }

        val raw = path.toString()
        val cleaned = raw
            .split('.')
            .filter { it.isNotBlank() }
            .filterNot { it.matches(Regex("arg\\d+")) }
            .let { segments ->
                if (segments.size > 1) segments.drop(1) else segments
            }
            .joinToString(".")
            .trim()
        return if (cleaned.isNotBlank()) cleaned else raw
    }
}
