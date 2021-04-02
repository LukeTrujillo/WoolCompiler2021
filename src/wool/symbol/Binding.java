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

import org.antlr.v4.runtime.Token;

/**
 * The interface for bindings. Different binding types will override methods differently.
 * @version Jul 27, 2018
 */
public interface Binding
{
    public static enum BindingType {bObject, bType, bMethod};
    public static final int NO_ADDRESS = Integer.MIN_VALUE;
    
    /**
     * @return this binding's binding type
     */
    BindingType getBindingType();
    
    /**
     * @return the string for the ID of the symbol. This is the type, object, or
     *  base method name.
     */
    String getSymbol();
    
    /**
     * The symbol type is the type/class associated with the symbol. For objects, it is
     * their type (e.g. Int or Str). For methods, it is the return type. For classes this 
     * is the same as the ID that is returned from getSymbol().
     * @see cool.symbol.Binding#getSymbol()
     * @return the symbol type
     */
    String getSymbolType();
    
    /**
     * @return the token associated with this symbol
     */
    Token getToken();
    
    
    /**
     * @return the name of the class where this symbol was defined
     */
    String getClassWhereDefined();
    
    /**
     * Set the class where this symbol was defined.
     * @param className
     */
    void setClassWhereDefined(String className);
    
    /**
     * If this binding has an associated class descriptor, return it. It will
     * be null for all but the class bindings.
     * @return the class descriptor for this binding or null if there is none
     */
    default ClassDescriptor getClassDescriptor() { return null; }
    
    /**
     * Set the class descriptor for this binding. This will only be applicable for
     * class bindings.
     * @param cd the class descriptor
     */
    default void setClassDescriptor(ClassDescriptor cd) { }
    
    /**
     * If this binding has an associated method descriptor, return it. It will
     * be null for all but the method bindings.
     * @return the method descriptor or null if there is none
     */
    default MethodDescriptor getMethodDescriptor() { return null; }
    
    /**
     * Set the method descriptor for this binding. This will only be applicable for
     * method bindings.
     * @param md the method descriptor
     */
    default void setMethodDescriptor(MethodDescriptor md) { }
    
    /**
     * @return the scope (the symbol table) where this binding is defined
     */
    default SymbolTable getScope() { return null; }
    
    /**
     * @return return the value of the address field
     */
    default int getAddress() { return NO_ADDRESS; }
    
    /**
     * Set the value of the address field
     * @param address
     */
    default void setAddress(int address) { }
}
