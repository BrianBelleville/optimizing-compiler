package ir;

import ir.base.Value;

public class NamedValue extends Value {
    private String name;
    public NamedValue(String name) {
	this.name = name;
    }

    @Override
    public boolean equals(Object o) {
	if(o instanceof NamedValue) {
	    NamedValue v = (NamedValue)o;
	    return this.name.equals(v.name);
	}
	return false;
    }

    @Override
    public int hashCode() {
	return name.hashCode();
    }
}
