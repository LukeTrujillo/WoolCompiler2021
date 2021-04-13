package wool.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolVisitor;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.descriptors.MethodDescriptor;
import wool.symbol.tables.SymbolTable;
import wool.symbol.tables.TableManager;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.ExprContext;
import wool.lexparse.WoolParser.FormalContext;
import wool.lexparse.WoolParser.FullMethodCallContext;
import wool.lexparse.WoolParser.IsNullExprContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.NewExprContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.VariableDefContext;

public class SymbolTableBuilder extends WoolBaseVisitor {
	
	private int scope = 0;
	
	private TableManager manager;
	
	
	public SymbolTableBuilder() {
		this.manager = TableManager.getInstance();
	}

	@Override
	public String visitProgram(ProgramContext ctx) {
		visitChildren(ctx);
		return null;
	}
	
	@Override
	public String visitClassDef(ClassDefContext ctx) {
		
		
		String className = ctx.className.getText();
		String inherits = ctx.className.getText();
		
		System.out.println("enter visitClassDef(" + className + ")");
		
		if(inherits == null) {
			manager.makeNewClass(className, "Object");
		} else {
			manager.makeNewClass(className, inherits);
		}
		
		manager.enterScopeInClass();
		
		visitChildren(ctx);
		
		manager.exitScopeInClass();
		
		System.out.println("exit visitClassDef(" + className + ")");
	
		return null;
	}
	

	@Override
	public String visitVariableDef(VariableDefContext ctx) {
		
		manager.newVariable(ctx.name.getText(), ctx.type.getText(), ctx.getStart());
		
		return null;
	}
	
	@Override
	public String visitMethod(MethodContext ctx) {
		
		String[] argTypes = new String[ctx.formal().size()];
			
		int index = 0;
		for(FormalContext fc : ctx.formals) {
			argTypes[index] = fc.type.getText();
			index = index + 1;
		}
		
		MethodDescriptor descriptor = new MethodDescriptor(ctx.methodName.getText(), ctx.type.getText(), argTypes);
		
		manager.newMethod(descriptor, ctx.getStart());
		
		manager.enterScopeInClass();
		visitChildren(ctx);
		manager.exitScopeInClass();
		
		return null;
	}
	

}
