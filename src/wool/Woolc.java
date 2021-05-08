/*******************************************************************************
 * This files was developed for CS4533: Techniques of Programming Language Translation
 * and/or CS544: Compiler Construction
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2020-21 Gary F. Pollice
 *******************************************************************************/
package wool;
import static wool.Woolc.Phase.*;
import java.io.*;
import java.util.*;
import javax.swing.JFrame;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

import wool.ast.ASTPrinter;
import wool.lexparse.WoolParser;
import wool.symbol.tables.TableManager;
import wool.utility.*;


/**
 * Compiler tool for the Wool programming language.
 */
public class Woolc
{
    public static enum Phase {PARSE, AST, SEMANTIC, IR, COMPILE};
    private List<String> fileNames;
    private String outputDirectory;
    private Phase phase = COMPILE;
    private boolean displayParseTree;
    private boolean displayGUI;
    private boolean displayAST;
    private boolean displayIR;
    private boolean displayTables;
    private boolean displaySource;
    private final String PACKAGE = "wool"; 
    private WoolRunnerImpl runner;
    private Map<String, byte[]> bytecode;
    
    public Woolc()
    {
        fileNames = new ArrayList<String>();
        outputDirectory = "woolcode";        // default
        phase = COMPILE;
        displayParseTree = false;
        displayGUI = false;
        displayAST = false;
        displayIR = false;
        displayTables = false;
        displaySource = false;
        bytecode = null;
    }
    
    
    public void executeTool(String[] args) throws Exception
    {
        parseArgs(args);
        StringBuilder woolText = new StringBuilder();
        if (fileNames.size() > 0) {
            for (String fn : fileNames) {
//                woolText.append("########################################\n"
//                        + "#\n# File: " +fn 
//                        + "\n#\n########################################\n");
                woolText.append(new Scanner(new File(fn)).useDelimiter("\\A").next());
            }
            processProgram(woolText);
            postProcess(woolText);
        }
    }
    
    private void processProgram(StringBuilder coolText) throws Exception
    {
        runner = WoolFactory.makeParserRunner(CharStreams.fromString(coolText.toString()));
        switch (phase) {
            case PARSE: runner.parse(); break;
//            case AST: runner.createAST(); break;
            case SEMANTIC: runner.typecheck(); break;
           //case SEMANTIC: runner.createAST(); break;
//            case IR: runner.makeIR(); break;
            case COMPILE: 
            	bytecode = runner.compile(); 
                writeOutput();
                break;
            default: 
                System.err.println("Phase not yet implemented: " + phase);
                System.exit(0);
        }
    }
    
    private void writeOutput() throws Exception
    {
        if (bytecode != null) {
        	
        	
        	
            for (String s : bytecode.keySet()) {
                String classFilePath =  outputDirectory + "/wool/" + s + ".class";
                FileOutputStream fos = new FileOutputStream(classFilePath);
                fos.write(bytecode.get(s));
                fos.close();
                
                

            }
        }
    }
    
    private void postProcess(StringBuilder coolText)
    {
        if (displaySource) {
            System.out.println("\n------------------------------\nSource:\n");
            System.out.println(coolText.toString());
        }
        if (displayTables) {
          System.out.println("\n------------------------------\nSymbol tables:\n");
          System.out.println(TableManager.getInstance().toString());
       }
        
        if (displayParseTree) {
            System.out.println("\n------------------------------\nParse tree:\n");
            System.out.println(runner.getParseTree().toStringTree(runner.getParser()));
        }
        
        if (displayGUI) {
            showGUI(runner.getParser(), runner.getParseTree());
        }
        
        if (displayAST) {
            System.out.println("\n------------------------------\nAbstract Syntax Tree:\n");
            ASTPrinter printer = new ASTPrinter();
           runner.getAst().accept(printer);
        }
        
//        if (displayIR) {
//            System.out.println("\n------------------------------\nIntermediate code:\n");
//            for (IRinstruction ins : runner.getIR()) {
//                System.out.println(ins.toString());
//            }
//        }
    }
    
    private void showGUI(WoolParser parser, ParserRuleContext tree) {
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
    
    private void showHelp()
    {
        System.out.println("Usage: coolc [ options ] [ sourcefiles ]\n"
            + "Options: \n"
            + "    -o outputdirectory\n"
            + "    -h\n"
            + "    -p phase (parse|ast|semantic|ir)\n"
            + "    -d internals (pt|gui|ast|ir|tables|source)");
    }
    
    /**
     * Parse the command line arguments.
     * @param args
     */
    private boolean parseArgs(String[] args)
    {
        boolean argsOK = true;
        boolean optionsDone = false;
        for (int i = 0; i < args.length; i++) {
            String s = args[i];
            switch (s) {
            case "-o":
                outputDirectory = args[++i];
                break;
            case "-h":
                showHelp();
                break;
            case "-p":
                switch (args[++i]) {
                    case "parse": phase = PARSE; break;
                    case "ast": phase = AST; break;
                    case "semantic": phase = SEMANTIC; break;
                    case "ir": phase = IR; break;
                    case "compile": phase = COMPILE; break;
                    default: throw new RuntimeException("Invalid phase");
                }
                break;
            case "-d":
                switch (args[++i]) {
                    case "pt": displayParseTree = true; break;
                    case "gui": displayGUI = true; break;
                    case "ast": displayAST = true; break;
                    case "ir": displayIR = true; break;
                    case "tables": displayTables = true; break;
                    case "source": displaySource = true; break;
                    default: throw new RuntimeException("Invalid display");
                }
                break;
            default: 
                fileNames.add(args[i]);
                break;
            }
        }
        return argsOK;
    }

    /**
     * Main runner
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        Woolc tool = new Woolc();
        tool.executeTool(args);
    }

}
