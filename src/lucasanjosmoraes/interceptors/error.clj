(ns lucasanjosmoraes.interceptors.error
  (:require [io.pedestal.interceptor.error :as error-int]))

(def service-error-handler
  (error-int/error-dispatch [ctx ex]
    [{:exception-type :java.lang.AssertionError}]
    (assoc ctx :response {:status 400 :body "Invalid request"})

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))