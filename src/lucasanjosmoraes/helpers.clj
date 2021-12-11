(ns lucasanjosmoraes.helpers)

(def truthy? #{"true"})

(def falsy? #{"false"})

(defn str-is-boolean
  [str]
  (or (truthy? str) (falsy? str)))

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))