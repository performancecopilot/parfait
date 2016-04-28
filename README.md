![Parfait](https://raw.githubusercontent.com/performancecopilot/parfait/master/site/images/logo.jpg "Parfait Performance Monitoring")

Parfait is a performance monitoring library for Java which extracts metrics and makes them available in a variety of ways (including JMX beans and the open-source cross-platform [Performance Co-Pilot](http://pcp.io)).

[![Build Status](https://travis-ci.org/performancecopilot/parfait.svg?branch=master)](https://travis-ci.org/performancecopilot/parfait)

# Requirements

Parfait requires Java 7.

# About parfait

Parfait consists of several modules:
- A core monitoring subsytem, which defines a series of monitorable values and counters (which can be of any Java type) and some basic operations on these
- Interfaces for exporting these values to various 'data sinks' as they change over time.  The *parfait* project defines a few common data sinks for monitoring the current state of these values, exporting them to JMX and the Performance Co-Pilot (PCP) monitoring system.
- A number of modules built on top of this core system, which enable the collection of metrics from common data sources (such as JDBC drivers and other JMX beans) to provide easy monitoring of existing Java subsystems.

# Quickstart

For a rapid deployment, the *parfait-agent* can be used in unmodified applications using the Java "-javaagent" command line option.

This mode uses many of the *parfait* modules described below, internally, so that you don't have to use the API right away. It accesses JMX and other metric values available from within the JVM and exports those as PCP Memory Mapped Value (MMV) metrics. This instrumentation mechanism is designed for use in production applications - it is proven, robust and very lightweight.

To build the standalone *parfait-agent* module, the Maven build contains an extra step:

    $ mvn clean package install
    $ pushd parfait-agent
    $ mvn assembly:single
    $ popd

Install to a well-known place:

    $ mkdir lib
    $ cp parfait-agent/target/parfait-agent-jar-with-dependencies.jar lib/parfait.jar
    $ export PARFAIT_HOME=`pwd`

To run an application with the *parfait-agent* loaded, a helper script is provided:

    $ bin/parfait [.sh|.bat] -- MyApplication

In PCP, new **mmv** metrics will then appear automatically for the duration of the instrumented Java application - these metrics can be recorded, charted, used for automated live and historical analysis, and so on, using PCP tools.

    $ pminfo --desc --fetch --helptext  mmv.MyApplication.java.jvm.compilation

    mmv.MyApplication.java.jvm.compilation
        Data Type: 64-bit int  InDom: PM_INDOM_NULL 0xffffffff
        Semantics: counter  Units: millisec
    Help:
    Time spent in the JVM doing Java bytecode compilation
        value 489

# The Layers of parfait

*parfait* is, like an ogre, made up of many layers. *parfait* provides several modules assisting with the various stages of metrics collection and performance monitoring (integration, collection, output), and each module is a separate Maven subproject (and hence, separate .jar artifact). The following diagram illustrates the provided core modules, along with their key responsibilities and some interactions between them:

![Parfait Layers](https://raw.githubusercontent.com/performancecopilot/parfait/master/site/images/parfait-layers.png "Parfait Layers")

## parfait-core
*parfait-core* is the main module of *parfait*.  Fundamentally, it defines two things: a set of basic types used to encapsulate monitored values (such as the number of active user sessions, total database query execution count, and so on), and a very simple output mechanism for same. This provides a simple, powerful, and flexible way of collecting values from a variety of sources and collating them to different monitoring systems.

### MonitorableRegistry
A *MonitorableRegistry* is merely a collection of monitorable values: a 'suite' of metrics which can be output to a particular source. Most output views on your metrics will take a single *MonitorableRegistry* as their metric source, and operate on all the metrics within that registry. Think of this as a related group of metrics for a single application; multiple applications in the same JVM, for example, may have completely disparate *MonitorableRegistry* objects.

Note that most metric values are aware of the **default** *MonitorableRegistry*, a singleton for use in situations with a single globally-accessible *MonitorableRegistry* context.

### Monitorables
The most important class in parfait is *Monitorable<T>*. This encapsulates all the key metadata for an observable monitored value, and can be used by the various output subsystems to display, log, or otherwise use the value in an appropriate way.

A *Monitorable* has a few important key properties:

- **name**
  - A machine-readable name for the metric.
  - This is freeform text, but is typically of a log4j-style dotted hierarchical String (e.g. java.vm.gc.count). It is recommended to follow this format as later work will likely enable conditional output etc. for different branches of the hierarchy.

- **description**
  - A human-readable description of the metric's meaning and origin.

- **unit**
  - A JSR-275 measurement unit for the metric.
  - This may or may not be used by various output formats, but is intended to provide additional context and comparisons between units (e.g. between B/s and KB/s)

- **valueSemantics**
  - The semantics of the individual unit value -- is it a free-running point-in-time value, a constant, or a monotonically increasing counter?

In reality, it is unlikely that you will need to implement *Monitorable* directly. There are a few simple classes which cover the vast majority of your needs:

- **MonitoredCounter**
  - A *Monitorable<Long>* which covers values with increasing-counter semantics.
  - An example is a metric for 'total number of user logins since app startup'.
  - A *MonitoredCounter* may not be decremented, and should only ever increase over time:

    MonitoredCounter counter = new MonitoredCounter("user.logins", "# of user logins since app startup");

    // ... more ...

    public void logUserOn() {
        counter.inc();
        // ... more ...
    }

- **MonitoredValue<T>**
  - A *Monitorable<T>* which is used for free-running point-in-time values which can be set arbitrarily.
  - For the common cases of integer and long values, the subclasses *MonitoredIntValue* and *MonitoredLongValue* are useful shortcuts, and provide increment and decrement methods.

    MonitoredIntValue inProgress = new MonitoredIntValue("emails.inprogress", "# of email sends currently in progress", 0);

    // ... more ...

    public synchronized void sendEmail() {
        inProgress.inc();
        // ... more ...
        inProgress.dec();
    }

    public synchronized void cancelInProgressEmails() {
        // ... more ...
        inProgress.set(0);
    }

- **PollingMonitoredValue<T>**
  - Uses a *Timer* and a callback mechanism to poll for *Monitorable* values from sources which do not provide asynchronous notifications.

### MonitoringView
A *MonitoringView* is the bridge between the core *parfait* libraries and the various possible output mechanisms.

A particular *parfait* output library will typically implement its own *MonitoringView*, which is responsible for initializing the monitoring output and being notified (via a *Monitor*) of any changes to the *Monitorable* objects it obtains from the provided *MonitorableRegistry*.

# Maven Repository

All binary versions of *parfait* are synced to the Maven central repository. If you're using Maven for your project, you can automatically add dependencies to your project with no further configuration required. For example, to include the *parfait-core* library, simply use the POM snippet:

    <project>
      ...
      <dependencies>
        ...
        <dependency>
          <groupId>com.custardsource.parfait</groupId>
          <artifactId>parfait-core</artifactId>
          <version>0.3.0.RC1</version>
        </dependency>
        ...
      <dependencies>
      ...
    <project>

If you are not using Maven for your project, the JAR files (including source and Javadoc) may be downloaded from the appropriate subdirectory of the repository's *parfait* folder.

