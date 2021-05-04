package wool.ast;

import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.typechecking.TypeChecker;

public class WoolMethod extends ASTNode {
	
	public MethodBinding binding;

	public WoolMethod(MethodBinding binding) {
		super(ASTNodeType.nMethod);
		this.binding = binding;
		super.binding = binding;
	}
	
	@Override
	public String printNicely() { 
		
		if(this.binding.getSymbolType() != null)
			return getTabsForDepth() + ((MethodBinding) binding).getMethodDescriptor().methodName + "(children: " + this.children.size() + ") : " + this.binding.getSymbolType();
		else 
			return getTabsForDepth() + ((MethodBinding) binding).getMethodDescriptor().methodName + "(children: " + this.children.size() + ") : void";
	}
	
	public int argumentDefinedHereAtChildIndex(String name) {
		int x = 0;
		for(ASTNode child : this.children) {
				if(child instanceof WoolVariable) {
					WoolVariable arg = (WoolVariable) child;
					String symbol = arg.binding.symbol;
					if(symbol.equals(name)) {
						return x;
					}
					
				}
			x++;
		}
		return -1;
	}

}
