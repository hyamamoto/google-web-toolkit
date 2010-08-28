
var $wnd = parent;
var $doc = $wnd.document;
var $moduleName = window.name;
var $moduleBase, $entry, $errFn
,$stats = $wnd.__gwtStatsEvent ? function(a) {return $wnd.__gwtStatsEvent(a);} : null
,$sessionId = $wnd.__gwtStatsSessionId ? $wnd.__gwtStatsSessionId : null;
// Lightweight metrics
if ($stats) {
  $stats({moduleName:$moduleName,sessionId:$sessionId,subSystem:'startup',evtGroup:'moduleStartup',millis:(new Date()).getTime(),type:'moduleEvalStart'});
}
var $hostedHtmlVersion="2.1";

var gwtOnLoad;

function loadIframe(url) {
  var topDoc = window.top.document;

  // create an iframe
  var iframeDiv = topDoc.createElement("div");
  iframeDiv.innerHTML = "<iframe scrolling=no frameborder=0 src='" + url + "'>";
  var iframe = iframeDiv.firstChild;
  
  // mess with the iframe style a little
  var iframeStyle = iframe.style;
  iframeStyle.position = "absolute";
  iframeStyle.borderWidth = "0";
  iframeStyle.left = "0";
  iframeStyle.top = "0";
  iframeStyle.width = "100%";
  iframeStyle.backgroundColor = "#ffffff";
  iframeStyle.zIndex = "1";
  iframeStyle.height = "100%";

  // update the top window's document's body's style
  var hostBodyStyle = window.top.document.body.style; 
  hostBodyStyle.margin = "0";
  hostBodyStyle.height = iframeStyle.height;
  hostBodyStyle.overflow = "hidden";

  // insert the iframe
  topDoc.body.insertBefore(iframe, topDoc.body.firstChild);
}

function getCodeServer() {
  var server = "localhost:9997";
  var query = $wnd.location.search;
  var idx = query.indexOf("gwt.codesvr=");
  if (idx >= 0) {
    idx += 12;  // "gwt.codesvr=".length() == 12
  }
  if (idx >= 0) {
    var amp = query.indexOf("&", idx);
    if (amp >= 0) {
      server = query.substring(idx, amp);
    } else {
      server = query.substring(idx);
    }
    // According to RFC 3986, some of this component's characters (e.g., ':')
    // are reserved and *may* be escaped.
    return decodeURIComponent(server);
  }
}

function doBrowserSpecificFixes() {
  var ua = navigator.userAgent.toLowerCase();
  if (ua.indexOf("gecko") != -1) {
    // install eval wrapper on FF to avoid EvalError problem
    var __eval = window.eval;
    window.eval = function(s) {
      return __eval(s);
    }
  }
  if (ua.indexOf("chrome") != -1) {
    // work around __gwt_ObjectId appearing in JS objects
    var hop = Object.prototype.hasOwnProperty;
    Object.prototype.hasOwnProperty = function(prop) {
      return prop != "__gwt_ObjectId" && hop.call(this, prop);
    };
    // do the same in our parent as well -- see issue 4486
    // NOTE: this will have to be changed when we support non-iframe-based DevMode
    var hop2 = parent.Object.prototype.hasOwnProperty;
    parent.Object.prototype.hasOwnProperty = function(prop) {
      return prop != "__gwt_ObjectId" && hop2.call(this, prop);
    };
  }
}

// wrapper to call JS methods, which we need both to be able to supply a
// different this for method lookup and to get the exception back
function __gwt_jsInvoke(thisObj, methodName) {
  try {
    var args = Array.prototype.slice.call(arguments, 2);
    return [0, window[methodName].apply(thisObj, args)];
  } catch (e) {
    return [1, e];
  }
}

var __gwt_javaInvokes = [];
function __gwt_makeJavaInvoke(argCount) {
  return __gwt_javaInvokes[argCount] || __gwt_doMakeJavaInvoke(argCount);
}

function __gwt_doMakeJavaInvoke(argCount) {
  // IE6 won't eval() anonymous functions except as r-values
  var argList = "";
  for (var i = 0; i < argCount; i++) {
    argList += ",p" + i;
  }
  var argListNoComma = argList.substring(1);

  return eval(
    "__gwt_javaInvokes[" + argCount + "] =\n" +
    "  function(thisObj, dispId" + argList + ") {\n" +
    "    var result = __static(dispId, thisObj" + argList + ");\n" +
    "    if (result[0]) {\n" +
    "      throw result[1];\n" +
    "    } else {\n" +
    "      return result[1];\n" +
    "    }\n" +
    "  }\n"
  ); 
}

/*
 * This is used to create tear-offs of Java methods.  Each function corresponds
 * to exactly one dispId, and also embeds the argument count.  We get the "this"
 * value from the context in which the function is being executed.
 * Function-object identity is preserved by caching in a sparse array.
 */
var __gwt_tearOffs = [];
var __gwt_tearOffGenerators = [];
function __gwt_makeTearOff(proxy, dispId, argCount) {
  return __gwt_tearOffs[dispId] || __gwt_doMakeTearOff(dispId, argCount);
}

function __gwt_doMakeTearOff(dispId, argCount) {
  return __gwt_tearOffs[dispId] = 
      (__gwt_tearOffGenerators[argCount] || __gwt_doMakeTearOffGenerator(argCount))(dispId);
}

function __gwt_doMakeTearOffGenerator(argCount) {
  // IE6 won't eval() anonymous functions except as r-values
  var argList = "";
  for (var i = 0; i < argCount; i++) {
    argList += ",p" + i;
  }
  var argListNoComma = argList.substring(1);

  return eval(
    "__gwt_tearOffGenerators[" + argCount + "] =\n" +
    "  function(dispId) {\n" +
    "    return function(" + argListNoComma + ") {\n" +
    "      var result = __static(dispId, this" + argList + ");\n" +
    "      if (result[0]) {\n" +
    "        throw result[1];\n" +
    "      } else {\n" +
    "        return result[1];\n" +
    "      }\n" +
    "    }\n" +
    "  }\n"
  ); 
}

function __gwt_makeResult(isException, result) {
  return [isException, result];
}

function __gwt_disconnected() {
  // Prevent double-invocation.
  window.__gwt_disconnected = new Function();
  // Do it in a timeout so we can be sure we have a clean stack.
  window.setTimeout(__gwt_disconnected_impl, 1);
}

function __gwt_disconnected_impl() {
  __gwt_displayGlassMessage('GWT Code Server Disconnected',
      'Most likely, you closed GWT Development Mode. Or, you might have lost '
      + 'network connectivity. To fix this, try restarting GWT Development Mode and '
      + '<a style="color: #FFFFFF; font-weight: bold;" href="javascript:location.reload()">'
      + 'REFRESH</a> this page.');
}

// Note this method is also used by ModuleSpace.java
function __gwt_displayGlassMessage(summary, details) {
  var topWin = window.top;
  var topDoc = topWin.document;
  var outer = topDoc.createElement("div");
  // Do not insert whitespace or outer.firstChild will get a text node.
  outer.innerHTML = 
    '<div style="position:absolute;z-index:2147483646;left:0px;top:0px;right:0px;bottom:0px;filter:alpha(opacity=75);opacity:0.75;background-color:#000000;"></div>' +
    '<div style="position:absolute;z-index:2147483647;left:50px;top:50px;width:600px;color:#FFFFFF;font-family:verdana;">' +
      '<div style="font-size:30px;font-weight:bold;">' + summary + '</div>' +
      '<p style="font-size:15px;">' + details + '</p>' +
    '</div>'
  ;
  topDoc.body.appendChild(outer);
  var glass = outer.firstChild;
  var glassStyle = glass.style;

  // Scroll to the top and remove scrollbars.
  topWin.scrollTo(0, 0);
  if (topDoc.compatMode == "BackCompat") {
    topDoc.body.style["overflow"] = "hidden";
  } else {
    topDoc.documentElement.style["overflow"] = "hidden";
  }

  // Steal focus.
  glass.focus();

  if ((navigator.userAgent.indexOf("MSIE") >= 0) && (topDoc.compatMode == "BackCompat")) {
    // IE quirks mode doesn't support right/bottom, but does support this.
    glassStyle.width = "125%";
    glassStyle.height = "100%";
  } else if (navigator.userAgent.indexOf("MSIE 6") >= 0) {
    // IE6 doesn't have a real standards mode, so we have to use hacks.
    glassStyle.width = "125%"; // Get past scroll bar area.
    // Nasty CSS; onresize would be better but the outer window won't let us add a listener IE.
    glassStyle.setExpression("height", "document.documentElement.clientHeight");
  }

  $doc.title = summary + " [" + $doc.title + "]";
}

function findPluginObject() {
  try {
    return document.getElementById('pluginObject');
  } catch (e) {
    return null;
  }
}

function findPluginEmbed() {
  try {
    return document.getElementById('pluginEmbed')
  } catch (e) {
    return null;
  }
}

function findPluginXPCOM() {
  try {
    return __gwt_HostedModePlugin;
  } catch (e) {
    return null;
  }
}

function generateSessionId() {
  var ASCII_EXCLAMATION = 33;
  var ASCII_TILDE = 126;
  var chars = [];
  for (var i = 0; i < 16; ++i) {
    chars.push(Math.floor(ASCII_EXCLAMATION
    + Math.random() * (ASCII_TILDE - ASCII_EXCLAMATION + 1)));
  }
  return String.fromCharCode.apply(null, chars);
}

function tryConnectingToPlugin(sessionId, url) {
  // Note that the order is important
  var pluginFinders = [
    findPluginXPCOM,
    findPluginObject,
    findPluginEmbed,
  ];
  var codeServer = getCodeServer();
  var plugin;
    for (var i = 0; i < pluginFinders.length; ++i) {
      try {
        plugin = pluginFinders[i]();
        if (plugin != null && plugin.init(window)) {
          if (!plugin.connect(url, sessionId, codeServer, $moduleName,
            $hostedHtmlVersion)) {
            pluginConnectionError(codeServer);
            return null;
          }
          return plugin;
        }
      } catch (e) {
    }
  }
  return null;
}

function pluginConnectionError(codeServer) {
  if ($errFn) {
    $errFn($moduleName);
  } else {
    alert("Plugin failed to connect to hosted mode server at " + codeServer);
    loadIframe("http://code.google.com/p/google-web-toolkit/wiki/TroubleshootingOOPHM");
  }
}

gwtOnLoad = function(errFn, modName, modBase){
  $moduleName = modName;
  $moduleBase = modBase;
  $errFn = errFn;

  var topWin = window.top;
  var url = topWin.location.href;
  if (!topWin.__gwt_SessionID) {
    topWin.__gwt_SessionID = generateSessionId();
  }
  var plugin = tryConnectingToPlugin(topWin.__gwt_SessionID, url);
  if (plugin == null) {
    loadIframe("http://gwt.google.com/missing-plugin/");
    return;
  }
  window.onUnload = function() {
    try {
      // wrap in try/catch since plugins are not required to supply this
      plugin.disconnect();
    } catch (e) {
    }
  };
};

window.onunload = function() {
};

// Lightweight metrics
window.fireOnModuleLoadStart = function(className) {
  $stats && $stats({moduleName:$moduleName, sessionId:$sessionId, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date()).getTime(), type:'onModuleLoadStart', className:className});
};

window.__gwt_module_id = 0;

// Lightweight metrics
$stats && $stats({moduleName:$moduleName, sessionId:$sessionId, subSystem:'startup', evtGroup:'moduleStartup', millis:(new Date()).getTime(), type:'moduleEvalEnd'});

doBrowserSpecificFixes();

// DevMode currently only supports iframe based linkers
var query = parent.location.search;
if (!findPluginXPCOM()) {
  document.write('<embed id="pluginEmbed" type="application/x-gwt-hosted-mode" width="10" height="10">');
  document.write('</embed>');
  document.write('<object id="pluginObject" CLASSID="CLSID:1D6156B6-002B-49E7-B5CA-C138FB843B4E">');
  document.write('</object>');
}

setTimeout(function() { $wnd[$moduleName].onScriptInstalled(gwtOnLoad) }, 1);

