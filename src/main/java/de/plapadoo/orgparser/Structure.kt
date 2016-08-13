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
    class Sexp(val descriptor : String) : Timestamp() {
        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Sexp

            if (descriptor != other.descriptor) return false

            return true
        }

        override fun hashCode(): Int{
            return descriptor.hashCode()
        }

        override fun toString(): String{
            return "Sexp(descriptor='$descriptor')"
        }
    }

    class Active(val date : Date,val time : Time,val repeater1 : RepeaterOrDelay?,val repeater2 : RepeaterOrDelay?) : Timestamp() {
        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Active

            if (date != other.date) return false
            if (time != other.time) return false
            if (repeater1 != other.repeater1) return false
            if (repeater2 != other.repeater2) return false

            return true
        }

        override fun hashCode(): Int{
            var result = date.hashCode()
            result = 31 * result + time.hashCode()
            result = 31 * result + (repeater1?.hashCode() ?: 0)
            result = 31 * result + (repeater2?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String{
            return "Active(date=$date, time=$time, repeater1=$repeater1, repeater2=$repeater2)"
        }
    }

    class Inactive(val date : Date,val time : Time,val repeater1 : RepeaterOrDelay?,val repeater2 : RepeaterOrDelay?) : Timestamp() {
        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as Inactive

            if (date != other.date) return false
            if (time != other.time) return false
            if (repeater1 != other.repeater1) return false
            if (repeater2 != other.repeater2) return false

            return true
        }

        override fun hashCode(): Int{
            var result = date.hashCode()
            result = 31 * result + time.hashCode()
            result = 31 * result + (repeater1?.hashCode() ?: 0)
            result = 31 * result + (repeater2?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String{
            return "Inactive(date=$date, time=$time, repeater1=$repeater1, repeater2=$repeater2)"
        }
    }

    class ActiveRange(val from : Active,val to : Active) : Timestamp() {
        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as ActiveRange

            if (from != other.from) return false
            if (to != other.to) return false

            return true
        }

        override fun hashCode(): Int{
            var result = from.hashCode()
            result = 31 * result + to.hashCode()
            return result
        }

        override fun toString(): String{
            return "ActiveRange(from=$from, to=$to)"
        }
    }

    class InactiveRange(val from : Inactive,val to : Inactive) : Timestamp() {
        override fun equals(other: Any?): Boolean{
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as InactiveRange

            if (from != other.from) return false
            if (to != other.to) return false

            return true
        }

        override fun hashCode(): Int{
            var result = from.hashCode()
            result = 31 * result + to.hashCode()
            return result
        }

        override fun toString(): String{
            return "InactiveRange(from=$from, to=$to)"
        }
    }
}
