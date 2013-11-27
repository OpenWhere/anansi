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

package com.github.rconner.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Helper methods for other tests, and tests for those methods (which is why this class is named ...Test, so the test
 * runner will pick it up).
 */
public final class IterableTest {

    // TODO: These should really be hamcrest Matcher implementations. I'm sure these already exist somewhere,
    // but it's not in the junit jar.

    public static <E> void assertIteratorEmpty( final Iterator<E> iterator ) {
        assertThat( iterator.hasNext(), is( false ) );
        try {
            iterator.next();
            fail( "Should throw NoSuchElementException." );
        } catch( NoSuchElementException ignored ) {
            // expected
        }
    }

    @Test
    public void assertIteratorEmpty() {
        assertIteratorEmpty( Iterators.emptyIterator() );
    }

    public static void assertIteratorsEqual( final Iterator<?> actual, final Iterator<?> expected ) {
        assertThat( Iterators.elementsEqual( actual, expected ), is( true ) );
        assertIteratorEmpty( actual );
        assertIteratorEmpty( expected );
    }

    public static void assertIteratorContains( final Iterator<?> iterator, final Object... elements ) {
        assertIteratorsEqual( iterator, Arrays.asList( elements ).iterator() );
    }

    @Test
    public void assertIteratorContains() {
        assertIteratorContains( Iterators.emptyIterator() );
        assertIteratorContains( Arrays.asList( 2 ).iterator(), 2 );
        assertIteratorContains( Arrays.asList( 3, 5 ).iterator(), 3, 5 );
        assertIteratorContains( Arrays.asList( 7, 11, 13, 17, 19 ).iterator(), 7, 11, 13, 17, 19 );
    }

    public static void assertIterablesEqual( final Iterable<?> actual, final Iterable<?> expected ) {
        assertThat( Iterables.elementsEqual( actual, expected ), is( true ) );
    }

    @Test
    public void assertIterablesEqual() {
        assertIterablesEqual( Collections.emptyList(), ImmutableSet.of() );
        assertIterablesEqual( Arrays.asList( 2 ), Lists.newArrayList( 2 ) );
        assertIterablesEqual( Arrays.asList( 7, 11, 13, 17, 19 ), Lists.newArrayList( 7, 11, 13, 17, 19 ) );
    }
}
