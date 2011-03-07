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
package com.google.gwt.dev.util.log.speedtracer;

import com.google.gwt.dev.json.JsonArray;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.dev.util.collect.Lists;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Logs performance metrics for internal development purposes. The output is
 * formatted so it can be opened directly in the SpeedTracer Chrome extension.
 * This class formats events using SpeedTracer's custom event feature. The html
 * file output can be viewed by using Chrome to open the file on a Chrome
 * browser that has the SpeedTracer extension installed.
 *
 * <p>
 * Enable logging by setting the system property {@code gwt.speedtracerlog} to
 * the output file path.
 * </p>
 *
 */
public final class SpeedTracerLogger {
  
  // Log file name (logging is enabled if this is non-null)
  private static final String logFile = 
    System.getProperty("gwt.speedtracerlog");
  
  // Allow a system property to override the default output format
  private static final String defaultFormatString = 
    System.getProperty("gwt.speedtracerformat");
  
  // Use cummulative multi-threaded process cpu time instead of wall time
  private static final boolean logProcessCpuTime = 
    getBooleanProperty("gwt.speedtracer.logProcessCpuTime");
  
  // Use per thread cpu time instead of wall time
  private static final boolean logThreadCpuTime = 
    getBooleanProperty("gwt.speedtracer.logThreadCpuTime");
  
  // Turn on logging summarizing gc time during an event
  private static final boolean logGcTime =
    getBooleanProperty("gwt.speedtracer.logGcTime");
  
  // Turn on logging estimating overhead used for speedtracer logging.
  private static final boolean logOverheadTime =
    getBooleanProperty("gwt.speedtracer.logOverheadTime");

  // Disable logging of JSNI calls and callbacks to reduce memory usage where
  // the heap is already tight.
  private static final boolean jsniCallLoggingEnabled = 
      !getBooleanProperty("gwt.speedtracer.disableJsniLogging");

  /**
   * Represents a node in a tree of SpeedTracer events.
   */
  public class Event {
    protected final EventType type;
    List<Event> children;
    List<String> data;
    long durationNanos;
    final long startTimeNanos;

    Event() {
      if (enabled) {
        timeKeeper.resetTimeBase();
        this.startTimeNanos = timeKeeper.normalizedTimeNanos();
        this.data = Lists.create();
        this.children = Lists.create();
      } else {
        this.startTimeNanos = 0L;
        this.data = null;
        this.children = null;
      }
      this.type = null;
    }
    
    Event(Event parent, EventType type, String... data) {
      this(timeKeeper.normalizedTimeNanos(), parent, type, data);
    }
    
    Event(long startTimeNanos, Event parent, EventType type, String... data) {
      if (parent != null) {
        parent.children = Lists.add(parent.children, this);
      }
      this.type = type;
      assert (data.length % 2 == 0);
      this.data = Lists.create(data);
      this.children = Lists.create();
      this.startTimeNanos = startTimeNanos;
    }

    /**
     * @param data key/value pairs to add to JSON object.
     */
    public void addData(String... data) {
      if (data != null) {
        assert (data.length % 2 == 0);
        this.data = Lists.addAll(this.data, data);
      }
    }

    /**
     * Signals the end of the current event.
     */
    public void end(String... data) {
      endImpl(this, data);
    }

    @Override
    public String toString() {
      return type.getName();
    }

    JsonObject toJson() {
      JsonObject json = JsonObject.create();
      json.put("type", -2);
      json.put("typeName", type.getName());
      json.put("color", type.getColor());
      double startMs = convertToMilliseconds(startTimeNanos);
      json.put("time", startMs);
      double durationMs = convertToMilliseconds(durationNanos);
      json.put("duration", durationMs);

      JsonObject jsonData = JsonObject.create();
      for (int i = 0; i < data.size(); i += 2) {
        jsonData.put(data.get(i), data.get(i + 1));
      }
      json.put("data", jsonData);

      JsonArray jsonChildren = JsonArray.create();
      for (Event child : children) {
        jsonChildren.add(child.toJson());
      }
      json.put("children", jsonChildren);

      return json;
    }
  }

  /**
   * Enumerated types for logging events implement this interface.
   */
  public interface EventType {
    String getColor();

    String getName();
  }

  static enum Format {
    /**
     * Standard SpeedTracer log that includes JSON wrapped in HTML that will
     * launch a SpeedTracer monitor session.
     */
    HTML,

    /**
     * Only the JSON data without any HTML wrappers.
     */
    RAW
  }

  /**
   * A dummy implementation to do nothing if logging has not been turned on.
   */
  private class DummyEvent extends Event {
    @Override
    public void addData(String... data) {
      // do nothing
    }

    @Override
    public void end(String... data) {
      // do nothing
    }

    @Override
    public String toString() {
      return "Dummy";
    }
  }
  
  private interface NormalizedTimeKeeper {
    long normalizedTimeNanos();
    void resetTimeBase();
    long zeroTimeMillis();
  }

  /*
   * Time keeper which uses wall time.
   */
  private class DefaultNormalizedTimeKeeper implements NormalizedTimeKeeper {

    private final long zeroTimeNanos;
    private final long zeroTimeMillis;

    public DefaultNormalizedTimeKeeper() {
      zeroTimeNanos = System.nanoTime();
      zeroTimeMillis = (long) convertToMilliseconds(zeroTimeNanos);
    }

    public long normalizedTimeNanos() {
      return System.nanoTime() - zeroTimeNanos;
    }
    
    public void resetTimeBase() {
    }
    
    public long zeroTimeMillis() {
      return zeroTimeMillis;
    }
  }

  /*
   * Time keeper which uses process cpu time.  This can be greater than wall
   * time, since it is cummulative over the multiple threads of a process.
   */
  private class ProcessNormalizedTimeKeeper implements NormalizedTimeKeeper {
    private final OperatingSystemMXBean osMXBean;
    private final Method getProcessCpuTimeMethod;
    private final long zeroTimeNanos;
    private final long zeroTimeMillis;
    
    public ProcessNormalizedTimeKeeper() {
      try {
        osMXBean = ManagementFactory.getOperatingSystemMXBean();
        /* 
         * Find this method by reflection, since it's part of the Sun
         * implementation for OperatingSystemMXBean, and we can't alwayws assume
         * that com.sun.management.OperatingSystemMXBean will be available.
         */
        getProcessCpuTimeMethod = 
          osMXBean.getClass().getMethod("getProcessCpuTime");
        getProcessCpuTimeMethod.setAccessible(true);
        zeroTimeNanos = (Long) getProcessCpuTimeMethod.invoke(osMXBean);
        zeroTimeMillis = (long) convertToMilliseconds(zeroTimeNanos);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    
    public long normalizedTimeNanos() {
      try {
        return (Long) getProcessCpuTimeMethod.invoke(osMXBean) - zeroTimeNanos;
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
    
    public void resetTimeBase() {
    }
    
    public long zeroTimeMillis() {
      return zeroTimeMillis;
    }
  }

  /*
   * Time keeper which uses per thread cpu time.  It is assumed that individual
   * events logged will be single threaded, and that top-level events will call
   * {@link #resetTimeBase()} prior to logging time.  The resettable time base
   * is needed since each individual thread starts its timing at 0, regardless
   * of when the thread is created.  So we reset the time base at the beginning
   * of an event, so that we can generate a chronologically representative
   * output, although the relation to wall time is actually compressed within
   * a logged event (thread cpu time does not include wait time, etc.).
   */
  private class ThreadNormalizedTimeKeeper implements NormalizedTimeKeeper {

    private final ThreadMXBean threadMXBean;
    private final ThreadLocal<Long> resettableTimeBase = new ThreadLocal<Long>();
    private final long zeroTimeNanos;
    private final long zeroTimeMillis;

    public ThreadNormalizedTimeKeeper() {
      threadMXBean = ManagementFactory.getThreadMXBean();
      if (!threadMXBean.isCurrentThreadCpuTimeSupported()) {
        throw new RuntimeException("Current thread cpu time not supported");
      }
      zeroTimeNanos = System.nanoTime();
      zeroTimeMillis = (long) convertToMilliseconds(zeroTimeNanos);
    }

    public long normalizedTimeNanos() {
      return threadMXBean.getCurrentThreadCpuTime() + resettableTimeBase.get();
    }
    
    public void resetTimeBase() {
      /*
       * Since all threads start individually at time 0L, we use this to
       * offset each event's time so we can generate chronological output.
       */
      resettableTimeBase.set(System.nanoTime() 
          - zeroTimeNanos - threadMXBean.getCurrentThreadCpuTime());
    }
    
    public long zeroTimeMillis() {
      return zeroTimeMillis;
    }
  } 

  /**
   * Initializes the singleton on demand.
   */
  private static class LazySpeedTracerLoggerHolder {
    public static SpeedTracerLogger singleton = new SpeedTracerLogger();
  }

  /**
   * Thread that converts log requests to JSON in the background.
   */
  private class LogWriterThread extends Thread {
    private static final int FLUSH_TIMER_MSECS = 10000;
    private final String fileName;
    private final BlockingQueue<Event> threadEventQueue;
    private final Writer writer;

    public LogWriterThread(
        Writer writer, String fileName, final BlockingQueue<Event> eventQueue) {
      super();
      this.writer = writer;
      this.fileName = fileName;
      this.threadEventQueue = eventQueue;
    }

    @Override
    public void run() {
      long nextFlush = System.currentTimeMillis() + FLUSH_TIMER_MSECS;
      try {
        while (true) {
          Event event =
              threadEventQueue.poll(nextFlush - System.currentTimeMillis(),
                  TimeUnit.MILLISECONDS);
          if (event == null) {
            // ignore.
          } else if (event == shutDownSentinel) {
            break;
          } else if (event == flushSentinel) {
            writer.flush();
            flushLatch.countDown();
          } else {
            JsonObject json = event.toJson();
            json.write(writer);
            writer.write('\n');
          }
          if (System.currentTimeMillis() >= nextFlush) {
            writer.flush();
            nextFlush = System.currentTimeMillis() + FLUSH_TIMER_MSECS;
          }
        }
        // All queued events have been written.
        if (outputFormat.equals(Format.HTML)) {
          writer.write("</div></body></html>\n");
        }
        writer.close();
      } catch (InterruptedException ignored) {
      } catch (IOException e) {
        System.err.println("Unable to write to gwt.speedtracerlog '"
            + (fileName == null ? "" : fileName) + "'");
        e.printStackTrace();
      } finally {
        shutDownLatch.countDown();
      }
    }
  }

  /**
   * Records a LOG_MESSAGE type of SpeedTracer event.
   */
  private class MarkTimelineEvent extends Event {
    public MarkTimelineEvent(Event parent) {
      super();
      if (parent != null) {
        parent.children = Lists.add(parent.children, this);
      }
    }

    @Override
    JsonObject toJson() {
      JsonObject json = JsonObject.create();
      json.put("type", 11);
      double startMs = convertToMilliseconds(startTimeNanos);
      json.put("time", startMs);
      json.put("duration", 0.0);
      JsonObject jsonData = JsonObject.create();
      for (int i = 0; i < data.size(); i += 2) {
        jsonData.put(data.get(i), data.get(i + 1));
      }
      json.put("data", jsonData);
      return json;
    }
  }

  /**
   * Annotate the current event on the top of the stack with more information.
   * The method expects key, value pairs, so there must be an even number of
   * parameters.
   *
   * @param data JSON property, value pair to add to current event.
   */
  public static void addData(String... data) {
    SpeedTracerLogger.get().addDataImpl(data);
  }

  /**
   * Create a new global instance. Force the zero time to be recorded and the
   * log to be opened if the default logging is turned on with the <code>
   * -Dgwt.speedtracerlog</code> VM property.
   *
   * This method is only intended to be called once.
   */
  public static void init() {
    get();
  }

  /**
   * Returns true if JSNI calls and callbacks are being logged.
   */
  public static boolean jsniCallLoggingEnabled() {
    return jsniCallLoggingEnabled;
  }
  
  /**
   * Adds a LOG_MESSAGE SpeedTracer event to the log. This represents a single
   * point in time and has a special representation in the SpeedTracer UI.
   */
  public static void markTimeline(String message) {
    SpeedTracerLogger.get().markTimelineImpl(message);
  }

  /**
   * Signals that a new event has started. You must end each event for each
   * corresponding call to {@code start}. You may nest timing calls.
   *
   * @param type the type of event
   * @data a set of key-value pairs (each key is followed by its value) that
   *       contain additional information about the event
   * @return an Event object to be closed by the caller
   */
  public static Event start(EventType type, String... data) {
    return SpeedTracerLogger.get().startImpl(type, data);
  }

  private static double convertToMilliseconds(long nanos) {
    return nanos / 1000000.0d;
  }

  /**
   * For accessing the logger as a singleton, you can retrieve the global
   * instance. It is prudent, but not necessary to first initialize the
   * singleton with a call to {@link #init()} to set the base time.
   *
   * @return the current global {@link SpeedTracerLogger} instance.
   */
  private static SpeedTracerLogger get() {
    return LazySpeedTracerLoggerHolder.singleton;
  }
  
  private static boolean getBooleanProperty(String propName) {
    try {
      return System.getProperty(propName) != null;
    } catch (RuntimeException ruEx) {
      return false;
    }
  }

  private final boolean enabled;
  
  private final DummyEvent dummyEvent;

  private final BlockingQueue<Event> eventsToWrite;

  private CountDownLatch flushLatch;

  private final Event flushSentinel;

  private final Format outputFormat;

  private final ThreadLocal<Stack<Event>> pendingEvents;
  
  private final CountDownLatch shutDownLatch;

  private final Event shutDownSentinel;

  private final List<GarbageCollectorMXBean> gcMXBeans;
  
  private final Map<String, Long> lastGcTimes;

  private final NormalizedTimeKeeper timeKeeper;

  /**
   * Constructor intended for unit testing.
   *
   * @param writer alternative {@link Writer} to send speed tracer output.
   */
  SpeedTracerLogger(Writer writer, Format format) {
    enabled = true;
    outputFormat = format;
    eventsToWrite = openLogWriter(writer, "");
    pendingEvents = initPendingEvents();
    timeKeeper = initTimeKeeper();
    gcMXBeans = null;
    lastGcTimes = null;
    shutDownSentinel = new DummyEvent();
    flushSentinel = new DummyEvent();
    shutDownLatch = new CountDownLatch(1);
    dummyEvent = null;
  }

  private SpeedTracerLogger() {
    // Enabled flag (will be true if logFile is non-null)
    this.enabled = logFile != null;
    
    if (enabled) {
      // Allow a system property to override the default output format
      Format format = Format.HTML;
      if (defaultFormatString != null) {
        for (Format value : Format.values()) {
          if (value.name().toLowerCase().equals(defaultFormatString.toLowerCase())) {
            format = value;
            break;
          }
        }
      }
      
      outputFormat = format;
      eventsToWrite = openDefaultLogWriter();
      pendingEvents = initPendingEvents();
      timeKeeper = initTimeKeeper();
      
      if (logGcTime) {
        gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        lastGcTimes = new ConcurrentHashMap<String, Long>();
      } else {
        gcMXBeans = null;
        lastGcTimes = null;
      }
      
      shutDownSentinel = new DummyEvent();
      flushSentinel = new DummyEvent();
      shutDownLatch = new CountDownLatch(1);
      dummyEvent = null;
    } else {
      outputFormat = null;
      eventsToWrite = null;
      pendingEvents = null;
      timeKeeper = null;
      gcMXBeans = null;
      lastGcTimes = null;
      shutDownSentinel = null;
      flushSentinel = null;
      shutDownLatch = null;
      dummyEvent = new DummyEvent();
    }
  }

  public void addDataImpl(String... data) {
    Stack<Event> threadPendingEvents = pendingEvents.get();
    if (threadPendingEvents.isEmpty()) {
      throw new IllegalStateException(
          "Tried to add data to an event that never started!");
    }

    Event currentEvent = threadPendingEvents.peek();
    currentEvent.addData(data);
  }

  public void markTimelineImpl(String message) {
    Stack<Event> threadPendingEvents = pendingEvents.get();
    Event parent = null;
    if (!threadPendingEvents.isEmpty()) {
      parent = threadPendingEvents.peek();
    }
    Event newEvent = new MarkTimelineEvent(parent);
    threadPendingEvents.push(newEvent);
    newEvent.end("message", message);
  }
  
  void addGcEvents(Event refEvent) {
    for (java.lang.management.GarbageCollectorMXBean gcMXBean : gcMXBeans) {
      String gcName = gcMXBean.getName();
      Long lastGcTime = lastGcTimes.get(gcName);
      long currGcTime = gcMXBean.getCollectionTime();
      if (lastGcTime == null) {
        lastGcTime = 0L;
      }
      if (currGcTime > lastGcTime) {
        // create a new event
        long gcDurationNanos = (currGcTime - lastGcTime) * 1000000L;
        long gcStartTimeNanos = refEvent.startTimeNanos + refEvent.durationNanos
          - gcDurationNanos;
        Event gcEvent = new Event(gcStartTimeNanos, null, 
            SpeedTracerEventType.GC, "Collector Type", gcName, 
            "Cummulative Collection Count", Long.toString(gcMXBean.getCollectionCount()));
        gcEvent.durationNanos = gcDurationNanos;
        eventsToWrite.add(gcEvent);
        
        lastGcTimes.put(gcName, currGcTime);
      }
    }
  }
  
  void addOverheadEvent(Event refEvent) {
    long overheadStartTime = refEvent.startTimeNanos + refEvent.durationNanos;
    Event overheadEvent = 
      new Event(overheadStartTime, refEvent, SpeedTracerEventType.OVERHEAD);
    overheadEvent.durationNanos = 
      timeKeeper.normalizedTimeNanos() - overheadStartTime;
    refEvent.durationNanos += overheadEvent.durationNanos;
  }

  void endImpl(Event event, String... data) {
    if (!enabled) {
      return;
    }

    long endTimeNanos = timeKeeper.normalizedTimeNanos();
    
    if (data.length % 2 == 1) {
      throw new IllegalArgumentException("Unmatched data argument");
    }
    
    Stack<Event> threadPendingEvents = pendingEvents.get();
    if (threadPendingEvents.isEmpty()) {
      throw new IllegalStateException(
          "Tried to end an event that never started!");
    }
    Event currentEvent = threadPendingEvents.pop();

    assert (endTimeNanos >= currentEvent.startTimeNanos);
    currentEvent.durationNanos = endTimeNanos - currentEvent.startTimeNanos;

    while (currentEvent != event && !threadPendingEvents.isEmpty()) {
      // Missed a closing end for one or more frames! Try to sync back up.
      currentEvent.addData("Missed",
          "This event was closed without an explicit call to Event.end()");
      currentEvent = threadPendingEvents.pop();
      assert (endTimeNanos >= currentEvent.startTimeNanos);
      currentEvent.durationNanos = endTimeNanos - currentEvent.startTimeNanos;
    }

    if (threadPendingEvents.isEmpty() && currentEvent != event) {
      currentEvent.addData(
          "Missed", "Fell off the end of the threadPending events");
    }
    
    if (logGcTime) {
      addGcEvents(currentEvent);
    }

    currentEvent.addData(data);
    
    if (logOverheadTime) {
      addOverheadEvent(currentEvent);
    }
    
    if (threadPendingEvents.isEmpty()) {
      eventsToWrite.add(currentEvent);
    }
  }

  /**
   * Notifies the background thread to finish processing all data in the queue.
   * Blocks the current thread until the data is flushed in the Log Writer
   * thread.
   */
  void flush() {
    try {
      // Wait for the other thread to drain the queue.
      flushLatch = new CountDownLatch(1);
      eventsToWrite.add(flushSentinel);
      flushLatch.await();
    } catch (InterruptedException e) {
      // Ignored
    }
  }

  Event startImpl(EventType type, String... data) {
    if (!enabled) {
      return dummyEvent;
    }

    if (data.length % 2 == 1) {
      throw new IllegalArgumentException("Unmatched data argument");
    }

    Stack<Event> threadPendingEvents = pendingEvents.get();
    Event parent = null;
    if (!threadPendingEvents.isEmpty()) {
      parent = threadPendingEvents.peek();
    } else {
      // start new time base for top-level events
      timeKeeper.resetTimeBase();
    }
    
    Event newEvent = new Event(parent, type, data);
    // Add a field to the top level event in order to  track the base time
    // so we can re-normalize the data
    if (threadPendingEvents.size() == 0) {
      newEvent.addData("baseTime", "" + timeKeeper.zeroTimeMillis());
    }
    threadPendingEvents.push(newEvent);
    return newEvent;
  }

  private ThreadLocal<Stack<Event>> initPendingEvents() {
    return new ThreadLocal<Stack<Event>>() {
      @Override
      protected Stack<Event> initialValue() {
        return new Stack<Event>();
      }
    };
  }
  
  private NormalizedTimeKeeper initTimeKeeper() {
    if (logProcessCpuTime) {
      return new ProcessNormalizedTimeKeeper();
    } else if (logThreadCpuTime) {
      return new ThreadNormalizedTimeKeeper();
    } else {
      return new DefaultNormalizedTimeKeeper();
    } 
  }
  
  private BlockingQueue<Event> openDefaultLogWriter() {
    Writer writer = null;
    if (enabled) {
      try {
        writer = new BufferedWriter(new FileWriter(logFile));
        return openLogWriter(writer, logFile);
      } catch (IOException e) {
        System.err.println(
            "Unable to open gwt.speedtracerlog '" + logFile + "'");
        e.printStackTrace();
      }
    }
    return null;
  }

  private BlockingQueue<Event> openLogWriter(
      final Writer writer, final String fileName) {
    try {
      if (outputFormat.equals(Format.HTML)) {
        writer.write(
                "<HTML isdump=\"true\"><body>"
                + "<style>body {font-family:Helvetica; margin-left:15px;}</style>"
                + "<h2>Performance dump from GWT</h2>"
                + "<div>This file contains data that can be viewed with the "
                + "<a href=\"http://code.google.com/speedtracer\">SpeedTracer</a> "
                + "extension under the <a href=\"http://chrome.google.com/\">"
                + "Chrome</a> browser.</div><p><span id=\"info\">"
                + "(You must install the SpeedTracer extension to open this file)</span></p>"
                + "<div style=\"display: none\" id=\"traceData\" version=\"0.17\">\n");
      }
    } catch (IOException e) {
      System.err.println("Unable to write to gwt.speedtracerlog '"
          + (fileName == null ? "" : fileName) + "'");
      e.printStackTrace();
      return null;
    }

    final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>();
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          // Wait for the other thread to drain the queue.
          eventQueue.add(shutDownSentinel);
          shutDownLatch.await();
        } catch (InterruptedException e) {
          // Ignored
        }
      }
    });

    // Background thread to write SpeedTracer events to log
    Thread logWriterWorker = new LogWriterThread(writer, fileName, eventQueue);

    // Lower than normal priority.
    logWriterWorker.setPriority(
        (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);

    /*
     * This thread must be daemon, otherwise shutdown hooks would never begin to
     * run, and an app wouldn't finish.
     */
    logWriterWorker.setDaemon(true);
    logWriterWorker.setName("SpeedTracerLogger writer");
    logWriterWorker.start();
    return eventQueue;
  }
}
