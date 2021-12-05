(ns lucasanjosmoraes.server
  (:gen-class)
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [io.pedestal.test :as test]
            [lucasanjosmoraes.service :as service]))

(defonce custom-server (atom nil))

;; This is an adapted service map, that can be started and stopped
;; From the REPL you can call server/start and server/stop on this service
(defonce runnable-service (server/create-server service/service))

;; Helpers

(defn test-request [verb url]
  (test/response-for (::server/service-fn @custom-server) verb url))

(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& _]
  (println "\nCreating your [DEV] server...")
  (-> service/service
      (merge {:env :dev
              ;; do not block thread that starts web server
              ::server/join? false
              ;; Routes can be a function that resolve routes,
              ;;  we can use this to set the routes to be reloadable
              ::server/routes #(route/expand-routes (deref #'service/routes))
              ;; all origins are allowed in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
              ;; Content Security Policy (CSP) is mostly turned off in dev mode
              ::server/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})
      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))

(defn start-dev []
  (reset! custom-server
          (run-dev)))

(defn stop-dev []
  (server/stop @custom-server))

(defn restart []
  (stop-dev)
  (start-dev))

(defn -main
  "The entry-point for 'lein run'"
  [& _]
  (println "\nCreating your server...")
  (server/start runnable-service))
