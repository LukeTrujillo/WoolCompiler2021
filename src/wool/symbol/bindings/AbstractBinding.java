package wool.symbol.bindings;

import org.antlr.v4.runtime.Token;

public abstract class AbstractBinding {
	
	private static final int NO_ADDRESS = Integer.MIN_VALUE;
	
	protected BindingType bindingType;
	public String symbol;

	protected String symbolType;

	protected String classWhereDefined;
	public Token token;
	protected int address;
	
	public AbstractBinding(String symbol, String symbolType, BindingType bindingType, Token token) {
		this.symbol = symbol;
		this.symbolType = symbolType;
		this.bindingType = bindingType;
		this.token = token;
		
		this.classWhereDefined = null;
		this.address = NO_ADDRESS;
	}
	
	
	public BindingType getBindingType() {
		return bindingType;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getSymbolType() {
		return symbolType;
	}
	
	public String getClassWhereDefined() {
		return classWhereDefined;
	}
	
	public Token getToken() {
		return token;
	}
	
	public int getAddress() {
		return address;
	}
	
	public void setAddress(int address) {
		this.address = address;
	}
	
	public void setClassWhereDefined(String classWhereDefined) {
		this.classWhereDefined = classWhereDefined;
	}

	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        sb.append("Binding [" + bindingType);
        sb.append(", " + symbol + " : " + symbolType);
        if (token != null) {
            sb.append(" (" + token.getCharPositionInLine() + "," + token.getLine() + ")");
        }
        if (classWhereDefined != null) {
            sb.append(", defined in " + classWhereDefined);
        }
        switch (bindingType) {
            case OBJECT:
                if (address != NO_ADDRESS) {
                    sb.append(", address=" + address);
                }
                break;
            case TYPE:
                sb.append('\n');
                sb.append("    descriptor:" + extraInfo());
                break;
            case METHOD:
                sb.append(",   descriptor: " + extraInfo());
                break;
        }
        sb.append("]");
        return sb.toString();
	}
	
	 /**
     * @return any extra information that is useful in printing
     */
	protected String extraInfo() {
        return null;
    }

	/*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((bindingType == null) ? 0 : bindingType.hashCode());
        result = prime * result
                + ((classWhereDefined == null) ? 0 : classWhereDefined.hashCode());
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((symbolType == null) ? 0 : symbolType.hashCode());
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractBinding)) {
            return false;
        }
        AbstractBinding other = (AbstractBinding) obj;
        if (bindingType != other.bindingType) {
            return false;
        }
        if (classWhereDefined == null) {
            if (other.classWhereDefined != null) {
                return false;
            }
        } else if (!classWhereDefined.equals(other.classWhereDefined)) {
            return false;
        }
        if (!symbol.equals(other.symbol)) {
            return false;
        }
        if (symbolType == null) {
            if (other.symbolType != null) {
                return false;
            }
        } else if (!symbolType.equals(other.symbolType)) {
            return false;
        }
        return true;
    }
}
