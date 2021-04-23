package wool.ast;

import wool.symbol.bindings.ClassBinding;

public class WoolType extends ASTNode {
	
	protected ClassBinding binding; 
	
	public WoolType(ClassBinding binding) {
		super(ASTNodeType.nType); //parent would be WoolProgram
		
		this.binding = binding;
		this.nodeClass = binding.getClassDescriptor().className;
	}
	
	@Override
	protected String extraInfo() { return ", inherits: " + binding.getClassDescriptor().inherits; }

}
