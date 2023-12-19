package compilation;

import java.util.ArrayList;
import java.util.Stack;

public class CompTreeTraverser {


	private Node root;

	public CompTreeTraverser(Node root) {
		this.root = root;
	}

	StringBuilder sb=new StringBuilder();

	public String buildResult() {
		Stack<Node>  branch = new Stack<>();
		branch.push(root);

		while(!branch.isEmpty()) {
			Node current=branch.pop();
			if(current.s!=null) sb.append(current.s);


			ArrayList<Node> children = current.childNodes;
			if(children != null) {
				for(int i=children.size()-1;i>=0; i--) {
					branch.push(children.get(i));

				}
			}
		}
		return sb.toString();
	}

}
