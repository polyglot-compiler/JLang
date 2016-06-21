package polyllvm.util;

public class PolyLLVMMangler {
    public static String mangleMethodName(String className, String methodName) {
        if (methodName.equals("main")) {
            return methodName;
        }
        return "_" + className + "_" + methodName;
    }
}
