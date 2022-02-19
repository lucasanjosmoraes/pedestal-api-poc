(ns lucasanjosmoraes.interceptors.error
  (:require [io.pedestal.interceptor.error :as error-int])
  (:import (clojure.lang ExceptionInfo)))

(def service-error-handler
  (error-int/error-dispatch [ctx ex]
    ;; Any validation error from the schema is of the type clojure.lang.ExceptionInfo.
    ;; In a production-ready application, we must handle its exceptions properly.
    [{:exception-type ExceptionInfo}]
    (assoc ctx :response {:status 500
                          :body   "Internal Error"})

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))
