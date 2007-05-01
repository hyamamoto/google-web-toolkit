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

function __MODULE_FUNC__() {
  // ---------------- INTERNAL GLOBALS ----------------
  
  // Cache symbols locally for good obfuscation
  var $wnd = window
  ,$doc = document
  ,external = $wnd.external
  
  // If non-empty, an alternate base url for this module
  ,base = ''
  
  // A map of properties that were declared in meta tags
  ,metaProps = {}
  
  // Maps property names onto sets of legal values for that property.
  ,values = []
  
  // Maps property names onto a function to compute that property.
  ,providers = []
  
  // A multi-tier lookup map that uses actual property values to quickly find
  // the strong name of the cache.js file to load.
  ,answers = []

  // Error functions.  Default unset in compiled mode, may be set by meta props.
  ,onLoadErrorFunc, propertyErrorFunc
  
  ; // end of global vars

  // ------------------ TRUE GLOBALS ------------------

  // Maps to synchronize the loading of styles and scripts; resources are loaded
  // only once, even when multiple modules depend on them.  This API must not
  // change across GWT versions.
  if (!$wnd.__gwt_stylesLoaded) { $wnd.__gwt_stylesLoaded = {}; }
  if (!$wnd.__gwt_scriptsLoaded) { $wnd.__gwt_scriptsLoaded = {}; }

  // --------------- INTERNAL FUNCTIONS ---------------

  function isHostedMode() {
    return (external && external.gwtOnLoad &&
        ($wnd.location.search.indexOf('gwt.hybrid') == -1));
  }
  
  // Determine our own script's URL via magic :)
  //
  function computeScriptBase() {
    // see if gwt.js left a marker for us
    var thisScript, markerScript;

    // try writing a marker
    $doc.write('<script id="__gwt_marker___MODULE_NAME__"></script>');
    markerScript = $doc.getElementById("__gwt_marker___MODULE_NAME__");
    if (markerScript) {
      // this script should be the previous element
      thisScript = markerScript.previousSibling;
    }

    if (thisScript) {
      // Compute our base url
      var content = thisScript.src;
      if (content) {
        var eq = content.lastIndexOf('/');
        if (eq >= 0) {
          base = content.substring(0, eq + 1);
        }
      }
    }
    
    if (markerScript) {
      // remove the marker element
      markerScript.parentNode.removeChild(markerScript);
    }
  }
  
  // Called to slurp up all <meta> tags:
  // gwt:property, gwt:onPropertyErrorFn, gwt:onLoadErrorFn
  //
  function processMetas() {
    var metas = document.getElementsByTagName('meta');
    for (var i = 0, n = metas.length; i < n; ++i) {
      var meta = metas[i], name = meta.getAttribute('name'), content;
  
      if (name) {
        if (name == 'gwt:property') {
          content = meta.getAttribute('content');
          if (content) {
            var value, eq = content.indexOf('=');
            if (eq >= 0) {
              name = content.substring(0, eq);
              value = content.substring(eq+1);
            } else {
              name = content;
              value = '';
            }
            metaProps[name] = value;
          }
        } else if (name == 'gwt:onPropertyErrorFn') {
          content = meta.getAttribute('content');
          if (content) {
            try {
              propertyErrorFunc = eval(content);
            } catch (e) {
              alert('Bad handler \"' + content +
                '\" for \"gwt:onPropertyErrorFn\"');
            }
          }
        } else if (name == 'gwt:onLoadErrorFn') {
          content = meta.getAttribute('content');
          if (content) {
            try {
              onLoadErrorFunc = eval(content);
            } catch (e) {
              alert('Bad handler \"' + content + '\" for \"gwt:onLoadErrorFn\"');
            }
          }
        }
      }
    }
  }

  /**
   * Determines whether or not a particular property value is allowed. Called by
   * property providers.
   * 
   * @param propName the name of the property being checked
   * @param propValue the property value being tested
   */
  function __gwt_isKnownPropertyValue(propName, propValue) {
    return propValue in values[propName];
  }

  /**
   * Returns a meta property value, if any.  Used by DefaultPropertyProvider.
   */
  function __gwt_getMetaProperty(name) {
    var value = metaProps[name];
    return (value == null) ? null : value;
  }

  // Deferred-binding mapper function.  Sets a value into the several-level-deep
  // answers map. The keys are specified by a non-zero-length propValArray,
  // which should be a flat array target property values. Used by the generated
  // PERMUTATIONS code.
  //
  function unflattenKeylistIntoAnswers(propValArray, value) {
    var answer = answers;
    for (var i = 0, n = propValArray.length - 1; i < n; ++i) {
      // lazy initialize an empty object for the current key if needed
      answer = answer[propValArray[i]] || (answer[propValArray[i]] = []);
    }
    // set the final one to the value
    answer[propValArray[n]] = value;
  }
  
  // Computes the value of a given property.  propName must be a valid property
  // name. Used by the generated PERMUTATIONS code.
  //
  function computePropValue(propName) {
    var value = providers[propName](), allowedValuesMap = values[propName];
    if (value in allowedValuesMap) {
      return value;
    }
    var allowedValuesList = [];
    for (var k in allowedValuesMap) {
      allowedValuesList[allowedValuesMap[k]] = k;
    }
    if (propertyErrorFunc) {
      propertyErrorFunc(propName, allowedValuesList, value);
    }
    throw null;
  }
  
  // --------------- PROPERTY PROVIDERS ---------------

// __PROPERTIES_BEGIN__
// __PROPERTIES_END__

  // --------------- EXPOSED FUNCTIONS ----------------

  // Called when the compiled script identified by moduleName is done loading.
  //
  __MODULE_FUNC__.onScriptLoad = function(gwtOnLoad) {
    // remove this whole function from the global namespace to allow GC
    __MODULE_FUNC__ = null;
    gwtOnLoad(onLoadErrorFunc, '__MODULE_NAME__');
  }

  // --------------- STRAIGHT-LINE CODE ---------------

  // do it early for compile/browse rebasing
  computeScriptBase();
  
// __SHELL_SERVLET_ONLY_BEGIN__
  if (!isHostedMode()) {
    // Force shell servlet to serve compiled output for web mode
    $doc.write('<script src="' + base + '__MODULE_NAME__.nocache-xs.js?compiled"></script>');
    return;
  } else {
    // This script cannot run hosted mode properly; redirect to the html version
    // TODO: figure out how to run hosted mode in the main window
    $doc.write('<script src="' + base + '__MODULE_NAME__.nocache.js"></script>');
    return;
  }
// __SHELL_SERVLET_ONLY_END__

  processMetas();

  var strongName;
  try {
// __PERMUTATIONS_BEGIN__
    // Permutation logic
// __PERMUTATIONS_END__
  } catch (e) {
    // intentionally silent on property failure
    return;
  }  
  strongName += '.cache.js';
// __MODULE_DEPS_BEGIN__
  // Module dependencies, such as scripts and css
// __MODULE_DEPS_END__
  $doc.write('<script src="' + base + strongName + '"></script>');
}

// Called from compiled code to hook the window's resize & load events (the
// code running in the script frame is not allowed to hook these directly).
// 
// Notes:
// 1) We declare it here in the global scope so that it won't closure the
// internals of the module func.
//
// 2) We hang it off the module func to avoid polluting the global namespace.
//
// 3) This function will be copied directly into the script namespace.
//
__MODULE_FUNC__.__gwt_initHandlers = function(resize, beforeunload, unload) {
  var $wnd = window
  , oldOnResize = $wnd.onresize
  , oldOnBeforeUnload = $wnd.onbeforeunload
  , oldOnUnload = $wnd.onunload
  ;

  $wnd.onresize = function() {
   resize();
   if (oldOnResize)
     oldOnResize();
  };
  
  $wnd.onbeforeunload = function() {
   var ret = beforeunload();
  
   var oldRet;
   if (oldOnBeforeUnload)
     oldRet = oldOnBeforeUnload();
  
   if (ret !== null)
     return ret;
   return oldRet;
  };
  
  $wnd.onunload = function() {
   unload();
   if (oldOnUnload)
     oldOnUnload();
  };
}

__MODULE_FUNC__();
