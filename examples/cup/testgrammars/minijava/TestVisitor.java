import java_cup.runtime.SyntaxTreeDFS.AbstractVisitor;
import java_cup.runtime.SyntaxTreeDFS;
import java_cup.runtime.XMLElement;
import java.util.List;

public class TestVisitor extends AbstractVisitor {
    public TestVisitor(){
	registerPreVisit("stmt",this::preVisitStatement);
    }
    public void preVisitStatement(XMLElement element, List<XMLElement> children){
	System.out.println("Found a statement");
	SyntaxTreeDFS.dfs(element,new TerminalFilter());
    }
    public void defaultPre(XMLElement elem, List<XMLElement> children) {}
    public void defaultPost(XMLElement elem, List<XMLElement> children) {}
}

class TerminalFilter extends AbstractVisitor {
    public void defaultPre(XMLElement elem, List<XMLElement> children) {
	if (elem instanceof XMLElement.Terminal)
	    System.out.println(""+elem);
    }
    public void defaultPost(XMLElement elem, List<XMLElement> children) {}
}
