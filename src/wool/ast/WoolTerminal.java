package wool.ast;


import org.antlr.v4.runtime.Token;

import wool.symbol.bindings.AbstractBinding;

public class WoolTerminal extends ASTNode{

	public static enum TerminalType {tInt, tStr, tBool, tID, tMethod, tType };
		
	public TerminalType terminalType;
	public AbstractBinding binding;
	
	public WoolTerminal(AbstractBinding binding) {
		super(ASTNodeType.nTerminal);
	
		this.binding = binding;
		this.terminalType = TerminalType.tID;
		token = null;
	}
	
	public WoolTerminal(Token token, TerminalType node) {
		super(ASTNodeType.nTerminal);
		
		this.token = token;
		this.terminalType = node;
		
	}

}
