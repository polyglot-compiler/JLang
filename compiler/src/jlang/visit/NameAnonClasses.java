//Copyright (C) 2017 Cornell University

package jlang.visit;

import polyglot.ast.ClassBody;
import polyglot.frontend.Job;
import polyglot.types.ClassType;
import polyglot.types.ParsedClassType;
import polyglot.util.Pair;

import java.util.HashMap;
import java.util.Map;

import jlang.ast.JLangNodeFactory;
import jlang.types.JLangTypeSystem;

/** Gives local and anonymous classes globally unique names. */
public class NameAnonClasses extends DesugarVisitor {

    /**
     * Maps (enclosing class, local class name) pairs to increasing integers.
     * This is used to make sure that the qualified names of local classes are unique.
     */
    private final Map<Pair<ClassType, String>, Integer> localClassNameCount = new HashMap<>();

    public NameAnonClasses(Job job, JLangTypeSystem ts, JLangNodeFactory nf) {
        super(job, ts, nf);
    }

    @Override
    public ClassBody leaveClassBody(ParsedClassType ct, ClassBody cb) {

        if (ct.isLocal() || ct.isAnonymous()) {
            String name = ct.name() != null ? ct.name() : "Anon$Class";
            ClassType enclosing = ct.outer();
            Pair<ClassType, String> countKey = new Pair<>(enclosing, name);
            int count = localClassNameCount.getOrDefault(countKey, 0) + 1;
            localClassNameCount.put(countKey, count);
            ct.name(name + "$" + count);
        }

        return super.leaveClassBody(ct, cb);
    }
}
