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

		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		return null;
	}

	@Override
	public AbstractBinding visit(WoolCompare node) {
		visitChildren(node); // they should all have static binding

		ClassBinding lastStaticType = null;

		node.staticType = tm.getClassBindingFromString("Bool"); // compares will always eval to this
		switch (node.tokenText) {
		case "<":
		case "<=":
		case ">":
		case ">=":
			lastStaticType = null;
			for (ASTNode child : node.getChildren()) {
				if (lastStaticType != null) {

					if (!lastStaticType.getClassDescriptor().className.equals("Int"))
						throw new WoolException("<, <=, >, and >= can only be used with Int");
				}

				lastStaticType = child.staticType;
			}

			return null;
		case "=":
		case "~=":
			lastStaticType = null;
			for (ASTNode child : node.getChildren()) {
				if (lastStaticType != null) {

					if (!boundTo(lastStaticType, child.staticType) && !boundTo(child.staticType, lastStaticType))
						throw new WoolException("=, ~= must be used to the same type attempted");
				}

				lastStaticType = child.staticType;
			}

		default:
			return null;
		}

	}

	@Override
	public AbstractBinding visit(WoolMath node) {

		ClassBinding binding = null;
		for (ASTNode n : node.getChildren()) {
			n.accept(this);

			if (binding != null) {
				if (!boundTo(binding, n.staticType) && boundTo(n.staticType, binding))
					throw new WoolException("Static type mismatch in WoolMath node");

			}

			binding = n.staticType;
		}

		node.staticType = binding;

		if (node.getChildren().size() == 1 && !node.token.getText().equals("-")) {
			throw new WoolException(
					"WoolMath with one child has '" + node.token.getText() + "' operator; needs to be '-'");
		}

		return null;
	}

	@Override
	public AbstractBinding visit(WoolMethod node) {
		currentMethod = node;
		// verify the arguments
		List<String> args = node.binding.getMethodDescriptor().getArgumentTypes();

		int x = 0;
		for (String arg : args) {
			if (arg != null && arg.equals("int")) {
				node.binding.getMethodDescriptor().argumentTypes.set(x, "Int");
				arg = node.binding.getMethodDescriptor().argumentTypes.get(x);
			} else if (arg != null && arg.equals("boolean")) {
				node.binding.getMethodDescriptor().argumentTypes.set(x, "Bool");
				arg = node.binding.getMethodDescriptor().argumentTypes.get(x);
			}

			x++;

			AbstractBinding search = tm.getClassTable().lookup(arg);
			if (search == null)
				throw new WoolException("Type \"" + arg + "\" not registered in method " + node.binding.getSymbol());
		}

		// verify the return type
		String returnType = node.binding.getMethodDescriptor().returnType;

		if (returnType != null && returnType.equals("int")) {
			node.binding.getMethodDescriptor().returnType = "Int";
			returnType = node.binding.getMethodDescriptor().returnType;
		} else if (returnType != null && returnType.equals("boolean")) {
			node.binding.getMethodDescriptor().returnType = "Bool";
			returnType = node.binding.getMethodDescriptor().returnType;
		}

		AbstractBinding search = tm.getClassTable().lookup(returnType);

		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		if (node.binding.getMethodDescriptor().getMethodName().equalsIgnoreCase("<init>")) {
			currentMethod = null;
			return null;
		}

		if (search == null)
			throw new WoolException(
					"Return type \"" + returnType + "\" not registered in method " + node.binding.getSymbol());

		ClassBinding cb = tm.getClassBindingFromString(currentClassBinding.getClassDescriptor().className);
		while (cb.getClassDescriptor().inherits != null) {

			cb = tm.getClassBindingFromString(cb.getClassDescriptor().inherits);

			MethodBinding check = cb.getClassDescriptor()
					.getMethodBinding(node.binding.getMethodDescriptor().methodName);
			if (check != null) { // defined in parents

				if (!check.getMethodDescriptor().getReturnType().equals("SELF_TYPE") && check.getMethodDescriptor()
						.getReturnType() != node.binding.getMethodDescriptor().returnType) {
					throw new WoolException(node.binding.getMethodDescriptor().methodName
							+ " is defined in the parent class with a different return type ");
				}

				List<String> nodeArgs = node.binding.getMethodDescriptor().getArgumentTypes();
				List<String> parentArgs = check.getMethodDescriptor().getArgumentTypes();

				if (nodeArgs.size() != parentArgs.size())
					throw new WoolException(node.binding.getMethodDescriptor().methodName
							+ " is defined in the parent class with a different number of args");

				for (int t = 0; t < nodeArgs.size(); t++) {

					if (!nodeArgs.get(t).equals(parentArgs.get(t))) {
						throw new WoolException(node.binding.getMethodDescriptor().methodName
								+ " is defined in the parent class with different arg types");
					}
				}

			}

		}

		currentMethod = null;

		return null;
	}

	@Override
	public AbstractBinding visit(WoolMethodCall node) {

		visitChildren(node);

		node.calledIn = currentClassBinding;

		MethodBinding binding = null;
		String name = node.methodName;

		if (node.dispatch == DispatchType.mcLocal) { // load the self object
			binding = (MethodBinding) node.scope.lookup(name);

			if (binding == null) {
				ClassBinding parent = tm.getClassBindingFromString(currentClassBinding.getClassDescriptor().inherits);

				while (binding == null) {
					binding = parent.getClassDescriptor().getMethodBinding(name);

					if (parent.getClassDescriptor().inherits == null)
						break;

					parent = tm.getClassBindingFromString(parent.getClassDescriptor().inherits);
				}

			}

			if (binding == null)
				throw new WoolException(
						"self type not defined in class " + currentClassBinding.getClassDescriptor().className);

		} else if (node.dispatch == DispatchType.mcObject) {
			AbstractBinding ab = node.getObject().accept(this);
			if (ab == null)
				throw new WoolException(node.getObject() + " has no binding ");

			String starting = ab.getSymbolType();

			if (ab instanceof MethodBinding) {
				starting = currentClassBinding.symbolType;
			}

			if (node.getObject() instanceof WoolMethodCall) {

				MethodBinding inner = (MethodBinding) ((WoolMethodCall) node.getObject()).binding;

				String definedIn = inner.getClassWhereDefined();
				ClassBinding parent = tm.getClassBindingFromString(definedIn);
				inner = parent.getClassDescriptor().getMethodBinding(((WoolMethodCall) node.getObject()).methodName);

				String context = inner.getMethodDescriptor().getReturnType();

				parent = tm.getClassBindingFromString(context);
				binding = parent.getClassDescriptor().getMethodBinding(name);

			}

			ClassBinding parent = tm.getClassBindingFromString(starting);

			while (binding == null) {
				binding = parent.getClassDescriptor().getMethodBinding(name);

				if (parent.getClassDescriptor().inherits == null || binding != null)
					break;

				parent = tm.getClassBindingFromString(parent.getClassDescriptor().inherits);
			}

			if (binding == null)
				throw new WoolException("No binding found for the object context method call " + node.methodName);
		}
		node.binding = binding;

		if (binding.getMethodDescriptor().getReturnType() != null)
			node.staticType = tm.getClassBindingFromString(binding.getMethodDescriptor().getReturnType());

		return binding;
	}

	public AbstractBinding visit(WoolProgram node) {

		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		return null;
	}

	public AbstractBinding visit(WoolTerminal node) {

		if (node.binding == null) {
			if (node.terminalType == TerminalType.tID) {
				node.binding = node.scope.lookup(node.token.getText());

				if (node.token.getText().equals("this")) {
					node.binding = currentClassBinding;
				}

			} else if (node.terminalType == TerminalType.tType) {
				node.binding = tm.getClassBindingFromString(node.token.getText());
			}
			if (node.terminalType == TerminalType.tInt) {
				node.binding = tm.getClassTable().lookup("Int");
			} else if (node.terminalType == TerminalType.tBool) {
				node.binding = tm.getClassTable().lookup("Bool");
			} else if (node.terminalType == TerminalType.tStr) {
				node.binding = tm.getClassTable().lookup("Str");
			}
		}

		if (node.binding == null && (node.terminalType != TerminalType.tMethod)
				&& node.terminalType != TerminalType.tNull) {
			throw new WoolException("Undefined variable " + node.token.getText() + " used");
		}

		node.staticType = tm.getClassBindingFromString(node.binding.getSymbolType());

		return node.binding;
	}

	public AbstractBinding visit(WoolVariable node) {

		ObjectBinding binding = node.binding;

		AbstractBinding search = null;

		String symbolType = node.binding.getSymbolType();
		if (symbolType.equals("int")) {
			node.binding.symbolType = "Int";
		} else if (symbolType.contentEquals("boolean")) {
			node.binding.symbolType = "Bool";
		}

		search = tm.getClassTable().lookup(node.binding.getSymbolType());
		if (search == null)
			throw new WoolException("Type \"" + node.binding.getSymbolType() + "\" not registered");

		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		node.staticType = tm.getClassBindingFromString(node.binding.getSymbolType());

		if (currentMethod == null) { // check that parent classes dont have definition

			ClassBinding current = currentClassBinding;

			while (current.getClassDescriptor().inherits != null) {
				current = tm.getClassBindingFromString(current.getClassDescriptor().inherits);
				

				if (current.getClassDescriptor().getVariable(node.binding.getSymbol()) != null) {
					throw new WoolException("parent-child variable redefinition");
				}

			}

		}

		return search;
	}

	public AbstractBinding visit(WoolWhile node) {
		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		return null;
	}

	public AbstractBinding visit(WoolType node) {

		currentClassBinding = node.binding;

		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}
		return null;
	}

	public AbstractBinding visit(WoolIf node) {
		for (ASTNode n : node.getChildren()) {
			n.accept(this);
		}

		return null;
	}

	public boolean boundTo(ClassBinding child, ClassBinding parent) {

		if (child.getClassDescriptor().className == "Int" && parent.getClassDescriptor().className != "Int")
			return false;

		if (child.getClassDescriptor().className == "Bool" && parent.getClassDescriptor().className != "Bool")
			return false;

		if (child.equals(parent))
			return true;

		ClassBinding current = null;

		while (child.getClassDescriptor().inherits != null) {
			current = tm.getClassBindingFromString(child.getClassDescriptor().inherits);

			if (current.equals(parent))
				return true;
		}

		return false;

	}

}
