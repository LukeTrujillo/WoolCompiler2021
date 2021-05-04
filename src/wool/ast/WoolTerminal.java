package wool.ast;


import org.antlr.v4.runtime.Token;

import wool.symbol.bindings.AbstractBinding;
import wool.typechecking.TypeChecker;

public class WoolTerminal extends ASTNode{

	public static enum TerminalType {tInt, tStr, tBool, tID, tMethod, tType};
		
	public TerminalType terminalType;
	
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
	
	@Override
	public String printNicely() { 
		
		if(terminalType == TerminalType.tID)
			return getTabsForDepth() + "binding_symbol=" + binding.getSymbol()+ " (Node"
					+ " Type: " + nodeType.name() + "  Termninal Type: " + terminalType.name() + ")"; 
		else 
			return getTabsForDepth() + "token=" + token.getText() + " (Node Type: " + nodeType.name() + "  Termninal Type: " + terminalType.name() + ")"; 
	
	}

}
