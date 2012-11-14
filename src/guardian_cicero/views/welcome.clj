(ns guardian-cicero.views.welcome
  (:require [guardian-cicero.views.common :as common])
  (:use [noir.core :only [defpage]]
        [net.cgrand.enlive-html]
        [cheshire.core]
        [clojure.string :only [trim]] 
        [ring.util.codec :only [url-decode url-encode]] 
        [noir.request :only [ring-request]] )

  (:import java.net.URL)) 

(def base-url "http://www.guardian.co.uk")

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to guardian-cicero"]))


;; you can also try
;; localhost:8080/api?page=/world/2012/nov/13/us-general-john-allen-petraeus-affair

(defpage "/api" {:keys [page]}
         (let [page-url 
               (str base-url (or page  "/world/2012/sep/06/honduras-new-city-laws-investors"))
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

(def comment-form 
  "<form method='GET' action'.'>
    <label>Permalink?</label>
    <input type='text' name='permalink'></input>
    <input type='submit' name='submit'>Submit</input>
  </form>
  "
  )

(defn embed-code [comment-body embed-url]
  (str 
    "<div id='guardian-comment'>" comment-body "</div>"
    "<script type='text/javascript'>
    var __gcxhr = new XMLHttpRequest();
    __gcxhr.open('GET', window.location.href.replace('submit','notsubmit'), false);
    __gcxhr.send ();
    document.querySelector('div#guardian-comment').innerHTML = __gcxhr.responseText;
     
    </script>"
    )
  )

(defpage "/comment" {:keys [permalink submit]}
         (if permalink 
         (let 
           [uri (.getPath (java.net.URI. (url-decode permalink)))
            path (str base-url (.getPath (java.net.URI. permalink))) ;; make sure we're on guardian
            url (URL. path)
            redirect-node (html-resource url)
            redirect-url (:href (:attrs (first (select redirect-node [:a]))))
            comment-article (URL. redirect-url)
            comment-node (html-resource comment-article)
            anchor (.getRef comment-article)
            permalinked-comment (first (select comment-node [(keyword  (str "#" anchor)) (but [:.comment-tools])]))]
           (println permalinked-comment)
           {:headers {"Content-Type" "text/html; charset=utf-8"
                      "Access-Control-Allow-Origin" "*" }
            :body (common/layout (if submit 
                                   (embed-code "hi" "lol.js") 
                                   (emit* [permalinked-comment])
                                   )) 
            })
           (common/layout comment-form)
           ))
