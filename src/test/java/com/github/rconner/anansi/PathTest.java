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

package com.github.rconner.anansi;

import com.github.rconner.util.ChainTest;
import org.junit.Test;

import java.util.Iterator;

import static com.github.rconner.util.IterableTest.assertIteratorEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class PathTest {

    @Test
    public void simple() {
        Path<Integer, ?> path = Path.newInstance(2, 3);
        assertThat(path.getFrom(), is(2));
        assertThat(path.getTo(), is(3));
        assertThat(path.getOver(), nullValue());
    }

    @Test
    public void simpleOver() {
        Path<Integer, String> path = Path.newInstance(5, 7, "abc");
        assertThat(path.getFrom(), is(5));
        assertThat(path.getTo(), is(7));
        assertThat(path.getOver(), is("abc"));
    }

    private static <V, E> void assertPathContains(Path<V, Iterable<Path<V, E>>> actual, Path<V, E>... expected) {
        assertThat(actual.getFrom(), is(expected[0].getFrom()));
        assertThat(actual.getTo(), is(expected[expected.length - 1].getTo()));

        Iterator<Path<V, E>> iterator = actual.getOver().iterator();
        for (Path<V, E> expectedSubPath : expected) {
            Path<V, E> actualSubPath = iterator.next();
            assertThat(actualSubPath.getFrom(), is(expectedSubPath.getFrom()));
            assertThat(actualSubPath.getTo(), is(expectedSubPath.getTo()));
            assertThat(actualSubPath.getOver(), is(expectedSubPath.getOver()));
        }
        assertIteratorEmpty(iterator);
    }

    private static <V, E> void assertPathEmpty(Path<V, Iterable<Path<V, E>>> actual, V root) {
        assertThat(actual.getFrom(), is(root));
        assertThat(actual.getTo(), is(root));
        assertIteratorEmpty(actual.getOver().iterator());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void builder() {
        Path.Builder<Integer, String> rootBuilder = Path.from(11);
        Path<Integer, Iterable<Path<Integer, String>>> rootPath = rootBuilder.build();
        assertPathEmpty(rootPath, 11);

        Path.Builder<Integer, String> builderTo13 = rootBuilder.to(13);
        Path<Integer, Iterable<Path<Integer, String>>> pathTo13 = builderTo13.build();
        assertPathContains(pathTo13, Path.<Integer, String>newInstance(11, 13));

        Path.Builder<Integer, String> builderTo17 = builderTo13.to(17, "to 17");
        Path<Integer, Iterable<Path<Integer, String>>> pathTo17 = builderTo17.build();
        assertPathContains(pathTo17, Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 17, "to 17"));

        // From 13, not from the recently added 17
        Path.Builder<Integer, String> builderTo19 = builderTo13.to(19, "to 19");
        Path<Integer, Iterable<Path<Integer, String>>> pathTo19 = builderTo19.build();
        assertPathContains(pathTo19, Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 19, "to 19"));

        // Test that the builders are still building the same paths.
        assertPathEmpty(rootBuilder.build(), 11);
        assertPathContains(builderTo13.build(), Path.<Integer, String>newInstance(11, 13));
        assertPathContains(builderTo17.build(), Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 17, "to 17"));
        assertPathContains(builderTo19.build(), Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 19, "to 19"));

        // Test that all the previously built paths have not changed.
        assertPathEmpty(rootPath, 11);
        assertPathContains(pathTo13, Path.<Integer, String>newInstance(11, 13));
        assertPathContains(pathTo17, Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 17, "to 17"));
        assertPathContains(pathTo19, Path.<Integer, String>newInstance(11, 13), Path.newInstance(13, 19, "to 19"));
    }
}
