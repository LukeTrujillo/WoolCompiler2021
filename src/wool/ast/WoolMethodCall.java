package wool.ast;

import java.util.ArrayList;
import java.util.List;

import wool.symbol.bindings.AbstractBinding;
import wool.typechecking.TypeChecker;

public class WoolMethodCall extends ASTNode {
	
	public static enum DispatchType { mcLocal, mcObject};
	
	public DispatchType dispatch;
	
	public String methodName;

	
	public WoolMethodCall(DispatchType dispatch) {
		super();
		this.nodeType = ASTNodeType.nMethodCall;
		this.dispatch = dispatch;
	}
	
	public void setObject(ASTNode node) { children.add(0, node); }
	public void setMethodName(ASTNode node) { 
		if(dispatch == DispatchType.mcLocal) {
			children.add(0, node);
		} else {
			children.add(1, node);
		}
	}

	public ASTNode getObject() { return dispatch == DispatchType.mcLocal ? null : this.getChild(0); }


	/*@Override
	public String toString() { return getTabsForDepth() + nodeType.name() + "(calling: " + getMethodName().binding.getSymbol() + " children: " + children.size() + ")"; }*/
}
