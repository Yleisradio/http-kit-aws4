# http-kit-aws4

AWS Request Signing v4 for http-kit

Provides `aws4-request`,
a [aws4-signature](https://docs.aws.amazon.com/general/latest/gr/signature-version-4.html)-providing wrapper
for [org.httpkit.client/request](http://www.http-kit.org/client.html#options)

## Installation
In `project.clj`:
```clojure
 (defproject your-project
   :repositories [["http-kit-aws4" "https://maven.pkg.github.com/yleisradio/http-kit-aws4"]]
   :dependencies [[http-kit-aws4 "0.2.0"]])
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

## License
This library is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).

## Acknowledgements
This project was inspired by and modeled after [sharetribe/aws-sig4](https://github.com/sharetribe/aws-sig4) - a [clj-http](https://github.com/dakrone/clj-http) middleware for signing AWS requests. Thank you.
