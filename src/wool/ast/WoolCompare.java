package wool.ast;

import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

import wool.utility.WoolException;

public class WoolCompare extends ASTNode {
	
	private int opcode;
	public String tokenText;

	public WoolCompare(Token token) {
		super(ASTNodeType.nCompare);
		
		tokenText = token.getText();
	}
	
	public WoolCompare(String token) {
		super(ASTNodeType.nCompare);
		this.token = null;

		tokenText = token;
	}
	
	public int getOpcode() {
		
		
		
		if(getChild(0).binding.getSymbolType().equals("Int") || getChild(0).binding.getSymbolType().equals("Bool")) {
			switch(tokenText) {
			case "=": 
				return Opcodes.IF_ICMPEQ;
			case "~=": 
				return Opcodes.IF_ICMPNE;
			case "<":
				return Opcodes.IF_ICMPLT;
			case ">":
				return Opcodes.IF_ICMPGT;
			case "<=":
				return Opcodes.IF_ICMPLE;
			case ">=":
				return Opcodes.IF_ICMPGE;
			case "isnull":
				return Opcodes.IFNULL;
			case "~":
				return Opcodes.IFNE;
			}	
		} else {
			switch(tokenText) {
			case "=": 
				return Opcodes.IF_ACMPEQ;
			case "~=": 
				return Opcodes.IF_ACMPNE;
			case "isnull":
				return Opcodes.IFNULL;
			case "~":
				return Opcodes.IFNE;
			}	
		}
		
		throw new WoolException("Invalid use of a comparator for a given type");
	}
	
	@Override
	public String toString() {
		return super.toString() + "(" + tokenText + ")";
	}
}
