package wool.codegen;

import org.objectweb.asm.MethodVisitor;

public class LukesMethodVisitor extends MethodVisitor {
	
	boolean debug = true;

	public LukesMethodVisitor(int arg0) {
		super(arg0);
	}
}
