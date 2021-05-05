package wool.ast;

import org.antlr.v4.runtime.Token;

public class WoolMath extends ASTNode {
	
	public WoolMath(Token op) {
		super(ASTNodeType.nMath);
		this.token = op;
	}

	@Override
	public String toString() {
		return super.toString() + "(" + this.token.getText() + ")";
	}

	
}
