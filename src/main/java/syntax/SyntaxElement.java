package syntax;

import descriptor.CharSequenceDescriptor;

public interface SyntaxElement {

    public boolean isDescriptor();

    public String getReferencedGroup();

    public int getDescribedLength();

    @Override
    public boolean equals(Object other);

    @Override
    public String toString();

    public CharSequenceDescriptor getCsd();

    public SyntaxElement copy();

}
