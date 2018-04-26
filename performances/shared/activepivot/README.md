# Sandbox ActivePivot
================

Introduction
------------

This project illustrates various features of ActivePivot. 
This document pinpoints the various classes and files to explore to understand the project.


ActivePivot configuration
----------------------

ActivePivot sandbox project is only configured using Spring Configuration classes. One single class centralized all
imports made by the project.

See `com.qfs.sandbox.cfg.impl.ActivePivotServerConfig` or `com.qfs.sandbox.cfg.impl.FullServerConfig` 
as a starting point to discover all defined beans.
Consult imported classes Javadoc to get a description of the beans, their names and purposes.

2 classes are interesting to look at first:

- `com.qfs.sandbox.cfg.impl.DatastoreConfig` for Datastore configuration
- `com.qfs.sandbox.source.impl.SourceConfig` for Data source configuration

Resources
-----------

The resources used to configure the ActivePivot application (cube descriptions, context values, etc.) can be found at
`src/main/resources`. They are shared by Full server and Standalone ActivePivot server.
They will be copied and put at the root of the WAR when it is created.
You can take a look at the POM of these two servers to see how it is done by `maven-dependency-plugin`