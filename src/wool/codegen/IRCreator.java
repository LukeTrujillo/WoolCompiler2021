package wool.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import wool.ast.ASTFactory;
import wool.ast.ASTNode;
import wool.ast.ASTVisitor;
import wool.ast.WoolAssignExpr;
import wool.ast.WoolMethod;
import wool.ast.WoolProgram;
import wool.ast.WoolTerminal;
import wool.ast.WoolTerminal.TerminalType;
import wool.ast.WoolType;
import wool.ast.WoolVariable;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.ClassDescriptor;
import wool.symbol.descriptors.MethodDescriptor;
import wool.symbol.tables.TableManager;

import static org.objectweb.asm.Opcodes.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class IRCreator extends ASTVisitor<byte[]> {
	
	public Map<String, byte[]> classes;
	
	ClassWriter cw;
	MethodVisitor mv;
	FieldVisitor fv;
	
	ClassDescriptor currentClass;
	WoolMethod currentMethod;
	
	
	int nextLocalAddr;
	
	boolean inConstructor;

	
	private final static String DEFAULT_PACKAGE = "wool/";
	
	private TableManager tm;
	
	public IRCreator() {
		classes = new HashMap<String, byte[]>();
		currentMethod = null;
		inConstructor = false;
		
		this.tm = TableManager.getInstance();
	}

	@Override
	public byte[] visit(WoolProgram node) {
		
		for(ASTNode type : node.getChildren()) {
			WoolType tcast = (WoolType) type;
			byte[] byteCode = this.visit(tcast);
			
			classes.put((tcast.binding).getClassDescriptor().className, byteCode);
		}
		
		return null;
	}
	
	
	@Override
	public byte[] visit(WoolMethod node) {
		nextLocalAddr = 0;
		
		MethodDescriptor descriptor = node.binding.getMethodDescriptor();
		
		String signature = this.getMethodTypeString(descriptor);
		mv = cw.visitMethod(ACC_PUBLIC, descriptor.methodName, signature, null, null);
		mv.visitCode();
		
		if(node.binding.getMethodDescriptor().methodName == "<init>") {
			inConstructor = true;
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PACKAGE + currentClass.inherits, "<init>", "()V", false);
		}
		
		nextLocalAddr = 1 + descriptor.argumentTypes.size();
		
		currentMethod = node;
		visitChildren(node); //TODO this may cause issue with variables under method dec but above expr list
		currentMethod = null;
		
		inConstructor = false;
		
		mv.visitInsn(methodReturn(descriptor.returnType)); //method return type
		mv.visitMaxs(ClassWriter.COMPUTE_FRAMES, ClassWriter.COMPUTE_MAXS);
		mv.visitEnd();
		return null;
	}
	
	private int methodReturn(String returnType) {
		
		if(returnType == null) return RETURN;
		
		switch(returnType) {
		case "Bool":
		case "Int": return IRETURN;
		default: return ARETURN;
		}
		
	}


	@Override
	public byte[] visit(WoolAssignExpr node) {
		
		WoolTerminal setThis = (WoolTerminal)node.getIdentifer();
		ASTNode withThis = node.getSetValue();

		if(currentMethod != null) {
			int index = currentMethod.argumentDefinedHereAtChildIndex(setThis.binding.symbol);
			
			if(index == -1) {
				mv.visitIntInsn(ALOAD, 0);
			}
			
			withThis.accept(this); //whatever the value is should be on the TOS
			
			if(index != -1) { //it is a local variable
				mv.visitIntInsn(ISTORE, index + 1);	
				return null;
			}
			
		}
		
		mv.visitFieldInsn(PUTFIELD, DEFAULT_PACKAGE + currentClass.className, setThis.binding.symbol, this.getTypeString(setThis.binding.getSymbolType()));
		
		return null; 
	}
	
	/*
	 * Need to fix this later
	 */
	@Override
	public byte[] visit(WoolVariable node) {
		ObjectBinding binding = node.binding;
		
		if(currentMethod == null) {
			fv = cw.visitField(ACC_PROTECTED, binding.symbol, this.getTypeString(binding.getSymbolType()), null, null);
			fv.visitEnd();
		}
		
		return null;
	}
	
	@Override 
	public byte[] visit(WoolTerminal node) {
		
	
			/*
			 * This switch statement puts clas vars onto the stack
			 */
			switch(node.terminalType) {
			case tInt:
				mv.visitIntInsn(BIPUSH, Integer.parseInt(node.token.getText()));
				break;
			
			case tBool:
				//mv.visitIntInsn(ALOAD, 0);
				if(node.token.getText() == "true")
					mv.visitInsn(ICONST_1);
				else
					mv.visitInsn(ICONST_0);
				break;
			case tID:
				if(!isMethodLocal(node.binding))  {
					mv.visitIntInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, DEFAULT_PACKAGE + currentClass.className, node.binding.getSymbol(), this.getTypeString(node.binding.getSymbolType()));
				} else {
					int index = currentMethod.argumentDefinedHereAtChildIndex(node.binding.getSymbol());
					mv.visitIntInsn(ILOAD, index + 1);
				}
				break;
			case tStr:
				break;
			case tType:
				break;
			}
		
		return null;
	}
	
	
	@Override
	public byte[] visit(WoolType node) {
		currentClass = node.binding.getClassDescriptor();
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, DEFAULT_PACKAGE + node.binding.getClassDescriptor().className, null, DEFAULT_PACKAGE + node.binding.getClassDescriptor().inherits, null);
		
		
		for(ASTNode child : node.getChildren()) {
			if(child instanceof WoolVariable) {
				child.accept(this);
			}
		}
		
		for(ASTNode child : node.getChildren()) {
			if(child instanceof WoolMethod) {
				child.accept(this);
			}
		}

		
		cw.visitEnd();
		return cw.toByteArray();
	}
	
	
	private String getMethodTypeString(MethodDescriptor md) {
		String typeString = "(";
		
		for(String type : md.argumentTypes) {
			typeString += this.getTypeString(type);
		}
		
		typeString += ")";
		
		if(md.returnType == null) {
			typeString += "V";
		} else {
			typeString += this.getTypeString(md.returnType);
		}
		
		return typeString;
		
	}
	
	private String getTypeString(String type) {
		if(type.equals("Int")) return "I";
		if(type.equals("Bool")) return "Z";
	
		return "L" + DEFAULT_PACKAGE + type + ";";
	}
	
	public boolean isMethodLocal(AbstractBinding binding) {
		return currentMethod != null && currentMethod.argumentDefinedHereAtChildIndex(binding.getSymbol()) != -1;
	}

}