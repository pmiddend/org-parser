package de.plapadoo.orgparser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Created by philipp on 8/7/16.
 */
class DocumentParserTest {
    @Test
    fun `document with all elements combined`() {
        val document = "* headline\n" +
                "SCHEDULED: <2016-08-16 Tue 15:32 ++3h> DEADLINE: [2016-08-16 Tue 15:32 ++4h]\n" +
                ":PROPERTIES:\n" +
                ":LAST_REPEAT: [2016-08-17 Wed 22:26]\n" +
                ":WITHOUT_VALUE:\n" +
                ":WITH_PLUS+:\n" +
                ":END:\n" +
                ":SOMEBLOCK:\n" +
                "somecontents\n" +
                ":END:\n" +
                "#+BEGIN: name params\n" +
                "contents\n" +
                "#+END:\n" +
                "paragraph\n" +
                "[fn:haha] a wild footnote appears\n" +
                "\n" +
                "------\n" +
                "- a list item\n" +
                "  a) [ ]a list item\n" +
                "    1. tag :: a list item\n" +
                "\n" +
                "#+BEGIN_QUOTE\n" +
                "lol\n" +
                "#+END_QUOTE\n" +
                ""

        val doc = documentParser().parse(document)
        assertThat(toOrg(doc)).isEqualTo(document)
    }

    @Test
    fun `document with table`() {
        val document = "* headline\n" +
                "|---+-----|\n" +
                "| a | b c |\n" +
                "|---+-----|\n" +
                "done\n"

        val doc = documentParser().parse(document)
        assertThat(toOrg(doc)).isEqualTo(document)
    }
}