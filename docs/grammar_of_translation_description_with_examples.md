# Grammar of the MetaTrans description format

Below is a concise, practical grammar and explanation of the description format used by the transpiler (MetaTrans). It combines a formal-ish EBNF with plain-language notes and examples for all important constructs: groups, rules, syntax (left) side, character-sequence descriptors, optional & repeater markers, labels, compilation (right of >>), compilation element kinds, escapes and commenting rules.

This description is derived from the provided source code (parsers and validators) and reflects the concrete expectations implemented there.

---

## High-level file structure (what you edit)

- A file is a sequence of groups.
- Comments are C-style multiline only: /* ... */ (they are removed before parsing).
- Inside groups there are rules terminated by semicolons `;`.

EBNF:
file          ::= { group }
group         ::= IDENT "{" { rule ";" } "}"
rule          ::= rule-body
rule-body     ::= groupname "->" [ "..." ] rightside [ ">>" compilation ]
rightside     ::= element { SP element }
compilation   ::= compElem { SP compElem }
SP            ::= one or more ASCII whitespace characters

Notes:
- `IDENT` = group names and labels: any sequence of non-whitespace, non-special chars used as identifiers by the engine (practically tokens without quotes/parentheses). Labels appear as `label:element` in the rightside and are optional.
- `...` (three dots) before the rightside marks the rule as a REPEATER rule (applies to repeated sequences).
- Rules separated by `;` within a group; trailing rule without `;` is tolerated by the reader if it's last.

Examples:
- Simple group with one rule:
  Root{ "Hi" >> "Hello"; }
- Repeater:
  items{ ...item " " >> *item ; }

---

## Right side (syntax pattern to match)

A rule's left/syntax side is a sequence of elements separated by spaces. Each element is one of:
- a group reference (identifier),
- a character sequence descriptor (starts with a single-quote `'`),
- a labeled element (label:`element`),
- an optional element (prefixed by `?`),
- a dot-prefix element (prefixed by `.` used by internal parsing; treated like element but `.` is stripped in code when building element),
- the `...` repeater applies to the whole rule, not to individual elements.

EBNF (elements):
element       ::= [ "?" ] [ label ":" ] atom
label         ::= IDENT
atom          ::= charsetDescriptor | IDENT

- `charsetDescriptor` is a character-sequence descriptor (see next section).
- `IDENT` used here denotes a group reference (GroupName).

Examples of rightside:
- `name` — group reference to `name`.
- `?"re" "try"` — optional descriptor elements (note: descriptors start with `'` in external syntax, see below).
- `first:name ',' :separator` — a labeled group reference.

Important: The parser identifies descriptors by seeing a leading single-quote (`'`) at the start of the token; the token extends until the next whitespace (unless inside parentheses used by descriptors). There is no trailing closing `'` char used by the parser — the single quote is a prefix marking the token as a descriptor.

---

## Character-sequence descriptor (CharSequenceDescriptor)

Character-sequence descriptors let you describe a fixed-length sequence of characters. They are used as syntax elements on the left side of rules and are prefixed by a single quote `'` when written in a rule.

General shape: `'descriptorBody` where descriptorBody is parsed by CharSequenceDescriptor.

High-level:
- A descriptor is a sequence of one-character descriptors.
- A single one-character descriptor can be:
  - a literal character,
  - an escaped character (starts with backslash `\`),
  - a parenthesized set `( ... )` containing one-or-more tokens separated by spaces; each token inside may be a single char, an interval `a-z`, or a square-bracket numeric charcode `[65]`,
  - a numeric charcode written as `[NNN]` (decimal Unicode code point) — used both inside parens and as a standalone one-character descriptor.

Concrete EBNF (approximate):
descriptorBody ::= { oneCharDescriptor }
oneCharDescriptor ::= escapeSeq
                    | "(" innerList ")"
                    | "[" digits "]"
                    | singleChar
innerList      ::= token { SP token }
token          ::= range | bracketNumber | escapedChar | singleChar
range          ::= tokenNoRange "-" tokenNoRange
tokenNoRange   ::= singleChar | "[" digits "]" | escapeSeq
escapeSeq      ::= "\" specChar
specChar       ::= one of: '[' ']' 's' '\' '(' ')' '-' 'r' 'n' 't'
singleChar     ::= any char other than space, backslash, '(' , ')' (but can appear in contexts)

Notes / constraints (from DescriptorValidator and OneCharDesc):
- Parentheses `( ... )` are used to express a set of allowed characters at a single position: e.g. `(a b c)` matches any of 'a', 'b' or 'c' in that position.
- Inside `( ... )` items are separated by spaces. Items may be:
  - single literal characters (like `a`),
  - escaped specials like `\s` (space), `\n`, `\r`, `\t`, `\\`, `\\(`, `\\)` and `\\[` `\\]`,
  - numeric charcodes in square brackets `[65]` which represent the Unicode character with that decimal code,
  - ranges `a-d`, or `[15]-[30]` (hyphen denotes interval).
- A top-level `[N]` used outside parentheses stands for a single character with codepoint N (same as inside).
- Backslash rules:
  - A backslash `\` must be followed by one of the allowed spec characters set: isSpec() in code: "[]s\\\\()-rnt" (that is: `[`, `]`, `s`, `\`, `(`, `)`, `-`, `r`, `n`, `t`).
  - A single trailing backslash at the very end of the whole descriptor is invalid.
- Parentheses and square brackets must be balanced and used correctly; square brackets must occur only inside parentheses or as single-number descriptors; contents between square brackets must be digits only (no empties).
- Ranges are only valid inside parentheses and must follow allowed form (Validator enforces spacing rules around `-` and parentheses).

Examples:
- `'alma` — matches the literal 4-character sequence a l m a.
- `'(0-9)` — single-character descriptor that matches any digit (written as one `oneCharDescriptor` that itself is parentheses with a range).
- `'(a-z A-Z 32)` — first char any lowercase or uppercase or char code 32 (space).
- `'a(a-d \s [15] [25]-[30])g` — sequence of three positions: 'a', one-of (interval a-d or escape \s or code 15 or codes 25..30), and 'g'.

Important parsing note: CharSequenceDescriptor.sliceDescriptor logic:
- It iterates through the descriptor string and:
  - if sees `\` it takes the next character together (treats as one token `\x`),
  - if sees `(` it takes everything until the next `)` as a single token (content inside parens, not including delimiters),
  - otherwise a single character token.
So each char-position in a descriptor becomes one token for OneCharDesc.

---

## Optional elements and labels

- Optional element syntax: prefix `?` before the element. Example: `?name` or `?label:name` or `? 'a'`.
  - In internal processing `?` is stripped and handled by OptionalVConverter which generates rules for all combinations.
- Labeling: `label:element` assigns a label to an element. Labels are used later in compilation expressions (they are referred to by name).
  - If no explicit label is provided, the engine sets defaults (the referenced group name or autogenerated label).
- A `.` prefix (dot) on rightside element is allowed in input and removed for the element; used by some higher-level constructs in the reader.

---

## Compilation (the transformation description after >>)

- The compilation part appears after `>>` and is a sequence of compilation elements separated by spaces. It describes how to build the output when that rule matches.
- Each compilation element is one of:
  - Escaped string literal — enclosed in double quotes "..." or single quotes '...': becomes an ESCAPED_STRING element.
    - Inside such string the engine supports escapes and bracketed `[N]` character codes which are resolved (CharSeqUtil.resolveFormattedSeq).
  - Source reference — a bare identifier that refers to a labeled element from the matched syntax (in compilation it is used to insert the original source substring).
  - Group reference — `*IDENT` — means recursively transpile the referenced matched group (use that group's compilation) for the matched substring.
  - INNER_CALL — a call to another group with a constructed inner source: IDENT(params) where params are `+`-separated compilation-element-like fragments (but nested calls are not allowed).
  - PUT — `[ "key", params ]` — sets a global variable named `key` to the value of the built params (params separated by `+` inside).
  - GET — `<"key">` — inserts the stored value for `key` (if present) into the output.

EBNF (essential):
compElem      ::= escapedString
                | "*" IDENT                    -- group reference
                | IDENT                        -- source (label) reference
                | IDENT "(" callParams ")"     -- inner call (no nested calls allowed by parser)
                | "[" quotedString "," putParams "]" -- PUT
                | "<" quotedString ">"         -- GET
callParams    ::= callParam { "+" callParam }
putParams     ::= callParams
callParam     ::= escapedString
                | IDENT       -- source reference (label)
                | "*" IDENT   -- group reference (treated specially inside inner-call params)
escapedString ::= '"' formattedString '"' | "'" formattedString "'"
quotedString  ::= escapedString (must be quoted)
formattedString ::= resolved sequence; supports backslash escapes (\n \r \t \s \\ \\[ \\] \\() and [NNN] numeric codes

Notes:
- In compilation, literal strings must be quoted (single or double). The engine's CompilationElement constructor accepts both `'` and `"` as string start for compilation elements.
- GET must be of form `<"key">` (a GET wrapping a quoted key inside `<" ... ">`).
- PUT must be of form `["key", value+value...]` where the key is a quoted string literal; the values are parsed into CompilationElement objects by splitting on `+` (but respecting quoted strings).
- For inner calls, parameters are joined to form an inner source string; the called group's compilation is run with that inner source. Nested calls are forbidden (constructor enforces callAllowed flag).

Examples:
- `>> *name " " other` — transpile by invoking group's transpilation for label `name`, then append a space, then append label `other` (source substring).
- `>> "*header" [ "x", name + "!" ] <"x">` — set PUT key `"x"` to `name + "!"`, then GET it.

---

## Tokens, quoting and escaping

- Strings in compilation are quoted with double quotes "..." or single quotes '...'. CompilationElement.resolveFormattedSeq resolves escapes within these: backslash sequences `\n`, `\r`, `\t`, `\s` (space), `\\` and bracket notation `[NNN]` (decimal code) are supported.
- In character-sequence descriptors (left side), escapes allowed after `\` are limited to spec characters: "[]s\\\\()-rnt" — that is, you may write `\s` (space), `\\`, `\\(`, `\\)`, `\\[`, `\\]`, `\n`, `\r`, `\t`, and `\-` (if used).
- Descriptor parentheses and square brackets must be balanced and used in the allowed contexts (Validator enforces many constraints: no stray square brackets outside parentheses, no empty `[]` or `()` pairs, digits only inside `[]`, no illegal minus usage, etc).

---

## Special rules & validation performed by the implementation

The code enforces numerous syntactic constraints; some of the important ones:

- Descriptor validation:
  - Descriptor string cannot be null.
  - Single trailing backslash is invalid.
  - Only allowed escape spec characters after `\`.
  - Square brackets must appear inside parentheses or be used alone as `[N]`.
  - Square-bracket contents must be digits and cannot be empty.
  - Parentheses and square brackets must be balanced.
  - Interval syntax around `-` must follow tight rules (no missing neighbors, required spaces around interval sequences within parentheses, etc).
  - No empty `()` or `[]`.
- Compilation element parsing:
  - PUT must have the key quoted and present: `["key", ...]`.
  - GET must be `<"key">` (starts with `<"` and ends with `">`).
  - Inner calls are allowed but nested calls are forbidden (the parser sets callAllowed=false for params).
- Optional elements expansion:
  - Optional elements (`?`) are expanded into multiple rules by OptionalVConverter; maximum allowed optional elements is enforced (max 10).
- Repeaters:
  - `...` before rightside turns the rule into a repeater (Rule.repeater = true). Repeater matching logic expects the rightside to be matched as repeating sequence.

---

## Quick examples and idioms

- Literal match of exact characters: use a descriptor for the sequence `'hello` which matches exactly "hello".
- Single-character character-class: `'(0-9)` — matches any digit for a single position.
- Character set at a single position: `'(a A ' ' 45)` where `45` in square brackets stands for char code 45.
- Labeling and compilation:
  Rule: `pair->first:name ":" second:name >> *second ":" *first`
  (swap first/second by labels and use group references).
- Inner call:
  `E->e1:exp op e2:exp >> *e1 " " *e2 " " op`
  or inner-call example: `some-> a b >> other(a + "X")` — calls group `other` with parameter built from label `a` and literal `"X"`.

---

## Implementation notes (for authors of parsers or format consumers)

- Left side descriptors are tokenized by a simple routine:
  - `\x` is one token (escape + following char),
  - `( ... )` region is one token (content without the parentheses),
  - otherwise each single char is a token.
- Character intervals are normalized and merged; e.g. `a-f z-h` may be merged as a contiguous interval if overlapping/adjacent.
- The engine internally converts descriptor escapes into a placeholder ('A') during validation to ease bracket/paren checks, but this is an internal detail; conforming input must follow the specified escape rules.
- `CharSeqUtil.getNonQuotedIndex` is used often to search for separators while skipping quoted substrings; quoting rules must be respected to avoid mis-splitting parameters.
