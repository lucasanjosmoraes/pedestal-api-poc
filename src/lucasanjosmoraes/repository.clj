(ns lucasanjosmoraes.repository
  (:require [clojure.spec.alpha :as s]
            [lucasanjosmoraes.domain :as domain]
            [lucasanjosmoraes.interceptors.database :as db]))

(s/def ::nilable-item (s/nilable ::domain/list-item))
(s/def ::nilable-list (s/nilable ::domain/list))

(defn find-list-by-id
  [dbval db-id]
  {:pre  [(s/valid? ::db/database dbval) (s/valid? ::db/id db-id)]
   :post [(s/valid? ::nilable-list %)]}
  (get dbval db-id))

(s/fdef find-list-by-id
  :args (s/cat :dbval ::db/database :db-id ::db/id)
  :ret ::nilable-list)

(defn find-list-item-by-ids
  [dbval list-id item-id]
  {:pre  [(s/valid? ::db/database dbval) (s/valid? ::db/id list-id) (s/valid? ::db/id item-id)]
   :post [(s/valid? ::nilable-item %)]}
  (get-in dbval [list-id :items item-id] nil))

(s/fdef find-list-item-by-ids
  :args (s/cat :dbval ::db/database :list-id ::db/id :item-id ::db/id)
  :ret ::nilable-item)

(defn list-item-add
  [dbval list-id item-id new-item]
  {:pre  [(s/valid? ::db/database dbval)
          (s/valid? ::db/id list-id)
          (s/valid? ::db/id item-id)
          (s/valid? ::domain/list-item new-item)]
   :post [(s/valid? ::db/database %)]}
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(s/fdef list-item-add
  :args (s/cat :dbval ::db/database :list-id ::db/id :item-id ::db/id :new-item ::domain/list-item)
  :ret ::db/database)

(defn delete-item
  [dbval the-list list-id item-id]
  {:pre  [(s/valid? ::db/database dbval) (s/valid? ::domain/list the-list) (s/valid? ::db/id list-id) (s/valid? ::db/id item-id)]
   :post [(s/valid? ::db/database %)]}
  (if (contains? (:items the-list) item-id)
    (update-in dbval [list-id :items] dissoc item-id)
    dbval))

(s/fdef delete-item
  :args (s/cat :dbval ::db/database :the-list ::domain/list :list-id ::db/id :item-id ::db/id)
  :ret ::db/database)
