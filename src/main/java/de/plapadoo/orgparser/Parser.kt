package de.plapadoo.orgparser

import org.codehaus.jparsec.Parser
import org.codehaus.jparsec.Parsers
import org.codehaus.jparsec.pattern.Patterns
import org.codehaus.jparsec.sequence6
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalTime
import java.util.regex.Pattern

/**
 * Created by philipp on 8/7/16.
 */
// General parsers
val nl: Parser<String> = Patterns.isChar('\n').toScanner("NEWLINE").source()!!

fun stringParser(s: String): Parser<String> = Patterns.string(s).toScanner("'$s'").source()!!

fun regexParser(@Language("RegExp") pattern: String, description: String): Parser<String> = Patterns.regex(Pattern.compile(pattern,Pattern.DOTALL)).toScanner(description).source()!!

fun charParser(c: Char): Parser<String> = Patterns.isChar(c).toScanner(if (c == '\n') "NEWLINE" else "'$c'").source()!!

operator fun <T> Parser<T>.plus(o: Parser<T>): Parser<T> = Parsers.sequence(this, o)!!

operator fun <T> Parser<T>.times(o: Parser<T>): Parser<T> = Parsers.or(this, o)!!

fun headlineParser(): Parser<Headline> {
    val headingStars = Patterns.isChar('*').many1().toScanner("heading stars").source().map { s -> s.length }
    val keyword = stringParser("TODO") * stringParser("DONE")
    val priorityValue = regexParser("[A-Z]", "priority").map { s -> s[0] }
    val priorityBegin = stringParser("[#")
    val priorityEnd = charParser(']')
    val priority = Parsers.between(priorityBegin, priorityValue, priorityEnd)
    val title = regexParser("[^:\n]+", "title")
    val tag = regexParser("[^:\n]+", "tag")
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
    val name = regexParser(":[^:]+:", "doc begin").map({ it.substring(1, it.length - 1) })
    val content = regexParser("[^:][^\\n]*|:(?!END:)[^\\n]*","doc line")
    val end = stringParser(":END:")
    return Parsers.sequence(name.followedBy(nl), content.sepEndBy(charParser('\n')), end, { name, content, end -> Drawer(name, content) })
}

fun propertyDrawerParser(): Parser<PropertyDrawer> {
    val begin = stringParser(":PROPERTIES:")
    val nameLineRaw = regexParser(":([^E][^:]*|E(?!ND)[^:]*):", "property doc name line").map { it.substring(1, it.length - 1) }
    val namePlusLineRaw = regexParser(":([^E][^:]*|E(?!ND)[^:]*)\\+:", "property doc name plus line").map { it.substring(1, it.length - 2) }
    val ws = regexParser("[ \\t]*", "white space")
    val value = regexParser("[^\\n]+", "property doc value")
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

fun babelCallParser(): Parser<BabelCall> {
    val begin = stringParser("#+CALL: ")
    return Parsers.sequence(
            begin,
            regexParser("[^\\n]+", "babel call value").optional(),
            { begin, value -> BabelCall(value) })
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

// This does not actually parse sexp, just correctly matched pairs.
fun sexpParser(): Parser<Timestamp.Sexp> {
    val openBrace = charParser('(')
    val closingBrace = charParser(')')
    val exceptBrace = regexParser("[^()]+", "sexp non-brace chars")
    val inner = Parser.newReference<String>()
    val sexp = Parsers.sequence(
            openBrace,
            inner.lazy(),
            closingBrace,
            { open, inner, close -> open + inner + close })
    inner.set(Parsers.or(exceptBrace, sexp).many().map { strings -> strings.joinToString(separator = "") })
    return sexp.map { Timestamp.Sexp(descriptor = it) }
}

fun repeaterOrDelayParser(): Parser<RepeaterOrDelay> {
    val mark = Parsers.or(
            stringParser("++").map { Repeater.CATCH_UP },
            charParser('+').map { Repeater.CUMULATE },
            stringParser(".+").map { Repeater.RESTART },
            stringParser("--").map { Repeater.ALL },
            charParser('-').map { Repeater.FIRST })
    val value = regexParser("[0-9]+", "value").map { it.toInt() }
    val unit = Parsers.or(
            charParser('h').map { RepeaterUnit.HOUR },
            charParser('d').map { RepeaterUnit.DAY },
            charParser('w').map { RepeaterUnit.WEEK },
            charParser('m').map { RepeaterUnit.MONTH },
            charParser('y').map { RepeaterUnit.YEAR })
    return Parsers.sequence(
            mark,
            value,
            unit,
            { mark, value, unit -> RepeaterOrDelay(mark, value, unit) })
}

fun timestampParser(): Parser<Timestamp> {
    val sexp = stringParser("<%%").next(sexpParser()).followedBy(charParser('>'))
    val space = charParser(' ')
    val dash = charParser('-')
    val date =
            Parsers.sequence(
                    regexParser("[0-9]{4}", "date, year").followedBy(dash),
                    regexParser("[0-9]{1,2}", "date, month").followedBy(dash),
                    regexParser("[0-9]{1,2}", "date, day").followedBy(space),
                    regexParser("[^\\]+-> \t]+", "day name"),
                    { year, month, day, dayname -> Date(LocalDate.of(year.toInt(), month.toInt(), day.toInt())) })
    val time =
            Parsers.sequence(
                    regexParser("[0-9]{1,2}", "date, hour").followedBy(charParser(':')),
                    regexParser("[0-9]{1,2}", "date, minute"),
                    { hour, minute -> Time(LocalTime.of(hour.toInt(), minute.toInt())) })
    val activePrefix = charParser('<')
    val activeSuffix = charParser('>')
    val inactivePrefix = charParser('[')
    val inactiveSuffix = charParser(']')

    // This is an ugly helper type to get around the jparsec "restriction" of only sequencing
    // up to 5 parsers
    data class TimeRange(val from: Time, val to: Time)

    val repeaterOrDelay = repeaterOrDelayParser()
    val active =
            Parsers.sequence(
                    activePrefix.next(date.followedBy(space)),
                    time.optional(),
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    activeSuffix,
                    { date, time, repeater1, repeater2, suffix -> Timestamp.Active(date, time, repeater1, repeater2) })
    val inactive =
            Parsers.sequence(
                    inactivePrefix.next(date.followedBy(space)),
                    time.optional(),
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    inactiveSuffix,
                    { date, time, repeater1, repeater2, suffix -> Timestamp.Inactive(date, time, repeater1, repeater2) })

    val inactiveTimeRange =
            Parsers.sequence(
                    inactivePrefix.next(date.followedBy(space)),
                    Parsers.sequence(
                            time.followedBy(dash),
                            time,
                            { from, to -> TimeRange(from, to) }
                    ),
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    inactiveSuffix,
                    { date, timeRange, repeater1, repeater2, suffix -> Timestamp.InactiveRange(Timestamp.Inactive(date, timeRange.from, repeater1, repeater2), Timestamp.Inactive(date, timeRange.to, repeater1, repeater2)) })
    val activeTimeRange =
            Parsers.sequence(
                    activePrefix.next(date.followedBy(space)),
                    Parsers.sequence(
                            time.followedBy(dash),
                            time,
                            { from, to -> TimeRange(from, to) }
                    ),
                    space.next(repeaterOrDelay).optional(),
                    space.next(repeaterOrDelay).optional(),
                    activeSuffix,
                    { date, timeRange, repeater1, repeater2, suffix -> Timestamp.ActiveRange(Timestamp.Active(date, timeRange.from, repeater1, repeater2), Timestamp.Active(date, timeRange.to, repeater1, repeater2)) })
    val activeTotalRange =
            Parsers.sequence(
                    active.followedBy(stringParser("--")),
                    active,
                    { from, to -> Timestamp.ActiveRange(from, to) })
    val inactiveTotalRange =
            Parsers.sequence(
                    inactive.followedBy(stringParser("--")),
                    inactive,
                    { from, to -> Timestamp.InactiveRange(from, to) })
    return Parsers.or(
            sexp,
            inactiveTimeRange,
            activeTimeRange,
            inactiveTotalRange,
            activeTotalRange,
            inactive,
            active)
}

fun durationParser(): Parser<Duration> {
    return Parsers.sequence(regexParser("[0-9]{1,2}", "duration hours").followedBy(charParser(':')), regexParser("[0-9]{2}", "duration minutes"), { hours, minutes -> Duration(hours.toInt(), minutes.toInt()) })
}

fun clockParser(): Parser<Clock> {
    return Parsers.sequence(
            stringParser("CLOCK: ").next(timestampParser()).followedBy(charParser(' ')),
            durationParser(),
            { timestamp, duration -> Clock(timestamp, duration) })
}

fun planningParser(): Parser<Planning> {
    return Parsers.sequence(
            Parsers.or(
                    stringParser("DEADLINE").map { PlanningKeyword.DEADLINE },
                    stringParser("SCHEDULED").map { PlanningKeyword.SCHEDULED },
                    stringParser("CLOSED").map { PlanningKeyword.CLOSED }).followedBy(stringParser(": ")),
            timestampParser(),
            { keyword, timestamp -> Planning(keyword, timestamp) })
}

fun planningLineParser(): Parser<PlanningLine> {
    return planningParser().sepBy1(regexParser("[\\t ]+", "planning line separator")).map { PlanningLine(it) }
}

fun listItemParser(): Parser<ListItem> {
    val counter = Parsers.or(
            regexParser("[0-9]+", "list numeric counter").map { it.toInt() }.map { Counter(numberValue = it, charValue = null) },
            regexParser("[a-zA-Z]", "list alphabetic counter").map { it[0] }.map { Counter(numberValue = null, charValue = it) })
    val bullet = Parsers.or(
            // This conflicts with headline definitions (no idea how to parse this correctly)
//            charParser('*').map { asterisk() },
            charParser('-').map { hyphen() },
            charParser('+').map { plus() },
            counter.followedBy(charParser('.')).map { Bullet(type = BulletType.COUNTER_DOT, counter = it) },
            counter.followedBy(charParser(')')).map { Bullet(type = BulletType.COUNTER_PAREN, counter = it) })
    val counterSet = Parsers.between(stringParser("[@"), counter, charParser(']'))
    val indentation = regexParser("[ \\t]*", "indentation").map { it.length }
    val checkboxContent = Parsers.or(
            charParser(' ').map { CheckboxType.EMPTY },
            charParser('-').map { CheckboxType.HALF },
            charParser('X').map { CheckboxType.FULL })
    val checkbox = Parsers.between(charParser('['), checkboxContent, charParser(']'))
    val tagText = regexParser(".*? ::[ \\t]*", "tag text").map { it.substring(0, it.indexOf("::") - 1) }
    val content = regexParser("[^\\n]*", "list item content")
    val ws = regexParser("[ \\t]*", "white space")
    return sequence6(
            indentation,
            bullet.followedBy(charParser(' ')),
            counterSet.followedBy(ws).optional(),
            checkbox.followedBy(ws).optional(),
            tagText.optional(),
            content,
            { indentation, bullet, counterset, checkbox, tagtext, content -> ListItem(indentation, bullet, counterset, checkbox, tagtext, content) })
}

fun tableRowParser(): Parser<TableRow> {
    val indentation = regexParser("[ \\t]*", "indentation").map { it.length }
    val tableCell = regexParser("[^\\n|]+", "table cell").map { it.trim() }
    return Parsers.sequence(
            indentation,
            Parsers.or(
                    regexParser("\\|-[^\\n]*", "table separator row").map { null },
                    charParser('|').next(tableCell.sepEndBy1(charParser('|'))).map { it }),
            { indentation, columns -> TableRow(indentation, columns) })
}

fun tableFormulaParser(): Parser<String> {
    return regexParser("[ \\t]*", "indentation").next(stringParser("#+TBLFM: ").next(regexParser("[^\\n]*", "table formula")))
}

fun tableParser(): Parser<Table> {
    return Parsers.sequence(
            tableRowParser().sepBy1(charParser('\n')),
            tableFormulaParser().sepBy(charParser('\n')),
            { rows, formulas -> Table(rows, formulas) })
}

fun commentLineParser(): Parser<String> {
    return stringParser("# ").next(regexParser("[^\\n]*", "comment content"))
}

fun fixedWidthLineParser(): Parser<String> {
    return stringParser(": ").next(regexParser("[^\\n]*", "fixed width line content"))
}

fun horizontalRuleLineParser(): Parser<String> {
    return regexParser("-{5,}", "horizontal rule")
}

fun keywordParser(): Parser<Keyword> {
    return Parsers.sequence(
            stringParser("#+").next(regexParser("[^:]+", "keyword key").followedBy(stringParser(": "))),
            regexParser("[^\n]*", "keyword content"),
            { key, value -> Keyword(key, value) })
}

fun latexEnvironmentParser(): Parser<LatexEnvironment> {
    // This is very poorly executed.
    val begin = regexParser("[ \\t]*\\\\begin\\{","latex environment begin")
    val beginClose = regexParser("\\}\\s*\\n","latex environment begin close")
    val content = regexParser(".*?\\\\end\\{[^}]+\\}\\s*\\n","latex environment content")
    return Parsers.sequence(
            begin.next(regexParser("[^}]+","latex environment name")).followedBy(beginClose),
            content,
            {name,content -> LatexEnvironment(name,content.substring(0,content.indexOf("\\end{$name}")))}
    )
}

fun exportSnippetParser(): Parser<ExportSnippet> {
    return Parsers.sequence(
            stringParser("@@").next(regexParser("[^:]*","export snippet name")).followedBy(charParser(':')),
            regexParser("([^@]|@(?!@))*","export snippet value").followedBy(stringParser("@@")),
            {name,value -> ExportSnippet(name,value)}
    )
}

fun paragraphParser(): Parser<Paragraph> {
    return Parsers.or(regexParser("[^\n]+","paragraph").map { Paragraph(it) },Patterns.ALWAYS.toScanner("empty par").map { Paragraph("") })
}

fun documentElementParser(): Parser<DocumentElement> {
    return Parsers.or(
                headlineParser().map { DocumentElement(headline = it) }.label("headline"),
                planningLineParser().map { DocumentElement(planningLine = it) }.label("planning line"),
                horizontalRuleLineParser().map { DocumentElement(horizontalRule = it) }.label("horizontal rule"),
                greaterBlockParser().map { DocumentElement(greaterBlock = it) }.label("greater block"),
                propertyDrawerParser().map { DocumentElement(propertyDrawer = it) }.label("property doc"),
                drawerParser().map { DocumentElement(drawer = it) }.label("doc"),
                dynamicBlockParser().map { DocumentElement(dynamicBlock = it) }.label("dynamic block"),
                footnoteParser().map { DocumentElement(footnote = it) }.label("footnote"),
                listItemParser().map { DocumentElement(listItem = it) }.label("list item"),
                tableParser().map { DocumentElement(table = it) }.label("table"),
                paragraphParser().map { DocumentElement(paragraph = it) }.label("paragraph"))
}

fun documentParser(): Parser<Document> {
    return documentElementParser().sepEndBy(charParser('\n')).map { Document(it) }
}
