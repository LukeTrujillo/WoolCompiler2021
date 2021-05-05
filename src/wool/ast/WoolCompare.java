package wool.ast;

import org.antlr.v4.runtime.Token;
import org.objectweb.asm.Opcodes;

public class WoolCompare extends ASTNode {
	
	public int opcode;

	public WoolCompare(Token token) {
		super(ASTNodeType.nCompare);
		this.token = token;
		
		
		switch(this.token.getText()) {
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
		}
	}
	
	@Override
	public String toString() {
		return super.toString() + "(" + token.getText() + ")";
	}
}
