(ns lucasanjosmoraes.interceptors.domain
  (:require [io.pedestal.http.route :as route]
            [lucasanjosmoraes.helpers :as h]
            [lucasanjosmoraes.domain :as domain]
            [lucasanjosmoraes.repository :as repository]
            [schema.core :as s]))

;; Common schema

(def Path-Params
  {(s/optional-key :list-id) s/Str
   (s/optional-key :item-id) s/Str
   s/Any                     s/Any})

(def Query-Params
  {(s/optional-key :name) s/Str
   (s/optional-key :done) h/str-parseable-to-bool
   s/Any                  s/Any})

(def Request
  {(s/optional-key :path-params)  Path-Params
   (s/optional-key :query-params) Query-Params
   ;; TODO: declare :database schema
   s/Any                          s/Any})

(def Result-Headers
  (s/constrained [s/Str]
    #(even? (count %))))

(def Headers (s/maybe {s/Str s/Str}))

(def Response
  {:status                   (s/constrained s/Int #(> % 0))
   :body                     s/Any
   (s/optional-key :headers) Headers})

(def Tx-Data [(s/one (s/pred ifn? 'ifn?) 'ifn?) s/Any])

(def Context
  {(s/optional-key :request)        Request
   (s/optional-key :result)         s/Any
   (s/optional-key :result-headers) Result-Headers
   (s/optional-key :response)       Response
   (s/optional-key :tx-data)        Tx-Data
   s/Any                            s/Any})

;; Entity render

(s/defn ^:always-validate entity-reader-leave :- Context
  [context :- Context]
  (if-let [item (:result context)]
    (if-let [headers (:result-headers context)]
      (assoc context :response (apply (partial h/ok item) headers))
      (assoc context :response (h/ok item)))
    context))

(def entity-render
  {:name  :entity-render
   :leave entity-reader-leave})

;; list-create

(s/defn ^:always-validate list-create-enter :- Context
  [context :- Context]
  (let [nm       (get-in context [:request :query-params :name] "Unnamed List")
        new-list (domain/make-list nm)
        db-id    (str (gensym "1"))
        ;; Any leftover entries in the :params map that do not correspond to path parameters get turned into query string
        ;; parameters
        url      (route/url-for :list-view :params {:list-id db-id})]
    (assoc context
      ;; http://pedestal.io/guides/your-first-api
      ;; [...]
      ;; in general, adding something to the database might cause it to change. Sometimes there are triggers or stored
      ;; procedures that change it. Other times it's just an ID being assigned. Usually, instead of attaching the new
      ;; entity here, I would attach its ID to the context and use another interceptor later in the chain to look up
      ;; the entity after db-interceptor executes the transaction.
      ;; [...]
      :response (h/created new-list "Location" url)
      :tx-data [assoc db-id new-list])))

(def list-create
  {:name  :list-create
   :enter list-create-enter})

;; list-view

(s/defn ^:always-validate list-view-leave :- Context
  [context :- Context]
  (h/if-let* [db-id    (get-in context [:request :path-params :list-id])
              the-list (repository/find-list-by-id (get-in context [:request :database]) db-id)]
    (assoc context :result the-list)
    context))

(def list-view
  {:name  :list-view
   :leave list-view-leave})

;; list-item-view

(s/defn ^:always-validate list-item-view-leave :- Context
  [context :- Context]
  (h/if-let* [list-id (get-in context [:request :path-params :list-id])
              item-id (get-in context [:request :path-params :item-id])
              item    (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
    (assoc context :result item)
    context))

(def list-item-view
  {:name  :list-item-view
   :leave list-item-view-leave})

;; list-item-create

(s/defn ^:always-validate list-item-create-enter :- Context
  [context :- Context]
  (if-let [list-id (get-in context [:request :path-params :list-id])]
    (let [nm       (get-in context [:request :query-params :name] "Unnamed Item")
          new-item (domain/make-list-item nm)
          item-id  (str (gensym "i"))
          url      (route/url-for :get-item :params {:list-id list-id
                                                     :item-id item-id})]
      (-> context
        (assoc :tx-data [repository/list-item-add list-id item-id new-item])
        (assoc-in [:request :path-params :item-id] item-id)
        (assoc :result-headers ["Location" url])))
    context))

(def list-item-create
  {:name  :list-item-create
   :enter list-item-create-enter})

;; list-item-update

(s/defn ^:always-validate list-item-update-enter :- Context
  [context :- Context]
  (h/if-let* [list-id      (get-in context [:request :path-params :list-id])
              item-id      (get-in context [:request :path-params :item-id])
              done         (h/str->bool (get-in context [:request :query-params :done]))
              item         (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)
              updated-item (domain/update-list-item item done)]
    (assoc context :tx-data [repository/list-item-add list-id item-id updated-item])
    context))

(def list-item-update
  {:name  :list-item-update
   :enter list-item-update-enter})

;; list-item-delete

(s/defn ^:always-validate list-item-delete-enter :- Context
  [context :- Context]
  (h/if-let* [list-id  (get-in context [:request :path-params :list-id])
              the-list (repository/find-list-by-id (get-in context [:request :database]) list-id)
              item-id  (get-in context [:request :path-params :item-id])]
    (assoc context :tx-data [repository/delete-item the-list list-id item-id])
    ;; If we use list-view interceptor with this one, it will not respond 404 status if the given :list-id exist,
    ;; due to the list-view behavior
    context))

(def list-item-delete
  {:name  :list-item-delete
   :enter list-item-delete-enter})
