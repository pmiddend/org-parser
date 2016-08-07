package de.plapadoo.orgparser

import org.codehaus.jparsec.Parser
import org.codehaus.jparsec.Parsers
import org.codehaus.jparsec.pattern.Patterns

/**
 * Created by philipp on 8/7/16.
 */

fun stringParser(s : String): Parser<String> = Patterns.string(s).toScanner(s).source()!!

operator fun <T> Parser<T>.plus(o : Parser<T>): Parser<T> = Parsers.sequence(this,o)!!

operator fun <T> Parser<T>.times(o : Parser<T>): Parser<T> = Parsers.or(this,o)!!

fun headlineParser() : Parser<Headline> {
    val headingStars = Patterns.isChar('*').many1().toScanner("heading stars").source().map { s -> s.length }
    val keyword = stringParser("TODO") * stringParser("DONE")
    val priorityValue = Patterns.regex("[A-Z]").toScanner("priority").source().map { s -> s[0] }
    val priorityBegin = Patterns.string("[#").toScanner("'[#'")
    val priorityEnd = Patterns.isChar(']').toScanner("']'")
    val priority = Parsers.between(priorityBegin,priorityValue, priorityEnd)
    val title = Patterns.regex("[^:]+").toScanner("title").source()
    val tag = Patterns.regex("[^:]+").toScanner("tag").source()
    val tagDelimiter = Patterns.isChar(':').toScanner("':'")
    val tagList = tag.followedBy(tagDelimiter).many()
    val tags: Parser<MutableList<String>> = tagDelimiter.next(tagList)
    val ws = Patterns.regex("[ \t]*").toScanner("white space")
    return Parsers.sequence(
            headingStars.followedBy(ws),
            keyword.followedBy(ws).optional(),
            priority.followedBy(ws).optional(),
            title.followedBy(ws),
            tags.optional(mutableListOf()),
            { a, b, c, d, e -> Headline(a, b, c, d, e) })
}

