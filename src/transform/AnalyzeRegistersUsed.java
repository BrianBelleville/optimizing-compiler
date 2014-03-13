package transform;

import java.util.HashSet;
import java.util.ListIterator;
import ir.Instruction;
import ir.Call;

public class AnalyzeRegistersUsed extends Pass {
    private int registersAvailable;
    public  HashSet<Integer> registersUsed;
    public int memNeeded;

    private int retAddrReg;

    // caller must normalize the return address register to be 0 based
    public AnalyzeRegistersUsed(int registersAvailable, int retColor) {
        this.registersAvailable = registersAvailable;
        registersUsed = new HashSet<Integer>();
        retAddrReg = retColor;
        memNeeded = 0;
    }
    
    @Override
    public void run(ListIterator<Instruction> iter){
        Instruction i = iter.next();
        // if this function does any function calls, the return
        // address will need to be saved
        if(i instanceof Call) {
            registersUsed.add(retAddrReg);
        }
        Integer color = i.getColor();
        if(color != null) {
            // color is a 0 based number, check if this value will be
            // spilled
            if(color >= registersAvailable) {
                memNeeded = Math.max(memNeeded, color - registersAvailable + 1);
            } else {
                registersUsed.add(color);
            }            
        }
    }
}
