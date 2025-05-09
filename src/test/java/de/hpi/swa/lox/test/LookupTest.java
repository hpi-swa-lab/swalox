package de.hpi.swa.lox.test;

import org.junit.Test;

public class LookupTest extends AbstractLoxTest {

    @Test
    public void testLookupJavaString() {
        runAndExpect("string", " var a = lookup(\"java\", \"java.lang.String\"); print a;",
                "JavaClass[java.lang.String]\n");
    }

    @Test
    public void testLookupRandomNumberGenerator() {
        runAndExpect("random", " var a = lookup(\"java\", \"java.lang.Math\"); print a.random() + 1 > 0;", "true\n");
    }

    @Test
    public void testInstantiateJava() {
        runAndExpect("instance", " var a = lookup(\"java\", \"java.lang.String\"); print a();", "\n");
    }

    @Test
    public void testArrayList() {
        runAndExpect("instance", """
                var ArrayList = lookup("java", "java.util.ArrayList");
                var list = ArrayList();
                list.add(3);
                print list;
                """, "JavaObject[[3] (java.util.ArrayList)]\n");
    }

    @Test
    public void testLookupSL() {
        runAndExpect("instance", """
                var slEvalBuiltin = lookup("sl", "eval");
                 print slEvalBuiltin("sl", "function main(){ return 3 + 4;}");
                 """, "7\n");
    }

}
