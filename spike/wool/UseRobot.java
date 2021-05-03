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
		System.out.println("num expects 25, got " + this.num);
		System.out.println("guess expects true (1), got " + (this.guess == true));
	}

	public static void main(String args[]) throws IOException {

		UseRobot robot = new UseRobot();
		
		 Class cls = Wheel.class;
	        InputStream inputStream 
	            = cls.getResourceAsStream(cls.getSimpleName() + ".class");
	        ClassReader reader = new ClassReader(inputStream);
	        ClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));  // ASMified code
	        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

	}

}
