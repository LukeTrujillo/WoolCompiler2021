package wool.ast;

import wool.ast.WoolMethodCall.DispatchType;
import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolParser.AssignExprContext;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.ExprContext;
import wool.lexparse.WoolParser.ExprListContext;
import wool.lexparse.WoolParser.FullMethodCallContext;
import wool.lexparse.WoolParser.IfExprContext;
import wool.lexparse.WoolParser.LocalMethodCallContext;

import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.VariableDefContext;
import wool.lexparse.WoolParser.WhileExprContext;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.tables.TableManager;
import wool.utility.WoolFactory;

public class ASTBuilder extends WoolBaseVisitor<ASTNode> {
	
	private TableManager tm;
	
	public ASTBuilder() {
		this.tm = TableManager.getInstance();
	}
	
	@Override
	public ASTNode visitProgram(ProgramContext ctx) {
		WoolProgram program = ASTFactory.makeWoolProgram();
		
		for(ClassDefContext t : ctx.classes) {
			ASTNode type = t.accept(this);
			program.addChildAndSetAsParent(type); //also sets the parent
		}
		
		return program;
	}
	
	@Override
	public ASTNode visitClassDef(ClassDefContext ctx) {
		String className = ctx.className.getText();
		String inherits = ctx.inherits == null? null : ctx.inherits.getText();
		
		ClassBinding classBinding = null;
		if(inherits == null) {
			classBinding = tm.makeNewClass(className, "Object");

		} else {
			classBinding = tm.makeNewClass(className, inherits);
		}
		
		WoolType type = ASTFactory.makeWoolType(classBinding); //make the AST

		
		ClassBodyContext body = ctx.classBody(); //maket
		
		for(VariableDefContext variable : body.variables) {
			WoolVariable varAST = (WoolVariable) this.visitVariableDef(variable);
			type.addChildAndSetAsParent(varAST); //add varAST as a child of type, make type the child of varAST 
			
		}
		
		for(MethodContext m : body.methods) {
			ASTNode methodAST = this.visitMethod(m);
			type.addChildAndSetAsParent(methodAST);
		}
		
		tm.exitScopeInClass();
		
		
		return type;
	}
	
	@Override
	public ASTNode visitMethod(MethodContext ctx) {
		MethodBinding binding = tm.registerMethod(ctx);
	
		WoolMethod method = ASTFactory.makeWoolMethod(binding);
		
		tm.enterScopeInClass();
	
		for(VariableDefContext variable : ctx.vars) {
			WoolVariable varAST = (WoolVariable) this.visitVariableDef(variable);
			method.addChildAndSetAsParent(varAST); //add varAST as a child of type, make type the child of varAST 
		}
		
		if (ctx.expr() instanceof ExprListContext) {
			ExprListContext context = (ExprListContext) ctx.expr();
			
			for(ExprContext expr : context.expr()) {
				ASTNode node = expr.accept(this);
				method.addChildAndSetAsParent(node);
			}
			
		}

		
		tm.exitScopeInClass();
		
		return method;
		
		}
	
	@Override
	public ASTNode visitVariableDef(VariableDefContext ctx) {
		ObjectBinding binding = tm.registerVariable(ctx); //add to the symbol table
		return ASTFactory.makeWoolVariable(binding);
	}
	
	@Override
	public ASTNode visitIfExpr(IfExprContext ctx) {
		WoolIf expr = ASTFactory.makeIf();
		expr.token = ctx.start;
		expr.addChildAndSetAsParent(ctx.condition.accept(this));
		expr.addChildAndSetAsParent(ctx.thenExpr.accept(this));
		expr.addChildAndSetAsParent(ctx.elseExpr.accept(this));
		return expr;
	}
	
	@Override
	public ASTNode visitWhileExpr(WhileExprContext ctx) {
		WoolWhile expr = ASTFactory.makeWhile();
		expr.token = ctx.start;
		expr.addChildAndSetAsParent(ctx.condition.accept(this));
		expr.addChildAndSetAsParent(ctx.loopExpr.accept(this));
		
		return expr;
	}
	
	@Override
	public ASTNode visitAssignExpr(AssignExprContext ctx) {
		WoolAssignExpr expr = ASTFactory.makeAssignExpr();
		
		
		ASTNode internalExpr = ctx.expr().accept(this);
		
		expr.addChildAndSetAsParent(id);
		expr.addChildAndSetAsParent(internalExpr);
	
		return expr;
	}
	
	@Override
	public ASTNode visitFullMethodCall(FullMethodCallContext ctx) {
		
		WoolMethodCall method = ASTFactory.makeMethodCall(DispatchType.mcObject);
				
		ASTNode caller = ctx.object.accept(this);
		method.object = caller;
		method.addChildAndSetAsParent(caller);
	
		ASTNode methodName = ASTFactory.makeID(ctx.methodName);
		method.methodName = methodName;
		method.addChildAndSetAsParent(methodName);
		
		for(ExprContext param: ctx.expr()) {
			ASTNode pNode = param.accept(this);
			methodName.addChildAndSetAsParent(pNode);
		}
		
	
		return method;
	}
	
	
	@Override
	public ASTNode visitLocalMethodCall(LocalMethodCallContext ctx) {
		return ASTFactory.makeMethodCall(DispatchType.mcLocal);
	}
	


}
