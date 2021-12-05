(ns lucasanjosmoraes.service
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            ;[io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.content-negotiation :as con-neg]))

;; Handlers

(defn respond-hello
  [request]
  (println (:accept request))
  {:status 200
   :body (str "Hello, "
              (get-in request [:path-params :name])
              "!")})

(defn respond-hi
  [request]
  {:status 200
   :body (str "Hi, "
              (get-in request [:query-params :name])
              "!")})

;; Interceptors

(def supported-types ["application/json" "text/plain"])

(def content-neg-intc (con-neg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/plain"       body
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (nil? (get-in context [:response :headers "Content-Type"]))
       (update-in [:response] coerce-to (accepted-type context))))})

;(def common-interceptors [(body-params/body-params) http/html-body])

;; Routes

(def routes #{["/hi" :get [coerce-body content-neg-intc respond-hi] :route-name :hi]
              ["/hello/:name" :get [coerce-body content-neg-intc respond-hello] :route-name :hello]})

;; Service

(def service {:env :prod
              ::http/routes routes
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})