//Interface for a memory address

public interface MemoryPort {
	public byte read();
	public void write(byte val);
}
