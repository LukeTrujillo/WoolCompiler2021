/*******************************************************************************
 * This files was developed for CS4533: Techniques of Programming Language Translation
 * and/or CS544: Compiler Construction
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Copyright ©2020-21 Gary F. Pollice
 *******************************************************************************/

package wool.utility;

import java.util.*;
import wool.lexparse.WoolBaseVisitor;
import wool.lexparse.WoolParser.*;

/**
 * This is a pretty printer using the parse tree.
 */
public class ParseTreePrinter extends WoolBaseVisitor<String>
{
	private int indentLevel;
	private StringBuilder printedTree;
	private String indentString;
	
	/**
	 * Default constructor
	 */
	public ParseTreePrinter()
	{
		indentLevel = 0;
		indentString = "  ";
	}

	@Override
	public String visitProgram(ProgramContext ctx)
	{
		printedTree = new StringBuilder(">>> Program Start <<<\n");
		super.visitProgram(ctx);
		return printedTree.toString();
	}

	public String visitClassDef(ClassDefContext ctx)
	{
		indentLevel++;
		indent();
		addText("class ", ctx.className.getText());
		if (ctx.inherits != null) {
			addText(" inherits ", ctx.inherits.getText());
		}
		indent();
		addText("{");
		super.visitClassDef(ctx);
		indent();
		addText("}\n");
		indentLevel--;
		return null;
	}
	
	@Override
	public String visitClassBody(ClassBodyContext ctx)
	{
		indentLevel++;
		for (VariableDefContext v : ctx.variables) {
			v.accept(this);
		}
		for (MethodContext m : ctx.methods) {
			m.accept(this);
		}
		indentLevel--;
		return null;
	}

	@Override
	public String visitVariableDef(VariableDefContext ctx)
	{
		indent();
		addText(ctx.ID().getText(), " : ", ctx.type.getText());
		if (ctx.initializer != null) {
			addText(" <- ", ctx.initializer.accept(this));
		}
		addText(";");
		return null;
	}

	@Override
	public String visitMethod(MethodContext ctx)
	{
		addText("\n");
		indent();
		addText(ctx.ID().getText(), "(");
		String comma = "";
		for (FormalContext f : ctx.formals) {
			addText(comma, f.name.getText(), " : ", f.type.getText());
			comma = ", ";
		}
		addText(") : ", ctx.type.getText());
		indent();
		addText("{");
		indentLevel++;
		for (VariableDefContext v : ctx.vars) {
			v.accept(this);
		}
		ctx.expr().accept(this);
		indentLevel--;
		indent();
		addText("}");
		return null;
	}


	/* *************************************************
	 * Expressions return the string.
	 ***************************************************/
	@Override
	public String visitFullMethodCall(FullMethodCallContext ctx)
	{
		String object = ctx.object.accept(this);
		String methodName = ctx.methodName.getText();
		List<String> arguments = new LinkedList<String>();
		for (ExprContext e : ctx.args) {
			arguments.add(e.accept(this));
		}
		StringBuilder sb = new StringBuilder(object + "." + methodName + "(");
		if (!arguments.isEmpty()) {
			sb.append(arguments.remove(0));
			for (String s : arguments) {
				sb.append(", ");
				sb.append(s);
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public String visitNewExpr(NewExprContext ctx)
	{
		return "new " + ctx.type.getText();
	}

	@Override
	public String visitIsNullExpr(IsNullExprContext ctx)
	{
		return "isnull " + ctx.expr().accept(this);
	}

	/* ********************************** helpers ********************************** */
	private void indent()
	{
		printedTree.append('\n');
		for (int i = 0; i < indentLevel; i++) {
			printedTree.append(indentString);
		}
	}
	
	private void addText(String ...strings)
	{
		for (String s : strings) {
			printedTree.append(s);
		}
	}
}
