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

package wool.lexparse;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import javax.swing.JFrame;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import wool.testutil.WoolTestRunner;
import wool.utility.WoolException;
import wool.utility.WoolRunnerImpl;

/**
 * Description
 */
class WoolParserTest extends WoolTestRunner
{
    /**
     * Clear the state for the new test
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception
    {
        parser = null;
        tree = null;
        runner = testRunner = new WoolRunnerImpl();
    }
    
    @ParameterizedTest
    @ValueSource( strings = {
        "emptyClass.wl",
        "ambiguity.wl",
        "bst.wl",
        "classWithMethods.wl",
        "classwithVariables.wl",
        "emptyClassWithParent.wl",
        "fizzbuzz.wl",
        "literals.wl",
        "multiArgumentMethods.wl",
        "negationAndIsNull.wl",
        "precedence.wl",
        "Robot.wl"
    })
    void useValidTestFiles(String f) throws IOException
    {
        doParse(CharStreams.fromFileName("test-files/" + f));
        assertNotNull(tree);
    }
    
    @ParameterizedTest
    @ValueSource( strings = {
        "invalid/overmatchedComment.wl",
        "invalid/unmatchedComment.wl",
        "invalid/unmatchedNestedComment.wl"
    })
    void useInvaidTestFiles(String f) throws IOException
    {	
    	try {
    		  doParse(CharStreams.fromFileName("test-files/" + f));
    	} catch(WoolException exception) {
    		assert(true);
    	}
       
    }
}
