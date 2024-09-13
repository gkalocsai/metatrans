## S2T -  Text/code conversion tool


This tool helps to convert any kind of source text/code to whatever you need. 
It has no built-in file formats, but one can easily create **file format definitions** and **conversion rules**.

Usage:

    java -jar s2t.jar conversionDescriptionFile sourceFile [options] 

The conversion rules are in one file.
Source file content (**hi.txt**):

    Hi!
	
The conversion description contains only of groups and comments. E.g.: (**welcome.s2t**) 

    /* comment example */
     welcome_message{
	      "Hi!" >> "Hello!" ;
	}
What you see here is a comment and group called welcome_message.
It has one conversion rule. ("Hi!" >> "Hello!" ;)  If the source file contains only the character sequence Hi! and NOTHING ELSE  (no space, no new line character) then it prints the "Hello!" message to the standard output.

Save these files and try to run the following command:

     java -jar s2t.jar welcome.s2t hi.txt

You can use multiple rules in the groups. The conversion rules are built from two parts. On the left side of the '>>' signs you define the syntax of the source file.
The syntax part (E.g: "Hi!") can be built from elements which are separated with spaces.

The element types:




   


