(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]
           [postal.core :refer [send-message]]))

(import org.apache.commons.mail.SimpleEmail)


(defn email-sender [email-to name email-address account-password message-subject message-body]
  (doto (SimpleEmail.)
    (.setHostName "smtp.gmail.com")
    (.setSslSmtpPort "465")
    (.setSSL true)
    (.addTo email-to)
    (.setFrom email-address name)
    (.setSubject message-subject)
    (.setMsg message-body)
    (.setAuthentication email-address account-password)
    (.send)))

(defn set-infos
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


    ; (loop [emails list-of-emails]
    ; (when (not (empty? emails))
    ;(email-sender (second (first emails)) name email-address account-password message-subject message-body)
    ; (recur (rest emails))
    ;)
    ;)
    ;(dorun (map #(email-sender (second %) name email-address account-password message-subject message-body) list-of-emails))
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
      (set-infos opts))))