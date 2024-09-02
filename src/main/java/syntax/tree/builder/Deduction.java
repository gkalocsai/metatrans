package syntax.tree.builder;

public class Deduction {

    private RuleInterval from;
    private RuleInterval[] to;

    public Deduction(RuleInterval from, RuleInterval[] to) {
        this.from = from;
        this.to = to;
    }

    public RuleInterval getFrom() {
        return from;
    }

    public RuleInterval[] getTo() {
        return to;
    }

}
