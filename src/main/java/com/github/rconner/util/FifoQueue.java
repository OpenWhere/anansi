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

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.UnmodifiableIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A minimal FIFO queue implementation, which does <strong>not</strong> implement {@link java.util.Collection}. This
 * implementation is not thread-safe, and does not even have a fail-fast iterator.
 */
public final class FifoQueue<E> implements Iterable<E> {

    private Node<E> head = null;
    private Node<E> tail = null;
    private int size = 0;

    public FifoQueue() {
        // nothing to do
    }

    public static <E> FifoQueue<E> of( E... elements ) {
        FifoQueue<E> queue = new FifoQueue<E>();
        for( E element : elements ) {
            queue.enqueue( element );
        }
        return queue;
    }

    public void enqueue( E element ) {
        Node<E> node = new Node<E>( element );
        if( tail == null ) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public E dequeue() {
        if( head == null ) {
            throw new NoSuchElementException();
        }
        E element = head.element;
        head = head.next;
        if( head == null ) {
            tail = null;
        }
        size--;
        return element;
    }

    public E head() {
        if( head == null ) {
            throw new NoSuchElementException();
        }
        return head.element;
    }

    public boolean contains( Object object ) {
        Node<E> node = head;
        while( node != null ) {
            if( Objects.equal( object, node.element ) ) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<E> iterator() {
        return new UnmodifiableIterator<E>() {
            Node<E> node = head;

            @Override
            public boolean hasNext() {
                return node != null;
            }

            @Override
            public E next() {
                if( node == null ) {
                    throw new NoSuchElementException();
                }
                E element = node.element;
                node = node.next;
                return element;
            }
        };
    }

    private static final Joiner JOINER = Joiner.on( ", " ).useForNull( "null" );

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append( '[' );
        JOINER.appendTo( s, this );
        s.append( ']' );
        return s.toString();
    }


    private static class Node<E> {
        final E element;
        Node<E> next = null;

        private Node( E element ) {
            this.element = element;
        }
    }
}
