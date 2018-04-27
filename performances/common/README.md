# Sandbox Common
================

Introduction
------------

This project keeps the common configuration classes for all the sandbox project.
This document pinpoints the various classes and files to explore to understand the project.

Resources
-----------

This project contains the resources to configure Content service and JWT filter. The configuration resource for
Content service is also used to configure ActivePivot server, thus it is kept in the project sandbox-common
instead of sandbox-contentservice.
The `jwt.properties` will be copied to all WARs (Full server and all the standalone servers)
The `hibernate.properties` will be copied to the WARs of Full server and standalone content server
These resources will be copied and put at the root of the WAR when it is created.
You can take a look at the POM of these two servers to see how it is done by `maven-dependency-plugin`