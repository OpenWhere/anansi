/*
 * Copyright (c) 2013 Ray A. Conner
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

import com.google.common.collect.Lists;
import com.google.common.collect.TreeTraverser;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static com.github.rconner.util.IterableTest.assertIteratorContains;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class LazyTest {

    static class TestIterable<T> implements Iterable<T> {
        private final List<T> list;
        boolean hasBeenInvoked = false;

        TestIterable( final T... items ) {
            this.list = Lists.newArrayList( items );
        }

        @Override
        public Iterator<T> iterator() {
            hasBeenInvoked = true;
            return list.iterator();
        }
    }

    static class TestTraverser extends TreeTraverser<String> {
        final TestIterable<String> iterable;

        TestTraverser( final String... items ) {
            iterable = new TestIterable<String>( items );
        }

        @Override
        public Iterable<String> children( final String root ) {
            return iterable;
        }
    }

    @Test
    public void iterator() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        assertThat( iterable.hasBeenInvoked, is( false ) );
        final Iterator<String> lazyIterator = Lazy.iterator( iterable );
        assertThat( iterable.hasBeenInvoked, is( false ) );
        lazyIterator.hasNext();
        assertThat( iterable.hasBeenInvoked, is( true ) );
        assertIteratorContains( lazyIterator, "a", "b", "c" );
    }

    @Test
    public void iteratorRemove() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        Iterator<String> lazyIterator = Lazy.iterator( iterable );
        try {
            lazyIterator.remove();
            fail( "Should throw IllegalStateException" );
        } catch( IllegalStateException e ) {
            // ignored
        }
        assertIteratorContains( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.iterator( iterable );
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        assertIteratorContains( Lazy.iterator( iterable ), "a", "c" );
    }

    @Test
    public void iteratorAlreadyLazy() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        final Iterable<String> lazyIterable = Lazy.iterable( iterable );
        // There's no good way to test the result of this, but it does cover the condition/branch
        Lazy.iterator( lazyIterable );
    }

    @Test( expected = NullPointerException.class )
    public void iteratorNull() {
        Lazy.iterator( null );
    }

    @Test
    public void iterable() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        assertThat( iterable.hasBeenInvoked, is( false ) );
        final Iterable<String> lazyIterable = Lazy.iterable( iterable );
        assertThat( iterable.hasBeenInvoked, is( false ) );
        final Iterator<String> lazyIterator = lazyIterable.iterator();
        assertThat( iterable.hasBeenInvoked, is( false ) );
        lazyIterator.hasNext();
        assertThat( iterable.hasBeenInvoked, is( true ) );
        assertIteratorContains( lazyIterator, "a", "b", "c" );
    }

    @Test
    public void iterableRemove() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        final Iterable<String> lazyIterable = Lazy.iterable( iterable );
        Iterator<String> lazyIterator = lazyIterable.iterator();
        try {
            lazyIterator.remove();
            fail( "Should throw IllegalStateException" );
        } catch( IllegalStateException e ) {
            // ignored
        }
        assertIteratorContains( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.iterable( iterable ).iterator();
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        assertIteratorContains( Lazy.iterable( iterable ).iterator(), "a", "c" );
    }

    @Test
    public void iterableAlreadyLazy() {
        final TestIterable<String> iterable = new TestIterable<String>( "a", "b", "c" );
        final Iterable<String> lazyIterable = Lazy.iterable( iterable );
        assertThat( Lazy.iterable( lazyIterable ), is( lazyIterable ) );
    }

    @Test( expected = NullPointerException.class )
    public void iterableNull() {
        Lazy.iterable( null );
    }

    @Test
    public void traverser() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final TreeTraverser<String> lazyTraverser = Lazy.traverser( traverser );
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        final Iterable<String> lazyIterable = lazyTraverser.children( "root" );
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        final Iterator<String> lazyIterator = lazyIterable.iterator();
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        lazyIterator.hasNext();
        assertThat( traverser.iterable.hasBeenInvoked, is( true ) );
        assertIteratorContains( lazyIterator, "a", "b", "c" );
    }

    @Test
    public void traverserRemove() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final TreeTraverser<String> lazyTraverser = Lazy.traverser( traverser );
        final Iterable<String> lazyIterable = lazyTraverser.children( "root" );
        Iterator<String> lazyIterator = lazyIterable.iterator();
        try {
            lazyIterator.remove();
            fail( "Should throw IllegalStateException" );
        } catch( IllegalStateException e ) {
            // ignored
        }
        assertIteratorContains( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.traverser( traverser ).children( "root" ).iterator();
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        assertIteratorContains( Lazy.traverser( traverser ).children( "root" ).iterator(), "a", "c" );
    }

    @Test
    public void traverserAlreadyLazy() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final TreeTraverser<String> lazyTraverser = Lazy.traverser( traverser );
        assertThat( Lazy.traverser( lazyTraverser ), is( lazyTraverser ) );
    }

    @Test( expected = NullPointerException.class )
    public void traverserNull() {
        Lazy.traverser( null );
    }
}
