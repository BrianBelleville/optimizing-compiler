package support;

import java.util.ArrayList;
import java.util.HashMap;

import ir.base.Value;

public class Environment {
    private ArrayList< HashMap<Identifier, Value>> env;
    private ArrayList< HashMap<Identifier, Type>> types;
    public Environment() {
	env = new ArrayList< HashMap<Identifier, Value>>();
        types = new ArrayList< HashMap<Identifier, Type>>();
    }

    // todo: enter and exit will create a lot of uneccesary entries in
    //       the types table. New types for variables can only occur
    //       at function declarations, while new values may be added for
    //       basic blocks. However the basic block level enter and exit
    //       seems to work.
    public void enter() {
	env.add(new HashMap<Identifier, Value>());
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
	env.get(env.size() - 1).put(id, val);
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
