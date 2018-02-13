package polyllvm.types;

import polyglot.ext.jl5.types.JL5MethodInstance;
import polyglot.ext.jl5.types.JL5Subst;

/**
 * Represents a Java method whose type parameters have been substituted, but
 * also keeps track of information about this substitution. All methods produced
 * by type-checking a method call are instances of this type.
 * 
 * <p>
 * In Polyglot, type-checking a method call produces the substituted method
 * instance, but does not give back the substitution for the method's type
 * parameters. This interface provides a workaround to allow for querying about
 * the substitution for a method's type parameters in the scenario of a method
 * call.
 *
 */
public interface SubstMethodInstance extends JL5MethodInstance {
    /**
     * Returns the method before substituting its type parameters. The returned
     * value cannot be a {@link SubstMethodInstance}.
     */
    JL5MethodInstance base();

    /**
     * Returns the substitution for the type parameters of the method. If the
     * original method is not parameterized, it returns an empty substitution.
     */
    JL5Subst subst();
}
