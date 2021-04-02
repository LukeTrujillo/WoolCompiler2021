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


public class WoolFactory
{
    /**
     * Return a runner that has just a lexer.
     * @param input the input stream
     * @return the runner
     */
    public static WoolRunnerImpl makeLexerRunner(CharStream input)
    {
        WoolRunnerImpl runner = new WoolRunnerImpl();
        runner.setLexer(makeLexer(input));
        return runner;
    }
    
    /**
     * Return a runner that has a working parser
     * @param input
     * @return input the input stream
     * @return the runner
     */
    public static WoolRunnerImpl makeParserRunner(CharStream input)
    {
        final WoolRunnerImpl runner = new WoolRunnerImpl();
        final WoolLexer lexer = makeLexer(input);
        runner.setLexer(lexer);
        final WoolParser parser = makeParser(lexer);
        runner.setParser(parser);
        return runner;
    }
    
    /**
     * Make the Lexer for the given input. Add a base error listener.
     * @param input the input stream
     * @return the lexer
     */
    private static WoolLexer makeLexer(CharStream input)
    {
        final WoolLexer lexer = new WoolLexer(input);
//      lexer.removeErrorListeners();
        lexer.addErrorListener(
                new BaseErrorListener() {
                    @Override
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e)
                    {
                        throw new WoolException(msg, e);
                    }
                }
        );
        return lexer;
    }
    
    private static WoolParser makeParser(WoolLexer lexer)
    {
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final WoolParser parser = new WoolParser(tokenStream);
//      parser.removeErrorListeners();
        parser.addErrorListener(
                new BaseErrorListener() {
                    @Override
                    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg,
                            RecognitionException e)
                    {
                        throw new WoolException(
                            e == null ? "Recoverable parser error" : e.getMessage(), e);
                    }
                }
        );
        return parser;
    }
}