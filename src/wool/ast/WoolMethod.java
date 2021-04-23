package wool.ast;

import wool.symbol.bindings.MethodBinding;

public class WoolMethod extends ASTNode {
	
	private MethodBinding binding;

	public WoolMethod(MethodBinding binding) {
		super(ASTNodeType.nMethod);
		this.binding = binding;
	}

}
