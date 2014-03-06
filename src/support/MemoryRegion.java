package support;

public abstract class MemoryRegion {

    public abstract int getSize();
    
    protected class addressRecord {
        public int address;
        public int size;
        public addressRecord(int address, int size) {
            this.address = address;
            this.size = size;
        }
    }
        
    public abstract int getAddress(Identifier var, int size)
        throws Exception;
}
