package support;

import ir.BasicBlock;
import ir.Adda;
import ir.Load;
import ir.Store;
import ir.Add;
import ir.Mul;
import ir.NamedValue;
import ir.Immediate;
import ir.base.Value;
import ir.base.Instruction;
import java.util.ArrayList;

public class ArrayType extends Type {
    // this stores the multipliers used to index into the array
    private int[] multipliers;
    private boolean global;
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
        if(!(d instanceof ArrayDesignator)) {
            throw new Exception("Array accessed as a variable");
        }
        Adda a = emitIndexingCode((ArrayDesignator)d, cur);
        Store s = new Store(d.getVarName(), a, newVal);
        cur.addInstruction(s);
    }

    @Override
    public Value getValue(Designator d, BasicBlock cur, Environment env)
        throws Exception {
        if(!(d instanceof ArrayDesignator)) {
            throw new Exception("Array accessed as a variable");
        }
        Adda a = emitIndexingCode((ArrayDesignator)d, cur);
        Load l = new Load(d.getVarName(), a);
        cur.addInstruction(l);
        return l;
    }

    // todo: this generates bad code, may be able to statically
    //       determine the offset in some cases
    private Adda emitIndexingCode(ArrayDesignator d, BasicBlock cur)
        throws Exception {
        ArrayList<Value> index = d.getIndex();
        if(index.size() != multipliers.length) {
            throw new Exception("Error, incorrect number of array indexes");
        }
        Value acc = null;
        int s = multipliers.length;
        int immediateAcc = 0;   // accumulate immediate offsets
        for(int i = 0; i < s; i++) {
            Value v = index.get(i);
            if(v instanceof Immediate) {
                int val = ((Immediate)v).getValue();
                immediateAcc = immediateAcc + (val * multipliers[i]);
            } else {
                Value currentOffset;
                // if multiplier is 1, don't need a mul inst
                if(multipliers[i] == 1) {
                    currentOffset = v;
                } else {
                    Mul mul = new Mul(v, new Immediate(multipliers[i]));
                    cur.addInstruction(mul);
                    currentOffset = mul;
                }
                if(acc != null) {
                    Add add = new Add(acc, currentOffset);
                    cur.addInstruction(add);
                    acc = add;
                } else {
                    acc = currentOffset;
                }
            }
        }

        // now depending on how much of the offset is already
        // determined, generate the final code to index into the array
        Value finalOffset = null;
        if(acc == null && immediateAcc == 0) {
            // offset entirely determined during compilation to be 0
            // load base address
            finalOffset = getAddr(d);
        } else if(acc == null && immediateAcc != 0) {
            // offset entirely determined during compilation, and is not 0
            Instruction t = new Add(getAddr(d), new Immediate(immediateAcc));
            cur.addInstruction(t);
            finalOffset = t;
        } else if(acc != null && immediateAcc != 0) {
            // the final offset will be determined at runtime
            // take into account what can be determined at compile time
            Instruction a = new Add(acc, new Immediate(immediateAcc));
            cur.addInstruction(a);
            Instruction f = new Add(getAddr(d), a);
            cur.addInstruction(f);
            finalOffset = f;
        } else if (acc != null && immediateAcc == 0) {
            // final offset will be determined at runtime, there is no
            // contribution determined at compile time that needs to
            // be added
            Instruction f = new Add(getAddr(d), acc);
            cur.addInstruction(f);
            finalOffset = f;
        } else {                // shouldn't get here, all cases should be acounted for
            throw new Exception("Unable to determine array offset");
        }
        Adda rval = new Adda(global ? getGBP() : getFP(), finalOffset);
        cur.addInstruction(rval);
        return rval;
    }

    private Value getAddr(Designator d) {
        return new NamedValue("array_" + d.getVarName().getString() + "_addr");
    }
}
