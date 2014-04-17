(ns lds-scriptures-api.core
  (:use lds-scriptures-api.handler)
  (:require [appengine-magic.core :as ae]))

(ae/def-appengine-app lds-scriptures-api-app #'app)
