(ns lucasanjosmoraes.handlers)

(defn respond-hello
  [request]
  (println (:accept request))
  {:status 200
   :body   (str "Hello, "
             (get-in request [:path-params :name])
             "!")})

(defn respond-hi
  [request]
  {:status 200
   :body   (str "Hi, "
             (get-in request [:query-params :name])
             "!")})
