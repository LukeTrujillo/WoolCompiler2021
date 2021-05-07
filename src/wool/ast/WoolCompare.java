package wool.ast;

import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

public class WoolCompare extends ASTNode {
	
	public int opcode;
	public String tokenText;

	public WoolCompare(Token token) {
		super(ASTNodeType.nCompare);
		this.token = token;
		
		tokenText = this.token.getText();
		
		switch(tokenText) {
		case "=": 
			opcode = Opcodes.IF_ICMPEQ;
			break;
		case "~=": 
			opcode = Opcodes.IF_ICMPNE;
			break;
		case "<":
			opcode = Opcodes.IF_ICMPLT;
			break;
		case ">":
			opcode = Opcodes.IF_ICMPGT;
			break;
		case "<=":
			opcode = Opcodes.IF_ICMPLE;
			break;
		case ">=":
			opcode = Opcodes.IF_ICMPGE;
			break;
		case "isnull":
			opcode = Opcodes.IFNULL;
			break;
		case "~":
			opcode = Opcodes.IFEQ;
			break;
		}
	}
	
	public WoolCompare(String token) {
		super(ASTNodeType.nCompare);
		this.token = null;
		
		
		switch(token) {
		case "=": 
			opcode = Opcodes.IF_ICMPEQ;
			break;
		case "~=": 
			opcode = Opcodes.IF_ICMPNE;
			break;
		case "<":
			opcode = Opcodes.IF_ICMPLT;
			break;
		case ">":
			opcode = Opcodes.IF_ICMPGT;
			break;
		case "<=":
			opcode = Opcodes.IF_ICMPLE;
			break;
		case ">=":
			opcode = Opcodes.IF_ICMPGE;
			break;
		case "isnull":
			opcode = Opcodes.IFNULL;
			break;
		case "~":
			opcode = Opcodes.IFNE;
			break;
		}
		tokenText = token;
	}
	
	@Override
	public String toString() {
		return super.toString() + "(" + tokenText + ")";
	}
}
