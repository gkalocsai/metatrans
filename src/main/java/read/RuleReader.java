package read;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import syntax.Rule;
import util.CharSeqUtil;

public class RuleReader {

    private String preprocessed;

    public RuleReader(String init) {
        this.preprocessed = CharSeqUtil.removeMultilineComments(init);
        this.preprocessed = convertNeighbourWhitespacesToSingleSpace();

    }

    private List<String> createGroups() {
        List<String> groups = new LinkedList<>();
        for (int i = 0; i < preprocessed.length(); i++) {
            char c = preprocessed.charAt(i);
            if (Character.isWhitespace(c)) continue;
            int end = getEndIndexOfTheGroup(i);
            groups.add(preprocessed.substring(i, end + 1));
            i = end;
        }
        return groups;
    }

    private int getEndIndexOfTheGroup(int i) {

        for (; i < preprocessed.length(); i++) {
            char c = preprocessed.charAt(i);
            if (c == '{') return CharSeqUtil.getClosingIndexInEscaped(preprocessed, i);
        }

        return -1;
    }

    public List<Rule> getAllRules() {
        List<String> groupStrings = createGroups();
        List<Rule> result = new LinkedList<>();
        for (String gs : groupStrings) {
            result.addAll(getRulesOfSingleGroup(gs));
        }
        return result;
    }

    private Collection<? extends Rule> getRulesOfSingleGroup(String gs) {
        List<Rule> result = new LinkedList<>();
        String groupname = gs.substring(0, gs.indexOf('{')).trim();
        String inner = gs.substring(gs.indexOf('{') + 1, gs.lastIndexOf('}'));
        List<String> rulesWoGroupname = createRuleStrings(inner);

        for (String rsAndComp : rulesWoGroupname) {
            result.addAll(ComplexRuleCreator.createRules(groupname, rsAndComp));
        }
        return result;
    }

    private List<String> createRuleStrings(String inner) {
        List<String> result = new LinkedList<>();
        for (int i = 0; i < inner.length(); i++) {
            int begin = i;
            int end = CharSeqUtil.getNonQuotedIndex(inner, ";", begin);
            if (end < 0) {
                String mayLast = inner.substring(begin).trim();
                if (!mayLast.isEmpty()) result.add(mayLast);
                return result;
            } else {
                String r = inner.substring(begin, end).trim();
                if (!r.isEmpty()) result.add(r);
            }
            i = end;
        }
        return result;
    }

    private String convertNeighbourWhitespacesToSingleSpace() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < preprocessed.length()) {
            char c = preprocessed.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
                i++;
                continue;
            }
            sb.append(' ');
            while (Character.isWhitespace(c)) {
                i++;
                if (i >= preprocessed.length()) break;
                c = preprocessed.charAt(i);
            }
        }
        return sb.toString();

    }

    public String getPreprocessed() {
        return preprocessed;
    }

}
