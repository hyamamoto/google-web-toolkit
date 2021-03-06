/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.aria.client;

import com.google.gwt.aria.client.CommonAttributeTypes.AriaAttributeType;
import com.google.gwt.dom.client.Element;

/**
 * <p>Class representing ARIA state/property attribute. Contains methods for setting, getting,
 * removing ARIA attributes for an HTML {@link com.google.gwt.dom.client.Element}.</p>
 *
 * <p>For more details about ARIA states and properties check the W3C ARIA documentation
 * <a href="http://www.w3.org/TR/wai-aria/states_and_properties"> Supported States and Properties
 * </a>.</p>
 *
 * @param <T> The attribute value type
 */
public class Attribute<T> {
  private final String name;
  private String defaultValue;

  /**
   * Constructs a state/property ARIA attribute with name {@code name} and {@code defaultValue}.
   *
   * @param name State/Property name
   * @param defaultValue Default values
   */
  public Attribute(String name, String defaultValue) {
    assert name != null : "Name cannot be null";
    this.name = name;
    this.defaultValue = defaultValue;
  }

  /**
   * Constructs a state/property ARIA attribute with name {@code name} and null default value.
   *
   * @param name State/Property name
   */
  public Attribute(String name) {
    this(name, null);
  }

  /**
   * Gets the HTML attribute value for the attribute with name {@code name} for element
   * {@code element}
   *
   * @param element HTM element
   * @return The attribute value for {@code element}
   */
  public String get(Element element) {
    assert element != null : "Element cannot be null.";
    return element.getAttribute(name);
  }

  /**
   * Gets the property/state name
   *
   * @return The attribute name
   */
  public String getName() {
    return name;
  }

  /**
   * Removes the state/property attribute for element {@code element}.
   *
   * @param element HTM element
   */
  public void remove(Element element) {
    assert element != null : "Element cannot be null.";
    element.removeAttribute(name);
  }

  /**
   * Sets the state/property {@code value} for the HTML element {@code element}.
   *
   * @param element HTML element
   * @param value Attribute value
   */
  public void set(Element element, T value) {
    assert element != null : "Element cannot be null.";
    element.setAttribute(name, getAriaValue(value));
  }

  /**
   * Sets the state/property value to the defaultValue if not null. If a list of default values is
   * set, every default value is converted to string and the string values are concatenated in a
   * string token list. There is an assertion checking whether the default is null. Note that the
   * asserts are enabled during development and testing but they will be stripped in production
   * mode.
   *
   * @param element HTML element
   */
  public void setDefault(Element element) {
    assert element != null : "Element cannot be null.";
    assert defaultValue != null && !defaultValue.isEmpty() : "Default value cannot be null.";
    element.setAttribute(name, defaultValue);
  }

  private String getAriaValue(T value) {
    if (value instanceof AriaAttributeType) {
      return ((AriaAttributeType) value).getAriaValue();
    } else {
      return String.valueOf(value);
    }
  }
}
