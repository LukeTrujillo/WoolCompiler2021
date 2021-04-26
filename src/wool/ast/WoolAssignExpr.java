package wool.ast;

import wool.typechecking.TypeChecker;

public class WoolAssignExpr extends ASTNode {

	public WoolAssignExpr() {
		super(ASTNodeType.nAssign);
	}
	
	public ASTNode getIdentifer() { return children.get(0); }
	
	public ASTNode getSetValue() { return children.get(1); }
}
