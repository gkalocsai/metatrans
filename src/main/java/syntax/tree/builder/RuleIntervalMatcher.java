package syntax.tree.builder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import syntax.tree.tools.RuleInterval;

public class RuleIntervalMatcher {

    private Map<String, List<RuleInterval>> forward;
    private Map<String, List<RuleInterval>> backward;
    private RuleInterval[] currentRight;
    private String[] afterGroupNames;
    private String[] beforeGroupNames;
    private RuleInterval[] currentLeft;

    public RuleIntervalMatcher(Map<String, List<RuleInterval>> forward, Map<String, List<RuleInterval>> backward) {
        this.forward = forward;
        this.backward = backward;
    }

    public void reInit(String[] patternRightSide, int groupIndexInPattern) {
        beforeGroupNames = rangeCopyToExclusive(patternRightSide, groupIndexInPattern);
        currentLeft = new RuleInterval[beforeGroupNames.length];
        afterGroupNames = rangeCopyTillEndExclusive(patternRightSide, groupIndexInPattern);
        currentRight = new RuleInterval[afterGroupNames.length];
    }

    List<RuleInterval[]> getRigths(int firstSourceIndex) {

        List<RuleInterval[]> rights = new LinkedList<RuleInterval[]>();

        if (!stepRight(afterGroupNames, 0, firstSourceIndex)) return null;
        rights.add(copy(currentRight));
        if (afterGroupNames.length == 0) return rights;

        int groupNameIndex = afterGroupNames.length - 1;

        for (;;) {
            int sourceIndex = setSourceIndexRightside(firstSourceIndex, groupNameIndex);
            while (stepRight(afterGroupNames, groupNameIndex, sourceIndex)) {
                rights.add(copy(currentRight));
            }

            groupNameIndex--;
            if (groupNameIndex < 0) return rights;
            sourceIndex = setSourceIndexRightside(firstSourceIndex, groupNameIndex);
            while (!stepRight(afterGroupNames, groupNameIndex, sourceIndex)) {
                groupNameIndex--;
                if (groupNameIndex < 0) return rights;
            }
            rights.add(copy(currentRight));
            groupNameIndex = afterGroupNames.length - 1;

        }
    }

    List<RuleInterval[]> getLefts(int lastSourceIndex) {

        List<RuleInterval[]> lefts = new LinkedList<RuleInterval[]>();

        if (!stepLeft(beforeGroupNames, beforeGroupNames.length - 1, lastSourceIndex)) return null;
        lefts.add(copy(currentLeft));
        if (beforeGroupNames.length == 0) return lefts;

        int groupNameIndex = 0;

        for (;;) {
            int sourceIndex = setSourceIndexLeftside(lastSourceIndex, groupNameIndex);
            while (stepLeft(beforeGroupNames, groupNameIndex, sourceIndex)) {
                lefts.add(copy(currentLeft));
            }

            groupNameIndex++;
            if (groupNameIndex >= beforeGroupNames.length) return lefts;
            sourceIndex = setSourceIndexLeftside(lastSourceIndex, groupNameIndex);
            while (!stepLeft(beforeGroupNames, groupNameIndex, sourceIndex)) {
                groupNameIndex++;
                if (groupNameIndex >= beforeGroupNames.length) return lefts;
            }
            lefts.add(copy(currentLeft));
            groupNameIndex = beforeGroupNames.length - 1;
        }
    }

    private boolean stepRight(String[] groupNames, int fromGroupNameIndex, int sourceIndex) {

        if (fromGroupNameIndex >= groupNames.length) return true;
        RuleInterval searchAfterThis = currentRight[fromGroupNameIndex];
        String groupName = groupNames[fromGroupNameIndex];
        List<RuleInterval> l = forward.get("" + sourceIndex);

        if (l == null) return false;
        boolean found = false;

        if (searchAfterThis == null) found = true;

        for (RuleInterval ri : l) {

            if (found) if (ri.getRule().getGroupname().equals(groupName)) {
                currentRight[fromGroupNameIndex] = ri;
                for (int i = fromGroupNameIndex + 1; i < currentRight.length; i++) {
                    currentRight[i] = null;
                }
                boolean stepped = stepRight(groupNames, fromGroupNameIndex + 1, ri.getLast() + 1);
                if (stepped) return true;
            }
            if (ri == searchAfterThis) {
                found = true;
            }
        }
        for (int i = fromGroupNameIndex; i < currentRight.length; i++) {
            currentRight[i] = null;
        }
        return false;
    }

    private boolean stepLeft(String[] groupNames, int fromGroupNameIndex, int sourceIndex) {

        if (fromGroupNameIndex < 0) return true;
        RuleInterval searchAfterThis = currentLeft[fromGroupNameIndex];
        String groupName = groupNames[fromGroupNameIndex];
        List<RuleInterval> l = backward.get("" + sourceIndex);

        if (l == null) return false;
        boolean found = false;

        if (searchAfterThis == null) found = true;

        for (RuleInterval ri : l) {

            if (found) if (ri.getRule().getGroupname().equals(groupName)) {
                currentLeft[fromGroupNameIndex] = ri;
                for (int i = fromGroupNameIndex - 1; i >= 0; i--) {
                    currentLeft[i] = null;
                }
                boolean stepped = stepLeft(groupNames, fromGroupNameIndex - 1, ri.getBegin() - 1);
                if (stepped) return true;
            }
            if (ri == searchAfterThis) {
                found = true;
            }
        }
        for (int i = fromGroupNameIndex; i >= 0; i--) {
            currentLeft[i] = null;
        }
        return false;
    }

    private int setSourceIndexRightside(int firstSourceIndex, int groupNameIndex) {
        int sourceIndex = firstSourceIndex;
        if (groupNameIndex > 0) {
            sourceIndex = currentRight[groupNameIndex - 1].getLast() + 1;
        }
        return sourceIndex;
    }

    private int setSourceIndexLeftside(int lastSourceIndex, int groupNameIndex) {
        int sourceIndex = lastSourceIndex; // part.getBegin()-1;
        if (groupNameIndex < currentLeft.length - 1) {
            sourceIndex = currentLeft[groupNameIndex - 1].getBegin() - 1;
        }
        return sourceIndex;
    }

    private String[] rangeCopyTillEndExclusive(String[] array, int index) {
        return Arrays.copyOfRange(array, index + 1, array.length);
    }

    private String[] rangeCopyToExclusive(String[] array, int index) {
        return Arrays.copyOfRange(array, 0, index);
    }

    private RuleInterval[] copy(RuleInterval[] current) {
        return Arrays.copyOf(current, current.length);
    }
}
