package wool.utility;

import java.util.*;
import java.util.function.Supplier;
import org.antlr.v4.runtime.*;

import wool.ast.ASTBuilder;
import wool.ast.ASTNode;
import wool.lexparse.*;
import wool.typechecking.TypeChecker;
import wool.codegen.*;

public class WoolRunnerImpl implements WoolRunner {
	private WoolLexer lexer;
	private WoolParser parser;
	private ASTNode ast;
	private ParserRuleContext parseTree;

	private Supplier<Token> nextToken;

	/**
	 * Default constructor. Everything is uninitialized.
	 */
	public WoolRunnerImpl() {
		nextToken = () -> {
			throw new WoolException("Lexer has not been initialized");
		};
	}

	/**************************************************************************
	 * Compiler Actions These methods will usually be called by external clients.
	 * These are the methods called by the CoolRunner interface
	 */

	/*
	 * @see cool.utility.CoolRunner#nextToken()
	 */
	@Override
	public Token nextToken() {
		return nextToken.get();
	}

	/*
	 * @see cool.utility.CoolRunner#parse()
	 */
	@Override
	public ParserRuleContext parse() {
		parseTree = parser.program();
		return parseTree;
	}

	@Override
	public ParserRuleContext buildSymbolTable() {
		parseTree = parse();
		ASTBuilder builder = new ASTBuilder();
		parseTree.accept(builder);
		return parseTree;
	}

	@Override
	public ASTNode createAST() {
		parseTree = parse();
		ASTBuilder builder = new ASTBuilder();
		ast = parseTree.accept(builder);
		return ast;
	}

//    
	@Override
	public ASTNode typecheck() {
		createAST();
		// ast.accept(new SymbolTableChecker());
		ast.accept(new TypeChecker());
		return ast;
	}

	@Override
	public LinkedList<irInstruction> makeIR() {
		typecheck();
		/*IRCreator irc = new IRCreator();
		ast.accept(irc);
		ir = irc.ir;
		return ir;*/
		return null;
	}
//    
//    @Override
    public Map<String, byte[]> compile()
    {
        makeIR();
        IRCreator irc = new IRCreator();
        ast.accept(irc);
        return irc.classes;
    }

	/**************************************************************************
	 * Initializers These methods are called by the factory in order to set up and
	 * initialize the compiler components.
	 */

	/**
	 * Set the lexer and change the nextToken variable
	 * 
	 * @param lexer the lexer to set
	 */
	public void setLexer(WoolLexer lexer) {
		this.lexer = lexer;
		nextToken = () -> lexer.nextToken();
	}

	public void setParser(WoolParser parser) {
		this.parser = parser;
	}

	/**
	 * @return the lexer
	 */
	public WoolLexer getLexer() {
		return lexer;
	}

	/**
	 * @return the parser
	 */
	public WoolParser getParser() {
		return parser;
	}

	public ParserRuleContext getParseTree() {
		return parseTree;
	}

//    /**
//     * @return the ast
//     */
	public ASTNode getAst() {
		return ast;
	}
//    }
//    
//    /**
//     * get the IR code
//     */
//    public LinkedList<IRinstruction> getIR()
//    {
//        return ir;
//    }
}
