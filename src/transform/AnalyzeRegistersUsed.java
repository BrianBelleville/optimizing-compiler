package transform;

import java.util.HashSet;
import java.util.ListIterator;
import ir.Instruction;

public class AnalyzeRegistersUsed extends Pass {
    private int registersAvailable;
    private HashSet<Integer> registersUsed;
    private int memNeeded;

    public AnalyzeRegistersUsed(int registersAvailable) {
        this.registersAvailable = registersAvailable;
        memNeeded = 0;
    }
    
    @Override
    public void run(ListIterator<Instruction> iter){
        Integer color = iter.next().getColor();
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
