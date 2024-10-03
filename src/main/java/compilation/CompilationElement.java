package compilation;

import java.util.Arrays;

import util.CharSeqUtil;

public class CompilationElement {

    private String base;
    private CompilationElement[] params;
    private CompilationElementType type;

    public CompilationElement(String base, CompilationElementType type) {
        this.base = base;
        this.type = type;
        this.params = new CompilationElement[0];
    }

    public CompilationElement(String groupName, CompilationElement[] params) {
        this.type = CompilationElementType.INNER_CALL;
        this.base = groupName;
        this.params = params;
    }

    private CompilationElement() {
        if (params == null) {
            params = new CompilationElement[0];
        }
    }
    // parameter: if it beginsWith " then it is handled as literal

    public CompilationElement(String seq, boolean callAllowed) {
    	
    	if (seq.isEmpty()) {
    		this.type = CompilationElementType.ESCAPED_STRING;
    		base = "";
    		return;
    	}
    	char ch = seq.charAt(0);
    	
    	if (ch == '\"' || ch == '\'') {
    		this.type = CompilationElementType.ESCAPED_STRING;
    		base = CharSeqUtil.resolveFormattedSeq(seq.substring(1, seq.length() - 1));
    	} else if (ch == '*') {
    		this.type = CompilationElementType.GROUP_REFERENCE;
    		base = seq.substring(1);
        } else if (ch == '<') {
            if (seq.startsWith("<\"") && seq.endsWith("\">")) {
                this.type = CompilationElementType.GET;
                base = seq.substring(2, seq.length() - 2);
                if (base.isEmpty())
                    throw new RuntimeException("Empty GET!" + seq);
            } else {
                throw new RuntimeException("Invalid GET: " + seq);
            }
        } else if (ch == '[') {
            if (seq.endsWith("]")) {
                this.type = CompilationElementType.PUT;
                seq = seq.substring(1, seq.length() - 1);
                int firstCommaIndex = CharSeqUtil.getNonQuotedIndex(seq, ",", 0);
                String key = seq.substring(0, firstCommaIndex);
                key = key.trim();
                if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("\'") && key.endsWith("\'"))) {
                    base = key.substring(1, key.length() - 1);
                } else
                    throw new RuntimeException("Invalid PUT: [" + seq + "]");
                params = createValues(seq.substring(firstCommaIndex + 1));
            } else {
                throw new RuntimeException("Invalid PUT: " + seq);
            }
        }
        else {
    		int openBracketIndex = seq.indexOf("(");
    		if (openBracketIndex < 0) {
    			this.type = CompilationElementType.SOURCE_REFERENCE;
    			base = seq;
    		} else {
    		    if(!callAllowed)  throw new RuntimeException("Nested calls are not allowed!");
    			this.type = CompilationElementType.INNER_CALL;
    			base = seq.substring(0, openBracketIndex);
    			params = createParams(seq, openBracketIndex);
    		}
    	}
    	if (params == null) {
    		params = new CompilationElement[0];
    	}
    }

    private CompilationElement[] createValues(String putValue) {
        params = createParamsFromBareString(putValue);
        return params;
    }

    public CompilationElement(String seq) {
        this(seq,true);
    }


	private CompilationElement[] createParams(String seq, int openBracketIndex) {
        int closingBracketIndex = seq.indexOf(")", openBracketIndex);
        String params = seq.substring(openBracketIndex + 1, closingBracketIndex);
        CompilationElement[] result = createParamsFromBareString(params);
        return result;
    }

    public CompilationElement[] createParamsFromBareString(String params) {
        String[] splittedParams = params.split("\\+");
        CompilationElement[] result = new CompilationElement[splittedParams.length];

        for (int i = 0; i < splittedParams.length; i++) {
            result[i] = new CompilationElement(splittedParams[i].trim() ,false);
        }
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CompilationElement)) {
            return false;
        }
        CompilationElement o = (CompilationElement) other;
        if (!Arrays.deepEquals(params, o.getParams())) {
            return false;
        }
        if (!base.equals(o.base)) {
            return false;
        }
        return type == o.type;
    }

    public String getBase() {
        return base;
    }

    public CompilationElementType getType() {
        return type;
    }

    public void setBase(String base) {
        this.base = base;
    }

    @Override
    public String toString() {

        if (type == CompilationElementType.ESCAPED_STRING) {
            return "\"" + base + "\"";
        }
        if (type == CompilationElementType.GROUP_REFERENCE) {
            return "*" + base;
        }
        if (type == CompilationElementType.SOURCE_REFERENCE) {
            return base;
        }
        if (type == CompilationElementType.INNER_CALL) {
            return base + "(" + createParamString() + ")";
        }
        if (type == CompilationElementType.GET) {
            return "<" + base + ">";
        }
        if (type == CompilationElementType.PUT) {
            return "[" + base + "," + createParamString() + "]";
        }

        throw new RuntimeException("Invalid parameter type: " + "\'" + type + "\'");
    }

    private String createParamString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                sb.append("+");
            }
            sb.append(params[i].toString());
        }
        return sb.toString();
    }

    public CompilationElement[] getParams() {
        return params;
    }

    public CompilationElement copy() {
        CompilationElement ceCopy = new CompilationElement();
        ceCopy.base = this.base;
        ceCopy.type = this.type;
        if (params == null) {
            params = new CompilationElement[0];
        }
        ceCopy.params = new CompilationElement[params.length];

        for (int i = 0; i < params.length; i++) {
        	if(params[i].getType() == CompilationElementType.INNER_CALL) continue;
            ceCopy.params[i] = params[i];
        }
        return ceCopy;
    }

    public void setParams(CompilationElement[] params) {
        this.params = params;
    }

}
