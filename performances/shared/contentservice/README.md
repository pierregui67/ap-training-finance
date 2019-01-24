# Sandbox Content Server
================

Introduction
------------

This project illustrates various features of Content server.
This document pinpoints the various classes and files to explore to understand the project.


Content Server configuration
----------------------

This project only provides one class: `com.qfs.migration.ContentServerDbMigration` provides help method to migrate your Content server database from older version to 5.5 which now works with Hibernate 5.1

You may also take a look at 
- `com.qfs.sandbox.cfg.impl.LocalContentServiceConfig` in `full-server` module to see how to configure ActivePivot
to use an embedded Content server
- `com.qfs.sandbox.cfg.impl.RemoteContentServiceConfig` in `activepivot-server` module to see how to configure ActivePivot
to use a remote Content server