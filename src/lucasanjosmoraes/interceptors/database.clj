(ns lucasanjosmoraes.interceptors.database
  (:require [clojure.spec.alpha :as s]
            [lucasanjosmoraes.domain :as domain]))

(s/def ::database (s/keys :opt-un [::domain/name ::domain/items]))
(s/def ::request (s/keys :opt-un [::database]))
(s/def ::tx-data (s/cat :fn ifn? :args (s/* any?)))
(s/def ::context (s/keys :req-un [::request]
                   :opt-un [::tx-data]))

(defonce database (atom {}))

(defn in-memory-enter
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context %)]}
  (update context :request assoc :database @database))

(s/fdef in-memory-enter
  :args (s/cat :context ::context)
  :ret ::context)

(defn in-memory-leave
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context %)]}
  (if-let [[op & args] (:tx-data context)]
    (do
      (apply swap! database op args)
      (assoc-in context [:request :database] @database))
    context))

(s/fdef in-memory-leave
  :args (s/cat :context ::context)
  :ret ::context)

(def in-memory
  {:name  ::database-interceptor
   :enter in-memory-enter
   :leave in-memory-leave})