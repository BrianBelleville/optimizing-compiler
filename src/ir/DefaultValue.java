package ir;

import support.Identifier;

public class DefaultValue extends NamedValue {
    public DefaultValue(Identifier varName) {
        super("var_" + varName.getString());
    }
}
