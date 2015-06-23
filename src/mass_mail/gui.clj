(ns mass-mail.gui
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [clojure.string :as s])
  (:require [seesaw.core :as seesaw])
  (require [mass-mail.core :refer :all]))

(use 'seesaw.mig)
(use 'seesaw.core)
(use 'seesaw.chooser)

(declare check-test-mode)
(declare default-mode-state)
(declare test-mode-state)
(declare pre-send-mode-state)
(declare sending-mode-state)

(def test-mode-file (atom "emails-test.csv"))

(def bar
  (progress-bar :orientation :horizontal :min 0 :max 100 :value 0 :paint-string? true :size [80 :by 25]))

(add-watch progress :prog
           (fn [k r old-value new-value]
             (seesaw/config! bar :value new-value)))

(def file-field
  (seesaw/text :text "/" :columns 30 :editable? false))

(def email-field
  (seesaw/text :text "clojure.research@gmail.com" :columns 30))

(def password-field
  (seesaw/password :text "clojure123" :echo-char \* :columns 20))

(def name-field
  (seesaw/text :columns 20))

(def subject-field
  (seesaw/text :columns 20))

(def content-field
  (seesaw/text :multi-line? true :columns 60 :rows 10))

(defn set-email-values
  []
  (do
    (reset! email (seesaw/value email-field))
    (reset! pass (seesaw/value password-field))
    (reset! subject (seesaw/value subject-field))
    (reset! body (seesaw/value content-field))
    (reset! sender-name (seesaw/value name-field)))
)

(def send-button
  (seesaw/button
    :text "Send"
    :enabled? false
    :size [150 :by 50] :listen [:action (fn [e] (do
                                                  (seesaw/config! bar :max (count (read-file (seesaw/value file-field))))
                                                  (reset! progress 0)
                                                  (sending-mode-state)

                                                  (set-email-values)

                                                  (future
                                                    (log-results (seesaw/value file-field))
                                                    (default-mode-state))))]))

(defn error-state
  [file-instance]
  (alert (str "There are erros in the lines: " (apply str (interpose ", " (errors (read-file file-instance))))
              ".\n Go back and fix it before continue."))
  (default-mode-state))

(defn warning-state
  [file-instance]
  (alert (str "There are warnings in the lines: " (apply str (interpose ", " (missing-name-or-city (read-file file-instance))))
              ".\n You can continue at your own risk."))
  (pre-send-mode-state))

(defn check-file
  [file error-type]
  (seq (error-type (read-file file))))

(defn choose-csv-file
  []
  (when-let [file-instance (choose-file :filters [["CSV File" ["csv"] (constantly true)]
                                             ["Folders" #(.isDirectory %)]
                                             ])]
    (if (csv? (str file-instance))
      file-instance
      (do
        (alert "Choose a CSV (Comma-separated values) file.")
        (seesaw/config! file-field :text "/")
        (seesaw/config! send-button :enabled? false)
        false))))

(defn ensure-no-errors
  [file]
  (if (check-file file errors)
    (do (error-state file)
        nil)
    file))

(defn show-warnings
  [file]
  (if (check-file file missing-name-or-city)
    (do (warning-state file)
        file)
    file))

(defn set-file-field!
  [file]
  (seesaw/config! file-field :text (str file))
  (pre-send-mode-state))

(defn search-action-action
  [e]
  (when-let [file (choose-csv-file)]
    (some-> file
            ensure-no-errors
            show-warnings
            set-file-field!)))

(def search-action
  (seesaw/button
    :text "Browse file"
    :size [80 :by 25]
    :enabled? true
    :listen [:action search-action-action]))

(def config-test
  (menu-item
    :text "Change emails test file"
    :listen [:action (fn [e]
                       (when-let [file (choose-csv-file)]
                         (reset! test-mode-file (str file))))]))

(def check-test-mode (seesaw/checkbox
                       :text "Test mode"
                       :selected? false
                       :listen [:action (fn [e] (do
                                                  (if (seesaw/config check-test-mode :selected?)
                                                    (test-mode-state)
                                                    (default-mode-state))))]))

(defn default-mode-state
  []
  (do
    (seesaw/config! search-action :enabled? true)
    (seesaw/config! file-field :text "/")
    (seesaw/config! send-button :enabled? false)
    (seesaw/config! config-test :enabled? true)
    (seesaw/config! check-test-mode :enabled? true)))

(defn pre-send-mode-state
  []
  (do
    (seesaw/config! send-button :enabled? true)))

(defn test-mode-state
  []
  (do
    (seesaw/config! search-action :enabled? false)
    (seesaw/config! file-field :text @test-mode-file)
    (seesaw/config! send-button :enabled? true)))

(defn sending-mode-state
  []
  (do
    (seesaw/config! search-action :enabled? false)
    (seesaw/config! send-button :enabled? false)
    (seesaw/config! config-test :enabled? false)
    (seesaw/config! check-test-mode :enabled? false)))

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
                 :items [content-field
                         bar]))

(def panel
  (mig-panel
    :items [[grid-file "wrap"]
            [grid-credentials "wrap"]
            [grid-header "wrap"]
            [grid-body "wrap"]
            [send-button "center"]]))

(defn display
  [content]
  (let [window (seesaw/frame :title "Mass Email" :on-close :exit)]
    (-> window
        (seesaw/config! :content content
                        :menubar
                        (menubar :items
                                 [(menu :text "Configs" :items [config-test])
                                  ])) (seesaw/pack!) (seesaw/show!))))