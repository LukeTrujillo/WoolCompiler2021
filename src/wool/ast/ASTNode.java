package wool.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import wool.symbol.bindings.AbstractBinding;
import wool.symbol.tables.SymbolTable;
import wool.symbol.tables.TableManager;

public abstract class ASTNode {

	public enum ASTNodeType {
		nAssign, nBinary, nUrinary, nVariable, nWhile, nClass
	}
	
	public ASTNodeType nodeType;
	public String nodeClass; //the class of an expression or some other activity
	public ASTNode parent;
	public List<ASTNode> children;
	public Token token;
	public SymbolTable scope; // the current
	public AbstractBinding binding;
	
	public ASTNode() {
		this(null); // use this only for the cooltext node
	}
	
	public ASTNode(ASTNode parent) {
		children = new ArrayList<ASTNode>();
		nodeClass = null;
		this.parent = parent;
		token = null;
		scope = TableManager.getInstance().getCurrentTable();
	}
	
	public void addChild(ASTNode type) {
		this.children.add(type);
	}

}
