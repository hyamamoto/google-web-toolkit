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
package com.google.gwt.sample.expenses.gwt.request;

import com.google.gwt.requestfactory.shared.Record;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * "API Generated" tool for resolving an arbitray Class to a specific proxy
 * type.
 * <p>
 * IRL this class will be generated by a JPA-savvy tool run before compilation.
 * <p>
 * A helper class for dealing with proxy types. Subclass it and override the
 * various handle methods for type specific handling of proxy objects or
 * classes, then call {@link #process(Class)} or {@link #process(Object)}.
 * Optionally use {#setResult} to set the return value of the {@link #process}
 * call.
 * <P>
 * Use {@link #getAll} for a set of all proxy types.
 * 
 * @param <T> the type to filter to
 */
public abstract class ExpensesEntityTypesProcessor<T> {

  /**
   * Return a set of all proxy types available to this application.
   */
  public static Set<Class<? extends Record>> getAll() {
    Set<Class<? extends Record>> rtn = new HashSet<Class<? extends Record>>();

    rtn.add(EmployeeRecord.class);
    rtn.add(ReportRecord.class);

    return Collections.unmodifiableSet(rtn);
  }

  private static void process(ExpensesEntityTypesProcessor<?> processor,
      Class<?> clazz) {
    if (EmployeeRecord.class.equals(clazz)) {
      processor.handleEmployee((EmployeeRecord) null);
      return;
    }
    if (ReportRecord.class.equals(clazz)) {
      processor.handleReport((ReportRecord) null);
      return;
    }
    processor.handleNonProxy(null);
  }

  private static void process(ExpensesEntityTypesProcessor<?> processor,
      Object proxy) {
    if (proxy instanceof EmployeeRecord) {
      processor.handleEmployee((EmployeeRecord) proxy);
      return;
    }
    if (proxy instanceof ReportRecord) {
      processor.handleReport((ReportRecord) proxy);
      return;
    }
    processor.handleNonProxy(proxy);
  }

  private final T defaultValue;

  private T result;

  /**
   * Create an instance with a null default value.
   */
  public ExpensesEntityTypesProcessor() {
    defaultValue = null;
  }

  /**
   * Create an instance with the given default value.
   * 
   * @param the value that will be returned by {@link #process} if
   *          {@link #setResult} is not called.
   */
  public ExpensesEntityTypesProcessor(T defaultValue) {
    this.defaultValue = defaultValue;
  }

  public abstract void handleEmployee(EmployeeRecord proxy);

  /**
   * Called if {@link #process} is called with a non-proxy object. This default
   * implementation does nothing.
   */
  public void handleNonProxy(@SuppressWarnings("unused") Object object) {
  }

  public abstract void handleReport(ReportRecord proxy);

  /**
   * Call the handle method of the appropriate type, with a null argument. Note
   * that this will not work as expected on the class objects returned by the
   * {@link #getClass()} method of a proxy object, due to limitations of GWT's
   * metadata. It will only work with against class objects in the set returned
   * by {@link #getAll()}, or returned by
   * {@link com.google.gwt.requestfactory.shared.RequestFactory#getClass(Record)}
   * or
   * {@link com.google.gwt.requestfactory.shared.RequestFactory#getClass(String)}
   * .
   * 
   * @param clazz the proxy type to resolve
   * @return the value provided via {@link #setResult}, or the default value
   */
  public T process(Class<?> clazz) {
    setResult(defaultValue);
    ExpensesEntityTypesProcessor.process(this, clazz);
    return result;
  }

  /**
   * Process a proxy object
   * 
   * @param proxy the proxy to process
   * @return the value provided via {@link #setResult}, or the default value
   */
  public T process(Object proxy) {
    setResult(defaultValue);
    ExpensesEntityTypesProcessor.process(this, proxy);
    return result;
  }

  /**
   * Set the value to return from a call to {@link #process(Class)} or
   * {@link #process(Object)}.
   * 
   * @param result the value to return
   */
  protected void setResult(T result) {
    this.result = result;
  }
}
