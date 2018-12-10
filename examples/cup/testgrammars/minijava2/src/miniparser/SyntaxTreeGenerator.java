package miniparser;
import minijava.*;
import java.util.*;
import java.io.*;

public class SyntaxTreeGenerator extends MinijavaVisitor implements Constants{
    PrintWriter writer;
    public SyntaxTreeGenerator(String filename){
        try{
            writer = new PrintWriter(filename+".mp");
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    public void flush(){
        writer.flush();
        writer.close();
    }
    public boolean preVisit(Program p){
	writer.println("prologues:=3;");
	writer.println("verbatimtex");
	writer.println("%&latex");
	writer.println("\\documentclass{article}");
        writer.println("\\usepackage{graphics}");
        writer.println("\\usepackage{color}");
        writer.println("\\definecolor{dgreen}{rgb}{0.1,0.5,0.1}");
	writer.println("\\begin{document}");
	writer.println("etex");
	writer.println("input boxes;");
	writer.println("input trees;");
	writer.println("beginfig(1);");

	writer.println("interim levelsep:=80pt;");
	writer.println("interim treesep:=-2pt;");

	writer.println("tree.root(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}program}} etex)(");
	p.ld.get(0).accept(this);
	for (int i = 1; i< p.ld.size();i++) {
	    writer.print(",");
	    p.ld.get(i).accept(this);
	}
	writer.print(",");
	p.ls.get(0).accept(this);
	for (int i = 1; i< p.ls.size();i++) {
	    writer.print(",");
	    p.ls.get(i).accept(this);
	}
	writer.println(");");
	writer.println("drawtrees(root);");
	writer.println("endfig;");
	writer.println("end;");
	return false;
    }
    public boolean preVisit(Decl d){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}decl}} etex)(");
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}type}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} int} etex))\n");
        writer.println(",tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}name}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+d.varlist.get(0)+"} etex))");
	for (int i=1; i< d.varlist.size(); i++)
	    writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} ,} etex),tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}name}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+d.varlist.get(i)+"} etex))");
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} ;} etex))");
	return true;
    }
    public boolean preVisit(Stmt.Assign a){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(");
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}name}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+a.lhs+"} etex)),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} =} etex),");
	return true;
    }
    public void postVisit(Stmt.Assign a){
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} ;} etex))");
    }
    public boolean preVisit(Expr.Priority p){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}expr}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),");
	p.e.accept(this);
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex))");
	return false;
    }
    public boolean preVisit(Cond.Priority p){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}cond}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),");
	p.c.accept(this);
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex))");
	return false;
    }
    public boolean preVisit(Expr.Binex i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}expr}} etex)(");
	i.e1.accept(this);
	String operator=null;
	if (i.op==PLUS)  operator = "+";
	if (i.op==MINUS) operator = "-";
	if (i.op==MULT)  operator = "*";
	if (i.op==DIV)   operator = "/";
	if (i.op==MOD)   operator = "\\%";
	writer.println(",tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}binop}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+operator+"} etex)),");
	i.e2.accept(this);
	writer.println(")");
	return false;
    }
    public void visit(Expr.Identifier d){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}expr}} etex)(tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}name}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+d.i+"} etex)))");
    }
    public void visit(Expr.IntConst d){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}expr}} etex)(tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}number}} etex)");
	writer.println("(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+d.i+"} etex)))");
    }
    public boolean preVisit(Stmt.Compound c){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $\\{$} etex)");
	for (int i = 0; i< c.ls.size();i++) {
	    writer.print(",");
	    c.ls.get(i).accept(this);
	}
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $\\}$} etex))");
	return false;
    }
    public boolean preVisit(Stmt.IfThenElse i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(");
	writer.println("leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} if} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),");
	i.cond.accept(this);
	//writer.println("leaf(btex \\tt BED etex)");
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex),");
	i.then.accept(this);
	if (i.els!=null){
	    writer.println(",leaf(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}else}} etex),");
	    i.els.accept(this);
	}
	writer.println(")");
	return false;
    }
    public boolean preVisit(Stmt.Loop l){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(");
	writer.println("leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} while} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),");
	l.cond.accept(this);
	//writer.println("leaf(btex \\tt BED etex)");
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex),");
	l.body.accept(this);
	writer.println(")");
	return false;
    }
    public boolean preVisit(Stmt.Write i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(");
	writer.println("leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} write} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),");
	if (i.e !=null) i.e.accept(this);
	writer.println(",leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} ;} etex))");
	return false;
    }
    public void visit(Stmt.Read i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}stmt}} etex)(");
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}name}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+i.lhs+"} etex)),");
	writer.println("leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} =} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} read} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $($} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} $)$} etex),leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} ;} etex))");
    }
    public boolean preVisit(Cond.BinCond c){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}cond}} etex)(");
	c.e1.accept(this);
	String operator=null;
	if (c.op==LEQ)  operator = "<=";
	if (c.op==GTQ)  operator = ">=";
	if (c.op==GT)   operator = ">";
	if (c.op==LE)   operator = "<";
	if (c.op==NEQ)  operator = "!=";
	if (c.op==EQ)   operator = "==";
	writer.println(",tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}comp}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+operator+"} etex)),");
	c.e2.accept(this);
	writer.println(")");
	return false;
    }
    public boolean preVisit(Cond.BBinCond c){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}cond}} etex)(");
	c.c1.accept(this);
	String operator=null;
	if (c.op==Constants.AND)  operator = "\\&\\&";
	if (c.op==Constants.OR)  operator = "||";
	writer.println(",tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}bbinop}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} "+operator+"} etex)),");
	c.c2.accept(this);
	writer.println(")");
	return false;
    }
    public boolean preVisit(Expr.Unex i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}expr}} etex)(tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}unop}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} -} etex)),");
	i.e1.accept(this);
	writer.println(")");
	return false;
    }
    public boolean preVisit(Cond.BUnOp i){
	writer.println("tree(btex \\fcolorbox{blue}{white}{\\tt {\\color{dgreen}bunop}} etex)(leaf(btex \\fcolorbox{red}{white}{\\phantom{!g}\\tt\\hspace{-1.2em} !} etex),");
	i.c.accept(this);
	writer.println(")");
	return false;
    }

}
