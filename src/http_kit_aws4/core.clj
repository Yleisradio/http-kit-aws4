(ns http-kit-aws4.core
  (:require [clojure.string :as str]
            [buddy.core.codecs :as codecs]
            [buddy.core.hash :as hash]
            [buddy.core.mac :as mac]
            [clj-time.core :refer [now]]
            [clj-time.format :refer [formatter formatters parse unparse]])
  (:import (org.joda.time DateTime)))

(def date-formats
  {:basicDate             (formatters :basic-date)
   :basicDateTimeNoMillis (formatters :basic-date-time-no-ms)
   :rfc1132               (formatter "EEE, dd MMM yyyy HH:mm:ss z")})

(defn- request-date->string [request-date]
  (unparse (:basicDateTimeNoMillis date-formats) request-date))

(defn- credential-scope [request-date region service]
  (str/join "/"
            [(unparse (:basicDate date-formats) request-date)
             region
             service
             "aws4_request"]))

(defn- hashed-canonical-request [canonical-request]
  (->> canonical-request
       (hash/sha256)
       (codecs/bytes->hex)))

(defn- hmac [key data]
  (mac/hash data {:key key :alg :hmac+sha256}))

(defn request-date [headers]
  (if-let [date-header (get headers "Date")]
    (parse (:rfc1132 date-formats) date-header)
    (now)))

(defn string-to-sign
  "Create a string to sign

  StringToSign =
    Algorithm + \n +
    RequestDateTime + \n +
    CredentialScope + \n +
    HashedCanonicalRequest

  Reference: https://docs.aws.amazon.com/general/latest/gr/sigv4-create-string-to-sign.html"
  [^DateTime request-date region service canonical-request]
  (str/join "\n" ["AWS4-HMAC-SHA256"
                  (request-date->string request-date)
                  (credential-scope request-date region service)
                  (hashed-canonical-request canonical-request)]))

(defn signing-key
  "Derive the signing key

  Reference: https://docs.aws.amazon.com/general/latest/gr/sigv4-calculate-signature.html"
  [secret-access-key ^DateTime request-date region service]
  (-> (str "AWS4" secret-access-key)
      (hmac (unparse (:basicDate date-formats) request-date))
      (hmac region)
      (hmac service)
      (hmac "aws4_request")))

(defn signature [key data]
  (codecs/bytes->hex (hmac key data)))

(defn authorization
  "Construct the Authorization header value

  Authorization: algorithm Credential=access key ID/credential scope, SignedHeaders=SignedHeaders, Signature=signature

  Reference: https://docs.aws.amazon.com/general/latest/gr/sigv4-add-signature-to-request.html"
  [access-key-id secret-access-key request-date region service signed-headers canonical-request]
  (str "AWS4-HMAC-SHA256 Credential=" access-key-id "/" (credential-scope request-date region service) ", "
       "SignedHeaders=" signed-headers ", "
       "Signature=" (signature
                     (signing-key secret-access-key request-date region service)
                     (string-to-sign request-date region service canonical-request))))

(defn x-amz-date [^DateTime dt]
  (unparse (:basicDateTimeNoMillis date-formats) dt))
