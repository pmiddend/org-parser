package de.plapadoo.orgparser

import org.junit.Test

/**
 * Created by philipp on 8/7/16.
 */
class DocumentParserTest {
    @Test
    fun `document with all elements combined`() {
        val document = "* headline\n" +
                ":PROPERTIES:\n" +
                ":LAST_REPEAT: [2016-08-17 Wed 22:26]\n" +
                ":END:\n" +
                "paragraph\n" +
                "\n" +
                "#+BEGIN_QUOTE\n" +
                "lol\n" +
                "#+END_QUOTE\n" +
                ""
        val doc = documentParser().parse(document)
        println(doc.toOrg())
    }

    @Test
    fun `headline then planning line then paragraph`() {
        val document = "* headline\n" +
                "SCHEDULED: <2016-08-16 Thu 15:32 ++3h>\n" +
                "paragraph\n"
        val doc = documentParser().parse(document)
        println(doc.toOrg())
    }
}