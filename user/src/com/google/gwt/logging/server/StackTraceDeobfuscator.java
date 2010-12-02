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

package com.google.gwt.logging.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deobfuscates stack traces on the server side. This class requires that you
 * have turned on emulated stack traces and moved your symbolMap files to a
 * place accessible by your server. More concretely, you must compile with the
 * -extra command line option, copy the symbolMaps directory to somewhere your
 * server side code has access to it, and then set the symbolMapsDirectory in
 * this class through the constructor, or the setter method.
 * For example, this variable could be set to "WEB-INF/classes/symbolMaps/"
 * if you copied the symbolMaps directory to there or compiled your application
 * using <code>-extra war/WEB-INF/classes/</code>.
 *
 * TODO(unnurg): Combine this code with similar code in JUnitHostImpl
 */
public class StackTraceDeobfuscator {
  
  private static class SymbolMap extends HashMap<String, String> { }
  
  // From JsniRef class, which is in gwt-dev and so can't be accessed here
  // TODO(unnurg) once there is a place for shared code, move this to there.
  private static Pattern JsniRefPattern =
    Pattern.compile("@?([^:]+)::([^(]+)(\\((.*)\\))?");
  
  private File symbolMapsDirectory;
  
  private Map<String, SymbolMap> symbolMaps =
    new HashMap<String, SymbolMap>();

  /**
   * Constructor, which takes a <code>symbolMaps</code> directory as its
   * argument. Symbol maps can be generated using the <code>-extra</code> GWT
   * compiler argument.
   *
   * @param symbolMapsDirectory the <code>symbolMaps</code> directory with, or
   *          without trailing directory separator character
   */
  public StackTraceDeobfuscator(String symbolMapsDirectory) {
    setSymbolMapsDirectory(symbolMapsDirectory);
  }
  
  public LogRecord deobfuscateLogRecord(LogRecord lr, String strongName) {
    if (lr.getThrown() != null && strongName != null) {
      lr.setThrown(deobfuscateThrowable(lr.getThrown(), strongName));
    }
    return lr;
  }
  
  public StackTraceElement resymbolize(StackTraceElement ste,
      String strongName) {
    SymbolMap map = loadSymbolMap(strongName);
    String symbolData = map == null ? null : map.get(ste.getMethodName());

    if (symbolData != null) {
      // jsniIdent, className, memberName, sourceUri, sourceLine
      String[] parts = symbolData.split(",");
      if (parts.length == 5) {
        String[] ref = parse(
            parts[0].substring(0, parts[0].lastIndexOf(')') + 1));

        // parts[3] contains the source file URI or "Unknown"
        String filename = "Unknown".equals(parts[3]) ? null
            : parts[3].substring(parts[3].lastIndexOf('/') + 1);
        
        int lineNumber = ste.getLineNumber();
        /*
         * When lineNumber is zero, either because compiler.stackMode is not
         * emulated or compiler.emulatedStack.recordLineNumbers is false, use
         * the method declaration line number from the symbol map.
         */
        if (lineNumber == 0) {
          lineNumber = Integer.parseInt(parts[4]);
        }
        
        return new StackTraceElement(ref[0], ref[1], filename, lineNumber);
      }
    }
    // If anything goes wrong, just return the unobfuscated element
    return ste;
  }
  
  public void setSymbolMapsDirectory(String symbolMapsDirectory) {
    // permutations are unique, no need to clear the symbolMaps hash map
    this.symbolMapsDirectory = new File(symbolMapsDirectory);
  }
  
  private StackTraceElement[] deobfuscateStackTrace(
      StackTraceElement[] st, String strongName) {
    StackTraceElement[] newSt = new StackTraceElement[st.length];
    for (int i = 0; i < st.length; i++) {
      newSt[i] = resymbolize(st[i], strongName);
    }
    return newSt;
  }
  
  private Throwable deobfuscateThrowable(Throwable old, String strongName) {
    Throwable t = new Throwable(old.getMessage());
    if (old.getStackTrace() != null) {
      t.setStackTrace(deobfuscateStackTrace(old.getStackTrace(), strongName));
    } else {
      t.setStackTrace(new StackTraceElement[0]);
    }
    if (old.getCause() != null) {
      t.initCause(deobfuscateThrowable(old.getCause(), strongName));
    }
    return t;
  }
  
  private SymbolMap loadSymbolMap(
      String strongName) {
    SymbolMap toReturn = symbolMaps.get(strongName);
    if (toReturn != null) {
      return toReturn;
    }
    toReturn = new SymbolMap();
    String line;

    try {
      String filename = symbolMapsDirectory.getCanonicalPath()
          + File.separatorChar + strongName + ".symbolMap";
      BufferedReader bin = new BufferedReader(new FileReader(filename));
      try {
        while ((line = bin.readLine()) != null) {
          if (line.charAt(0) == '#') {
            continue;
          }
          int idx = line.indexOf(',');
          toReturn.put(new String(line.substring(0, idx)),
                       line.substring(idx + 1));
        }
      } finally {
        bin.close();
      }
    } catch (IOException e) {
      toReturn = null;
    }

    symbolMaps.put(strongName, toReturn);
    return toReturn;
  }
  
  private String[] parse(String refString) {
    Matcher matcher = JsniRefPattern.matcher(refString);
    if (!matcher.matches()) {
      return null;
    }
    String className = matcher.group(1);
    String memberName = matcher.group(2);
    String[] toReturn = new String[] {className, memberName};
    return toReturn;
  }
}
