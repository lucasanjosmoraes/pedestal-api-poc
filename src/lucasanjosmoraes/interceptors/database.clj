(ns lucasanjosmoraes.interceptors.database
  (:require [schema.core :as s]
            [lucasanjosmoraes.domain :as domain]))

(defonce database (atom {}))

(def Database
  {domain/ID domain/TODOList})

(def Request
  {(s/optional-key :database) Database
   s/Any                      s/Any})

(def Tx-Data [(s/one (s/pred ifn? 'ifn?) 'ifn?) s/Any])

(def Context
  {(s/optional-key :request) Request
   (s/optional-key :tx-data) Tx-Data
   s/Any                     s/Any})

(s/defn ^:always-validate in-memory-enter :- Context
  [context :- Context]
  (update context :request assoc :database @database))

(s/defn ^:always-validate in-memory-leave :- Context
  [context :- Context]
  (if-let [[op & args] (:tx-data context)]
    (do
      (apply swap! database op args)
      (assoc-in context [:request :database] @database))
    context))

(def in-memory
  {:name  ::database-interceptor
   :enter in-memory-enter
   :leave in-memory-leave})