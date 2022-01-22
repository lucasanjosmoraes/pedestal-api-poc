(ns lucasanjosmoraes.interceptors.domain
  (:require [clojure.spec.alpha :as s]
            [io.pedestal.http.route :as route]
            [lucasanjosmoraes.helpers :as h]
            [lucasanjosmoraes.domain :as domain]
            [lucasanjosmoraes.repository :as repository]))

;; Common spec

(s/def ::result (s/keys))
(s/def ::result-headers (s/& (s/* string?) #(even? (count %))))
(s/def ::context (s/keys :opt-un [::result ::result-headers]))

(s/def ::status pos-int?)
(s/def ::body any?)
(s/def ::headers (s/nilable (s/map-of string? string?)))
(s/def ::response (s/keys :req-un [::status ::body]
                    :opt-un [::headers]))
(s/def ::tx-data (s/cat :fn ifn? :args (s/* any?)))
(s/def ::context-with-actions (s/merge ::context
                                (s/keys :opt-un [::response ::tx-data])))

;; Entity render

(defn entity-render-leave
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context-with-actions %)]}
  (if-let [item (:result context)]
    (if-let [headers (:result-headers context)]
      (assoc context :response (apply (partial h/ok item) headers))
      (assoc context :response (h/ok item)))
    context))

(s/fdef entity-render-leave
  :args (s/cat :context ::context)
  :ret ::context-with-actions)

(def entity-render
  {:name  :entity-render
   :leave entity-render-leave})

;; list-create

(defn list-create-enter
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context-with-actions %)]}
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

(s/fdef list-create-enter
  :args (s/cat :context ::context)
  :ret ::context-with-actions)

(def list-create
  {:name  :list-create
   :enter list-create-enter})

;; list-view

(defn list-view-leave
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context %)]}
  (h/if-let* [db-id    (get-in context [:request :path-params :list-id])
              the-list (repository/find-list-by-id (get-in context [:request :database]) db-id)]
    (assoc context :result the-list)
    context))

(s/fdef list-view-leave
  :args (s/cat :context ::context)
  :ret ::context)

(def list-view
  {:name  :list-view
   :leave list-view-leave})

;; list-item-view

(defn list-item-view-leave
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context %)]}
  (h/if-let* [list-id (get-in context [:request :path-params :list-id])
              item-id (get-in context [:request :path-params :item-id])
              item    (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)]
    (assoc context :result item)
    context))

(s/fdef list-item-view-leave
  :args (s/cat :context ::context)
  :ret ::context)

(def list-item-view
  {:name  :list-item-view
   :leave list-item-view-leave})

;; list-item-create

(defn list-item-create-enter
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context-with-actions %)]}
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

(s/fdef list-item-create-enter
  :args (s/cat :context ::context)
  :ret ::context-with-actions)

(def list-item-create
  {:name  :list-item-create
   :enter list-item-create-enter})

;; list-item-update

(defn list-item-update-enter
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context-with-actions %)]}
  (h/if-let* [list-id      (get-in context [:request :path-params :list-id])
              item-id      (get-in context [:request :path-params :item-id])
              done         (domain/parse-done (get-in context [:request :query-params :done]))
              item         (repository/find-list-item-by-ids (get-in context [:request :database]) list-id item-id)
              updated-item (domain/update-list-item item done)]
    (assoc context :tx-data [repository/list-item-add list-id item-id updated-item])
    context))

(s/fdef list-item-update-enter
  :args (s/cat :context ::context)
  :ret ::context-with-actions)

(def list-item-update
  {:name  :list-item-update
   :enter list-item-update-enter})

;; list-item-delete

(defn list-item-delete-enter
  [context]
  {:pre  [(s/valid? ::context context)]
   :post [(s/valid? ::context-with-actions %)]}
  (h/if-let* [list-id  (get-in context [:request :path-params :list-id])
              the-list (repository/find-list-by-id (get-in context [:request :database]) list-id)
              item-id  (get-in context [:request :path-params :item-id])]
    (assoc context :tx-data [repository/delete-item the-list list-id item-id])
    ;; If we use list-view interceptor with this one, it will not respond 404 status if the given :list-id exist,
    ;; due to the list-view behavior
    context))

(s/fdef list-item-delete-enter
  :args (s/cat :context ::context)
  :ret ::context-with-actions)

(def list-item-delete
  {:name  :list-item-delete
   :enter list-item-delete-enter})
