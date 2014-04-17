(ns lds-scriptures-api.handler
  (:use lds-scriptures-api.middleware)
  (:use compojure.core)
  (:use ring.util.response)
  (:use ring.adapter.jetty)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as rmw]
            [lds-scriptures-api.views.volume :as volume]
            [lds-scriptures-api.views.book :as book]
            [lds-scriptures-api.views.chapter :as chapter]
            [lds-scriptures-api.views.verse :as verse]))

;; JSON Responses / Statuses
(defn empty-response []
  {:body {}})

(defn mirror-response [body]
  {:body body})

(defn delete-response [body]
  {:status 204
   :body body})

(defn- parse [p param-key]
  (if (nil? (get p param-key))
    "1981"
    (get p param-key)))

(def api-version "/api/v1")

;; App Routes, basically an ANY but easier to debug
(defroutes app-routes
    ;; Volumes
  (GET (str api-version)
    [volume]
      ;; Render
      {:body (volume/render-all)})

  ;; Search
  (GET (str api-version "/search*")
    {params :params} []
      {:body (verse/render-search (get params :query))})

  ;; Volumes
  (GET (str api-version "/:volume")
    [volume]
      ;; Render
      {:body (volume/render volume)})

  ;; Books
  (GET (str api-version "/:volume/:book")
    [volume book]
      {:body (book/render book)})

  ;; Chapter
  (GET (str api-version "/:volume/:book/:chapter")
    [volume book chapter & params]
      {:body (chapter/render chapter book volume (parse params :edition))})

  ;; Verses
  (GET (str api-version "/:volume/:book/:chapter/:verses*")
    [volume book chapter verses & query]
      {:body (verse/render verses chapter book volume (query :*))})

  ;; 404
  (route/not-found (str "Site map: /api/v1/:volume/:book/:chapter/:verse/"
                        "\n\nSearch: /api/v1/search?query=boat"
                        "\n\nVolumes: ot, nt, bm, dc, pgp"
                        "\n\nUpdate 04/05/2014: You can now do `?edition=1830` for most bm functions")))

;; Create the App here
(def app
  (-> (handler/api app-routes)
      (rmw/wrap-json-response)
      (with-ignore-trailing-slash)
      (wrap-json)
      (wrap-cors)))
