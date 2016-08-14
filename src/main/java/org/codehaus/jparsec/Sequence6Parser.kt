/*****************************************************************************
 * Copyright (C) Codehaus.org                                                *
 * ------------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License");           *
 * you may not use this file except in compliance with the License.          *
 * You may obtain a copy of the License at                                   *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                                *
 * *
 * Unless required by applicable law or agreed to in writing, software       *
 * distributed under the License is distributed on an "AS IS" BASIS,         *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *
 * See the License for the specific language governing permissions and       *
 * limitations under the License.                                            *
 */
package org.codehaus.jparsec

// Helper class for jparsecs (type-safe) limit
class Sequence6Parser<A, B, C, D, E, F, T>(
        private val p1: Parser<A>,
        private val p2: Parser<B>,
        private val p3: Parser<C>,
        private val p4: Parser<D>,
        private val p5: Parser<E>,
        private val p6: Parser<F>,
        private val f: (A, B, C, D, E, F) -> T) : Parser<T>() {
    internal override fun apply(ctxt: ParseContext): Boolean {
        val r1 = p1.run(ctxt)
        if (!r1) return false
        val o1 = p1.getReturn(ctxt)
        val r2 = p2.run(ctxt)
        if (!r2) return false
        val o2 = p2.getReturn(ctxt)
        val r3 = p3.run(ctxt)
        if (!r3) return false
        val o3 = p3.getReturn(ctxt)
        val r4 = p4.run(ctxt)
        if (!r4) return false
        val o4 = p4.getReturn(ctxt)
        val r5 = p5.run(ctxt)
        if (!r5) return false
        val o5 = p5.getReturn(ctxt)
        val r6 = p6.run(ctxt)
        if (!r6) return false
        val o6 = p6.getReturn(ctxt)
        ctxt.result = f(o1, o2, o3, o4, o5,o6)
        return true
    }

    override fun toString(): String {
        return f.toString()
    }
}