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
package com.google.gwt.sample.expenses.shared;

import com.google.gwt.requestfactory.shared.Id;
import com.google.gwt.requestfactory.shared.LongString;
import com.google.gwt.requestfactory.shared.ServerType;
import com.google.gwt.requestfactory.shared.EntityKey;
import com.google.gwt.requestfactory.shared.Version;
import com.google.gwt.valuestore.shared.Property;

import java.util.HashSet;
import java.util.Set;

/**
 * "Generated" proxy of
 * {@link com.google.gwt.sample.expenses.server.domain.Employee domain.Employee}
 * .
 */
@ServerType(com.google.gwt.sample.expenses.server.domain.Employee.class)
public class EmployeeKey implements EntityKey<EmployeeKey> {
  private static EmployeeKey instance;

  public static EmployeeKey get() {
    if (instance == null) {
      instance = new EmployeeKey();
    }
    return instance;
  }

  private final Property<EmployeeKey, String> id = new Property<EmployeeKey, String>(
      String.class, "id");

  private final Property<EmployeeKey, String> displayName = new Property<EmployeeKey, String>(
      String.class, "displayName");

  private final Property<EmployeeKey, EmployeeKey> supervisor = new Property<EmployeeKey, EmployeeKey>(
      EmployeeKey.class, "superVisor");

  private final Property<EmployeeKey, String> userName = new Property<EmployeeKey, String>(
      String.class, "userName");

  private final Property<EmployeeKey, Integer> version = new Property<EmployeeKey, Integer>(
      Integer.class, "version");

  private final Set<Property<EmployeeKey, ?>> properties = new HashSet<Property<EmployeeKey, ?>>();

  EmployeeKey() {
    properties.add(id);
    properties.add(displayName);
    properties.add(supervisor);
    properties.add(userName);
    properties.add(version);
  }

  public Property<EmployeeKey, String> getDisplayName() {
    return displayName;
  }

  @LongString
  @Id
  public Property<EmployeeKey, String> getId() {
    return id;
  }

  public Set<Property<EmployeeKey, ?>> getProperties() {
    return properties;
  }

  public Property<EmployeeKey, EmployeeKey> getSupervisor() {
    return supervisor;
  }

  public Property<EmployeeKey, String> getUserName() {
    return userName;
  }
  
  @Version
  public Property<EmployeeKey, Integer> getVersion() {
    return version;
  }
}
