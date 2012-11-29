/*
 * Copyright (c) 2012 Ray A. Conner
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

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.github.rconner.util.IterableTest.assertIteratorContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ChainTest {

    private static <E> void assertChainContains(Chain<E> chain, E... elements) {

        // These are repeated intentionally, to make sure invoking iterator() or reverse() does not change state.

        assertIteratorContains(chain.iterator(), elements);
        assertIteratorContains(chain.iterator(), elements);

        Iterable<E> reverse = chain.reverse();
        Object[] expectedReverse = Lists.reverse(Lists.newArrayList(elements)).toArray();
        assertIteratorContains(reverse.iterator(), expectedReverse);
        assertIteratorContains(reverse.iterator(), expectedReverse);

        assertThat(chain.size(), is(elements.length));

        if (elements.length == 0) {
            try {
                chain.head();
                fail("Should throw NoSuchElementException.");
            } catch (NoSuchElementException ignored) {
                // expected
            }
            try {
                chain.tail();
                fail("Should throw NoSuchElementException.");
            } catch (NoSuchElementException ignored) {
                // expected
            }
        } else {
            assertThat(chain.head(), is(elements[0]));
            assertChainContains(chain.tail(), Arrays.copyOfRange(elements, 1, elements.length));
        }

        // Test them again to make sure following the head/tail references didn't change state.

        assertIteratorContains(chain.iterator(), elements);
        assertIteratorContains(chain.reverse().iterator(), expectedReverse);
        assertThat(chain.size(), is(elements.length));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEmpty() {
        Chain<Integer> chain = Chain.of();
        assertChainContains(chain);
        assertChainContains(chain.with(101).with(102), 102, 101);
        assertChainContains(chain);
    }

    @Test
    public void testSingle() {
        Chain<Integer> chain = Chain.of(42);
        assertChainContains(chain, 42);
        assertChainContains(chain.with(101).with(102), 102, 101, 42);
        assertChainContains(chain, 42);
    }

    @Test
    public void testMany() {
        Chain<Integer> chain = Chain.of(2, 3, 5, 7, 11);
        assertChainContains(chain, 2, 3, 5, 7, 11);
        assertChainContains(chain.with(101).with(102), 102, 101, 2, 3, 5, 7, 11);
        assertChainContains(chain, 2, 3, 5, 7, 11);
    }

}
