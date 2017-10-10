[//]: # ("Copyright (c) 2016-2017 Red Hat.")
[//]: # ("A markdown man page for Parfait.")
[//]: # ("Process with 'ronn' or similar: ")
[//]: # ("https://github.com/rtomayko/ronn")

parfait(1) -- Java instrumentation for Performance Co-Pilot (PCP)
=================================================================

## SYNOPSIS
`parfait` [`-n`/`--name` _name_] [`-c`/`--cluster` _id_] [`-i`/`--interval` _msec_] [`-s`/`--startup` _delay_] [`-j`/`--jmxserver` `--connect` _host:port_] [`--`] [`javaargs` ...]

## DESCRIPTION


The `parfait` wrapper script provides instrumentation for
an _unmodified_ Java application.  It is a front end to the
[Parfait](https://github.com/performancecopilot/parfait)
modules which developers use to instrument Java applications
directly.

`parfait` is thus limited to exposing performance metrics
that it can find indirectly, such as via JMX.

In order to be exported to the PCP Memory Mapped Values PMDA
(see `pmdammv`(1) for details)
these metrics must first be categorised with PCP metadata.

This is performed by configuration of the _parfait-agent.jar_
file used by `parfait`.  In the simplest form the configuration
is sourced from files in the _/etc/parfait_ directory.
Additional JMX managed beans can be added to the default set
through configuration files in the _/etc/parfait_ directory.

If this directory is empty, does not exist, or is otherwise
inaccessible, a minimal configuration is read from within the
resources of the _parfait-agent.jar_ file.

Configuration files must be in the JSON format - refer to the
[CONFIGURATION][] section below for details of the file format.

There are two forms of instrumentation available from the
`parfait` script - direct instrumentation (agent mode) or
via a JMX server (proxy mode).

See the [EXAMPLES][] section below for an example invocation
for each mode.

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
* pmdumptext
* pmchart
* pmie
* [... and many others ...]

The default mode of operation involves directly running the Java
process to be instrumented with a javaagent:

* _java_ -Dparfait.name=sleep -javaagent:/usr/share/java/parfait/parfait.jar -jar /usr/share/java/parfait/sleep.jar _Main_:
`PCP monitoring bridge started [PCP-TMP-DIR/mmv/sleep]]]; G'day World!`

The alternative is the proxy mode, where an already running Java
process is instrumented using its JMX server.
To start the Java application such that a JMX server is exposed,
use the following options:

* _java_ -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9875 -Dcom.sun.management.jmxremote.local.only=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar /usr/share/java/parfait/sleep.jar _Main_:
`G'day World!`

The JMX server is then allowing access from instrumentation by
`parfait`, which can be invoked as follows:

* _java_ -Dparfait.name=sleep -jar /usr/share/java/parfait/parfait.jar -connect=localhost:9875:
`PCP monitoring bridge started [PCP-TMP-DIR/mmv/sleep]]]`

## CONFIGURATION

TODO.

## FILES

* _$PCP-TMP-DIR/mmv/*_
default local of memory mapped values files created by `parfait`.
* _/etc/parfait/*.json_
configuration files defining metrics in the format described above.

## REFERENCES

[`PCPIntro`](http://man7.org/linux/man-pages/man1/pcpintro.1.html)
[`pmcd`](http://man7.org/linux/man-pages/man1/pmcd.1.html)
[`pmchart`](http://man7.org/linux/man-pages/man1/pmchart.1.html)
[`pmdammv`](http://man7.org/linux/man-pages/man1/pmdammv.1.html)
[`pmdumptext`](http://man7.org/linux/man-pages/man1/pmdumptext.1.html)
[`pmprobe`](http://man7.org/linux/man-pages/man1/pmprobe.1.html)
[`pminfo`](http://man7.org/linux/man-pages/man1/pminfo.1.html)
[`pmie`](http://man7.org/linux/man-pages/man1/pmie.1.html)
[`PMAPI`](http://man7.org/linux/man-pages/man3/PMAPI.3.html)
and
[`PMNS`](http://man7.org/linux/man-pages/man5/pmns.5.html)

## SEE ALSO

[`PCPIntro`](1),
[`pmcd`](1),
[`pmchart`](1)
[`pmdammv`](1)
[`pmdumptext`](1),
[`pmprobe`](1),
[`pminfo`](1),
[`pmie`](1),
[`PMAPI`](3),
and
[`PMNS`](5).
