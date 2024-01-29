import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class DoubleDragonAi implements AiAgent {
	private Clock clock;
	private CPU cpu;
	private PPU ppu;
	private APU apu;
	private Memory ppuMem;
	private Memory cpuMem;
	private Thread cpuThread;
	private Thread ppuThread;
	private Thread apuThread;
	private GUI gui;
	private Thread guiThread;
	private volatile double highScore = 0;
	private volatile double finalScore;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long score;
	private volatile long livesLost;
	private volatile long totalTime;
	
	private static DoubleDragonAi instance;
	
	private long firstUsableCycle = 102186813;
	
	public static void main(String[] args)
	{
		instance = new DoubleDragonAi();
		instance.main();
	}
	
	private void main()
	{
		while (true)
		{
			setup();
			load("double_dragon.nes", "sav");
			makeModifications();
			ArrayList<ControllerEvent> eventList = getEventList("double_dragon.rec");
			eventList = reduceEvents(eventList);
			eventList = addRandomEvents(eventList);
			gui.setEventList(eventList);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + finalScore);
	
			highScore = finalScore;
			writeCurrentBestRecording(eventList, totalTime, "double_dragon.rec");
			System.out.println("New high score!");
			
			System.out.println();
			
			teardown();
			
			boolean improved = false;
			while (!improved)
			{
				setup();
				load("double_dragon.nes", "sav");
				makeModifications();
			
				eventList = getEventList("double_dragon.rec");
				eventList = modifyEventList(eventList);
				eventList = addRandomEvents(eventList);
				System.out.println("Trying random things");
			
				gui.setEventList(eventList);
				run();
			
				while (!done) {}
			
				printResults();
				System.out.println("Score of " + finalScore);
				if (finalScore > highScore)
				{
					highScore = finalScore;
					
					writeCurrentBestRecording(eventList, totalTime, "double_dragon.rec");
					System.out.println("New high score!");
					improved = true;
				}

				System.out.println();
				teardown();
			}
		}
	}
	
	private void writeCurrentBestRecording(ArrayList<ControllerEvent> events, long end, String filename)
	{
		try
		{
			FileWriter file = new FileWriter(filename);
			PrintWriter out = new PrintWriter(file);
			
			for (ControllerEvent event : events)
			{
				long cycle = event.getCycle();
				if (cycle < end)
				{
					int type = 0;
					if (event.getDown())
					{
						type = 1;
					}
					
					int code = event.getCode();
					out.println(cycle + "," + type + "," + code);
				}
				else
				{
					break;
				}
			}
			
			out.close();
			file.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void setup()
	{
		livesLost = 0;
		score = 0;
		totalTime = 0;
		done = false;
		startedDone = false;
		gui = new DefaultGUI();
		guiThread = new Thread(gui);
		guiThread.setPriority(10);
		guiThread.start();
		
		clock = new Clock();
		ppuMem = new Memory(Memory.PPU, null, gui);
		ppu = new PPU(clock, ppuMem, gui);
		cpuMem = new Memory(Memory.CPU, ppu, gui);
		cpu = new CPU(clock, cpuMem, ppu, gui);
		apu = new APU(cpu, gui, clock);
		cpu.setApu(apu);
		ppu.setCPU(cpu);
		cpuMem.setCpu(cpu);
		ppuMem.setCpu(cpu);
		gui.setCpu(cpu);
		gui.setClock(clock);
	}
	
	private void teardown()
	{
		apu.terminate();
		cpu.terminate();
		ppu.terminate();
		gui.terminate();
		
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e) {}
	}

	private void load(String filename, String saveFilename)
	{
		Cartridge cart = Cartridge.loadCart(filename);
		
		if (cart != null)
		{
			cpu.setupCart(cart, saveFilename);
			ppu.setupCart(cart);
		}
	}
	
	private void run()
	{
		on();
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private void printResults()
	{
		System.out.println("Game score = " + gameScore());
	}
	
	private void on()
	{
		ppuThread = new Thread(ppu);
		ppuThread.setPriority(10);
		cpuThread = new Thread(cpu);
		cpuThread.setPriority(10);
		apuThread = new Thread (apu);
		apuThread.setPriority(10);
		cpu.debugHold(true);
		ppu.debugHold(true);
		ppuThread.start();
		apuThread.start();
		cpuThread.start();
	}
	
	private void makeModifications()
	{
		gui.setAgent(this);
		Clock.periodNanos = 1.0;
		cpu.getMem().getLayout()[0x43] = new NotifyChangesPort(this, clock, (byte)4); //Lives remaining
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			this.totalTime = totalTime;
			++livesLost;
			score = gameScore();
			finalScore = (score * 1.0) / livesLost;
			done = true;
		}
	}
	
	private void pause()
	{
		cpu.debugHold(true);
		ppu.debugHold(true);
	}
	
	private void cont()
	{
		cpu.debugHold(false);
		ppu.debugHold(false);
	}
	
	private ArrayList<ControllerEvent> getEventList(String filename)
	{
		ArrayList<ControllerEvent> queue = new ArrayList<ControllerEvent>();
		try
		{
			File file = new File(filename);
			Scanner in = new Scanner(file);
			
			while (in.hasNextLine())
			{
				String line = in.nextLine();
				StringTokenizer tokens = new StringTokenizer(line, ",", false);
				long cycle = Long.parseLong(tokens.nextToken());
				int type = Integer.parseInt(tokens.nextToken());
				boolean down = (type == 1);
				int code = Integer.parseInt(tokens.nextToken());
				queue.add(new ControllerEvent(cycle, down, code));
			}
			
			in.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return queue;
	}
	
	private int indexOfFirstLargerElement(ArrayList<ControllerEvent> events, long cycle)
	{	
		int a = 0;
		int b = events.size() - 1;
		
		if (events.get(a).getCycle() > cycle)
		{
			return 0;
		}
		
		if (events.get(b).getCycle() <= cycle)
		{
			return - 1;
		}
		
		int c = 0;
		while (b - a > 2)
		{
			c = (a + b) / 2;
			if (events.get(c).getCycle() <= cycle)
			{
				a = c;
			}
			else
			{
				b = c;
			}
		}
		
		if (events.get(a+1).getCycle() > cycle)
		{
			return a+1;
		}
		
		return b;
	}
	
	private boolean deleteNextEventAfter(long cycle, ArrayList<ControllerEvent> events)
	{
		//System.out.println("Deleting an event");
		//System.out.println("Size before is " + events.size());
		int i = indexOfFirstLargerElement(events, cycle);
		
		if (i != -1)
		{
			if (i > 0 && events.get(i - 1).getCycle() == cycle)
			{
				events.remove(i - 1);
			}
			else
			{
				events.remove(i);
			}
			
			return true;
		}
		
		return false;
	}
	
	private ArrayList<ControllerEvent> addEvent(long cycle, boolean pressed, int keyCode, ArrayList<ControllerEvent> events)
	{
		//System.out.println("Adding an event");
		//System.out.println("Size before is " + events.size());
		int i = indexOfFirstLargerElement(events, cycle);
		
		if (i != -1)
		{
			events.add(i, new ControllerEvent(cycle, pressed, keyCode));
			return events;
		}
		
		events.add(new ControllerEvent(cycle, pressed, keyCode));
		//System.out.println("Size after is " + events.size());
		return events;
	}
	
	private ArrayList<ControllerEvent> modifyEventList(ArrayList<ControllerEvent> old)
	{
		long lastCycle = old.get(old.size() - 1).getCycle();
		long totalModfiableTime = lastCycle - firstUsableCycle;
		
		System.out.println("Modifying event from " + firstUsableCycle + " to " + lastCycle);
		
		int numMods = (int)(totalModfiableTime / 500000);
		for (int i = 0; i < numMods;)
		{
			int modType = Math.abs(ThreadLocalRandom.current().nextInt()) % 3;
			long offset = (long)(ThreadLocalRandom.current().nextDouble() * totalModfiableTime);
			long cycle = firstUsableCycle + offset;
			
			if (modType == 2)
			{
				if (deleteNextEventAfter(cycle, old))
				{
					++i;
				}
			}
			else if (modType == 0 || modType == 1)
			{
				++i;
				int randVal = Math.abs(ThreadLocalRandom.current().nextInt());
				int key = randVal % 7;

				if (key == 0)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_RIGHT, old);
						old = addEvent(cycle, false, KeyEvent.VK_UP, old);
						old = addEvent(cycle, false, KeyEvent.VK_DOWN, old);
					}
					
					key =  KeyEvent.VK_LEFT;
				} else if (key == 1)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_LEFT, old);
						old = addEvent(cycle, false, KeyEvent.VK_UP, old);
						old = addEvent(cycle, false, KeyEvent.VK_DOWN, old);
					}
					
					key =  KeyEvent.VK_RIGHT;
				} else if (key == 2)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_LEFT, old);
						old = addEvent(cycle, false, KeyEvent.VK_RIGHT, old);
						old = addEvent(cycle, false, KeyEvent.VK_DOWN, old);
					}
					
					key =  KeyEvent.VK_UP;
				} else if (key == 3)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_LEFT, old);
						old = addEvent(cycle, false, KeyEvent.VK_UP, old);
						old = addEvent(cycle, false, KeyEvent.VK_RIGHT, old);
					}
					
					key =  KeyEvent.VK_DOWN;
				} 
				else if (key == 4)
				{	
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_A, old);
					}
					
					key =  KeyEvent.VK_S;
				} else if (key == 5)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_S, old);
					}
					
					key =  KeyEvent.VK_A;
				} else
				{
					if (modType == 1)
					{
						old = addEvent(cycle, true, KeyEvent.VK_S, old);
						old = addEvent(cycle + 400000, false, KeyEvent.VK_S, old);
					}
					else
					{
						old = addEvent(cycle, false, KeyEvent.VK_S, old);
					}
					
					key =  KeyEvent.VK_A;
				}
				
				old = addEvent(cycle, modType == 1, key, old);
				
				if (modType == 1)
				{
					if (key == KeyEvent.VK_S || key == KeyEvent.VK_A)
					{
						old = addEvent(cycle + 400000, false, key, old);
					} 
				}
			}
		}
		
		return old;
	}
	
	public synchronized void progress(long cycle)
	{
		pause();
		
		if (cycle >= firstUsableCycle)
		{
			++livesLost;
			System.out.println("Died");
		}
		
		cont();
	}
	
	private long gameScore()
	{
		long retval = 0;
		int val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x44].read());
		retval += val;
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x45].read());
		retval += val * 100;
		val = Byte.toUnsignedInt(cpu.getMem().getLayout()[0x46].read());
		retval += val * 10000;
		
		return retval;
	}
	
	private ArrayList<ControllerEvent> addRandomEvents(ArrayList<ControllerEvent> events)
	{
		long first = events.get(events.size() - 1).getCycle() + 1;
		if (first < firstUsableCycle)
		{
			first = firstUsableCycle;
		}
		
		long totalModfiableTime = first;
		
		int numMods = (int)(totalModfiableTime / 500000);
		for (int i = 0; i < numMods; ++i)
		{
			int modType = Math.abs(ThreadLocalRandom.current().nextInt()) % 2;
			long offset = (long)(ThreadLocalRandom.current().nextDouble() * totalModfiableTime);
			long cycle = first + offset;
		
			int randVal = Math.abs(ThreadLocalRandom.current().nextInt());
			int key = randVal % 7;

			if (key == 0)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_RIGHT, events);
					events = addEvent(cycle, false, KeyEvent.VK_UP, events);
					events = addEvent(cycle, false, KeyEvent.VK_DOWN, events);
				}
				
				key =  KeyEvent.VK_LEFT;
			} else if (key == 1)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_LEFT, events);
					events = addEvent(cycle, false, KeyEvent.VK_UP, events);
					events = addEvent(cycle, false, KeyEvent.VK_DOWN, events);
				}
				
				key =  KeyEvent.VK_RIGHT;
			} else if (key == 2)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_LEFT, events);
					events = addEvent(cycle, false, KeyEvent.VK_RIGHT, events);
					events = addEvent(cycle, false, KeyEvent.VK_DOWN, events);
				}
				
				key =  KeyEvent.VK_UP;
			} else if (key == 3)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_LEFT, events);
					events = addEvent(cycle, false, KeyEvent.VK_UP, events);
					events = addEvent(cycle, false, KeyEvent.VK_RIGHT, events);
				}
				
				key =  KeyEvent.VK_DOWN;
			} 
			else if (key == 4)
			{	
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_A, events);
				}
				
				key =  KeyEvent.VK_S;
			} else if (key == 5)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_S, events);
				}
				
				key =  KeyEvent.VK_A;
			} else
			{
				if (modType == 1)
				{
					events = addEvent(cycle, true, KeyEvent.VK_S, events);
					events = addEvent(cycle + 400000, false, KeyEvent.VK_S, events);
				}
				else
				{
					events = addEvent(cycle, false, KeyEvent.VK_S, events);
				}
				
				key =  KeyEvent.VK_A;
			}
			
			events = addEvent(cycle, modType == 1, key, events);
			
			if (modType == 1)
			{
				if (key == KeyEvent.VK_S || key == KeyEvent.VK_A)
				{
					events = addEvent(cycle + 400000, false, key, events);
				} 
			}
		}
		
		return events;
	}

	@Override
	public void setDeath(long cycle) {
		//Easier just to handle in progress()
	}
	
	private ArrayList<ControllerEvent> reduceEvents(ArrayList<ControllerEvent> events)
	{
		int state = 0;
		ArrayList<ControllerEvent> retval = new ArrayList<ControllerEvent>();
		
		for (ControllerEvent event : events)
		{
			int newState = updateState(state, event);
			if (newState != state)
			{
				state = newState;
				retval.add(event);
			}
		}
		
		return retval;
	}
	
	private int updateState(int state, ControllerEvent event)
	{
		//A, B, up, down, left, right, start, select
		if (event.getCode() == KeyEvent.VK_S)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 7);
			}
			else
			{
				state = Utils.clearBit(state, 7);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_A)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 6);
			}
			else
			{
				state = Utils.clearBit(state, 6);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_UP)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 5);
			}
			else
			{
				state = Utils.clearBit(state, 5);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_DOWN)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 4);
			}
			else
			{
				state = Utils.clearBit(state, 4);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_LEFT)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 3);
			}
			else
			{
				state = Utils.clearBit(state, 3);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_RIGHT)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 2);
			}
			else
			{
				state = Utils.clearBit(state, 2);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_ENTER)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 1);
			}
			else
			{
				state = Utils.clearBit(state, 1);
			}
		}
		
		if (event.getCode() == KeyEvent.VK_SPACE)
		{
			if (event.getDown())
			{
				state = Utils.setBit(state, 0);
			}
			else
			{
				state = Utils.clearBit(state, 0);
			}
		}
		
		return state;
	}
}
