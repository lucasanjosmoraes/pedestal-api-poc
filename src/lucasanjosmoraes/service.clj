(ns lucasanjosmoraes.service
  (:require [io.pedestal.http :as http]
            [lucasanjosmoraes.handlers :as handlers]
            [lucasanjosmoraes.interceptors.http :as inc-http]
            [lucasanjosmoraes.interceptors.basic :as inc-basic]
            [lucasanjosmoraes.interceptors.database :as inc-db]
            [lucasanjosmoraes.interceptors.domain :as inc-domain]))

;; Routes

(def routes #{["/hi" :get [inc-http/coerce-body inc-http/content-negotiator handlers/respond-hi] :route-name :hi]
              ["/hello/:name" :get [inc-http/coerce-body inc-http/content-negotiator handlers/respond-hello] :route-name :hello]
              ["/echo"                    :get    inc-basic/echo :route-name :list-query-form]
              ["/todo"                    :post   [inc-http/coerce-body inc-http/content-negotiator inc-db/in-memory inc-domain/list-create]]
              ["/todo/:list-id"           :get    [inc-http/coerce-body inc-http/content-negotiator inc-domain/entity-render inc-db/in-memory inc-domain/list-view]]
              ["/todo/:list-id"           :post   [inc-http/coerce-body inc-http/content-negotiator inc-domain/entity-render inc-domain/list-item-view inc-db/in-memory inc-domain/list-item-create]]
              ["/todo/:list-id/:item-id"  :get    [inc-http/coerce-body inc-http/content-negotiator inc-domain/entity-render inc-domain/list-item-view inc-db/in-memory] :route-name :get-item]
              ["/todo/:list-id/:item-id"  :put    [inc-http/coerce-body inc-http/content-negotiator inc-domain/entity-render inc-domain/list-item-view inc-db/in-memory inc-domain/list-item-update]]
              ["/todo/:list-id/:item-id"  :delete [inc-http/coerce-body inc-http/content-negotiator inc-domain/entity-render inc-domain/list-view inc-db/in-memory inc-domain/list-item-delete]]})

;; Service

(def service {:env :prod
              ::http/routes routes
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})