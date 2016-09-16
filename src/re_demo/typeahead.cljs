(ns re-demo.typeahead
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-com.core   :refer [h-box v-box box gap line typeahead label checkbox radio-button slider title p]]
            [re-com.typeahead   :refer [typeahead-args-desc]]
            [re-demo.utils :refer [panel-title title2 args-table github-hyperlink status-text]]
            [reagent.core  :as    reagent]
            [cljs.core.async  :refer    [timeout]]))

(declare md-icon-names)

(defn md-classes-for-icon [name] (str "zmdi zmdi-" name " zmdi-hc-2x"))

(defn typeahead-demo
  []
  (let [typeahead-on-change-value (reagent/atom nil)
        typeahead-model           (reagent/atom {})
        status                    (reagent/atom nil)
        status-icon?              (reagent/atom false)
        status-tooltip            (reagent/atom "")
        disabled?                 (reagent/atom false)
        change-on-blur?           (reagent/atom true)
        rigid?                    (reagent/atom false)
        data-source-choice        (reagent/atom :immediate)
        md-icon-result            #(-> {:name % :more :stuff})

        ;; simulate a search feature by scanning the baked-in constant collection md-icon-names
        suggestions-for-search
        (fn [s]
          (into []
                (take 16
                      (for [n md-icon-names
                            :when (re-find (re-pattern (str "(?i)" s)) n)]
                        (md-icon-result n)))))

        ;; if the suggestions are available immediately, just return them:
        data-source-immediate
        (fn [s]
          (suggestions-for-search s))

        ;; simulate an asynchronous source of suggestion objects:
        data-source-async
        (fn [s callback]
          (go
            (<! (timeout 500))
            (callback (suggestions-for-search s)))
          ;; important! return value must be falsey for an async :data-source
          nil)]
    (fn
      []
      [v-box
       :size     "auto"
       :gap      "10px"
       :children [[panel-title  "[typeahead ... ]"
                   "src/re_com/typeahead.cljs"
                   "src/re_demo/typeahead.cljs"]

                  [h-box
                   :gap      "100px"
                   :children [[v-box
                               :gap      "10px"
                               :width    "450px"
                               :children [[title2 "Notes"]
                                          [status-text "Stable"]
                                          [p "Text input that shows suggestions as the user types, and allows the user to choose one of the suggestions using the keyboard or mouse."]
                                          [p "The " [:code ":data-source"] " function provides suggestions (which can be arbitrary objects). This can be done synchronously or asynchronously."]
                                          [p "The " [:code ":on-change"] " function will be called when the user chooses a selection (the default), or as the user navigates through the available suggestions."]
                                          [p "The user can navigate through suggestions using the mouse, the UP & DOWN arrow keys, or the TAB key. The ENTER key chooses the active suggestion. The ESCAPE key resets."]
                                          [p "Setting "[:code ":rigid?" ]" to "[:code "false" ]" will allow the typeahead's model value to be arbitrary text input from the user. By default the model value can only be one of the suggestions."]
                                          [p "The " [:code ":render-suggestion"] " function can override the default rendering of suggestions."]
                                          [p "Input warnings and errors can be indicated visually by border colors and icons."]
                                          [args-table typeahead-args-desc]]]
                              [v-box
                               :gap      "10px"
                               :children [[title2 "Demo"]
                                          [h-box
                                           :gap "40px"
                                           :children [[v-box
                                                       :children [[label :label "[typeahead ... ]"]
                                                                  [gap :size "5px"]
                                                                  [typeahead
                                                                   :model typeahead-model
                                                                   :data-source      (case @data-source-choice :async data-source-async :immediate data-source-immediate)
                                                                   :suggestion-to-string #(:name %)
                                                                   :render-suggestion (fn [{:keys [name]}]
                                                                                        [:span
                                                                                         [:i {:style {:width "40px"} :class (md-classes-for-icon name)}]
                                                                                         name])
                                                                   :status           @status
                                                                   :status-icon?     @status-icon?
                                                                   :status-tooltip   @status-tooltip
                                                                   :width            "300px"
                                                                   :placeholder      "Material Design Icons"
                                                                   :on-change        #(reset! typeahead-on-change-value %)
                                                                   :change-on-blur?  change-on-blur?
                                                                   :rigid?           rigid?
                                                                   :disabled?        disabled?]]]
                                                      [v-box
                                                       :gap      "15px"
                                                       :children [[title :level :level3 :label "Callbacks"]
                                                                  [h-box
                                                                   :align    :center
                                                                   :gap      "5px"
                                                                   :children [[v-box
                                                                               :children [[:p [:code ":on-change"] " last called with this value: " ]
                                                                                          [:pre (str @typeahead-on-change-value)]]]]]
                                                                  [title :level :level3 :label "Parameters"]
                                                                  [v-box
                                                                   :children [[box :align :start :child [:code ":data-source"]]
                                                                              [radio-button
                                                                               :label     [:p "Synchronous " [:code "string -> results"]]
                                                                               :value     :immediate
                                                                               :model     @data-source-choice
                                                                               :on-change #(reset! data-source-choice :immediate)
                                                                               :style     {:margin-left "20px"}]
                                                                              [radio-button
                                                                               :label     [:p "Asynchronous " [:code "string, callback -> nil"]]
                                                                               :value     :async
                                                                               :model     @data-source-choice
                                                                               :on-change #(reset! data-source-choice :async)
                                                                               :style     {:margin-left "20px"}]]]
                                                                  [v-box
                                                                   :children [[box :align :start :child [:code ":change-on-blur?"]]
                                                                              [radio-button
                                                                               :label     "false - Call on-change on every keystroke"
                                                                               :value     false
                                                                               :model     @change-on-blur?
                                                                               :on-change #(reset! change-on-blur? false)
                                                                               :style     {:margin-left "20px"}]
                                                                              [radio-button
                                                                               :label     "true - Call on-change only on blur or Enter key (Esc key resets text)"
                                                                               :value     true
                                                                               :model     @change-on-blur?
                                                                               :on-change #(reset! change-on-blur? true)
                                                                               :style     {:margin-left "20px"}]]]
                                                                  [v-box
                                                                   :children [[box :align :start :child [:code ":rigid?"]]
                                                                              [radio-button
                                                                               :label     "false - Arbitrary text can be chosen as well as suggestion objects"
                                                                               :value     false
                                                                               :model     @rigid?
                                                                               :on-change #(reset! rigid? false)
                                                                               :style     {:margin-left "20px"}]
                                                                              [radio-button
                                                                               :label     "true - Only a suggestion object can be chosen"
                                                                               :value     true
                                                                               :model     @rigid?
                                                                               :on-change #(reset! rigid? true)
                                                                               :style     {:margin-left "20px"}]]]
                                                                  [v-box
                                                                   :children [[box :align :start :child [:code ":status"]]
                                                                              [radio-button
                                                                               :label     "nil/omitted - normal input state"
                                                                               :value     nil
                                                                               :model     @status
                                                                               :on-change #(do
                                                                                             (reset! status nil)
                                                                                             (reset! status-tooltip ""))
                                                                               :style {:margin-left "20px"}]
                                                                              [radio-button
                                                                               :label     ":warning - border color becomes orange"
                                                                               :value     :warning
                                                                               :model     @status
                                                                               :on-change #(do
                                                                                             (reset! status :warning)
                                                                                             (reset! status-tooltip "Warning tooltip - this (optionally) appears when there are warnings on typeahead components."))
                                                                               :style     {:margin-left "20px"}]
                                                                              [radio-button
                                                                               :label     ":error - border color becomes red"
                                                                               :value     :error
                                                                               :model     @status
                                                                               :on-change #(do
                                                                                             (reset! status :error)
                                                                                             (reset! status-tooltip "Error tooltip - this (optionally) appears when there are errors on typeahead components."))
                                                                               :style     {:margin-left "20px"}]]]
                                                                  [h-box
                                                                   :align :start
                                                                   :gap      "5px"
                                                                   :children [[checkbox
                                                                               :label     [:code ":status-icon?"]
                                                                               :model     status-icon?
                                                                               :on-change (fn [val]
                                                                                            (reset! status-icon? val))]
                                                                              [:span " (notice the tooltips on the icons)"]]]

                                                                  [checkbox
                                                                   :label     [box :align :start :child [:code ":disabled?"]]
                                                                   :model     disabled?
                                                                   :on-change (fn [val]
                                                                                (reset! disabled? val))]
                                                                  ]]]]]]]]]])))


;; core holds a reference to panel, so need one level of indirection to get figwheel updates
(defn panel
  []
  [typeahead-demo])

(def ^:private md-icon-names
  ["google"
   "google-plus-box"
   "google-plus"
   "paypal"
   "3d-rotation"
   "airplane-off"
   "airplane"
   "album"
   "archive"
   "assignment-account"
   "assignment-alert"
   "assignment-check"
   "assignment-o"
   "assignment-return"
   "assignment-returned"
   "assignment"
   "attachment-alt"
   "attachment"
   "audio"
   "badge-check"
   "balance-wallet"
   "balance"
   "battery-alert"
   "battery-flash"
   "battery-unknown"
   "battery"
   "bike"
   "block-alt"
   "block"
   "boat"
   "book-image"
   "book"
   "bookmark-outline"
   "bookmark"
   "brush"
   "bug"
   "bus"
   "cake"
   "car-taxi"
   "car-wash"
   "car"
   "card-giftcard"
   "card-membership"
   "card-travel"
   "card"
   "case-check"
   "case-download"
   "case-play"
   "case"
   "cast-connected"
   "cast"
   "chart-donut"
   "chart"
   "city-alt"
   "city"
   "close-circle-o"
   "close-circle"
   "close"
   "cocktail"
   "code-setting"
   "code-smartphone"
   "code"
   "coffee"
   "collection-bookmark"
   "collection-case-play"
   "collection-folder-image"
   "collection-image-o"
   "collection-image"
   "collection-item-1"
   "collection-item-2"
   "collection-item-3"
   "collection-item-4"
   "collection-item-5"
   "collection-item-6"
   "collection-item-7"
   "collection-item-8"
   "collection-item-9-plus"
   "collection-item-9"
   "collection-item"
   "collection-music"
   "collection-pdf"
   "collection-plus"
   "collection-speaker"
   "collection-text"
   "collection-video"
   "compass"
   "cutlery"
   "delete"
   "dialpad"
   "dns"
   "drink"
   "edit"
   "email-open"
   "email"
   "eye-off"
   "eye"
   "eyedropper"
   "favorite-outline"
   "favorite"
   "filter-list"
   "fire"
   "flag"
   "flare"
   "flash-auto"
   "flash-off"
   "flash"
   "flip"
   "flower-alt"
   "flower"
   "font"
   "fullscreen-alt"
   "fullscreen-exit"
   "fullscreen"
   "functions"
   "gas-station"
   "gesture"
   "globe-alt"
   "globe-lock"
   "globe"
   "graduation-cap"
   "home"
   "hospital-alt"
   "hospital"
   "hotel"
   "hourglass-alt"
   "hourglass-outline"
   "hourglass"
   "http"
   "image-alt"
   "image-o"
   "image"
   "inbox"
   "invert-colors-off"
   "invert-colors"
   "key"
   "label-alt-outline"
   "label-alt"
   "label-heart"
   "label"
   "labels"
   "lamp"
   "landscape"
   "layers-off"
   "layers"
   "library"
   "link"
   "lock-open"
   "lock-outline"
   "lock"
   "mail-reply-all"
   "mail-reply"
   "mail-send"
   "mall"
   "map"
   "menu"
   "money-box"
   "money-off"
   "money"
   "more-vert"
   "more"
   "movie-alt"
   "movie"
   "nature-people"
   "nature"
   "navigation"
   "open-in-browser"
   "open-in-new"
   "palette"
   "parking"
   "pin-account"
   "pin-assistant"
   "pin-drop"
   "pin-help"
   "pin-off"
   "pin"
   "pizza"
   "plaster"
   "power-setting"
   "power"
   "print"
   "puzzle-piece"
   "quote"
   "railway"
   "receipt"
   "refresh-alt"
   "refresh-sync-alert"
   "refresh-sync-off"
   "refresh-sync"
   "refresh"
   "roller"
   "ruler"
   "scissors"
   "screen-rotation-lock"
   "screen-rotation"
   "search-for"
   "search-in-file"
   "search-in-page"
   "search-replace"
   "search"
   "seat"
   "settings-square"
   "settings"
   "shape"
   "shield-check"
   "shield-security"
   "shopping-basket"
   "shopping-cart-plus"
   "shopping-cart"
   "sign-in"
   "sort-amount-asc"
   "sort-amount-desc"
   "sort-asc"
   "sort-desc"
   "spellcheck"
   "spinner"
   "storage"
   "store-24"
   "store"
   "subway"
   "sun"
   "tab-unselected"
   "tab"
   "tag-close"
   "tag-more"
   "tag"
   "thumb-down"
   "thumb-up-down"
   "thumb-up"
   "ticket-star"
   "toll"
   "toys"
   "traffic"
   "translate"
   "triangle-down"
   "triangle-up"
   "truck"
   "turning-sign"
   "wallpaper"
   "washing-machine"
   "window-maximize"
   "window-minimize"
   "window-restore"
   "wrench"
   "zoom-in"
   "zoom-out"
   "alert-circle-o"
   "alert-circle"
   "alert-octagon"
   "alert-polygon"
   "alert-triangle"
   "help-outline"
   "help"
   "info-outline"
   "info"
   "notifications-active"
   "notifications-add"
   "notifications-none"
   "notifications-off"
   "notifications-paused"
   "notifications"
   "account-add"
   "account-box-mail"
   "account-box-o"
   "account-box-phone"
   "account-box"
   "account-calendar"
   "account-circle"
   "account-o"
   "account"
   "accounts-add"
   "accounts-alt"
   "accounts-list-alt"
   "accounts-list"
   "accounts-outline"
   "accounts"
   "face"
   "female"
   "male-alt"
   "male-female"
   "male"
   "mood-bad"
   "mood"
   "run"
   "walk"
   "cloud-box"
   "cloud-circle"
   "cloud-done"
   "cloud-download"
   "cloud-off"
   "cloud-outline-alt"
   "cloud-outline"
   "cloud-upload"
   "cloud"
   "download"
   "file-plus"
   "file-text"
   "file"
   "folder-outline"
   "folder-person"
   "folder-star-alt"
   "folder-star"
   "folder"
   "gif"
   "upload"
   "border-all"
   "border-bottom"
   "border-clear"
   "border-color"
   "border-horizontal"
   "border-inner"
   "border-left"
   "border-outer"
   "border-right"
   "border-style"
   "border-top"
   "border-vertical"
   "copy"
   "crop"
   "format-align-center"
   "format-align-justify"
   "format-align-left"
   "format-align-right"
   "format-bold"
   "format-clear-all"
   "format-clear"
   "format-color-fill"
   "format-color-reset"
   "format-color-text"
   "format-indent-decrease"
   "format-indent-increase"
   "format-italic"
   "format-line-spacing"
   "format-list-bulleted"
   "format-list-numbered"
   "format-ltr"
   "format-rtl"
   "format-size"
   "format-strikethrough-s"
   "format-strikethrough"
   "format-subject"
   "format-underlined"
   "format-valign-bottom"
   "format-valign-center"
   "format-valign-top"
   "redo"
   "select-all"
   "space-bar"
   "text-format"
   "transform"
   "undo"
   "wrap-text"
   "comment-alert"
   "comment-alt-text"
   "comment-alt"
   "comment-edit"
   "comment-image"
   "comment-list"
   "comment-more"
   "comment-outline"
   "comment-text-alt"
   "comment-text"
   "comment-video"
   "comment"
   "comments"
   "check-all"
   "check-circle-u"
   "check-circle"
   "check-square"
   "check"
   "circle-o"
   "circle"
   "dot-circle-alt"
   "dot-circle"
   "minus-circle-outline"
   "minus-circle"
   "minus-square"
   "minus"
   "plus-circle-o-duplicate"
   "plus-circle-o"
   "plus-circle"
   "plus-square"
   "plus"
   "square-o"
   "star-circle"
   "star-half"
   "star-outline"
   "star"
   "bluetooth-connected"
   "bluetooth-off"
   "bluetooth-search"
   "bluetooth-setting"
   "bluetooth"
   "camera-add"
   "camera-alt"
   "camera-bw"
   "camera-front"
   "camera-mic"
   "camera-party-mode"
   "camera-rear"
   "camera-roll"
   "camera-switch"
   "camera"
   "card-alert"
   "card-off"
   "card-sd"
   "card-sim"
   "desktop-mac"
   "desktop-windows"
   "device-hub"
   "devices-off"
   "devices"
   "dock"
   "floppy"
   "gamepad"
   "gps-dot"
   "gps-off"
   "gps"
   "headset-mic"
   "headset"
   "input-antenna"
   "input-composite"
   "input-hdmi"
   "input-power"
   "input-svideo"
   "keyboard-hide"
   "keyboard"
   "laptop-chromebook"
   "laptop-mac"
   "laptop"
   "mic-off"
   "mic-outline"
   "mic-setting"
   "mic"
   "mouse"
   "network-alert"
   "network-locked"
   "network-off"
   "network-outline"
   "network-setting"
   "network"
   "phone-bluetooth"
   "phone-end"
   "phone-forwarded"
   "phone-in-talk"
   "phone-locked"
   "phone-missed"
   "phone-msg"
   "phone-paused"
   "phone-ring"
   "phone-setting"
   "phone-sip"
   "phone"
   "portable-wifi-changes"
   "portable-wifi-off"
   "portable-wifi"
   "radio"
   "reader"
   "remote-control-alt"
   "remote-control"
   "router"
   "scanner"
   "smartphone-android"
   "smartphone-download"
   "smartphone-erase"
   "smartphone-info"
   "smartphone-iphone"
   "smartphone-landscape-lock"
   "smartphone-landscape"
   "smartphone-lock"
   "smartphone-portrait-lock"
   "smartphone-ring"
   "smartphone-setting"
   "smartphone-setup"
   "smartphone"
   "speaker"
   "tablet-android"
   "tablet-mac"
   "tablet"
   "tv-alt-play"
   "tv-list"
   "tv-play"
   "tv"
   "usb"
   "videocam-off"
   "videocam-switch"
   "videocam"
   "watch"
   "wifi-alt-2"
   "wifi-alt"
   "wifi-info"
   "wifi-lock"
   "wifi-off"
   "wifi-outline"
   "wifi"
   "arrow-left-bottom"
   "arrow-left"
   "arrow-merge"
   "arrow-missed"
   "arrow-right-top"
   "arrow-right"
   "arrow-split"
   "arrows"
   "caret-down-circle"
   "caret-down"
   "caret-left-circle"
   "caret-left"
   "caret-right-circle"
   "caret-right"
   "caret-up-circle"
   "caret-up"
   "chevron-down"
   "chevron-left"
   "chevron-right"
   "chevron-up"
   "forward"
   "long-arrow-down"
   "long-arrow-left"
   "long-arrow-return"
   "long-arrow-right"
   "long-arrow-tab"
   "long-arrow-up"
   "rotate-ccw"
   "rotate-cw"
   "rotate-left"
   "rotate-right"
   "square-down"
   "square-right"
   "swap-alt"
   "swap-vertical-circle"
   "swap-vertical"
   "swap"
   "trending-down"
   "trending-flat"
   "trending-up"
   "unfold-less"
   "unfold-more"
   "directions-bike"
   "directions-boat"
   "directions-bus"
   "directions-car"
   "directions-railway"
   "directions-run"
   "directions-subway"
   "directions-walk"
   "directions"
   "layers-off"
   "layers"
   "local-activity"
   "local-airport"
   "local-atm"
   "local-bar"
   "local-cafe"
   "local-car-wash"
   "local-convenience-store"
   "local-dining"
   "local-drink"
   "local-florist"
   "local-gas-station"
   "local-grocery-store"
   "local-hospital"
   "local-hotel"
   "local-laundry-service"
   "local-library"
   "local-mall"
   "local-movies"
   "local-offer"
   "local-parking"
   "local-pharmacy"
   "local-phone"
   "local-pizza"
   "local-activity"
   "local-post-office"
   "local-printshop"
   "local-see"
   "local-shipping"
   "local-store"
   "local-taxi"
   "local-wc"
   "map"
   "my-location"
   "nature-people"
   "nature"
   "navigation"
   "pin-account"
   "pin-assistant"
   "pin-drop"
   "pin-help"
   "pin-off"
   "pin"
   "traffic"
   "apps"
   "grid-off"
   "grid"
   "view-agenda"
   "view-array"
   "view-carousel"
   "view-column"
   "view-comfy"
   "view-compact"
   "view-dashboard"
   "view-day"
   "view-headline"
   "view-list-alt"
   "view-list"
   "view-module"
   "view-quilt"
   "view-stream"
   "view-subtitles"
   "view-toc"
   "view-web"
   "view-week"
   "widgets"
   "alarm-check"
   "alarm-off"
   "alarm-plus"
   "alarm-snooze"
   "alarm"
   "calendar-alt"
   "calendar-check"
   "calendar-close"
   "calendar-note"
   "calendar"
   "time-countdown"
   "time-interval"
   "time-restore-setting"
   "time-restore"
   "time"
   "timer-off"
   "timer"
   "android-alt"
   "android"
   "apple"
   "behance"
   "codepen"
   "dribbble"
   "dropbox"
   "evernote"
   "facebook-box"
   "facebook"
   "github-box"
   "github"
   "google-drive"
   "google-earth"
   "google-glass"
   "google-maps"
   "google-pages"
   "google-play"
   "google-plus-box"
   "google-plus"
   "google"
   "instagram"
   "language-css3"
   "language-html5"
   "language-javascript"
   "language-python-alt"
   "language-python"
   "lastfm"
   "pocket"
   "polymer"
   "rss"
   "share"
   "steam-square"
   "steam"
   "twitter-box"
   "twitter"
   "vk"
   "wikipedia"
   "windows"
   "500px"
   "8tracks"
   "aspect-ratio-alt"
   "aspect-ratio"
   "blur-circular"
   "blur-linear"
   "blur-off"
   "blur"
   "brightness-2"
   "brightness-3"
   "brightness-4"
   "brightness-5"
   "brightness-6"
   "brightness-7"
   "brightness-auto"
   "brightness-setting"
   "broken-image"
   "center-focus-strong"
   "center-focus-weak"
   "compare"
   "crop-16-9"
   "crop-3-2"
   "crop-5-4"
   "crop-7-5"
   "crop-din"
   "crop-free"
   "crop-landscape"
   "crop-portrait"
   "crop-square"
   "exposure-alt"
   "exposure"
   "filter-b-and-w"
   "filter-center-focus"
   "filter-frames"
   "filter-tilt-shift"
   "gradient"
   "grain"
   "graphic-eq"
   "hdr-off"
   "hdr-strong"
   "hdr-weak"
   "hdr"
   "iridescent"
   "leak-off"
   "leak"
   "looks"
   "loupe"
   "panorama-horizontal"
   "panorama-vertical"
   "panorama-wide-angle"
   "photo-size-select-large"
   "photo-size-select-small"
   "picture-in-picture"
   "slideshow"
   "texture"
   "tonality"
   "vignette"
   "wb-auto"
   "eject-alt"
   "eject"
   "equalizer"
   "fast-forward"
   "fast-rewind"
   "forward-10"
   "forward-30"
   "forward-5"
   "hearing"
   "pause-circle-outline"
   "pause-circle"
   "pause"
   "play-circle-outline"
   "play-circle"
   "play"
   "playlist-audio"
   "playlist-plus"
   "repeat-one"
   "repeat"
   "replay-10"
   "replay-30"
   "replay-5"
   "replay"
   "shuffle"
   "skip-next"
   "skip-previous"
   "stop"
   "surround-sound"
   "tune"
   "volume-down"
   "volume-mute"
   "volume-off"
   "volume-up"
   "n-1-square"
   "n-2-square"
   "n-3-square"
   "n-4-square"
   "n-5-square"
   "n-6-square"
   "neg-1"
   "neg-2"
   "plus-1"
   "plus-2"
   "sec-10"
   "sec-3"
   "zero"])
