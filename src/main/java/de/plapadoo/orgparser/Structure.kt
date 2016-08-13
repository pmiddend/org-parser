package de.plapadoo.orgparser

import java.time.LocalDate
import java.time.LocalTime

/**
 * Created by philipp on 8/7/16.
 */
data class Headline(
        val level : Int,
        val keyword : String?,
        val priority : Char?,
        val title : String,
        val tags : List<String>)

data class GreaterBlock(
        val type : String,
        val parameters : String?,
        val content : List<String>)

data class DynamicBlock(
        val type : String,
        val parameters : String?,
        val content : List<String>)

data class Property(
        val name : String,
        val value : String?,
        val plus : Boolean)

data class PropertyDrawer(
        val properties : List<Property>)

data class Drawer(
        val name : String,
        val content : String)

data class FootnoteLabel(
        val label : String,
        val numeric : Boolean)

data class Footnote(
        val label : FootnoteLabel,
        val content : String)

data class AffiliatedKeyword(
        val key : String,
        val optional : String?,
        val value : String)

data class Date(
        val date : LocalDate)

data class Time(
        val time : LocalTime)

enum class Repeater {
    CUMULATE,
    CATCH_UP,
    RESTART,
    ALL,
    FIRST
}

enum class RepeaterUnit {
    HOUR,
    DAY,
    WEEK,
    MONTH,
    YEAR
}

data class RepeaterOrDelay(
        val mark : Repeater,
        val value : Int,
        val unit : RepeaterUnit)

sealed class Timestamp {
    class Sexp(val descriptor : String) : Timestamp()
    class Active(val date : Date,val time : Time,val repeater1 : RepeaterOrDelay?,val repeater2 : RepeaterOrDelay?)
    class Inactive(val date : Date,val time : Time,val repeater1 : RepeaterOrDelay?,val repeater2 : RepeaterOrDelay?)
    class ActiveRange(val from : Active,val to : Active)
    class InactiveRange(val from : Inactive,val to : Inactive)
}
