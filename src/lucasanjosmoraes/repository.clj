(ns lucasanjosmoraes.repository)

(defn find-list-by-id
  [dbval db-id]
  (get dbval db-id))

(defn find-list-item-by-ids
  [dbval list-id item-id]
  (get-in dbval [list-id :items item-id] nil))

(defn list-item-add
  [dbval list-id item-id new-item]
  (if (contains? dbval list-id)
    (assoc-in dbval [list-id :items item-id] new-item)
    dbval))

(defn delete-item
  [dbval the-list list-id item-id]
  (if (contains? (:items the-list) item-id)
    (update-in dbval [list-id :items] dissoc item-id)
    dbval))
