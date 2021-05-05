package wool.ast;

import wool.typechecking.TypeChecker;

public class WoolAssign extends ASTNode {

	public WoolAssign() {
		super(ASTNodeType.nAssign);
	}
	
	public ASTNode getIdentifer() { return children.get(0); }
	
	public ASTNode getSetValue() { return children.get(1); }
}
