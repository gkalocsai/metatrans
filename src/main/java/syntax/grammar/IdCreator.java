package syntax.grammar;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IdCreator {


	private Grammarhost grammarhost;

	public IdCreator(Grammarhost grammarhost) {
		this.grammarhost = grammarhost;
		
	}
	
	

	
	public List<String> createNewNames(int nameCount, String prefixOfGenerated,Set<String> alreadyExistingIds){
		List<String> newNames = new LinkedList<>();
		Set<String> checker=new HashSet<>();
		for(int i=0;i<nameCount;i++){
			String candidate=generateYetUnusedId(alreadyExistingIds,prefixOfGenerated);
			while(checker.contains(candidate)){
				candidate=generateYetUnusedId(alreadyExistingIds, prefixOfGenerated);
			}
			newNames.add(candidate);
			checker.add(candidate);
		}
		return newNames;
	}

	public  String generateYetUnusedId(Set<String> allIdentifiers, String prefixOfGenerated){
		
		String id=generateRandomId();
		while(allIdentifiers.contains(id)){
			id=generateRandomId();
		}
		for(int i = 1; i <=id.length();i++){
			String s = id.substring(0,i); 
			if(!allIdentifiers.contains(prefixOfGenerated+s)) {
			    this.grammarhost.allIds.add(prefixOfGenerated+s);
				return prefixOfGenerated+s;
			}
		}
		this.grammarhost.allIds.add(prefixOfGenerated+id);
		return prefixOfGenerated+id;
	}

	private static String generateRandomId() {
		
		return UUID.randomUUID().toString();

	}

}
