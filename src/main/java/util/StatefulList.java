package util;

import java.util.Collection;
import java.util.Iterator;

public class StatefulList<T> implements Iterable<T> {

    private Entry<T> head = null;
    private Entry<T> last = null;

    private Entry<T> choosenElement = null;

    private int size = 0;

    public class Entry<T> {
        private Entry<T> next;
        private T value;

        private Entry(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }

    public StatefulList(Collection<T> coll) {
        for (T e : coll) {
            addAfter(e);
        }

    }

    public StatefulList() {
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void removeAll() {
        while (head != null) {
            pop();
        }
    }

    public T get() {
        if (choosenElement == null) return null;
        return choosenElement.value;
    }

    public Entry<T> getEntry() {
        return choosenElement;
    }

    public boolean setValue(T value) {
        if (choosenElement == null) return false;
        choosenElement.value = value;
        return true;
    }

    public boolean setEntry(Entry<T> select) {
        if (select == null) return false;
        choosenElement = select;

        return true;
    }

    public void addAfter(T value) {
        this.size++;
        Entry<T> newElement = new Entry<>(value);
        if (choosenElement == null) {
            last = head = choosenElement = newElement;
        } else {
            newElement.next = choosenElement.next;
            choosenElement.next = newElement;
            choosenElement = newElement;
            if (choosenElement.next == null) {
                last = choosenElement;
            }
        }
    }

    public void addLast(T value) {
        choosenElement = last;
        addAfter(value);
    }

    public void pushAfter(T value) {
        this.size++;
        Entry<T> newElement = new Entry<>(value);
        if (choosenElement == null) {
            head = choosenElement = newElement;
        } else {
            if (choosenElement.next == null) {
                last = choosenElement;
            }
            newElement.next = choosenElement.next;
            choosenElement.next = newElement;
        }
    }

    public void push(T value) {

        this.size++;
        Entry<T> newElement = new Entry<>(value);
        newElement.next = head;
        if (head == null) {
            last = choosenElement = head = newElement;
        }
        head = newElement;
    }

    public T removeNext() {
        if (choosenElement == null || choosenElement.next == null) return null;
        this.size--;
        StatefulList<T>.Entry<T> originalEntry = choosenElement.next;
        choosenElement.next = choosenElement.next.next;
        if (choosenElement.next == null) last = choosenElement;
        return originalEntry.value;
    }

    public T pop() {

        if (head == null) return null;
        this.size--;
        StatefulList<T>.Entry<T> originalEntry = head;
        head = head.next;
        if (head == null) {
            last = null;
        }
        choosenElement = head;
        return originalEntry.value;
    }

    public T top() {
        if (head == null) return null;
        choosenElement = head;
        return head.value;
    }

    public boolean stepNext() {
        if (choosenElement == null && head != null) {
            choosenElement = head;
            return true;
        }
        if (choosenElement != null && choosenElement.next != null) {
            choosenElement = choosenElement.next;
            return true;
        } else {
            return false;
        }
    }

    public int size() {

        return this.size;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            Entry<T> state = head;

            @Override
            public boolean hasNext() {
                return state != null;
            }

            @Override
            public T next() {
                T result = state.value;
                state = state.next;
                return result;
            }

        };
    }

    public void selectFirstElement() {
        choosenElement = head;
    }

    @Override
    public String toString() {
        final char BLACKSUN = 9728;
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        Iterator<T> it = iterator();
        if (it.hasNext()) {
            T v = choosenElement.value;
            T n = it.next();
            if (v == n) sb.append(BLACKSUN);
            sb.append(String.valueOf(n));
        }
        while (it.hasNext()) {
            sb.append(", ");
            T v = choosenElement.value;
            T n = it.next();
            if (v == n) sb.append(BLACKSUN);
            sb.append(String.valueOf(n));
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean containsValue(T value) {
        for (T t : this) {
            if (t == null && value == null) return true;
            if (t.equals(value)) return true;
        }

        return false;
    }

    public boolean contains(T value) {
        for (T t : this) {
            if (t == value) return true;
        }
        return false;
    }

}