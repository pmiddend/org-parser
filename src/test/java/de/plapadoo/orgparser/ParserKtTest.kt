package de.plapadoo.orgparser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Created by philipp on 8/7/16.
 */
class ParserKtTest {
    @Test
    fun `headline with just a title`() {
        val title = "foo"
        assertThat(headlineParser().parse("*** $title")).isEqualTo(Headline(3,null,null,title,listOf()))
    }

    @Test
    fun `headline with a keyword`() {
        val title = "foo"
        val keyword = "TODO"
        assertThat(headlineParser().parse("*** $keyword $title")).isEqualTo(Headline(3,keyword,null,title,listOf()))
    }

    @Test
    fun `headline with a priority`() {
        val title = "foo"
        val prio = 'A'
        assertThat(headlineParser().parse("*** [#$prio] $title")).isEqualTo(Headline(3,null,prio,title,listOf()))
    }

    @Test
    fun `headline with single tag`() {
        val title = "foo"
        val tag = "first"
        assertThat(headlineParser().parse("*** $title :$tag:")).isEqualTo(Headline(3,null,null,"$title ",listOf(tag)))
    }

    @Test
    fun `headline with multiple tags`() {
        val title = "foo"
        val tag1 = "first"
        val tag2 = "second"
        assertThat(headlineParser().parse("*** $title :$tag1:$tag2:")).isEqualTo(Headline(3,null,null,"$title ",listOf(tag1,tag2)))
    }

    @Test
    fun `headline with everything`() {
        val title = "foo"
        val tag1 = "first"
        val tag2 = "second"
        val prio = 'A'
        val keyword = "TODO"
        assertThat(headlineParser().parse("*** $keyword [#$prio] $title :$tag1:$tag2:")).isEqualTo(Headline(3,keyword,prio,"$title ",listOf(tag1,tag2)))
    }
}