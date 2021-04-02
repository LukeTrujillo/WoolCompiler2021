/*******************************************************************************
 * This files was developed for CS4533/CS544.
 * The course was taken at Worcester Polytechnic Institute.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2021 Gary F. Pollice
 *******************************************************************************/

package wool.utility;

import org.antlr.v4.runtime.*;
import wool.lexparse.*;

/**
 * The WoolRunner is an object produced by the WoolFactory. It provides handles
 * to all of the methods needed to run the compiler. By default, each of these methods
 * throws a CoolException unless the factory created the necessary components when
 * creating the runner.
 * @version Oct 25, 2017
 */
public interface WoolRunner
{
     /**
     * Scan the next token using the CoolLexer in the runner.
     * @return the next token.
     */
    Token nextToken();
    
    /**
     * Parse the input using the CoolParser in the runner.
     * @return the parse tree
     */
    ParserRuleContext parse();
    
    ParserRuleContext buildSymbolTable();
    
//    /**
//     * Parse the input and produce the AST.
//     * @return the AST
//     */
//    ASTNode createAST();
//    
//    /**
//     * Typecheck after running the AST.
//     * @return the AST after typechecking, unless there are errors, in which case
//     *  it may be incomplete.
//     */
//    ASTNode typecheck();
//    
//    /**
//     * Do typechecking and then create the IR stream.
//     * @return
//     */
//    LinkedList<IRinstruction> makeIR();
//    
//    /**
//     * Compile the source code.
//     * @return the class file bytecode for each class that can be written.
//     */
//    Map<String, byte[]> compile();
}
