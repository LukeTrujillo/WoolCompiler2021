package wool.symbol.bindings;

import org.antlr.v4.runtime.Token;

import wool.symbol.tables.TableManager;

public class ObjectBinding extends AbstractBinding {
	


	public ObjectBinding(String symbol, String symbolType, Token token) {
		super(symbol, symbolType, BindingType.OBJECT, token);
	}

	public static ObjectBinding makeObjectBinding(String symbol, String symbolType, Token t) {
		return new ObjectBinding(symbol, symbolType, t);
	}

}
