package wool.ast;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import wool.ast.WoolMethodCall.DispatchType;
import wool.ast.WoolTerminal.TerminalType;
import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolParser;
import wool.lexparse.WoolParser.AssignExprContext;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.ExprContext;
import wool.lexparse.WoolParser.ExprListContext;
import wool.lexparse.WoolParser.FalseExprContext;
import wool.lexparse.WoolParser.FormalContext;
import wool.lexparse.WoolParser.FullMethodCallContext;
import wool.lexparse.WoolParser.IDExprContext;
import wool.lexparse.WoolParser.IfExprContext;
import wool.lexparse.WoolParser.LocalMethodCallContext;

import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.NewExprContext;
import wool.lexparse.WoolParser.NullExprContext;
import wool.lexparse.WoolParser.NumExprContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.StrExprContext;
import wool.lexparse.WoolParser.TrueExprContext;
import wool.lexparse.WoolParser.VariableDefContext;
import wool.lexparse.WoolParser.WhileExprContext;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.MethodDescriptor;
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
		
		
		MethodDescriptor constructorDescriptor = new MethodDescriptor("<init>", null);
		MethodBinding methodBinding = new MethodBinding(constructorDescriptor, null);
		WoolMethod constructor =  ASTFactory.makeWoolMethod(methodBinding);
		
		
		WoolExprList exprList = ASTFactory.makeExprList();
		constructor.addChildAndSetAsParent(exprList);
		
		
		for(VariableDefContext variable : body.variables) {
			ASTNode varAST = this.visitVariableDef(variable);
			
			if(varAST instanceof WoolVariable) {
				type.addChildAndSetAsParent(varAST); //add varAST as a child of type, make type the child of varAST 
				continue;
			}
		
			//By here it has to be an assign but lets double check
			
			if(varAST instanceof WoolAssignExpr) {
				type.addChildAndSetAsParent(((WoolAssignExpr) varAST).getIdentifer()); //add the variable node to the top
				
				WoolTerminal variableName = ASTFactory.makeID(((WoolAssignExpr) varAST).getIdentifer().binding);
				WoolAssignExpr newAssign = ASTFactory.makeAssignExpr();
				
				newAssign.addChildAndSetAsParent(variableName);
				newAssign.addChildAndSetAsParent(((WoolAssignExpr) varAST).getSetValue());
				
				exprList.addChildAndSetAsParent(newAssign);
			}
		}
		
		type.addChildAndSetAsParent(constructor);

		
		
		for(MethodContext m : body.methods) {
			ASTNode methodAST = this.visitMethod(m);
			type.addChildAndSetAsParent(methodAST);
			
			if(classBinding.getClassDescriptor().getMethodBinding(m.methodName.getText()) == null) {
				classBinding.getClassDescriptor().addMethod((MethodBinding) methodAST.binding);
			}
		}
		
		tm.exitScopeInClass();
		
		
		return type;
	}
	
	@Override
	public ASTNode visitMethod(MethodContext ctx) {
		MethodBinding binding = tm.registerMethod(ctx);
	
		WoolMethod method = ASTFactory.makeWoolMethod(binding);
		
		tm.enterScopeInClass();
		
		//define the method parameters too
		
		for(FormalContext f : ctx.formals) {
			ASTNode arg = f.accept(this);
			binding.getMethodDescriptor().addArgumentType(arg.binding.getSymbolType());
			method.addChildAndSetAsParent(arg);
		}
	
		for(VariableDefContext variable : ctx.vars) {
			WoolVariable varAST = (WoolVariable) this.visitVariableDef(variable);
			method.addChildAndSetAsParent(varAST); //add varAST as a child of type, make type the child of varAST 
		}
		
		
		ASTNode child = this.visitExprContext(ctx.expr());
		method.addChildAndSetAsParent(child);
		
		
		tm.exitScopeInClass();
		
		return method;
	}
	
	public ASTNode visitExprContext(ExprContext ctx) {
		
		if(ctx instanceof ExprListContext) return ((ExprListContext) ctx).accept(this);
		
		if(ctx instanceof FalseExprContext) return ASTFactory.makeConstant(((FalseExprContext)ctx).f, TerminalType.tBool);
		if(ctx instanceof TrueExprContext) return ASTFactory.makeConstant(((TrueExprContext)ctx).t, TerminalType.tBool);
		if(ctx instanceof StrExprContext) return ASTFactory.makeConstant(((StrExprContext)ctx).s, TerminalType.tStr);
		if(ctx instanceof NumExprContext) return ASTFactory.makeConstant(((NumExprContext)ctx).niceNumber, TerminalType.tInt);
		
		if(ctx instanceof IDExprContext) return ASTFactory.makeID(((IDExprContext) ctx).name);
		
		if(ctx instanceof AssignExprContext) return ((AssignExprContext) ctx).accept(this);
		
		return null;
	}
	
	@Override
	public ASTNode visitExprList(ExprListContext ctx) {
		WoolExprList node = ASTFactory.makeExprList();
		
		for(ExprContext expr : ctx.exprs) {
			ASTNode child = expr.accept(this);
			node.addChildAndSetAsParent(child);
		}
		return node;
	}
	
	@Override
	public ASTNode visitFormal(FormalContext ctx) {
		ObjectBinding binding = tm.registerVariable(ctx);
		return ASTFactory.makeWoolVariable(binding);
	}
	
	@Override
	public ASTNode visitVariableDef(VariableDefContext ctx) {
		
		ASTNode formal = ctx.dec.accept(this);
		
		ObjectBinding binding =  (ObjectBinding) formal.binding; //add to the symbol table
		WoolVariable variable = ASTFactory.makeWoolVariable(binding);
		
		if(ctx.initializer != null) {
			WoolAssignExpr expr = ASTFactory.makeAssignExpr();
			
			expr.addChildAndSetAsParent(variable);
			
			
			ASTNode node = ctx.expr().accept(this);
			
			if(node == null) { //then it is likely a terminal
			
				if (ctx.initializer instanceof NumExprContext) {
					
					node = ASTFactory.makeConstant(((NumExprContext) ctx.initializer).niceNumber, TerminalType.tInt);
					expr.addChildAndSetAsParent(node);
				}
				
				if(ctx.initializer instanceof TrueExprContext) {

					node = ASTFactory.makeConstant(((TrueExprContext) ctx.initializer).t, TerminalType.tBool);
					expr.addChildAndSetAsParent(node);
				}
				if(ctx.initializer instanceof FalseExprContext) {
					node = ASTFactory.makeConstant(((FalseExprContext) ctx.initializer).f, TerminalType.tBool);
					expr.addChildAndSetAsParent(node);
				}
				
				if(ctx.initializer instanceof StrExprContext) {
					node = ASTFactory.makeConstant(((StrExprContext) ctx.initializer).s, TerminalType.tStr);
					expr.addChildAndSetAsParent(node);
				}
				//TODO: might need to add null here
			}
			
			return expr;
		}
		
		return variable;
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
		
		WoolTerminal variableName = ASTFactory.makeID(ctx.variableName);
		
		ASTNode internalExpr = this.visitExprContext(ctx.expr());

		
		expr.addChildAndSetAsParent(variableName);
		expr.addChildAndSetAsParent(internalExpr);
	
		return expr;
	}
	
	
	@Override
	public ASTNode visitFullMethodCall(FullMethodCallContext ctx) {
		
		WoolMethodCall method = ASTFactory.makeMethodCall(DispatchType.mcObject);
				
		ASTNode caller = ASTFactory.makeConstant(ctx.object.start, TerminalType.tID);
		method.object = caller;
		method.addChildAndSetAsParent(caller);
	
		ASTNode methodName = ASTFactory.makeConstant(ctx.methodName, TerminalType.tMethod);
		method.methodName = methodName;
		
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
	
	@Override
	public ASTNode visitNewExpr(NewExprContext ctx) {
		WoolNew woolNew = ASTFactory.makeWoolNew();
		WoolTerminal type = ASTFactory.makeTypeTerminal(ctx.type);
		woolNew.addChildAndSetAsParent(type);
		
		return woolNew;
	}
	
	@Override
	public ASTNode visitIDExpr(IDExprContext ctx) {
		return ASTFactory.makeID(ctx.name);
	}


}
