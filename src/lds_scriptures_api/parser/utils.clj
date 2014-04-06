(ns lds-scriptures-api.parser.utils
  (:require [lds-scriptures-api.db :as db])
  (:use [net.cgrand.enlive-html])
  (:use [lds-scriptures-api.parser.dictionary]))

;;
;; Utils
;;

(defn file->html
  "Get html given some file path"
  ([file]
  (html-resource
    (java.io.StringReader. (slurp file)))))

;;
;; DB Utils
;;
(defn book-id [heading]
  (-> (db/get-book (tite->slug heading)) :id))
