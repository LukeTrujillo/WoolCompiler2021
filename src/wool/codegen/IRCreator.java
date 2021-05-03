package wool.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import wool.ast.ASTNode;
import wool.ast.ASTVisitor;
import wool.ast.WoolAssignExpr;
import wool.ast.WoolMethod;
import wool.ast.WoolProgram;
import wool.ast.WoolTerminal;
import wool.ast.WoolType;
import wool.ast.WoolVariable;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.ClassDescriptor;
import wool.symbol.descriptors.MethodDescriptor;

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
	
	Stack<Label> guardLabelStack;
	Stack<Label> returnLabelStack;
	
	
	private final static String DEFAULT_PACKAGE = "wool/";
	
	
	public IRCreator() {
		classes = new HashMap<String, byte[]>();
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
	public byte[] visit(WoolAssignExpr node) {
		
		return null; 
	}
	
	/*
	 * Need to fix this later
	 */
	@Override
	public byte[] visit(WoolVariable node) {
		ObjectBinding binding = node.binding;
		
		fv = cw.visitField(ACC_PROTECTED, binding.symbol, this.getTypeString(binding.getSymbolType()), null, null);
		fv.visitEnd();
		
		return null;
	}
	
	@Override 
	public byte[] visit(WoolTerminal node) {
		mv.visitVarInsn(ALOAD, 0);
		
		switch(node.terminalType) {
		case tInt:
			int number = Integer.parseInt(node.token.getText());
			mv.visitIntInsn(BIPUSH, 25);
			break;
		case tBool:
			boolean guess = Boolean.parseBoolean(node.token.getText());
			mv.visitInsn(guess? ICONST_1 : ICONST_0);
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
			if(child instanceof WoolAssignExpr) {
				child = ((WoolAssignExpr) child).getIdentifer();
			}
			
			if(child instanceof WoolVariable) {
				WoolVariable variable = (WoolVariable) child;
				
				this.visit((WoolVariable) variable);
			}
		}
		
		
		
		MethodDescriptor md = node.binding.getClassDescriptor().getMethodDescriptor("init");
		
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PACKAGE + node.binding.getClassDescriptor().inherits, "<init>", "()V", false);
		
		//initalize the items in the variables
		for(ASTNode child : node.getChildren()) {
			if(child instanceof WoolAssignExpr) {
				WoolAssignExpr expr = (WoolAssignExpr) child;
			
				//load the terminal
				expr.getSetValue().accept(this);
				
				String targetVarName = expr.getIdentifer().binding.getSymbol();
				String targetVarType = this.getTypeString(expr.getIdentifer().binding.getSymbolType());
				
				mv.visitFieldInsn(PUTFIELD, DEFAULT_PACKAGE + currentClass.className, targetVarName, targetVarType);
				
			}
		}
		
		mv.visitInsn(RETURN);
		mv.visitMaxs(ClassWriter.COMPUTE_FRAMES, ClassWriter.COMPUTE_MAXS);
		mv.visitEnd();
			
		
		

		for(ASTNode child : node.getChildren()) {
			if(child instanceof WoolMethod) {
				WoolMethod m = (WoolMethod) child;
				
				this.visit((WoolMethod) m);
			}
		}
		
		
		//visit all of the children
		
		cw.visitEnd();
		return cw.toByteArray();
	}
	
	/*@Override
	public byte[] visit(WoolMethod node) {
		String methodName = node.binding.getMethodDescriptor().methodName;
		String typeString = this.getMethodTypeString(node.binding.getMethodDescriptor());
		
		mv = cw.visitMethod(ACC_PUBLIC, methodName, typeString, null, null);
		mv.visitCode();
		
		
		
		
		mv.visitEnd();
		
		return null;
	}*/
	
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
}