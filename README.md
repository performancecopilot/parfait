![Parfait](https://raw.githubusercontent.com/performancecopilot/parfait/master/site/images/logo.jpg "Parfait Performance Monitoring")

Parfait is a performance monitoring library for Java which provides mechanisms for collecting counter and timing metrics, then exposing them through a variety of mechanisms (including JMX beans and the open-source cross-platform [Performance Co-Pilot](http://pcp.io) ).

[![Build Status](https://travis-ci.org/performancecopilot/parfait.svg?branch=master)](https://travis-ci.org/performancecopilot/parfait)

#Requirements

Parfait requires Java 7.  It has been used heavily with Oracle Java environments; other JVMs have to been tried.

#Maven Repository

Since release 0.2.0, all binary versions of parfait are synced to the Maven central repository. If you're using Maven for your project, you can automatically add dependencies to your project with no further configuration required. For example, to include the parfait-core library, simply use the POM snippet:

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
If you're not using Maven for your project, the JAR files (including source and Javadoc) may be downloaded from the appropriate subdirectory of the repository's parfait folder.

##About parfait
parfait is a performance management and monitoring framework for Java 1.6. parfait consists of several modules: a core monitoring subsytem, which defines a series of monitorable values and counters (which can be of any Java type) and some basic operations on these, along with standard interfaces for exporting these values to various 'data sinks' as they change over time. The parfait project defines a few common data sinks for monitoring the current state of these values, exporting them to JMX and SGI's PCP monitoring system. There are also a number of modules built on top of this core system, which enable the collection of metrics from common data sources (such as JDBC drivers and other JMX beans) to provide easy monitoring of existing Java subsystems.

##The Layers of parfait
parfait is, like an ogre, made up of many layers. parfait provides several modules assisting with the various stages of metrics collection and performance monitoring (integration, collection, output), and each module is a separate Maven subproject (and hence, separate .jar artifact). The following diagram illustrates the provided core modules, along with their key responsibilities and some interactions between them:

![Parfait Layers](https://raw.githubusercontent.com/performancecopilot/parfait/master/site/images/parfait-layers.png "Parfait Layers")

##parfait-core
parfait-core is the main module of parfait. It consists of two Java packages, com.custardsource.parfait and com.custardsource.parfait.timing. Let's look at each in more detail:

##com.custardsource.parfait
This package represents the very essence of the parfait system. Fundamentally, it defines two things: a set of basic types used to encapsulate monitored values (such as the number of active user sessions, total database query execution count, and so on), and a very simple output mechanism for same. This provides a simple, powerful, and flexible way of collecting values from a variety of sources and collating them to different monitoring systems.

##MonitorableRegistry?
A MonitorableRegistry is merely a collection of monitorable values: a 'suite' of metrics which can be output to a particular source. Most output views on your metrics will take a single MonitorableRegistry as their metric source, and operate on all the metrics within that registry. Think of this as a related group of metrics for a single application; multiple applications in the same JVM, for example, may have completely disparate MonitorableRegistries.

Note that most metric values are aware of the 'default' MonitorableRegistry?, a singleton for use in situations with a single globally-accessible MonitorableRegistry? context.

##Monitorables
The most important class in parfait is Monitorable<T>. This encapsulates all the key metadata for an observable monitored value, and can be used by the various output subsystems to display, log, or otherwise use the value in an appropriate way.

A Monitorable has a few important key properties:

name a machine-readable name for the metric. This is freeform text, but is typically of a log4j-style dotted hierarchical String (e.g. java.vm.gc.count). It is recommended to follow this format as later work will likely enable conditional output etc. for different branches of the hierarchy<

description a human-readable description of the metric's meaning and origin.

unit a JSR-275 measurement unit for the metric. This may or may not be used by various output formats, but is intended to provide additional context and comparisons between units (e.g. between B/s and KB/s)

valueSemantics the semantics of the individual unit value -- is it a free-running point-in-time value, a constant, or a monotonically increasing counter?

In reality, it is unlikely that you will need to implement Monitorable directly. There are a few simple classes which cover the vast majority of your needs:

MonitoredCounter is a Monitorable<Long> which covers values with increasing-counter semantics. An example is a metric for 'total number of user logins since app startup'. A MonitoredCounter may not be decremented, and should only ever increase over time:

    MonitoredCounter counter = new MonitoredCounter("user.logins", "# of user logins since app startup");

    // ... more ...

    public void logUserOn() {
        counter.inc();
        // ... more ...
    }
MonitoredValue<T> is a Monitorable<T> which is used for free-running point-in-time values which can be set arbitrarily. For the common cases of integer and long values, the subclasses MonitoredIntValue and MonitoredLongValue are useful shortcuts, and provide increment and decrement methods.

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
PollingMonitoredValue<T> uses a Timer and a callback mechanism to poll for Monitorable values from sources which do not provide asynchronous notifications.

##MonitoringView?
A MonitoringView is the bridge between the core parfait libraries and the various possible output mechanisms. A particular parfait output library will typically implement its own MonitoringView, which is responsible for initializing the monitoring output and being notified (via a Monitor) of any changes to the Monitorables it obtains from the provided MonitorableRegistry.