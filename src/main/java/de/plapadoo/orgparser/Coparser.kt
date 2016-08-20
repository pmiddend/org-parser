package de.plapadoo.orgparser

import java.time.DayOfWeek
import java.time.format.DateTimeFormatter

/**
 * Contains functions to convert the structure back to an org document.
 *
 * Created by philipp on 8/20/16.
 */
fun toOrg(doc: Headline): String {
    return "*".repeat(doc.level) + (if (doc.keyword != null) " ${doc.keyword}" else "") + (if (doc.priority != null) "#[${doc.priority}]" else "") + " ${doc.title}" + (if (doc.tags.isNotEmpty()) "\t" + doc.tags.fold(":", { prev, tag -> "$prev$tag:" }) else "")
}

fun toOrg(doc: RepeaterOrDelay): String {
    val mark = when (doc.mark) {
        Repeater.CUMULATE -> "+"
        Repeater.CATCH_UP -> "++"
        Repeater.RESTART -> ".+"
        Repeater.ALL -> "-"
        Repeater.FIRST -> "--"
    }
    val unit = when (doc.unit) {
        RepeaterUnit.HOUR -> "h"
        RepeaterUnit.DAY -> "d"
        RepeaterUnit.WEEK -> "w"
        RepeaterUnit.MONTH -> "m"
        RepeaterUnit.YEAR -> "y"
    }

    return mark + doc.value + unit
}

fun toOrg(doc: Timestamp.Sexp): String {
    return "<%%${doc.descriptor}>"
}

fun toOrg(doc: Timestamp.Active): String {
    return "<" + toOrg(doc.time, doc.date, doc.repeater1, doc.repeater2) + ">"
}

fun toOrg(time: Time?, date: Date, repeater1: RepeaterOrDelay?, repeater2: RepeaterOrDelay?): String {
    val timeStr = if (time != null) " ${time.time.hour}:${time.time.minute} " else ""
    val repeater1Str = if (repeater1 != null) toOrg(repeater1) else ""
    val repeater2Str = if (repeater2 != null) toOrg(repeater2) else ""
    val dateStr = DateTimeFormatter.ISO_LOCAL_DATE.format(date.date)
    return "$dateStr ${toOrg(date.date.dayOfWeek)}$timeStr$repeater1Str$repeater2Str"
}

fun toOrg(doc: Timestamp.Inactive): String {
    return "[" + toOrg(doc.time, doc.date, doc.repeater1, doc.repeater2) + "]"
}

fun toOrg(doc: Timestamp.ActiveRange): String {
    return "${toOrg(doc.from)}--${toOrg(doc.to)}"
}

fun toOrg(doc: Timestamp.InactiveRange): String {
    return "${toOrg(doc.from)}--${toOrg(doc.to)}"
}

fun toOrg(doc: Timestamp): String {
    return when (doc) {
        is Timestamp.Sexp -> toOrg(doc)
        is Timestamp.Active -> toOrg(doc)
        is Timestamp.Inactive -> toOrg(doc)
        is Timestamp.ActiveRange -> toOrg(doc)
        is Timestamp.InactiveRange -> toOrg(doc)
    }
}

fun toOrg(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}

fun toOrg(doc: Planning): String {
    return "${doc.keyword}: ${toOrg(doc.timestamp)}"
}

fun toOrg(doc: PlanningLine): String {
    return doc.plannings.map { toOrg(it) }.reduce({ prev, planning -> "$prev $planning" })
}

fun toOrg(doc: Paragraph) = doc.content

fun toOrg(doc: DocumentElement): String {
    if (doc.paragraph != null)
        return toOrg(doc.paragraph)
    if (doc.headline != null)
        return toOrg(doc.headline)
    if (doc.planningLine != null)
        return toOrg(doc.planningLine)
    if (doc.horizontalRule != null)
        return "${doc.horizontalRule}"
    if (doc.greaterBlock != null)
        return toOrg(doc.greaterBlock)
    if (doc.propertyDrawer != null)
        return toOrg(doc.propertyDrawer)
    if (doc.drawer != null)
        return toOrg(doc.drawer)
    if (doc.dynamicBlock != null)
        return toOrg(doc.dynamicBlock)
    if (doc.footnote != null)
        return toOrg(doc.footnote)
    if (doc.listItem != null)
        return toOrg(doc.listItem)
    return ""
}

fun toOrg(doc: Counter): String {
    if (doc.charValue == null && doc.numberValue == null)
        return ""
    else if (doc.charValue != null)
        return doc.charValue.toString()
    else
        return doc.numberValue.toString()
}

fun toOrg(doc: ListItem): String {
    val bullet = when (doc.bulletType.type) {
        BulletType.ASTERISK -> "*"
        BulletType.HYPHEN -> "-"
        BulletType.PLUS -> "+"
        BulletType.COUNTER_DOT ->
            (if (doc.bulletType.counter == null) "" else toOrg(doc.bulletType.counter))+"."
        BulletType.COUNTER_PAREN ->
            (if (doc.bulletType.counter == null) "" else toOrg(doc.bulletType.counter))+")"
    }
    val counterset = if(doc.counterSet != null) " [@${toOrg(doc.counterSet)}]" else ""
    val checkbox = if(doc.checkbox != null) " ${toOrg(doc.checkbox)}" else ""
    val tag = if(doc.tag != null) "${doc.tag} ::" else ""
    val indentation = " ".repeat(doc.indentation)
    return "$indentation$bullet$counterset$checkbox$tag ${doc.content}"
}

fun toOrg(doc: CheckboxType): String {
    val content = when(doc) {
        CheckboxType.EMPTY -> " "
        CheckboxType.HALF -> "-"
        CheckboxType.FULL -> "X"
    }
    return "[$content]"
}

fun toOrg(doc: Footnote): String {
    val label = doc.label.label
    return "[fn:$label]${doc.content}"
}

fun toOrg(doc: DynamicBlock): String {
    val params = if (doc.parameters != null) " ${doc.parameters}" else ""
    val contentLines = doc.content.joinToString(separator = "\n")
    return "#+BEGIN: ${doc.type}$params\n$contentLines\n#+END:"
}

fun toOrg(doc: Drawer): String {
    val contentLines = doc.content.joinToString(separator = "\n")
    return ":${doc.name}:\n$contentLines\n:END:"
}

fun toOrg(doc: PropertyDrawer): String {
    val properties = doc.properties.map { toOrg(it) }.joinToString(separator = "\n")
    return ":PROPERTIES:\n$properties\n:END:"
}

fun toOrg(doc: Property): String {
    return ":" + doc.name + (if (doc.plus) "+" else "") + ":" + (if (doc.value != null) " " + doc.value else "")
}

fun toOrg(doc: GreaterBlock): String {
    val documentLines = doc.content.joinToString(separator = "\n")
    return "#+BEGIN_${doc.type}\n$documentLines\n#+END_${doc.type}"
}

fun toOrg(doc: Document): String {
    return doc.elements.map { toOrg(it) }.reduce { prev, cur -> "$prev\n$cur" }
}
