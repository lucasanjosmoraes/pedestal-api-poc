(defproject lucasanjosmoraes/pedestal-api-poc "0.1.0-SNAPSHOT"
  :description "POC of an HTTP API using Pedestal"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/data.json "2.4.0"]
                 [io.pedestal/pedestal.service "0.5.9"]
                 [io.pedestal/pedestal.jetty "0.5.9"]
                 [ch.qos.logback/logback-classic "1.2.10" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.32"]
                 [org.slf4j/jcl-over-slf4j "1.7.32"]
                 [org.slf4j/log4j-over-slf4j "1.7.32"]]
  :resource-paths ["config", "resources"]
  :main ^:skip-aot lucasanjosmoraes.server
  :plugins [[lein-ancient "1.0.0-RC3"]]
  :target-path "target/%s"
  :profiles {:dev     {:aliases      {"run-dev" ["trampoline" "run" "-m" "lucasanjosmoraes.server/run-dev"]}
                       :dependencies [[io.pedestal/pedestal.service-tools "0.5.9"]]}
             :uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
