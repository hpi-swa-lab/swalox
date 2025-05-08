package de.hpi.swa.lox.runtime.objects;

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class GlobalObject {

    private final Map<String, Object> globals = new HashMap<>();

    @TruffleBoundary
    public Object get(String name) {
        return globals.get(name);
    }

    @TruffleBoundary
    public void set(String name, Object value) {
        globals.put(name, value);
    }

    @TruffleBoundary
    public boolean hasKey(String name) {
        return globals.containsKey(name);
    }
}
