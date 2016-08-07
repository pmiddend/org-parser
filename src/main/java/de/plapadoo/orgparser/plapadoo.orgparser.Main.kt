package de.plapadoo.orgparser

/**
 * Created by philipp on 8/7/16.
 */
fun main(args : Array<String>) {
    println("enter: ")
    val line = readLine()
    println("parsed: ${headlineParser().parse(line)}")
}