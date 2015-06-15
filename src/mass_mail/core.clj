(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]
           [postal.core :refer [send-message]]))

(defn send-email
  "Set all the informations that were given through command line"
  ([opts]
    (let [{file :file name :name email :email password :password subject :subject body :body}
          opts]
      (send-email file name email password subject body)))

  ([file name email password subject body]
  (let [list-of-emails (->> (clojure.string/split (slurp file) #"\n")
                            (map #(clojure.string/split % #":")))

        conn {:host "smtp.gmail.com"
              :ssl true
              :user email
              :pass password}]

    (mapv #(send-message conn {:from email
                               :to (second %)
                               :subject subject
                               :body body}) list-of-emails)
    ))
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