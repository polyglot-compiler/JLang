package polyllvm.extension;

import polyglot.ast.Node;
import polyglot.ast.StringLit;
import polyglot.types.ReferenceType;
import polyllvm.ast.PolyLLVMExt;
import polyllvm.visit.LLVMTranslator;

import java.lang.Override;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.bytedeco.javacpp.LLVM.*;

public class PolyLLVMStringLitExt extends PolyLLVMExt {

    @Override
    public Node translatePseudoLLVM(LLVMTranslator v) {
        StringLit n = (StringLit) node();

        char[] chars = n.value().toCharArray();

        ReferenceType arrayType = v.utils.getArrayType();
        LLVMValueRef dvGlobal = v.utils.getDvGlobal(arrayType);
        LLVMValueRef length = LLVMConstInt(LLVMInt32TypeInContext(v.context), chars.length, /*sign-extend*/ 0);
        List<LLVMValueRef> charTranslated = new ArrayList<>();

        charTranslated.add(LLVMConstInt(LLVMInt32TypeInContext(v.context), 0, 0)); //Needed to align struct properly
        for (char c : chars){
            charTranslated.add(LLVMConstInt(LLVMInt16TypeInContext(v.context), c, 0));
        }
        LLVMValueRef[] structBody = Stream.concat(Stream.of(dvGlobal, length), charTranslated.stream()).toArray(LLVMValueRef[]::new);

        LLVMValueRef charArray = v.utils.buildConstStruct(structBody);
        LLVMValueRef stringLit = v.utils.getGlobal(v.mod, "char_arr_" + n.value(), LLVMTypeOf(charArray));
        LLVMSetLinkage(stringLit, LLVMLinkOnceODRLinkage);
        LLVMSetInitializer(stringLit, charArray);

        LLVMValueRef dvString = v.utils.getDvGlobal(n.type().toReference());
        LLVMValueRef[] stringLitBody = Stream.of(dvString, LLVMConstBitCast(stringLit, v.utils.typeRef(arrayType))).toArray(LLVMValueRef[]::new);

        LLVMValueRef string = v.utils.buildConstStruct(stringLitBody);
        LLVMValueRef stringVar = v.utils.getGlobal(v.mod, "string_lit_" + n.value(), LLVMTypeOf(string));
        LLVMSetLinkage(stringVar, LLVMLinkOnceODRLinkage);
        LLVMSetInitializer(stringVar, string);

        v.addTranslation(n,LLVMConstBitCast(stringVar, v.utils.typeRef(n.type())));

        return super.translatePseudoLLVM(v);
    }
}
