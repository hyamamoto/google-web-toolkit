// Copyright 2006 Google Inc. All Rights Reserved.
package com.google.gwt.dev.jjs.test;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.junit.client.GWTTestCase;

import junit.framework.Assert;

public class CompilerTest extends GWTTestCase {

  private static abstract class Apple implements Fruit {
  }

  private static interface Fruit {
  }

  private static class Fuji extends Apple {
  }

  private static class Granny extends Apple {
  }

  private static class NonSideEffectCauser {
    public static final String NOT_A_COMPILE_TIME_CONSTANT = null;
  }

  private static class SideEffectCauser {
    private static Object instance = new Object();

    static {
      CompilerTest.sideEffectChecker++;
    }

    public static Object causeClinitSideEffect() {
      return instance;
    }
  }

  private static class SideEffectCauser2 {
    static {
      CompilerTest.sideEffectChecker++;
    }

    public static Object causeClinitSideEffect() {
      return null;
    }
  }

  private static final class UninstantiableType {
    public Object field;

    private UninstantiableType() {
    }

    public Object returnNull() {
      return null;
    }
  }

  private static int sideEffectChecker;

  private static String barShouldInline() {
    return "bar";
  }

  private static native boolean cannotOptimize() /*-{
    return true;
  }-*/;

  private static void foo(String string) {
    Object o = string;
  }

  private static void foo(Throwable throwable) {
    Object o = throwable;
  }

  public String getModuleName() {
    return "com.google.gwt.dev.jjs.CompilerSuite";
  }

  public void testArrayStore() {
    Object[][] oaa;
    oaa = new Object[4][4];
    oaa[0][0] = "foo";
    assertEquals(oaa[0][0], "foo");

    oaa = new Object[4][];
    oaa[0] = new Object[4];
    oaa[0][0] = "bar";
    assertEquals(oaa[0][0], "bar");

    Apple[] apple = cannotOptimize() ? new Granny[3] : new Apple[3];
    Apple g = cannotOptimize() ? (Apple) new Granny() : (Apple) new Fuji();
    Apple a = apple[0] = g;
    assertEquals(g, a);
  }

  public void testCastOptimizer() {
    Granny g = new Granny();
    Apple a = g;
    Fruit f = g;
    a = (Apple) f;
    g = (Granny) a;
    g = (Granny) f;
  }

  public void testClassLiterals() {
    assertEquals("void", void.class.toString());
    assertEquals("int", int.class.toString());
    assertEquals("class java.lang.String", String.class.toString());
    assertEquals("class com.google.gwt.dev.jjs.test.CompilerTest",
        CompilerTest.class.toString());
    assertEquals(
        "class com.google.gwt.dev.jjs.test.CompilerTest$UninstantiableType",
        UninstantiableType.class.toString());
    assertEquals("interface com.google.gwt.dev.jjs.test.CompilerTest$Fruit",
        Fruit.class.toString());
    assertEquals("class [I", int[].class.toString());
    assertEquals("class [Ljava.lang.String;", String[].class.toString());
    assertEquals("class [Lcom.google.gwt.dev.jjs.test.CompilerTest;",
        CompilerTest[].class.toString());
    assertEquals(
        "class [Lcom.google.gwt.dev.jjs.test.CompilerTest$UninstantiableType;",
        UninstantiableType[].class.toString());
    assertEquals("class [Lcom.google.gwt.dev.jjs.test.CompilerTest$Fruit;",
        Fruit[].class.toString());
  }

  public void testClinitSideEffectInlining() {
    sideEffectChecker = 0;
    SideEffectCauser.causeClinitSideEffect();
    assertEquals(1, sideEffectChecker);
    SideEffectCauser2.causeClinitSideEffect();
    if (GWT.isScript()) {
      ++sideEffectChecker; // CR #767
    }
    assertEquals(2, sideEffectChecker);
    String checkRescued = NonSideEffectCauser.NOT_A_COMPILE_TIME_CONSTANT;
    assertEquals(null, checkRescued);
  }

  public void testDeadCode() {
    while (returnFalse()) {
      break;
    }

    do {
      break;
    } while (false);

    do {
      break;
    } while (returnFalse());

    for (; returnFalse();) {
    }

    boolean check = false;
    for (check = true; returnFalse(); fail()) {
      fail();
    }
    assertTrue(check);

    if (returnFalse()) {
      fail();
    } else {
    }

    if (!returnFalse()) {
    } else {
      fail();
    }
  }

  public void testDeadTypes() {
    if (false) {
      new Object() {
      }.toString();

      class Foo {
        void a() {
        }
      }
      new Foo().a();
    }
  }

  public void testEmptyBlockStatements() {
    boolean b = false;
    while (b) {
    }

    do {
    } while (b);

    for (; b;) {
    }

    for (;;) {
      break;
    }

    if (b) {
    }

    if (b) {
    } else {
      b = false;
    }

    if (b) {
    } else {
    }

  }

  public native void testEmptyBlockStatementsNative() /*-{
    var b = false;
    while (b) {
    }

    do {
    } while (b);

    for (; b; ) {
    }

    for (;;) {
      break;
    }

    if (b) {
    }

    if (b) {
    } else {
      b = false;
    }

    if (b) {
    } else {
    }
  }-*/;

  public void testEmptyStatements() {
    boolean b = false;

    while (b);

    do; while (b);

    for (; b;);

    for (;;)
      break;

    if (b)
      ;

    if (b)
      ;
    else
      b = false;

    if (b)
      ;
    else
      ;
  }

  public native void testEmptyStatementsNative() /*-{
    var b = false;

    while (b);

    do; while (b);

    for (; b;);

    for (;;)
      break;

    if (b)
      ;

    if (b)
      ;
    else
      b = false;

    if (b)
      ;
    else
      ;
  }-*/;

  public void testForStatement() {
    {
      int i;
      for (i = 0; i < 10; ++i) {
      }
      assertEquals(i, 10);
    }
    {
      int i, c;
      for (i = 0, c = 10; i < c; ++i) {
      }
      assertEquals(i, 10);
      assertEquals(c, 10);
    }
    {
      int j = 0;
      for (int i = 0; i < 10; ++i) {
        ++j;
      }
      assertEquals(j, 10);
    }
    {
      int j = 0;
      for (int i = 0, c = 10; i < c; ++i) {
        ++j;
      }
      assertEquals(j, 10);
    }
  }

  /**
   * Issue #615: Internal Compiler Error
   */
  public void testImplicitNull() {
    boolean b;
    String test = ((((b = true) ? null : null) + " ") + b);
  }

  public void testLabels() {
    int i = 0, j = 0;
    outer : for (i = 0; i < 1; ++i) {
      inner : for (j = 0; j < 1; ++j) {
        break outer;
      }
      fail();
    }
    assertEquals(0, i);
    assertEquals(0, j);

    outer : for (i = 0; i < 1; ++i) {
      inner : for (j = 0; j < 1; ++j) {
        continue outer;
      }
      fail();
    }
    assertEquals(1, i);
    assertEquals(0, j);

    outer : for (i = 0; i < 1; ++i) {
      inner : for (j = 0; j < 1; ++j) {
        break inner;
      }
    }
    assertEquals(1, i);
    assertEquals(0, j);

    outer : for (i = 0; i < 1; ++i) {
      inner : for (j = 0; j < 1; ++j) {
        continue inner;
      }
    }
    assertEquals(1, i);
    assertEquals(1, j);
  }

  public void testLocalRefs() {
    final String foo = cannotOptimize() ? "foo" : "bar";
    final String bar = cannotOptimize() ? "bar" : "foo";
    String result = new Object() {

      private String a = foo;

      {
        a = foo;
      }

      public String toString() {
        return new Object() {

          private static final String constantString = "wallawalla";

          private String ai = foo;

          {
            ai = foo;
          }

          public String toString() {
            // this line used to cause ICE due to no synthetic path to bar
            bar.valueOf(false);

            assertEquals("wallawalla", constantString);
            return foo + a + ai;
          }

        }.toString() + a;
      }

    }.toString();
    assertEquals(result, "foofoofoofoo");
  }

  public void testNullFlow() {
    UninstantiableType f = null;

    try {
      f.returnNull().toString();
      fail();
    } catch (NullPointerException e) {
      // hosted mode
    } catch (JavaScriptException e) {
      // web mode
    }

    try {
      f.field = null;
      fail();
    } catch (NullPointerException e) {
      // hosted mode
    } catch (JavaScriptException e) {
      // web mode
    }

    try {
      UninstantiableType[] fa = null;
      fa[4] = null;
      fail();
    } catch (NullPointerException e) {
      // hosted mode
    } catch (JavaScriptException e) {
      // web mode
    }
  }

  public void testNullFlowArray() {
    UninstantiableType[] uta = new UninstantiableType[10];
    assertEquals(uta.length, 10);
    assertEquals(uta[0], null);
    uta[1] = null;
    assertEquals(uta[1], null);
  }

  public void testNullFlowOverloads() {
    foo((Throwable) null);
    foo((String) null);
  }

  public void testNullFlowVsClassCastPrecedence() {
    try {
      ((UninstantiableType) new Object()).returnNull();
      fail();
    } catch (ClassCastException e) {
      // success
    }
  }

  public void testOuterSuperThisRefs() {
    new B();
  }

  public void testReturnStatementInCtor() {
    class Foo {
      int i;

      Foo(int i) {
        this.i = i;
        if (i == 0)
          return;
        else if (i == 1)
          return;
        return;
      }
    }
    assertEquals(new Foo(0).i, 0);
    assertEquals(new Foo(1).i, 1);
    assertEquals(new Foo(2).i, 2);
  }
  
  public void testStringOptimizations() {
    assertEquals("Herro, AJAX", "Hello, AJAX".replace('l', 'r'));
    assertEquals('J', "Hello, AJAX".charAt(8));
    assertEquals(11, "Hello, AJAX".length());
    assertFalse("Hello, AJAX".equals("me"));
    assertTrue("Hello, AJAX".equals("Hello, AJAX"));
    assertTrue("Hello, AJAX".equalsIgnoreCase("HELLO, ajax"));
    assertEquals("hello, ajax", "Hello, AJAX".toLowerCase());
    
    assertEquals("foobar", "foo" + barShouldInline());
    assertEquals("1bar", 1 + barShouldInline());
    assertEquals("fbar", 'f' + barShouldInline());
    assertEquals("truebar", true + barShouldInline());
    assertEquals("3.3bar", 3.3 + barShouldInline());
    assertEquals("3.3bar", 3.3f + barShouldInline());
    assertEquals("27bar", 27L + barShouldInline());
    assertEquals("nullbar", null + barShouldInline());
  }

  public void testSubclassStaticInnerAndClinitOrdering() {
    new CheckSubclassStaticInnerAndClinitOrdering();
  }

  public void testSwitchStatement() {
    switch (0) {
      case 0:
        int test; // used to cause an ICE
        break;
    }
  }

  public void testUnaryPlus() {
    int x, y = -7;
    x = +y;
    assertEquals(-7, x);
  }

  private boolean returnFalse() {
    return false;
  }

}

class A {
  public abstract class AA {
  }
}

class B extends A {
  {
    new AA() {
    };
  }
}

// This construct used to cause an ICE
class CheckSubclassStaticInnerAndClinitOrdering extends Outer.StaticInner {
  private static class Foo {
  }

  private static final Foo FOO = new Foo();

  public CheckSubclassStaticInnerAndClinitOrdering() {
    this(FOO);
  }

  public CheckSubclassStaticInnerAndClinitOrdering(Foo foo) {
    // This used to be null due to clinit ordering issues
    Assert.assertNotNull(foo);
  }
}

class Outer {
  public static class StaticInner {
  }
}
