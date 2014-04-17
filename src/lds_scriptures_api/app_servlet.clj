(ns lds-scriptures-api.app_servlet
  (:gen-class :extends javax.servlet.http.HttpServlet)
  (:use lds-scriptures-api.core)
  (:use [appengine-magic.servlet :only [make-servlet-service-method]]))


(defn -service [this request response]
  ((make-servlet-service-method lds-scriptures-api-app) this request response))
