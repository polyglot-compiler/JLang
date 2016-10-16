package polyllvm.visit;

import polyglot.ast.Lang;
import polyglot.ast.Node;
import polyglot.frontend.Job;
import polyglot.frontend.TargetFactory;
import polyglot.util.CodeWriter;
import polyglot.util.ErrorInfo;
import polyglot.util.InternalCompilerError;
import polyglot.visit.PrettyPrinter;
import polyllvm.ast.PolyLLVMLang;
import polyllvm.ast.PseudoLLVM.LLVMSourceFile;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.Collection;

public class LLVMPrinter extends PrettyPrinter {

    private TargetFactory tf;
    private Job job;

    public LLVMPrinter(Job job, Lang lang, TargetFactory tf) {
        super(lang);
        if (!(lang instanceof PolyLLVMLang)) {
            throw new InternalCompilerError("LLVMPrinter only supports PolyLLVMLang");
        }
        this.tf = tf;
        this.job = job;
    }

    @Override
    public PolyLLVMLang lang() {
        return (PolyLLVMLang) super.lang();
    }

    public boolean translate(Node ast) {
        if (!(ast instanceof LLVMSourceFile)) {
            throw new InternalCompilerError("Only LLVMSourceFile represents"
                    + " entire LLVM ast");
        }
        LLVMSourceFile llvmAst = (LLVMSourceFile) ast;

        int outputWidth = job.compiler().outputWidth();
        Collection<JavaFileObject> outputFiles = job.compiler().outputFiles();
        JavaFileObject of;

//        String name;
//        name = source.name();
//        name = name.substring(0, name.lastIndexOf('.'));
//        int lastIndex = name.lastIndexOf(separatorChar);
//        name = lastIndex >= 0 ? name.substring(lastIndex + 1) : name;
//        return outputFileObject(packageName, name, source);

        of = tf.outputFileObject("", llvmAst.source());
        String opfPath = of.getName();
        if (!opfPath.endsWith("$")) outputFiles.add(of);

        try (CodeWriter w = tf.outputCodeWriter(of, outputWidth)) {
            llvmAst.prettyPrint(w, this);
        }
        catch (IOException e) {
            job.compiler()
               .errorQueue()
               .enqueue(ErrorInfo.IO_ERROR,
                        "I/O error while translating: " + e.getMessage());
            return false;
        }
        return true;
    }

}
