(ns guardian-cicero.views.welcome
  (:require [guardian-cicero.views.common :as common]
            [noir.content.getting-started])
  (:use [noir.core :only [defpage]]
        [net.cgrand.enlive-html]
        [cheshire.core]
        [clojure.string :only [trim]])

  (:import java.net.URL)) 

(def *base-url* "http://www.guardian.co.uk")

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to guardian-cicero"]))


(defpage "/api" {:keys [page]}
         (let [page-url 
               (str *base-url* (or page  "/world/2012/sep/06/honduras-new-city-laws-investors"))
               res (-> page-url URL. html-resource)
               comments (select res [:.comment])
               ]
           {:headers {"Content-Type" "application/json; charset=utf-8"} 
            :body (generate-string  
             (map #(hash-map :name (first  %) :comment (->  (second  % ) trim)) 
                  (map vector 
                    (map text (select comments [:div.profile :a]))
                    (map text (select comments [:div.comment-body]))     
                   )))}))
