(ns lucasanjosmoraes.domain
  (:require [clojure.spec.alpha :as s]
            [lucasanjosmoraes.helpers :as h]))

(s/def ::name string?)
(s/def ::done? boolean?)
(s/def ::parseable-to-done h/str-is-boolean?)
(s/def ::list-item (s/keys :opt-un [::name ::done?]))

(s/def ::items ::list-item)
(s/def ::list (s/keys :req-un [::name ::items]))

(defn parse-done
  [done-str]
  {:pre  [(s/valid? ::parseable-to-done done-str)]
   :post [(s/valid? ::done? %)]}
  (new Boolean done-str))

(s/fdef parse-done
  :args (s/cat :done-str ::parseable-to-done)
  :ret ::done?)

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
