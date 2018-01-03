(ns full.contacts-api-client.http
  (:require [org.httpkit.client :as http]
            [clojure.data.json :as json]
            [full.contacts-api-client.util :as util]
            [ring.util.codec :refer [form-encode]]))

(def version "1.0.0")
(def api-version "v1")
(defn get-url [endpoint]
  (str util/base-url api-version endpoint))

(defn request [endpoint & {:keys [auth json form body headers params]
                           :or   {headers {} params {}}}]
  (delay
    (let [json-req (not (nil? json))
          form-req (not (nil? form))
          res @(http/request (get-url endpoint)
                             (into
                               {:user-agent (str "Contacts API Clojure SDK v" version)
                                :headers    (into {"Content-Type"  (cond json-req "application/json"
                                                                         form-req "application/x-www-form-urlencoded"
                                                                         :else "text/plain")
                                                   "Authorization" (str "Bearer " auth)} headers)
                                :body       (cond
                                              json-req (json/write-str json)
                                              form-req (form-encode form)
                                              :else body)}
                               params))]
      {:status  (:status res)
       :headers (:headers res)
       :body    (if (re-find #"(i?)json" (get-in res [:headers :content-type]))
                  (json/read-str (:body res))
                  (:body res))})))