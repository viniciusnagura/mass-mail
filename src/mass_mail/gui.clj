(ns mass-mail.gui
  (:gen-class :main true)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as s])
  (:require [seesaw.core :as seesaw])
  (require [clojure.tools.cli :refer [cli]]
    [postal.core :refer [send-message]]
    [mass-mail.core :refer [send-email-from-repl]]))


(use 'seesaw.core)
(use 'seesaw.chooser)

(def search-action
  (seesaw/button
    :text "Search file..."
    :size [150 :by 50]))

(def file-field
  (seesaw/text :text "Choose the file with the list of email addresses" :columns 40 :editable? false))

(def name-field
  (seesaw/text :text "Name" :columns 40))

(def email-field
  (seesaw/text :text "Email" :columns 30))

(def password-field
  (seesaw/text :text "Password" :columns 20))

(def login-fields
  (let [part-one email-field part-two password-field]
    (seesaw/top-bottom-split part-one part-two)))

(def name-field
  (seesaw/text :text "Name" :columns 20))

(def subject-field
  (seesaw/text :text "Subject" :columns 20))

(def name-subject-fields
  (let [part-one name-field part-two subject-field]
    (seesaw/top-bottom-split part-one part-two)))

(def subject-login-fields
  (let [part-one name-subject-fields part-two login-fields]
    (seesaw/left-right-split part-one part-two)))

(def content-field
  (seesaw/text :text "Content of the email" :multi-line? true :size [150 :by 120]))

(def send-button
  (seesaw/button
    :text "Send email"
    :size [150 :by 50] :listen [:action (fn [e] (do (send-email-from-repl (seesaw/value file-field) (seesaw/value name-field)
                                                       (seesaw/value email-field) (seesaw/value password-field)
                                                       (seesaw/value subject-field) (seesaw/value content-field)))
                                                    )]))

(def content-send-fields
  (let [part-one content-field part-two send-button]
    (seesaw/top-bottom-split part-one part-two)))

(def search-area
  (let [message file-field search-file search-action]
    (seesaw/config! search-action :listen [:action (fn [e] (if-let [f (choose-file)]
                                                           (do
                                                             (seesaw/config! file-field :text (str f))
                                                             )))])
    (seesaw/left-right-split message search-file)
    ))

(def two-widgets
  (let [part-one search-area part-two subject-login-fields]
    (seesaw/top-bottom-split part-one part-two)))

(defn three-widgets []
  (let [part-one two-widgets part-two content-send-fields]
    (seesaw/top-bottom-split part-one part-two)))

(defn display
  [content]
  (let [window (seesaw/frame :title "Mass Email")]
    (-> window
      (seesaw/config! :content content) (seesaw/pack!) (seesaw/show!))))

(defn -main [& args]
  (display (three-widgets))
  )