package wool.codegen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import wool.ast.WoolProgram;
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
	
	Stack<Label> jumps;
	Stack<ObjectBinding> localTypes;

	
	
	//int nextLocalAddr;
	
	boolean inConstructor;


	private final static String DEFAULT_PACKAGE = "wool/";
	
	private TableManager tm;
	
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
		}
		
		return null;
	}
	
	
	@Override
	public byte[] visit(WoolMethod node) {
		
		MethodDescriptor descriptor = node.binding.getMethodDescriptor();
		
		String signature = this.getMethodTypeString(descriptor);
		mv = cw.visitMethod(ACC_PUBLIC, descriptor.methodName, signature, null, null);
		mv.visitCode();
		
		if(node.binding.getMethodDescriptor().methodName == "<init>") {
			inConstructor = true;
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, DEFAULT_PACKAGE + currentClass.inherits, "<init>", "()V", false);
		}
	
		jumps = new Stack<Label>();
		
		localTypes  = new Stack<ObjectBinding>();
		localTypes.add(currentClass.getVariable("self"));
		
		for(ASTNode child : node.getChildren()) {
			
			if(child instanceof WoolAssign) {
				child = ((WoolAssign) child).getIdentifer();
			}
			
			if(child instanceof WoolVariable) {
				localTypes.add((ObjectBinding) child.binding);
			}
		}
	
		currentMethod = node;
		visitChildren(node); //TODO this may cause issue with variables under method dec but above expr list
		currentMethod = null;
		
		inConstructor = false;
		
		mv.visitInsn(methodReturn(descriptor.returnType)); //method return type
		
		System.out.println(ClassWriter.COMPUTE_FRAMES + " " +  localTypes.size());
		mv.visitMaxs(ClassWriter.COMPUTE_FRAMES, localTypes.size());
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
		
		System.out.println(node.getChildren());
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
			
			if(index != -1) { //it is a local variable
				mv.visitVarInsn(ISTORE, index + 1);	
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
			/*int index = currentMethod.argumentDefinedHereAtChildIndex(node.binding.symbol);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, index + 1);	*/
		}
		return null;
	}
	
	@Override
	public byte[] visit(WoolIf node) {
		node.getChild(0).accept(this);
		
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		
		mv.visitJumpInsn(((WoolCompare) node.getChild(0)).opcode, l2);
		mv.visitJumpInsn(GOTO, l1);
	
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME, this.getLocalTypeArray().length, this.getLocalTypeArray(), 2, new Object[] {DEFAULT_PACKAGE + this.currentClass.className, Opcodes.INTEGER});
		node.getChild(1).accept(this);
		mv.visitJumpInsn(GOTO, l3);
	
		mv.visitLabel(l1);
		mv.visitFrame(Opcodes.F_SAME, this.getLocalTypeArray().length,  this.getLocalTypeArray(),  2, new Object[] {DEFAULT_PACKAGE + this.currentClass.className, Opcodes.INTEGER});
		node.getChild(2).accept(this);
		mv.visitJumpInsn(GOTO, l3);
		
		mv.visitLabel(l3); //end0
		
		
		return null;
		
	}
	
	@Override
	public byte[] visit(WoolWhile node) {
		
		Label l1 = new Label();
		Label l2 = new Label();
		Label l3 = new Label();
		
	

		mv.visitLabel(l1);
		mv.visitFrame(Opcodes.F_SAME, this.getLocalTypeArray().length, this.getLocalTypeArray(), 0, null);
		node.getChild(0).accept(this);
		mv.visitJumpInsn(((WoolCompare) node.getChild(0)).opcode, l2);
		mv.visitJumpInsn(GOTO, l3);
		
		mv.visitLabel(l2);
		mv.visitFrame(Opcodes.F_SAME, this.getLocalTypeArray().length, this.getLocalTypeArray(), 0, null);
		node.getChild(1).accept(this);
		mv.visitJumpInsn(GOTO, l1);
		
		
		mv.visitLabel(l3);
		return null;
	}
	
	@Override
	public byte[] visit(WoolCompare node) {
		if(node.getChildren().size() == 1) { //flip it
			
		} else { //compare
			
			node.getChild(0).accept(this);
			node.getChild(1).accept(this);

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
				if(node.token.getText().contentEquals("true"))
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
					mv.visitVarInsn(ILOAD, index + 1);
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
	public byte[] visit(WoolMethodCall node) {
		
		if(node.dispatch == DispatchType.mcLocal) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, DEFAULT_PACKAGE + currentClass.className, node.getMethodName().binding.getSymbol(), this.getMethodTypeString(((MethodBinding)node.getMethodName().binding).getMethodDescriptor()), false);
			
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
		
		System.out.println(node.token.getText());
		
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
		if(type.equals("Int") || type.equals("int")) return "I";
		if(type.equals("Bool") || type.equals("boolean")) return "Z";
	
		return "L" + DEFAULT_PACKAGE + type + ";";
	}
	
	public boolean isMethodLocal(AbstractBinding binding) {
		return currentMethod != null && currentMethod.argumentDefinedHereAtChildIndex(binding.getSymbol()) != -1;
	}

	public Object[] getLocalTypeArray() {
		Object[] arr = new Object[localTypes.size()];
		
		for(int x = 0; x < localTypes.size(); x++) {
			ObjectBinding binding = localTypes.get(x);
			
			if(binding.getSymbolType().equals("Int") || binding.getSymbolType().equals("Bool")) {
				arr[x] = Opcodes.INTEGER;
			} else {
				arr[x] = DEFAULT_PACKAGE + binding.getSymbolType();
			}
		}
		
		return arr;
	}
	
	
}