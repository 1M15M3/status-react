(ns status-im.models.bootnode
  (:require
   [clojure.string :as string]
   [status-im.ui.screens.accounts.utils :as accounts.utils]
   [status-im.utils.handlers-macro :as handlers-macro]
   [status-im.utils.ethereum.core :as ethereum]))

(def address-regex #"enode://[a-zA-Z0-9]+:?(.*)\@\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b:(\d{1,5})")

(defn valid-address? [address]
  (re-matches address-regex address))

(defn- build [id bootnode-name address chain]
  {:address address
   :chain   chain
   :id      (string/replace id "-" "")
   :name    bootnode-name})

(defn fetch [id cofx]
  (let [network (get-in cofx [:db :network])]
    (get-in cofx [:db :account/account :bootnodes network id])))

(defn set-input [input-key value {:keys [db]}]
  {:db (update
        db
        :bootnodes/manage
        assoc
        input-key
        {:value value
         :error (case input-key
                  :id   false
                  :name (string/blank? value)
                  :url  (not (valid-address? value)))})})

(defn edit [id {:keys [db] :as cofx}]
  (let [{:keys [id
                address
                name]}   (fetch id cofx)
        fxs              (handlers-macro/merge-fx
                          cofx
                          (set-input :id id)
                          (set-input :url (str address))
                          (set-input :name (str name)))]
    (assoc fxs :dispatch [:navigate-to :edit-bootnode])))

(defn save [{{:bootnodes/keys [manage] :account/keys [account] :as db} :db :as cofx}]
  (let [{:keys [name url]} manage
        network            (:network db)
        bootnode           (build
                            (:random-id cofx)
                            (:value name)
                            (:value url)
                            network)
        new-bootnodes      (assoc-in
                            (:bootnodes account)
                            [network (:id bootnode)]
                            bootnode)]

    (handlers-macro/merge-fx
     cofx
     {:db       (dissoc db :bootnodes/manage)
      :dispatch [:navigate-back]}
     (accounts.utils/account-update {:bootnodes new-bootnodes}))))
