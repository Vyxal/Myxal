# How Myxal Works

Here I'll get the reveal all the intricacies of the Myxal compiler and what I learned from this project.

## Parsing

Obviously you cannot get very far in creating a programming language without parsing it. Unlike its parent
language [Vyxal](https://github.com/Vyxal/Vyxal), Myxal does not use a custom lexer/parser. Using a custom parsing
system allows for a lot of random parsing quirks, like the inability of Vyxal to parse the single byte backslash
character. Instead, the grammar is written out in [ANTLR](https://www.antlr.org) form.

The lexer may appear to be slightly more complicated than it should be (and it probably is, even with two rewrites), but
it is relatively simple once you read it. It starts out by defining basic element features: prefixes, alias separators,
special elements, modifiers, digits. It is here in the lexer that comments are captured and immediately thrown away,
like they were never there. Then alphabetic characters are captured, and then whitespace. After swallowing lambdas and
variables, the lexer gets to strings. Initially, strings were defined in the parser, until that wreaked havoc with
precedence of tokens. After that, the random syntax elements that deal with various structures are lexed. Finally, the
very last token, `LITERALLY_ANY_TEXT`, captures anything and everything else. This is the token used for elements.

The parser is relatively simple: first come alias definitions, then the program. Each structure gets its own dedicated
node. Almost. There is a `fori_loop` node that is simply a normal `for_loop`, but with nine digits prepended to it. The
nine digits are the highest power of ten that a 32-bit number can store. This pattern, when verified through the AST
transformer, can be converted into the target implementation as a simple counting `for` loop, instead of a complex big
number object and the iterator to it.

## Transformation

Jyxal, the precursor of Myxal, used to compile code directly from the ANTLR AST generated. 