(ns mass-mail.gui
  (:gen-class :main true)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as s])
  (:require [seesaw.core :as seesaw]))
(use 'seesaw.core)
(use 'seesaw.chooser)


(def search-action
  (seesaw/button
    :text "Search file..."
    :size [150 :by 50]))

(def file-field
  (seesaw/text :text "Choose the file with the list of email addresses" :columns 40 :editable? false))

(def content-field
  (seesaw/text :text "Content of the email" :multi-line? true :size [150 :by 120]))

(def send-button
  (seesaw/button
    :text "Send email"
    :size [150 :by 50]))

(def two-widgets
  (let [message file-field search-file search-action]
    (seesaw/config! search-action :listen [:action (fn [e] (if-let [f (choose-file)]
                                                           (do
                                                             (seesaw/config! file-field :text (str f))
                                                             (seesaw/config! content-field :text f)
                                                             )))])
    (seesaw/left-right-split message search-file)
    ))

(def three-widgets
  (let [part-one two-widgets part-two content-field]
    (seesaw/top-bottom-split part-one part-two)))

(defn four-widgets []
  (let [part-one three-widgets part-two send-button]
    (seesaw/top-bottom-split part-one part-two)))

(defn display
  [content]
  (let [window (seesaw/frame :title "Mass Email")]
    (-> window
      (seesaw/config! :content content) (seesaw/pack!) (seesaw/show!))))

(defn -main [& args]
  (display (four-widgets))
  )

