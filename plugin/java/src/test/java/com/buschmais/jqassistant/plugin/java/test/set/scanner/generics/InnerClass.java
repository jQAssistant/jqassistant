package com.buschmais.jqassistant.plugin.java.test.set.scanner.generics;

import java.io.Serializable;
import java.util.*;

public class InnerClass<X> {

    public class Inner<Y> extends InnerClass<X> {

        private X x;

        private Y y;

    }

    public static class CheckedNavigableSet<E> implements NavigableSet<E>, Serializable {
        @Override
        public E lower(E e) {
            return null;
        }

        @Override
        public E floor(E e) {
            return null;
        }

        @Override
        public E ceiling(E e) {
            return null;
        }

        @Override
        public E higher(E e) {
            return null;
        }

        @Override
        public E pollFirst() {
            return null;
        }

        @Override
        public E pollLast() {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(E e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public NavigableSet<E> descendingSet() {
            return null;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return null;
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return null;
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return null;
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return null;
        }

        @Override
        public Comparator<? super E> comparator() {
            return null;
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return null;
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return null;
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return null;
        }

        @Override
        public E first() {
            return null;
        }

        @Override
        public E last() {
            return null;
        }
    }

}
