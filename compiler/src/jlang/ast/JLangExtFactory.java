//Copyright (C) 2017 Cornell University

package jlang.ast;

import polyglot.ast.Ext;
import polyglot.ext.jl7.ast.JL7ExtFactory;

/**
 * Extension factory for the JLang extension.
 */
public interface JLangExtFactory extends JL7ExtFactory {

    Ext extESeq();

    Ext extAddressOf();

    Ext extLoad();
}
