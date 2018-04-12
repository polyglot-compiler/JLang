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
import static polyllvm.util.Constants.DEBUG_DWARF_VERSION;
import static polyllvm.util.Constants.DEBUG_INFO_VERSION;

/**
 * Created by Daniel on 2/24/17.
 */
public class DebugInfo {
    private final LLVMTranslator v;
    public final LLVMDIBuilderRef diBuilder;
    private final String fileName;
    private final String fileDir;
    public final LLVMMetadataRef debugFile;
    private final LLVMMetadataRef compileUnit;
    private Deque<LLVMMetadataRef> scopes = new ArrayDeque<>();
    private HashMap<Type, LLVMMetadataRef> typeCache = new HashMap<>();

    public DebugInfo(LLVMTranslator v, LLVMModuleRef mod, String path) {
        this.v = v;
        diBuilder = LLVMNewDIBuilder(mod);

        File file = new File(path);
        fileName = file.getName();
        fileDir = file.getParent();
        debugFile = LLVMDIBuilderCreateFile(diBuilder, fileName, fileDir);

        compileUnit = LLVMDIBuilderCreateCompileUnit(
                diBuilder, DW_LANG_Java, fileName, fileDir, "PolyLLVM", 0, "", 0);

        LLVMAddModuleFlag(mod, Warning, "Debug Info Version", DEBUG_INFO_VERSION);

        //TODO: Make darwin check more robust : Triple(sys::getProcessTriple()).isOSDarwin()
        BytePointer defaultTargetTriple = LLVMGetDefaultTargetTriple();
        if (defaultTargetTriple.getString().contains("darwin"))
            LLVMAddModuleFlag(mod, Warning, "Dwarf Version", DEBUG_DWARF_VERSION);
        LLVMDisposeMessage(defaultTargetTriple);
    }

    public void pushScope(LLVMMetadataRef scope) {
        scopes.push(scope);
        updateLocationScope();
    }

    public void popScope() {
        scopes.pop();
        updateLocationScope();
    }

    private void updateLocationScope() {
        LLVMDebugLocMetadata loc = LLVMGetCurrentDebugLocation2(v.builder);
        LLVMSetCurrentDebugLocation2(v.builder, loc.Line(), loc.Col(), currentScope(), null);
    }

    public LLVMMetadataRef currentScope() {
        return scopes.isEmpty() ? compileUnit : scopes.peek();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Debug locations.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public LLVMValueRef getLocation() {
        return LLVMGetCurrentDebugLocation(v.builder);
    }

    public void setLocation(LLVMValueRef loc) {
        LLVMSetCurrentDebugLocation(v.builder, loc);
    }

    public void setLocation(Node n) {
        LLVMMetadataRef scope = currentScope();
        assert scope != null && !scope.isNull();
        int line = n.position().line();
        int col = n.position().column();
        LLVMSetCurrentDebugLocation2(v.builder, line, col, scope, /*inlinedAt*/ null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Variables, parameters, and functions.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private LLVMMetadataRef createExpression() {
        long[] longs = new long[0];
        return LLVMDIBuilderCreateExpression(diBuilder, longs, 0);
    }

    private void insertDeclareAtEnd(
            LLVMTranslator v, LLVMValueRef alloc, LLVMMetadataRef varMetadata, Position p) {
        LLVMDIBuilderInsertDeclareAtEnd(
                diBuilder, alloc, varMetadata,
                createExpression(),
                LLVMGetInsertBlock(v.builder),
                p.line(), p.column(), currentScope());
    }

    public void createParamVariable(LLVMTranslator v, Formal f, int index, LLVMValueRef alloc) {
        String name = f.name();
        Position p = f.position();
        Type t = f.declType();
        LLVMMetadataRef paramVar = LLVMDIBuilderCreateParameterVariable(
                diBuilder, currentScope(),
                name, index, debugFile, p.line(),
                debugType(t), /*alwaysPreserve*/ 0, /*flags*/ 0);
        insertDeclareAtEnd(v, alloc, paramVar, p);
    }

    public void createLocalVariable(LLVMTranslator v, VarDecl n, LLVMValueRef alloc) {
        String name = n.name();
        Position p = n.position();
        Type t = n.declType();
        LLVMMetadataRef localVar = LLVMDIBuilderCreateAutoVariable(
                diBuilder, currentScope(),
                name, debugFile, p.line(),
                debugType(t), /*alwaysPreserve*/ 0, /*flags*/ 0, /*align*/ 0);
        insertDeclareAtEnd(v, alloc, localVar, p);
    }

    public void funcDebugInfo(ProcedureDecl n, LLVMValueRef funcRef) {
        ProcedureInstance pi = n.procedureInstance();
        LLVMMetadataRef unit = debugFile;
        int line = n.position().line();
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(
                diBuilder, unit, n.name(), v.mangler.mangleProcName(pi), unit, line,
                createFunctionType(pi, unit), /*internalLinkage*/ 0, /*definition*/ 1,
                line, /*DINode::FlagPrototyped*/ 1 << 8, /*isOptimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
    }

    public void funcDebugInfo(
            LLVMValueRef funcRef, String name, String linkageName,
            LLVMMetadataRef funcType, int line) {
        LLVMMetadataRef unit = debugFile;
        LLVMMetadataRef sp = LLVMDIBuilderCreateFunction(
                diBuilder, unit, name, linkageName, unit, line,
                funcType, /*internalLinkage*/ 0, /*definition*/ 1,
                line, /*DINode::FlagPrototyped*/ 1 << 8, /*isOptimized*/ 0);
        LLVMSetSubprogram(funcRef, sp);
        pushScope(sp);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Debug types.
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public LLVMMetadataRef debugType(Type t) {
        Type erased = v.utils.erasureLL(t);
        if (typeCache.containsKey(erased)) {
            return typeCache.get(erased);
        }

        LLVMMetadataRef debugType;
        if (erased.isBoolean() || erased.isLongOrLess() || erased.isFloat() || erased.isDouble()) {
            debugType = debugBasicType(erased);
        }
        else if (erased.isNull()) {
            debugType = LLVMDIBuilderCreateBasicType(
                    diBuilder, "null", 8 * v.utils.llvmPtrSize(), DW_ATE_address);
        }
        else if (erased.isArray()) {
            int size = v.utils.sizeOfType(erased);
            LLVMMetadataRef elemType = debugType(erased.toArray().base());
            debugType = LLVMDIBuilderCreateArrayType(
                    diBuilder, size, size, elemType, /*subscripts*/ null);
        }
        else if (erased.isClass()) {
            debugType = LLVMDIBuilderCreateStructType(
                    diBuilder, currentScope(), erased.toString(), debugFile,
                    erased.position().line(), /*TODO*/ 0, /*TODO*/ 0, /*flags*/0 ,
                    /*TODO*/ null, /*TODO*/ null);
        }
        else throw new InternalCompilerError("Cannot handle " + erased.getClass());

        typeCache.put(erased, debugType);
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

    private LLVMMetadataRef createFunctionType(ProcedureInstance pi, LLVMMetadataRef unit) {
        LLVMMetadataRef[] formals = pi.formalTypes().stream()
                .map(this::debugType).toArray(LLVMMetadataRef[]::new);
        LLVMMetadataRef typeArray = LLVMDIBuilderGetOrCreateTypeArray(
                diBuilder, new PointerPointer<>(formals), formals.length);
        return LLVMDIBuilderCreateSubroutineType(diBuilder, unit, typeArray);
    }

    // TODO: Use this to limit variables to the correct scope.
    public void enterBlock(Block node) {
        LLVMMetadataRef lexicalBlockScope = LLVMDIBuilderCreateLexicalBlock(
                diBuilder, currentScope(), debugFile,
                node.position().line(), node.position().column());
        pushScope(lexicalBlockScope);
    }
}
