package polyllvm.extension;

import java.util.List;

import polyglot.ast.Expr;
import polyglot.ast.New;
import polyglot.ast.Node;
import polyglot.types.ConstructorInstance;
import polyglot.util.SerialVersionUID;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.PseudoLLVMTranslator;

public class PolyLLVMNewExt extends PolyLLVMExt {
    private static final long serialVersionUID = SerialVersionUID.generate();

    @Override
    public Node translatePseudoLLVM(PseudoLLVMTranslator v) {
        New n = (New) node();
        ConstructorInstance ci = n.constructorInstance();
        List<Expr> args = n.arguments();

        //Allocate space for the new object - need to get the size of the object

        //Call the constructor function

        return super.translatePseudoLLVM(v);
    }

}
