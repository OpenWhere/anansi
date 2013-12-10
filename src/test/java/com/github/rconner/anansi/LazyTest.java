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

import com.github.rconner.util.IterableTest;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

        TestIterable( final List<T> items ) {
            this.list = Lists.newArrayList( items );
        }

        @Override
        public Iterator<T> iterator() {
            hasBeenInvoked = true;
            return list.iterator();
        }
    }

    static class TestTraverser implements Traverser<String, String> {
        final TestIterable<Walk<String,String>> iterable;

        TestTraverser( final String... items ) {
            final List<Walk<String,String>> list = Lists.newArrayList();
            for( final String item : items ) {
                list.add( Walk.<String,String>empty( item ) );
            }
            iterable = new TestIterable<Walk<String, String>>( list );
        }

        @Override
        public Iterable<Walk<String, String>> apply( final String input ) {
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
        IterableTest.assertIteratorContains( lazyIterator, "a", "b", "c" );
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
        IterableTest.assertIteratorContains( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.iterator( iterable );
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        IterableTest.assertIteratorContains( Lazy.iterator( iterable ), "a", "c" );
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
        IterableTest.assertIteratorContains( lazyIterator, "a", "b", "c" );
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
        IterableTest.assertIteratorContains( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.iterable( iterable ).iterator();
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        IterableTest.assertIteratorContains( Lazy.iterable( iterable ).iterator(), "a", "c" );
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

    private static void assertContainsWalks( final Iterator<Walk<String, String>> iterator, final String... vertices ) {
        for( final String vertex : vertices ) {
            assertThat( iterator.hasNext(), is( true ) );
            final Walk<String, String> walk = iterator.next();
            assertThat( walk.getFrom(), is( vertex ) );
        }
        assertThat( iterator.hasNext(), is( false ) );
        try {
            iterator.next();
            fail( "Should throw NoSuchElementException." );
        } catch( NoSuchElementException ignored ) {
            // expected
        }
    }

    @Test
    public void traverser() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final Traverser<String,String> lazyTraverser = Lazy.traverser( traverser );
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        final Iterable<Walk<String, String>> lazyIterable = lazyTraverser.apply( "root" );
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        final Iterator<Walk<String,String>> lazyIterator = lazyIterable.iterator();
        assertThat( traverser.iterable.hasBeenInvoked, is( false ) );
        lazyIterator.hasNext();
        assertThat( traverser.iterable.hasBeenInvoked, is( true ) );
        assertContainsWalks( lazyIterator, "a", "b", "c" );
    }

    @Test
    public void traverserRemove() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final Traverser<String, String> lazyTraverser = Lazy.traverser( traverser );
        final Iterable<Walk<String, String>> lazyIterable = lazyTraverser.apply( "root" );
        Iterator<Walk<String, String>> lazyIterator = lazyIterable.iterator();
        try {
            lazyIterator.remove();
            fail( "Should throw IllegalStateException" );
        } catch( IllegalStateException e ) {
            // ignored
        }
        assertContainsWalks( lazyIterator, "a", "b", "c" );

        lazyIterator = Lazy.traverser( traverser ).apply( "root" ).iterator();
        lazyIterator.next();
        lazyIterator.next();
        lazyIterator.remove();
        assertContainsWalks( Lazy.traverser( traverser ).apply( "root" ).iterator(), "a", "c" );
    }

    @Test
    public void traverserAlreadyLazy() {
        final TestTraverser traverser = new TestTraverser( "a", "b", "c" );
        final Traverser<String, String> lazyTraverser = Lazy.traverser( traverser );
        assertThat( Lazy.traverser( lazyTraverser ), is( lazyTraverser ) );
    }

    @Test( expected = NullPointerException.class )
    public void traverserNull() {
        Lazy.traverser( (Traverser<Object, Object>) null );
    }
}
