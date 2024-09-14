
# S2T - Text/code conversion engine

## Download: 
**https://github.com/gkalocsai/s2t/releases**

This tool helps to convert any source text/code to whatever you need. 
It has no built-in file formats, but one can easily create **file format definitions** and **conversion rules**.
The base idea of the translation is that you can classify the parts of the source and give names to those. E.g.:

    rootGroup{
        package_declaration package_body >> "Ready."
    }
means that the source file can be divided into two parts: package_declaration followed by package_body and it has to be translated to "Ready." as the result text.

More rules can be in a group and S2T tries to apply them by their syntax description from top to bottom.

## Usage:

    java -jar s2t.jar conversionDescriptionFile sourceFile [options] 

The conversion rules are in one file.
Source file content (**hi.txt**):

    Hi!
	
The conversion description contains only groups and comments. E.g.: (**welcome.s2t**) 

    /* comment example */
     welcome_message{
	      "Hi!" >> "Hello!" ;
	}
What you see here is a comment and group called welcome_message.
It has one conversion rule. ("Hi!" >> "Hello!" ;)  If the source file contains only the character sequence Hi! and NOTHING ELSE  (no space, no newline character) then it prints the "Hello!" message to the standard output.

Save these files and try to run the following command:

     java -jar s2t.jar welcome.s2t hi.txt

The conversion rules are built from two parts. On the left side of the >> rule, there is the syntax description. 

## Syntax description

The aim is to match the groups to some parts of the source. 
 The elements of the syntax description are separated by **space**(s).

**The elements are:**
 - Group name ref  (e.g.: welcome_message) 
 - Repeater
 - Character sequence descriptor 

**Group name ref**
Just the name of the group

**Recursive group name references**

You can use only direct references to the group in each rule. If you use indirect recursion, then s2t tries to convert those rules to direct recursive. 
If you use only one in the rule it is always valid.
You can use two recursive references if those are at the beginning and at the end of the rule.
You cannot use three or more recursive references in a rule.

E.g.:

    expression{
       "(" expression ")";
       expression "+" expression;
       "-" expression;
    }

is valid, but 

    expression{
       digits;       
       expression "*" expression "+" expression;
    }
is not!

**Repeater:**

This element tells that all the syntax elements in the rule have to be matched to repeating element(s) in the source. E.g.:

    rootGroup {
         ...welcome_message " " name "!" >> "Hello";
    }
    welcome_message {
    "Hi";
    }
    name
    { 
      "Peter";
      "Lucy";
    }  
The three dots note that this whole rule must be applied to the repeating elements. 
So if s2t founds `"Hi Peter!Hi Lucy!"` in the source then the result will be `"HelloHello"`.


**Character sequence descriptor:** 

In most of the cases, a character sequence descriptor is a simple string. 
In the string instead of any character you can define a non-empty character set, which means s2t applies that rule on multiple strings. 
Character set definitions:

     "(c h a r a c t e r s S e p a r a t e d b y S p a c e s)ello" 

At the first position, there is a set. This matches for example "hello", "sello", "cello" etc.)
You can use the minus sign to describe character-intervals:

    "(0-9)"

this means a decimal digit. 

    "(0-9 a-z)"

means a decimal digit OR a lowercase letter.
   
Inside the parentheses, you can define characters with their decimal UNICODE-16 character code. E.g.:

    /*semicolon*/
    SC{
      "([59])";
    }
    /*double quote*/
    DQ {
        "([34])";
    }
    /*quote*/
    Q {
       "([39])";
    }
    OPEN_PARENTHESIS{
       "([40])";
    }
    CLOSING_PARENTHESIS {
       "([41])";
    }
    COMMA{
      "([44])";
    }

**Recursive (self) references**

You can use only direct references to the group in each rule. If you use indirect recursion, then s2t tries to convert those rules to direct recursive. 
If you use only one in the rule it is always valid.
You can use two recursive references only if they are at the beginning and the end of the rule.
You cannot use three or more recursive references in a rule.

E.g.:

    expression{
       "(" expression ")";
       expression "+" expression;
       "-" expression;
    }

is valid, but 

    expression "*" expression "+" expression;

    
is not!

## Result description

The result can be described after the '>>' sign. If you don't want to translate this named part of the source, you can omit this part.
The elements of the description are separated with space(s). The result of each component is appended to the standard output.


**The result elements:**

 - group name: translates to the corresponding substring of the source E.g.: name >> name;
 - *T means that group T will decide how to translate this source part.
 - "format string" or 'format string' -> translates to the resolved format string.
 -  label or *label: you can give each element a label. E.g.: `name1:name COMMA name2:name >> name2 "," name1;`   <- This changes the sequence of the two names;


## More examples:
https://github.com/gkalocsai/s2t/tree/main/examples

## Contact: 
gen.gabor@gmail.com

 
