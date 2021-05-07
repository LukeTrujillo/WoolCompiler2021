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
import wool.lexparse.WoolParser.EqExprContext;
import wool.lexparse.WoolParser.ExprContext;
import wool.lexparse.WoolParser.ExprListContext;
import wool.lexparse.WoolParser.FalseExprContext;
import wool.lexparse.WoolParser.FormalContext;
import wool.lexparse.WoolParser.FullMethodCallContext;
import wool.lexparse.WoolParser.IDExprContext;
import wool.lexparse.WoolParser.IfExprContext;
import wool.lexparse.WoolParser.IsNullExprContext;
import wool.lexparse.WoolParser.LocalMethodCallContext;
import wool.lexparse.WoolParser.MathExprContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.NewExprContext;
import wool.lexparse.WoolParser.NotExprContext;
import wool.lexparse.WoolParser.NullExprContext;
import wool.lexparse.WoolParser.NumExprContext;
import wool.lexparse.WoolParser.ParenExprContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.RelExprContext;
import wool.lexparse.WoolParser.SelectAltContext;
import wool.lexparse.WoolParser.SelectExprContext;
import wool.lexparse.WoolParser.StrExprContext;
import wool.lexparse.WoolParser.TrueExprContext;
import wool.lexparse.WoolParser.UMinusExprContext;
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
			ASTNode varAST = variable.accept(this);
			
			if(varAST instanceof WoolVariable) {
				type.addChildAndSetAsParent(varAST); //add varAST as a child of type, make type the child of varAST 
				continue;
			}
		
			//By here it has to be an assign but lets double check
			
			if(varAST instanceof WoolAssign) {
				type.addChildAndSetAsParent(((WoolAssign) varAST).getIdentifer()); //add the variable node to the top
				
				WoolTerminal variableName = ASTFactory.makeID(((WoolAssign) varAST).getIdentifer().binding);
				WoolAssign newAssign = ASTFactory.makeAssignExpr();
				
				newAssign.addChildAndSetAsParent(variableName);
				newAssign.addChildAndSetAsParent(((WoolAssign) varAST).getSetValue());
				
				exprList.addChildAndSetAsParent(newAssign);
			}
		}
		
		type.addChildAndSetAsParent(constructor);

		
		
		for(MethodContext m : body.methods) {
			ASTNode methodAST = m.accept(this);
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
		
		
		for(FormalContext f : ctx.formals) {
			ASTNode arg = f.accept(this);
			binding.getMethodDescriptor().addArgumentType(arg.binding.getSymbolType());
			method.addChildAndSetAsParent(arg);
		}
	
		for(VariableDefContext variable : ctx.vars) {
			ASTNode varAST = variable.accept(this);
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
		if(ctx instanceof LocalMethodCallContext) return ((LocalMethodCallContext) ctx).accept(this);
		if(ctx instanceof FullMethodCallContext) return ((FullMethodCallContext) ctx).accept(this);
		
		if(ctx instanceof MathExprContext) return ((MathExprContext) ctx).accept(this);
		if(ctx instanceof ParenExprContext) return ((ParenExprContext) ctx).accept(this);
		if(ctx instanceof UMinusExprContext) return ((UMinusExprContext) ctx).accept(this);
		
		if(ctx instanceof IfExprContext) return ((IfExprContext) ctx).accept(this);
		if(ctx instanceof WhileExprContext) return ((WhileExprContext) ctx).accept(this);
		
		if(ctx instanceof RelExprContext) return ((RelExprContext) ctx).accept(this);
		if(ctx instanceof EqExprContext) return ((EqExprContext) ctx).accept(this);
		if(ctx instanceof NotExprContext) return ((NotExprContext) ctx).accept(this);
		
		if(ctx instanceof SelectExprContext) return ((SelectExprContext) ctx).accept(this);
		
		if(ctx instanceof NewExprContext) return ((NewExprContext) ctx).accept(this);
		
		if(ctx instanceof NullExprContext) return ASTFactory.makeConstant(null, TerminalType.tNull);
		
		if(ctx instanceof IsNullExprContext) { return ((IsNullExprContext) ctx).accept(this);}
		
		return null;
	}
	
	@Override
	public ASTNode visitSelectExpr(SelectExprContext ctx) {
		WoolSelect select = ASTFactory.makeWoolSelect();
		
		for(SelectAltContext expr: ctx.selectAlt()) {
			ASTNode child = expr.accept(this);
			select.addChildAndSetAsParent(child);
		}
		
		return select;
	}
	
	@Override
	public ASTNode visitSelectAlt(SelectAltContext ctx) {
		WoolSelectAlt selectAlt = ASTFactory.makeWoolSelectAlt();
		
		selectAlt.addChildAndSetAsParent(this.visitExprContext(ctx.conditon));
		selectAlt.addChildAndSetAsParent(this.visitExprContext(ctx.action));
		
		return selectAlt;
	}
	
	
	
	
	@Override
	public ASTNode visitIsNullExpr(IsNullExprContext ctx) {
		WoolCompare comp = ASTFactory.makeWoolCompare("isnull");
		comp.addChildAndSetAsParent(this.visitExprContext(ctx.expr()));
		
		return comp;
	}
	
	
	@Override
	public ASTNode visitNotExpr(NotExprContext ctx) {
		WoolCompare comp = ASTFactory.makeWoolCompare(ctx.op);
		
		ASTNode left = this.visitExprContext(ctx.left);
		comp.addChildAndSetAsParent(left);
		
		return comp;
	}
	
	@Override
	public ASTNode visitRelExpr(RelExprContext ctx) {
		WoolCompare comp = ASTFactory.makeWoolCompare(ctx.op);
		
		ASTNode left = this.visitExprContext(ctx.left);
		comp.addChildAndSetAsParent(left);
		
		ASTNode right = this.visitExprContext(ctx.right);
		comp.addChildAndSetAsParent(right);
		
		return comp;
	}
	
	@Override
	public ASTNode visitEqExpr(EqExprContext ctx) {
		WoolCompare comp = ASTFactory.makeWoolCompare(ctx.op);
		
		ASTNode left = this.visitExprContext(ctx.left);
		comp.addChildAndSetAsParent(left);
		
		ASTNode right = this.visitExprContext(ctx.right);
		comp.addChildAndSetAsParent(right);
		
		return comp;
	}
	
	
	@Override
	public ASTNode visitUMinusExpr(UMinusExprContext ctx) {
		WoolMath math = ASTFactory.makeWoolMath(ctx.op);
		
		ASTNode inner = this.visitExprContext(ctx.right);
		math.addChildAndSetAsParent(inner);
		
		return math;
	}
	
	@Override
	public ASTNode visitParenExpr(ParenExprContext ctx) {
		WoolParen paren = ASTFactory.makeWoolParen();
		ASTNode inner = this.visitExprContext(ctx.go);
		paren.addChildAndSetAsParent(inner);
		
		return paren;
	}
	
	@Override
	public ASTNode visitMathExpr(MathExprContext ctx) {
		WoolMath math = ASTFactory.makeWoolMath(ctx.op);
		
		ExprContext firstDive = ctx.left;
		ExprContext secondDive = ctx.right;
		
		if(secondDive instanceof ParenExprContext) {
			firstDive = ctx.right;
			secondDive = ctx.left;
		} 
		
		ASTNode leftNode = this.visitExprContext(firstDive);
		ASTNode rightNode = this.visitExprContext(secondDive);
		
		math.addChildAndSetAsParent(leftNode);
		math.addChildAndSetAsParent(rightNode);
		
		return math;
	}
	
	
	@Override
	public ASTNode visitExprList(ExprListContext ctx) {
		WoolExprList node = ASTFactory.makeExprList();
		
		for(ExprContext expr : ctx.exprs) {
			ASTNode child =  this.visitExprContext(expr);
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
			WoolAssign expr = ASTFactory.makeAssignExpr();
			
			expr.addChildAndSetAsParent(variable);
			
			

			
			ASTNode node = this.visitExprContext(ctx.expr());
			expr.addChildAndSetAsParent(node);
			
			
			return expr;
		}
		
		return variable;
	}
	
	
	
	@Override
	public ASTNode visitIfExpr(IfExprContext ctx) {
		WoolIf expr = ASTFactory.makeIf();
		expr.token = ctx.start;
		expr.addChildAndSetAsParent(this.visitExprContext(ctx.condition));
		expr.addChildAndSetAsParent(this.visitExprContext(ctx.thenExpr));
		expr.addChildAndSetAsParent(this.visitExprContext(ctx.elseExpr));
		return expr;
	}
	
	@Override
	public ASTNode visitWhileExpr(WhileExprContext ctx) {
		WoolWhile expr = ASTFactory.makeWhile();
		expr.token = ctx.start;
		expr.addChildAndSetAsParent(this.visitExprContext(ctx.condition));
		expr.addChildAndSetAsParent(this.visitExprContext(ctx.loopExpr));
		
		return expr;
	}
	
	@Override
	public ASTNode visitAssignExpr(AssignExprContext ctx) {
		WoolAssign expr = ASTFactory.makeAssignExpr();
		
		WoolTerminal variableName = ASTFactory.makeID(ctx.variableName);
		
		ASTNode internalExpr = this.visitExprContext(ctx.expr());

		
		expr.addChildAndSetAsParent(variableName);
		expr.addChildAndSetAsParent(internalExpr);
	
		return expr;
	}
	
	
	@Override
	public ASTNode visitFullMethodCall(FullMethodCallContext ctx) {
		
		WoolMethodCall method = ASTFactory.makeMethodCall(DispatchType.mcObject);
		
		method.methodName = ctx.methodName.getText();
				
		ASTNode caller = this.visitExprContext(ctx.object);
		method.setObject(caller);
		
		for(ExprContext param: ctx.args) {
			ASTNode pNode = this.visitExprContext(param);
			method.addChildAndSetAsParent(pNode);
		}
		
	
		return method;
	}
	
	
	@Override
	public ASTNode visitLocalMethodCall(LocalMethodCallContext ctx) {
		WoolMethodCall method = ASTFactory.makeMethodCall(DispatchType.mcLocal);
		
		method.methodName = ctx.methodName.getText();
		
		for(ExprContext expr : ctx.args) {
			ASTNode node = this.visitExprContext(expr);
			method.addChildAndSetAsParent(node);
		}
		
		return method;
	}
	
	@Override
	public ASTNode visitNewExpr(NewExprContext ctx) {

		WoolTerminal type = ASTFactory.makeTypeTerminal(ctx.type);
		
		return type;
	}
	
	@Override
	public ASTNode visitIDExpr(IDExprContext ctx) {
		return ASTFactory.makeID(ctx.name);
	}


}
