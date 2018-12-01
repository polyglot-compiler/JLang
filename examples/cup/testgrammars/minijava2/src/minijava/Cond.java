package minijava;
public abstract class Cond {
    public String toTexString(){
        return toString();
    }
    public Cond(){
    }
    public static class Priority extends Cond {
	public Cond c;
	public Priority(Cond c){
	    this.c=c;
	}
	public String toString(){
	    return "("+c+")";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    c.accept(v);
	    v.postVisit(this);
	}

    }
    public static Cond priority(Cond c){
	return new Priority(c);
    }
    public static class BBinCond extends Cond {
	public Cond c1, c2;
	public int op;
	public BBinCond(Cond c1, int op, Cond c2){
	    this.c1=c1;
	    this.c2=c2;
	    this.op=op;
	}
	public String toString(){
	    String operator=null;
	    if (op==Constants.AND)  operator = "&&";
	    if (op==Constants.OR)  operator = "||";
	    return c1 + ""+operator + c2;
	}
	public String toTexString(){
	    String operator=null;
	    if (op==Constants.AND)  operator = "\\&\\&";
	    if (op==Constants.OR)  operator = "||";
	    return c1 + ""+operator + c2;
	}

	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this))return;
	    c1.accept(v);
	    c2.accept(v);
	    v.postVisit(this);
	}

    }
    public static Cond binop(Cond c1, int op, Cond c2){
	return new BBinCond(c1,op,c2);
    }
    public static class BinCond extends Cond {
	public Expr e1, e2;
	public int op;
	public BinCond(Expr e1, int op, Expr e2){
	    this.e1=e1;
	    this.e2=e2;
	    this.op=op;
	}
	public String toString(){
	    String operator=null;
	    if (op==Constants.LEQ)  operator = "<=";
	    if (op==Constants.GTQ)  operator = ">=";
	    if (op==Constants.GT)  operator = ">";
	    if (op==Constants.LE)  operator = "<";
	    if (op==Constants.NEQ) operator = "!=";
	    if (op==Constants.EQ) operator = "==";
	    return e1 + ""+operator + e2;
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this))return;
	    e1.accept(v);
	    e2.accept(v);
	    v.postVisit(this);
	}
    }

    public static Cond binop(Expr e1, int op, Expr e2){
	return new BinCond(e1,op,e2);
    }
    public static class BUnOp extends Cond {
	public Cond c;
	public BUnOp(Cond c){
	    this.c=c;
	}
	public String toString(){
	    return "!"+c;
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this))return;
	    c.accept(v);
	    v.postVisit(this);
	}

    }
    public static Cond unop(Cond c){
	return new BUnOp(c);
    }
    public static class BoolConst extends Cond {
	public boolean b;
	public BoolConst(boolean b){
	    this.b=b;
	}
	public String toString(){
	    if (b) return "true";
	    else return "false";
	}
	public void accept(MinijavaVisitor v){
	    v.visit(this);
	}
    }
    public static Cond boolconst(boolean b){
	return new BoolConst(b);
    }
    public abstract void accept(MinijavaVisitor v);
}
