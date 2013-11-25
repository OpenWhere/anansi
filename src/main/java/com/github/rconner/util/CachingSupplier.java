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

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import java.lang.ref.WeakReference;

/**
 * A Supplier which caches the instance retrieved from a delegate Supplier in a {@link WeakReference}. If the reference
 * is clear on a call to {@code get()}, it is retrieved again from the delegate. Note that the delegate Supplier cannot
 * return null. Instances of this class are internally thread-safe and will never invoke the delegate concurrently.
 * <p/>
 * This class is not currently used, but was designed to help ImmutableStack.reverse() to be lazy, allow its value to
 * be reused, and allow its value to be garbage collected.
 *
 * @param <T>
 */
@Beta
public final class CachingSupplier<T> implements Supplier<T> {
    private final Supplier<T> delegate;
    private volatile WeakReference<T> ref = new WeakReference<T>( null );
    private final Object lock = new Object();

    private CachingSupplier( final Supplier<T> delegate ) {
        this.delegate = delegate;
    }

    public static <T> Supplier<T> of( final Supplier<T> delegate ) {
        Preconditions.checkNotNull( delegate );
        return delegate instanceof CachingSupplier ? delegate : new CachingSupplier<T>( delegate );
    }

    @Override
    public T get() {
        T value = ref.get();
        if( value == null ) {
            synchronized( lock ) {
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
