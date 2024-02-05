# Silverpeas Kernel

__Silverpeas Kernel__ is the technical foundation upon which are built all the code of the
Silverpeas projects.
It provides common objects, technical APIs, and wrappers of libraries or frameworks about technical
need like, for example, logging or IoC by IoD (Inversion of Control by Injection of Dependency).

The goal of wrappers is to provide to the Silverpeas projects an API for usual tools and that are
agnostic to any solution providers so that the change of an implementation can easily transparently
be done without having to modify the code of the Silverpeas projects. Because __Silverpeas Kernel__
must be itself agnostic to any frameworks, its code must be built only upon the Java standard
library. This is why the implementation of the wrappers are loaded by the Java Service Provider
Interface (SPI).

The more important technical components provided by __Silverpeas Kernel__ are:

* An API to access the resources of the Silverpeas projects (l10n bundles, configuration
  properties, ...)
* A wrapper for logging. By default if no candidate is available by SPI, it is the Java Logging
  system that is used.
* A wrapper for IoC/IoD (Inversion of Control/Injection of Dependency). The wrappers is based upon
  the JSR-330 and as such it expects the implementation to be compliant to this specification.

Beside the code, __Silverpeas Kernel__ provides also a library to write unit tests based upon the 
APIs and the wrappers provided by its technical library. For doing, it provides a simple 
implementation of some of the wrappers as well as some Junit extensions to ease the access of 
resources in the tests.

To be less intrusive, __Silverpeas Kernel__ defines all of its dependencies on libraries as
_provided_, so any projects using __Silverpeas Kernel__ can use its own version of those 
dependencies.


