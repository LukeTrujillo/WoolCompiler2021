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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.CommonTokenFactory;
import wool.symbol.BindingFactory.*;
import wool.utility.WoolException;
import static wool.symbol.BindingFactory.*;
import static wool.symbol.Binding.BindingType.*;

/**
 * This class manages the symbol tables, the class tables and methods in the base
 * environment. It is a singleton.
 * @version Jul 27, 2018
 */
public class TableManager
{
    public SymbolTable classTable;
    public String currentClassName;
    public ClassBinding currentClassBinding;
    public SymbolTable currentTable;
    public CommonTokenFactory tokenFactory = (CommonTokenFactory) CommonTokenFactory.DEFAULT;
    // Self token is put in every class.
    public Token selfToken;
    
    private List<SymbolTable> tables;   // the symbol tbles
    
    private static TableManager tm = new TableManager();
    
    /**
     * Default constructor.
     */
    private TableManager()
    {
        tables = new ArrayList<SymbolTable>();
        initializeBaseClasses();
        currentClassName = null;
        currentClassBinding = null;
        selfToken = tokenFactory.create(wool.lexparse.WoolLexer.ID, "self");
    }
    
    /**
     * @return the instance of the table manager.
     */
    public static TableManager getInstance()
    {
        return tm;
    }
    
    /**
     * The class table is initialized with the base classes.
     */
    private void initializeBaseClasses()
    {
        // Object
        classTable = new SymbolTable(null);
        ClassDescriptor cd = new ClassDescriptor("Object", null);
        MethodDescriptor md = new MethodDescriptor("abort", "Object");
        cd.addMethod(makeMethodBinding(md, null, "Object"));
        md = new MethodDescriptor("typeName", "Str");
        cd.addMethod(makeMethodBinding(md, null, "Object"));
        md = new MethodDescriptor("copy", "SELF_TYPE");
        cd.addMethod(makeMethodBinding(md, null, "Object"));
        cd.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        classTable.add("Object", makeClassBinding(cd));
        // IO
        cd = new ClassDescriptor("IO");
        md = new MethodDescriptor("outInt", "SELF_TYPE", "Int");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        md = new MethodDescriptor("outStr", "SELF_TYPE", "Str");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        md = new MethodDescriptor("outBool", "SELF_TYPE", "Bool");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        md = new MethodDescriptor("inInt", "Int");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        md = new MethodDescriptor("inStr", "Str");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        md = new MethodDescriptor("inBool", "Bool");
        cd.addMethod(makeMethodBinding(md, null, "IO"));
        cd.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        classTable.add("IO", makeClassBinding(cd));
        // Str
        cd = new ClassDescriptor("Str");
        md = new MethodDescriptor("length", "Int");
        cd.addMethod(makeMethodBinding(md, null, "Str"));
        md = new MethodDescriptor("concat", "Str", "Str");
        cd.addMethod(makeMethodBinding(md, null, "Str"));
        md = new MethodDescriptor("substr", "Str", "Int", "Int");
        cd.addMethod(makeMethodBinding(md, null, "Str"));
        cd.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        classTable.add("Str",  makeClassBinding(cd));
        // Int
        cd = new ClassDescriptor("Int");
        cd.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        classTable.add("Int", makeClassBinding(cd));
        // Bool
        cd = new ClassDescriptor("Bool");
        cd.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        classTable.add("Bool", makeClassBinding(cd));
        // SELF_TYPE
        classTable.add("SELF_TYPE", makeClassBinding(new ClassDescriptor("SELF_TYPE")));
        
        //exitScope();
    }
    
    /**
     * We are starting to work on a new class. We need to start a new
     * top-level symbol table for it.
     * @param className
     * @param inherits
     * @return the class binding for the class
     * @throws CoolException if there is a duplicate class or a duplicate class
     *  name with different inherits.
     */
    public ClassBinding StartNewClass(String className, String inherits)
    {
        if (classTable.probe(className) != null) {
            throw new WoolException("Attempt to redefine class: " + className);
        }
        if (currentTable != null) {
            throw new WoolException("Starting a new class: " + className
                + " and there is still a symbol table that has not been closed");
        }
        ClassDescriptor descriptor =
            new ClassDescriptor(className, inherits == null ? "Object" : inherits);
        descriptor.addVariable(makeObjectBinding("self", "SELF_TYPE", selfToken));
        ClassBinding binding = makeClassBinding(descriptor);
        classTable.add(className, binding);
        currentTable = new SymbolTable(null);
        currentClassName = className;
        currentClassBinding = binding;
        tables.add(currentTable);
        return binding;
    }
    
    /**
     * We have encountered a new method definition. We need to enter it into the 
     * class descriptor for the current class.
     * @param md the method descriptor
     * @param t the token for the method name
     * @return the method binding
     */
    public MethodBinding newMethod(MethodDescriptor md, Token t)
    {
        if (currentClassBinding.descriptor.getMethodBinding(md.methodName) != null) {
            throw new WoolException("Method " + md.methodName + 
                " has already been defined in class " + currentClassName);
        }
        MethodBinding mb = makeMethodBinding(md, t, currentClassName);
        currentClassBinding.descriptor.addMethod(mb);
        return mb;
    }
    
    /**
     * We have a new variable definition that must go into the current class table. If
     * this variable is an actual feature definition for a class, then the AST creator
     * is responsible for putting it into the appropriate method descriptor.
     * @param varName the variable name
     * @param varType the variable's type
     * @param t the tokenwhere the variable is found
     * @return the variable binding
     */
    public ObjectBinding newVariable(String varName, String varType, Token t)
    {
        if (currentTable.elements.containsKey(varName)) {
            throw new WoolException("Attempt to redefine variable " + varName);
        }
        ObjectBinding b = makeObjectBinding(varName, varType, t);
        currentTable.add(varName, b);
        return b;
    }
    
    /**
     * Enter a new scope. Creates a new current symbol table.
     */
    public void enterScope()
    {
        currentTable = new SymbolTable(currentTable);
        tables.add(currentTable);
    }
    
    /**
     * Exits the scope and restores the parent of the current symbol table to
     * be the new current symbol table.
     */
    public void exitScope()
    {
        currentTable = currentTable.getParent();
    }
    
    /**
     * Search for a binding for an ID. If it's not in the symbol table hierarchy
     * then look in the class hierarchy
     * @param id the identifier
     * @param className the name of the class where this identifier was encountered
     * @param table the symbol table to start in
     * @return the binding or null if there is none.
     */
    public ObjectBinding lookupID(String id, String className, SymbolTable table)
    {
        Binding result = null;
        result = table.lookup(id);
        if (result == null) {   // See if it's in the class hierarchy
            result = lookupIDInClass(id, className);
        }
        if (result != null && result.getBindingType() != bObject) {
            throw new WoolException("Binding for non-object found in the symbol table "
                + result.toString());
        }
        return (ObjectBinding)result;
    }
    
    /**
     * Search for an object binding for an identifier, given a specific class.
     * If there is no binding for the variable in the specified class, look up the
     * class hierarchy until one is found or you reach the top of the hierarchy.
     * The identifer must be an object defined as a feature in the class.
     * @param id the identifier to search for
     * @param className the starting class
     * @return the binding or null if there is none
     */
    public ObjectBinding lookupIDInClass(String id, String className)
    {
        ClassDescriptor cd = lookupClass(className).getClassDescriptor();
        ObjectBinding ob = cd.getVariable(id);
        if (ob == null) {
            if (cd.inherits == null) {
                return null;
            } else {
                return lookupIDInClass(id, cd.inherits);
            }
        }
        return ob;
    }
    
    /**
     * Search for a method binding for a nethod, given a starting class. If the binding
     * is not found in the specified class, then search up the class hierarchy.
     * @param methodName the method name
     * @param className the starting class
     * @return the binding if it is found, null if there is no such binding
     */
    public MethodBinding lookupMethodInClass(String methodName, String className)
    {
        ClassDescriptor cd = lookupClass(className).getClassDescriptor();
        MethodBinding mb = cd.getMethodBinding(methodName);
        if (mb == null) {
            if(cd.inherits == null) {
                return null;
            } else {
                return lookupMethodInClass(methodName, cd.inherits);
            }
        }
        return mb;
    }
    
    /**
     * Lookup a class in the class table.
     * @param className the class to find
     * @return the class binding or null if the class has not been defined
     */
    public ClassBinding lookupClass(String className)
    {
        return (ClassBinding)classTable.lookup(className);
    }
    
    /**
     * @return the scope level of the current symbol table
     */
    public int currentScopeLevel()
    {
        return currentTable.scopeLevel;
    }

    /**
     * Get the list of tables that have been defined. This is typically used
     * for validating that the tables are correct and consistent.
     * @return the list of tables
     */
    public List<SymbolTable> getTables()
    {
        return tables;
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("\n-----------------------------------------------");
        sb.append("Class table:\n");
        sb.append(classTable.toString());
        sb.append("\n-----------------------------------------------");
        for (SymbolTable st : tables) {
            sb.append("\nSymbol table " + st.tableNumber++ + "\n");
            sb.append(st.toString() + "\n");
        }
        sb.append("\n-----------------------------------------------");
        return sb.toString();
    }
    
    /**
     * Create a new instance of the symbol table manager. Mainly for testing purposes.
     */
    public static void reset()
    {
        SymbolTable.nextTableNumber = 0;
        tm = new TableManager();
    }

    /**
     * @return the currentTable
     */
    public SymbolTable getCurrentTable()
    {
        return currentTable;
    }
    
}
