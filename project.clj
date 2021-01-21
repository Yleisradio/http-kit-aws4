(defproject http-kit-aws4 "0.3.0"
  :description "AWS Request Signing v4 for http-kit"
  :url "https://github.com/Yleisradio/http-kit-aws4"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.memoize "1.0.236"]
                 [buddy "2.0.0"]
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.0"]
                 [http-kit "2.4.0-alpha3"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]
                   :plugins [[jonase/eastwood "0.3.11"]
                             [lein-kibit "0.1.5"]
                             [lein-cljfmt "0.5.7"]]
                   :cljfmt {:indents {around   [[:inner 0]]
                                      context  [[:inner 0]]
                                      describe [[:inner 0]]
                                      it       [[:inner 0]]
                                      should=  [[:block 0]]}}
                   }}
  :plugins [[fi.yle.tools/aws-maven "1.4.2"]
            [speclj "3.3.2"]]
  :repositories [["yle-public" "https://maven.yle.fi/release"]]
  :test-paths ["spec"]
  :aliases {"lint" ["with-profile" "dev" "do" ["cljfmt" "check"] ["eastwood"] ["kibit"]]})
