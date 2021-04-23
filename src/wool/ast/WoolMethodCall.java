package wool.ast;

import wool.symbol.bindings.AbstractBinding;

public class WoolMethodCall extends ASTNode {
	
	public static enum DispatchType { mcLocal, mcObject};
	
	public DispatchType dispatch;
	public ASTNode object;
	public ASTNode methodName;
	
	public WoolMethodCall(DispatchType dispatch) {
		super();
		this.nodeType = ASTNodeType.nMethodCall;
		this.dispatch = dispatch;
		object = methodName = null;
	}
	
	public ASTNode getObject() { return dispatch == DispatchType.mcLocal ? null : this.getChild(0); }

	public ASTNode getMethodName() {
		return dispatch == DispatchType.mcLocal ? getChild(0) : getChild(1);
	}

	

}
