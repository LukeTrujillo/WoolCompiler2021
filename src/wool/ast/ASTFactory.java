package wool.ast;

public class ASTFactory {
	
	public static CoolText makeCoolText() { return new CoolText(); }
	
	public static Type makeType(String className) {
		return new Type(className);
	}


}
