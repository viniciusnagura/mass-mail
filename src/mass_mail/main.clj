(ns mass-mail.main
  (:gen-class :main true)
  (require [mass-mail.gui :refer [display panel]]))

(defn -main [& args]
  (display panel)
  )