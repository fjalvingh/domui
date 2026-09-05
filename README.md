DomUI
=====

DomUI is a server-side Java web UI framework. A page is a Java class building a
server-side DOM tree; the framework renders it, and after every event it diffs the
tree and pushes only the changes to the browser as AJAX deltas. No Javascript,
templates or REST plumbing are needed to write an application.

* Documentation and tutorial: [https://domui.org/](https://domui.org/)
* Live demo application: [https://demo.domui.org/](https://demo.domui.org/)

Building
--------

DomUI builds with **Java 21** and Maven 3.9:

```bash
mvn clean install
```

Parts of the build are generated (grammars, the property annotation processor), so a
full Maven build must have run once before an IDE build works. Compilation uses the
Eclipse batch compiler, which IntelliJ must be told to use as well.

To run the demo application locally on
[http://localhost:8088/demo/](http://localhost:8088/demo/):

```bash
mvn jetty:run -pl to.etc.domui.demo
```

Getting started
---------------

To start a new application, use the
[domui-skeleton](https://github.com/fjalvingh/domui-skeleton) repository as a
starting point.
