package descriptor;

public class DescriptorValidator {

    
	public static BooleanResult check (String dsc){
		
		
		if (dsc==null) {
			return new BooleanResult(false, "Descriptor string cannot be null");
		}
		if (dsc.isEmpty()) {
			return new BooleanResult(true, "Everything is OK!");
		}

		if (isLastCharIsASingleBackslash(dsc)) {
			return new BooleanResult(false, "LastCharIsASingleBackslash");
		}

		dsc = convertValidBackslashesToA(dsc);
		if(dsc == null) {
			return new BooleanResult(false, "Invalid escape sequence");
		}


		if(isLastCharInvalid(dsc, "([")) {
			return new BooleanResult(false, "Last char is invalid");
		}


		if (sgAfterBracket(dsc,' ')) {
			return new BooleanResult(false, "oneSpaceAfterBracket");
		}

		if (spaceBeforeClosingBracket(dsc)) {
			return new BooleanResult(false, "oneSpaceBeforeClosingBracket");
		}

		if (!areBracketsOK(dsc)) {
			return new BooleanResult(false, "A bracket is missing");
		}

		if (!areSqrBracketsInBrackets(dsc)) {
			return new BooleanResult(false, "Square bracket without parenthesis");
		}

		if (!areSqrBracketsOK(dsc)) {
			return new BooleanResult(false, "A square bracket is missing");
		}

		if (!areOnlyDigitsInSqrBrackets(dsc)) {
			return new BooleanResult(false, "Only digits are allowed between square brackets");
		}

		if(dsc.indexOf("[]")>=0){
			return new BooleanResult(false, "There is nothing between square brackets");
		}

		if(dsc.indexOf("()")>=0){
			return new BooleanResult(false, "There is nothing between brackets");
		}

		if (sgAfterBracket(dsc,'-')) {
			return new BooleanResult(false, "oneMinusAfterBracket");
		}

		if(!allIntervalDescriptionsOK(dsc)){
			return new BooleanResult(false, "Bad syntax around the minus character");
		}
		if(missingSpaceInParens(dsc)){
			return new BooleanResult(false, "Missing space inside the parentheses");
		}
		return new BooleanResult(true, "Everything is OK!");

	}


	public static boolean missingSpaceInParens(String dsc){
		boolean inside = false; 
		for (int i=0; i<dsc.length(); i++) {
			char c=dsc.charAt(i);
			if (c == '(') {
				inside = true;
				continue;
			}
			
			if (c == ')') {
				inside = false;
				continue;
			}
			//if (c =='-') continue;
			if(inside){
				if(c == '[' ) i = dsc.indexOf("]",i);
				char next = dsc.charAt(i+1);
				if(next != '-' && next!=' ' && next!=')'){
					return true;
				}else if(next == '-' || next == ' '){
					i++;
				}
			}
		}
		return false;
	}


	static boolean allIntervalDescriptionsOK(String dsc){
		boolean inside = false; 
		for (int i=0; i<dsc.length(); i++) {
			char c=dsc.charAt(i);
			if (c == '(') {
				if(inside) return false;
				inside = true;
				continue;
			}
			if (c == ')') {
				if(!inside) return false;
				inside = false;
				continue;
			}
			boolean condition = c=='-' && inside && !isCharIntervalSyntaxValid(dsc,i);
			if (condition) {
				return false;
			}
		}
		return true;
	}



	static boolean isCharIntervalSyntaxValid(String dsc, int i){

		char before=dsc.charAt(i-1);
		char after=dsc.charAt(i+1);
		if(isPartOf("( )", before))  return false;
		if(isPartOf("( )", after))  return false;

		int beforePreviousCharIndex=getBeforePreviousCharIndexToLeft(dsc,i-1);
		if(beforePreviousCharIndex < 0 ) return false;
		char p = dsc.charAt(beforePreviousCharIndex);
		if(p !=' ' && p!= '(') return false;

		int afterNextCharIndex=getAfterNextCharIndexToRight(dsc,i+1);
		if(afterNextCharIndex >=dsc.length() ) return false;
		char n= dsc.charAt(afterNextCharIndex);
		if(n !=' ' && n!= ')') return false;

		return true;
	}


	public static int getAfterNextCharIndexToRight(String dsc, int i){
		if(dsc.charAt(i) !='[') return i+1;
		for(;i<dsc.length();i++){
			if(dsc.charAt(i) == ']') return i+1;
		}

		throw new RuntimeException("Validation failed");
	}


	static int getBeforePreviousCharIndexToLeft(String dsc, int i){
		if(dsc.charAt(i) !=']') return i-1;

		for(;i>=0;i--){
			if(dsc.charAt(i) == '[') return i-1;
		}

		throw new RuntimeException("Validation failed");
	}
	static String convertValidBackslashesToA(String dsc) {
		StringBuilder sb = new StringBuilder();
		char c;
		for(int i=0;i<dsc.length();i++){
			c=dsc.charAt(i);
			if(c == '\\'){ 
				char next = dsc.charAt(i+1); //bec, we checked before: last char cannot be '\'
				if(!isSpec(next)){
					return null;
				}
				i++;
				sb.append('A');
				if (i==dsc.length()-1){
					break;
				}
			}else{
				sb.append(c);
			}
		}

		return sb.toString();

	}

	static boolean isSpec(char c) {
		return isPartOf("[]s\\()-rnt" , c);
	}



	static boolean isLastCharInvalid (String dsc, String invalidChars) {
		int lastCharIndex = dsc.length()-1;
		char lastChar =  dsc.charAt(lastCharIndex);
		return invalidChars.contains(""+lastChar);
	}


	static boolean isPartOf(String str, char c) {
		return str.contains(""+c);
	}



	static boolean isLastCharIsASingleBackslash (String dsc) {
		int lastCharIndex = dsc.length()-1;
		char lastChar =  dsc.charAt(lastCharIndex);
		if (lastChar == '\\' && dsc.length() == 1) {
			return true;
		}		
		if(dsc.length() == 1) return false;
		int almostLastCharIndex = dsc.length()-2;
		char almostLastChar = dsc.charAt(almostLastCharIndex);
		if (lastChar == '\\' && almostLastChar != '\\') {
			return true;
		} else {
			return false;
		}
	}	


	static boolean sgAfterBracket (String dsc, char sg) {
		String toSearch="("+sg;
		return dsc.indexOf(toSearch) >= 0;
	}

	static boolean spaceBeforeClosingBracket (String dsc) {
		return dsc.indexOf(" )") >= 0;
	}

	static boolean areBracketsOK (String dsc) {
		char c;
		boolean nextMustBeOpeningBracket = true;
		for(int i=0;i<dsc.length();i++){
			c=dsc.charAt(i);
			if (c == '(') {
				if (nextMustBeOpeningBracket == false) {
					return false;
				}
				nextMustBeOpeningBracket = false;
			}
			if (c == ')') {
				if (nextMustBeOpeningBracket == true) {
					return false;
				}
				nextMustBeOpeningBracket = true;
			}
		}
		return nextMustBeOpeningBracket;
	}
	

	static boolean areSqrBracketsInBrackets (String dsc) {
		char c;
		boolean inside = false;
		for(int i=0;i<dsc.length();i++){
			c=dsc.charAt(i);
			if (c == '(') {
				inside = true;
			}
			if (c == ')') {
				inside = false;
			}
			if ((c=='[' || c== ']') && !inside) {
				return false;
			}
		}
		return true;
	}

	static boolean areSqrBracketsOK (String dsc) {
		char c;
		boolean opening = true;
		for(int i=0;i<dsc.length();i++){
			c=dsc.charAt(i);
			if (c == '[') {
				if (opening == false) {
					return false;
				}
				opening = false;
			}
			if (c == ']') {
				if (opening == true) {
					return false;
				}
				opening = true;
			}
		}
		return opening;
	}

	static boolean areOnlyDigitsInSqrBrackets (String dsc) {
		char c;
		boolean inside = false;
		for(int i=0;i<dsc.length();i++){
			c=dsc.charAt(i);
			if (c == '[') {
				inside = true;
				continue;
			}
			if (c == ']') {
				inside = false;
				continue;
			}
			if (inside && !Character.isDigit(c)) {				
				return false;
			}
		}
		return true;
	}



}
