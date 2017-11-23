# java-lexer-parser-for-grammars
Java Lexer/Parser for Grammars

# Grammar: 

Expression  =  BooleanExpression .

BooleanExpression  =  BooleanTerm { "or" BooleanTerm } .

BooleanTerm  =  BooleanFactor { "and" BooleanFactor } .

BooleanFactor  =  [ "not" ]
                  ArithmeticExpression [ ( "=" | "<") ArithmeticExpression) ] .

ArithmeticExpression  =  Term { ("+" | "-") Term } .

Term  =  Factor { ("*" | "/") Factor } .

Factor  =  Literal  |  Identifier  |  "(" Expression ")" .

Literal  =  BooleanLiteral  |  IntegerLiteral .

BooleanLiteral  =  "false"  |  "true" .

IntegerLiteral  =  Digit { Digit } .

Identifier  =  Letter { Letter | Digit | "_" }.

Digit  =  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" .

Letter  = "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k"
        | "l" | "m" | "n" | "o" | "p" | "q" | "u" | "r" | "s" | "t" | "u"
        | "v" | "w" | "x" | "y" | "z"
        | "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K"
        | "L" | "M" | "N" | "O" | "P" | "Q" | "U" | "R" | "S" | "T" | "U"
        | "V" | "W" | "X" | "Y" | "Z" .


Comments:

0.  Integer literals and identifiers are treated as complete terminal symbols. 
    That is, `id` is a single terminal symbol, not a sequence of two symbols
    (`i` and `d`).

1.  White space is allowed anywhere, except within terminal symbols.

2.  A comment begins with a double slash (`//`) and terminates at the end of
    line.

3.  "Maximum munch" applies. For example, `begin1` is an identifier, unlike
    `begin 1`, which is the keyword `begin` followed by the integer `1`.

4.  Do not attach too much importance to the names of the various kinds of
    expression: they do not always indicate the type.
