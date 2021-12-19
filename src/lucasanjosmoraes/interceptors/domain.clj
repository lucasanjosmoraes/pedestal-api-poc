(ns lucasanjosmoraes.interceptors.domain
  (:require [io.pedestal.http.route :as route]
            [lucasanjosmoraes.helpers :as h]
            [lucasanjosmoraes.domain :as domain]
            [lucasanjosmoraes.repository :as repository]))

(def entity-render
  {:name :entity-render
   :leave
   (fn [context]
     (if-let [item (:result context)]
       (if-let [headers (:result-headers context)]
         (assoc context :response (apply (partial h/ok item) headers))
         (assoc context :response (h/ok item)))
       context))})

(def list-create
  {:name :list-create
   :enter
   (fn [context]
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
         :tx-data [assoc db-id new-list])))})

(def list-view
  {:name :list-view
   :leave
   (fn [context]
     (h/if-let* [db-id (get-in context [:request :path-params :list-id])
                 the-list (repository/find-list-by-id (get-in context [:request :database]) db-id)]
       (assoc context :result the-list)
       context))})

(def list-item-view
  {:name :list-item-view
   :leave
   (fn [context]
     (h/if-let* [list-id (get-in context [:request :path-params :list-id])
                 item-id (get-in context [:request :path-params :item-id])
                 item (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
       (assoc context :result item)
       context))})

(def list-item-create
  {:name :list-item-create
   :enter
   (fn [context]
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
       context))})

(def list-item-update
  {:name :list-item-update
   :enter
   (fn [context]
     (h/if-let* [list-id (get-in context [:request :path-params :list-id])
                 item-id (get-in context [:request :path-params :item-id])
                 item (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
       (let [done (get-in context [:request :query-params :done] false)]
         (if (h/str-is-boolean done)
           (let [updated-item (domain/update-list-item item (new Boolean done))]
             (assoc context :tx-data [repository/list-item-add list-id item-id updated-item]))
           ;; If we use list-item-view interceptor with this one, it will not respond 404 status due to the list-item-view behavior
           context))
       context))})

(def list-item-delete
  {:name :list-item-delete
   :enter
   (fn [context]
     (h/if-let* [list-id (get-in context [:request :path-params :list-id])
                 the-list (repository/find-list-by-id (get-in context [:request :database]) list-id)
                 item-id (get-in context [:request :path-params :item-id])]
       (assoc context :tx-data [repository/delete-item the-list list-id item-id])
       ;; If we use list-view interceptor with this one, it will not respond 404 status if the given :list-id exist,
       ;; due to the list-view behavior
       context))})
