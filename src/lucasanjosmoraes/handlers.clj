(ns lucasanjosmoraes.handlers
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::status pos-int?)
(s/def ::body string?)

(s/def ::path-params (s/keys :req-un [::name]))
(s/def ::request-by-path (s/keys :req-un [::path-params]))

(s/def ::query-params (s/keys :req-un [::name]))
(s/def ::request-by-query (s/keys :req-un [::query-params]))

(s/def ::response (s/keys :req-un [::status ::body]))

(defn respond-hello
  [request]
  {:pre  [(s/valid? ::request-by-path request)]
   :post [(s/valid? ::response %)]}
  {:status 200
   :body   (str "Hello, "
             (get-in request [:path-params :name])
             "!")})

(s/fdef respond-hello
  :args (s/cat :request ::request-by-path)
  :ret ::response)

(defn respond-hi
  [request]
  {:pre  [(s/valid? ::request-by-query request)]
   :post [(s/valid? ::response %)]}
  {:status 200
   :body   (str "Hi, "
             (get-in request [:query-params :name])
             "!")})

(s/fdef respond-hi
  :args (s/cat :request ::request-by-query)
  :ret ::response)
