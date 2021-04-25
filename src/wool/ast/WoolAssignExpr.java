package wool.ast;

import wool.typechecking.TypeChecker;

public class WoolAssignExpr extends ASTNode {

	public WoolAssignExpr() {
		super(ASTNodeType.nAssign);
	}
}
