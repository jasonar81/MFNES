//Interface for a memory address
import java.io.Serializable;

public interface MemoryPort extends Serializable {
public static final long serialVersionUID = -6732487624928621347L;

	public byte read();
	public void write(byte val);
}
