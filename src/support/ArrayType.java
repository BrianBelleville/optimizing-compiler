package support;

import ir.BasicBlock;
import ir.Load;
import ir.Store;
import ir.ArrayIndex;
import ir.NamedValue;
import ir.Value;

import java.util.ArrayList;

public class ArrayType extends Type {
    // this stores the multipliers used to index into the array
    private int[] multipliers;
    private boolean global;
    private boolean used = false;
    private int word_size = 4;  // assume 32 bit machine, may have to change this, or make it configurable

    public ArrayType(ArrayList<Integer> dimension, boolean global) {
        int size = 1;
        multipliers = new int[dimension.size()];
        for(int i = dimension.size() - 1; i >= 0; i--) {
            // factor the word size into the multipliers to avoid
            // having to perform another multiplication at runtime.
            multipliers[i] = size * word_size;
            size = dimension.get(i) * size;
        }
        this.global = global;
    }

    @Override
    public void assignValue(Designator d, BasicBlock cur, BasicBlock join, Environment env, Value newVal)
        throws Exception {
        if(global) {
            used = true;
        }
        if(!(d instanceof ArrayDesignator)) {
            throw new Exception("Array accessed as a variable");
        }
        Value a = emitIndexingCode((ArrayDesignator)d, cur);
        Store s = new Store(d.getVarName(), a, newVal);
        cur.addInstruction(s);
    }

    @Override
    public Value getValue(Designator d, BasicBlock cur, Environment env)
        throws Exception {
        if(global) {
            used = true;
        }
        if(!(d instanceof ArrayDesignator)) {
            throw new Exception("Array accessed as a variable");
        }
        Value a = emitIndexingCode((ArrayDesignator)d, cur);
        Load l = new Load(d.getVarName(), global, a);
        cur.addInstruction(l);
        return l;
    }

    // If globally delcared array isn't used anywhere excepct the main
    // routine, we can optimize it like a stack allocated array, right
    // now this will also cause it to be allocated on the stack space
    // of main, which requires main to have an activation record, so
    // this may need to be tweaked when code is emited.
    @Override
    public void freeze() {
        if(!used && global) {
            global = false;
        }
    }

    private Value emitIndexingCode(ArrayDesignator d, BasicBlock cur)
        throws Exception {
        ArrayList<Value> index = d.getIndex();
        if(index.size() != multipliers.length) {
            throw new Exception("Error, incorrect number of array indexes");
        }
        ArrayIndex rval =  new ArrayIndex(global ? getGBP() : getFP(), getAddr(d), multipliers, index);
        cur.addInstruction(rval);
        return rval;
    }

    private Value getAddr(Designator d) {
        return new NamedValue("array_" + d.getVarName().getString() + "_addr");
    }
}
