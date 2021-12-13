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

;; This implementation doesn't short circuit on falsey forms: https://clojuredocs.org/clojure.core/if-let#example-5b8deb7ce4b00ac801ed9e81
(defmacro if-let*
  "Retrieved from https://clojuredocs.org/clojure.core/if-let#example-5797f83ce4b0bafd3e2a04b9
  Else branch did not work with expressions."
  ([bindings then]
   `(if-let* ~bindings ~then nil))
  ([bindings then else]
   (if (seq bindings)
     `(if-let [~(first bindings) ~(second bindings)]
        (if-let* ~(drop 2 bindings) ~then ~else)
        ~else)
     then)))