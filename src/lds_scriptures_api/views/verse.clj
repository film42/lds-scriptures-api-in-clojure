(ns lds-scriptures-api.views.verse
  (:use [clojure.string :only [split lower-case]])
  (:require [clojure.string :only [replace] :as cstr])
  (:require [lds-scriptures-api.db :as db]))

(defn underscore
  "Replace all spaces with underscores"
  [& w] (cstr/replace (reduce str w) #" " "_"))

(defn parse-book-slug
  "Regex lookup for the book slug of a verse title (short)"
  [title]
  (let [cleaned (-> title
                    (cstr/replace #"-" "_")
                    (cstr/replace #"&" ""))]
    (let [p (re-matches #"(\d*)\s*([\w|\s]+)[\.|\s].*" cleaned)]
      (if (empty? (second p))
        (underscore (lower-case (nth p 2)))
        (underscore (second p) " " (lower-case (nth p 2)))))))

;; Verses
(defn template [v]
  (let [s (last (split (v :verse_title) #":"))]
    {:title       (v :verse_title)
     :title_short (v :verse_title_short)
     :text        (v :verse_scripture)
     :edition     (v :edition)
     :verse       (read-string s)}))

;; Search Verse
(defn search-template [v]
  (let [s (last (split (v :verse_title) #":"))]
    {:title       (v :verse_title)
     :title_short (v :verse_title_short)
     :text        (v :verse_scripture)
     :edition     (v :edition)
     :verse       (read-string s)
     :chapter     (v :chapter)
     :book        (parse-book-slug (v :verse_title_short))}))

(defn get-param-tree-as-list
  "Create a param tree for looking up verses, concat to single list"
  [params]
  (apply concat
    (let [focus (split params #",")]
      (for [i (range (count focus))]
        (let [ranges (split (focus i) #"-")]
          (if (= 1 (count ranges))
              (list (read-string (first ranges)))
              (range (read-string (ranges 0))
                     (inc (read-string (ranges 1))))))))))

(defn render-search [query]
  (let [v (vec (db/search query))]
    (if (= 0 (count v))
        ;; Not found
        {:error "No :query param provided"}
        ;; Render)
        (vec
          (for [i (range (count v))]
            (search-template (v i)))))))

(defn render [verses chapter book volume query]
  (let [vset (get-param-tree-as-list (str verses query))]
    (let [v (vec (db/get-verses vset chapter book volume "1981"))]
      (if (= 0 (count v))
        ;; Not found
        {:error "Not found"}
        ;; Render)
        (vec
          (for [i (range (count v))]
            (template (v i))))))))
