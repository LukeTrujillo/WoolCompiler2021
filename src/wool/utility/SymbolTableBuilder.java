package wool.utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolVisitor;
import wool.symbol.BindingFactory.ClassBinding;
import wool.symbol.BindingFactory.MethodBinding;
import wool.symbol.ClassDescriptor;
import wool.symbol.MethodDescriptor;
import wool.symbol.SymbolTable;
import wool.symbol.TableManager;
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
	public String visitClassDef(ClassDefContext ctx) {
		System.out.println("start of visitClassDef()");
		if(manager.lookupClass(ctx.className.toString()) == null) { // this class has not been defined yet
			
			manager.StartNewClass(ctx.className.getText(), ctx.inherits != null? ctx.inherits.getText() : null);
			super.visitClassDef(ctx);
		}
		
		System.out.println("end of visitClassDef()");
		return null;
	}
	
	@Override
	public String visitClassBody(ClassBodyContext ctx) {
		super.visitClassBody(ctx);
		manager.exitScope();
		return null;
	}
	
	
	@Override
	public String visitVariableDef(VariableDefContext ctx) {
		
		if(manager.lookupIDInClass(ctx.name.getText(), manager.currentClassName) == null) { // the variable is not defined yet
			
			manager.newVariable(ctx.name.getText(), ctx.type.getText(), ctx.getStart());
			
		}
		
		return null;
	}
	
	
	
	@Override
	public String visitMethod(MethodContext ctx) {
		System.out.println("start of visitMethod()");
		if(manager.currentClassName != null && manager.lookupClass(manager.currentClassName) != null) { // the class is defined
			
			if(manager.lookupMethodInClass(ctx.methodName.getText(), manager.currentClassName) == null) { // the method has not been defined yet
					manager.enterScope();
				
					List<String> vargs = new ArrayList<String>();
					
					ctx.formals.stream().forEach((formal) -> vargs.add(formal.type.getText()));
					
					MethodDescriptor md = new MethodDescriptor(ctx.methodName.getText(), ctx.type.getText(),  vargs.toArray(new String[0]));
					MethodBinding mb = manager.newMethod(md, ctx.methodName);
					
					manager.currentTable.add(ctx.methodName.getText(), mb);
					
					super.visitMethod(ctx);
					manager.exitScope();
			} else {
				throw new WoolException("Method '" + ctx.methodName.getText() + "' already defined in class '" + manager.currentClassName + "'");
			}
		}
		
		System.out.println("end of visitMethod()");
		 
		return null;
	}
	

}
