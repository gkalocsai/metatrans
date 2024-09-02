package descriptor;

import java.util.Objects;

import syntax.SyntaxElement;

public class GroupName implements SyntaxElement {

    private String groupname;

    public GroupName(String name) {
        this.groupname = name;
    }

    @Override
    public boolean isDescriptor() {
        return false;
    }

    @Override
    public String getReferencedGroup() {
        return this.groupname;
    }

    @Override
    public int getDescribedLength() {

        throw new RuntimeException("Unknown");
    }

    @Override
    public CharSequenceDescriptor getCsd() {
        return null;
    }

    @Override
    public SyntaxElement copy() {
        return this;
    }

    @Override
    public String toString() {
        return groupname;
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupname);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupName other = (GroupName) obj;
        return Objects.equals(groupname, other.groupname);
    }

}
