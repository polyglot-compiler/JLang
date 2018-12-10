package minijava;
import java.util.List;
import java_cup.runtime.ComplexSymbolFactory.Location;
public abstract class Stmt {
    public static class Loop extends Stmt {
	public Stmt body;
	public Cond cond;
	public Loop(Cond c, Stmt t){
	    cond = c;
	    body= t;
	}
	public String toString(){
	    return "while ("+cond+")"+ body+"\n";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    cond.accept(v);
	    body.accept(v);
	    v.postVisit(this);
	}
    }
    public static Stmt whileloop(Cond c, Stmt s){
	return new Loop(c,s);
    }
    public static class IfThenElse extends Stmt {
	public Stmt then;
	public Stmt els;
	public Cond cond;
	public IfThenElse(Cond c, Stmt t, Stmt e){
	    cond = c;
	    then = t;
	    els  = e;
	}
	public String toString(){
	    return "if("+cond+")"+ then + ((els==null)?"":("else "+els))+"\n";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    cond.accept(v);
	    then.accept(v);
	    if (els!=null) els.accept(v);
	    v.postVisit(this);
	}
    }
    public static Stmt ifthenelse(Cond c, Stmt t, Stmt e){
	return new IfThenElse(c,t,e);
    }
    public static Stmt ifthen(Cond c, Stmt t){
	return new IfThenElse(c,t,null);
    }
    public static class Write extends Stmt {
	public Expr e;
        public String s;
	public Write(Expr e){
	    this.e = e;
	}
        public Write(String s){
            this.s=s;
        }
	public String toString(){
            if (e==null) return "write(\""+s+"\");\n";
	    return "write("+e+");\n";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    if (e!=null) e.accept(v);
	    v.postVisit(this);
	}
    }
    public static Stmt write(Expr e){
	return new Write(e);
    }
    public static Stmt write(String s){
	return new Write(s);
    }
    public static class Read extends Stmt {
	public String lhs;
        public String message;
	public Location left,right;
	public Read(Location l,String s,Location r){
	    lhs = s;
	    left=l;
	    right=r;
	}
        public Read(Location l, String ident, String mess,Location r){
            lhs=ident;
            message=mess;
	    left=l;
	    right=r;
        }
	public String toString(){
	    if (message==null) return lhs+"=read();\n";
	    return lhs+"=read();\n";
	}
	public void accept(MinijavaVisitor v){
	    v.visit(this);
	}
    }
    public static Stmt read(Location l,String s,Location r){
	return new Read(l,s,r);
    }
    public static Stmt read(Location l,String ident,String s,Location r){
	return new Read(l,ident,s,r);
    }
    public static class Assign extends Stmt {
	public String lhs;
	public Expr rhs;
	public Location left, right;
	public Assign(Location l,String s, Expr e,Location r){
	    lhs = s;
	    rhs = e;
	    left =l;
	    right=r;
	}
	public String toString(){
	    return lhs+"="+rhs+";\n";
	}
	public void accept(MinijavaVisitor v){
	    v.preVisit(this);
	    rhs.accept(v);
	    v.postVisit(this);
	}
    }
    public static Stmt assign(Location l,String i, Expr e,Location r){
	return new Assign(l,i,e,r);
    }
    public static class Compound extends Stmt {
	public List<Stmt> ls;
	public Compound(List<Stmt> l){
	    ls = l;
	}
	public String toString(){
	    String ret= "{\n";
	    for (Stmt s : ls) ret += s;
	    return ret+"}";
	}
	public void accept(MinijavaVisitor v){
	    if (!v.preVisit(this)) return;
	    for (Stmt s: ls) s.accept(v);
	    v.postVisit(this);
	}
    }
    public static Stmt compound(List<Stmt> l){
	return new Compound(l);
    }
    public static class Empty extends Stmt {
	public Empty(){
	}
	public void accept(MinijavaVisitor v){
	    v.visit(this);
	}
    }
    public static Stmt empty(){
	return new Empty();
    }
    public abstract void accept(MinijavaVisitor v);

}
