(ns lucasanjosmoraes.repository
  (:require [schema.core :as s]
            [lucasanjosmoraes.interceptors.database :as database]
            [lucasanjosmoraes.domain :as domain]))

(def NilableListItem (s/maybe domain/TODOListItem))
(def NilableList (s/maybe domain/TODOList))

(s/defn find-list-by-id :- NilableList
  [dbval :- database/Database
   db-id :- domain/ID]
  (get dbval db-id))

(s/defn find-list-item-by-ids :- NilableListItem
  [dbval :- database/Database
   list-id :- domain/ID
   item-id :- domain/ID]
  (get-in dbval [list-id :items item-id] nil))

(s/defn list-item-add :- database/Database
  [dbval :- database/Database
   list-id :- domain/ID
   item-id :- domain/ID
   new-item :- domain/TODOListItem]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(s/defn delete-item :- database/Database
  [dbval :- database/Database
   the-list :- domain/TODOList
   list-id :- domain/ID
   item-id :- domain/ID]
  (if (contains? (:items the-list) item-id)
    (update-in dbval [list-id :items] dissoc item-id)
    dbval))
