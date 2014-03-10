package support;

import java.util.HashMap;

public class LocalMemoryRegion extends MemoryRegion {
    private HashMap<Identifier, addressRecord> usedMem;
    private int freeAddress;

    public LocalMemoryRegion() {
        // old FP is stored at FP[0], locals start at FP[1]
        freeAddress = Type.getWordSize();
        usedMem = new HashMap<Identifier, addressRecord>();
    }


    @Override
    public int getSize() {
        // FP[0] isn't part of this memory region
        return freeAddress - Type.getWordSize();
    }

    @Override
    public int getAddress(Identifier var, int size)
        throws Exception {
        addressRecord a = usedMem.get(var);
        if(a != null) {
            if(a.size != size) {
                throw new Exception("varaible accessed as different size than expected");
            }
            return a.address;
        }
        int address = freeAddress;
        freeAddress += size;
        usedMem.put(var, new addressRecord(address, size));
        return address;
    }

    // get the address of the nth (0 based) spill cell
    public int getCellAddress(int n) {
        return 0;
    }

    // allocate memory for the spilled local variables
    public void allocateMemoryCells(int cellCount) {
        
    }
}
