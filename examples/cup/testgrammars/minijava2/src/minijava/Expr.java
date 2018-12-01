package minijava;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class Expr implements Constants {
    public Expr(){
    }
    public static class Priority extends Expr {
	public Expr e;
	public Priority(Expr e){
	    this.e=e;
	}
	public String toString(){
	    return "("+e+")";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    e.accept(v);
	    v.postVisit(this);
	}

    }
    public static Priority priority(Expr e){
	return new Priority(e);
    }
    public static class Binex extends Expr {
	public Expr e1, e2;
	public int op;
	public Binex(Expr e1, int op, Expr e2){
	    this.e1=e1;
	    this.e2=e2;
	    this.op=op;
	}
	public String toString(){
	    String operator=null;
	    if (op==Constants.PLUS)  operator = "+";
	    if (op==Constants.MINUS) operator = "-";
	    if (op==Constants.MULT)  operator = "*";
	    if (op==Constants.DIV)   operator = "/";
	    if (op==Constants.MOD)   operator = "\\%";
	    return e1 + ""+operator + e2;
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    e1.accept(v);
	    e2.accept(v);
	    v.postVisit(this);
	}

    }
    public static Binex binop(Expr e1, int op, Expr e2){
	return new Binex(e1,op,e2);
    }
    public static class Unex extends Expr {
	public Expr e1;
	public Unex(Expr e1){
	    this.e1=e1;
	}
	public String toString(){
	    return "-"+e1;
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    e1.accept(v);
	    v.postVisit(this);
	}
    }
    public static Unex unop(Expr e){
	return new Unex(e);
    }
    public static class IntConst extends Expr {
	public int i;
	public IntConst(int i){
	    this.i=i;
	}
	public String toString(){
	    return i+"";
	}
	public void accept(MinijavaVisitor v){
	    v.visit(this);
	}
    }
    public static IntConst intconst(int i){
	return new IntConst(i);
    }
    public static class Identifier extends Expr {
	public Location left, right;
	public String i;
	public Identifier(Location l, String i,Location r){
	    this.i=i;
	    this.left=l;
	    this.right=r;
	}
	public String toString(){
	    return i;
	}
	public void accept(MinijavaVisitor v){
	    v.visit(this);
	}
    }
    public static Identifier ident(Location l, String s, Location r){
	return new Identifier(l,s,r);
    }
    public abstract void accept(MinijavaVisitor v);
}
