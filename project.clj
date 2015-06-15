(defproject mass-mail "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.draines/postal "1.11.3"]
                 [seesaw "1.4.4"]
                 [semantic-csv "0.1.0"]]
  :main ^:skip-aot mass-mail.gui
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})