(defproject xyzzy "0.1.0-SNAPSHOT"
  :description "Smarter zippers for Clojure"
  :url "http://github.com/mkremins/xyzzy"
  :dependencies [[org.clojure/clojure "1.6.0"]]
  :plugins [[com.keminglabs/cljx "0.3.2"]]
  :cljx {:builds [{:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :clj}
                  {:source-paths ["src"]
                   :output-path "target/classes"
                   :rules :cljs}]})
