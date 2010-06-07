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
package com.google.gwt.cell.client;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * Tests for {@link ActionCell}.
 */
public class ActionCellTest extends CellTestBase<String> {

  /**
   * A mock {@link Delegate} used for testing.
   * 
   * @param <T> the value type
   */
  private static class MockDelegate<T> implements Delegate<T> {
    private T lastObject;

    public void assertLastObject(T expected) {
      assertEquals(expected, lastObject);
    }

    public void execute(T object) {
      assertNull(lastObject);
      lastObject = object;
    }
  }

  public void testOnBrowserEvent() {
    MockDelegate<String> delegate = new MockDelegate<String>();
    ActionCell<String> cell = new ActionCell<String>("hello", delegate);
    Element parent = Document.get().createDivElement();
    NativeEvent event = Document.get().createClickEvent(0, 0, 0, 0, 0, false,
        false, false, false);
    assertNull(cell.onBrowserEvent(parent, "test", null, event, null));
    delegate.assertLastObject("test");
  }

  @Override
  protected boolean consumesEvents() {
    return true;
  }

  @Override
  protected Cell<String> createCell() {
    Delegate<String> delegate = new MockDelegate<String>();
    return new ActionCell<String>("clickme", delegate);
  }

  @Override
  protected String createCellValue() {
    return "ignored";
  }

  @Override
  protected boolean dependsOnSelection() {
    return false;
  }

  @Override
  protected String getExpectedInnerHtml() {
    return "<button>clickme</button>";
  }

  @Override
  protected String getExpectedInnerHtmlNull() {
    // ActionCell always renders the same message.
    return "<button>clickme</button>";
  }
}
