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

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.lang.ref.WeakReference;

/**
 * A Supplier which caches the instance retrieved from a delegate Supplier in a {@link java.lang.ref.WeakReference}. If
 * the reference is clear on a call to {@code get()}, it is retrieved again from the delegate. Note that the delegate
 * Supplier cannot return null. Instances of this class are internally thread-safe and will never invoke the delegate
 * concurrently.
 *
 * @param <T>
 */
public class CachingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private volatile WeakReference<T> ref = new WeakReference<T>( null );

    private CachingSupplier( Supplier<T> delegate ) {
        this.delegate = delegate;
    }

    public static <T> Supplier<T> of( Supplier<T> delegate ) {
        return ( delegate instanceof CachingSupplier ) ? delegate : new CachingSupplier<T>( Preconditions.checkNotNull( delegate ) );
    }

    @Override
    public T get() {
        T value = ref.get();
        if( value == null ) {
            synchronized( this ) {
                value = ref.get();
                if( value == null ) {
                    value = delegate.get();
                    Preconditions.checkState( value != null, "Value returned by delegate supplier cannot be null." );
                    ref = new WeakReference<T>( value );
                }
            }
        }
        return value;
    }
}
