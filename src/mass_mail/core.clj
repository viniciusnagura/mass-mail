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
           [selmer.parser :as selmer]
           )
  )

(def progress (atom 0))
(def subject (atom "Subject"))
(def body (atom ""))
(def email (atom ""))
(def pass (atom ""))
(def sender-name (atom ""))

(defn is-email?
  [email]
  (let [regex (re-matches #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?" email)]
    (if (= regex email)
      true
      false)
    )
  )

(defn is-csv?
  [file]
  (let [ext "csv"]
    (if (.endsWith file ext)
      true
      false
      )
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
  (println "read-file")
  (let [output (with-open [in-file (io/reader file)]
               (->>
                 (csv/parse-csv in-file)
                 remove-comments
                 mappify
                 doall))]
    output
    )
)

(defn create-body
  [dest]
  (selmer/render "Hello {{name}}, \n\n {{body}} \n\n Bests, \n {{sender-name}}"
                 {:name (:name dest) :body @body :sender-name @sender-name})
)

(defn create-message
  [dest]
  {:from @email
   :to (:email dest)
   :subject @subject
   :body (create-body dest)}
)

(defn connection
  []
  {:host "smtp.gmail.com"
   :ssl true
   :user @email
   :pass @pass
   }
)

(defn- send-email-private
  [dest]
  (send-message (connection) (create-message dest))
)

(defn send-email-2
  [dest]
  (info "Sending to:" (get dest :email))
  (let [result (send-email-private dest)]
    (info "Sent: " result)
    (reset! progress (inc @progress))
    result)
)

(defn log-results
  [file]
  (let [dest (read-file file)
        results (mapv send-email-2 dest)
        successes (filter #(= :SUCCESS (:error %)) results)]
    {:attempted (count results)
     :sent (count successes)})
  )

(defn send-email
  "Set all the informations that were given through command line or gui"
  ([file email password subject body sender-name]
   (let [dest (read-file file)
         conn {:host "smtp.gmail.com"
               :ssl true
               :user email
               :pass password}
         body (create-body (get dest :name))]

     (mapv (fn[x]
             (do
               (try
                 (info "Sending email to:" (str (val (first x))))
                 (if (nil? (send-message conn {:from email
                                               :to (get x :email)
                                               :subject subject
                                               :body body} ))
                   (info "Failed to send email to:" (str (val (first x))))
                   (info "Email sent to:" (str (val (first x)))))
                 (catch Exception e
                   (error "Failed to send email to:" (str (val (first x))) "Error" (str e)))
                 )
               (reset! progress (inc @progress))
               )) dest)

     ))

  )



;------------ C O M M A N D    L I N E -----------
(comment ([opts]
           (let [{file :file email :email password :password subject :subject body :body}
                 opts]
             (send-email file email password subject body "name"))))
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