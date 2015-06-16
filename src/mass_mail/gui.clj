(ns mass-mail.gui
  (:gen-class :main true)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as s])
  (:require [seesaw.core :as seesaw])
  (require [mass-mail.core :refer [send-email warnings read-file errors ]]))

(use 'seesaw.mig)
(use 'seesaw.core)
(use 'seesaw.chooser)

(def test-mode-file (atom "emails-test.csv"))

(def file-field
  (seesaw/text :text "/" :columns 30 :editable? false))

(def email-field
  (seesaw/text :columns 30))

(def password-field
  (seesaw/password :echo-char \* :columns 20))

(def name-field
  (seesaw/text :columns 20))

(def subject-field
  (seesaw/text :columns 20))

(def content-field
  (seesaw/text :multi-line? true :columns 60 :rows 10))

(def send-button
  (seesaw/button
    :text "Send"
    :enabled? false
    :size [150 :by 50] :listen [:action (fn [e] (do (send-email (seesaw/value file-field) (seesaw/value email-field)
                                                                (seesaw/value password-field)
                                                                (seesaw/value subject-field) (seesaw/value content-field)))
                                          )]))

(def config-test
  (menu-item
    :text "Change emails test file"
    :listen [:action (fn [e] (if-let [f (choose-file :filters [["CSV File" ["csv"] (constantly true)]
                                                               ["Folders" #(.isDirectory %)]
                                                               ])]
                               (do
                                 (reset! test-mode-file (str f))
                                 )))])
)

(defn display
  [content]
  (let [window (seesaw/frame :title "Mass Email" :on-close :exit
                             )]
    (-> window
        (seesaw/config! :content content
                        :menubar
                        (menubar :items
                                 [(menu :text "Configs" :items [config-test])
                                  ])) (seesaw/pack!) (seesaw/show!))))

(def search-action
  (seesaw/button
    :text "Browse file"
    :size [80 :by 25]
    :enabled? true
    :listen [:action (fn [e] (if-let [f (choose-file :filters [["CSV File" ["csv"] (constantly true)]
                                                               ["Folders" #(.isDirectory %)]
                                                               ])]
                                                 (do
                                                   (seesaw/config! file-field :text (str f))
                                                   (if (not (empty? (errors (read-file f))))
                                                     (do (alert (str "There are erros in the lines: " (apply str (interpose ", " (errors (read-file f)))) ".\n Go back and fix it before continue."))
                                                         (seesaw/config! send-button :enabled? false)
                                                     )
                                                     (if (not (empty? (warnings (read-file f))))
                                                       (do (alert (str "There are warnings in the lines: " (apply str (interpose ", " (warnings (read-file f)))) ".\n You can continue at your own risk."))
                                                           (seesaw/config! send-button :enabled? true)
                                                       )
                                                       (seesaw/config! send-button :enabled? true)
                                                       )))))]))

(def check-test-mode (seesaw/checkbox
                       :text "Test mode"
                       :selected? false
                       :listen [:action (fn [e] (if (seesaw/config check-test-mode :selected?)
                                                  (do
                                                    (seesaw/config! search-action :enabled? false)
                                                    (seesaw/config! file-field :text @test-mode-file)
                                                    (seesaw/config! send-button :enabled? true))
                                                  (do
                                                    (seesaw/config! search-action :enabled? true)
                                                    (seesaw/config! file-field :text "/")
                                                    (seesaw/config! send-button :enabled? false))))]
                       )
  )

(def grid-file (seesaw/grid-panel
                    :border "Choose a file"
                    :columns 2
                    :items [file-field search-action]))

(def grid-credentials (seesaw/grid-panel
                 :border "Credentials"
                 :columns 2
                 :items ["E-mail:" "Password:"
                         email-field password-field]))

(def grid-header (seesaw/grid-panel
                 :border "Email header"
                 :columns 3
                 :items ["Name:" "Subject:" ""
                         name-field subject-field check-test-mode]))

(def grid-body (seesaw/grid-panel
                 :border "E-mail body"
                 :columns 1
                 :items [content-field]))

(def panel
  (mig-panel
    :items [[grid-file "wrap"]
            [grid-credentials "wrap"]
            [grid-header "wrap"]
            [grid-body "wrap"]
            [send-button "center"]
            ]
  )
)

(defn -main [& args]
  (display panel)
  )