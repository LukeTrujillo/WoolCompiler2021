package wool.symbol.tables;

import java.util.HashMap;

import wool.symbol.bindings.AbstractBinding;
import wool.utility.WoolException;

public class SymbolTable {

	public static int nextTableNumber = 0;

	private HashMap<String, AbstractBinding> elements;
	private SymbolTable parentTable;
	private final int scopeLevel;
	private int tableNumber;

	public SymbolTable(SymbolTable parentTable) {
		this.parentTable = parentTable;

		if (parentTable == null) {
			this.scopeLevel = 1;
		} else {
			this.scopeLevel = parentTable.getScopeLevel() + 1;
		}

		this.elements = new HashMap<String, AbstractBinding>();

		this.tableNumber = nextTableNumber++;
	}

	public AbstractBinding add(String key, AbstractBinding value) {
		if (elements.containsKey(key)) {
			throw new WoolException("Attempt to add a duplicate " + value.getClass().getName() + " with key: " + key
					+ " to " + this.getClass().getName());
		}
		elements.put(key, value);
		return value;
	}

	public AbstractBinding lookup(String key) {
		AbstractBinding result = null;
		result = elements.get(key);
		
		if (result == null && parentTable != null) {
			result = parentTable.lookup(key);
		}
		
		return result;
	}
	
	@Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("  parent scope level: " 
    		+ (parentTable == null ? "none" : parentTable.getScopeLevel()) + '\n');

        sb.append("  scopeLevel: " + scopeLevel + '\n');
    	sb.append("  elements: \n");
    	for (AbstractBinding b : elements.values()) {
    	    sb.append("  ");
    	    sb.append(b.toString());
    	}
    	return sb.toString();
    }
	
	public AbstractBinding probe(String key) { return elements.get(key); }
	

	public SymbolTable getParentTable() {
		return parentTable;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public HashMap<String, AbstractBinding> getElements() {
		return elements;
	}

	public int getScopeLevel() {
		return scopeLevel;
	}

}
