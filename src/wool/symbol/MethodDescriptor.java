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

import java.util.LinkedList;

/**
 * The MethodDescriptor describes the properties of a method needed to do type
 * checking and code generation.
 * @version Jul 5, 2018
 */
public class MethodDescriptor
{
    public String returnType;                  // type this method returns
    public LinkedList<String> argumentTypes;   // the class of each argument in order.
    public String methodName;
    
    /**
     * Constructor requires the methodName.
     */
    public MethodDescriptor(String methodName, String returnType)
    {
        this.methodName = methodName;
        this.returnType = returnType;
        argumentTypes = new LinkedList<String>();
    }
    
    /**
     * Constructor that takes a list of argument types. Useful for constructing
     * the initial class descriptors for the initial environment.
     * @param methodName
     * @param returnType
     * @param arguments array of argument types
     */
    public MethodDescriptor(String methodName, String returnType, String ... arguments)
    {
        this.methodName = methodName;
        this.returnType = returnType;
        argumentTypes = new LinkedList<String>();
        for (String arg : arguments) {
            addArgumentType(arg);
        }
    }
    
    /**
     * Add the type of the next argument to this method's arguments.
     * @param type
     */
    public void addArgumentType(String type)
    {
        argumentTypes.add(type);
    }

    /**
     * @return the returnType
     */
    public String getReturnType()
    {
        return returnType;
    }

    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(String returnType)
    {
        this.returnType = returnType;
    }

    /**
     * @return the methodName
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * @param methodName the methodName to set
     */
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }

    /**
     * @return the argumentTypes
     */
    public LinkedList<String> getArgumentTypes()
    {
        return argumentTypes;
    }

    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argumentTypes == null) ? 0 : argumentTypes.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        return result;
    }

    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor other = (MethodDescriptor) obj;
        if (argumentTypes == null) {
            if (other.argumentTypes != null) {
                return false;
            }
        } else if (!argumentTypes.equals(other.argumentTypes)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        if (returnType == null) {
            if (other.returnType != null) {
                return false;
            }
        } else if (!returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(methodName + "(");
        for (String at : argumentTypes) {
            sb.append(" " + at);
        }
        sb.append(" ) : " + returnType);
        return sb.toString();
    }
}
