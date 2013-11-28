/*
 * Copyright (c) 2012-2013 Ray A. Conner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.rconner.anansi;

import org.junit.Test;

import java.util.NoSuchElementException;

public class TraversalMoveTest {

    // This test class is exercising TraversalMove.rootIterator(T).next() when the iterator is exhausted.
    // The classes which use the package-private TraversalMove never call next() without having tested hasNext()
    // first, so this logic is not otherwise executed.

    @Test
    public void rootIteratorNotExhausted() {
        TraversalMove<String, String> move = TraversalMove.start( "A" );
        move.iterator.next();
    }

    @Test( expected = NoSuchElementException.class )
    public void rootIteratorExhausted() {
        TraversalMove<String, String> move = TraversalMove.start( "A" );
        move.iterator.next();
        move.iterator.next();
    }
}
