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
package com.google.gwt.valuestore.shared;

/**
 * @param <V> the type of the value
 */
public class Value<V> extends PropertyReference<V> {

  public static <V> Value<V> of(V value) {
    return new Value<V>(value);
  }

  private final V value;
  
  private Value(V value) {
    this.value = value;
  }

  @Override
  public V get() {
    return value;
  }
}
