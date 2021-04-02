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
import static wool.symbol.Binding.BindingType.*;

/**
 * Abstract base class for all bindings.
 * @version Jul 27, 2018
 */
public abstract class AbstractBinding implements Binding
{
    public final String symbol;
    public final String symbolType;
    public final BindingType bindingType;
    public Token token;
    public String classWhereDefined;
    public int address;

    /**
     * Default constructor
     * @param symbol the name of the symbol
     * @param symbolType the type (class) associated with the binding
     * @param bindingType identifes the binding type
     * @param t the token associated with this binding
     */
    public AbstractBinding(String symbol, String symbolType, 
            BindingType bindingType, Token t)
    {
        this.symbol = symbol;
        this.symbolType = symbolType;
        this.bindingType = bindingType;
        this.token = t;
        classWhereDefined = null;
        address = NO_ADDRESS;
    }

    /*
     * @see cool.symbol.Binding#getSymbol()
     */
    @Override
    public String getSymbol()
    {
        return symbol;
    }

    /*
     * @see cool.symbol.Binding#getSymbolType()
     */
    @Override
    public String getSymbolType()
    {
        return symbolType;
    }

    /*
     * @see cool.symbol.Binding#getBindingType()
     */
    @Override
    public BindingType getBindingType()
    {
        return bindingType;
    }

    /*
     * @see cool.symbol.Binding#getToken()
     */
    @Override
    public Token getToken() 
    {
        return token;
    }
    
    /*
     * @see cool.symbol.Binding#getClassWhereDefined()
     */
    @Override
    public String getClassWhereDefined()
    {
        return classWhereDefined;
    }
    
    /*
     * @see cool.symbol.Binding#setClassWhereDefined(java.lang.String)
     */
    @Override
    public void setClassWhereDefined(String className)
    {
        classWhereDefined = className;
    }
    
    /**
     * Use this for printing the binding.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Binding [" + bindingType);
        sb.append(", " + symbol + " : " + symbolType);
        if (token != null) {
            sb.append(" (" + token.getCharPositionInLine() + "," + token.getLine() + ")");
        }
        if (classWhereDefined != null) {
            sb.append(", defined in " + classWhereDefined);
        }
        switch (bindingType) {
            case bObject:
                if (address != NO_ADDRESS) {
                    sb.append(", address=" + address);
                }
                break;
            case bType:
                sb.append('\n');
                sb.append("    descriptor:" + extraInfo());
                break;
            case bMethod:
                sb.append(",   descriptor: " + extraInfo());
                break;
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * @return any extra information that is useful in printing
     */
    protected String extraInfo()
    {
        return "";
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
                + ((bindingType == null) ? 0 : bindingType.hashCode());
        result = prime * result
                + ((classWhereDefined == null) ? 0 : classWhereDefined.hashCode());
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((symbolType == null) ? 0 : symbolType.hashCode());
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
        if (!(obj instanceof AbstractBinding)) {
            return false;
        }
        AbstractBinding other = (AbstractBinding) obj;
        if (bindingType != other.bindingType) {
            return false;
        }
        if (classWhereDefined == null) {
            if (other.classWhereDefined != null) {
                return false;
            }
        } else if (!classWhereDefined.equals(other.classWhereDefined)) {
            return false;
        }
        if (!symbol.equals(other.symbol)) {
            return false;
        }
        if (symbolType == null) {
            if (other.symbolType != null) {
                return false;
            }
        } else if (!symbolType.equals(other.symbolType)) {
            return false;
        }
        return true;
    }
}
