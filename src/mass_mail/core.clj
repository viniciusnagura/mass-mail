(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]))
(import 'org.apache.commons.mail.SimpleEmail)

(defn send-email [list-of-emails email-address account-password message-subject message-body]
  (def list list-of-emails)
  (while (not (empty? list))
    ()))

(defn email-sender [email-to email-address account-password message-subject message-body]
  (doto (SimpleEmail.)
    (.setHostName "smtp.gmail.com")
    (.setSslSmtpPort "465")
    (.setSSL true)
    (.addTo email-to)
    (.setFrom email-address "Qualquer coisa")
    (.setSubject message-subject)
    (.setMsg message-body)
    (.setAuthentication email-address account-password)
    (.send)))

(defn set-infos
  "Set all the informations that were given through command line"
  [opts]
  (let [list-of-emails (->> (clojure.string/split (slurp (get opts :file)) #"\n")
                            (map #(clojure.string/split % #":")))
        email-address (get opts :email)
        account-password (get opts :password)
        message-subject (get opts :subject)
        message-body (get opts :body)]
    (send-email list-of-emails email-address account-password message-subject message-body)))

(defn -main
  "Read the list of email addresses and set the email informations"
  [& args]
  (let [[opts args banner]
        (cli args
             ["-f" "--file" "REQUIRED: file containing the list of email addresses"]
             ["-e" "--email" "REQUIRED: email address where the message will be sent from"]
             ["-p" "--password" "REQUIRED: password for email account"]
             ["-s" "--subject" "NOT REQUIRED: subject of the message" :default ""]
             ["-b" "--body" "NOT REQUIRED: body of the message" :default ""])]
    (if (and
          (:file opts)
          (:email opts)
          (:password opts)
          (:subject opts)
          (:body opts))
      (set-infos opts))))