package wool.ast;

import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.VariableDefContext;
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
		
		//Now it has entered a new scope
	
		
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
		
		tm.exitScopeInClass();
		
		return method;
	}
	
	@Override
	public ASTNode visitVariableDef(VariableDefContext ctx) {
		ObjectBinding binding = tm.registerVariable(ctx); //add to the symbol table
		return ASTFactory.makeWoolVariable(binding);
	}

}
