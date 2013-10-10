(ns lds-scriptures-api.views.book
  (:require [lds-scriptures-api.db :as db]))

(defn template [b]
  ;; Found let's template
  {:title       (b :book_title)
   :title_long  (b :book_title_long)
   :title_short (b :book_title_short)
   :slug        (b :lds_org)
   :chapters    (b :num_chapters)
   :verses      (b :num_verses)})

(defn render-books [volume]
  (let [b (db/get-books volume)]
    (if (nil? b)
      ;; Not found
      {:error "Not found"}
      ;; Render
      (for [i (range (count b))]
        (template (b i))))))

(defn render [book]
  (let [b (db/get-book book)]
    (if (nil? b)
      ;; Not found
      {:error "Not found"}
      ;; Render
      (template b))))
