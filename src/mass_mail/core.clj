(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]
           [postal.core :refer [send-message]]))

(defn send-email
  "Set all the informations that were given through command line"
  [opts]
  (let [list-of-emails (->> (clojure.string/split (slurp (get opts :file)) #"\n")
                            (map #(clojure.string/split % #":")))
        name (get opts :name)
        email-address (get opts :email)
        account-password (get opts :password)
        message-subject (get opts :subject)
        message-body (get opts :body)
        conn {:host "smtp.gmail.com"
              :ssl true
              :user email-address
              :pass account-password}]

    ;(dorun (map #(email-sender (second %) name email-address account-password message-subject message-body) list-of-emails))
    (mapv #(send-message conn {:from email-address
                               :to (second %)
                               :subject message-subject
                               :body message-body}
                               ) list-of-emails)
    )
  )

(defn send-email-from-repl
  "Set all the informations that were given through command line"
  [file name email password subject body]
  (let [list-of-emails (->> (clojure.string/split (slurp file) #"\n")
                            (map #(clojure.string/split % #":")))
        name name
        email-address email
        account-password password
        message-subject subject
        message-body body
        conn {:host "smtp.gmail.com"
              :ssl true
              :user email-address
              :pass account-password}]

    (mapv #(send-message conn {:from email-address
                               :to (second %)
                               :subject message-subject
                               :body message-body
                               :user-agent name}) list-of-emails)
    )
  )

(defn -main
  "Read the list of email addresses and set the email informations"
  [& args]
  (let [[opts args banner]
        (cli args
             ["-f" "--file" "REQUIRED: file containing the list of email addresses"]
             ["-n" "--name" "NOT REQUIRED: name" :default ""]
             ["-e" "--email" "REQUIRED: email address where the message will be sent from"]
             ["-p" "--password" "REQUIRED: password for email account"]
             ["-s" "--subject" "NOT REQUIRED: subject of the message" :default ""]
             ["-b" "--body" "NOT REQUIRED: body of the message" :default ""])]
    (if (and
          (:file opts)
          (:name opts)
          (:email opts)
          (:password opts)
          (:subject opts)
          (:body opts))
      (send-email opts))))