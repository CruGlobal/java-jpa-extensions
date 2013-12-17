package org.ccci.gto.persistence;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FoundRowsList<E> implements List<E> {
    private final List<E> _del;
    private final long _rows;

    public FoundRowsList(final List<E> list) {
        this(list, -1);
    }

    public FoundRowsList(final List<E> list, final long rows) {
        _del = list;
        _rows = rows;
    }

    public long getFoundRows() {
        return _rows;
    }

    @Override
    public int size() {
        return _del.size();
    }

    @Override
    public boolean isEmpty() {
        return _del.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return _del.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return _del.iterator();
    }

    @Override
    public Object[] toArray() {
        return _del.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return _del.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return _del.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return _del.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return _del.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return _del.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return _del.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return _del.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return _del.retainAll(c);
    }

    @Override
    public void clear() {
        _del.clear();
    }

    @Override
    public E get(int index) {
        return _del.get(index);
    }

    @Override
    public E set(int index, E element) {
        return _del.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        _del.add(index, element);
    }

    @Override
    public E remove(int index) {
        return _del.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return _del.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return _del.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return _del.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return _del.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return _del.subList(fromIndex, toIndex);
    }
}
