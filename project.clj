(defproject http-kit-aws4 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main http-kit-aws4.core
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [buddy "1.3.0"]
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.7.1"]
                 [clj-time "0.14.0"]
                 [yleisradio/http-kit "2.2.0-sni-support"]]
  :profiles {:dev {:dependencies [[speclj "3.3.2"]]}}
  :plugins [[speclj "3.3.2"]]
  :test-paths ["spec"])
