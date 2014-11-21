(defproject time-it "0.1.0-SNAPSHOT"
  :description "Web application allowing the logging of working time."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-json "0.3.1"]
                 [compojure "1.2.1"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler dictive.time-it.core/app
         :port 3060
         :auto-reload? true})
