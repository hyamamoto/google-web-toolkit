/*
 * Copyright 2007 Google Inc.
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
package com.google.gwt.user.client.impl;

import com.google.gwt.user.client.Event;

/**
 * Opera implementation of {@link com.google.gwt.user.client.impl.DOMImpl}.
 */
public class DOMImplOpera extends DOMImplStandard {

  public native int eventGetButton(Event evt) /*-{
    // Opera and IE disagree on what the button codes for left button should be.
    // Translating to match IE standard.
    var button = evt.button;
    if(button == 0){
      return 1;
    } else {
      return button;
    }
  }-*/;

  public native int eventGetMouseWheelVelocityY(Event evt) /*-{
    return evt.detail * 4;
  }-*/;

}
