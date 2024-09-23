package syntax.grammar;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public enum IdCreator {
    INSTANCE;

    private Set<String> theIds = new HashSet<String>();

    void addExistingIds(Collection<String> ids) {
        theIds.addAll(ids);
    }

    public String generateYetUnusedId(String prefixOfGenerated) {

        String id = generateRandomId();
        while (theIds.contains(id))
            id = generateRandomId();
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

        return UUID.randomUUID().toString().toLowerCase();

    }

}
