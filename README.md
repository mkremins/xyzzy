# xyzzy
Zipper-like structures that:
* Store the path from the root to the selected node in easily retrievable form
* Have a uniform internal tree structure (a `:children` vector in each branch node)

## Rationale
I use this in [Flense](https://github.com/mkremins/flense) to support paredit-like transformations on arbitrary Clojure source code. Since the zipper structure is "just a map", I don't have to do any extra work to get it to play well with [Om](https://github.com/swannodette/om).

## Usage
Add to your `project.clj`:

```clojure
[mkremins/xyzzy "0.3.3"]
```

## License
[MIT License](http://opensource.org/licenses/MIT). Hack away.
