package wool.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolVisitor;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.MethodDescriptor;
import wool.symbol.tables.SymbolTable;
import wool.symbol.tables.TableManager;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.ExprContext;
import wool.lexparse.WoolParser.ExprListContext;
import wool.lexparse.WoolParser.FormalContext;
import wool.lexparse.WoolParser.FullMethodCallContext;
import wool.lexparse.WoolParser.IsNullExprContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.NewExprContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.VariableDefContext;

public class SymbolTableBuilder extends WoolBaseVisitor<AbstractBinding> {
	
	private int scope = 0;
	
	private TableManager manager;
	
	
	public SymbolTableBuilder() {
		this.manager = TableManager.getInstance();
	}

	@Override
	public AbstractBinding visitProgram(ProgramContext ctx) {
		visitChildren(ctx);
		return null;
	}
	
	@Override
	public AbstractBinding visitClassDef(ClassDefContext ctx) {
		
		
		String className = ctx.className.getText();
		String inherits = ctx.inherits == null? null : ctx.inherits.getText();
		
		System.out.println("enter visitClassDef(" + className + ")");
		
		ClassBinding classBinding;
		if(inherits == null) {
			classBinding = manager.makeNewClass(className, "Object");

		} else {
			classBinding = manager.makeNewClass(className, inherits);
		}
		
		/* 
		 * Lets add variables for everything in this scope first
		 */
		for(VariableDefContext variable : ctx.classBody().variables) {
			ObjectBinding binding = (ObjectBinding) this.visitVariableDef(variable);			
		}
	
		for(MethodContext method : ctx.classBody().methods) {
			
			
			
			MethodBinding binding = (MethodBinding) this.visitMethod(method);
			
		}
		
		manager.exitScopeInClass();
		
		System.out.println("exit visitClassDef(" + className + ")");
	
		return null;
	}
	

	@Override
	public AbstractBinding visitVariableDef(VariableDefContext ctx) {
		return manager.registerVariable(ctx);
	}
	
	@Override
	public AbstractBinding visitMethod(MethodContext ctx) {
		
		MethodBinding binding = manager.registerMethod(ctx);
	
		manager.enterScopeInClass();
		visitChildren(ctx);
		manager.exitScopeInClass();
		
		return binding;
	}
	
	
	
	

}
