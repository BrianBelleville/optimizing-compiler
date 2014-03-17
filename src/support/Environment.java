package support;

import java.util.ArrayList;
import java.util.HashMap;

import ir.Value;
import ir.VariableReference;

public class Environment {
    private ArrayList< HashMap<Identifier, VariableReference>> env;
    private ArrayList< HashMap<Identifier, Type>> types;
    public Environment() {
	env = new ArrayList< HashMap<Identifier, VariableReference>>();
        types = new ArrayList< HashMap<Identifier, Type>>();
    }

    public void enter() {
	env.add(new HashMap<Identifier, VariableReference>());
        types.add(new HashMap<Identifier, Type>());
    }
    public void exit() {
	// pop the last element off of the list
	env.remove(env.size() - 1);
        types.remove(types.size() - 1);
    }
    public Value get(Identifier id) {
	// traverse the list of environments backwards
	for(int i = env.size() - 1; i >= 0; i--) {
	    Value rval = env.get(i).get(id);
	    if(rval != null) {
		return rval;
	    }
	}
	// exited the search without finding the Identifier, return null
	return null;
    }
    public void put(Identifier id, Value val) {
	// assignments always will occur in the outermost environment
        
        // add value as a variable reference, this way uses of the
        // variable will have information about what variable is being
        // referenced
	env.get(env.size() - 1).put(id, new VariableReference(id, val));
    }
    public Type getType(Identifier id) {
	// traverse the list of types backwards
	for(int i = types.size() - 1; i >= 0; i--) {
	    Type rval = types.get(i).get(id);
	    if(rval != null) {
		return rval;
	    }
	}
	// exited the search without finding the Identifier, return null
	return null;
    }
    public void putType(Identifier id, Type val) {
	types.get(types.size() - 1).put(id, val);
    }

    public void freezeGlobals() {
        // global variables should always be in the outermost type
        // environment
        HashMap<Identifier, Type> globals = types.get(0);    
        for(Identifier id : globals.keySet()) {
            globals.get(id).freeze();
        }
    }
}
