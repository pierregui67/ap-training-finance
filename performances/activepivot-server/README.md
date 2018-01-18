# ActivePivot Server
================

Introduction
------------

This sandbox starts a ActivePivot Server.
This project is NOT SELF-SUFFICIENT, it should be used with the Content Server,
as it needs a Remote Content Server to operates.
This document pinpoints the various classes and files to explore to understand the project.

Start the server
-----------------

Before the application can be start, it must be compiled with maven, to generate the needed resources and folders.

Run `mvn clean install` with failure. _You can skip tests if needed, using `-DskipTests=true` option._

You must first execute the class `com.qfs.sandbox.server.ContentServer` at `sandboxes/content-server`.
Once the Content Server is online, put the URL of the Content Server at `contentServer.remote.url` 
in the file `content.service.properties`. Run the class `com.qfs.sandbox.server.PushToContentServer` with user **admin** (pass: "admin").
After the application finishes running, connect to the Content Server UI, at [localhost:9091/content](http://localhost:9091/content),
verify that the data entry of pivot exists.

Then run the class `com.qfs.sandbox.server.ActivePivotServer`, located with test sources. This starts a Jetty server, offering
ActivePivot APIs. You can connect your ActiveUI to this ActivePivot server using [localhost:9090](http://localhost:9090).

This starts a Jetty server, offering:
- ActivePivot XMLA APIs., at [localhost:9090/xmla](http://localhost:9090/xmla),

You can also go to [localhost:9090/webservices](http://localhost:9090/webservices) for an exhausted list of web services
provided by the server.

(Optional)
If you happen to have ActiveMonitor run on a separate server (for example, ActiveMonitor Server).
You can modify the file `activemonitor.service.properties` to connect your ActiveMonitor with this ActivePivot Server by 
specifying these properties: 
- `sentinel.remote.url`
- `repository.remote.url`
- `activepivot.snl.url`
- `live.snl.url`

ActivePivot configuration (cf. sandbox-activepivot at sandboxes/shared/activepivot)
----------------------

ActivePivot Server project is only configured using Spring Configuration classes.
One single class centralized all imports made by the project.

See `com.qfs.sandbox.cfg.impl.ActivePivotServerConfig` as a starting point to discover all defined beans.
Consult imported classes Javadoc to get a description of the beans, their names and purposes.

Other configuration classes:

- 	`com.qfs.sandbox.cfg.impl.RemoteActiveMonitorConfig` configuration for working with a remote ActiveMonitor server
- 	`com.qfs.sandbox.cfg.impl.RemoteContentServiceConfig` and `com.qfs.sandbox.cfg.impl.RemoteI18nConfig` configuration 
	for working with a remote Content server
- 	`com.qfs.sandbox.cfg.impl.ActivePivotServerSecurityConfig` for the application security configuration, particularly users.
