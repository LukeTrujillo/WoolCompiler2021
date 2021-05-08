package wool.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;


import wool.ast.ASTNode;
import wool.ast.ASTVisitor;
import wool.ast.WoolAssign;
import wool.ast.WoolCompare;
import wool.ast.WoolExprList;
import wool.ast.WoolIf;
import wool.ast.WoolMath;
import wool.ast.WoolMethod;
import wool.ast.WoolMethodCall;
import wool.ast.WoolMethodCall.DispatchType;
import wool.ast.WoolNew;
import wool.ast.WoolProgram;
import wool.ast.WoolSelect;
import wool.ast.WoolSelectAlt;
import wool.ast.WoolTerminal;
import wool.ast.WoolTerminal.TerminalType;
import wool.ast.WoolType;
import wool.ast.WoolVariable;
import wool.ast.WoolWhile;
import wool.symbol.bindings.AbstractBinding;
import wool.symbol.bindings.ClassBinding;
import wool.symbol.bindings.MethodBinding;
import wool.symbol.bindings.ObjectBinding;
import wool.symbol.descriptors.ClassDescriptor;
import wool.symbol.descriptors.MethodDescriptor;
import wool.symbol.tables.TableManager;

import static org.objectweb.asm.Opcodes.*;

import java.io.PrintWriter;
import java.util.ArrayList;
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
	String currentMethodReturnType;
	
	
	boolean inConstructor;


	private final static String DEFAULT_PACKAGE = "wool/";
	
	private TableManager tm;
	
	String lastReturnType = "";
	
	public IRCreator() {
		classes = new HashMap<String, byte[]>();
		currentMethod = null;
		inConstructor = false;

		
	}

	@Override
	public byte[] visit(WoolProgram node) {
		
		for(ASTNode type : node.getChildren()) {
			WoolType tcast = (WoolType) type;
			byte[] byteCode = this.visit(tcast);
			
			classes.put((tcast.binding).getClassDescriptor().className, byteCode);
			
			/*ClassReader reader = new ClassReader(byteCode);
			
			ClassVisitor visitor = new TraceClassVisitor(new PrintWriter(System.out));
		        reader.accept(visitor, ClassReader.EXPAND_FRAMES);*/
		}
		
		return null;
	}
	
	
	@Override
	public byte[] visit(WoolMethod node) {
		
		MethodDescriptor descriptor = node.binding.getMethodDescriptor();
		
		String signature = this.getMethodTypeString(node.binding);
		mv = cw.visitMethod(ACC_PUBLIC, descriptor.methodName, signature, null, null);
		mv.visitCode();
		
		if(node.binding.getMethodDescriptor().methodName == "<init>") {
			inConstructor = true;
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PACKAGE + currentClass.inherits, "<init>", "()V", false);
		}
		
	
		currentMethod = node;
		visitChildren(node); //TODO this may cause issue with variables under method dec but above expr list
		currentMethod = null;
		
		inConstructor = false;
		
		mv.visitInsn(methodReturn(descriptor.returnType)); //method return type
		
		mv.visitMaxs(0, 0);
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
	public byte[] visit(WoolExprList node) {
		visitChildren(node);
		
		return null;
	}


	@Override
	public byte[] visit(WoolAssign node) {
		
		ASTNode setThis = node.getIdentifer();
		ASTNode withThis = node.getSetValue();

		if(currentMethod != null) {
			int index = currentMethod.argumentDefinedHereAtChildIndex(setThis.binding.symbol);
			
			if(index == -1) {
				
				mv.visitIntInsn(ALOAD, 0);
			}
			
			withThis.accept(this); //whatever the value is should be on the TOS
			if(node.parent instanceof WoolAssign) {
				mv.visitInsn(DUP_X1);
			}
			
			
			if(index != -1) { //it is a local variable
				
				if(setThis.binding.getSymbolType().equals("Int") || setThis.binding.getSymbolType().equals("Bool")) {
						mv.visitVarInsn(ISTORE, index + 1);	
				} else {
						mv.visitVarInsn(ASTORE, index + 1);	
					
				}
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
		} else if(node.parent instanceof WoolMethod) {
			int index = currentMethod.argumentDefinedHereAtChildIndex(node.binding.symbol);
			
			if(node.binding.getSymbolType() == "Int" || node.binding.getSymbolType() == "Bool") {
				mv.visitInsn(ICONST_0);
				mv.visitVarInsn(ISTORE, index + 1);	
			} else if(node.binding.getSymbolType() == "Str") {
				mv.visitLdcInsn("");
				mv.visitMethodInsn(INVOKESTATIC, DEFAULT_PACKAGE + "Str", "makeStr", "(Ljava/lang/String;)Lwool/Str;", false);
				mv.visitVarInsn(ISTORE, index + 1);	
			} else { //go null
				mv.visitInsn(ACONST_NULL);
				mv.visitVarInsn(ISTORE, index + 1);	
			}
		}
		return null;
	}
	
	@Override
	public byte[] visit(WoolIf node) {
		Label t = new Label();
		Label f = new Label();
		Label exit = new Label();
		
		
		node.getChild(0).accept(this);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(Opcodes.IF_ICMPEQ, t);
		mv.visitJumpInsn(GOTO, f);
		
		mv.visitLabel(f);
		node.getChild(2).accept(this);
		mv.visitJumpInsn(GOTO, exit);
	
		mv.visitLabel(t);
		node.getChild(1).accept(this);
		mv.visitJumpInsn(GOTO, exit);
		
		mv.visitLabel(exit); //end0
		
		return null;
		
	}
	
	@Override
	public byte[] visit(WoolWhile node) {
		
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();

		mv.visitLabel(l1);
	
		node.getChild(0).accept(this);
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(Opcodes.IF_ICMPEQ, l2);
		mv.visitJumpInsn(GOTO, l3);
		
		mv.visitLabel(l2);
		node.getChild(1).accept(this);
		mv.visitJumpInsn(GOTO, l1);
		
		
		mv.visitLabel(l3);
		return null;
	}
	@Override
	public byte[] visit(WoolSelect node) {

		
		for(ASTNode child : node.getChildren()) {
			Label next = new Label();
			
		
			child.getChild(0).accept(this); //eval the condition, return 1 or 0 
			
			Label t = new Label();
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(Opcodes.IF_ICMPEQ, t);
			mv.visitJumpInsn(GOTO, next);

		
			mv.visitLabel(t);
			child.getChild(1).accept(this);
			mv.visitInsn(methodReturn(currentMethod.binding.getMethodDescriptor().returnType)); //method return type
			
			mv.visitLabel(next);
		}
		
		mv.visitInsn(ICONST_1); //this will need to change depending upon the end goal
	
		return null;
	}
	
	@Override
	public byte[] visit(WoolSelectAlt node) {
		node.getChild(0).accept(this);
		
		return null;
	}
	
	@Override
	public byte[] visit(WoolCompare node) {
		if(node.getChildren().size() == 1) { //flip it
			Label t = new Label();
			Label f = new Label();
			
			Label exit = new Label();
			
			node.getChild(0).accept(this);
			
			mv.visitJumpInsn(node.getOpcode(), t);
			mv.visitJumpInsn(GOTO, f);
			
			mv.visitLabel(t);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, exit);
			
			mv.visitLabel(f);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit);
			
			
			mv.visitLabel(exit);
			
			
		} else { //compare
			
			Label t = new Label();
			Label f = new Label();
			
			Label exit = new Label();
			
			node.getChild(0).accept(this);
			node.getChild(1).accept(this);
			
			mv.visitJumpInsn(node.getOpcode(), t);
			mv.visitJumpInsn(GOTO, f);
			
			mv.visitLabel(t);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, exit);
			
			mv.visitLabel(f);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, exit);
			
			
			mv.visitLabel(exit);
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
				int number = Integer.parseInt(node.token.getText());
				
				if(node.parent instanceof WoolMath && node.parent.getChildren().size() == 1) { //it is just a negative node
					number = number * -1;
				}
				
				mv.visitIntInsn(BIPUSH, number);
				break;
			
			case tBool:
				//mv.visitIntInsn(ALOAD, 0);
				if(node.token.getText().contentEquals("true")) {
					mv.visitInsn(ICONST_1);
				} else {
					mv.visitInsn(ICONST_0);
				}
				break;
			case tID:
				int index = currentMethod.argumentDefinedHereAtChildIndex(node.binding.getSymbol());
				if(node.token.getText().equals("this")) {
					mv.visitVarInsn(ALOAD, 0);
				} else if(!isMethodLocal(node.binding))  {
					mv.visitIntInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, DEFAULT_PACKAGE + currentClass.className, node.binding.getSymbol(), this.getTypeString(node.binding.getSymbolType()));
				} else if(!(node.binding.getSymbolType().equals("Int") || node.binding.getSymbolType().equals("Bool"))) {
					mv.visitVarInsn(ALOAD, index + 1);
				} else {
					mv.visitVarInsn(ILOAD, index + 1);
				}
				break;
			case tStr:
				mv.visitLdcInsn(node.token.getText().substring(1, node.token.getText().length() - 1));
				mv.visitMethodInsn(INVOKESTATIC, DEFAULT_PACKAGE + "Str", "makeStr", "(Ljava/lang/String;)Lwool/Str;", false);
				lastReturnType = "Str";
				break;
			case tType:
				mv.visitTypeInsn(NEW, DEFAULT_PACKAGE + node.binding.getSymbolType());
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PACKAGE + node.binding.getSymbolType(), "<init>", "()V", false);
				break;
			case tNull:
				mv.visitInsn(ACONST_NULL);
				break;
			}
		
		return null;
	}
	
	@Override
	public byte[] visit(WoolMethodCall node) {
	
		String owner = "";
		String name = "";
		String signature = "";
		
		if(node.dispatch == DispatchType.mcLocal) {
		
			mv.visitVarInsn(ALOAD, 0);
			
			
			for(ASTNode child : node.getChildren()) {
				child.accept(this);
			}
			
			owner = DEFAULT_PACKAGE + currentClass.className;
			name = node.binding.getSymbol();
			signature = this.getMethodTypeString(((MethodBinding)node.binding));
			
			
		} else if(node.dispatch == DispatchType.mcObject) {
			
			for(ASTNode child : node.getChildren()) {
				child.accept(this);
			}
			
			owner = DEFAULT_PACKAGE + ((WoolMethodCall) node).getCallerType();
			name = node.binding.getSymbol();
			signature = this.getMethodTypeString(((MethodBinding)node.binding));
		}
	
		signature = signature.replaceAll("SELF_TYPE", node.getCallerType());
		
		mv.visitMethodInsn(INVOKEVIRTUAL, owner, name, signature, false);
		
		if(node.parent instanceof WoolExprList) {
			if(node.parent.getChild(node.parent.getChildren().size() - 1) != node && !signature.endsWith(")V")) {
				//this means its the last node in the expr list
				mv.visitInsn(POP);
			}
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
	
	@Override
	public byte[] visit(WoolMath node) {
		
		if(node.getChildren().size() == 1) {
			node.getChild(0).accept(this);
			return null;
		}
		
		node.getChild(0).accept(this);
		node.getChild(1).accept(this);
		
		
		if(node.token.getText().equals("+")) 
			mv.visitInsn(IADD);
		else if(node.token.getText().equals("-")) 
			mv.visitInsn(ISUB);
		else if(node.token.getText().equals("*")) 
			mv.visitInsn(IMUL);
		else if(node.token.getText().equals("/"))
			mv.visitInsn(IDIV);
		
		return null;
		
	}
	
	
	private String getMethodTypeString(MethodBinding mb) {
		MethodDescriptor md = mb.getMethodDescriptor();
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
		if(type.equals("Int") || type.equals("int")) return "I";
		if(type.equals("Bool") || type.equals("boolean")) return "Z";
		
		return "L" + DEFAULT_PACKAGE + type + ";";
	}
	
	public boolean isMethodLocal(AbstractBinding binding) {
		return currentMethod != null && currentMethod.argumentDefinedHereAtChildIndex(binding.getSymbol()) != -1;
	}

	
	
}