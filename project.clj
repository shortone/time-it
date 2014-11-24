(defproject time-it "0.1.0-SNAPSHOT"
  :description "A little web application allowing people to log work time."
  :url "https://github.com/shortone/time-it"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.2.1"]
                 [com.ashafa/clutch "0.4.0"]
                 [crypto-password "0.1.3"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler dictive.time-it.core/app
         :port 3060
         :auto-reload? true}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
