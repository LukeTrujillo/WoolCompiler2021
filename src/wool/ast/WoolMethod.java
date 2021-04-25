package wool.ast;

import wool.symbol.bindings.MethodBinding;
import wool.typechecking.TypeChecker;

public class WoolMethod extends ASTNode {
	
	public MethodBinding binding;

	public WoolMethod(MethodBinding binding) {
		super(ASTNodeType.nMethod);
		this.binding = binding;
	}

}
