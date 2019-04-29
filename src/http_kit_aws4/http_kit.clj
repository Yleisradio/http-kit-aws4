(ns http-kit-aws4.http-kit
  (:require [clojure.string :as str]
            [buddy.core.codecs :as codecs]
            [buddy.core.hash :as hash]
            [http-kit-aws4.aws-credentials :refer [get-aws-credentials]]
            [http-kit-aws4.core :refer [authorization request-date x-amz-date]]
            [org.httpkit.client :as http-client])
  (:import [java.net URI]
           [javax.net.ssl SNIHostName SSLEngine SSLParameters]))

(defn- get-url-params [url]
  (second (str/split url #"\?")))

(defn- http-request-method [request]
  (-> (:method request)
      (or :get)
      (name)
      (str/upper-case)))

(defn- canonical-uri [request]
  (if-let [url (:url request)]
    (.getPath (clojure.java.io/as-url url))
    "/"))

(defn- canonical-query-string [request]
  (if (:query-params request)
    (->> (or (:query-params request) {})
         (clojure.walk/stringify-keys)
         (into (sorted-map))
         (map (fn [[k v]] (str k "=" v)))
         (str/join "&"))
    (get-url-params (:url request))))

(defn- normalized-headers [request]
  (->> (or (:headers request) {})
       (remove (fn [[k v]] (nil? v)))
       (map (fn [[k v]]
              [(str/lower-case k)
               (str/trim v)]))
       (sort-by first)))

(defn- canonical-headers [request]
  (->> (normalized-headers request)
       (map (fn [[k v]] (str k ":" v "\n")))
       (str/join)))

(defn- signed-headers [request]
  (->> (normalized-headers request)
       (map first)
       (str/join ";")))

(defn- hashed-payload [request]
  (->> (or (:body request) "")
       (hash/sha256)
       (codecs/bytes->hex)))

(defn- url->host [url]
  (.getHost (clojure.java.io/as-url url)))

(defn- request->host [request]
  (or (get-in request [:headers "Host"])
      (url->host (:url request))))

(defn canonical-request
  "Build a canonical request string from the given http-kit request map.

  CanonicalRequest =
    HTTPRequestMethod + '\n' +
    CanonicalURI + '\n' +
    CanonicalQueryString + '\n' +
    CanonicalHeaders + '\n' +
    SignedHeaders + '\n' +
    HexEncode(Hash(RequestPayload))

  Reference: https://docs.aws.amazon.com/general/latest/gr/sigv4-create-canonical-request.html"
  [request]
  (->> request
       ((juxt http-request-method
              canonical-uri
              canonical-query-string
              canonical-headers
              signed-headers
              hashed-payload))
       (str/join "\n")))

(defn- ensure-host-header [request]
  (assoc-in request [:headers "Host"] (request->host request)))

(defn- authorize-request
  [access-key-id
   secret-access-key
   region
   service
   request]
  (let [request-date   (request-date (:headers request))]
    (update-in request [:headers] assoc
               "Authorization" (authorization access-key-id
                                              secret-access-key
                                              request-date
                                              region
                                              service
                                              (signed-headers request)
                                              (canonical-request request))
               "x-amz-date"    (x-amz-date request-date))))

(defn signed-request
  [access-key-id
   secret-access-key
   region
   service
   request]
  (->> request
       (ensure-host-header)
       (authorize-request access-key-id secret-access-key region service)))

(defn- with-session-token [session-token request]
  (if session-token
    (update-in request [:headers] merge {"X-Amz-Security-Token" session-token})
    request))

(defn- sni-enabled-ssl-configurer
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setSSLParameters ssl-engine ssl-params)
    (.setUseClientMode ssl-engine true)))

(defn- sni-enabled-client
  []
  (http-client/make-client {:ssl-configurer sni-enabled-ssl-configurer}))

(defonce client (sni-enabled-client))

(defn aws4-request [region service options]
  (let [{:keys [access-key-id secret-access-key token]} (get-aws-credentials)
        options (assoc options :client client)]
    (->> options
         (with-session-token token)
         (signed-request access-key-id
                         secret-access-key
                         region
                         service)
         (http-client/request))))
