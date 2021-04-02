package wool.symbol.descriptors;

import java.util.HashMap;

import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.utility.WoolException;

public class ClassDescriptor {

	public String className, inherits;

	private HashMap<String, MethodBinding> methods;
	private HashMap<String, ObjectBinding> variables;

	public ClassDescriptor(String className) {
		this(className, "Object");
	}

	public ClassDescriptor(String className, String inherits) {
		this.className = className;
		this.inherits = inherits;

		this.methods = new HashMap<String, MethodBinding>();
		this.variables = new HashMap<String, ObjectBinding>();
	}

	public void addVariable(ObjectBinding var) {
		if (variables.containsKey(var.getSymbol())) {
			throw new WoolException(
					"Attempt to add a duplicate variable: " + var.getSymbol() + " to class " + className);
		}
		variables.put(var.getSymbol(), var);
	}

	public void addMethod(MethodBinding method) {
		methods.put(method.getSymbol(), method);
	}
	
	public ObjectBinding getVariable(String id)
    {
        return variables.get(id);
    }
    
    /**
     * Get the method descriptor the a method in this Cool-W class.
     * @param methodName the name of the method
     * @return the MethodDescriptor for the method or null if the method is not declared in
     *  this class or in any of its inheritance 
     */
    public MethodDescriptor getMethodDescriptor(String methodName) {
        MethodBinding mb = methods.get(methodName);
        return mb == null ? null : mb.getMethodDescriptor();
    }
    
    /**
     * Get the binding for this method in this class.
     * @param methodName
     * @return the binding or null if it is not declared in this class.
     */
    public MethodBinding getMethodBinding(String methodName)
    {
        return methods.get(methodName);
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "[className=" + className + ", inherits=" + inherits
                + "\n      methods= {" + methodsString() + "}\n      variables=" + variables
                + "]";
    }
    
    /**
     * Used in printing the descriptor.
     * @return a string for all of the methods.
     */
    private String methodsString()
    {
        StringBuilder sb = new StringBuilder();
        for (String m : methods.keySet()) {
            sb.append("\n        ");
            sb.append(methods.get(m).toString());
        }
        return sb.toString();
    }

}
