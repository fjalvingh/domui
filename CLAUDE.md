# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DomUI is a server-side Java UI framework library. The main development branch is `skarp-master`.

## Build Commands

Use `mvn21` instead of `mvn` to ensure correct Java 21 environment:

```bash
# Full build
mvn21 clean install

# Build without tests
mvn21 clean install -DskipTests

# Compile specific module
mvn21 compile -pl to.etc.domui

# Compile module with dependencies
mvn21 compile -pl integrations/to.etc.domui.hibutil -am
```

## Running Tests

```bash
# Run all tests in a module
mvn21 test -pl to.etc.domui.demo

# Run specific test class
mvn21 test -pl to.etc.domui.demo -Dtest=TestDbQCriteria

# Run specific test method
mvn21 test -pl to.etc.domui.demo -Dtest=TestDbQCriteria#testExistsWith2ListsInSubquery

# Run tests matching pattern
mvn21 test -pl to.etc.domui.demo -Dtest="TestDbQCriteria#testExists*"
```

## Module Structure

Key modules:
- **to.etc.domui** - Core UI framework (components, data binding, AJAX)
- **to.etc.webapp.core** - Web application framework, includes QCriteria query abstraction
- **integrations/to.etc.domui.hibutil** - Hibernate/JPA integration, translates QCriteria to JPA Criteria
- **integrations/to.etc.domui.maxgraph** - `MaxGraphPanel`: diagrams drawn by maxGraph from a
  Java `GraphModel`. Ships a committed javascript bundle, so it builds without node; see
  `finished-plans/MAXGRAPH.md` for how it works and the module's README for rebuilding the bundle
- **to.etc.domui.demo** - Demo application and integration tests
- **common/** - Shared utilities (logging, database, algorithms, security)

## Architecture

### QCriteria Query System
The framework has its own query abstraction (`QCriteria`, `QSelection`) in `to.etc.webapp.core` that gets translated to JPA/Hibernate queries by `CriteriaCreatingVisitor` in the hibutil module. Key classes:
- `QCriteria` - Type-safe query builder
- `QExistsSubquery` - EXISTS subquery representation
- `CriteriaCreatingVisitor` - Translates QCriteria tree to JPA Criteria using visitor pattern

### Component System
UI components in `to.etc.domui` follow a server-side rendering model with AJAX updates. Components are in `to.etc.domui.component` and `to.etc.domui.component2` packages.

## Key Technical Details

- **Java Version**: 21
- **Kotlin Version**: 2.2.20
- **Hibernate**: 7.2.x with Jakarta EE
- **Test Framework**: JUnit 4
- **Build System**: Maven multi-module
- **Primary database**: PostgreSQL version 15+
- **Servlet API**: Jakarta EE (`jakarta.servlet`), on Jetty 11.0.26

## Running Projects

### Documentation & demo improvement (ongoing)

A multi-step improvement of the documentation website and the demo/tutorial
application, including removal of old and incorrect code and information. The
plan and current state live in **[IMPROVEMENT-PLAN.md](IMPROVEMENT-PLAN.md)**, which
holds only what is still open; the finished work and the decisions log are next to it
in **[IMPROVEMENT-LOG.md](IMPROVEMENT-LOG.md)**. Read the plan before working on
documentation or on `to.etc.domui.demo`, and keep both up to date: a finished item
moves to the log, and what it decided becomes an entry in the log's decisions log.

A plan that is finished as a whole moves to **`finished-plans/`**. What is there is
done and is kept for the account it gives of how a part of the framework now works -
`THEMES.md`, the theming write-up, is the first of them.

The documentation website source is a separate repository at
`/home/jal/git/update-domui/domui.github.io` (Markdown under `site/content`, static site
generator in `sitegenerator`).
