package wool;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;



public class UseRobot extends Wheel{

	
	public UseRobot() {
		/*
		 * Variable Initialization Tests
		 */
		assert five == 5: "five != 5";
		assert truth == false : "truth != false";
		assert zero == 0 : "zero != 0";
		
		/*
		 * Function Return Tests
		 */
		assert simpleBoolReturnFalse() == false : "simpleBoolReturnFalse() != false";
		assert simpleBoolReturnTrue() == false : "simpleBoolReturnTrue() != true";
		assert simpleIntReturnTest() == 5: "simpleIntReturnTest() != 5";
		assert simpleLocalVariableReturnTest() == 3 : "simpleLocalVariableReturnTest() != 3";
		
		assert localVarWithInnerAssignment(5) == 5 : "localVarWithInnerAssignment != 5";
		assert innerMethodAssignment(5) == 6 : "innerMethodAssignment(5) != 6";
		
		assert this.callMethod() == 9 : "callMethod does not equal 9";
		
		/*
		 * Polymorphism
		 */
		assert this.parentInheritedMethod() == false : "method not inherited by the child class";
		
		/*
		 * Mathmatics
		 */
		assert this.neg == -1;
		assert this.add == -2;
		assert this.mult == 6;
		assert this.paren == 3;
		assert this.precedence == 10;
		
		assert this.sign(-5) == -1;
		
		assert this.sign(2) == 1;
		assert this.sign(0) == 0;
		
		this.countdown(100);
		
		System.out.println("all assertions passed");
	}

	public static void main(String args[]) throws IOException {
		
		 Class cls = Wheel.class;
	        InputStream inputStream 
	            = cls.getResourceAsStream(cls.getSimpleName() + ".class");
	        ClassReader reader = new ClassReader(inputStream);
	        ClassVisitor visitor = new TraceClassVisitor(new PrintWriter(System.out));
	        //ClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));  // ASMified code
	        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
	        
	       UseRobot robot = new UseRobot();
	}

}
