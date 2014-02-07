package support;

import java.util.ArrayList;
import java.util.HashMap;
import ir.Value;

public class Environment {
    private ArrayList< HashMap<Identifier, Value>> env;
    public void enter() {
	env.add(new HashMap<Identifier, Value>());
    }
    public void exit() {
	// pop the last element off of the list
	env.remove(env.size() - 1);
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
}
