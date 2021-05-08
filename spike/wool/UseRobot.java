package wool;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;



public class UseRobot extends wool.Test {

	
	public UseRobot() {
		this.run();
	}

	public static void main(String args[]) throws IOException {

		
		 Class cls = Test.class;
	        InputStream inputStream 
	            = cls.getResourceAsStream(cls.getSimpleName() + ".class");
	        ClassReader reader = new ClassReader(inputStream);
	        ClassVisitor visitor = new TraceClassVisitor(new PrintWriter(System.out));
	        //ClassVisitor visitor = new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out));  // ASMified code
	        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
	        
	    try {
	       UseRobot robot = new UseRobot();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

}
