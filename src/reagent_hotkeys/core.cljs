(ns reagent-hotkeys.core
  (:require [reagent.core :as r]
            [goog.events :as events]
            [clojure.string :as string])
  (:import [goog.events EventType]))

;; ## Register/Unregister

(def ^:private event-types
  {:key-up   EventType/KEYUP
   :key-down EventType/KEYDOWN})

(defn- handler-from-atom
  [handler-atom k]
  (fn [evt]
    (when-let [h (get @handler-atom k)]
      (h evt))))

(defn- generate-window-handlers
  [hotkey-handlers]
  (->> (for [[k _] event-types
             :let [h (handler-from-atom hotkey-handlers k)]
             :when h]
         [k h])
       (into {})))

(defn- register-window-handlers!
  [window-handlers]
  (doseq [[k t] event-types
          :let [h (get window-handlers k)]
          :when h]
    (.addEventListener js/window t h)))

(defn- unregister-window-handlers!
  [window-handlers]
  (doseq [[k t] event-types
          :let [h (get window-handlers k)]
          :when h]
    (.removeEventListener js/window t h)))

;; ## Keys

(def ^:private keycodes
  {;; special keys
   "BACKSPACE" 8
   "TAB"       9
   "ENTER"     13
   "ESC"       27
   "SPACE"     32
   ;; arrow keys
   "LEFT"      37
   "UP"        38
   "RIGHT"     39
   "DOWN"      40
   ;; modifiers (special handling)
   "SHIFT"     ::shift
   "ALT"       ::alt
   "CTRL"      ::ctrl})

(defn- as-keycode
  [s]
  (let [s (.toUpperCase s)]
    (or (get keycodes s)
        (when-not (next s)
          (.charCodeAt s 0))
        s)))

(defn- as-keycodes
  [s]
  (->> (string/split s "-")
       (keep as-keycode)
       (into #{})))

(defn- as-hotkey-specs
  [{:keys [keys]}]
  (->> (for [[s spec] keys]
         [(as-keycodes s) spec])
       (into {})))

;; ## Hotkey Handlers

(defn- event->keycodes
  [event]
  (cond-> #{(.-keyCode event)}
    (.-shiftKey event) (conj ::shift)
    (.-altKey event) (conj ::alt)
    (.-ctrlKey event) (conj ::ctrl)))

(defn- handle-key-down!
  [specs]
  (fn [event]
    (let [keycodes (event->keycodes event)]
      (when-let [{:keys [handler prevent-default?]
                  :or {prevent-default? true}}
                 (get specs keycodes)]
        (handler)
        (when prevent-default?
          (.preventDefault event))))))

(defn- wrap-debug
  [handlers]
  (->> (for [[k f] handlers]
         [k (fn [event]
              (println "[hotkeys]"
                       (.-code event) "->" (event->keycodes event))
              (f event))])
       (into {})))

(defn- generate-hotkey-handlers
  [props]
  (let [specs    (as-hotkey-specs props)
        handlers {:key-down (handle-key-down! specs)}]
    (if (:debug? props)
      (wrap-debug handlers)
      handlers)))

;; ## Component

(defn- generate-state
  [props]
  ;; We're using an atom to store the hotkey handlers. This allows us to replace
  ;; them dynamically when the props are updated, without having to unregister
  ;; and re-register the window handlers.
  (let [handlers        (atom (generate-hotkey-handlers props))
        window-handlers (generate-window-handlers handlers)]
    {:on-mount        #(register-window-handlers! window-handlers)
     :on-unmount      #(unregister-window-handlers! window-handlers)
     :on-update       #(reset! handlers (generate-hotkey-handlers %))}))

(defn hotkeys
  [props]
  (let [{:keys [on-mount on-unmount on-update]} (generate-state props)]
    (r/create-class
      {:component-did-mount    on-mount
       :component-will-unmount on-unmount
       :component-did-update   (comp on-update second r/argv)
       :reagent-render         (constantly [:span])})))
