package rubit.coreweb.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DateRangeValidator : ConstraintValidator<DateRange, Any?> {
    private var min: String = ""
    private var max: String = ""
    private var type: DateRangeType = DateRangeType.AUTO
    private var allowNull: Boolean = true
    private var allowBlank: Boolean = false

    override fun initialize(constraintAnnotation: DateRange) {
        min = constraintAnnotation.min.trim()
        max = constraintAnnotation.max.trim()
        type = constraintAnnotation.type
        allowNull = constraintAnnotation.allowNull
        allowBlank = constraintAnnotation.allowBlank
    }

    override fun isValid(value: Any?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return allowNull
        }
        return when (value) {
            is LocalDate -> isValidDate(value)
            is LocalDateTime -> isValidDateTime(value)
            is LocalTime -> isValidTime(value)
            is CharSequence -> isValidText(value.toString())
            else -> false
        }
    }

    private fun isValidDate(value: LocalDate): Boolean {
        if (type != DateRangeType.AUTO && type != DateRangeType.DATE) {
            return false
        }
        val pattern = DateRangeType.DATE.defaultPattern ?: return false
        return isInRange(
            value,
            parseDate(min, pattern),
            parseDate(max, pattern)
        )
    }

    private fun isValidDateTime(value: LocalDateTime): Boolean {
        if (type != DateRangeType.AUTO && type != DateRangeType.DATE_TIME) {
            return false
        }
        val pattern = DateRangeType.DATE_TIME.defaultPattern ?: return false
        return isInRange(
            value,
            parseDateTime(min, pattern),
            parseDateTime(max, pattern)
        )
    }

    private fun isValidTime(value: LocalTime): Boolean {
        if (type != DateRangeType.AUTO && type != DateRangeType.TIME) {
            return false
        }
        val pattern = DateRangeType.TIME.defaultPattern ?: return false
        return isInRange(
            value,
            parseTime(min, pattern),
            parseTime(max, pattern)
        )
    }

    private fun isValidText(text: String): Boolean {
        if (text.isBlank()) {
            return allowBlank
        }
        return when (type) {
            DateRangeType.DATE -> {
                val pattern = DateRangeType.DATE.defaultPattern ?: return false
                val value = parseDate(text, pattern) ?: return false
                isInRange(value, parseDate(min, pattern), parseDate(max, pattern))
            }
            DateRangeType.DATE_TIME -> {
                val pattern = DateRangeType.DATE_TIME.defaultPattern ?: return false
                val value = parseDateTime(text, pattern) ?: return false
                isInRange(value, parseDateTime(min, pattern), parseDateTime(max, pattern))
            }
            DateRangeType.TIME -> {
                val pattern = DateRangeType.TIME.defaultPattern ?: return false
                val value = parseTime(text, pattern) ?: return false
                isInRange(value, parseTime(min, pattern), parseTime(max, pattern))
            }
            DateRangeType.AUTO -> {
                DateRangeType.DATE_TIME.defaultPattern?.let { pattern ->
                    parseDateTime(text, pattern)?.let { value ->
                        return isInRange(value, parseDateTime(min, pattern), parseDateTime(max, pattern))
                    }
                }
                DateRangeType.DATE.defaultPattern?.let { pattern ->
                    parseDate(text, pattern)?.let { value ->
                        return isInRange(value, parseDate(min, pattern), parseDate(max, pattern))
                    }
                }
                DateRangeType.TIME.defaultPattern?.let { pattern ->
                    parseTime(text, pattern)?.let { value ->
                        return isInRange(value, parseTime(min, pattern), parseTime(max, pattern))
                    }
                }
                false
            }
        }
    }

    private fun parseDate(value: String, pattern: String): LocalDate? {
        if (value.isBlank()) {
            return null
        }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return runCatching { LocalDate.parse(value, formatter) }.getOrNull()
    }

    private fun parseDateTime(value: String, pattern: String): LocalDateTime? {
        if (value.isBlank()) {
            return null
        }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return runCatching { LocalDateTime.parse(value, formatter) }.getOrNull()
    }

    private fun parseTime(value: String, pattern: String): LocalTime? {
        if (value.isBlank()) {
            return null
        }
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return runCatching { LocalTime.parse(value, formatter) }.getOrNull()
    }

    private fun <T : Comparable<T>> isInRange(
        value: T,
        minValue: T?,
        maxValue: T?
    ): Boolean {
        if (minValue != null && value < minValue) {
            return false
        }
        if (maxValue != null && value > maxValue) {
            return false
        }
        return true
    }
}
