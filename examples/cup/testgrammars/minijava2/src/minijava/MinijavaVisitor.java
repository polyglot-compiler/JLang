package minijava;
public class MinijavaVisitor {
    public boolean preVisit(Program p){
	return true;
    }
    public void postVisit(Program p){
    }
    public void visit(Decl d){
    }
    public boolean preVisit(Stmt.Assign i){
	return true;
    }
    public void postVisit(Stmt.Assign i){
    }
    public boolean preVisit(Stmt.Write i){
	return true;
    }
    public void postVisit(Stmt.Write i){
    }
    public void visit(Stmt.Read i){
    }
    public void visit(Stmt.Empty e){
    }
    public boolean preVisit(Stmt.Compound i){
	return true;
    }
    public void postVisit(Stmt.Compound i){
    }
    public boolean preVisit(Stmt.IfThenElse i){
	return true;
    }
    public void postVisit(Stmt.IfThenElse i){
    }
    public boolean preVisit(Stmt.Loop i){
	return true;
    }
    public void postVisit(Stmt.Loop i){
    }
    public boolean preVisit(Decl d){
	return true;
    }
    public void postVisit(Decl d){
    }
    public boolean preVisit(Cond.BUnOp i){
	return true;
    }
    public void postVisit(Cond.BUnOp i){
    }
    public boolean preVisit(Cond.BBinCond i){
	return true;
    }
    public void postVisit(Cond.BBinCond i){
    }
    public boolean preVisit(Cond.BinCond i){
	return true;
    }
    public void postVisit(Cond.BinCond i){
    }
    public void visit(Cond.BoolConst d){
    }
    public void visit(Expr.Identifier d){
    }
    public void visit(Expr.IntConst d){
    }
    public boolean preVisit(Expr.Priority i){
	return true;
    }
    public void postVisit(Expr.Priority i){
    }
    public boolean preVisit(Cond.Priority i){
	return true;
    }
    public void postVisit(Cond.Priority i){
    }
    public boolean preVisit(Expr.Binex i){
	return true;
    }
    public void postVisit(Expr.Binex i){
    }
    public boolean preVisit(Expr.Unex i){
	return true;
    }
    public void postVisit(Expr.Unex i){
    }
    
}
