package wool.ast;

import java.util.ArrayList;
import java.util.List;

import wool.ast.WoolTerminal.TerminalType;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.BindingType;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.typechecking.TypeChecker;

public class WoolMethodCall extends ASTNode {
	
	public static enum DispatchType { mcLocal, mcObject};
	
	public DispatchType dispatch;
	
	public String methodName;
	
	public ClassBinding calledIn;
	
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


	public String getCallerType() {
		MethodBinding mb = (MethodBinding) binding;
		
		if(dispatch == DispatchType.mcLocal) {
			return mb.getClassWhereDefined();
		} else { //the caller type can be nested in the 
			
			ASTNode node = getObject();
			
			if(node instanceof WoolTerminal) {
				return node.binding.getSymbolType();
			}
			

			AbstractBinding ab = node.binding;
			
			if(ab != null) {
				if(ab.getBindingType() == BindingType.METHOD) {
					if(!((MethodBinding) ab).getMethodDescriptor().getReturnType().contentEquals("SELF_TYPE")) {
						return ((MethodBinding) ab).getMethodDescriptor().getReturnType();
					}
				}
				
				if(ab.getBindingType() == BindingType.OBJECT) {
					return ab.getSymbolType();
				}
			}
			
			if(node instanceof WoolMethodCall) {
				return ((WoolMethodCall) node).getCallerType();
			}
				
			
		}
		return null;
	}
}
