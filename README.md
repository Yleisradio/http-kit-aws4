# http-kit-aws4

AWS Request Signing v4 for http-kit

Provides `aws4-request`,
a [aws4-signature](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html)-providing wrapper
for [org.httpkit.client/request](http://www.http-kit.org/client.html#options)

## Usage
```clojure
(:require [http-kit-aws4.http-kit :refer [aws4-request]])
(aws4-request
  "eu-west-1"
  "iam"
  {:url "https://iam.amazonaws.com/"
   :method :get
   :query-params {"Action" "ListUsers"
                  "Version" "2010-05-08"}})
```
