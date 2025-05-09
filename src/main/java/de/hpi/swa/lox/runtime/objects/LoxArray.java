package de.hpi.swa.lox.runtime.objects;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.strings.TruffleString;

public class LoxArray {

    private Object[] elements;

    int size = 0;

    public LoxArray() {
        // elements = new ArrayList<Object>();
        elements = new Object[10];
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
