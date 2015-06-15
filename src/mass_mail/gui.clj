(ns mass-mail.gui
  (:gen-class :main true)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as s])
  (:require [seesaw.core :as seesaw])
  (require [mass-mail.core :refer [send-email]]))

(use 'seesaw.mig)
(use 'seesaw.core)
(use 'seesaw.chooser)

(def file-field
  (seesaw/text :text "Choose the file" :columns 30 :editable? false))

(def search-action
  (seesaw/button
    :text "Search file"
    :size [80 :by 25] :listen [:action (fn [e] (if-let [f (choose-file)]
                                                  (do
                                                    (seesaw/config! file-field :text (str f))
                                                    )))]))

(def email-field
  (seesaw/text :text "Email" :columns 30))

(def password-field
  (seesaw/password :text "password" :echo-char \* :columns 20))

(def name-field
  (seesaw/text :text "Name" :columns 20))

(def subject-field
  (seesaw/text :text "Subject" :columns 20))

(def content-field
  (seesaw/text :text "Content of the email" :multi-line? true :columns 60 :rows 10))

(def send-button
  (seesaw/button
    :text "Send email"
    :size [150 :by 50] :listen [:action (fn [e] (do (send-email (seesaw/value file-field) (seesaw/value email-field)
                                                                (seesaw/value password-field)
                                                                (seesaw/value subject-field) (seesaw/value content-field)))
                                          )]))

(defn display
  [content]
  (let [window (seesaw/frame :title "Mass Email" :on-close :exit)]
    (-> window
        (seesaw/config! :content content) (seesaw/pack!) (seesaw/show!))))

(def grid-file (seesaw/grid-panel
                    :border "Choose a file"
                    :columns 2
                    :items [file-field search-action]))

(def grid-credentials (seesaw/grid-panel
                 :border "Credentials"
                 :columns 2
                 :items [email-field password-field]))

(def grid-header (seesaw/grid-panel
                 :border "Email header"
                 :columns 2
                 :items [name-field subject-field]))

(def grid-body (seesaw/grid-panel
                 :border "E-mail"
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