(ns http-kit-aws4.core-spec
  (:require [speclj.core :refer :all]
            [http-kit-aws4.core :refer [string-to-sign signing-key signature authorization request-date]]
            [buddy.core.codecs :as codecs]
            [clj-time.core :as t]))

(describe "string-to-sign"
  (it "post-x-www-form-urlencoded"
    (let [canonical-request "POST\n/\n\ncontent-type:application/x-www-form-urlencoded\ndate:Mon, 09 Sep 2011 23:36:00 GMT\nhost:host.foo.com\n\ncontent-type;date;host\n3ba8907e7a252327488df390ed517c45b96dead033600219bdca7107d1d3f88a"
          expected "AWS4-HMAC-SHA256\n20110909T233600Z\n20110909/us-east-1/host/aws4_request\n4c5c6e4b52fb5fb947a8733982a8a5a61b14f04345cbfe6e739236c76dd48f74"]
      (should=
        expected
        (string-to-sign (t/date-time 2011 9 9 23 36 0)
                        "us-east-1"
                        "host"
                        canonical-request)))))

(describe "signing-key"
  (it "example"
    (should=
      "c4afb1cc5771d871763a393e44b703571b55cc28424d1a5e86da6ed3c154a4b9"
      (let [secret-access-key "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
            request-date (t/date-time 2015 8 30 0 0 0)
            region "us-east-1"
            service "iam"]
        (->> (signing-key secret-access-key request-date region service)
             (codecs/bytes->hex))))))

(describe "signature"
  (it "post-x-www-form-urlencoded"
    (let [secret-access-key "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
          request-date (t/date-time 2011 9 9 23 36 0)
          region "us-east-1"
          service "host"
          skey (signing-key secret-access-key request-date region service)
          sts "AWS4-HMAC-SHA256\n20110909T233600Z\n20110909/us-east-1/host/aws4_request\n4c5c6e4b52fb5fb947a8733982a8a5a61b14f04345cbfe6e739236c76dd48f74"]
      (should=
        "5a15b22cf462f047318703b92e6f4f38884e4a7ab7b1d6426ca46a8bd1c26cbc"
        (signature skey sts)))))

(describe "authorization"
  (it "post-x-www-form-urlencoded"
    (should=
      "AWS4-HMAC-SHA256 Credential=AKIDEXAMPLE/20110909/us-east-1/host/aws4_request, SignedHeaders=content-type;date;host, Signature=5a15b22cf462f047318703b92e6f4f38884e4a7ab7b1d6426ca46a8bd1c26cbc"
      (authorization "AKIDEXAMPLE"
                     "wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY"
                     (t/date-time 2011 9 9 23 36 0)
                     "us-east-1"
                     "host"
                     "content-type;date;host"
                     "POST\n/\n\ncontent-type:application/x-www-form-urlencoded\ndate:Mon, 09 Sep 2011 23:36:00 GMT\nhost:host.foo.com\n\ncontent-type;date;host\n3ba8907e7a252327488df390ed517c45b96dead033600219bdca7107d1d3f88a"))))

(describe "request-date"
  (it "use Date header if present"
    (should=
      (t/date-time 2011 9 9 23 36 0)
      (request-date {"Date" "Fri, 09 Sep 2011 23:36:00 GMT"}))))
