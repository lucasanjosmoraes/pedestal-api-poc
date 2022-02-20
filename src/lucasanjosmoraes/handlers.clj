(ns lucasanjosmoraes.handlers
  (:require [schema.core :as s]))

(def Entity
  {(s/optional-key :name) s/Str})

(def Request
  {(s/optional-key :path-params)  Entity
   (s/optional-key :query-params) Entity
   s/Any                          s/Any})

(def Response
  {:status s/Int
   :body   s/Str})

(s/defn ^:always-validate respond-hello :- Response
  [request :- Request]
  {:status 200
   :body   (str "Hello, " '(get-in request [:path-params :name])
             "!")})

(s/defn ^:always-validate respond-hi :- Response
  [request :- Request]
  {:status 200
   :body   (str "Hi, "
             (get-in request [:query-params :name])
             "!")})
