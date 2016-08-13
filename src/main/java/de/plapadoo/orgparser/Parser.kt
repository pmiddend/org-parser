package de.plapadoo.orgparser

import org.codehaus.jparsec.Parser
import org.codehaus.jparsec.Parsers
import org.codehaus.jparsec.pattern.Patterns
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by philipp on 8/7/16.
 */
// General parsers
val nl: Parser<String> = Patterns.isChar('\n').toScanner("NEWLINE").source()!!

fun stringParser(s: String): Parser<String> = Patterns.string(s).toScanner("'$s'").source()!!

fun regexParser(@Language("RegExp") pattern: String, description: String): Parser<String> = Patterns.regex(pattern).toScanner(description).source()!!

fun charParser(c: Char): Parser<String> = Patterns.isChar(c).toScanner("'$c'").source()!!

operator fun <T> Parser<T>.plus(o: Parser<T>): Parser<T> = Parsers.sequence(this, o)!!

operator fun <T> Parser<T>.times(o: Parser<T>): Parser<T> = Parsers.or(this, o)!!

fun headlineParser(): Parser<Headline> {
    val headingStars = Patterns.isChar('*').many1().toScanner("heading stars").source().map { s -> s.length }
    val keyword = stringParser("TODO") * stringParser("DONE")
    val priorityValue = regexParser("[A-Z]", "priority").map { s -> s[0] }
    val priorityBegin = stringParser("[#")
    val priorityEnd = charParser(']')
    val priority = Parsers.between(priorityBegin, priorityValue, priorityEnd)
    val title = regexParser("[^:]+", "title")
    val tag = regexParser("[^:]+", "tag")
    val tagDelimiter = charParser(':')
    val tagList = tag.followedBy(tagDelimiter).many()
    val tags: Parser<MutableList<String>> = tagDelimiter.next(tagList)
    val ws = regexParser("[ \t]*", "white space")
    return Parsers.sequence(
            headingStars.followedBy(ws),
            keyword.followedBy(ws).optional(),
            priority.followedBy(ws).optional(),
            title.followedBy(ws),
            tags.optional(mutableListOf()),
            { a, b, c, d, e -> Headline(a, b, c, d, e) })
}

fun greaterBlockParser(): Parser<GreaterBlock> {
    val begin = stringParser("#+BEGIN_")
    val type = regexParser("\\S+", "block type")
    val parameters = regexParser("[^\\n]+", "block parameters")
    val endLine = regexParser("#\\+END_[^\\n]+", "block end line")
    val contentLine = regexParser("[^#][^\\n]*|#(?!\\+END_)[^\\n]*", "block content line")
    val ws = regexParser("[ \\t]*", "white space")
    return Parsers.sequence(
            begin.next(type),
            ws.next(parameters).optional(),
            nl,
            contentLine.sepEndBy(nl),
            endLine,
            { type, parameters, newline, contentLines, endLine -> GreaterBlock(type, parameters, contentLines) })
}

fun dynamicBlockParser(): Parser<DynamicBlock> {
    val begin = stringParser("#+BEGIN: ")
    val type = regexParser("\\S+", "dynamic block type")
    val parameters = regexParser("[^\\n]+", "dynamic block parameters")
    val endLine = regexParser("#\\+END:", "dynamic block end line")
    val contentLine = regexParser("[^#][^\\n]*|#(?!\\+END:)[^\\n]*", "dynamic block content line")
    val ws = regexParser("[ \\t]*", "white space")
    return Parsers.sequence(
            begin.next(type),
            ws.next(parameters).optional(),
            nl,
            contentLine.sepEndBy(nl),
            endLine,
            { type, parameters, newline, contentLines, endLine -> DynamicBlock(type, parameters, contentLines) })
}

fun drawerParser(): Parser<Drawer> {
    val name = regexParser(":[^:]+:", "drawer begin").map({ it.substring(1, it.length - 1) })
    val content = regexParser("\n|(([^:][^\\n]*|:(?!END:)[^\\n]*)\n)*", "drawer line")
    val end = stringParser(":END:")
    return Parsers.sequence(name.followedBy(nl), content, end, { name, content, end -> Drawer(name, content) })
}

fun propertyDrawerParser(): Parser<PropertyDrawer> {
    val begin = stringParser(":PROPERTIES:")
    val nameLineRaw = regexParser(":([^E][^:]*|E(?!ND)[^:]*):", "property drawer name line").map { it.substring(1, it.length - 1) }
    val namePlusLineRaw = regexParser(":([^E][^:]*|E(?!ND)[^:]*)\\+:", "property drawer name plus line").map { it.substring(1, it.length - 2) }
    val ws = regexParser("[ \\t]*", "white space")
    val value = regexParser("[^\\n]+", "property drawer value")
    val nameValueLine = Parsers.sequence(nameLineRaw.followedBy(ws), value, { name, value -> Property(name = name, value = value, plus = false) })
    val namePlusValueLine = Parsers.sequence(namePlusLineRaw.followedBy(ws), value, { name, value -> Property(name = name, value = value, plus = true) })
    val end = stringParser(":END:")
    val line = Parsers.or(
            namePlusValueLine,
            nameValueLine,
            namePlusLineRaw.map { it -> Property(name = it, value = null, plus = true) },
            nameLineRaw.map { it -> Property(name = it, value = null, plus = false) })
    return Parsers.sequence(
            begin.followedBy(nl),
            line.sepEndBy(nl),
            end,
            { begin, lines, end -> PropertyDrawer(lines) })
}

fun footnoteParser(): Parser<Footnote> {
    val fnbegin = stringParser("[fn:")
    val fncontent = regexParser("[^\\]]+", "footnote string label")
    val begin = charParser('[')
    val fnnumber = regexParser("[0-9]+", "footnote numeric label")
    val end = charParser(']')
    val firstLineEnd = regexParser("[^\\n]*", "footnote first line")
    // In words:
    // - a newline, but not two adjacent newlines,
    // - a newline, but not followed by a star (section)
    // - a newline, but not followed by [ (a new footnote definition)
    val contentLine = regexParser("\\n(?![*\n\\[])[^\\n]*", "footnote content line")
    return Parsers.sequence(
            Parsers.or(
                    fnbegin.next(fncontent).map { FootnoteLabel(label = it, numeric = false) },
                    begin.next(fnnumber).map { FootnoteLabel(label = it, numeric = true) }).followedBy(end),
            Parsers.sequence(
                    firstLineEnd,
                    //                    contentLine,
                    contentLine.many().map { it.joinToString(separator = "") },
                    { firstLine, restLines -> firstLine + restLines }),
            { label, content -> Footnote(label, content) })
}

fun affiliatedKeywordParser(): Parser<AffiliatedKeyword> {
    val begin = stringParser("#+")
    val caption = stringParser("CAPTION")
    val header = stringParser("HEADER")
    val name = stringParser("NAME")
    val plot = stringParser("PLOT")
    val attr = regexParser("ATTR_[a-zA-Z0-9-_]+", "attribute affiliated keyword name")
    val results = stringParser("RESULTS")
    val optionalBegin = charParser('[')
    val optionalEnd = charParser(']')
    val middle = stringParser(": ")
    val value = regexParser("[^\n]*", "affiliated keyword value")
    val optional = regexParser("[^\\]]*", "affiliated keyword optional value")
    return Parsers.sequence(
            begin.next(
                    Parsers.or(
                            caption,
                            header,
                            name,
                            plot,
                            attr,
                            results
                    )
            ),
            optionalBegin.next(optional).followedBy(optionalEnd).optional(),
            middle.next(value),
            { key, optional, value -> AffiliatedKeyword(key, optional, value) })
}

fun timestampParser(): Parser<Timestamp> {
    val sexp = stringParser("<%%(").next(regexParser("[^)]*", "timestamp sexp")).followedBy(charParser('>')).map { Timestamp.Sexp(it) }
    val space = charParser(' ')
    val date =
            Parsers.sequence(
                    regexParser("[0-9]{4}", "date, year").followedBy(space),
                    regexParser("[0-9]{1,2}", "date, month").followedBy(space),
                    regexParser("[0-9]{1,2}", "date, day").followedBy(space),
                    regexParser("[^+-\\]>\\S]+", "dayname"),
                    { year, month, day, dayname -> Date(LocalDate.of(year.toInt(), month.toInt(), day.toInt())) })
    val time =
            Parsers.sequence(
                    regexParser("[0-9]{1,2}", "date, hour").followedBy(charParser(':')),
                    regexParser("[0-9]{1,2}", "date, minute"),
                    { hour, minute -> Time(LocalTime.of(hour.toInt(), minute.toInt())) })
    val mark = Parsers.or(
            stringParser("++").map { Repeater.CATCH_UP },
            charParser('+').map { Repeater.CUMULATE },
            stringParser(".+").map { Repeater.RESTART },
            stringParser("--").map { Repeater.ALL },
            charParser('-').map { Repeater.FIRST })
    val value = regexParser("[0-9]+","value").map { it.toInt() }
    val unit = Parsers.or(
            charParser('h').map { RepeaterUnit.HOUR },
            charParser('d').map { RepeaterUnit.DAY },
            charParser('w').map { RepeaterUnit.WEEK },
            charParser('m').map { RepeaterUnit.MONTH },
            charParser('y').map { RepeaterUnit.YEAR }
    )
    val repeaterOrDelay =
            Parsers.sequence(
                    mark,
                    value,
                    unit,
                    { mark, value, unit -> RepeaterOrDelay(mark, value, unit) }
            )
    val activePrefix = charParser('<')
    val activeSuffix = charParser('>')
    val inactivePrefix = charParser('[')
    val inactiveSuffix = charParser(']')
    val active =
            Parsers.sequence(
                    activePrefix.next(date.followedBy(space)),
                    time,
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    activeSuffix,
                    {date,time,repeater1,repeater2,suffix -> Timestamp.Active(date,time,repeater1,repeater2)})
    val inactive =
            Parsers.sequence(
                    inactivePrefix.next(date.followedBy(space)),
                    time,
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    inactiveSuffix,
                    {date,time,repeater1,repeater2,suffix -> Timestamp.Inactive(date,time,repeater1,repeater2)})
    val inactiveTimeRange =
            Parsers.sequence(
                    activePrefix.next(date.followedBy(space)),
                    time.followedBy(charParser('-')),
                    time.followedBy(charParser(' ')),
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    activeSuffix,
                    {date,time1,time2,repeater1,repeater2,suffix -> Timestamp.ActiveRange(Timestamp.Active(date,time1,repeater1,repeater2),Timestamp.Active(date,time2,repeater1,repeater2))})
    val activeTotalRange =
            Parsers.sequence(
                    active.followedBy(stringParser("--")),
                    active,
                    { from,to -> Timestamp.ActiveRange(from,to) })
    val inactiveTotalRange =
            Parsers.sequence(
                    inactive.followedBy(stringParser("--")),
                    inactive,
                    { from,to -> Timestamp.InactiveRange(from,to) })
    return Parsers.or(
            sexp,
            charParser('<'))
}
