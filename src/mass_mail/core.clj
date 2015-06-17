(ns mass-mail.core
  (:gen-class)
  (require [clojure.tools.cli :refer [cli]]
           [postal.core :refer [send-message]]
           [semantic-csv.core :refer :all]
           [clojure-csv.core :as csv]
           [clojure.java.io :as io]
           [taoensso.timbre :as timbre
            :refer (log trace debug info warn error fatal report
                        logf tracef debugf infof warnf errorf fatalf reportf
                        spy)]
           [seesaw.core :as seesaw])
  )

(def progress (atom 0))

(defn is-email?
  [email]
  (let [regex (re-matches #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" email)]
    (if (= regex email)
      true
      false)
    )
  )

(defn warnings
  [list]
  (keep-indexed #(if (or (= (get %2 :name) "") (= (get %2 :city) ""))
            (+ 2 %1)) list))

(defn errors
  [list]
  (keep-indexed #(if (or (not (is-email? (get %2 :email))) (= (get %2 :email) ""))
            (+ 2 %1)) list))

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
               :pass password}
         ]

     (mapv (fn[x]
             (do
               (reset! progress (inc @progress))
               (try
                 (info "Sending email to:" (str (val (first x))))
                 (if (nil? (send-message conn {:from email
                                               :to (get x :email)
                                               :subject subject
                                               :body body}))
                   (info "Failed to send email to:" (str (val (first x))))
                   (info "Email sent to:" (str (val (first x)))))
                 (catch Exception e
                   (error "Failed to send email to:" (str (val (first x))) "Error" (str e)))
                 )
               )) dest)
     ))

  )


(comment (defn -main
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
      (send-email opts)))))