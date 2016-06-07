package polyllvm;

/**
 * Version information for PolyLLVM extension
 */
public class Version extends polyglot.main.Version {
    @Override
    public String name() {
        return "PolyLLVM";
    }

    // TODO: define a version number, the default (below) is 0.1.0
    @Override
    public int major() {
        return 0;
    }

    @Override
    public int minor() {
        return 1;
    }

    @Override
    public int patch_level() {
        return 0;
    }
}
