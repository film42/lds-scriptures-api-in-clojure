(ns lds-scriptures-api.db
  (:require [korma.db :as korma]
            [korma.core :as kc]))

;;
;; Database Config
;;
(def db-sqlite-config
  {:db "scriptures.sqlite3"})

(korma/defdb db
  (korma/sqlite3 db-sqlite-config))

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

(defn get-chapter
  "Optimized get chapter, this time no joins"
  [chapter book volume]
  ;; Sanitize inputs
  (let [c (if (integer? chapter) chapter (name chapter)),
        b (get-book book),
        v (get-volume volume)]
    (try
      (kc/select verses
        (kc/where {:book_id (b :id),
                   :volume_id (v :id),
                   :chapter c}))
      (catch NullPointerException e))))

(defn get-verse
  "Get a verse with a book, volume, and scripture"
  ([verse chapter book volume]
  (let [res (get-chapter chapter book volume)]
    ;; Return the index of the verse
    (try
      (res (dec verse))
      (catch Exception e nil))))
  ;; Chaining
  ([verse verses]
    ;; Return the index of the verse
    (try
      (verses (dec verse))
      (catch Exception e nil))))
