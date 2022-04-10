# pedestal-api-poc

POC of an HTTP API using [Pedestal](http://pedestal.io).

## About the project

- Useful dependencies
  - [prismatic/schema](https://github.com/plumatic/schema): declarative data description and validation.
- Basic profiles configuration;
- Default logback configuration;
- Default REPL configuration;
- [Branch with example of schema specification using clojure.spec](https://github.com/lucasanjosmoraes/pedestal-api-poc/tree/feature/type-with-spec)
  ;
- Configuration for dev and prod environments;
- Example of [Pedestal](http://pedestal.io) usage and its features with a basic CRUD;
- Example of HTTP negotiation and response coercion, native on Pedestal;
- Example of domain errors management;
- Example of `if-let` with multiple bindings;
- Missing `user` namespace configuration.

## Usage

### From command line

You can start the **API** in **DEV** or **PROD** mode with `lein run-dev` and `lein run` respectively. If you need to
debug your **API** using **REPL**, there are some commands available to help:

```clj
;; To start the API
(start-dev)

;; To stop
(stop-dev)

;; or restart
(restart)

;; You can use this helper to do a specific request
(test-request :post "/todo")
```

### From Intellij

There are scripts in the directory `.run` that allow you to run REPL, start **API** in **DEV** or **PROD** mode. You
just need to choose one of them from the dropdown menu and press play 🛀🏽.

## License

Copyright © 2021 Lucas dos Anjos Moraes

This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which
is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary Licenses when the conditions for such
availability set forth in the Eclipse Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your option) any later version, with the GNU
Classpath Exception which is available at https://www.gnu.org/software/classpath/license.html.
