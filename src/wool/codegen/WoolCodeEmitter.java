package wool.codegen;

import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import wool.symbol.descriptors.ClassDescriptor;

public class WoolCodeEmitter {
	private static final String WOOL = "wool/";
	
	private ClassWriter cw;
	private FieldVisitor fv;
	private MethodVisitor mv;
	
	private ClassDescriptor currentClass;
	
	
	
	
	
	public void emitStartClass() {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER , WOOL + currentClass.className, null, WOOL + currentClass.inherits, null);
	}
	public byte[] emitEndClass() {
		cw.visitEnd();
		return cw.toByteArray();
	}
	
	public void emitStartConstructor() {
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);	
	}
	public void emitEndConstructor() {
		mv.visitInsn(RETURN);
		mv.visitMaxs(ClassWriter.COMPUTE_FRAMES, ClassWriter.COMPUTE_MAXS);
		mv.visitEnd();
	}

}
