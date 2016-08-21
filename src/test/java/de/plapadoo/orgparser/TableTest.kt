package de.plapadoo.orgparser

import org.junit.Test

/**
 * Created by philipp on 8/21/16.
 */
class TableTest {
    @Test
    fun `simple table is kind of sanitized`() {
        val resultingTable = toOrg(Table(rows = listOf(TableRow(0, listOf("a b", "c")), TableRow(0, null)), formulas = listOf()))
        println(resultingTable)
    }
}
