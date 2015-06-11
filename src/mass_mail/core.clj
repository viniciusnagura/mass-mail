(ns mass-mail.core
  (:gen-class)
  (use clojure.java.io)
  (require [clojure.tools.cli :refer [cli]]))

(defn set-infos
  "Set all the informations that were given through command line"
  [opts]
  (let [list-of-emails (->> (clojure.string/split (slurp (get opts :file)) #"\n")
                            (map #(clojure.string/split % #":")))
        email-address (get opts :email)
        account-password (get opts :password)
        message-subject (get opts :subject)
        message-body (get opts :body)]
    (println list-of-emails email-address account-password message-subject message-body)))

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