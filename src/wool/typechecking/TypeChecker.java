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
	public AbstractBinding visit(WoolAssignExpr node) {
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		boolean allowed = isTypeAToTypeBAllowed( node.getSetValue().binding.getSymbolType(), node.getIdentifer().binding.getSymbolType());
		
		if(!allowed) throw new WoolException("Type " + node.getSetValue().binding.getSymbolType() + " cannot be set of a variable of Type " + node.getIdentifer().binding.getSymbolType());
		
		
		return null;
	}
	
	@Override
	public AbstractBinding visit(WoolMethod node) {
		currentMethod = node;
		//verify the arguments
		List<String> args = node.binding.getMethodDescriptor().getArgumentTypes();
		
		 for(String arg : args) {
				AbstractBinding search = tm.getClassTable().lookup(arg);
				if(search == null) throw new WoolException("Type \"" + arg + "\" not registered in method " + node.binding.getSymbol());
		 }
		 
		 //verify the return type
		 String returnType = node.binding.getMethodDescriptor().returnType;
		 
		 AbstractBinding search = tm.getClassTable().lookup(returnType);
		 
		 if(node.binding.getMethodDescriptor().getMethodName().equalsIgnoreCase("<init>")) {
			 return null;
		 }
		 
		if(search == null) throw new WoolException("Return type \"" + returnType + "\" not registered in method " + node.binding.getSymbol());
		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		currentMethod = null;
	
		return null;
	}
	
	public AbstractBinding visit(WoolMethodCall node) {
	
		AbstractBinding binding = node.object.accept(this);
		
		ClassBinding methodTarget = tm.getClassBindingFromString(binding.getSymbolType());
		
		System.out.println(node.methodName.token.getText());
		
		MethodBinding mb = methodTarget.getClassDescriptor().getMethodBinding(node.methodName.token.getText());
		
		if(mb == null) throw new WoolException("Object call does not contain method");
		
		node.methodName.binding = mb;

		return null;
	}
	
	public AbstractBinding visit(WoolNew node) {
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		return null;
	}
	
	public AbstractBinding visit(WoolProgram node) {

		
		for(ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		
		
		return null;
	}
	
	public AbstractBinding visit(WoolTerminal node) {
		
		if(node.binding == null) {

			
			
			if(node.terminalType == TerminalType.tID) { //now start checking at class definitons
				
				if(currentMethod != null) {
					int index = currentMethod.argumentDefinedHereAtChildIndex(node.token.getText());
					
					if(index != -1)
						node.binding = currentMethod.getChild(index).binding;
				}
					
				if(node.binding == null)
					node.binding = currentClassBinding.getClassDescriptor().getVariable(node.token.getText());
				
				
				
			} else if(node.terminalType == TerminalType.tType) {
				node.binding = tm.getClassTable().lookup(node.token.getText());
			} else if(node.terminalType == TerminalType.tInt) {
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
	
	
	private boolean isTypeAToTypeBAllowed(String aType, String bType) {
		ClassBinding aB = (ClassBinding) tm.getClassTable().lookup(aType);
		ClassBinding bB = (ClassBinding) tm.getClassTable().lookup(bType);
		
		if(aB == bB) return true;
		
		if(aB == null || bB == null) return false;
		
		return isTypeAToTypeBAllowed(aB.getClassDescriptor().inherits, bType);
	}

}
