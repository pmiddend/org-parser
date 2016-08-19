package org.codehaus.jparsec

/*class FlatMapParser<T,U>(
        private val p: Parser<T>,
        private val f: (T) -> Parser<U>) : Parser<U>() {
    internal override fun apply(ctxt: ParseContext): Boolean {
        val r1 = p.run(ctxt)
        if (!r1) return false
        val o1 = p.getReturn(ctxt)
        val up = f(o1)
        val r2 = up.run(ctxt)
        if (!r2) return false
        val o2 = up.getReturn(ctxt)
        ctxt.result = o2
        return true
    }

    override fun toString(): String {
        return f.toString()
    }
}

fun <T,U>flatMap(p: Parser<T>,f: (T) -> Parser<U>) = FlatMapParser(p,f)*/
