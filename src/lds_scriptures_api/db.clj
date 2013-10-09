(ns lds-scriptures-api.db
  (:require [korma.db :as korma]
            [korma.core :as kc]
            [clojure.string :as string])
  (:import (java.net URI)))

;;
;; Database Herlpers
;;
(def db-local
  "postgresql://film42:none@localhost:5432/lds_scriptures")

(def db-production
  (System/getenv "DATABASE_URL"))

(def db-postrgres-uri
  (if (not (nil? db-production))
    db-production
    db-local))

(defn set-app-pg-db! []
  (let [db-uri (java.net.URI. db-postrgres-uri)]
    (->> (string/split (.getUserInfo db-uri) #":")
      (#(identity {:db (last (string/split db-postrgres-uri #"\/"))
                   :host (.getHost db-uri)
                   :port (.getPort db-uri)
                   :user (% 0)
                   :password (% 1)}))
      (korma/postgres)
      (korma/defdb db))))

;;
;; Database Config
;;

(set-app-pg-db!)

;;
;; Model Definitions
;;
(declare verses books volumes)

(kc/defentity verses
  ;; Associations
  (kc/belongs-to books {:fk :book_id})
  (kc/belongs-to volumes {:fk :volume_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :chapter :pilcrow :verse_scripture :verse_title :verse_title_short)

  ;; Meta
  (kc/table :verses)
  (kc/database db))

(kc/defentity books
  ;; Associations
  (kc/belongs-to volumes {:fk :volume_id})
  (kc/has-many verses {:fk :verse_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :book_title :book_title_jst :book_title_long :book_title_short :book_subtitle :lds_org :num_chapters :num_verses)

  ;; Meta
  (kc/table :books)
  (kc/database db))

(kc/defentity volumes
  ;; Associations
  (kc/has-many books {:fk :book_id})
  (kc/has-many verses {:fk :verse_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :volume_title :volume_title_long :volume_subtitle :lds_org :num_chapters :num_verses)

  ;; Meta
  (kc/table :volumes)
  (kc/database db))

;; Interface
(defn get-volume
  "Get a volume"
  [volume]
  (let [v (name volume)]
    (first (kc/select volumes
      (kc/where {:lds_org v})))))

(defn get-book
  "Get a book"
  [book]
  (let [b (name book)]
    (first (kc/select books
      (kc/where {:lds_org b})))))

(defn get-books
  "Get all books for a volume"
  [volume]
  (let [v (get-volume volume)]
    (try
      (kc/select books
        (kc/where {:volume_id (v :id)}))
      (catch NullPointerException e))))

(defn get-chapter
  "Optimized get chapter, this time no joins"
  [chapter book volume]
  ;; Sanitize inputs
  (let [c (if (string? chapter) chapter (Long/toString chapter)),
        b (get-book book),
        v (get-volume volume)]
    (try
      (kc/select verses
        (kc/where {:book_id (b :id),
                   :volume_id (v :id),
                   :chapter (read-string chapter)}))
      (catch NullPointerException e nil))))

(defn get-verse
  "Get a verse with a book, volume, and scripture"
  ([verse chapter book volume]
  (let [res (get-chapter chapter book volume)]
    ;; Return the index of the verse
    (if (not (nil? res))
      (try
        (res (dec (read-string verse)))
        (catch Exception e nil)))))
  ;; Chaining
  ([verse verses]
    ;; Return the index of the verse
    (try
      (verses (dec verse))
      (catch Exception e nil))))

