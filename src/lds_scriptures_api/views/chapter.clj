(ns lds-scriptures-api.views.chapter
  (:require [lds-scriptures-api.db :as db]))

;; Verses
(defn template [v s]
  {:title       (v :verse_title)
   :title_short (v :verse_title_short)
   :text        (v :verse_scripture)
   :edition     (v :edition)
   :verse       s })

(defn render [chapter book volume ed]
  (let [c (db/get-chapter chapter book volume ed)]
    (if (nil? c)
      ;; Not found
      {:error "Not found"}
      ;; Render
      (for [i (range (count c))]
        (template (c i) (+ i 1))))))
