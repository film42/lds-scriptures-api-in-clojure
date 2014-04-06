(ns lds-scriptures-api.parser.parser
  (:require [lds-scriptures-api.db :as db])
  (:use [net.cgrand.enlive-html])
  (:use [lds-scriptures-api.parser.dictionary])
  (:use [lds-scriptures-api.parser.utils]))

;
; Parser Secret Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- get-title
  "Parse html for a specific Book Title"
  [html]
  (let [title-html (select html [:div.content :h2])]
    (loop [old (first title-html)]
      ;; check to see if the tag has been nested
      (let [new (-> old :content last)]
        ;; return if old was valid
        (if (nil? new) old
          ;; recurse if not base case
          (recur new))))))

(defn- str->verse
  "Parses a verse and returns map with ch, verse, and text"
  [verse]
  (let [spliced (re-matches #"(\d*):(\d*)\s(.*)" verse)]
    {:chapter (spliced 1)
     :verse (spliced 2)
     :text (spliced 3)}))

(defn- verse?
  "Valid verse must contain a chapter, verse number and text"
  [verse]
  (if-not (string? verse) false
    (let [splices (re-matches #"(\d*):(\d*)\s(.*)" verse)]
      (if (= (count splices) 4) true false))))

(defn- parse-verses
  "Iterate through all <p> tags and check if their a verse, return: [str]"
  [html]
  (let [vl (select html [:div.content :p])]
    (loop [v (first vl)
           r (rest vl)
           acc []]
      (if (nil? v) acc
        (let [verse (-> v :content first)]
          (if (verse? verse)
            (recur (first r) (rest r) (conj acc verse))
            (recur (first r) (rest r) acc)))))))

(defn- get-verses
  "Uses parse-verses to return a formatted verse list"
  [html]
  (let [verse-list (parse-verses html)]
    (map str->verse verse-list)))

(defn- verse-template [vers heading]
  { ;:versetext
    ;:id
    :pilcrow   0
    :volume_id 3
    :verse     (-> (vers :verse) read-string)
    :book_id   (book-id heading)
    :chapter   (-> (vers :chapter) read-string)
    :verse_scripture (vers :text)
    :verse_title_short (str (tite->short heading) " " (vers :chapter) ":" (vers :verse))
    :verse_title (str heading " " (vers :chapter) ":" (vers :verse))
    :edition 1830
  })

;
; Parser Public Api
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol BookProtocol
  (verses [this] "Return list of verses")
  (title [this] "Return the book title")
  (volume [this] "Return the book's parent volume")
  (sqlize [this] "Convert each verse to a sql formatted hash")
  (sql-format [this index] "Return a db template"))

(defrecord BookParser [html]
  BookProtocol
  (verses [this] (-> html get-verses vec))
  (title [this] (-> html get-title))
  (volume [this] "bm")
  (sqlize [this] (map #(verse-template % (title this)) (verses this)))
  (sql-format [this index] (verse-template ((verses this) index) (title this))))


(defn path->parser
  "For a given file path, return a BookParser"
  [path]
  (->BookParser (file->html path)))


(let [b (path->parser (books 0))]
  (-> b verses first (verse-template (title b))))


;(-> (books 9) path->parser (sql-format 14) db/add-verse)

;
; Runner
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(doseq [b books]
  ; Create parser from each file
  (let [p (path->parser b)]
    ; Map each verse to sql hash
    (let [vs (sqlize p)]
      (db/add-verse vs)
      (println b))))

;
; Example
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;(doseq [path books]
;  (let [book (path->parser path)]
;    (println (str "Title: " (title book)
;                  "\t\t\t Verse Count: " (count (verses book))))))

