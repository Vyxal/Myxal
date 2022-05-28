# Myxal

Terse. Elegant. Readable. Even faster. A <s>top secret</s> experiment to make [Jyxal](https://github.com/Vyxal/Jyxal)
even faster and multitarget compiled.

## How to run

1. Download the latest release from the [releases page](https://github.com/Vyxal/Myxal/releases).
2. Run the file with `java -jar Myxal-<version>.jar -f <input file> [other args]`
3. Enjoy the compiled goodness!

## Arguments

| Argument | Description                                                                                       |
|----------|---------------------------------------------------------------------------------------------------|
| d        | Debug mode. Prints stuff like parse trees, adds `-g` to g++, and the like                         |
| c        | Makes the compiler use the Myxal codepage instead of UTF-8 for an input encoding                  |
| h        | Prints the help page                                                                              |
| p        | Specifies which platform to compile for. Options are either `jvm` or `native`. Defaults to `jvm`. |
| O        | Disables optimization                                                                             |
| f        | Specifies input file                                                                              |


