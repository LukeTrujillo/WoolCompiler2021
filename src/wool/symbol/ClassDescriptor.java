/*******************************************************************************
 * This file is used in CS4533/CS544, Compiler Construction & Techniques of
 * Language Translation, Worcester Polytechnic Institute.
 *
 * Copyright (c) 2016-18 Gary F. Pollice
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2016 Gary F. Pollice
 *******************************************************************************/

package wool.symbol;

import java.util.*;
import wool.symbol.BindingFactory.*;
import wool.utility.WoolException;

/**
 * The ClassDescriptor contains the key parts of class definitions so that we can
 * quickly determine if a reference to a class or part of a class is correct.
 * @version Jul 5, 2018
 */
public class ClassDescriptor
{
    public String className;
    public String inherits;
    Map<String, MethodBinding> methods;  // <method name, descriptor>
    Map<String, ObjectBinding> variables;          // <id, className>
    
    /**
     * Constructs a new class descriptor for the named class. By default, the class
     * inherits from Object.
     */
    public ClassDescriptor(String className)
    {
        this(className, "Object");
    }
    
    /**
     * Constructor that explicitly indicates the class the new class inherits
     * from.
     * @param className
     * @param inherits
     */
    public ClassDescriptor(String className, String inherits)
    {
        this.className = className;
        this.inherits = inherits;
        methods = new HashMap<String, MethodBinding>();
        variables = new HashMap<String, ObjectBinding>();
    }
    
    /**
     * Add a variable to the class descriptor.
     * @param var the variable binding
     * @throws CoolException if the variable has already been declared as a
     *  feature of the class or any of it's inherited classes.
     */
    public void addVariable(ObjectBinding var)
    {
        // TODO: Check for inherited definitions too.
        if (variables.containsKey(var.symbol)) {
            throw new WoolException("Attempt to add a duplicate variable: " + var.symbol
                + " to class " + className);
        }
        variables.put(var.symbol,  var);
    }

    /**
     * Add a method to the class descriptor.
     * @param methodName
     * @param descriptor the method descriptor
     * @throws CoolException if the method has already been declared in this
     *  class or has been declared in an inherited class, but does not have the
     *  same signature. See section 6 of the Cool-W manual.
     */
    public void addMethod(MethodBinding method)
    {
        methods.put(method.symbol, method);
    }
    
    /**
     * Get the ObjectBinding for the specified variable
     * @return the binding or null if the variable has not been declared
     * 
     */
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
    public MethodDescriptor getMethodDescriptor(String methodName)
    {
        MethodBinding mb = methods.get(methodName);
        return mb == null ? null : mb.md;
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
