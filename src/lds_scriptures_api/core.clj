(ns lds-scriptures-api.core
  (:require [lds-scriptures-api.db :as db])
  (:use [lds-scriptures-api.views.volume]))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))

(defn -main [& args]
  (println (works))
  (println (count (db/get-chapter 1 :1_ne :bm))))
