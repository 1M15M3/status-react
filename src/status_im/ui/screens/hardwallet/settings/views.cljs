(ns status-im.ui.screens.hardwallet.settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.hardwallet.components :as components]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.ui.components.common.common :as components.common]))

(defonce pin-retries 3)
(defonce puk-retries 5)

(defview enter-pin []
  (letsubs [pin [:hardwallet/pin]
            step [:hardwallet/pin-enter-step]
            status [:hardwallet/pin-status]
            pin-retry-counter [:hardwallet/pin-retry-counter]
            puk-retry-counter [:hardwallet/puk-retry-counter]
            error-label [:hardwallet/pin-error-label]]
    [react/keyboard-avoiding-view {:flex 1}
     [react/view {:flex             1
                  :background-color colors/white}
      [react/view {:flex-direction  :column
                   :flex            1
                   :align-items     :center
                   :justify-content :space-between}
       [components/maintain-card nil]
       (if (zero? pin-retry-counter)
         [pin.views/pin-view {:pin               pin
                              :retry-counter     (when (< puk-retry-counter puk-retries) puk-retry-counter)
                              :title-label       :t/enter-puk-code
                              :description-label :t/enter-puk-code-description
                              :step              step
                              :status            status
                              :error-label       error-label}]
         [pin.views/pin-view {:pin               pin
                              :retry-counter     (when (< pin-retry-counter pin-retries) pin-retry-counter)
                              :title-label       (case step
                                                   :current :t/current-pin
                                                   :original :t/create-pin
                                                   :confirmation :t/repeat-pin)
                              :description-label (case step
                                                   :current :t/current-pin-description
                                                   :t/new-pin-description)
                              :step              step
                              :status            status
                              :error-label       error-label}])]]]))

(defn- action-row [{:keys [icon label on-press color-theme]}]
  [react/touchable-highlight
   {:on-press on-press}
   [react/view {:flex-direction :row
                :margin-top     15}
    [react/view {:background-color (case color-theme
                                     :red colors/red-transparent-10
                                     colors/blue-light)
                 :width            40
                 :height           40
                 :border-radius    50
                 :align-items      :center
                 :justify-content  :center}
     [vector-icons/icon icon {:color (case color-theme
                                       :red colors/red
                                       colors/blue)}]]
    [react/view {:align-items     :center
                 :justify-content :center
                 :margin-left     16}
     [react/text {:style {:font-size 17
                          :color     (case color-theme
                                       :red colors/red
                                       colors/blue)}}
      (i18n/label label)]]]])

(defn reset-card []
  [react/view {:flex 1}
   [status-bar/status-bar]
   [toolbar/simple-toolbar
    (i18n/label :t/reset-card)]
   [react/view {:flex             1
                :background-color :white}
    [react/view {:margin-top  71
                 :flex        1
                 :align-items :center}
     [react/image {:source (:warning-sign resources/ui)
                   :style  {:width  160
                            :height 160}}]]
    [react/view {:flex               1
                 :padding-horizontal 30}
     [react/text {:style {:font-weight :bold
                          :color       colors/black
                          :font-size   22
                          :text-align  :center}}
      (i18n/label :t/reset-card-description)]]
    [react/view {:flex-direction   :row
                 :justify-content  :space-between
                 :align-items      :center
                 :width            "100%"
                 :height           52
                 :border-top-width 1
                 :border-color     colors/gray-light}
     [react/view {:flex 1}]
     [react/view {:margin-right 18}
      [components.common/bottom-button
       {:on-press   #(re-frame/dispatch [:keycard-settings.ui/reset-card-next-button-pressed])
        :uppercase? false
        :forward?   true}]]]]])

(defn- card-blocked []
  [react/view
   [react/text {:style {:font-size          20
                        :text-align         :center
                        :padding-horizontal 40
                        :color              colors/black}}
    (i18n/label :t/keycard-blocked)]])

(defview keycard-settings []
  (letsubs [paired-on [:keycard-paired-on]
            puk-retry-counter [:hardwallet/puk-retry-counter]
            pairing [:hardwallet/pairing]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/status-keycard)]
     [react/view {:flex             1
                  :background-color :white}
      [react/view {:margin-top  47
                   :flex        1
                   :align-items :center}
       [react/image {:source (:hardwallet-card resources/ui)
                     :style  {:width  255
                              :height 160}}]
       (when paired-on
         [react/view {:margin-top 27}
          [react/text
           (i18n/label :t/linked-on {:date paired-on})]])]
      [react/view {:margin-left    16
                   :flex           1
                   :width          "90%"
                   :flex-direction :column}
       (if (zero? puk-retry-counter)
         [card-blocked]
         [react/view
          [action-row {:icon     :icons/info
                       :label    :t/help-capitalized
                       :on-press #(.openURL react/linking "https://hardwallet.status.im")}]
          [action-row {:icon     :icons/add
                       :label    :t/change-pin
                       :on-press #(re-frame/dispatch [:keycard-settings.ui/change-pin-pressed])}]
          (when pairing
            [action-row {:icon     :icons/close
                         :label    :t/unpair-card
                         :on-press #(re-frame/dispatch [:keycard-settings.ui/unpair-card-pressed])}])])]
      [react/view {:margin-bottom 20
                   :margin-left   16}
       [action-row {:icon        :icons/logout
                    :color-theme :red
                    :label       :t/reset-card
                    :on-press    #(re-frame/dispatch [:keycard-settings.ui/reset-card-pressed])}]]]]))