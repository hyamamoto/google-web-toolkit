/*
 * Copyright 2006 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.dev.js;

import com.google.gwt.dev.js.ast.JsArrayAccess;
import com.google.gwt.dev.js.ast.JsArrayLiteral;
import com.google.gwt.dev.js.ast.JsBinaryOperation;
import com.google.gwt.dev.js.ast.JsBlock;
import com.google.gwt.dev.js.ast.JsBooleanLiteral;
import com.google.gwt.dev.js.ast.JsBreak;
import com.google.gwt.dev.js.ast.JsCase;
import com.google.gwt.dev.js.ast.JsCatch;
import com.google.gwt.dev.js.ast.JsConditional;
import com.google.gwt.dev.js.ast.JsContinue;
import com.google.gwt.dev.js.ast.JsDecimalLiteral;
import com.google.gwt.dev.js.ast.JsDefault;
import com.google.gwt.dev.js.ast.JsDoWhile;
import com.google.gwt.dev.js.ast.JsEmpty;
import com.google.gwt.dev.js.ast.JsExprStmt;
import com.google.gwt.dev.js.ast.JsExpression;
import com.google.gwt.dev.js.ast.JsFor;
import com.google.gwt.dev.js.ast.JsForIn;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsIf;
import com.google.gwt.dev.js.ast.JsIntegralLiteral;
import com.google.gwt.dev.js.ast.JsInvocation;
import com.google.gwt.dev.js.ast.JsLabel;
import com.google.gwt.dev.js.ast.JsNameRef;
import com.google.gwt.dev.js.ast.JsNew;
import com.google.gwt.dev.js.ast.JsNullLiteral;
import com.google.gwt.dev.js.ast.JsObjectLiteral;
import com.google.gwt.dev.js.ast.JsParameter;
import com.google.gwt.dev.js.ast.JsParameters;
import com.google.gwt.dev.js.ast.JsPostfixOperation;
import com.google.gwt.dev.js.ast.JsPrefixOperation;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.js.ast.JsPropertyInitializer;
import com.google.gwt.dev.js.ast.JsRegExp;
import com.google.gwt.dev.js.ast.JsReturn;
import com.google.gwt.dev.js.ast.JsStringLiteral;
import com.google.gwt.dev.js.ast.JsSwitch;
import com.google.gwt.dev.js.ast.JsThisRef;
import com.google.gwt.dev.js.ast.JsThrow;
import com.google.gwt.dev.js.ast.JsTry;
import com.google.gwt.dev.js.ast.JsVars;
import com.google.gwt.dev.js.ast.JsWhile;

/**
 * Precedence indices from "JavaScript - The Definitive Guide" 4th Edition (page
 * 57)
 * 
 * Precedence 16 is for indivisible primaries that either don't have children,
 * or provide their own delimiters.
 * 
 * Precedence 15 is for really important things that have their own AST classes.
 * 
 * Precedence 14 is for unary operators.
 * 
 * Precedences 12 through 4 are for non-assigning binary operators.
 * 
 * Precedence 3 is for the tertiary conditional.
 * 
 * Precedence 2 is for assignments.
 * 
 * Precedence 1 is for comma operations.
 */
class JsPrecedenceVisitor extends JsAbstractVisitorWithEndVisits {

  public static int exec(JsExpression expression) {
    JsPrecedenceVisitor visitor = new JsPrecedenceVisitor();
    expression.traverse(visitor);
    if (visitor.answer < 0) {
      throw new RuntimeException("Precedence must be >= 0!");
    }
    return visitor.answer;
  }

  private int answer = -1;

  private JsPrecedenceVisitor() {
  }

  public boolean visit(JsArrayAccess x) {
    answer = 15;
    return false;
  }

  public boolean visit(JsArrayLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsBinaryOperation x) {
    answer = x.getOperator().getPrecedence();
    return false;
  }

  public boolean visit(JsBlock x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsBooleanLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsBreak x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsCase x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsCatch x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsConditional x) {
    answer = 3;
    return false;
  }

  public boolean visit(JsContinue x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsDecimalLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsDefault x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsDoWhile x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsEmpty x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsExprStmt x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsFor x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsForIn x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsFunction x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsIf x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsIntegralLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsInvocation x) {
    answer = 15;
    return false;
  }

  public boolean visit(JsLabel x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsNameRef x) {
    if (x.isLeaf()) {
      answer = 16; // primary
    } else { 
      answer = 15; // property access
    }
    return false;
  }

  public boolean visit(JsNew x) {
    answer = 15;
    return false;
  }

  public boolean visit(JsNullLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsObjectLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsParameter x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsParameters x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsPostfixOperation x) {
    answer = x.getOperator().getPrecedence();
    return false;
  }

  public boolean visit(JsPrefixOperation x) {
    answer = x.getOperator().getPrecedence();
    return false;
  }

  public boolean visit(JsProgram x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsPropertyInitializer x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsRegExp x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsReturn x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsStringLiteral x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsSwitch x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsThisRef x) {
    answer = 16; // primary
    return false;
  }

  public boolean visit(JsThrow x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsTry x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsVars x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

  public boolean visit(JsWhile x) {
    throw new RuntimeException("Only expressions have precedence.");
  }

}
