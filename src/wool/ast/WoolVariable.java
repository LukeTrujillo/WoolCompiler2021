package wool.ast;

import wool.symbol.bindings.ObjectBinding;
import wool.typechecking.TypeChecker;

public class WoolVariable extends ASTNode {

	public ObjectBinding binding;
	
	public WoolVariable(ObjectBinding binding) {
		super(ASTNodeType.nVariable);

		this.binding = binding;
		this.nodeClass = binding.getSymbolType();
	}
	
	@Override
	protected String extraInfo() {
		return binding == null ? "" : (", binding: " + binding);
	}
	


}
