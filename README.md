# http-kit-aws4

AWS Request Signing v4 for http-kit

Provides `aws4-request`,
a [aws4-signature](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html)-providing wrapper
for [org.httpkit.client/request](http://www.http-kit.org/client.html#options)

## Installation
To access Yle private maven repository in S3, place this `.s3_config` in project root:
```
AWS_ASSUME_ROLE_NAME=dev
AWS_ASSUME_ROLE_ARN=arn:aws:iam::352476883983:role/dev
```

Then, in `project.clj`:
```clojure
 (defproject your-project
   :plugins [[fi.yle.tools/aws-maven "1.4.0"]]
   :repositories [["yle-public" "http://maven.c4.yle.fi/release"]
                  ["yle-private" "s3://yle-maven-private/release"]]
   :dependencies [[http-kit-aws4 "0.1.0"]])
```

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

### Credentials
Uses AWS credentials provided run-time by (in order of precedence)
- AWS ECS Agent, via AWS_CONTAINER_CREDENTIALS_RELATIVE_URI, when running in an ECS container
- environment variables AWS_ACCESS_KEY_ID etc

## Contributing
Bug reports and pull requests are welcome on GitHub at https://github.com/Yleisradio/http-kit-aws4. This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the [Contributor Covenant](http://contributor-covenant.org) code of conduct.

## Acknowledgements
This project was inspired by and modeled after [sharetribe/aws-sig4](https://github.com/sharetribe/aws-sig4) - a [clj-http](https://github.com/dakrone/clj-http) middleware for signing AWS requests. Thank you.
