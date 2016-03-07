(ns om-blog.core
  (:require [om.core :as om :include-macros true]
            [goog.events :as events]
            [om.dom :as dom :include-macros true])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))

(enable-console-print!)

(defn json-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
      (fn [e]
        (on-complete (js->clj (.getResponseJson xhr)))))
    (. xhr
      (send url method (when data (clj->js data))
        #js {"Content-Type" "application/json"}))))

(defn load-articles [url on-complete] 
  (json-xhr
    {:method "GET"
     :url url
     :data nil
     :on-complete on-complete}))

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:articles []}))

(defn article [article owner]
  (reify om/IRender
    (render [_]
      (dom/li nil 
        (apply dom/div nil 
          [(dom/h2 nil (get article "title")) 
           (dom/div nil (get article "body"))])))))

(defn articles [state owner] 
  (reify 
    om/IRender
    (render [_]
      (apply dom/ul nil (om/build-all article (:articles state))))
    om/IWillMount
    (will-mount [_]
      (load-articles 
        "http://jsonplaceholder.typicode.com/posts"
        (fn [res]
          (om/transact! state :articles (fn [] res)))))))

(om/root articles app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
