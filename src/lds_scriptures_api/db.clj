(ns lds-scriptures-api.db
  (:use clj-bonecp-url.core)
  (:require [korma.db :as korma]
            [korma.core :as kc]
            [clojure.string :as string]
            [clojure.edn :as edn])
  (:import (java.net URI)))


;;
;; Database Config
;;

(def datasource
  (datasource-from-url
    (or (System/getProperty "DATABASE_URL")
        "postgres://film42:none@localhost:5432/scriptures")))

(when (nil? @korma/_default)
  (korma/default-connection {:pool {:datasource datasource}}))

;;
;; Model Definitions
;;

(declare verses new-verses books volumes)

(kc/defentity verses
  ;; Associations
  (kc/belongs-to books {:fk :book_id})
  (kc/belongs-to volumes {:fk :volume_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :chapter :pilcrow :verse_scripture :verse_title :verse_title_short :edition)

  ;; Meta
  (kc/table :verses))

(kc/defentity new-verses
  ;; Associations
  (kc/belongs-to books {:fk :book_id})
  (kc/belongs-to volumes {:fk :volume_id})

  ;; Attributes
  (kc/entity-fields :chapter :pilcrow :verse_scripture :verse_title :verse_title_short :edition)

  ;; Meta
  (kc/table :verses))

(kc/defentity books
  ;; Associations
  (kc/belongs-to volumes {:fk :volume_id})
  (kc/has-many verses {:fk :verse_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :book_title :book_title_jst :book_title_long :book_title_short :book_subtitle :lds_org :num_chapters :num_verses)

  ;; Meta
  (kc/table :books))

(kc/defentity volumes
  ;; Associations
  (kc/has-many books {:fk :book_id})
  (kc/has-many verses {:fk :verse_id})

  ;; Attributes
  (kc/pk :id)
  (kc/entity-fields :volume_title :volume_title_long :volume_subtitle :lds_org :num_chapters :num_verses)

  ;; Meta
  (kc/table :volumes))

;; Interface
(defn get-volume
  "Get a volume"
  [volume]
  (let [v (name volume)]
    (first (kc/select volumes
      (kc/where {:lds_org v})))))

(defn get-volumes
  "Get all volumes in DB"
  []
  (vec (kc/select volumes)))

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
  [chapter book volume ed]
  ;; Sanitize inputs
  (let [c (if (string? chapter) chapter (Long/toString chapter)),
        b (get-book book),
        v (get-volume volume)]
    (try
      (kc/select verses
        (kc/order :verse :ASC)
        (kc/where {:book_id (b :id)
                   :volume_id (v :id)
                   :edition ed
                   :chapter (read-string chapter)}))
      (catch NullPointerException e nil))))

(defn get-verse
  "Get a verse with a book, volume, and scripture"
  ([verse chapter book volume ed]
  (let [res (get-chapter chapter book volume ed)]
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

(defn get-verses
  "Get verses with a set of verse numbers"
  [verses chapter book volume ed]
  (let [res (get-chapter chapter book volume ed)]
    (if (not (nil? res))
      (filter (fn [x] (not (nil? x)))
        (for [v verses]
          (try
            (res (dec v))
            (catch Exception e))))
      [])))

(defn search
  "Query against the database using the vectorized verses"
  [query]
  (let [sql "SELECT \"verses\".* FROM \"verses\" WHERE (verseText @@ plainto_tsquery('english', ?))"]
    (kc/exec-raw [sql [query]] :results)))


;
; Add Commands
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-verse [blob]
  (kc/insert new-verses
     (kc/values blob)))


; The Bomb
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- seq->slugs [col]
  (map #(:lds_org %) col))

(defprotocol
    IExporter
    (export [_ filename])
    (dump [_])
    (dump-volume [_ vol])
    (dump-book [_ vol book])
    (dump-chapter [_ vol book chapter]))

(defrecord Exporter [edition]
  IExporter

  (export [_ filename]
    (spit filename (pr-str (dump _))))

  (dump [_]
    (let [vol-slugs (seq->slugs (get-volumes))]
      (map #(dump-volume _ %) vol-slugs)))

  (dump-volume [_ vol]
    (pmap #(dump-book _ vol %) (get-books vol)))

  (dump-book [_ vol book]
    (let [cs (:num_chapters book)
          slug (:lds_org book)]
      (map #(dump-chapter _ vol slug %) (range 1 (inc cs)))))
  
  (dump-chapter [_ vol book chapter]
    (let [ch (get-chapter (str chapter) book vol (:edition _))]
      (map #(apply dissoc % [:versetext]) ch))))

(defn db->edn [edition filename]
  (-> (->Exporter edition) (export filename)))

