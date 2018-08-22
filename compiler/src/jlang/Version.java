package jlang;

/**
 * Version information for JLang extension
 */
public class Version extends polyglot.main.Version {
    @Override
    public String name() {
        return "jlang";
    }

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
