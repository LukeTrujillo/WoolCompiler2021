/*******************************************************************************
 * This files was developed for CS4533/CS544: 
 *     Techniques of Program Translation/Compiler Construction.
 * The course was taken at Worcester Polytechnic Institute.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2018 Gary F. Pollice
 *******************************************************************************/

package wool.symbol;

import java.util.*;
import wool.utility.WoolException;

/**
 * The symbol table is the data structure representing a single scope. The
 * SymbolTableManager manages the symbol tables and their relationships to
 * each other.
 * 
 * @see cool.symbol.SymbolTableManager
 * @version Jul 2, 2018
 */
public class SymbolTable
{
    public static int nextTableNumber = 0;
    public Map<String, Binding> elements;
    protected SymbolTable parent;
    public final int scopeLevel;
    public int tableNumber;     // Just for identification
    
    /**
     * Default constructor
     * @param parent the enclosing scope's symbol table or null if this is top level
     */
    public SymbolTable(SymbolTable parent)
    {
        this.parent = parent;
        scopeLevel = parent == null ? 1 : parent.scopeLevel + 1;
        elements = new HashMap<String,Binding>();
        tableNumber = nextTableNumber++;
    }
    
    /**
     * Add an element to the table. If the element already exists in this table, then
     * an exception is thrown.
     * @param key
     * @param value
     * @return the value added
     * @throws CoolException
     */
    public Binding add(String key, Binding value)
    {
        if (elements.containsKey(key)) {
            throw new WoolException("Attempt to add a duplicate " + value.getClass().getName() 
                    + " with key: " + key + " to " + this.getClass().getName());
        }
        elements.put(key, value);
        return value;
    }
    
    /**
     * Find the element that corresponds to the key in this table or any parent tables in
     * the chain. Return the first one encountered, starting with this table.
     * @param key
     * @return the first value corresponding to the key or null if there is no such value
     */
    public Binding lookup(String key)
    {
        Binding result = null;
        result = elements.get(key);
        if (result == null && parent != null) {
            result = parent.lookup(key);
        }
        return result;
    }
    
    /**
     * Tests for the existence of a specific binding in this table only.
     * @param key the binding's key string
     * @return the binding or null if it is not there
     */
    public Binding probe(String key)
    {
        return elements.get(key);
    }
    
    /**
     * @return the parent
     */
    public SymbolTable getParent()
    {
        return parent;
    }
    
    @Override
    public String toString()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("  parent scope level: " 
    		+ (parent == null ? "none" : parent.scopeLevel) + '\n');

        sb.append("  scopeLevel: " + scopeLevel + '\n');
    	sb.append("  elements: \n");
    	for (Binding b : elements.values()) {
    	    sb.append("  ");
    	    sb.append(b.toString());
    	}
    	return sb.toString();
    }
}
