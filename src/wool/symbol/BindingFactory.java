/*******************************************************************************
 * This file is used in CS4533/CS544, Compiler Construction & Techniques of Language
 * Translation, Worcester Polytechnic Institute. Copyright (c) 2016-18 Gary F. Pollice All
 * rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this distribution, and
 * is available at http://www.eclipse.org/legal/epl-v10.html Copyright ©2016 Gary F.
 * Pollice
 *******************************************************************************/

package wool.symbol;

import org.antlr.v4.runtime.Token;
import static wool.symbol.Binding.BindingType.*;

/**
 * Factory for producing bindings.
 * 
 * @version Jul 27, 2018
 */
public class BindingFactory
{

    /**
     * Factory method for the object binding
     * @param symbol the ID name
     * @param symbolType the 
     * @param t
     * @return
     */
    public static ObjectBinding makeObjectBinding(String symbol, String symbolType, Token t)
    {
        return new ObjectBinding(symbol, symbolType, t);
    }

//    public static MethodBinding makeMethodBinding(MethodDescriptor md, Token t)
//    {
//        return new MethodBinding(md, t);
//    }

    public static MethodBinding makeMethodBinding(MethodDescriptor md, Token t,
            String className)
    {
        MethodBinding mb = new MethodBinding(md, t);
        mb.classWhereDefined = className;
        return mb;
    }

    public static ClassBinding makeClassBinding(ClassDescriptor cd)
    {
        return new ClassBinding(cd);
    }

    /* ************************************************************ 
     * The concrete binding
     * classes.
     */
    /**
     * The object binding. This is used for all identifiers when they are 
     * declared. If the binding is a feature (defined at the class level)
     * it needs to be placed in the appropriate class descriptor.
     * @version Aug 3, 2018
     */
    public static class ObjectBinding extends AbstractBinding
    {
        /**
         * Default constructor
         * @param symbol the ID
         * @param symbolType the type (class) of the ID
         * @param t the token associated with the ID
         */
        public ObjectBinding(String symbol, String symbolType, Token t)
        {
            super(symbol, symbolType, bObject, t);
        }

        /*
         * @see cool.symbol.Binding#getAddress()
         */
        @Override
        public int getAddress()
        {
            return address;
        }

        /*
         * @see cool.symbol.Binding#setAddress(int)
         */
        @Override
        public void setAddress(int address)
        {
            this.address = address;
        }
    }

    /**
     * A binding class for method bindings. This binding does not get
     * placed in the symbol table directly.
     * @version Aug 3, 2018
     */
    public static class MethodBinding extends AbstractBinding
    {
        MethodDescriptor md;

        /**
         * Default constructor
         * @param md the method descriptor for this binding
         * @param t the token for this binding
         */
        public MethodBinding(MethodDescriptor md, Token t)
        {
            super(md.methodName, md.returnType, bMethod, t);
            this.md = md;
        }

        /*
         * @see cool.symbol.Binding#getMethodDescriptor()
         */
        @Override
        public MethodDescriptor getMethodDescriptor()
        {
            return md;
        }

        /*
         * @see cool.symbol.AbstractBinding#extraInfo()
         */
        @Override
        public String extraInfo()
        {
            return md == null ? "[]" : md.toString();
        }
        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((md == null) ? 0 : md.hashCode());
            return result;
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (!(obj instanceof MethodBinding)) {
                return false;
            }
            MethodBinding other = (MethodBinding) obj;
            if (md == null) {
                if (other.md != null) {
                    return false;
                }
            } else if (!md.equals(other.md)) {
                return false;
            }
            return true;
        }
    }


    /**
     * A binding for a class. This does not get placed directly in a 
     * symbol table.
     * @version Aug 3, 2018
     */
    public static class ClassBinding extends AbstractBinding
    {
        public ClassDescriptor descriptor;

        /**
         * Default constructor.
         * @param cd the class descriptor
         */
        public ClassBinding(ClassDescriptor cd)
        {
            super(cd.className, cd.className, bType, null);
            this.descriptor = cd;
        }

        /*
         * @see cool.symbol.Binding#getClassDescriptor()
         */
        @Override
        public ClassDescriptor getClassDescriptor()
        {
            return descriptor;
        }

        /*
         * @see cool.symbol.AbstractBinding#extraInfo()
         */
        @Override
        public String extraInfo()
        {
            return descriptor.toString();
        }
    }
}
