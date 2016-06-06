/*
 * Copyright 2005 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.ruleunit.reactive;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class ReactiveCollection<T> implements Collection<T> {

    private final Collection<T> collection;

    private Observable observable;

    public ReactiveCollection() {
        this(new ArrayList<T>());
    }

    public ReactiveCollection( Collection<T> collection ) {
        this.collection = collection;
    }

    public void register(Observable observable) {
        this.observable = observable;
    }

    @Override
    public int size() {
        return collection.size();
    }

    @Override
    public boolean isEmpty() {
        return collection.isEmpty();
    }

    @Override
    public boolean contains( Object o ) {
        return collection.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return collection.iterator();
    }

    @Override
    public Object[] toArray() {
        return collection.toArray();
    }

    @Override
    public <T1> T1[] toArray( T1[] a ) {
        return collection.toArray(a);
    }

    @Override
    public boolean add( T t ) {
        boolean result = collection.add(t);
        observable.notifyAdd( t );
        return result;
    }

    @Override
    public boolean remove( Object o ) {
        return collection.remove(o);
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        return collection.containsAll(c);
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        return collection.addAll(c);
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        return collection.removeAll(c);
    }

    @Override
    public boolean retainAll( Collection<?> c ) {
        return collection.retainAll(c);
    }

    @Override
    public void clear() {
        collection.clear();
    }
}
