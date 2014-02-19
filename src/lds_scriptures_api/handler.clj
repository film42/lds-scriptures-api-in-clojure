(ns lds-scriptures-api.handler
  (:use lds-scriptures-api.rest)
  (:use compojure.core)
  (:use ring.util.response)
  (:use ring.adapter.jetty)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as middleware]
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

(def api-version "/api/v1")

;; App Routes, basically an ANY but easier to debug
(defroutes app-routes
  ;; Search
  (GET (str api-version "/search*")
    {params :params} []
      {:body (verse/render-search (get params :query))})

  ;; Volumes
  (GET (str api-version "/:volume/")
    [volume]
      ;; Render
      {:body (volume/render volume)})
  (GET (str api-version "/:volume")
    [volume]
      ;; Render
      {:body (volume/render volume)})

  ;; Books
  (GET (str api-version "/:volume/:book/")
    [volume book]
      {:body (book/render book)})
  (GET (str api-version "/:volume/:book")
    [volume book]
      {:body (book/render book)})

  ;; Chapter
  (GET (str api-version "/:volume/:book/:chapter/")
    [volume book chapter]
      {:body (chapter/render chapter book volume)})
  (GET (str api-version "/:volume/:book/:chapter")
    [volume book chapter]
      {:body (chapter/render chapter book volume)})

  ;; Verses
  (GET (str api-version "/:volume/:book/:chapter/:verses*")
    [volume book chapter verses & query]
      {:body (verse/render verses chapter book volume (query :*))})

  ;; 404
  (route/not-found (str "Site map: /api/v1/:volume/:book/:chapter/:verse/"
                        "\n\nSearch: /api/v1/search?query=boat"
                        "\n\nVolumes: ot, nt, bm, dc, pgp")))

;; Create the App here
(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-response)
      (wrap-json)
      (wrap-cors)))
