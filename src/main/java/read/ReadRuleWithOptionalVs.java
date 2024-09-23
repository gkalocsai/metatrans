package read;

import compilation.CompilationElement;
import syntax.SyntaxElement;

public class ReadRuleWithOptionalVs {

    private String groupname;
    private boolean[] optional;
    private String[] label;
    private SyntaxElement[] rightside;
    private CompilationElement[] compilation;

    public ReadRuleWithOptionalVs(String groupname, boolean[] optional, String[] label, SyntaxElement[] rightside,
            CompilationElement[] compilation) {
        super();
        this.groupname = groupname;
        this.optional = optional;
        this.label = label;
        this.rightside = rightside;
        this.compilation = compilation;
    }

    public static final int MAX_ALLOWED_OPTIONAL_ELEMENTS = 10;

    public boolean[] getOptional() {
        return optional;
    }

    public String getGroupname() {
        return groupname;
    }

    public SyntaxElement[] getRightside() {
        return rightside;
    }

    public String[] getLabel() {
        return label;
    }

    public CompilationElement[] getCompilation() {
        return compilation;
    }

    public boolean hasTooMuchOptionalElements() {
        return countOfOptionalElements() > MAX_ALLOWED_OPTIONAL_ELEMENTS;
    }

    public int countOfOptionalElements() {
        int numberOfOptionalElements = 0;
        for (boolean temp : optional) {
            if (temp)
                numberOfOptionalElements++;
        }
        return numberOfOptionalElements;
    }

}
