(ns lucasanjosmoraes.service
  (:require [clojure.data.json :as json]
            [io.pedestal.http :as http]
            ;[io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.content-negotiation :as con-neg]
            [io.pedestal.http.route :as route]))

(defonce database (atom {}))

;; Helpers

(defn response [status body & {:as headers}]
  {:status status :body body :headers headers})

(def ok       (partial response 200))
(def created  (partial response 201))

;; Domain

(def truthy? #{"true"})

(def falsy? #{"false"})

(defn str-is-boolean
  [str]
  (or (truthy? str) (falsy? str)))

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

;; Repository

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

;; Handlers

(defn respond-hello
  [request]
  (println (:accept request))
  {:status 200
   :body (str "Hello, "
              (get-in request [:path-params :name])
              "!")})

(defn respond-hi
  [request]
  {:status 200
   :body (str "Hi, "
              (get-in request [:query-params :name])
              "!")})

;; Interceptors

(def supported-types ["application/json" "text/plain"])

(def content-neg-intc (con-neg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/plain"       body
    "application/json" (json/write-str body)))

(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def echo
  {:name :echo
   :enter
         (fn [context]
           (let [response (ok context)]
             (assoc context :response response)))})

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (nil? (get-in context [:response :headers "Content-Type"]))
       (update-in [:response] coerce-to (accepted-type context))))})

;(def common-interceptors [(body-params/body-params) http/html-body])

(def db-interceptor
  {:name ::database-interceptor
   :enter
   (fn [context]
     (update context :request assoc :database @database))
   :leave
   (fn [context]
     (if-let [[op & args] (:tx-data context)]
       (do
         (apply swap! database op args)
         (assoc-in context [:request :database] @database))
       context))})

(def entity-render
  {:name :entity-render
   :leave
   (fn [context]
     (if-let [item (:result context)]
       (assoc context :response (ok item))
       context))})


(def list-create
  {:name :list-create
   :enter
   (fn [context]
     (let [nm (get-in context [:request :query-params :name] "Unnamed List")
           new-list (make-list nm)
           db-id (str (gensym "1"))
           url (route/url-for :list-view :params {:list-id db-id})]
       (assoc context
         ;; http://pedestal.io/guides/your-first-api
         ;; [...]
         ;; in general, adding something to the database might cause it to change. Sometimes there are triggers or stored
         ;; procedures that change it. Other times it's just an ID being assigned. Usually, instead of attaching the new
         ;; entity here, I would attach its ID to the context and use another interceptor later in the chain to look up
         ;; the entity after db-interceptor executes the transaction.
         ;; [...]
         :response (created new-list "Location" url)
         :tx-data [assoc db-id new-list])))})

(def list-view
  {:name :list-view
   :enter
   (fn [context]
     (if-let [db-id (get-in context [:request :path-params :list-id])]
       (if-let [the-list (find-list-by-id (get-in context [:request :database]) db-id)]
         (assoc context :result the-list)
         context)
       context))})

(def list-item-view
  {:name :list-item-view
   :leave
   (fn [context]
     ;; TODO: Exercise - this repetitively nesting structure in list-item-view is a perfect candidate for a Clojure macro
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (if-let [item-id (get-in context [:request :path-params :item-id])]
         (if-let [item (find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
           (assoc context :result item)
           context)
         context)
       context))})

(def list-item-create
  {:name :list-item-create
   :enter
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (let [nm (get-in context [:request :query-params :name] "Unnamed Item")
             new-item (make-list-item nm)
             item-id (str (gensym "i"))]
         (-> context
             (assoc :tx-data [list-item-add list-id item-id new-item])
             (assoc-in [:request :path-params :item-id] item-id)))
       context))})

(def list-item-update
  {:name :list-item-update
   :enter
   (fn [context]
     (if-let [list-id (get-in context [:request :path-params :list-id])]
       (if-let [item-id (get-in context [:request :path-params :item-id])]
         (if-let [item (find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
           (let [done (get-in context [:request :query-params :done] false)]
             (if (str-is-boolean done)
               (let [updated-item (update-list-item item (new Boolean done))]
                 (-> context
                     (assoc :tx-data [list-item-add list-id item-id updated-item])
                     (assoc-in [:request :path-params :item-id] item-id)))
               ;; If we declare list-item-view interceptor with this one, this return will not respond 404, due to the
               ;; list-item-view behavior
               context))
           context)
         context)
       context))})

;; Routes

(def routes #{["/hi" :get [coerce-body content-neg-intc respond-hi] :route-name :hi]
              ["/hello/:name" :get [coerce-body content-neg-intc respond-hello] :route-name :hello]
              ["/todo"                    :post   [coerce-body content-neg-intc db-interceptor list-create]]
              ["/todo"                    :get    echo :route-name :list-query-form]
              ["/todo/:list-id"           :get    [coerce-body content-neg-intc entity-render db-interceptor list-view]]
              ["/todo/:list-id"           :post   [coerce-body content-neg-intc entity-render list-item-view db-interceptor list-item-create]]
              ["/todo/:list-id/:item-id"  :get    [coerce-body content-neg-intc entity-render list-item-view db-interceptor]]
              ["/todo/:list-id/:item-id"  :put    [coerce-body content-neg-intc entity-render list-item-view db-interceptor list-item-update]]
              ["/todo/:list-id/:item-id"  :delete echo :route-name :list-item-delete]})

;; Service

(def service {:env :prod
              ::http/routes routes
              ::http/type :jetty
              ::http/port 8080
              ::http/container-options {:h2c? true
                                        :h2? false
                                        :ssl? false}})