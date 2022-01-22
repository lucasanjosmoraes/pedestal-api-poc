(ns lucasanjosmoraes.interceptors.error
  (:require [io.pedestal.interceptor.error :as error-int]))

(def service-error-handler
  (error-int/error-dispatch [ctx ex]
    ;; Any validation error from the spec is of the type java.lang.AssertionError.
    ;; In a production-ready application, we must handle its exceptions properly.
    [{:exception-type :java.lang.AssertionError}]
    (assoc ctx :response {:status 400 :body "Invalid request"})

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))