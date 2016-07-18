import polyglot.ast.IntLit_c;
import polyglot.util.Position;

public class ExtendImportedClass extends IntLit_c {

    public ExtendImportedClass(Position pos, Kind kind, long value) {
        super(pos, kind, value);
        something();
    }

    private long something() {
        return 12312312312L;
    }


    public long value() {
        return super.value();
    }
}
