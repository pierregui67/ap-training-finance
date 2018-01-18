# Content Server
================

Introduction
------------

This sandbox is a self sufficient project, starting a Content Server with an embeded ActiveUI.
We recommend this deployment strategy for any project that wants to deploy Content server on a server separately from their ActivePivot Server.
This project should be used with the ActivePivot Server, as the latter needs a Remote Content Server to operate.
This document pinpoints the various classes and files to explore to understand the project.

Start the server
-----------------

Before the application can be start, it must be compiled with maven, to generate the needed resources and folders.

Run `mvn clean install` with failure. _You can skip tests if needed, using `-DskipTests=true` option._
 
Then run the class `com.qfs.sandbox.server.ContentServer`, located with test sources. This starts a Jetty server, offering
Content service APIs, as well as Content Server UI, accessible at [localhost:9091/content](http://localhost:9091/content).
You can connect into the web application using users **admin** (pass: "admin").
It uses in-memory databases for all persisted data.

This server also exposes the ActiveUI, at [localhost:9091/ui](http://localhost:9091/ui).
You can connect into the web application using users **admin** (pass: "admin").
You can also connect it with an ActivePivot server or an ActiveMonitor server using the Add servers button.

Content Server configuration (cf. sandbox-contentservice at sandboxes/shared/contentservice)
----------------------

Content Server project is only configured using Spring Configuration classes.
One single class centralized all imports made by the project.

See `com.qfs.contentserver.cfg.impl.ContentServerConfig` as a starting point to discover all defined beans.
Consult imported classes Javadoc to get a description of the beans, their names and purposes.

See `com.qfs.contentserver.cfg.impl.ContentServerSecurityConfig` for security configuration for the Content server

Also take a look at `com.qfs.contentserver.cfg.impl.ContentServerSecurityConfig` for security configuration for
the application.

Embedding ActiveUI Web application
-----------
You may find the folder `src/main/webapp/activeui` in the projects that have embedded ActiveUI web application (Full server
and Content server).
However, it contains only index.html and favicon.ico but no script of the web application.
In fact, the application is integrated to the sandbox when building with maven.
More information about the maven integration of ActiveUI can be found at: [maven integration](http://support.activeviam.com/documentation/activeui/4.1.1/start/maven-integration/)

ActiveUI configuration
---------------------

You can directly add the URL of the ActivePivot server or the ActiveMonitor server that you want to use with the
Content server by modifying the file `index.html` at src/main/webapp/activeui/

Additional documentation
------------------------

As always, browse [Content Server Confluence article](https://support.activeviam.com/confluence/display/AP5/Content+Server)
for how to configure your Content server.
