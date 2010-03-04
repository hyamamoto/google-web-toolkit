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
package com.google.gwt.sample.expenses.domain;

import java.util.Date;

/**
 * Models an expense report.
 */
// @javax.persistence.Entity
public class Report implements Entity {
  private final Long id;

  private final Integer version;
  @javax.validation.constraints.NotNull
  @javax.validation.constraints.Past
  // @javax.persistence.Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private java.util.Date created = new Date();

  @javax.validation.constraints.NotNull
  // @javax.persistence.Enumerated
  private Status status;

  @javax.validation.constraints.NotNull
  // @javax.persistence.ManyToOne(targetEntity =
  // com.google.io.expenses.server.domain.Employee.class)
  // @javax.persistence.JoinColumn
  private Employee reporter;

  @javax.validation.constraints.Size(min = 3, max = 100)
  private String purpose;

  // @javax.persistence.ManyToOne(targetEntity =
  // com.google.io.expenses.server.domain.Employee.class)
  // @javax.persistence.JoinColumn
  private Employee approved_supervisor;

  // @javax.persistence.OneToMany(cascade = javax.persistence.CascadeType.ALL,
  // mappedBy = "report")
  // private Set<ReportItem> items = new HashSet<ReportItem>();

  public Report() {
    id = null;
    version = null;
  }

  Report(Long id, Integer version) {
    this.id = id;
    this.version = version;
  }

  public <T> T accept(EntityVisitor<T> visitor) {
    return visitor.visit(this);
  }

  /**
   * @return the approved_supervisor
   */
  public Employee getApproved_supervisor() {
    return approved_supervisor;
  }

  /**
   * @return the created
   */
  public java.util.Date getCreated() {
    return created;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return the purpose
   */
  public String getPurpose() {
    return purpose;
  }

  /**
   * @return the reporter
   */
  public Employee getReporter() {
    return reporter;
  }

  /**
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * @return the version
   */
  public Integer getVersion() {
    return version;
  }

  /**
   * @param approvedSupervisor the approved_supervisor to set
   */
  public void setApproved_supervisor(Employee approvedSupervisor) {
    approved_supervisor = approvedSupervisor;
  }

  /**
   * @param purpose the purpose to set
   */
  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  /**
   * @param reporter the reporter to set
   */
  public void setReporter(Employee reporter) {
    this.reporter = reporter;
  }

  /**
   * @param status the status to set
   */
  public void setStatus(Status status) {
    this.status = status;
  }
}
