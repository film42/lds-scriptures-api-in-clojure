(ns lds-scriptures-api.views.chapter
  (:require [lds-scriptures-api.db :as db]))

;; Verses
(defn template [v s]
  {:title       (v :verse_title)
   :title_short (v :verse_title_short)
   :text        (v :verse_scripture)
   :verse       s })

(defn render [chapter book volume]
  (def verse-list (atom []))
  (let [c (db/get-chapter chapter book volume)]
    (if (nil? c)
      ;; Not found
      {:error "Not found"}
      ;; Render
      (doseq [i (range (count c))]
        (swap! verse-list conj (template (c i) (+ i 1))))))
  ;; Return the Atom (fix)
  @verse-list)

