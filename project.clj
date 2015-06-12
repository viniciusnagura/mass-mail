(defproject mass-mail "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.apache.commons/commons-email "1.2"]
                 [com.draines/postal "1.11.3"]]
  :repositories [["central-proxy" "http://repository.sonatype.org/content/repositories/central/"]]
  :main ^:skip-aot mass-mail.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})