package syntax.compilation.i2p;

import java.util.Stack;
import java.util.StringTokenizer;

public class Infix2PostfixConverter {

	private static boolean isOperator(String token)
	{
		if(token == null || token.length() != 1) {
			return false;
		}
		char t = token.charAt(0);
		return t == '+' || t == '-' || t == '*' || t == '/' || t == '^'
				|| t == '(' || t == ')';
	}

	private static boolean isLowerPrecedence(char op1, char op2)
	{
		switch (op1)
		{
		case '+':
		case '-':
			return !(op2 == '+' || op2 == '-');

		case '*':
		case '/':
			return op2 == '^' || op2 == '(';

		case '^':
			return op2 == '(';

		case '(':
			return true;

		default:
			return false;
		}
	}

	public static String convertToPostfix(String infix)
	{
		Stack<String> opStack = new Stack<String>();
		StringBuffer postfix = new StringBuffer(infix.length());

		StringTokenizer parser = new StringTokenizer(infix, "+-*/()", true);

		while(parser.hasMoreTokens()) {
              String token =parser.nextToken();
              if(isOperator(token)) {
            	 char c=token.charAt(0);
            	 while(!opStack.isEmpty() && !isLowerPrecedence(opStack.peek().charAt(0), c)) {
            		 postfix.append(" ");
            		 postfix.append(opStack.pop());
            	 }
            	 if(c==')') {
            		 String op= opStack.pop();
            		 while (op.charAt(0) !='(') {
            			 postfix.append(" "+op);
                         op = opStack.pop();
            		 }
            	 } else {
					opStack.push(token);
				 }
              }else if(token.trim().length() == 0) {
              }else {
                postfix.append(" "+token);
              }

		}
		while(!opStack.isEmpty()) {
			postfix.append(" "+opStack.pop());
		}
		return postfix.toString().trim();
	}
}

