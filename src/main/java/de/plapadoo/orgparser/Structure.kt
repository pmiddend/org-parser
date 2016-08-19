package de.plapadoo.orgparser

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by philipp on 8/7/16.
 */
data class Headline(
        val level: Int,
        val keyword: String?,
        val priority: Char?,
        val title: String,
        val tags: List<String>) {
    fun toOrg(): String {
        return "*".repeat(level) + (if (keyword != null) " $keyword" else "") + (if (priority != null) "#[$priority]" else "") + " $title" + (if (tags.isNotEmpty()) "\t" + tags.fold(":", { prev, tag -> "$prev$tag:" }) else "")
    }
}

data class GreaterBlock(
        val type: String,
        val parameters: String?,
        val content: List<String>)

data class DynamicBlock(
        val type: String,
        val parameters: String?,
        val content: List<String>)

data class Property(
        val name: String,
        val value: String?,
        val plus: Boolean)

data class PropertyDrawer(
        val properties: List<Property>)

data class Drawer(
        val name: String,
        val content: String)

data class FootnoteLabel(
        val label: String,
        val numeric: Boolean)

data class Footnote(
        val label: FootnoteLabel,
        val content: String)

data class AffiliatedKeyword(
        val key: String,
        val optional: String?,
        val value: String)

data class Date(
        val date: LocalDate)

data class Time(
        val time: LocalTime)

enum class Repeater {
    CUMULATE,
    CATCH_UP,
    RESTART,
    ALL,
    FIRST
}

enum class RepeaterUnit {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

data class RepeaterOrDelay(
        val mark: Repeater,
        val value: Int,
        val unit: RepeaterUnit) {
    fun toOrg(): String {
        val mark = when(mark) {
            Repeater.CUMULATE -> "+"
            Repeater.CATCH_UP -> "++"
            Repeater.RESTART -> ".+"
            Repeater.ALL -> "-"
            Repeater.FIRST -> "--"
        }
        val unit = when(unit) {
            RepeaterUnit.HOUR -> "h"
            RepeaterUnit.DAY -> "d"
            RepeaterUnit.WEEK -> "w"
            RepeaterUnit.MONTH -> "m"
            RepeaterUnit.YEAR -> "y"
        }

        return mark + value + unit
    }
}

sealed class Timestamp {
    class Sexp(val descriptor: String) : Timestamp() {
        override fun toOrg(): String {
            return "<%%$descriptor>"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Sexp

            if (descriptor != other.descriptor) return false

            return true
        }

        override fun hashCode(): Int {
            return descriptor.hashCode()
        }

        override fun toString(): String {
            return "Sexp(descriptor='$descriptor')"
        }
    }

    class Active(val date: Date, val time: Time?, val repeater1: RepeaterOrDelay?, val repeater2: RepeaterOrDelay?) : Timestamp() {
        override fun toOrg(): String {
            val time = if (time != null) " ${time.time.hour}:${time.time.minute} " else ""
            val repeater1 = if (repeater1 != null) repeater1.toOrg() else ""
            val repeater2 = if (repeater2 != null) repeater2.toOrg() else ""
            return "<${date.date.year}-${date.date.monthValue}-${date.date.dayOfMonth} ${date.date.dayOfWeek.toOrg()}$time$repeater1$repeater2>"
        }


        override fun toString(): String {
            return "Active(date=$date, time=$time, repeater1=$repeater1, repeater2=$repeater2)"
        }

        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Active

            if (date != other.date) return false
            if (time != other.time) return false
            if (repeater1 != other.repeater1) return false
            if (repeater2 != other.repeater2) return false

            return true
        }

        override fun hashCode(): Int{
            var result = date.hashCode()
            result = 31 * result + (time?.hashCode() ?: 0)
            result = 31 * result + (repeater1?.hashCode() ?: 0)
            result = 31 * result + (repeater2?.hashCode() ?: 0)
            return result
        }
    }

    class Inactive(val date: Date, val time: Time?, val repeater1: RepeaterOrDelay?, val repeater2: RepeaterOrDelay?) : Timestamp() {
        override fun toOrg(): String {
            val time = if (time != null) " ${time.time.hour}:${time.time.minute} " else ""
            val repeater1 = if (repeater1 != null) repeater1.toOrg() else ""
            val repeater2 = if (repeater2 != null) repeater2.toOrg() else ""
            return "[${date.date.year}-${date.date.monthValue}-${date.date.dayOfMonth} ${date.date.dayOfWeek.toOrg()}$time$repeater1$repeater2]"
        }

        override fun toString(): String {
            return "Inactive(date=$date, time=$time, repeater1=$repeater1, repeater2=$repeater2)"
        }

        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Inactive

            if (date != other.date) return false
            if (time != other.time) return false
            if (repeater1 != other.repeater1) return false
            if (repeater2 != other.repeater2) return false

            return true
        }

        override fun hashCode(): Int{
            var result = date.hashCode()
            result = 31 * result + (time?.hashCode() ?: 0)
            result = 31 * result + (repeater1?.hashCode() ?: 0)
            result = 31 * result + (repeater2?.hashCode() ?: 0)
            return result
        }
    }

    class ActiveRange(val from: Active, val to: Active) : Timestamp() {
        override fun toOrg(): String {
            return "${from.toOrg()}--${to.toOrg()}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as ActiveRange

            if (from != other.from) return false
            if (to != other.to) return false

            return true
        }

        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + to.hashCode()
            return result
        }

        override fun toString(): String {
            return "ActiveRange(from=$from, to=$to)"
        }
    }

    class InactiveRange(val from: Inactive, val to: Inactive) : Timestamp() {
        override fun toOrg(): String {
            return "${from.toOrg()}--${to.toOrg()}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as InactiveRange

            if (from != other.from) return false
            if (to != other.to) return false

            return true
        }

        override fun hashCode(): Int {
            var result = from.hashCode()
            result = 31 * result + to.hashCode()
            return result
        }

        override fun toString(): String {
            return "InactiveRange(from=$from, to=$to)"
        }
    }

    abstract fun toOrg(): String
}

fun DayOfWeek.toOrg(): String {
    return when(this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}

data class Duration(val hours: Int, val minutes: Int)

data class Clock(val timestamp: Timestamp, val duration: Duration)

enum class PlanningKeyword {
    DEADLINE,
    SCHEDULED,
    CLOSED
}

data class Planning(val keyword: PlanningKeyword, val timestamp: Timestamp) {
    fun toOrg(): String {
        return "$keyword: ${timestamp.toOrg()}"
    }
}

data class PlanningLine(val plannings: List<Planning>) {
    fun toOrg(): String {
        return plannings.fold("", { prev, planning -> "$prev ${planning.toOrg()}" })
    }
}

data class BabelCall(val value: String?)

enum class BulletType {
    ASTERISK,
    HYPHEN,
    PLUS,
    COUNTER_DOT,
    COUNTER_PAREN
}

data class Bullet(val type: BulletType, val counter: Counter?)

fun asterisk() = Bullet(type = BulletType.ASTERISK, counter = null)
fun hyphen() = Bullet(type = BulletType.HYPHEN, counter = null)
fun plus() = Bullet(type = BulletType.PLUS, counter = null)

data class Counter(val numberValue: Int?, val charValue: Char?)

enum class CheckboxType {
    EMPTY,
    HALF,
    FULL
}

data class ListItem(val indentation: Int, val bulletType: Bullet, val counterSet: Counter?, val checkbox: CheckboxType?, val tag: String?, val content: String)

data class TableRow(val indentation: Int, val columns: List<String>?)

data class Table(val rows: List<TableRow>, val formulas: List<String>)

data class Keyword(val key: String, val value: String)

data class LatexEnvironment(val name: String, val content: String)

data class ExportSnippet(val name: String, val value: String)

data class Paragraph(val content: String) {
    fun toOrg() = content
}

data class DocumentElement(
        val paragraph: Paragraph? = null,
        val planningLine: PlanningLine? = null,
        val horizontalRule: String? = null,
        val headline: Headline? = null) {
    fun toOrg(): String {
        if (paragraph != null)
            return paragraph.toOrg()
        if (headline != null)
            return headline.toOrg()
        if (planningLine != null)
            return planningLine.toOrg()
        if (horizontalRule != null)
            return "$horizontalRule"
        return ""
    }
}

data class Document(val elements: List<DocumentElement>) {
    fun toOrg() = elements.fold("", { prev, element -> "$prev\n${element.toOrg()}" })
}
