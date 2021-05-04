package wool.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import wool.codegen.IRCreator;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.tables.SymbolTable;
import wool.symbol.tables.TableManager;
import wool.typechecking.TypeChecker;

public abstract class ASTNode {
	
	protected ASTNodeType nodeType;
	protected String nodeClass;
	public ASTNode parent;
	protected List<ASTNode> children;
	
	public AbstractBinding binding;
	
	public Token token;
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
		
		if(child != null) {
			this.children.add(child);
			child.setParent(this);
		}
	}
	
	public ASTNode getChild(int i) { return children.get(i); }

	protected String extraInfo() {
		return null;
	}

	public String accept(ASTPrinter printer) {

		System.out.println(this.printNicely());
		
		for(ASTNode node : children) {
			node.accept(printer);
			
		}
		
		System.out.println();
		return null;
		
	}
	
	public List<ASTNode> getChildren() { return this.children; }
	
	public ASTNodeType getNodeType() { return this.nodeType; }
	
	public AbstractBinding accept(TypeChecker typeChecker) {
		AbstractBinding binding = typeChecker.visit(this);
		return binding;
		
	}
	public String printNicely() {
		return getTabsForDepth() + nodeType.name();
	}

	public void accept(IRCreator irc) {
		irc.visit(this);
	}
	
	public int getDepth() {
		int depth = 0;
		
		ASTNode up = this.parent;
		
		while(up != null) {
			up = up.parent;
			depth++;
		}
		return depth;
	}
	public String getTabsForDepth() {
		String tabs = "";
		for(int x = 0; x < getDepth(); x++) {
			tabs += "\t";
		}
		
		return tabs;
	}
	
}
