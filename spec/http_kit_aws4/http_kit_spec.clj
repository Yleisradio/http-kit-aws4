(ns http-kit-aws4.http-kit-spec
  (:require [speclj.core :refer :all]
            [http-kit-aws4.http-kit :refer [canonical-request signed-request]])
  (:import (org.joda.time DateTimeZone DateTime)))

(describe "normalized-headers"

  (it "omits header with nil value"
    (should=
      [["x-not-borked" "foo"]]
      (#'http-kit-aws4.http-kit/normalized-headers {:headers {"x-borked" nil
                                                         "x-not-borked" "foo"}}))))

;; https://docs.aws.amazon.com/general/latest/gr/signature-v4-test-suite.html#signature-v4-test-suite-example
(describe "canonical-request"

  (it "A Simple GET Request with query-params"
    (let [expected "GET\n/\nParam1=value1&Param2=value2\nhost:example.amazonaws.com\nx-amz-date:20150830T123600Z\n\nhost;x-amz-date\ne3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"]
      (should=
        expected
        (canonical-request {:url "http://example.amazonaws.com/"
                            :method :get
                            :headers {"X-Amz-Date" "20150830T123600Z"
                                      "Host" "example.amazonaws.com"}
                            :query-params {"Param2" "value2"
                                           "Param1" "value1"}}))))

  (it "post-x-www-form-urlencoded"
    (let [expected "POST\n/\n\ncontent-type:application/x-www-form-urlencoded\ndate:Mon, 09 Sep 2011 23:36:00 GMT\nhost:host.foo.com\n\ncontent-type;date;host\n3ba8907e7a252327488df390ed517c45b96dead033600219bdca7107d1d3f88a"]
      (should=
        expected
        (canonical-request {:url "http://host.foo.com/"
                            :method :post
                            :headers {"Content-Type" "application/x-www-form-urlencoded"
                                      "Date" "Mon, 09 Sep 2011 23:36:00 GMT"
                                      "Host" "host.foo.com"}
                            :body "foo=bar"})))))

(describe "signed-request"
  (it "post-x-www-form-urlencoded"
    (should=
      "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/host/aws4_request, SignedHeaders=content-type;date;host, Signature=bc1ffe73a885c1e92901e64c5ede7fa7435ab508efc46c24cc83efe6f7dc789d"
      (-> (signed-request "AKIDEXAMPLE"
                          "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
                          "us-east-1"
                          "host"
                          {:url "http://host.foo.com/"
                           :method :post
                           :headers {"Content-Type" "application/x-www-form-urlencoded"
                                     "Date" "Fri, 09 Sep 2011 23:36:00 GMT"
                                     "Host" "host.foo.com"}
                           :body "foo=bar"})
          (get-in [:headers "Authorization"]))))

  (it "add x-amz-date header"
    (should=
      "20110909T233600Z"
      (-> (signed-request "AKIDEXAMPLE"
                          "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
                          "us-east-1"
                          "host"
                          {:url "http://host.foo.com/"
                           :method :post
                           :headers {"Content-Type" "application/x-www-form-urlencoded"
                                     "Date" "Fri, 09 Sep 2011 23:36:00 GMT"
                                     "Host" "host.foo.com"}
                           :body "foo=bar"})
          (get-in [:headers "x-amz-date"]))))

  (it "persist Host header if present"
    (should=
      "host.foo.com"
      (-> (signed-request "AKIDEXAMPLE"
                          "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
                          "us-east-1"
                          "host"
                          {:url "http://other.foo.com/"
                           :headers {"Host" "host.foo.com"}})
          (get-in [:headers "Host"]))))

  (it "derive Host header from url if missing"
    (should=
      "host.foo.com"
      (-> (signed-request "AKIDEXAMPLE"
                          "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
                          "us-east-1"
                          "host"
                          {:url "http://host.foo.com/"})
          (get-in [:headers "Host"])))))
