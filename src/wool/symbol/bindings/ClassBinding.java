package wool.symbol.bindings;


import wool.symbol.descriptors.ClassDescriptor;

public class ClassBinding extends AbstractBinding {
	
	private ClassDescriptor descriptor;

	public ClassBinding(ClassDescriptor cd) {
		super(cd.className, cd.className,BindingType.TYPE, null);
		this.descriptor = cd;
	}
	
	public ClassDescriptor getClassDescriptor() {
		return this.descriptor;
	}
	
	public String extraInfo() { return descriptor.toString(); }

	
	public static ClassBinding makeClassBinding(ClassDescriptor cd) {
        return new ClassBinding(cd);
    }

}
