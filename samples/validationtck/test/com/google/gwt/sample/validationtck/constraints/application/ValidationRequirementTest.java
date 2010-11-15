/*
 * Copyright 2010 Google Inc.
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
package com.google.gwt.sample.validationtck.constraints.application;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Wraps
 * {@link org.hibernate.jsr303.tck.tests.constraints.application.ValidationRequirementTest}
 */
public class ValidationRequirementTest extends GWTTestCase {
  org.hibernate.jsr303.tck.tests.constraints.application.ValidationRequirementTest delegate =
      new org.hibernate.jsr303.tck.tests.constraints.application.ValidationRequirementTest();

  @Override
  public String getModuleName() {
    return "com.google.gwt.sample.validationtck.ValidationTckTest";
  }

  public void testClassLevelConstraints() {
    delegate.testClassLevelConstraints();
  }

  public void testConstraintAppliedOnFieldAndProperty() {
    delegate.testConstraintAppliedOnFieldAndProperty();
  }

  public void testFieldAccess() {
    delegate.testFieldAccess();
  }

  public void testFieldAndPropertyVisibilityIsNotConstrained() {
    delegate.testFieldAndPropertyVisibilityIsNotConstrained();
  }

  public void testIgnoreStaticFieldsAndProperties() {
    delegate.testIgnoreStaticFieldsAndProperties();
  }

  public void testPropertyAccess() {
    delegate.testPropertyAccess();
  }
}
