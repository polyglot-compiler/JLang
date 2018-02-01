package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.StringLit;
import polyglot.types.ParsedClassType;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMStringLitExt extends PolyLLVMExt {

    @Override
    public Node leaveTranslateLLVM(LLVMTranslator v) {
        StringLit n = (StringLit) node();
        char[] chars = n.value().toCharArray();

        ParsedClassType arrayType = v.utils.getArrayType();
        LLVMValueRef dvGlobal = v.utils.toCDVGlobal(arrayType);
        LLVMValueRef length = LLVMConstInt(v.utils.intType(32), chars.length, /*sign-extend*/ 0);
        List<LLVMValueRef> charTranslated = new ArrayList<>();

        LLVMValueRef sync_vars = LLVMConstInt(v.utils.llvmPtrSizedIntType(), 0, /*sign-extend*/0);

        // Add an i32 for alignment.
        charTranslated.add(LLVMConstInt(v.utils.intType(32), 0, 0));
        for (char c : chars) {
            charTranslated.add(LLVMConstInt(v.utils.intType(16), c, 0));
        }
        LLVMValueRef[] structBody =
                Stream.concat(Stream.of(dvGlobal, sync_vars, length), charTranslated.stream())
                        .toArray(LLVMValueRef[]::new);

        LLVMValueRef charArray = v.utils.buildConstStruct(structBody);
        String reduce = intStream(n.value().getBytes()).mapToObj(b -> b + "_").reduce("", (s1, s2) -> s1 + s2);
        String charVarName = "_char_arr_" + reduce;
        LLVMValueRef stringLit = v.utils.getGlobal(v.mod, charVarName, LLVMTypeOf(charArray));
        LLVMSetLinkage(stringLit, LLVMLinkOnceODRLinkage);
        LLVMSetInitializer(stringLit, charArray);

        LLVMValueRef dvString = v.utils.toCDVGlobal(v.typeSystem().String());
        LLVMValueRef[] stringLitBody =
                Stream.of(dvString, sync_vars, LLVMConstBitCast(stringLit, v.utils.toLL(arrayType)))
                        .toArray(LLVMValueRef[]::new);

        LLVMValueRef string = v.utils.buildConstStruct(stringLitBody);
        String stringVarName = "_string_lit_" + reduce;
        LLVMValueRef stringVar = v.utils.getGlobal(v.mod, stringVarName, LLVMTypeOf(string));
        LLVMSetLinkage(stringVar, LLVMLinkOnceODRLinkage);
        LLVMSetInitializer(stringVar, string);

        v.addTranslation(n,LLVMConstBitCast(stringVar, v.utils.toLL(n.type())));
        return super.leaveTranslateLLVM(v);
    }

    public static IntStream intStream(byte[] array) {
        return IntStream.range(0, array.length).map(idx -> array[idx]);
    }
}
