package support;

import java.util.ArrayList;
import java.util.HashMap;
import ir.Instruction;

public class Environment {
    private ArrayList< HashMap<Identifier, Instruction>> env;
    public void enter() {
	env.add(new HashMap<Identifier, Instruction>());
    }
    public void exit() {
	// pop the last element off of the list
	env.remove(env.size() - 1);
    }
    public Instruction get(Identifier id) {
	// traverse the list of environments backwards
	for(int i = env.size() - 1; i >= 0; i--) {
	    Instruction rval = env.get(i).get(id);
	    if(rval != null) {
		return rval;
	    }
	}
	// exited the search without finding the Identifier, return null
	return null;
    }
    public void put(Identifier id, Instruction val) {
	// assignments always will occur in the outermost environment
	env.get(env.size() - 1).put(id, val);
    }    
}
