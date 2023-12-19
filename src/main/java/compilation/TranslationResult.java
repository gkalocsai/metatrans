package compilation;

public class TranslationResult {
		
	    private String result;
		private String error;
		
		public TranslationResult(String result, String error) {
			super();
			this.result = result;
			this.error = error;
		}

		public TranslationResult(Node root) {
			this.result=createResult(root);
		}

		private String createResult(Node root) {
			StringBuilder sb=new StringBuilder();
			if(root.s != null) {
				sb.append(root.s);
				return sb.toString();
			}else{
				for(Node n:root.childNodes){
				   sb.append(createResult(n)); 	
				}
			}
			return sb.toString();
		}

		public String getResult() {
			return result;
		}

		public String getError() {
			return error;
		}
		@Override
		public String toString() {
			
			if(error!= null && !error.isEmpty()) return error;
			else if(result!=null && result.isEmpty()) return "EMPTY";
		    return ""+result;	
		}
}
