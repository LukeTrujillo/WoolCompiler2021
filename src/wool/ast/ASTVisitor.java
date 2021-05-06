package wool.ast;

import wool.utility.WoolException;

public abstract class ASTVisitor<T> {
	
	public T visit(ASTNode node) {
		if(node instanceof WoolProgram) return this.visit((WoolProgram) node);
		
		if(node instanceof WoolMethod) return this.visit((WoolMethod) node);
		
		if(node instanceof WoolType) return this.visit((WoolType) node);
		
		if(node instanceof WoolMethodCall) return this.visit((WoolMethodCall) node);
		
		if(node instanceof WoolTerminal) return this.visit((WoolTerminal) node);
		
		if(node instanceof WoolVariable) return this.visit((WoolVariable) node);
		
		if(node instanceof WoolWhile) return this.visit((WoolWhile) node);
		
		if(node instanceof WoolAssign) return this.visit((WoolAssign) node);
		
		if(node instanceof WoolExprList) return this.visit((WoolExprList) node);
		
		if(node instanceof WoolNew) return this.visit((WoolNew) node);
		
		if(node instanceof WoolMath) return this.visit((WoolMath) node);
		
		if(node instanceof WoolParen) return this.visit((WoolParen) node);
		
		if(node instanceof WoolIf) return this.visit((WoolIf) node);
		
		if(node instanceof WoolCompare) return this.visit((WoolCompare) node);
		
		if(node instanceof WoolNew)return this.visit((WoolNew) node);

		throw new WoolException("Unregistered ASTNode in WoolVisitor");
	}
	
	public T visit(WoolProgram node) { return visitChildren(node); }
	public T visit(WoolMethod node) { return visitChildren(node); }
	public T visit(WoolType node) { return visitChildren(node); }
	public T visit(WoolMethodCall node) { return visitChildren(node); }
	public T visit(WoolTerminal node) { return visitChildren(node); }
	public T visit(WoolVariable node) { return visitChildren(node); }
	public T visit(WoolAssign node) { return visitChildren(node); }
	public T visit(WoolWhile node) { return visitChildren(node); }
	public T visit(WoolExprList node) { return visitChildren(node); }
	public T visit(WoolNew node) { return visitChildren(node); }
	public T visit(WoolMath node) { return visitChildren(node); }
	public T visit(WoolParen node) { return visitChildren(node); }
	public T visit(WoolIf node) { return visitChildren(node); }
	public T visit(WoolCompare node) { return visitChildren(node); }
	
	
	public T visitChildren(ASTNode node) {
		
		for(ASTNode n : node.getChildren()) {
			this.visit(n);
		}
		
		return null;
	}

}
