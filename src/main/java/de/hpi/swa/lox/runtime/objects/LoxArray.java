package de.hpi.swa.lox.runtime.objects;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.strings.TruffleString;

public class LoxArray implements TruffleObject {

    private Object[] elements;

    int size = 0;

    public LoxArray() {
        elements = new Object[10];
    }

    public int size() {
        return size;
    }

    // availabble space without growing
    public int capacity() {
        return elements.length;
    }

    public Object get(int index) {
        if (elements.length <= index || index < 0) {
            return Nil.INSTANCE;
        }

        var result = elements[index];
        if (result != null) {
            return result;
        } else {
            return Nil.INSTANCE;
        }
    }

    public void set(int index, Object value) {
        if (index >= size) {
            size = index + 1;
        }
        if (index >= elements.length) {
            this.ensureCapacity();
        }
        elements[index] = value;
    }

    // does not need to grow, but size changes
    public void setInCapacity(int index, Object value) {
        if (index >= size) {
            size = index + 1;
        }
        elements[index] = value;
    }

    public void setInSize(int index, Object value) {
        elements[index] = value;
    }


    @TruffleBoundary
    private void ensureCapacity() {
        elements = Arrays.copyOf(elements, Math.max(elements.length, size) * 2);
    }

    @TruffleBoundary
    public String toString() {
        var s = "";
        for(int i = 0; i < size; i++) {
            if (elements[i] != null) {
                if (elements[i] instanceof TruffleString) {
                    s += "\"" + elements[i].toString() + "\"";
                } else {
                    s += elements[i].toString();
                }
            } else {
                s += "nil";
            }
            if (i < size - 1) {
                s += ", ";
            }
        }
        return "[" + s + "]";
    }   

}
