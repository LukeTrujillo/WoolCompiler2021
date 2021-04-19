package wool.ast;

import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolParser.ClassBodyContext;
import wool.lexparse.WoolParser.ClassDefContext;
import wool.lexparse.WoolParser.MethodContext;
import wool.lexparse.WoolParser.ProgramContext;
import wool.lexparse.WoolParser.VariableDefContext;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.tables.TableManager;

public class ASTCreator extends WoolBaseVisitor<ASTNode> {
	
	private TableManager tm;
	private ClassBinding currentClass;
	
	public ASTCreator() {
		tm = TableManager.getInstance();
	}
	
	
	@Override
	public ASTNode visitProgram(ProgramContext ctx) {
		final CoolText coolText = ASTFactory.makeCoolText();
		
		for(ClassDefContext t : ctx.classes) {
			ASTNode type = t.accept(this);
			coolText.addChild(type);
		}
		
		return coolText;
	}
	
	@Override
	public ASTNode visitClassDef(ClassDefContext ctx) {
		final String className = ctx.className.getText();
		//final InheritsContext inherits = ctx.inherits;
		
		//TODO: fix this code for inheritance later, C06-7
		currentClass = tm.makeNewClass(className, null);
		
		final Type typeNode = ASTFactory.makeType(className);
		currentClass.token = ctx.TYPE(0).getSymbol();
		
		typeNode.binding = currentClass;
		
		ClassBodyContext body = ctx.classBody();
		
		//visit the variables first
		for(VariableDefContext v : body.variables) {
			typeNode.addChild(v.accept(this));
		}
		
		for(MethodContext m : body.methods) {
			typeNode.addChild(m.accept(this));
		}
		
		tm.exitScopeInClass();
		return typeNode;
		
	}
	

}
