package wool.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import wool.symbol.tables.SymbolTable;
import wool.symbol.tables.TableManager;

public abstract class ASTNode {
	
	protected ASTNodeType nodeType;
	protected String nodeClass;
	protected ASTNode parent;
	protected List<ASTNode> children;
	
	protected Token token;
	protected SymbolTable scope;
	
	public ASTNode() {
		this(null);
	}
	
	public ASTNode(ASTNodeType nodeType) {
		this.children = new ArrayList<ASTNode>();
		this.nodeClass = null;
		this.parent = null;
		this.token = null;
		this.scope = TableManager.getInstance().getCurrentTable();
		this.nodeType = nodeType;
	}
	
	public void setParent(ASTNode parent) {
		this.parent = parent;
	}
	
	public void addChildAndSetAsParent(ASTNode child) { 
		this.children.add(child);
		child.setParent(this);
	}
	
	public ASTNode getChild(int i) { return children.get(i); }

	protected String extraInfo() {
		return null;
	}

	public String accept(ASTPrinter printer) {
		
		if(this instanceof WoolType) {
			System.out.print("WoolType node");
		} else if(this instanceof WoolMethod) {
			System.out.print("WoolMethod node");
		}
		
		
		System.out.print(" children={" );
		for(ASTNode node : children) {
			node.accept(printer);
			
		}
		System.out.print("}");
		
		System.out.println();
		return nodeClass;
		
	}
}
