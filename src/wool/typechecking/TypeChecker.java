package wool.typechecking;

import java.util.List;

import wool.ast.*;
import wool.ast.WoolMethodCall.DispatchType;
import wool.ast.WoolTerminal.TerminalType;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.tables.TableManager;
import wool.utility.WoolException;
import wool.ast.*;

public class TypeChecker extends ASTVisitor<AbstractBinding> {
	
	TableManager tm;
	ClassBinding currentClassBinding;
	WoolMethod currentMethod;
	
	public TypeChecker() {
		tm = TableManager.getInstance();
		
		currentMethod = null;
	}

	@Override
	public AbstractBinding visit(WoolAssign node) {
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
	
		return null;
	}

	
	@Override
	public AbstractBinding visit(WoolMath node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		if(node.getChildren().size() == 1 && !node.token.getText().equals("-")) {
			throw new WoolException("WoolMath with one child has '" + node.token.getText() + "' operator; needs to be '-'");
		}
		
		
		return null;
	}
	
	@Override
	public AbstractBinding visit(WoolMethod node) {
		currentMethod = node;
		//verify the arguments
		List<String> args = node.binding.getMethodDescriptor().getArgumentTypes();
		
		int x = 0 ;
		 for(String arg : args) {
			 if(arg != null && arg.equals("int")) {
			 		node.binding.getMethodDescriptor().argumentTypes.set(x, "Int");
			 		arg = node.binding.getMethodDescriptor().argumentTypes.get(x);
				} else if(arg != null && arg.equals("boolean")) {
					node.binding.getMethodDescriptor().argumentTypes.set(x, "Bool");
					arg = node.binding.getMethodDescriptor().argumentTypes.get(x);
				}
			 
			 x++;
			 
				AbstractBinding search = tm.getClassTable().lookup(arg);
				if(search == null) throw new WoolException("Type \"" + arg + "\" not registered in method " + node.binding.getSymbol());
		 }
		 
		 //verify the return type
		 String returnType = node.binding.getMethodDescriptor().returnType;
		 
		 	if(returnType != null && returnType.equals("int")) {
		 		node.binding.getMethodDescriptor().returnType = "Int";
		 		returnType = node.binding.getMethodDescriptor().returnType;
			} else if(returnType != null && returnType.equals("boolean")) {
				node.binding.getMethodDescriptor().returnType = "Bool";
				returnType = node.binding.getMethodDescriptor().returnType;
			} 
		 
		 AbstractBinding search = tm.getClassTable().lookup(returnType);
		 
		 for(ASTNode n : node.getChildren()) {
				n.accept(this);
			}
		 
		 if(node.binding.getMethodDescriptor().getMethodName().equalsIgnoreCase("<init>")) {
			 return null;
		 }
		 
		if(search == null) throw new WoolException("Return type \"" + returnType + "\" not registered in method " + node.binding.getSymbol());
		

		currentMethod = null;
	
		return null;
	}
	@Override
	public AbstractBinding visit(WoolMethodCall node) {
	
		AbstractBinding binding = null;
		String name = node.methodName;
		
		if(node.dispatch == DispatchType.mcLocal) { //load the self object
			binding = node.scope.lookup(name);
			
			if(binding == null) {
				ClassBinding parent = tm.getClassBindingFromString(currentClassBinding.getClassDescriptor().inherits);
				
				while(binding == null) {
					binding = parent.getClassDescriptor().getMethodBinding(name);
					
					if(parent.getClassDescriptor().inherits == null) break;
					
					parent = tm.getClassBindingFromString(parent.getClassDescriptor().inherits);
				}
				
			}
			
			
			if(binding == null) throw new WoolException("self type not defined in class " + currentClassBinding.getClassDescriptor().className);
			
		} else if(node.dispatch == DispatchType.mcObject) {
			AbstractBinding ab = node.getObject().accept(this);
			if(ab == null) throw new WoolException(node.getObject() + " has no binding ");
			
			String starting = ab.getSymbolType();
			
			if(ab instanceof MethodBinding) {
				starting = currentClassBinding.symbolType;
			}
			
			ClassBinding parent = tm.getClassBindingFromString(starting);
			
			while(binding == null) {
				binding = parent.getClassDescriptor().getMethodBinding(name);
				
				if(parent.getClassDescriptor().inherits == null || binding != null) break;
				
				parent = tm.getClassBindingFromString(parent.getClassDescriptor().inherits);
			}
			if(binding == null) throw new WoolException("mcObject " + currentClassBinding.getClassDescriptor().className);
		}
		
		node.binding = binding;
		
		visitChildren(node);
		return binding;
	}
	

	
	public AbstractBinding visit(WoolProgram node) {

		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		
		return null;
	}
	
	public AbstractBinding visit(WoolTerminal node) {
		
		if(node.binding == null) {
			if(node.terminalType == TerminalType.tID) {
				node.binding = node.scope.lookup(node.token.getText());
			} else if(node.terminalType == TerminalType.tType) {
				node.binding = tm.getClassBindingFromString(node.token.getText());
			}  if(node.terminalType == TerminalType.tInt) {
				node.binding = tm.getClassTable().lookup("Int");
			} else if(node.terminalType == TerminalType.tBool) {
				node.binding = tm.getClassTable().lookup("Bool");
			} else if(node.terminalType == TerminalType.tStr) {
				node.binding = tm.getClassTable().lookup("Str");
			}
		}
		
		if(node.binding == null && (node.terminalType != TerminalType.tMethod)) {
			throw new WoolException("Undefined variable " + node.token.getText() + " used");
		}
		
		return node.binding;
	}
	
	public AbstractBinding visit(WoolVariable node) {
		
		ObjectBinding binding = node.binding;
	
		AbstractBinding search = null;
		
		String symbolType = node.binding.getSymbolType();
		if(symbolType.equals("int")) {
			node.binding.symbolType = "Int";
		} else if(symbolType.contentEquals("boolean")) {
			node.binding.symbolType = "Bool";
		}
		
		search = tm.getClassTable().lookup(node.binding.getSymbolType());
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
		
		currentClassBinding = node.binding;
	
		
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
