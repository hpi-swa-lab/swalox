package de.hpi.swa.lox.test;

import org.junit.Test;

public class ClassInheritanceTest extends AbstractLoxTest {

     @Test
     public void testInheritMethod() {
        runAndExpect("inherit method", "class A { m() {return 3;}} class B < A {} var b = B(); print b.m();", "3\n");
     }

     @Test
     public void testSuperCall() {
      runAndExpect("super", 
         "class A { m() {return 3;}}\n" + 
         "class B < A {  m() {return 4;} test() { return super.m();}}\n" +
         "class C < B {  } \n" + 
         "var c = C(); print c.test();", "3\n");
   }
 
     
   

}