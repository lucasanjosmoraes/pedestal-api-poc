(ns lucasanjosmoraes.domain
  (:require [schema.core :as s]))

(def ID s/Str)

(def TODOListItem
  {:name  s/Str
   :done? s/Bool})

(def TODOList
  {:name  s/Str
   :items {ID TODOListItem}})

(s/defn make-list :- TODOList
  [nm :- s/Str]
  {:name  nm
   :items {}})

(s/defn make-list-item :- TODOListItem
  [nm :- s/Str]
  {:name  nm
   :done? false})

(s/defn update-list-item :- TODOListItem
  [item :- TODOListItem
   done :- s/Bool]
  (let [current-done (:done? item)]
    (if (= done current-done)
      item
      (assoc item :done? done))))
