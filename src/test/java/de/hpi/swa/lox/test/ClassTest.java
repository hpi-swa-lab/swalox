package de.hpi.swa.lox.test;

import org.junit.Test;

public class ClassTest extends AbstractLoxTest {

     @Test
     public void testDelcareClass() {
        runAndExpect("declare class", "class O {} print O;", "Class O\n");
     }

     @Test 
     public void testInstantiation() {
          runAndExpect("new object", "class O {} var o = O(); print o;", "O\n");
     }
  
     @Test 
     public void testReadProperty() {
          runAndExpect("read nil", "class O {} var o = O(); print o.a;", "nil\n");
     }

     @Test 
     public void testProperties() {
          runAndExpect("read and write object props", "class O {} var o = O(); o.a = 3; print o.a;", "3\n");
     }
     @Test 
     public void testMethodDeclaration() {
          runAndExpect("declare method", "class O { m() {return 1;}} var o = O(); print o.m;", "O#m\n");
     }

     @Test 
     public void testMethodCall() {
          runAndExpect("call method", "class O { m() {return 1;}} var o = O(); print o.m();", "1\n");
     }

     @Test 
     public void testMethodCallWithThis() {
          runAndExpect("call method", "class O { m() {return this;}} var o = O(); print o.m();", "O\n");
     }

     @Test 
     public void testMethodCallWithThisProperty() {
          runAndExpect("call method", "class O { m() {return this.a;}} var o = O(); o.a = 3; print o.m();", "3\n");
     }

     @Test 
     public void testBoundMethodCall() {
          runAndExpect("call bound method", "class O { m() {return this.a;}} var o = O(); o.a = 3; var x = o.m; print x();", "3\n");
     }

     @Test 
     public void testIniit() {
          runAndExpect("class init", "class O { init() {this.a = 3;}} var o = O(); print o.a;", "3\n");
     }

     @Test 
     public void testInitWithArguments() {
          runAndExpect("class init with args", "class O { init(a) {this.a = a;}} var o = O(3); print o.a;", "3\n");
     }

     @Test 
     public void testObjectsReferenceTheirClasss() {
          runAndExpect("class Member", "class O {} var o = O(); print o.Class;", "Class O\n");
     }

     @Test 
     public void testAssignmentExpressionValue() {
          runAndExpect("assign property expression", "class O {} var o = O(); print o.a = 3;", "3\n");
     }
     

}

