package polyllvm.util;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.PointerPointer;
import polyglot.ast.*;
import polyglot.types.ProcedureInstance;
import polyglot.types.Type;
import polyglot.util.InternalCompilerError;
import polyglot.util.Position;
import polyllvm.visit.LLVMTranslator;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import static org.bytedeco.javacpp.LLVM.*;

/**
 * Created by Daniel on 2/24/17.
 */
public class DebugInfo {
    private final LLVMTranslator v;
    public final LLVMDIBuilderRef diBuilder;
    public final LLVMBuilderRef builder;
    public final LLVMMetadataRef compileUnit;
    private Deque<LLVMMetadataRef> scopes;

    private HashMap<Type, LLVMMetadataRef> typeMap = new HashMap<>();

    public final String fileName;
    public final String filePath;

    public DebugInfo(LLVMTranslator v, LLVMModuleRef mod, LLVMBuilderRef builder, String filePath) {
        this.v = v;
        this.diBuilder = LLVMNewDIBuilder(mod);
        this.builder = builder;

        File file = new File(filePath);
        this.fileName = file.getName();
        this.filePath = file.getParent();

        this.compileUnit = LLVMDIBuilderCreateCompileUnit(diBuilder, DW_LANG_C, fileName, this.filePath, "PolyLLVM", 0, "", 0);
        this.scopes = new ArrayDeque<>();

        String s = "Debug Info Version";
        LLVMAddModuleFlag(mod, Warning, s, /*DEBUG_METADATA_VERSION*/ 3);

        BytePointer defaultTargetTriple = LLVMGetDefaultTargetTriple();
        //TODO: Make darwin check more robust : Triple(sys::getProcessTriple()).isOSDarwin()
        if (defaultTargetTriple.getString().contains("darwin")) {
            LLVMAddModuleFlag(mod, Warning, "Dwarf Version", 2);
        }
        LLVMDisposeMessage(defaultTargetTriple);


    }

    public void pushScope(LLVMMetadataRef scope) {
        scopes.push(scope);
    }

    public void popScope() {
        scopes.pop();
    }

    public LLVMMetadataRef currentScope() {
        LLVMMetadataRef scope;
        if (scopes.isEmpty()) {
            scope = compileUnit;
        } else {
            scope = scopes.peek();
        }
        assert scope != null;
        return scope;
    }

    public LLVMMetadataRef createFile() {
        return LLVMDIBuilderCreateFile(diBuilder, fileName, filePath);
    }


    /*
     * Helper functions for emitting locations
     */

    public void emitLocation(Node n) {
        if (n.position().line() == Position.UNKNOWN || n.position().column() == Position.UNKNOWN) {
            emitLocation();
            return;
        }
        emitLocation(n.position().line(), n.position().column());
    }

    public void emitLocationEnd(Node n) {
        if (n.position().line() == Position.UNKNOWN || n.position().column() == Position.UNKNOWN) {
            emitLocation();
            return;
        }
        emitLocation(n.position().endLine(), n.position().endColumn());
    }


    public void emitLocation() {
        emitLocation(0,0);
    }

    private void emitLocation(int line, int column) {
        LLVMMetadataRef scope = currentScope();
        assert scope !=null && !scope.isNull();
        LLVMSetCurrentDebugLocation2(builder, line, column, scope, null);
    }

    /*
     * Helper functions for creating debug information for variables and parameters
     */

    private LLVMMetadataRef createExpression() {
        long[] longs = new long[0];
        return LLVMDIBuilderCreateExpression(diBuilder, longs, 0);
    }

    private void insertDeclareAtEnd(LLVMTranslator v, LLVMValueRef alloc, LLVMMetadataRef varMetadata, Position p) {
        LLVMDIBuilderInsertDeclareAtEnd(diBuilder, alloc, varMetadata,
                createExpression(),
                p.line(), p.column(), currentScope(), null,
                LLVMGetInsertBlock(v.builder));
    }

    public void createParamVariable(LLVMTranslator v, Formal f, int index, LLVMValueRef alloc) {
        String name = f.name();
        Position p = f.position();
        Type t = f.type().type();
        LLVMMetadataRef paramVar = LLVMDIBuilderCreateParameterVariable(
                diBuilder, currentScope(),
                name, index, createFile(), p.line(),
                debugType(t), /*alwaysPreserve*/ 0, /*flags*/ 0);
        insertDeclareAtEnd(v, alloc, paramVar, p);
    }

    public void createLocalVariable(LLVMTranslator v, LocalDecl n, LLVMValueRef alloc) {
        String name = n.name();
        Position p = n.position();
        Type t = n.type().type();
        LLVMMetadataRef localVar = LLVMDIBuilderCreateAutoVariable(
                diBuilder, currentScope(),
                name, createFile(), p.line(),
                debugType(t), /*alwaysPreserve*/ 0, /*flags*/ 0);
        insertDeclareAtEnd(v, alloc, localVar, p);
    }

    public void funcDebugInfo(ProcedureDecl n, LLVMValueRef funcRef) {
        ProcedureInstance pi = n.procedureInstance();
        LLVMMetadataRef Unit = createFile();
        int LineNo = n.position().line();
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(diBuilder, Unit, n.name(), v.mangler.mangleProcedureName(pi), Unit, LineNo,
                createFunctionType(pi, Unit), /* internal linkage */ 0, /* definition */ 1,
                LineNo, /*DINode::FlagPrototyped*/ 1 << 8, /*is optimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
    }

    public void funcDebugInfo(int LineNo, String name, String linkageName, LLVMMetadataRef funcType, LLVMValueRef funcRef) {
        LLVMMetadataRef Unit = createFile();
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(diBuilder, Unit, name, linkageName, Unit, LineNo,
                funcType, /* internal linkage */ 0, /* definition */ 1,
                LineNo, /*DINode::FlagPrototyped*/ 1 << 8, /*is optimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
    }


    /*
     * Helper functions for creating debug types
     */

    public LLVMMetadataRef debugType(Type t) {
        if (typeMap.containsKey(t)) {
            return typeMap.get(t);
        }
        t = v.jl5Utils.translateType(t);

        LLVMMetadataRef debugType;
        if (t.isBoolean() || t.isLongOrLess() || t.isFloat() || t.isDouble()) {
            debugType = debugBasicType(t);
        } else if (t.isArray()) {
            debugType = LLVMDIBuilderCreateArrayType(diBuilder, v.utils.sizeOfType(t), v.utils.sizeOfType(t), debugType(t.toArray().base()), null);
            //debugType = LLVMDIBuilderCreatePointerType(diBuilder, debugType(t.toArray().base()), v.utils.sizeOfType(t), v.utils.sizeOfType(t), "array");
        } else if (t.isNull()) {
            debugType = LLVMDIBuilderCreatePointerType(diBuilder, LLVMDIBuilderCreateBasicType(diBuilder, t.toString(), 64, 64, DW_ATE_signed), v.utils.sizeOfType(t)*8, v.utils.sizeOfType(t)*8, "class");
        } else if (t.isReference()) {
            int line = t.position().line() == -1 ? 0 : t.position().line() ;
            debugType = LLVMDIBuilderCreateClassType(diBuilder, currentScope(), t.toString(), createFile(),
                    line, 0,0, /*Flags*/0 , t.toClass().superType() == null ? null : debugType(t.toClass().superType()), null);

        } else throw new InternalCompilerError("Invalid type");
        typeMap.put(t, debugType);
        return debugType;

    }


    private LLVMMetadataRef debugBasicType(Type t) {
        int encoding;
        if (t.isBoolean()) {
            encoding = DW_ATE_boolean;
        } else if (t.isLongOrLess()) {
            encoding = DW_ATE_signed;
        } else if (t.isFloat() || t.isDouble()) {
            encoding = DW_ATE_float;
        } else throw new InternalCompilerError("Type " + t + " is not a basic type");
        long numBits = v.utils.sizeOfType(t)*8;
        return LLVMDIBuilderCreateBasicType(diBuilder, t.toString(), numBits, numBits, encoding);
    }

    public LLVMMetadataRef createFunctionType(ProcedureInstance pi, LLVMMetadataRef unit) {
        LLVMMetadataRef[] formals = pi.formalTypes().stream().map(this::debugType).toArray(LLVMMetadataRef[]::new);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(diBuilder, new PointerPointer<>(formals), formals.length);
        return LLVMDIBuilderCreateSubroutineType(diBuilder, unit, typeArray);
    }


    public void enterBlock(Block node) {
        LLVMMetadataRef lexicalBlockScope = LLVMDIBuilderCreateLexicalBlock(diBuilder, currentScope(), createFile(),
                node.position().line(), node.position().column());
        pushScope(lexicalBlockScope);
    }
}


