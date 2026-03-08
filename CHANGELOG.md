# Changelog

While not ALL changes are outlined here, important changes that have potential impacts are covered.

## 1.2.2 (unreleased)

* CI modernization: Ubuntu 24.04 base, Java 11/17/21/25 matrix
* GitHub Actions release workflow, replacing Docker-based release process
* Migrate to Central Portal publishing
* Remove ByteBuffer mocking, use real instances (#144)
* Replace pmdumptext with pmrep, drop pcp-gui dependency
* Add CONTRIBUTING.md and developer setup documentation
* Container-based testing via test-in-container.sh
* Streamline README, move build-from-source details to CONTRIBUTING.md
* Monitorable caching performance improvement
* Fix race condition in metric updates with stopped writer
* Upgrade Mockito 5.13.0 → 5.22.0 for native JDK 25+ support, removing Byte Buddy experimental workaround (#145)
* Bump Jackson 2.17.2 → 2.18.6
* Bump CXF 4.0.5 → 4.0.7
* Bump XStream 1.4.20 → 1.4.21
* Bump Commons Lang3 3.16.0 → 3.18.0

## 1.2.1 (2024-11-29)

* Fix PcpMmvWriter memory leak
* Fix QuiescentRegistryListener leak
* Fix PcpMmvWriter strings leak
* Fix PcpMmvWriter race condition
* Upgrade hsqldb
* Fix parfait-agent packaging
* Dependency updates

## 1.2.0 (2023-12-15)

* Migrate parfait-cxf from cxf-api 2.x to cxf-core 4.x
* Bump Jackson from 2.11.2 to 2.16.0
* Bump Guava from 30.0-jre to 32.1.3-jre
* Bump XStream from 1.4.19 to 1.4.20
* Bump Spring to 5.3.27
* Java 11 build requirement (source/target remains Java 8)
* Fix deprecated warnings
* Update Mockito 3 → 4

## 1.1.1

* Fixed bug with UnitMapping #79
* Dependency updates (xstream via parfait-cxf)

## 1.1

* Update to use JSR 385 Units of Measure. This required a package name change in various places.

## 1.0

A variety of dependencies were upgraded:
* Xstream
* DropWizard
* Google Guava
* Spring
* Log4j (minor)
