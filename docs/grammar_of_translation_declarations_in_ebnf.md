# MetaTrans description format — EBNF grammar (parser-generator ready)

Below is a compact EBNF grammar suitable for feeding a parser generator (Lexer + Parser). It includes token regular expressions for identifiers, digits and whitespace and a descriptor subgrammar. Some checks are semantic (not easily enforceable in pure grammar) — these are listed after the grammar.

Notes:
- Terminals are written in ALL_CAPS. Nonterminals in lowerCamel.
- Use the lexer token regexps provided under "LEXER TOKENS".
- For descriptors (left-side char-sequence descriptors) the lexer produces a single DESCRIPTOR token that is then parsed by descriptorBody (provided below) or further validated. The production for DESCRIPTOR is expanded so a generator can implement the descriptor lexer or parser as needed.
- Comments (/* ... */) are removed before parsing (RuleReader removes them). If your tool supports lexing comments, accept and ignore them.

---

## LEXER TOKENS (regex-like)

- WHITESPACE ::= [ \t\r\n]+
- COMMENT     ::= "/\*" (any char)*? "\*/"    (strip/ignore before parsing)
- ARROW       ::= "->"
- TRANS       ::= ">>"
- SEMI        ::= ";"
- DOTS        ::= "..."                     (repeater marker before a rightside)
- PLUS        ::= "+"
- COMMA       ::= ","
- LPAREN      ::= "("
- RPAREN      ::= ")"
- LBRACE      ::= "{"
- RBRACE      ::= "}"
- LBRACK      ::= "["
- RBRACK      ::= "]"
- LT          ::= "<"
- GT          ::= ">"
- COLON       ::= ":"
- QUESTION    ::= "?"
- STAR        ::= "*"
- SINGLE_QUOTE_PREFIX ::= "'"                (used as prefix for descriptor token)
- IDENT       ::= [A-Za-z_][A-Za-z0-9_]*     (group names, labels; conservative, adjust if you allow other chars)
- DIGITS      ::= [0-9]+
- // String literals for compilation:
  - DQUOTE_STRING ::= '"' ( ( '\\' . ) | ( [^"\\\] ) )* '"'  
  - SQUOTE_STRING ::= '\'' ( ( '\\' . ) | ( [^'\\\] ) )* '\''
    - Compilation-level allowed escape sequences resolved: \n \r \t \s \\ and numeric charcodes [NNN] inside strings are treated specially by code; lexer accepts backslash escapes but semantic validation will restrict them.
- DESCRIPTOR   ::= "'" descriptorText
  - descriptorText is complex (see descriptor grammar below). Lexers may choose to emit the entire descriptorText as one token; a parser can then run the descriptor grammar on descriptorText.

---

## TOP-LEVEL SYNTAX (EBNF)

file              ::= { group }
group             ::= IDENT "{" { rule SEMI } [rule] "}"  
rule              ::= groupname ARROW [DOTS] rightside [TRANS compilation]
groupname         ::= IDENT

rightside         ::= element { WHITESPACE element }
element           ::= [ QUESTION ] [ label COLON ] atom
label             ::= IDENT
atom              ::= descriptor | IDENT | "." IDENT  /* dot-prefix allowed; reader strips leading dot for semantic use */

descriptor        ::= DESCRIPTOR
                  /* DESCRIPTOR holds the textual descriptor body (leading single quote removed) */
                  /* descriptorText is parsed by descriptorBody (below) if finer-grained parsing is needed */

compilation       ::= compElem { WHITESPACE compElem }
compElem          ::= DQUOTE_STRING
                   | SQUOTE_STRING
                   | STAR IDENT                /* group reference, e.g. *name */
                   | IDENT                     /* source reference (label) */
                   | IDENT "(" callParams ")"  /* inner call (no nested calls allowed semantically) */
                   | LBRACK SQUOTE_STRING COMMA callParams RBRACK  /* PUT: ["key", ...] key must be quoted */
                   | LT SQUOTE_STRING GT       /* GET: <"key"> */

callParams        ::= callParam { PLUS callParam }
callParam         ::= DQUOTE_STRING | SQUOTE_STRING | IDENT | STAR IDENT

/* lexical WHITESPACE between tokens is normally optional where tokens are separated in parser rules,
   but the parser should skip WHITESPACE tokens at lexing time. */

---

## DESCRIPTOR SUBGRAMMAR (for descriptorText; leading single-quote removed)

A descriptor is a sequence of one-position descriptors. The parser may either lex each position token or parse descriptorText according to this grammar:

descriptorBody     ::= { oneCharToken }
oneCharToken       ::= escapeSeq
                    | "(" parenSet ")"
                    | bracketNumber
                    | PLAIN_CHAR
parenSet           ::= parenItem { WHITESPACE parenItem }
parenItem          ::= range | bracketNumber | escapeSeq | PLAIN_CHAR
range              ::= parenAtom "-" parenAtom
parenAtom          ::= PLAIN_CHAR | bracketNumber | escapeSeq
bracketNumber      ::= "[" DIGITS "]"            /* digits only; non-empty */
escapeSeq          ::= "\" SPEC_CHAR
SPEC_CHAR          ::= one of: "[" | "]" | "s" | "\" | "(" | ")" | "-" | "r" | "n" | "t"
PLAIN_CHAR         ::= any single Unicode character except whitespace, backslash, '(', ')', '[' , ']'
                       /* PLAIN_CHAR may be any visible character; if it equals a special char in context,
                          it should be escaped. */

Notes:
- Examples:
  - "alma" descriptorText = a l m a (4 PLAIN_CHAR tokens)
  - "(0-9)" is one token when used as a oneCharToken (a parenthesized range); when the descriptor contains "'(0-9)g" the slice yields tokens: "(0-9)" then "g".
- Lexer approach: when scanning descriptorText:
  - If next char is '\' consume backslash + next char as escapeSeq token.
  - Else if next char is '(' consume until matching ')' (no nested parens in descriptor syntax) as parenSet (inner content parsed further).
  - Else if next char is '[' consume until matching ']' as bracketNumber.
  - Else consume single character as PLAIN_CHAR.
- This matches CharSequenceDescriptor.sliceDescriptor behaviour.

---

## SEMANTIC / VALIDATION RULES (to check after parse)

The following constraints are enforced by the implementation (DescriptorValidator and other code) and should be validated by a semantic checker:

1. Descriptor cannot be null. Empty descriptorBody is allowed (represents zero-length descriptor).
2. Last character of descriptor must not be an unpaired backslash (no single trailing '\').
3. Escape sequences: after `\` only SPEC_CHAR characters are allowed: one of []s\\()-rnt (i.e. `\\[` `\\]` `\s` `\\` `\\(` `\\)` `\-` `\r` `\n` `\t`).
4. Parentheses `(` `)` must be balanced, non-empty, and must not overlap improperly. Paren content cannot be empty.
5. Square brackets `[ ]` must contain only DIGITS and cannot be empty. Square-bracket groups must either be:
   - used inside parentheses `( ... [123] ... )` OR
   - appear as a standalone one-position descriptor `[123]` (outside parentheses).
   - Square brackets are not allowed as free tokens outside these two uses.
6. No empty `()` or `[]` tokens.
7. Hyphen (`-`) interval usage:
   - A hyphen describing a range must occur inside parentheses and must form a valid range `X-Y` where X and Y are single tokens (PLAIN_CHAR or bracketNumber or escapeSeq).
   - Validator enforces spaces/positioning rules around hyphens: ensure tokens adjacent to `-` are valid and not parentheses boundary tokens.
8. Optional elements: number of optional elements in a rule ≤ 10 (OptionalVConverter.MAX_ALLOWED_OPTIONAL_ELEMENTS).
9. PUT syntax must be exactly: `["key", value1 + value2 + ...]` where key is a quoted string.
10. GET syntax must be exactly `<"key">`.
11. INNER_CALL semantics: nested inner calls (calls inside call parameters) are forbidden by the parser/constructor — enforce at semantic level.
12. Repeater `...` applies to the entire rightside; repeater rules require additional semantic handling.

---

## Example snippets (parsed forms)

1. Group with a descriptor and compilation:
   Root { s -> 'hello TRANS *s "!" ; }
   - tokens: IDENT('Root') LBRACE IDENT('s') ARROW DESCRIPTOR("'hello") TRANS STAR IDENT('s') DQUOTE_STRING('"!"') SEMI RBRACE

2. Descriptor with parentheses and ranges:
   descriptorText: (a-z 0-9 \s [65] [97]-[122])
   - parenSet items: 'a-z', '0-9', '\s', '[65]', '[97]-[122]'

3. PUT / GET:
   >> [ "key", name + "!" ] <"key">

---

## Implementation guidance for a parser generator

- Lex as many atomic tokens as possible:
  - Produce DESCRIPTOR as a single token beginning with a single quote `'` and including the descriptorText (slice rules above). Alternatively, lex descriptor into sub-tokens (LPAREN etc.) but ensure the parser uses the descriptor subgrammar.
  - Treat DQUOTE_STRING and SQUOTE_STRING as string tokens; decode escapes in a post-lexing normalization step according to the engine rules (\n \r \t \s \\ and `[NNN]`).
- After parsing, run the semantic checks (DescriptorValidator, OptionalVConverter, CompilationElement constructor validations) to enforce constraints not easily encoded in grammar (e.g., interval hyphen checks, square bracket placement, PUT/GET key quoting, nested inner-call prohibition).
- The RuleReader normally strips comments before lexing; if your lexer keeps comments, ignore them.
