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

        this.compileUnit = LLVMDIBuilderCreateCompileUnit(diBuilder, DW_LANG_Java, fileName, this.filePath, "PolyLLVM", 0, "", 0);
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

    public void emitLocation() {
        emitLocation(0, 0);
    }

    private void emitLocation(int line, int column) {
        LLVMMetadataRef scope = currentScope();
        assert scope != null && !scope.isNull();
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
        LLVMDIBuilderInsertDeclareAtEnd(
                diBuilder, alloc, varMetadata,
                createExpression(),
                LLVMGetInsertBlock(v.builder),
                p.line(), p.column(), currentScope());
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

    public void createLocalVariable(LLVMTranslator v, VarDecl n, LLVMValueRef alloc) {
        String name = n.name();
        Position p = n.position();
        Type t = n.type().type();
        LLVMMetadataRef localVar = LLVMDIBuilderCreateAutoVariable(
                diBuilder, currentScope(),
                name, createFile(), p.line(),
                debugType(t), /*alwaysPreserve*/ 0, /*flags*/ 0, /*align*/ 0);
        insertDeclareAtEnd(v, alloc, localVar, p);
    }

    public void funcDebugInfo(ProcedureDecl n, LLVMValueRef funcRef) {
        ProcedureInstance pi = n.procedureInstance();
        LLVMMetadataRef unit = createFile();
        int line = n.position().line();
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(
                diBuilder, unit, n.name(), v.mangler.mangleProcedureName(pi), unit, line,
                createFunctionType(pi, unit), /*internalLinkage*/ 0, /*definition*/ 1,
                line, /*DINode::FlagPrototyped*/ 1 << 8, /*isOptimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
        emitLocation(n); // Update debug location with correct scope.
    }

    public void funcDebugInfo(
            int line, String name, String linkageName,
            LLVMMetadataRef funcType, LLVMValueRef funcRef) {
        LLVMMetadataRef unit = createFile();
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(
                diBuilder, unit, name, linkageName, unit, line,
                funcType, /*internalLinkage*/ 0, /*definition*/ 1,
                line, /*DINode::FlagPrototyped*/ 1 << 8, /*isOptimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
        emitLocation(line, 0); // Update debug location with correct scope.
    }

    /*
     * Helper functions for creating debug types
     */

    public LLVMMetadataRef debugType(Type t) {
        Type erased = v.utils.erasureLL(t);
        if (typeMap.containsKey(erased)) {
            return typeMap.get(erased);
        }

        LLVMMetadataRef debugType;
        if (erased.isBoolean() || erased.isLongOrLess() || erased.isFloat() || erased.isDouble()) {
            debugType = debugBasicType(erased);
        } else if (erased.isNull()) {
            debugType = LLVMDIBuilderCreatePointerType(diBuilder, LLVMDIBuilderCreateBasicType(diBuilder, erased.toString(), 64, DW_ATE_signed), v.utils.sizeOfType(erased)*8, v.utils.sizeOfType(erased)*8, "class");
        } else if (erased.isArray()) {
            debugType = LLVMDIBuilderCreateArrayType(diBuilder, v.utils.sizeOfType(erased), v.utils.sizeOfType(erased), debugType(erased.toArray().base()), null);
        } else if (erased.isClass()) {
            int line = erased.position().line() == -1 ? 0 : erased.position().line();
            debugType = LLVMDIBuilderCreateStructType(diBuilder, currentScope(), v.utils.erasureLL(erased).toString(), createFile(),
                    line, 0,0, /*Flags*/0 , erased.toClass().superType() == null ? null : debugType(erased.toClass().superType()), null);

        } else throw new InternalCompilerError("Cannot handle "+erased.getClass());
        typeMap.put(erased, debugType);
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
        long numBits = 8 * v.utils.sizeOfType(t);
        return LLVMDIBuilderCreateBasicType(diBuilder, t.toString(), numBits, encoding);
    }

    public LLVMMetadataRef createFunctionType(ProcedureInstance pi, LLVMMetadataRef unit) {
        LLVMMetadataRef[] formals = pi.formalTypes().stream()
                .map(this::debugType).toArray(LLVMMetadataRef[]::new);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(
                diBuilder, new PointerPointer<>(formals), formals.length);
        return LLVMDIBuilderCreateSubroutineType(diBuilder, unit, typeArray);
    }


    public void enterBlock(Block node) {
        LLVMMetadataRef lexicalBlockScope = LLVMDIBuilderCreateLexicalBlock(
                diBuilder, currentScope(), createFile(),
                node.position().line(), node.position().column());
        pushScope(lexicalBlockScope);
        emitLocation(node); // Update debug location with correct scope.
    }
}


