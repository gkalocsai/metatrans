package compilation;

import java.util.Arrays;

import util.CharSeqUtil;

public class CompilationElement {

    private String base;
    private char type;
    private CompilationElement[] params;

    public CompilationElement(String base, char type) {
        this.base = base;
        this.type = type;
        this.params = new CompilationElement[0];
    }

    public CompilationElement(String groupName, CompilationElement[] params) {
        this.type = '(';
        this.base = groupName;
        this.params = params;
    }

    private CompilationElement() {
        if (params == null) {
            params = new CompilationElement[0];
        }
    }
    // parameter: if it beginsWith " then it is handled as literal

    public CompilationElement(String seq) {
        if (seq.isEmpty()) {
            this.type = '\"';
            base = "";
            return;
        }
        char ch = seq.charAt(0);

        if (ch == '\"' || ch == '\'') {
            this.type = '\"';
            base = CharSeqUtil.resolveFormattedSeq(seq.substring(1, seq.length() - 1));
        } else if (ch == '*') {
            this.type = '*';
            base = seq.substring(1);
        } else {
            int openBracketIndex = seq.indexOf("(");
            if (openBracketIndex < 0) {
                this.type = ' ';
                base = seq;
            } else {
                this.type = '(';
                base = seq.substring(0, openBracketIndex);
                params = createParams(seq, openBracketIndex);
            }
        }

        if (params == null) {
            params = new CompilationElement[0];
        }

    }

    private CompilationElement[] createParams(String seq, int openBracketIndex) {
        int closingBracketIndex = seq.indexOf(")", openBracketIndex);
        String params = seq.substring(openBracketIndex + 1, closingBracketIndex);
        String[] splittedParams = params.split("\\+");
        CompilationElement[] result = new CompilationElement[splittedParams.length];

        for (int i = 0; i < splittedParams.length; i++) {
            result[i] = new CompilationElement(splittedParams[i].trim());
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

    public char getType() {
        return type;
    }

    public void setBase(String base) {
        this.base = base;
    }

    @Override
    public String toString() {

        if (type == '\"') {
            return "\"" + base + "\"";
        }
        if (type == '*') {
            return "*" + base;
        }
        if (type == ' ') {
            return base;
        }
        if (type == '(') {
            return base + "(" + createParamString() + ")";
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
            ceCopy.params[i] = params[i].copy();
        }
        return ceCopy;
    }

    public void setParams(CompilationElement[] params) {
        this.params = params;
    }

    // -E>

}
