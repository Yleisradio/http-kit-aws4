(ns http-kit-aws4.aws-credentials
  (:require [cheshire.core :as json]
            [org.httpkit.client :as http-client]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]))

(def ^:private aws-container-credentials-url
  (when-let [relative-uri (System/getenv "AWS_CONTAINER_CREDENTIALS_RELATIVE_URI")]
    (str "http://169.254.170.2" relative-uri)))

(defn- get-aws-container-credentials [url]
  (let [{:keys [status body error]} @(http-client/request
                                      {:url url
                                       :method :get
                                       :timeout 1000})]
    (cond (some? error) (throw error)
          (not (<= 200 status 299)) (throw (ex-info (str "Expected status 2xx, got " status)
                                                    {:error error :body body}))
          :else (json/parse-string body ->kebab-case-keyword))))

(def ^:private aws-environment-credentials
  {:access-key-id      (System/getenv "AWS_ACCESS_KEY_ID")
   :secret-access-key  (System/getenv "AWS_SECRET_ACCESS_KEY")
   :token              (System/getenv "AWS_SESSION_TOKEN")})

(defn get-aws-credentials
  "Returns a map of AWS credentials provided by (in order of precedence)
  - AWS ECS Agent, via AWS_CONTAINER_CREDENTIALS_RELATIVE_URI, when running in an ECS container
  - environment variables AWS_ACCESS_KEY_ID etc"
  []
  (if aws-container-credentials-url
    (get-aws-container-credentials aws-container-credentials-url)
    aws-environment-credentials))
