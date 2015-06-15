(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]
           [postal.core :refer [send-message]]
           [semantic-csv.core :refer :all]
           [clojure-csv.core :as csv]
           [clojure.java.io :as io]))

(defn read-file
  [file]
  (let [output (with-open [in-file (io/reader file)]
               (->>
                 (csv/parse-csv in-file)
                 remove-comments
                 mappify
                 doall))]
    output
    )
)

(defn send-email
  "Set all the informations that were given through command line or gui"
  ([opts]
   (let [{file :file email :email password :password subject :subject body :body}
         opts]
     (send-email file email password subject body)))

  ([file email password subject body]
   (let [dest (read-file file)
         conn {:host "smtp.gmail.com"
               :ssl true
               :user email
               :pass password}]

     (mapv #(send-message conn {:from email
                                :to (get % :email)
                                :subject subject
                                :body body}) dest)
     ))
  )

(defn send-email-original
  "Set all the informations that were given through command line"
  ([opts]
    (let [{file :file name :name email :email password :password subject :subject body :body}
          opts]
      (send-email-original file name email password subject body)))

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
      (send-email opts))))