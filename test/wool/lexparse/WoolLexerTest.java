package wool.lexparse;

import static org.antlr.v4.runtime.Recognizer.EOF;
import static org.junit.jupiter.api.Assertions.*;
import static wool.lexparse.WoolLexer.*;
import java.util.stream.Stream;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import junit.textui.TestRunner;
import wool.utility.*;

    /**
     * Acceptance tests for Assignment 1, Cool-W Lexer.
     * @version Mar 23, 2018
     */
    class WoolLexerMasterTest extends TestRunner
    {
        @ParameterizedTest
        @MethodSource("textTypeProvider")
        void recognizeSingleToken(String text, int type)
        {
            WoolRunnerImpl runner = newLexer(toStream(text));
            Token t = runner.nextToken();
            assertEquals(type, t.getType());
            assertEquals(EOF, runner.nextToken().getType());
        }
        
        @ParameterizedTest
        @ValueSource(strings = {
            "hello \""
        })
        void badSequenceOfLexemes(String text)
        {
            WoolRunner runner = newLexer(toStream(text));
            Token t = runner.nextToken();
            Executable e = () -> {
                Token x = t;
                while (x.getType() != EOF) {
                    x = runner.nextToken();
                }
            };
            assertThrows(Exception.class, e);
        }
        
        // Helper methods
        /**
         * Turn the string into an ANTLRInputStream
         * @param text the original text
         * @return the stream created from the text
         */
        private CharStream toStream(String text)
        {
            return CharStreams.fromString(text);
        }
        
        /**
         * Create the lexer for the current test
         * @param input the ANTLRInputStream to be scanned
         * @return the lexer
         */
        private WoolRunnerImpl newLexer(CharStream input)
        {
            return WoolFactory.makeLexerRunner(input);
        }
        
        /**
         * Data for single lexeme tests. These tests take a string that
         * should only return a single token and then be at EOF. Each instance
         * of the Arguments.of() method provides the two parameters for these
         * tests, a String and an int representing the token type.
         * @return the stream of arguments
         */
        private static Stream<Arguments> textTypeProvider()
        {
            return Stream.of(
                // INTEGERs, STRINGs, IDs, COMMENTS
                Arguments.of("# \"comment\"\nworld", ID),
                Arguments.of("(* comment *) hello", ID)
            );
        }
}
