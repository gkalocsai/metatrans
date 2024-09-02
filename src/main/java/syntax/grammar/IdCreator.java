package syntax.grammar;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public enum IdCreator {
    InSTANCE;

    private Set<String> theIds = new HashSet<String>();

    void addExistingIds(Collection<String> ids) {
        theIds.addAll(ids);
    }

    public List<String> createNewNames(int nameCount, String prefixOfGenerated, Set<String> alreadyExistingIds) {
        List<String> newNames = new LinkedList<>();
        Set<String> checker = new HashSet<>();
        for (int i = 0; i < nameCount; i++) {
            String candidate = generateYetUnusedId(prefixOfGenerated);
            while (checker.contains(candidate)) {
                candidate = generateYetUnusedId(prefixOfGenerated);
            }
            newNames.add(candidate);
            checker.add(candidate);
        }
        return newNames;
    }

    public String generateYetUnusedId(String prefixOfGenerated) {

        String id = generateRandomId();
        while (theIds.contains(id)) {
            id = generateRandomId();
        }
        for (int i = 1; i <= id.length(); i++) {
            String s = id.substring(0, i);
            if (!theIds.contains(prefixOfGenerated + s)) {
                this.theIds.add(prefixOfGenerated + s);
                return prefixOfGenerated + s;
            }
        }
        this.theIds.add(prefixOfGenerated + id);
        return prefixOfGenerated + id;
    }

    private static String generateRandomId() {

        return UUID.randomUUID().toString();

    }

}
