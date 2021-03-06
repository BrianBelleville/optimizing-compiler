package ir;

import java.util.ArrayList;
import java.io.Writer;

public class ArrayIndex extends NaryInstruction {
    private Value baseAddr;
    private int arrayAddr;
    private int[] multipliers;
    public ArrayIndex(Value baseAddr, int arrayAddr, int[] multipliers, ArrayList<Value> args) {
        super(Opcode.index, args);
        this.baseAddr = baseAddr;
        this.arrayAddr = arrayAddr;
        this.multipliers = multipliers;
    }

    public ArrayList<Instruction> getIndexingCalculation() throws Exception {
        ArrayList<Value> index = getArguments();
        if(index.size() != multipliers.length) {
            throw new Exception("Error, incorrect number of array indexes");
        }
        Value acc = null;
        int s = multipliers.length;
        int immediateAcc = 0;   // accumulate immediate offsets
        ArrayList<Instruction> rval = new ArrayList<Instruction>();
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
                    rval.add(mul);
                    currentOffset = mul;
                }
                if(acc != null) {
                    Add add = new Add(acc, currentOffset);
                    rval.add(add);
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
            finalOffset = new Immediate(arrayAddr);
        } else if(acc == null && immediateAcc != 0) {
            // offset entirely determined during compilation, and is not 0
            finalOffset = new Immediate(immediateAcc + arrayAddr);
        } else if(acc != null && immediateAcc != 0) {
            // the final offset will be determined at runtime
            // take into account what can be determined at compile time
            // factor in the array address to the immediate offset
            Instruction a = new Add(acc, new Immediate(immediateAcc + arrayAddr));
            rval.add(a);
            finalOffset = a;
        } else if (acc != null && immediateAcc == 0) {
            // final offset will be determined at runtime, there is no
            // contribution determined at compile time that needs to
            // be added
            Instruction f = new Add(new Immediate(arrayAddr), acc);
            rval.add(f);
            finalOffset = f;
        } else {                // shouldn't get here, all cases should be acounted for
            throw new Exception("Unable to determine array offset");
        }
        rval.add(new Adda(baseAddr, finalOffset));
        return rval;
    }

    @Override
    public void printInstruction(Writer w) throws Exception {
        throw new Exception("Attempt to print ArrayIndex, should have been eliminated");
    }

    @Override
    public boolean needsRegister() {
        return false;
    }
}
