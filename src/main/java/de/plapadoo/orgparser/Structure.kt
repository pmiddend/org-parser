package de.plapadoo.orgparser

/**
 * Created by philipp on 8/7/16.
 */
data class Headline(
        val level : Int,
        val keyword : String?,
        val priority : Char?,
        val title : String,
        val tags : List<String>)

