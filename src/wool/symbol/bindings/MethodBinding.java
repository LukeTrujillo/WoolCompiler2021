package wool.symbol.bindings;

import org.antlr.v4.runtime.Token;

import wool.symbol.descriptors.MethodDescriptor;

public class MethodBinding extends AbstractBinding {
	
	
	private MethodDescriptor md;

	public MethodBinding(MethodDescriptor md, Token t) {
		super(md.getMethodName(), md.getReturnType(), BindingType.METHOD, t);
		
		this.md = md;
	}
	
	
	public MethodDescriptor getMethodDescriptor() { return md; }

	/*
     * @see cool.symbol.AbstractBinding#extraInfo()
     */
    @Override
    public String extraInfo()
    {
        return md == null ? "[]" : md.toString();
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((md == null) ? 0 : md.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof MethodBinding)) {
            return false;
        }
        MethodBinding other = (MethodBinding) obj;
        if (md == null) {
            if (other.md != null) {
                return false;
            }
        } else if (!md.equals(other.md)) {
            return false;
        }
        return true;
    }
    
    public static MethodBinding makeMethodBinding(MethodDescriptor md, Token t,
            String className) {
        MethodBinding mb = new MethodBinding(md, t);
        mb.classWhereDefined = className;
        return mb;
    }
    
}
