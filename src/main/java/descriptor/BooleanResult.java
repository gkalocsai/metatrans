package descriptor;

public class BooleanResult {

	private boolean result;
	private String message;
	public BooleanResult(boolean result, String message) {
		this.result = result;
		this.message = message;
	}
	public boolean getBooleanResult() {
		return result;
	}
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString(){
	    StringBuilder sb= new StringBuilder();
	    if(result == false){
	      sb.append("Error: ");
	    }
	    sb.append(message);
	    return sb.toString();
	}
	
}
