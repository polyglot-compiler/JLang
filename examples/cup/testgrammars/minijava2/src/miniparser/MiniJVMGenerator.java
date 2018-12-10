package miniparser;
import minijava.*;
import java.util.*;
import java.io.*;

public class MiniJVMGenerator extends MinijavaVisitor implements Constants{
    PrintWriter writer;
    public MiniJVMGenerator(String filename){
        try{
            writer = new PrintWriter(filename+".jvm");
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    public void flush(){
        writer.flush();
        writer.close();
    }
    int declcounter = 0;
    int loopcounter = 0;
    int ifcounter = 0;
    Map<String,Integer> symtab = new HashMap<String,Integer>();
    public void postVisit(Program p){
	writer.println("HALT");
    }
    public boolean preVisit(Stmt.Assign i){
	writer.print("// "+i);
	return true;
    }
    public void postVisit(Stmt.Assign i){
	writer.println("STORE "+symtab.get(i.lhs));
    }
    public boolean preVisit(Stmt.Write i){
	writer.print("// "+i);
	return true;
    }
    public void postVisit(Stmt.Write i){
	writer.println("WRITE");
    }
    public void visit(Stmt.Read i){
	writer.print("// "+i);
	writer.println("READ");
	writer.println("STORE "+symtab.get(i.lhs));
    }
    public boolean preVisit(Stmt.IfThenElse i){
	int e = ifcounter++;
	int end = ifcounter++;
	writer.println("// "+i.cond);
	i.cond.accept(this);
	writer.println("FJUMP IF"+e);
	i.then.accept(this);
	if (i.els!=null)
	    writer.println("JUMP IF"+end);
	writer.print("IF"+e+":");
	if(i.els!=null) {
	    i.els.accept(this);
	    writer.println("IF"+end+":");
	}
	return false;
    }
    public boolean preVisit(Stmt.Loop i){
	int loop = loopcounter++;
	int loopexit = loopcounter++;
	writer.println("//"+ i.cond+" ??" );
	writer.print("L"+ loop +":" );
	i.cond.accept(this);
	writer.println("FJUMP L"+loopexit);
	i.body.accept(this);
	writer.println("JUMP L"+loop);
	writer.print("L"+loopexit+":");
	return false;
    }
    public boolean preVisit(Decl d){
	writer.print("//"+d);
	return true;
    }
    public void postVisit(Decl d){
	for (Expr.Identifier s :d.varlist) symtab.put(s.i,declcounter++);
	writer.println("ALLOC "+d.varlist.size());
    }
    public void postVisit(Cond.BUnOp i){
	writer.println("NOT");
    }
    public void postVisit(Cond.BinCond i){
	if (i.op==LEQ) writer.println("LEQ"); 
	if (i.op==LE) writer.println("LESS"); 
	if (i.op==GTQ) writer.println("GTQ"); 
	if (i.op==GT) writer.println("GT"); 
	if (i.op==EQ) writer.println("EQ"); 
	if (i.op==NEQ) writer.println("NEQ"); 
    }
    public void postVisit(Cond.BBinCond i){
	if (i.op==AND) writer.println("AND");
	if (i.op==OR) writer.println("OR");
    }
    public void visit(Cond.BoolConst d){
	writer.println("???");
    }
    public void visit(Expr.Identifier d){
	writer.println("LOAD "+symtab.get(d.i));
    }
    public void visit(Expr.IntConst d){
	writer.println("CONST "+d.i);
    }
    public void postVisit(Expr.Binex i){
	if (i.op==PLUS) writer.println("ADD");
	if (i.op==MINUS) writer.println("SUB");
	if (i.op==MULT) writer.println("MUL");
	if (i.op==DIV) writer.println("DIV");
	if (i.op==MOD) writer.println("MOD");
    }
    public void postVisit(Expr.Unex i){
	writer.println("NEG");
    }
}
