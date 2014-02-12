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

    public ArrayType(ArrayList<Integer> dimension, boolean global) {
        int size = 1;
        multipliers = new int[dimension.size()];
        for(int i = dimension.size() - 1; i >= 0; i--) {
            multipliers[i] = size;
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
        Instruction acc = null;
        int s = multipliers.length;
        for(int i = 0; i < s; i++) {
            Mul mul = new Mul(index.get(i), new Immediate(multipliers[i]));
            cur.addInstruction(mul);
            if(acc != null) {
                acc = new Add(acc, mul);
                cur.addInstruction(acc);
            } else {
                acc = mul;
            }
        }

        Mul m = new Mul(acc, new NamedValue("WORD_SIZE"));
        cur.addInstruction(m);
        Add a = new Add(getAddr(d), m);
        cur.addInstruction(a);
        Adda rval = new Adda(global ? getGBP() : getFP(), a);
        cur.addInstruction(rval);
        return rval;
    }

    private Value getAddr(Designator d) {
        return new NamedValue("array_" + d.getVarName().getString() + "_addr");
    }
}
