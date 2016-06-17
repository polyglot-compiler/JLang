/*******************************************************************************
 * This file is part of the Polyglot extensible compiler framework.
 *
 * Copyright (c) 2000-2012 Polyglot project group, Cornell University
 * Copyright (c) 2006-2012 IBM Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program and the accompanying materials are made available under
 * the terms of the Lesser GNU Public License v2.0 which accompanies this
 * distribution.
 *
 * The development of the Polyglot project has been supported by a
 * number of funding sources, including DARPA Contract F30602-99-1-0533,
 * monitored by USAF Rome Laboratory, ONR Grants N00014-01-1-0968 and
 * N00014-09-1-0652, NSF Grants CNS-0208642, CNS-0430161, CCF-0133302,
 * and CCF-1054172, AFRL Contract FA8650-10-C-7022, an Alfred P. Sloan
 * Research Fellowship, and an Intel Research Ph.D. Fellowship.
 *
 * See README for contributors.
 ******************************************************************************/

package polyllvm;

import polyglot.ast.Node;
import polyglot.frontend.AbstractPass;
import polyglot.frontend.goals.Goal;
import polyglot.util.InternalCompilerError;
import polyllvm.visit.LLVMPrinter;

/** An output pass generates output code from the processed AST. */
public class LLVMOutputPass extends AbstractPass {
    protected LLVMPrinter printer;

    /**
     * Create a Translator.  The output of the visitor is a collection of files
     * whose names are added to the collection {@code outputFiles}.
     */
    public LLVMOutputPass(Goal goal, LLVMPrinter printer) {
        super(goal);
        this.printer = printer;
    }

    @Override
    public boolean run() {
        Node ast = goal.job().ast();

        if (ast == null) {
            throw new InternalCompilerError("AST is null");
        }

        if (printer.translate(ast)) {
            return true;
        }

        return false;
    }
}
