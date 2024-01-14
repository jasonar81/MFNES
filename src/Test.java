
public class Test {
	public static void main(String[] args)
	{
		Clock clock = new Clock();
		long i = 0;
		while (clock.cycle() < 5369317l * 60l)
		{
			++i;
		}
		
		System.out.println(i + " calls in 1 minute");
	}
}
