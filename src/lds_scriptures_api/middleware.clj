(ns lds-scriptures-api.middleware)

;; Define the CORS set, this will allow everything from everyone
(def cors-headers
  {"Access-Control-Allow-Headers" "Content-Type, Authorization"
   "Access-Control-Allow-Methods" "PUT, GET, POST, DELETE, OPTIONS"
   "Access-Control-Allow-Origin"  "*"})

;; Define the JSON header set
(def json-headers
  {"Content-Type" "application/json"})

;; Generic Request Wrapper
(defn generic-request [handler headers]
  (fn [request]
    (let [response (handler request)]
      (assoc response :headers
        (merge (:headers response) headers)))))

;; Wrap-Cors inspired by ring-cors/wrap-cors
(defn wrap-cors [handler]
  (generic-request handler cors-headers))

;; Wrap-Json because we need to
(defn wrap-json [handler]
  (generic-request handler json-headers))

;; Ignore trailing slash
(defn with-ignore-trailing-slash [handler]
  (fn [request]
    (let [uri       (request :uri)
          clean-uri (if (and (not (= "/" uri)) (.endsWith uri "/"))
                      (subs uri 0 (- (count uri) 1))
                      uri)]
      (handler (assoc request :uri clean-uri)))))
