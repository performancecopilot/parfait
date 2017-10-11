parfait(1) -- Java instrumentation for Performance Co-Pilot (PCP)
=================================================================

## SYNOPSIS
`parfait` [`-n`/`--name` _name_] [`-c`/`--cluster` _id_] [`-i`/`--interval` _msec_] [`-s`/`--startup` _delay_] [`-j`/`--jmxserver` `--connect` _host:port_] [`--`] [`javaargs` ...]

## DESCRIPTION

The `parfait` wrapper script provides instrumentation for
an _unmodified_ Java application.  It is a front end to the
[Parfait](https://github.com/performancecopilot/parfait)
modules which developers use to access instrumentation from
Java applications.

The `parfait` script is limited to exposing performance metrics
that it can find indirectly, such as via JMX.

In order to be exported to the PCP Memory Mapped Values PMDA
(see `pmdammv`(1) for details)
these metrics must first be categorized with PCP metadata.

This is performed by configuration of the _parfait-agent.jar_
file used by `parfait`.  In the simplest form the configuration
is sourced from files in the _/etc/parfait_ directory.
Additional JMX managed beans can be added to the default set
through configuration files in the _/etc/parfait_ directory.

If this directory is empty, does not exist, or is otherwise
inaccessible, a minimal configuration is read from within the
resources of the _parfait-agent.jar_ file.

Configuration files must be in the JSON format - refer to the
CONFIGURATION section below for details of the file format.

There are two forms of instrumentation available from the
`parfait` script - direct instrumentation (agent mode) or
via a JMX server (proxy mode).

See the EXAMPLES section below for a sample invocation for
both modes.

## OPTIONS

The command line options available are:

* `-n` / `--name` _name_:
The _name_ argument specifies the _mmv.*_ metric tree name in
the `PMNS`(5) that will be used to identify this application.

* `-c` / `--cluster` _identifier_:
The numeric performance metric cluster _identifier_ to be used to
uniquely identify this application.
A value of zero is the default, and causes the MMV PMDA to simply
use the next available number.

* `-i` / `--interval` _msec_:
Delay between sampling (JMX values in particular) to refresh the
values exported to PCP, in milliseconds.
The default value is 1000 (1 second) and the minimum allowed value
is 250 milliseconds.

* `-j` / `--jmxserver` / `--connect` _hostname:port_:
Connect to the JMX server listening on the specified
_hostname_ and _port_ number as the source of JMX metrics.
This option allows proxying of metrics from a separate process,
instead of the default `-javaagent` mode of operation.

* `-s` / `--startup` _msec_:
Maximum startup time in which JMX values are still being created,
before exporting as memory mapped values, in milliseconds.
The default value is 5000 (5 seconds).

## EXAMPLES

The following examples can be installed locally using the
**parfait-examples** package.

The affects of each example invocation below can be seen using
any PCP client tool, such as

* pminfo `-f` mmv
* pmprobe `-v` mmv
* pmchart
* pmie
* pmrep
* [... and many others ...]

The default mode of operation involves directly running the Java
process to be instrumented with a javaagent:

* _java_ -Dparfait.name=sleep **-javaagent**:/usr/share/java/parfait/parfait.jar -jar /usr/share/java/parfait/sleep.jar _Main_

The alternative is the proxy mode, where an already running Java
process is instrumented using its JMX server.
To start the Java application with a JMX server exposed, use the
following options:

* _java_ **-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9875** -Dcom.sun.management.jmxremote.local.only=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar /usr/share/java/parfait/sleep.jar _Main_

The JMX server is then allowing access from instrumentation by
`parfait`, which can be invoked as follows:

* _java_ -Dparfait.name=sleep -jar /usr/share/java/parfait/parfait.jar **-connect=localhost:9875**

## CONFIGURATION

The statically configured metrics used by **parfait-agent** are
configured using JSON configuration files.  The default used when
no files are present below _/etc/parfait_ is:

* [_jvm.json_](https://github.com/performancecopilot/parfait/tree/master/parfait-agent/src/main/resources/jvm.json)

This provides a handy reference for the semantic elements of the
JSON configuration, which are:

* The **metrics** array:
JSON array of individual metrics, must appear at the top level.

* Metric **name**:
A string which forms the dotted-form metric name, as well as the
optional PCP instance name in square brackets.

* Metric **description**:
An optional string providing explanatory help text for a metric.

* Metric **semantics**:
An optional string with one of the following values: _constant_
or _discrete_; _count_ or _counter_; and _gauge_, _instant_ or
_instantaneous_.  These map directly to the PCP metric semantics.
The value _instantaneous_ is the default.

* Metric **units**:
A string which will be parsed to produce the JSR-363 units for
the metric.  Currently _bytes_ or _milliseconds_ are supported.

* Whether the metric is **optional**:
A boolean (default: _false_) which flags whether this metric
must exist in the JVM.  Certain metrics only appear in some
situations, or some versions of the JVM, these should be marked
as **optional** metrics.

* Managed bean name **mBeanName**:
A string used to identify the Java managed bean backing this
metric in the JVM (e.g. _java.lang:type=Memory_).

* Managed bean attribute name **mBeanAttributeName**:
An optional string used to identify a specific attribute of a
managed bean (e.g. _HeapMemoryUsage_).

* Managed bean composite data item (**mBeanCompositeDataItem**):
An optional string used to further classify an individual value
of the managed bean attribute (e.g. _max_).

## FILES

* _$PCP-TMP-DIR/mmv/*_:
default local of memory mapped values files created by `parfait`.
* _/etc/parfait/*.json_:
configuration files defining metrics in the format described above.

## LINKS

[Parfait](https://github.com/performancecopilot/parfait):

* [Default metrics](https://github.com/performancecopilot/parfait/tree/master/parfait-agent/src/main/resources/jvm.json)

[Performance Co-Pilot](http://pcp.io):

* [`PCPIntro`](http://man7.org/linux/man-pages/man1/pcpintro.1.html)
* [`pmcd`](http://man7.org/linux/man-pages/man1/pmcd.1.html)
* [`pmchart`](http://man7.org/linux/man-pages/man1/pmchart.1.html)
* [`pmdammv`](http://man7.org/linux/man-pages/man1/pmdammv.1.html)
* [`pmie`](http://man7.org/linux/man-pages/man1/pmie.1.html)
* [`pminfo`](http://man7.org/linux/man-pages/man1/pminfo.1.html)
* [`pmprobe`](http://man7.org/linux/man-pages/man1/pmprobe.1.html)
* [`pmrep`](http://man7.org/linux/man-pages/man1/pmrep.1.html)
* [`PMAPI`](http://man7.org/linux/man-pages/man3/PMAPI.3.html)
* [`PMNS`](http://man7.org/linux/man-pages/man5/pmns.5.html)

## SEE ALSO

`PCPIntro`(1),
`pmcd`(1),
`pmchart`(1)
`pmdammv`(1)
`pmie`(1),
`pminfo`(1),
`pmprobe`(1),
`pmrep`(1),
`PMAPI`(3),
and
`PMNS`(5).
