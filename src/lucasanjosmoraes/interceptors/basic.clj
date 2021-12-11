(ns lucasanjosmoraes.interceptors.basic
  (:require [lucasanjosmoraes.helpers :as helpers]))

(def echo
  {:name :echo
   :enter
   (fn [context]
     (let [response (helpers/ok context)]
       (assoc context :response response)))})
