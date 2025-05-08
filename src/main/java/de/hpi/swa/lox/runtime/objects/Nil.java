package de.hpi.swa.lox.runtime.objects;

public final class Nil {
    public static final Nil INSTANCE = new Nil();

    private Nil() {
    }

    @Override
    public String toString() {
        return "nil";
    }
}
