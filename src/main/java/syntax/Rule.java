package syntax;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import compilation.CompilationElement;
import compilation.CompilationElementType;

public class Rule {

    String groupname;
    String[] labels;

    SyntaxElement[] rightside;
    CompilationElement[] compilation;

    private String[] rightSideAsString;
    private boolean directRecursive;

    private boolean repeater;

    public Rule(String groupname, SyntaxElement[] rightside, String[] labels, CompilationElement[] compilation,
            boolean repeater) {

        this.groupname = groupname;
        this.rightside = rightside;
        this.repeater = repeater;

        if (labels == null) {
            this.labels = new String[rightside.length];
            Arrays.fill(this.labels, "");
        } else {
            this.labels = labels;
        }
        if (compilation == null || compilation.length == 0) {
            this.compilation = new CompilationElement[1];
            this.compilation[0] = new CompilationElement("", CompilationElementType.ESCAPED_STRING);
        } else {
            this.compilation = compilation;
        }

        setDefaultLabels();
        if (this.rightside.length != this.labels.length) {
            throw new RuntimeException("Different arrayLengths");
        }
        this.directRecursive = containsRefOnRightSide(groupname);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Rule)) {
            return false;
        }
        Rule o = (Rule) other;
        if (!groupname.equals(o.groupname)) {
            return false;
        }
        if (!Arrays.deepEquals(rightside, o.rightside)) {
            return false;
        }
        if (!Arrays.deepEquals(compilation, o.compilation)) {
            return false;
        }
        if (!Arrays.deepEquals(labels, o.labels)) {
            return false;
        }

        return true;

    }

    public Rule copy() {
        String[] labelsCopy = copyLabels();
        SyntaxElement[] rsCopy = copyRightside();
        CompilationElement[] ceArrayCopy = copyCompilation();
        return new Rule(this.groupname, rsCopy, labelsCopy, ceArrayCopy, repeater);
    }

    public CompilationElement[] copyCompilation() {
        CompilationElement[] ceArrayCopy = new CompilationElement[this.compilation.length];
        for (int i = 0; i < ceArrayCopy.length; i++) {
            ceArrayCopy[i] = compilation[i].copy();
        }
        return ceArrayCopy;
    }

    public SyntaxElement[] copyRightside() {
        SyntaxElement[] rsCopy = new SyntaxElement[this.rightside.length];
        for (int i = 0; i < rsCopy.length; i++) {
            rsCopy[i] = rightside[i].copy();
        }
        return rsCopy;
    }

    public String[] copyLabels() {
        String[] labelsCopy = new String[this.labels.length];
        for (int i = 0; i < labelsCopy.length; i++) {
            labelsCopy[i] = labels[i];
        }
        return labelsCopy;
    }

    @Override
    public String toString() {
        // return toStringWoCompilation();
        StringBuilder sb = new StringBuilder();
        sb.append(this.toSyntax());
        if (compilation.length != 0) {
            sb.append(">>" + createCompilationString(compilation));
        }
        return sb.toString();
    }

    public String toSyntax() {
        // return toStringWoCompilation();
        StringBuilder sb = new StringBuilder();
        sb.append(this.groupname + "->");
        if (isRepeater())
            sb.append("...");
        addRightsideString(sb);
        return sb.toString();
    }

    private void addRightsideString(StringBuilder sb) {
        for (int i = 0; i < rightside.length; i++) {
            if (getLabels()[i] != null && !getLabels()[i].isEmpty()
                    && !getLabels()[i].equals(rightside[i].getReferencedGroup())) {
                sb.append(getLabels()[i] + ":");
            }
            sb.append(rightside[i]);
            if (i < rightside.length - 1) {
                sb.append(" ");
            }
        }
    }

    public static boolean areInDifferentGroups(Rule r1, Rule r2) {
        return !r1.groupname.equals(r2.groupname);
    }

    private String createCompilationString(CompilationElement[] compilation) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (CompilationElement ce : compilation) {
            if (!first) {
                sb.append(" ");
            } else {
                first = false;
            }
            sb.append(ce);
        }
        return sb.toString();
    }

    public SyntaxElement getFirstV() {
        return rightside[0];
    }

    public List<String> getUsedIdentifiers() {
        List<String> result = new LinkedList<>();
        result.add(groupname);

        Collections.addAll(result, labels);

        for (SyntaxElement v : rightside) {
            String refG = v.getReferencedGroup();
            if (refG != null) {
                result.add(refG);
            }
        }

        return result;
    }

    public String[] getLabels() {
        return labels;
    }

    public CompilationElement[] getCompilation() {
        return compilation;
    }

    public String getGroupname() {
        return groupname;
    }

    public SyntaxElement[] getRightside() {
        return rightside;
    }

    public void setRightside(SyntaxElement[] rightside) {
        this.rightside = rightside;
    }

    public void setCompilation(CompilationElement[] compilation) {
        this.compilation = compilation;
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public void renameLabel(String original, String n) {

        int k = getIndexOfLabel(original);
        labels[k] = n;

        for (CompilationElement aCompilation : compilation) {
            CompilationElementType outerType = aCompilation.getType();
            if (outerType == CompilationElementType.SOURCE_REFERENCE || outerType == CompilationElementType.GROUP_REFERENCE) {
                aCompilation.setBase(n);
            }
            for (CompilationElement p : aCompilation.getParams()) {
                if (p.getType() == CompilationElementType.SOURCE_REFERENCE || p.getType() == CompilationElementType.GROUP_REFERENCE) {
                    p.setBase(n);
                }
            }
        }
    }

    public String[] extractRefGroups() {

        String[] result = new String[rightside.length];
        SyntaxElement[] rs = rightside;
        for (int i = 0; i < rs.length; i++) {
            result[i] = rs[i].getReferencedGroup();
        }
        return result;
    }

    public boolean containsRefOnRightSide(String groupRef) {
        SyntaxElement[] rs = rightside;
        for (SyntaxElement r : rs) {
            if (groupRef.equals(r.getReferencedGroup())) {
                return true;
            }
        }
        return false;
    }

    public String[] extractRefGroupsBefore(String groupRef) {
        for (int c = 0; c < rightside.length; c++) {
            if (groupRef.equals(rightside[c].getReferencedGroup())) {
                String[] result = new String[c];
                for (int i = 0; i < c; i++) {
                    result[i] = rightside[i].getReferencedGroup();
                }
                return result;
            }
        }
        throw new RuntimeException("Error in code " + this + " doesn't contain " + groupRef);
    }

    public String[] extractRefGroupsAfter(String groupRef) {
        for (int c = 0; c < rightside.length; c++) {
            if (groupRef.equals(rightside[c].getReferencedGroup())) {
                String[] result = new String[(rightside.length - c) - 1];
                for (int i = 0; i < result.length; i++) {
                    c++;
                    result[i] = rightside[c].getReferencedGroup();
                }
                return result;
            }
        }
        throw new RuntimeException("Error in code " + this + " doesn't contain " + groupRef);
    }

    public String toStringWoCompilation() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.groupname + "->");
        addRightsideString(sb);
        return sb.toString();
    }

    public int getIndexOfLabel(String base) {
        for (int i = 0; i < labels.length; i++) {
            if (base.equals(labels[i])) {
                return i;
            }
        }
        throw new RuntimeException("Invalid label: " + base + " " + this.toString());
    }

    public boolean isFreezer() {
        return !Character.isLowerCase(groupname.charAt(0));
    }

    public String getLastRefGroup() {
        return rightside[rightside.length - 1].getReferencedGroup();
    }

    public int getIndexOfRefGroup(String midGroup) {
        for (int i = 0; i < rightside.length; i++) {
            if (midGroup.equals(rightside[i].getReferencedGroup())) {
                return i;
            }
        }
        return -1;
    }

    public int getIndexOfRefGroup(String midGroup, int after) {
        for (int i = after + 1; i < rightside.length; i++) {
            if (midGroup.equals(rightside[i].getReferencedGroup())) {
                return i;
            }
        }
        return -1;
    }

    public String[] getGroupRefsAsArray() {
        if (rightSideAsString == null) {
            rightSideAsString = new String[rightside.length];
            int i = 0;
            for (SyntaxElement v : rightside) {
                rightSideAsString[i] = v.getReferencedGroup();
                i++;
            }
        }
        return rightSideAsString;
    }

    public String getRightSideRef(int index) {
        if (index < 0 || index >= rightside.length) {
            return null;
        }
        return getGroupRefsAsArray()[index];
    }

    public int getRightSideLength() {
        return rightside.length;
    }

    public void reset(SyntaxElement[] rightside, String[] labels, CompilationElement[] compilation) {
        if (rightside.length != labels.length) {
            throw new RuntimeException("Different arrayLengths");
        }
        this.rightside = rightside;
        this.labels = labels;
        this.compilation = compilation;
        this.rightSideAsString = null;
        this.setDefaultLabels();
    }

    private void setDefaultLabels() {
        for (int i = 0; i < labels.length; i++) {
            boolean condition = (labels[i] == null || labels[i].isEmpty()) && !this.rightside[i].isDescriptor();
            if (condition) {
                labels[i] = rightside[i].getReferencedGroup();
            }
        }
    }

    public boolean hasLabel(String s) {
        for (String l : labels) {
            if (l.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDirectRecursive() {
        return directRecursive;
    }

    public List<String> getRightSideAsStrList() {
        return Arrays.asList(getGroupRefsAsArray());
    }

    public boolean isMidRecursive() {
        int midRecCount = countMidRecursionRefs();
        return midRecCount >= 1;
    }

    public boolean hasMultipleMidRecursions() {
        return countMidRecursionRefs() > 1;
    }

    public boolean isLeftRecursive() {
        return groupname.equals(rightside[0].getReferencedGroup());
    }

    public boolean isRightRecursive() {
        return groupname.equals(rightside[rightside.length - 1].getReferencedGroup());
    }

    private int countMidRecursionRefs() {
        int midRecCount = 0;
        String[] sa = getGroupRefsAsArray();
        for (int i = 1; i < sa.length - 1; i++) {
            if (groupname.equals(sa[i])) {
                midRecCount++;
            }
        }
        return midRecCount;
    }

    public boolean isRepeater() {
        return repeater;
    }

}
