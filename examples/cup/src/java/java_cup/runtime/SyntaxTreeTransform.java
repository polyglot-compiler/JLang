package java_cup.runtime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class SyntaxTreeTransform {
	private static final XMLElement[] X = new XMLElement[0];
	private static class ListFlattener extends SyntaxTreeDFS.AbstractVisitor{
		private List<String> name;
		public ListFlattener(String ... name) {
			super();
			this.name=Arrays.asList(name);
		}
		public XMLElement root(){
			System.out.println(stack.size()+" - "+intstack.size());
			return stack.pop();
		}
		private Stack<XMLElement> stack = new Stack<>();
		private Stack<Integer> intstack = new Stack<>();
		@Override
		public void defaultPre(XMLElement element, List<XMLElement> children) {			
			intstack.push(children.size());
		}
		@Override
		public void defaultPost(XMLElement element, List<XMLElement> children) {
			int n = intstack.pop();
			if (n>0){
				XMLElement el=null;
				outer: do {
					if (name.contains(element.getTagname())) for (XMLElement e:children)
						if (e.getTagname().equals(element.getTagname())){
							el = e;
							break outer;
						}
					LinkedList<XMLElement> elems = new LinkedList<>();
					while (n-->0) elems.addFirst(stack.pop());
					stack.push(new XMLElement.NonTerminal(element.getTagname(), 0, elems.toArray(X)));
					System.out.println("  doch noch: ");
					return;
				}while (false);
				LinkedList<XMLElement> elems = new LinkedList<>();
				while (n-->0) {
					elems.addFirst(stack.pop());
//					XMLElement x = stack.pop();
//					if (x!=el) elems.addFirst(x);
				}
				stack.addAll(elems);
				intstack.push(intstack.pop()+n-1);				
			}
			if (n==0) { 
				stack.push(element);
			}
		}
		
	}
	public static XMLElement flattenLists(XMLElement elem,String ...names) {
		ListFlattener cr = new ListFlattener(names);
		SyntaxTreeDFS.dfs(elem, cr);
		return cr.root();
	}
	
	private static class ChainRemover extends SyntaxTreeDFS.AbstractVisitor{
		public XMLElement root(){
			return stack.pop();
		}
		private Stack<XMLElement> stack = new Stack<XMLElement>();
		@Override
		public void defaultPost(XMLElement arg0, List<XMLElement> arg1) {
			int n = arg1.size();
			if (n>1){
				LinkedList<XMLElement> elems = new LinkedList<>();
				while (n-->0) elems.addFirst(stack.pop());
        XMLElement.NonTerminal non = (XMLElement.NonTerminal)arg0;
				XMLElement ne = new XMLElement.NonTerminal(arg0.getTagname(),non.getVariant(),elems.toArray(X));
				stack.push(ne);
				return;
			}
			//if (n==1){}
			if (n==0) stack.push(arg0);
		}

		@Override
		public void defaultPre(XMLElement arg0, List<XMLElement> arg1) {}
		
	}
	public static XMLElement removeUnaryChains(XMLElement elem) {
		ChainRemover cr = new ChainRemover();
		SyntaxTreeDFS.dfs(elem, cr);
		return cr.root();
	}

}
