(ns bluegenes.pages.developer.devhome
  (:require [re-frame.core :as re-frame :refer [subscribe dispatch]]
            [bluegenes.pages.developer.events :as events]
            [bluegenes.pages.developer.subs :as subs]
            [bluegenes.pages.developer.icons :as icons]
            [bluegenes.pages.developer.tools :as tools]
            [bluegenes.persistence :as persistence]
            [clojure.string :refer [blank?]]
            [accountant.core :refer [navigate!]]))

(defn nav []
  "Buttons to choose which mine you're using."
  [:ul.dev-navigation
   [:li [:a {:on-click #(navigate! "/debug/main")}
         [:svg.icon.icon-cog
          [:use {:xlinkHref "#icon-cog"}]] "Debug Console"]]
   [:li
    [:a {:on-click #(navigate! "/debug/tool-store")}
     [:svg.icon.icon-star-full
      [:use {:xlinkHref "#icon-star-full"}]] "Tool 'App Store'"]]
   [:li [:a {:on-click #(navigate! "/debug/icons")}
         [:svg.icon.icon-intermine
          [:use {:xlinkHref "#icon-intermine"}]] "Icons"]]])

(defn mine-config []
  "Outputs current intermine and list of mines from registry
   To allow users to choose their preferred InterMine."
  (let [current-mine (subscribe [:current-mine])]
    (fn []
      [:div.panel.container [:h3 "Current mine: "]
       [:p (:name @current-mine) " at "
        [:span (:root (:service @current-mine))]]
       [:form
        [:legend "Select a new mine to draw data from:"]
        (into
         [:div.form-group.mine-choice
          [:label
           {:class "checked"}
           [:input
            {:type           "radio"
             :name           "urlradios"
             :id             (:id @current-mine)
             :defaultChecked true
             :value          (:id @current-mine)}]
           (:name @current-mine) " (current)"]]
         (map
          (fn [[id details]]
            (cond
              (not= id (:id @current-mine))
              (let [mine-name
                    (if (blank? (:name details))
                      id (:name details))]
                [:label {:title (:description details)}
                 [:input
                  {:on-change
                   (fn [e]
                     (dispatch
                      [:set-active-mine details]))
                   :type           "radio"
                   :name           "urlradios"
                   :id             id
                   :value          id}] mine-name])))
          @(subscribe [:registry])))
        [:button.btn.btn-primary.btn-raised
         {:on-click (fn [e] (.preventDefault e))} "Save"]]])))

(defn version-number []
  [:div.panel.container
   [:h3 "Client Version: "]
   [:code (str bluegenes.core/version)]])

(defn localstorage-destroyer []
  (fn []
    [:div.panel.container [:h3 "Delete local storage: "]
     [:form
      [:p "This will delete the local storage settings included preferred intermine instance, model, lists, and summaryfields. Model, lists, summaryfields should be loaded afresh every time anyway, but here's the easy pressable button to be REALLY SURE: "]
      [:button.btn.btn-primary.btn-raised
       {:on-click
        (fn [e]
          (.preventDefault e)
          (persistence/destroy!)
          (.reload js/document.location true))}
       "Delete bluegenes localstorage... for now."]]]))

(defn debug-panel []
  (fn []
    (let [panel (subscribe [::subs/panel])]
      [:div.developer
       [nav]
       (cond
         (= @panel "main")
         [:div
          [:h1 "Debug console"]
          [mine-config]
          [localstorage-destroyer]
          [version-number]]
         (= @panel "tool-store")
         [tools/tool-store]
         (= @panel "icons")
         [icons/iconview])])))
