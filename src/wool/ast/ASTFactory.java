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
	
	public static WoolMethodCall makeMethodCall(WoolMethodCall.DispatchType dispatch) {
		return new WoolMethodCall(dispatch);
	}
	
	public static WoolExprList makeExprList() {
		return new WoolExprList();
	}
	
	public static WoolAssign makeAssignExpr() { return new WoolAssign(); }
	
	public static WoolIf makeIf() { return new WoolIf(); }
	public static WoolWhile makeWhile() { return new WoolWhile(); }

	public static WoolTerminal makeID(AbstractBinding b) { return new WoolTerminal(b); }
	public static WoolTerminal makeID(Token t) {
		return new WoolTerminal(t, TerminalType.tID); 
	}
	public static WoolTerminal makeConstant(Token t, TerminalType type) { 
		return new WoolTerminal(t, type); 
		}
	public static WoolTerminal makeTypeTerminal(Token t) { return new WoolTerminal(t, WoolTerminal.TerminalType.tType); }
	public static WoolNew makeWoolNew() { return new WoolNew(); }
	
	public static WoolMath makeWoolMath(Token op) { return new WoolMath(op); }
	public static WoolParen makeWoolParen() { return new WoolParen(); }
	
	public static WoolCompare nakeWoolCompare(Token op) { return new WoolCompare(op); }

}
