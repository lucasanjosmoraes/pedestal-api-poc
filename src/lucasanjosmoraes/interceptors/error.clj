(ns lucasanjosmoraes.interceptors.error
  (:require [io.pedestal.interceptor.error :as error-int])
  (:import (clojure.lang ExceptionInfo)))

(def service-error-handler
  (error-int/error-dispatch [ctx ex]
    [{:exception-type ExceptionInfo}]
    (assoc ctx :response {:status 500
                          :body   "Internal Error"})

    :else
    (assoc ctx :io.pedestal.interceptor.chain/error ex)))
