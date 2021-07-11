(ns reagent-hotkeys.core
  (:require [reagent.core :as r]
            [goog.events :as events]
            [clojure.string :as string])
  (:import [goog.events EventType]))

;; ## Register/Unregister

(def ^:private as-event-type
  {:key-up   EventType/KEYUP
   :key-down EventType/KEYDOWN})

(defn- register-hotkey-handlers!
  [handlers]
  (doseq [[k handler] handlers
          :let [t (as-event-type k)]
          :when t]
    (.addEventListener js/window t handler)))

(defn- unregister-hotkey-handlers!
  [handlers]
  (doseq [[k handler] handlers
          :let [t (as-event-type k)]
          :when t]
    (.removeEventListener js/window t handler)))

;; ## Keys

(def ^:private keycodes
  {;; special keys
   "BACKSPACE" 8
   "TAB"       9
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
              (.log js/console event)
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

(defn hotkeys
  [props]
  (let [handlers (generate-hotkey-handlers props)]
    (r/create-class
      {:component-did-mount
       (fn [_]
         (register-hotkey-handlers! handlers))
       :component-will-unmount
       (fn [_]
         (unregister-hotkey-handlers! handlers))
       :reagent-render
       (fn [_]
         [:span])})))
