(ns lucasanjosmoraes.domain)

(defn make-list
  [nm]
  {:name  nm
   :items {}})

(defn make-list-item
  [nm]
  {:name  nm
   :done? false})

(defn update-list-item
  [item done]
  (let [current-done (:done? item)]
    (if (= done current-done)
      item
      (assoc item :done? done))))
