(ns lds-scriptures-api.views.verse
  (:use [clojure.string :only [split]])
  (:require [lds-scriptures-api.db :as db]))

;; Verses
(defn template [v]
  (let [s (last (split (v :verse_title) #":"))]
    {:title       (v :verse_title)
     :title_short (v :verse_title_short)
     :text        (v :verse_scripture)
     :verse       (read-string s)}))

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

(defn render [verses chapter book volume query]
  (let [vset (get-param-tree-as-list (str verses query))]
    (let [v (vec (db/get-verses vset chapter book volume))]
      (if (= 0 (count v))
        ;; Not found
        {:error "Not found"}
        ;; Render)
        (vec
          (for [i (range (count v))]
            (template (v i))))))))
