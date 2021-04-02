/*******************************************************************************
 * This files was developed for CS4233: Object-Oriented Analysis & Design.
 * The course was taken at Worcester Polytechnic Institute.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2020 Gary F. Pollice
 *******************************************************************************/

package wool.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.BeforeEach;
import wool.lexparse.WoolParser;
import wool.utility.*;

/**
 * Description
 */
public abstract class WoolTestRunner extends ClassLoader
{
    protected WoolParser parser;
    protected ParserRuleContext tree;
//    protected ASTNode ast;
    protected WoolRunner runner;
    protected WoolRunnerImpl testRunner;
    protected Map<String, byte[]> code;
    
    /**
     * Clear the state for the new test
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        parser = null;
        tree = null;
        runner = new WoolRunnerImpl();
    }
    
    // Helper methods
    /**
     * This method performs the parse. If you want to see what the tree looks like, use
     * <br>
     * <code>System.out.println(tree.toStringTree());<code></br>
     * after calling this method.
     * 
     * @param text
     *            the text to parse
     */
    protected void doParse(CharStream text)
    {
        runner = testRunner = 
            (WoolRunnerImpl) WoolFactory.makeParserRunner(text);
        parser = testRunner.getParser();
        tree = runner.parse();
        assertTrue(true);
    }
    
   protected void doSymbol(CharStream text)
    {
        runner = testRunner = 
                (WoolRunnerImpl) WoolFactory.makeParserRunner(text);
        tree = runner.buildSymbolTable();
    }
//    
//    protected ASTNode doAST(CharStream text)
//    {
//        TableManager.reset();
//        runner = testRunner = 
//            (WoolRunnerImpl) WoolFactory.makeParserRunner(text);
//        parser = testRunner.getParser();
//        ast = runner.createAST();
//        assertTrue(true);
//        return ast;
//    }
//    
//    protected ASTNode doSymbolCheck(CharStream text)
//    {
//        doAST(text);
//        ast.accept(new SymbolTableChecker());
//        return ast;
//    }
//    
//    protected ASTNode doTypeCheck(CharStream text)
//    {
//        doSymbolCheck(text);
//        ast.accept(new TypeChecker());
//        return ast;
//    }
//    
//    protected Map<String, byte[]> doCompile(CharStream text)
//    {
//        doTypeCheck(text);
//        CoolCodeEmitter emitter = new CoolCodeEmitter();
//        ast.accept(emitter);
//        code = emitter.getBytecodes();
//        return code;
//    }
    
    /**
     * Call this routine to display the parse tree. Hit ENTER on the console to continue.
     */
    protected void showTree()
    {
        ParserRuleContext tree = testRunner.getParseTree();
        System.out.println(tree.toStringTree(parser));
        List<String> ruleNames = Arrays.asList(parser.getRuleNames());
        TreeViewer tv = new TreeViewer(ruleNames, tree);
        JFrame frame = new JFrame("Parse Tree");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(tv);

        // Display the window.
        frame.pack();
        frame.setVisible(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            br.readLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}