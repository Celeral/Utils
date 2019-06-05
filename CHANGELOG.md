# Utils Change Log

All notable changes to Utils project will be documented in this file.

## [1.3.0][] (2019-01-04)

* Upgraded jackson-databind version to 2.9.9 from 2.9.8 to address a CVE
* Introduced instantiable Closeables to track serially allocated resources
* Introduced ThrowableFactory to instantiate Throwables with arbitrary constructors

## [1.2.0][] (2019-01-17)

* Upgraded jackson-databind version to 2.9.8 from 2.9.5 to address a few CVEs
* Introduced WeakIdentityHashMap

## [1.1.0][] (2018-06-21)

* Upgraded jackson-databind version to 2.9.6 from 2.9.5; Object2JSON is fully functional now
* Added ContextType to allow different flavors for resolution of the attribute values wrt parent context.
* Added NamedThreadFactory to create named theads

## 1.0.0 (2018-05-17)

* Initial release

[Semver]: http://semver.org
[Unreleased]: https://github.com/Celeral/Utils/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/Celeral/Utils/compare/v1.0.0...v1.1.0
[1.2.0]: https://github.com/Celeral/Utils/compare/v1.1.0...v1.2.0
[1.3.0]: https://github.com/Celeral/Utils/compare/v1.2.0...v1.3.0

