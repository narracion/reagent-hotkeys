# reagent-hotkeys

This library allows you to register hotkeys/keybindings in the browser using a [reagent][] component.

[reagent]: https://github.com/reagent-project/reagent

## Usage

```clj
(require '[reagent-hotkeys.core :refer [hotkeys]])
```

**Minimal example**

```clj
(defn- undo-last-action!
  []
  (swap! app-state ...))

(defn my-hotkeys
  []
  [hotkeys
    {:keys {"CTRL-Z" {:handler undo-last-action!}
           ...}}])
```

**Allow propagation**

```clj
(defn my-hotkeys
  []
  [hotkeys
   {:keys {"CTRL-Z" {:handler undo-last-action!
                     :prevent-default? false}}}])
```

**Enable debug output**

```clj
(defn my-hotkeys
  []
  [hotkeys
   {:keys   {...}
    :debug? true}])
```

## License

```
MIT License

Copyright (c) 2021 Yannick Scherer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
