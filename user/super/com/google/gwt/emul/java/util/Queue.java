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
package java.util;

/**
 * A collection designed for holding elements prior to processing.
 * 
 * @link http://java.sun.com/j2se/1.5.0/docs/api/java/util/Queue.html
 * 
 * @param <E> element type.
 */
public interface Queue<E> extends Collection<E> {
  
  public E element();
  
  public boolean offer(E o);
  
  public E peek();
  
  public E poll();
  
  public E remove();

}
