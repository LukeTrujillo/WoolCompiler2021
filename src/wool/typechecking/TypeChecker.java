package wool.typechecking;

import java.util.List;

import wool.ast.*;
import wool.ast.WoolMethodCall.DispatchType;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.tables.TableManager;
import wool.utility.WoolException;

public class TypeChecker {
	
	TableManager tm;
	
	public TypeChecker() {
		tm = TableManager.getInstance();
	}
	
	public AbstractBinding visit(ASTNode node) {
		
		if(node instanceof WoolProgram) return this.visit((WoolProgram) node);
		
		if(node instanceof WoolMethod) return this.visit((WoolMethod) node);
		
		if(node instanceof WoolType) return this.visit((WoolType) node);
		
		if(node instanceof WoolMethodCall) return this.visit((WoolMethodCall) node);
		
		if(node instanceof WoolTerminal) return this.visit((WoolTerminal) node);
		
		if(node instanceof WoolVariable) return this.visit((WoolVariable) node);
		
		if(node instanceof WoolWhile) return this.visit((WoolWhile) node);
		
		if(node instanceof WoolAssignExpr) return this.visit((WoolAssignExpr) node);
		
		if(node instanceof WoolExprList) return this.visit((WoolExprList) node);
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		return null;
	}
	
	
	public AbstractBinding visit(WoolAssignExpr node) {
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}
	
	public AbstractBinding visit(WoolExprList node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}
	
	public AbstractBinding visit(WoolMethod node) {
		//verify the arguments
		List<String> args = node.binding.getMethodDescriptor().getArgumentTypes();
		
		 for(String arg : args) {
				AbstractBinding search = tm.getClassTable().lookup(arg);
				if(search == null) throw new WoolException("Type \"" + arg + "\" not registered in method " + node.binding.getSymbol());
		 }
		 
		 //verify the return type
		 String returnType = node.binding.getMethodDescriptor().returnType;
		 
		 AbstractBinding search = tm.getClassTable().lookup(returnType);
		if(search == null) throw new WoolException("Return type \"" + returnType + "\" not registered in method " + node.binding.getSymbol());
		
		tm.enterScopeInClass();
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		tm.exitScopeInClass();
		
		return null;
	}
	
	public AbstractBinding visit(WoolMethodCall node) {
		
		System.out.println("here69");
		//Need to first find the class that the method is part of
		ObjectBinding caller = (ObjectBinding) node.getObject().accept(this);
		MethodBinding method = (MethodBinding) node.getMethodName().accept(this);
		
		if(node.dispatch == DispatchType.mcLocal) {
			 //unsure how to check here
			
		} else if(node.dispatch == DispatchType.mcObject) {
			String classWhereDefined = method.getClassWhereDefined();
			String symbolType = caller.getSymbolType();
			
			if(classWhereDefined != symbolType) throw new WoolException("Type variable calling undefined method");
		}
		
		
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}
	
	public AbstractBinding visit(WoolNew node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}
	
	public AbstractBinding visit(WoolProgram node) {
		tm.enterScopeInClass();
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		tm.exitScopeInClass();

		return null;
	}
	
	public AbstractBinding visit(WoolTerminal node) {
		
		
		
		ObjectBinding nodeBinding = (ObjectBinding) node.binding;
		if(node.binding == null) {
			//AbstractBinding binding = tm.getCurrentTable().lookup(node)
		}
		
		
		AbstractBinding search = tm.getClassTable().lookup(node.binding.getSymbolType());
		if(search == null) throw new WoolException("Type \"" + node.binding.getSymbolType() + "\" not defined");
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return search;
	}
	
	public AbstractBinding visit(WoolVariable node) {
		
		ObjectBinding binding = node.binding;
	
		AbstractBinding search = tm.getClassTable().lookup(node.binding.getSymbolType());
		if(search == null) throw new WoolException("Type \"" + node.binding.getSymbolType() + "\" not registered");
			
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return search;
	}
	
	public AbstractBinding visit(WoolWhile node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}

	public AbstractBinding visit(WoolType node) {
	
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		return null;
	}

	public AbstractBinding visit(WoolIf node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
		
	}
	

}
