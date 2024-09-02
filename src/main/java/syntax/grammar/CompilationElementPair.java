package syntax.grammar;

import compilation.CompilationElement;

class CompilationElementPair {
    CompilationElement dest;
    CompilationElement[] source;

    public CompilationElementPair(CompilationElement dest, CompilationElement[] source) {
        this.dest = dest;
        this.source = source;
    }
}