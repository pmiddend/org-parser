package de.plapadoo.orgparser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by philipp on 8/7/16.
 */
class ParserKtTest {
    @Test
    fun `headline with just a title`() {
        val title = "foo"
        assertThat(headlineParser().parse("*** $title")).isEqualTo(Headline(level = 3,keyword = null,priority = null,title = title,tags = listOf()))
    }

    @Test
    fun `headline with a keyword`() {
        val title = "foo"
        val keyword = "TODO"
        assertThat(headlineParser().parse("*** $keyword $title")).isEqualTo(Headline(level = 3,keyword = keyword,priority = null,title = title,tags = listOf()))
    }

    @Test
    fun `headline with a priority`() {
        val title = "foo"
        val prio = 'A'
        assertThat(headlineParser().parse("*** [#$prio] $title")).isEqualTo(Headline(level = 3,keyword = null,priority = prio,title = title,tags = listOf()))
    }

    @Test
    fun `headline with single tag`() {
        val title = "foo"
        val tag = "first"
        assertThat(headlineParser().parse("*** $title :$tag:")).isEqualTo(Headline(level = 3,keyword = null,priority = null,title = "$title ",tags = listOf(tag)))
    }

    @Test
    fun `headline with multiple tags`() {
        val title = "foo"
        val tag1 = "first"
        val tag2 = "second"
        assertThat(headlineParser().parse("*** $title :$tag1:$tag2:")).isEqualTo(Headline(level = 3,keyword = null,priority = null,title = "$title ",tags = listOf(tag1,tag2)))
    }

    @Test
    fun `headline with everything`() {
        val title = "foo"
        val tag1 = "first"
        val tag2 = "second"
        val prio = 'A'
        val keyword = "TODO"
        assertThat(headlineParser().parse("*** $keyword [#$prio] $title :$tag1:$tag2:")).isEqualTo(Headline(level = 3,keyword = keyword,priority = prio,title = "$title ",tags = listOf(tag1,tag2)))
    }

    @Test
    fun `greater block without parameters and with single content line`() {
        val type = "foo"
        val contentline = "contentline"
        assertThat(greaterBlockParser().parse("#+BEGIN_$type\n$contentline\n#+END_$type")).isEqualTo(GreaterBlock(type = type,parameters = null,content = listOf(contentline)))
    }

    @Test
    fun `greater block with parameters and with single content line`() {
        val type = "foo"
        val contentline = "contentline"
        val parameters = "params foo bar"
        assertThat(greaterBlockParser().parse("#+BEGIN_$type $parameters\n$contentline\n#+END_$type")).isEqualTo(GreaterBlock(type = type,parameters = parameters,content = listOf(contentline)))
    }

    @Test
    fun `greater block a suspicious content line`() {
        val type = "foo"
        val contentline = "#+ENDmylife"
        assertThat(greaterBlockParser().parse("#+BEGIN_$type\n$contentline\n#+END_$type")).isEqualTo(GreaterBlock(type = type,parameters = null,content = listOf(contentline)))
    }

    @Test
    fun `dynamic block without parameters and with single content line`() {
        val type = "foo"
        val contentline = "contentline"
        assertThat(dynamicBlockParser().parse("#+BEGIN: $type\n$contentline\n#+END:")).isEqualTo(DynamicBlock(type = type,parameters = null,content = listOf(contentline)))
    }

    @Test
    fun `dynamic block with parameters and with single content line`() {
        val type = "foo"
        val contentline = "contentline"
        val parameters = "params foo bar"
        assertThat(dynamicBlockParser().parse("#+BEGIN: $type $parameters\n$contentline\n#+END:")).isEqualTo(DynamicBlock(type = type,parameters = parameters,content = listOf(contentline)))
    }

    @Test
    fun `dynamic block a suspicious content line`() {
        val type = "foo"
        val contentline = "#+ENDmylife"
        assertThat(dynamicBlockParser().parse("#+BEGIN: $type\n$contentline\n#+END:")).isEqualTo(DynamicBlock(type = type,parameters = null,content = listOf(contentline)))
    }

    @Test
    fun `drawer without content`() {
        val name = "name"
        val content = "\n"
        assertThat(drawerParser().parse(":$name:\n$content:END:")).isEqualTo(Drawer(name = name,content = content))
    }

    @Test
    fun `drawer with single content line`() {
        val name = "name"
        val content = "foo\n"
        assertThat(drawerParser().parse(":$name:\n$content:END:")).isEqualTo(Drawer(name = name,content = content))
    }

    @Test
    fun `drawer with two content lines`() {
        val name = "name"
        val content = "foo\nbar\n"
        assertThat(drawerParser().parse(":$name:\n$content:END:")).isEqualTo(Drawer(name = name,content = content))
    }

    @Test
    fun `property drawer without properties`() {
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:END:")).isEqualTo(PropertyDrawer(properties = listOf()))
    }

    @Test
    fun `property drawer with one name property`() {
        val name = "name"
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:$name:\n:END:")).isEqualTo(PropertyDrawer(properties = listOf(Property(name = name,value = null,plus = false))))
    }

    @Test
    fun `property drawer with one name plus property`() {
        val name = "name"
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:$name+:\n:END:")).isEqualTo(PropertyDrawer(properties = listOf(Property(name = name,value = null,plus = true))))
    }

    @Test
    fun `property drawer with one name property with value`() {
        val name = "name"
        val value = "value"
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:$name: $value\n:END:")).isEqualTo(PropertyDrawer(properties = listOf(Property(name = name,value = value,plus = false))))
    }

    @Test
    fun `property drawer with one name plus property with value`() {
        val name = "name"
        val value = "value"
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:$name+: $value\n:END:")).isEqualTo(PropertyDrawer(properties = listOf(Property(name = name,value = value,plus = true))))
    }

    @Test
    fun `property drawer with two name properties`() {
        val name = "name"
        val name2 = "name2"
        assertThat(propertyDrawerParser().parse(":PROPERTIES:\n:$name:\n:$name2:\n:END:")).isEqualTo(PropertyDrawer(properties = listOf(Property(name = name,value = null,plus = false),Property(name = name2,value = null,plus = false))))
    }

    @Test
    fun `single line numeric footnote`() {
        val label = "1"
        val content = "content"
        assertThat(footnoteParser().followedBy(stringParser("\n\n")).parse("[$label]$content\n\n")).isEqualTo(Footnote(label = FootnoteLabel(label = label,numeric = true),content = content))
    }

    @Test
    fun `single line non-numeric footnote`() {
        val label = "foo"
        val content = "content"
        assertThat(footnoteParser().followedBy(stringParser("\n\n")).parse("[fn:$label]$content\n\n")).isEqualTo(Footnote(label = FootnoteLabel(label = label,numeric = false),content = content))
    }

    @Test
    fun `multiline non-numeric footnote`() {
        val label = "foo"
        val content = "content\nsecondline"
        assertThat(footnoteParser().followedBy(stringParser("\n\n")).parse("[fn:$label]$content\n\n")).isEqualTo(Footnote(label = FootnoteLabel(label = label,numeric = false),content = content))
    }

    @Test
    fun `multiline non-numeric footnote ended by section`() {
        val label = "foo"
        val epilogue = "\n* section"
        assertThat(footnoteParser().followedBy(stringParser(epilogue)).parse("[fn:$label]$epilogue")).isEqualTo(Footnote(label = FootnoteLabel(label = label,numeric = false),content = ""))
    }

    @Test
    fun `multiline non-numeric footnote ended by next footnote`() {
        val label = "foo"
        val epilogue = "\n[fn:1] foo"
        assertThat(footnoteParser().followedBy(stringParser(epilogue)).parse("[fn:$label]$epilogue")).isEqualTo(Footnote(label = FootnoteLabel(label = label,numeric = false),content = ""))
    }

    @Test
    fun `affiliated keyword caption`() {
        val key = "CAPTION"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = null,value = value))
    }

    @Test
    fun `affiliated keyword header`() {
        val key = "HEADER"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = null,value = value))
    }

    @Test
    fun `affiliated keyword name`() {
        val key = "NAME"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = null,value = value))
    }

    @Test
    fun `affiliated keyword plot`() {
        val key = "PLOT"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = null,value = value))
    }

    @Test
    fun `affiliated keyword attr`() {
        val key = "ATTR_LaTeX"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = null,value = value))
    }

    @Test
    fun `affiliated keyword caption with optional`() {
        val key = "CAPTION"
        val optional = "FOO"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key[$optional]: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = optional,value = value))
    }

    @Test
    fun `affiliated keyword results with optional`() {
        val key = "RESULTS"
        val optional = "FOO"
        val value = "value"
        assertThat(affiliatedKeywordParser().parse("#+$key[$optional]: $value")).isEqualTo(AffiliatedKeyword(key = key,optional = optional,value = value))
    }

    @Test
    fun `sexp`() {
        val sexpcontent = "(lol (foobar))"
        assertThat(sexpParser().parse("$sexpcontent")).isEqualTo(Timestamp.Sexp(descriptor = sexpcontent))
    }

    @Test
    fun `sexpTimestamp`() {
        val sexpcontent = "(lol (foobar))"
        assertThat(timestampParser().parse("<%%$sexpcontent>")).isEqualTo(Timestamp.Sexp(descriptor = sexpcontent))
    }

    @Test
    fun `active timestamp without repeater or delay`() {
        val content = "<2016-08-13 Sat 9:52>"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = null
        val repeater2 = null
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Active(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `catch up repeater hour`() {
        assertThat(repeaterOrDelayParser().parse("++3h")).isEqualTo(RepeaterOrDelay(mark = Repeater.CATCH_UP,value=3,unit = RepeaterUnit.HOUR))
    }

    @Test
    fun `catch up repeater day`() {
        assertThat(repeaterOrDelayParser().parse("++3d")).isEqualTo(RepeaterOrDelay(mark = Repeater.CATCH_UP,value=3,unit = RepeaterUnit.DAY))
    }

    @Test
    fun `catch up repeater week`() {
        assertThat(repeaterOrDelayParser().parse("++3w")).isEqualTo(RepeaterOrDelay(mark = Repeater.CATCH_UP,value=3,unit = RepeaterUnit.WEEK))
    }

    @Test
    fun `catch up repeater month`() {
        assertThat(repeaterOrDelayParser().parse("++3m")).isEqualTo(RepeaterOrDelay(mark = Repeater.CATCH_UP,value=3,unit = RepeaterUnit.MONTH))
    }

    @Test
    fun `catch up repeater year`() {
        assertThat(repeaterOrDelayParser().parse("++3y")).isEqualTo(RepeaterOrDelay(mark = Repeater.CATCH_UP,value=3,unit = RepeaterUnit.YEAR))
    }

    @Test
    fun `cumulate repeater year`() {
        assertThat(repeaterOrDelayParser().parse("+3y")).isEqualTo(RepeaterOrDelay(mark = Repeater.CUMULATE,value=3,unit = RepeaterUnit.YEAR))
    }

    @Test
    fun `restart repeater year`() {
        assertThat(repeaterOrDelayParser().parse(".+3y")).isEqualTo(RepeaterOrDelay(mark = Repeater.RESTART,value=3,unit = RepeaterUnit.YEAR))
    }

    @Test
    fun `all delay year`() {
        assertThat(repeaterOrDelayParser().parse("--3y")).isEqualTo(RepeaterOrDelay(mark = Repeater.ALL,value=3,unit = RepeaterUnit.YEAR))
    }

    @Test
    fun `first delay year`() {
        assertThat(repeaterOrDelayParser().parse("-3y")).isEqualTo(RepeaterOrDelay(mark = Repeater.FIRST,value=3,unit = RepeaterUnit.YEAR))
    }

    @Test
    fun `inactive timestamp without repeater or delay`() {
        val content = "[2016-08-13 Sat 9:52]"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = null
        val repeater2 = null
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Inactive(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `active timestamp with catch up repeater`() {
        val content = "<2016-08-13 Sat 9:52 ++3h>"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = null
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Active(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `inactive timestamp with catch up repeater`() {
        val content = "[2016-08-13 Sat 9:52 ++3h]"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = null
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Inactive(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `active timestamp with catch up repeater and all delay`() {
        val content = "<2016-08-13 Sat 9:52 ++3h --3h>"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Active(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `inactive timestamp with catch up repeater and all delay`() {
        val content = "[2016-08-13 Sat 9:52 ++3h --3h]"
        val date = Date(LocalDate.of(2016,8,13))
        val time = Time(LocalTime.of(9,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.Inactive(date = date,time = time, repeater1 = repeater1,repeater2 = repeater2))
    }

    @Test
    fun `active time range with catch up repeater and all delay`() {
        val content = "<2016-08-13 Sat 9:52-10:52 ++3h --3h>"
        val date = Date(LocalDate.of(2016,8,13))
        val time1 = Time(LocalTime.of(9,52))
        val time2 = Time(LocalTime.of(10,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.ActiveRange(Timestamp.Active(date = date,time = time1, repeater1 = repeater1,repeater2 = repeater2),Timestamp.Active(date = date,time = time2, repeater1 = repeater1,repeater2 = repeater2)))
    }

    @Test
    fun `inactive time range with catch up repeater and all delay`() {
        val content = "[2016-08-13 Sat 9:52-10:52 ++3h --3h]"
        val date = Date(LocalDate.of(2016,8,13))
        val time1 = Time(LocalTime.of(9,52))
        val time2 = Time(LocalTime.of(10,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.InactiveRange(Timestamp.Inactive(date = date,time = time1, repeater1 = repeater1,repeater2 = repeater2),Timestamp.Inactive(date = date,time = time2, repeater1 = repeater1,repeater2 = repeater2)))
    }

    @Test
    fun `inactive total range with catch up repeater and all delay`() {
        val content = "[2016-08-13 Sat 9:52 ++3h --3h]--[2016-08-13 Sat 10:52 ++3h --3h]"
        val date = Date(LocalDate.of(2016,8,13))
        val time1 = Time(LocalTime.of(9,52))
        val time2 = Time(LocalTime.of(10,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.InactiveRange(Timestamp.Inactive(date = date,time = time1, repeater1 = repeater1,repeater2 = repeater2),Timestamp.Inactive(date = date,time = time2, repeater1 = repeater1,repeater2 = repeater2)))
    }

    @Test
    fun `active total range with catch up repeater and all delay`() {
        val content = "<2016-08-13 Sat 9:52 ++3h --3h>--<2016-08-13 Sat 10:52 ++3h --3h>"
        val date = Date(LocalDate.of(2016,8,13))
        val time1 = Time(LocalTime.of(9,52))
        val time2 = Time(LocalTime.of(10,52))
        val repeater1 = RepeaterOrDelay(mark = Repeater.CATCH_UP,value = 3,unit = RepeaterUnit.HOUR)
        val repeater2 = RepeaterOrDelay(mark = Repeater.ALL,value = 3,unit = RepeaterUnit.HOUR)
        assertThat(timestampParser().parse("$content")).isEqualTo(Timestamp.ActiveRange(Timestamp.Active(date = date,time = time1, repeater1 = repeater1,repeater2 = repeater2),Timestamp.Active(date = date,time = time2, repeater1 = repeater1,repeater2 = repeater2)))
    }
}