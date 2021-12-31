(ns lucasanjosmoraes.domain
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::done? boolean?)
(s/def ::list-item (s/keys :opt-un [::name ::done?]))

(s/def ::items ::list-item)
(s/def ::list (s/keys :req-un [::name ::items]))

(defn make-list
  [nm]
  {:pre  [(s/valid? ::name nm)]
   :post [(s/valid? ::list %)]}
  {:name  nm
   :items {}})

(s/fdef make-list
  :args (s/cat :nm ::name)
  :ret ::list)

(defn make-list-item
  [nm]
  {:pre  [(s/valid? ::name nm)]
   :post [(s/valid? ::list-item %)]}
  {:name  nm
   :done? false})

(s/fdef make-list-item
  :args (s/cat :nm ::name)
  :ret ::list-item)

(defn update-list-item
  [item done]
  {:pre [(s/valid? ::list-item item) (s/valid? ::done? done)]
   :pos [(s/valid? ::list-item %)]}
  (let [current-done (:done? item)]
    (if (= done current-done)
      item
      (assoc item :done? done))))

(s/fdef update-list-item
  :args (s/cat :item ::list-item
          :done ::done?)
  :ret ::list-item)
