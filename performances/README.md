# ActiveViam Sandboxes
================

Introduction
------------
This repository contains self sufficient projects.

You can use it:
- as starting point your new ActiveViam projects,
- to try out new features in ActiveViam products.

The organisation of this repository it as follow:

Directories view

sandboxes                             (sandbox-activeviam)
|
|----- activemonitor-server           (sandbox-activemonitor-server)
|----- content-server                 (sandbox-content-server)
|----- activepivot-server             (sandbox-activepivot-server)
|----- full-server                    (sandbox-full-server)
|----- shared
        |-----  common                (sandbox-common)
        |-----  activemonitor         (sandbox-activemonitor)
        |-----  contentservice        (sandbox-contentservice)
        |-----  activepivot           (sandbox-activepivot)

Artifacts view

sandbox-activeviam                    The parent for ActiveViam sanboxes
|
|-----  sandbox-common                Common configuration classes for ActiveViam applications
|-----  sandbox-activemonitor         Configuration classes for ActiveMonitor application
|-----  sandbox-contentservice        Configuration classes for Content Service application
|-----  sandbox-activepivot           Configuration classes for ActivePivot application
|-----  sandbox-activemonitor-server  Contains configuration needed to deploy a server containing only ActiveMonitor
|-----  sandbox-content-server        Contains configuration needed to deploy a server containing only Content Server
|-----  sandbox-activepivot-server    Contains configuration needed to deploy a server containing only ActivePivot
|-----  sandbox-full-server           Contains configuration needed to deploy ActiveMonitor, ActivePivot and Content Server on one server

Requirements
------------
Before using this repository, first ensure that:
-   You have the JDK version 8 (minimum u102), 64-bit
-   Maven can find the ActivePivot libraries in the Maven repository file
    that is part of the ActivePivot distribution called `repository-X.Y.Z.zip`.
    The easiest way of doing it is to unzip it and copy paste
    its contents to your Maven `.m2` directory.
-   the `ACTIVEPIVOT_LICENSE` environment variable contains the full path
    to your ActivePivot license file

Compile and run the tests with to make sure that your configuration works

    mvn clean install

Full server deployment
------------
You may want to start the Full server which runs all the applications on the same server.
Start a Jetty development server from `full-server` folder with:

    mvn exec:java -Dexec.mainClass="com.qfs.sandbox.server.FullServer"

To check that the server is up, visit: [http://admin:admin@localhost:9090/webservices](http://admin:admin@localhost:9090/webservices) on your browser.
You should see a list of available services.

To better visualize the server, you should try to connect a GUI front-endlike Excel or ActiveUI to it.
The latter is at [http://localhost:9090/ui](http://localhost:9090/ui)

Deployment on separate servers
------------
Once you are familiar with the sandbox applications: ActiveMonitor, ActivePivot, and Content server,
you may want to deploy them on separate servers:
-	For Content Server check the project `content-server`
-	For ActivePivot Server check the project `activepivot-server`
-	For ActiveMonitor Server check the project `activemonitor-server`

Please note that the ActivePivot Server needs the Content Server to execute,
please read the README.md in these two projects for proper configuration.
Also, an ActiveUI web application is embeded with the Content Server.

Production deployments on tomcat
------------
Copy the generated .war of the project that you want to deploy on tomcat to the `webapps`.
We currently recommend the Apache Tomcat 8 application servers for ActivePivot and ActiveViam 5.5+.
Make sure that:

-   Tomcat sees the `ACTIVEPIVOT_LICENSE` variable.

-   Tomcat allows the application to allocate enough memory.

You may also want to take a look at our recommendation page to tune the GC at [GC recommendations](https://support.quartetfs.com/confluence/display/AP5/GC+Recommendations)

If you are deploying Content Server or Full Server, do not forget 
to change the RSA key pair in `jwt.properties`.

More deployment information can be found at:
[Deployment & Admin](http://support.quartetfs.com/confluence/pages/viewpage.action?pageId=16549317)

Applications
------------
The Content Server provides
- ActiveUI, as a desktop application, at [http://localhost:9091/ui](http://localhost:9091/ui)
- Content service, at [http://localhost:9091/content](http://localhost:9091/content)

The ActiveMonitor Server provides
- ActiveMonitor and Repository services, at [http://localhost:8081](http://localhost:8081)

The ActivePivot Server provides
- ActivePivot service, at [http://localhost:9090](http://localhost:9090)

The Full Server provides
- ActiveUI, as a desktop application, at [http://localhost:9090/ui](http://localhost:9090/ui)
- Content service, at [http://localhost:9090/content](http://localhost:9090/content)
- ActivePivot services, ActiveMonitor and Repository services, at [http://localhost:9090](http://localhost:9090), (there are 
filters that redirect the HTTP requests to the corresponding service).

Embedding ActiveUI Web application
-----------
You may find the folder `src/main/webapp/activeui` in the projects that have embedded ActiveUI web application (Full server
and Content server).
However, it contains only index.html and favicon.ico but no script of the web application.
In fact, the application is integrated to the sandbox when the latter is built with maven.
More information about the maven integration of ActiveUI can be found at: [maven integration](http://support.activeviam.com/documentation/activeui/4.1.0-m3/start/maven-integration/)

Logging (for ActivePivot Server and Full Server)
-----------
To use the `logging.properties`:

    -Djava.util.logging.config.file=logging.properties

Web APIs
-----------
A variety of web services are offered, depending on the type of server your are running
To have the list of deployed services, go to:

- **http://localhost:9090/webservices** on ActivePivot server or Full server
- **http://localhost:8081/webservices** on ActiveMonitor server
- **http://localhost:9090/xmla** for XMLA services on ActivePivot server or Full server (to use with Excel)

## Additional documentation resources

ActiveMonitor sandbox submodule has its own README describing the features of the project.

You can browse the following spaces on Confluence to investigate many topics:

- https://support.activeviam.com/confluence/display/AP5/ActivePivot+5+Documentation
- https://support.activeviam.com/confluence/display/AM5/ActiveMonitor+Documentation
- https://support.activeviam.com/confluence/display/UI/ActiveUI+Documentation
