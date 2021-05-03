package wool.symbol.tables;

import java.util.ArrayList;

import org.antlr.v4.runtime.CommonTokenFactory;
import org.antlr.v4.runtime.Token;

import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.FormalContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.VariableDefContext;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.ClassDescriptor;
import wool.symbol.descriptors.MethodDescriptor;
import wool.utility.WoolException;

public class TableManager {

	private ArrayList<SymbolTable> tables;
	private static TableManager manager = new TableManager();
	private CommonTokenFactory tokenFactory = (CommonTokenFactory) CommonTokenFactory.DEFAULT;
	private Token selfToken;

	private SymbolTable currentTable, classTable;

	private ClassBinding currentClassBinding;
	private String currentClassName;

	private TableManager() {
		this.tables = new ArrayList<SymbolTable>();

		this.selfToken = tokenFactory.create(wool.lexparse.WoolLexer.ID, "self");

		this.currentClassBinding = null;
		this.currentTable = null;
		this.classTable = new SymbolTable(null);
		
		this.currentClassName = null;
		
		this.initBaseClasses();
	}

	public static TableManager getInstance() {
		return manager;
	}

	private void initBaseClasses() {

		/*
		 * Make the Object base class
		 */
		ClassDescriptor cd = new ClassDescriptor("Object", null);
		MethodDescriptor md = new MethodDescriptor("abort", "Object");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Object"));
		md = new MethodDescriptor("typeName", "Str");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Object"));
		md = new MethodDescriptor("copy", "SELF_TYPE");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Object"));
		cd.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));

		getClassTable().add("Object", ClassBinding.makeClassBinding(cd));

		/*
		 * Make the IO base class
		 */

		cd = new ClassDescriptor("IO");
		md = new MethodDescriptor("outInt", "SELF_TYPE", "Int");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		md = new MethodDescriptor("outStr", "SELF_TYPE", "Str");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		md = new MethodDescriptor("outBool", "SELF_TYPE", "Bool");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		md = new MethodDescriptor("inInt", "Int");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		md = new MethodDescriptor("inStr", "Str");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		md = new MethodDescriptor("inBool", "Bool");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "IO"));
		cd.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));

		getClassTable().add("IO", ClassBinding.makeClassBinding(cd));

		/*
		 * Make Str class
		 */

		cd = new ClassDescriptor("Str");
		md = new MethodDescriptor("length", "Int");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Str"));
		md = new MethodDescriptor("concat", "Str", "Str");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Str"));
		md = new MethodDescriptor("substr", "Str", "Int", "Int");

		cd.addMethod(MethodBinding.makeMethodBinding(md, null, "Str"));
		cd.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));

		getClassTable().add("Str", ClassBinding.makeClassBinding(cd));

		/*
		 * Make Int class
		 */
		cd = new ClassDescriptor("Int");
		cd.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));
		getClassTable().add("Int", ClassBinding.makeClassBinding(cd));

		/*
		 * Make Bool class
		 */
		cd = new ClassDescriptor("Bool");
		cd.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));
		getClassTable().add("Bool", ClassBinding.makeClassBinding(cd));

		// SELF_TYPE
		getClassTable().add("SELF_TYPE", ClassBinding.makeClassBinding(new ClassDescriptor("SELF_TYPE")));
	}

	public ClassBinding makeNewClass(String className, String inherits) {
		if (getClassTable().probe(className) != null) {
			throw new WoolException("Attempt to redefine class: " + className);
		}
		if (getCurrentTable() == getClassTable()) {
			throw new WoolException("Starting a new class: " + className
					+ " and there is still a symbol table that has not been closed");
		}

		ClassDescriptor descriptor = new ClassDescriptor(className, inherits == null ? "Object" : inherits);
		descriptor.addVariable(ObjectBinding.makeObjectBinding("self", "SELF_TYPE", selfToken));
		
		
		descriptor.className = className;
		descriptor.inherits = inherits;
		
		ClassBinding binding = ClassBinding.makeClassBinding(descriptor);

		getClassTable().add(className, binding);

		currentTable = new SymbolTable(currentTable, className);
		currentClassBinding = binding;

		tables.add(currentTable);
	
		return binding;
	}


	
	public MethodBinding registerMethod(MethodContext ctx) {
		String methodName = ctx.methodName.getText();
		String typeName = ctx.typeName().getText();
		
		String[] argTypes = new String[ctx.formal().size()];
	
	
		if (currentClassBinding.getClassDescriptor().getMethodBinding(methodName) != null) {
			throw new WoolException(
					"Method " + methodName + " has already been defined in class " + currentClassName);
		}
		
		MethodDescriptor md = new MethodDescriptor(methodName, typeName);
		
		MethodBinding mb = MethodBinding.makeMethodBinding(md, ctx.methodName, currentClassName);
		mb.setClassWhereDefined(currentClassName);
		currentClassBinding.getClassDescriptor().addMethod(mb);
		
		currentTable.add(methodName, mb);
		
		return mb;
	}
	
	public ObjectBinding registerVariable(FormalContext ctx) {
		String name = ctx.name.getText();
		String type = ctx.typeName().getText();
		
		if (currentTable.getElements().containsKey(name)) { // or in lower scopes too
            throw new WoolException("Attempt to redefine variable " + name);
        }
		
		ObjectBinding b = ObjectBinding.makeObjectBinding(name, type, ctx.name);
		b.setClassWhereDefined(currentClassName);
	    currentTable.add(name, b);
	    
	    currentClassBinding.getClassDescriptor().addVariable(b);
	    
	    return b;
	}
	
		
	public String getCurrentClassName() {
		return this.getCurrentClassName();
	}
	
	public void enterScopeInClass() {
		currentTable = new SymbolTable(currentTable);
		tables.add(currentTable);
		
	}
	
	public void exitScopeInClass() {
		currentTable = currentTable.getParentTable();
	}

	public SymbolTable getClassTable() {
		return classTable;
	}

	public SymbolTable getCurrentTable() {
		return currentTable;
	}
	
	public ClassBinding getClassBindingFromString(String className) {
		AbstractBinding binding = getClassTable().lookup(className);
		
		if(binding == null || !(binding instanceof ClassBinding)) throw new WoolException("class name lookup for " + className + " not found");
		
		return (ClassBinding) binding;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        sb.append("\n-----------------------------------------------");
        sb.append("Class table:\n");
        sb.append(classTable.toString());
        sb.append("\n-----------------------------------------------");
        for (SymbolTable st : tables) {
            sb.append("\nSymbol table " + st.getTableNumber() + "\n");
            sb.append(st.toString() + "\n");
        }
        sb.append("\n-----------------------------------------------");
        return sb.toString();
	}



}
