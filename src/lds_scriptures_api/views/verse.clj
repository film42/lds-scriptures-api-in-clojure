(ns lds-scriptures-api.views.verse
  (:require [lds-scriptures-api.db :as db]))

;; Verses
(defn template [v s]
  {:title       (v :verse_title)
   :title_short (v :verse_title_short)
   :text        (v :verse_scripture)
   :verse       s})

(defn get-verse-num [v c]
  (doseq [i (range (count c))]
    (let [v2 (c i)]
      (if (= (v2 :id) (v :id))
        (def -id i))))
  (inc -id))

(defn render [verses chapter book volume]
  (let [v (db/get-verse (read-string verses) (read-string chapter) book volume)]
    (if (nil? v)
      ;; Not found
      {:error "Not found"}
      ;; Render
      (let [c (db/get-chapter chapter book volume)]
        (let [n (get-verse-num v c)]
          (template v n))))))
