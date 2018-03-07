package polyllvm.types;

import polyglot.ext.jl5.types.reflect.JL5ClassFileLazyClassInitializer;
import polyglot.types.FieldInstance;
import polyglot.types.Flags;
import polyglot.types.TypeSystem;
import polyglot.types.reflect.ClassFile;
import polyllvm.util.Constants;

public class PolyLLVMClassFileLazyClassInitializer extends JL5ClassFileLazyClassInitializer {

    PolyLLVMClassFileLazyClassInitializer(ClassFile file, TypeSystem ts) {
        super(file, ts);
    }

    @Override
    public void initFields() {
        if (fieldsInitialized)
            return;

        // Add static field to hold the class object.
        ct.addField(classObjectFieldInstance());

        super.initFields();
    }

    protected FieldInstance classObjectFieldInstance() {
        return ts.fieldInstance(
                ct.position(),
                ct,
                Flags.NONE.Static().Final(),
                ts.Class(),
                Constants.CLASS_OBJECT);
    }
}
