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

import com.google.gwt.requestfactory.shared.EntityListRequest;
import com.google.gwt.requestfactory.shared.LongString;
import com.google.gwt.requestfactory.shared.RequestFactory;
import com.google.gwt.valuestore.shared.DeltaValueStore;
import com.google.gwt.valuestore.shared.ValueRef;

/**
 * Generated for the service methods of
 * com.google.gwt.sample.expenses.server.domain.
 */
public interface ExpenseRequestFactory extends RequestFactory {

  /**
   * Request selector.
   */
  interface EmployeeRequest {

    /**
     * @return a request object
     */
    EntityListRequest<EmployeeKey> findAllEmployees();
  }

  /**
   * Request selector.
   */
  interface ReportRequest {

    /**
     * @return a request object
     */
    EntityListRequest<ReportKey> findReportsByEmployee(
        @LongString ValueRef<EmployeeKey, String> id);

    /**
     * @return a request object
     */
    EntityListRequest<ReportKey> findAllReports();
  }

  /**
   * @return a request selector
   */
  EmployeeRequest employeeRequest();

  /**
   * @return a request selector based on new values
   */
  EmployeeRequest employeeRequest(DeltaValueStore deltas);

  /**
   * @return a request selector
   */
  ReportRequest reportRequest();

  /**
   * @return a request selector based on new values
   */
  ReportRequest reportRequest(DeltaValueStore deltas);
}
