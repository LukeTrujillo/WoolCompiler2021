package wool.ast;


import org.antlr.v4.runtime.Token;

import wool.ast.WoolTerminal.TerminalType;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.tables.TableManager;

public class ASTFactory {
	
	private static TableManager tm = TableManager.getInstance(); 
	
	public static WoolProgram makeWoolProgram() { return new WoolProgram(); }
	
	public static WoolType makeWoolType(ClassBinding binding) {
		return new WoolType(binding);
	}
	
	public static WoolMethod makeWoolMethod(MethodBinding binding) {
		return new WoolMethod(binding);
	}
	
	public static WoolVariable makeWoolVariable(ObjectBinding binding) {
		return new WoolVariable(binding);
	}
	
	public static WoolMethodCall makeMethodCall(DispatchType dispatch) {
		return new WoolMethodCall(dispatch);
	}

	public static WoolTerminal makeID(AbstractBinding b) { return new WoolTerminal(b); }
	public static WoolTerminal makeID(Token t) { return new WoolTerminal(t, TerminalType.tID); }
	public static WoolTerminal makeConstant(Token t, TerminalType type) { return new WoolTerminal(t, type); }
	public static WoolTerminal makeTypeTerminal(Token t) { return new WoolTerminal(t, WoolTerminal.TerminalType.tType); }
}
