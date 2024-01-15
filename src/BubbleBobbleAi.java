import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

public class BubbleBobbleAi implements AiAgent {
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
	private volatile long highScore = 0;
	private volatile boolean done = false;
	private volatile boolean startedDone;
	private volatile long totalTime;
	private volatile long score;
	private volatile int currentLevel = 1;
	private volatile long previousScore = 0;
	
	private static BubbleBobbleAi instance;
	
	private long firstUsableCycle = 52853029;
	private volatile ArrayList<Long> screenScores;
	private volatile ArrayList<Long> screenStartTimes;
	private volatile ArrayList<Long> screenEndTimes;
	private volatile ArrayList<Integer> screenStartButtonStates;
	private ArrayList<Long> bestScreenScores = new ArrayList<Long>();
	private ArrayList<ArrayList<ControllerEvent>> bestScreenControls = new ArrayList<ArrayList<ControllerEvent>>();
	private ArrayList<Long> bestScreenStartTimes = new ArrayList<Long>();
	private ArrayList<Integer> bestScreenStartButtonStates = new ArrayList<Integer>();
	private ArrayList<Long> bestScreenEndTimes = new ArrayList<Long>();
	
	public static void main(String[] args)
	{
		instance = new BubbleBobbleAi();
		instance.main();
	}
	
	private void main()
	{
		while (true)
		{
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			ArrayList<ControllerEvent> eventList = getEventList("bubble_bobble.rec");
			eventList = reduceEvents(eventList);
			eventList = addRandomEvents(eventList);
			gui.setEventList(eventList);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
	
			highScore = score;
			writeCurrentBestRecording(eventList, totalTime, "bubble_bobble.rec");
			System.out.println("New high score!");
			
			processScreenResults(eventList);
			System.out.println();
			
			teardown();
			
			boolean improved = false;
			while (!improved)
			{
				setup();
				load("bubble_bobble.nes", "sav");
				makeModifications();
			
				eventList = getEventList("bubble_bobble.rec");
				eventList = modifyEventList(eventList);
				eventList = addRandomEvents(eventList);
				System.out.println("Trying random things");
			
				gui.setEventList(eventList);
				run();
			
				while (!done) {}
			
				printResults();
				System.out.println("Score of " + score);
				if (score > highScore)
				{
					highScore = score;
					
					writeCurrentBestRecording(eventList, totalTime, "bubble_bobble.rec");
					System.out.println("New high score!");
				}
			
				improved = processScreenResults(eventList);

				System.out.println();
				teardown();
			}
			
			setup();
			load("bubble_bobble.nes", "sav");
			makeModifications();
			
			eventList = combineBest(eventList);
			eventList = addRandomEvents(eventList);
			System.out.println("Combining the best screens");
			
			gui.setEventList(eventList);
			run();
			
			while (!done) {}
			
			printResults();
			System.out.println("Score of " + score);
			
			if (score > highScore)
			{
				highScore = score;
				writeCurrentBestRecording(eventList, totalTime, "bubble_bobble.rec");
				System.out.println("New high score!");
			}

			processScreenResults(eventList);
			System.out.println();
			teardown();
			
			bestScreenScores.clear();
			bestScreenControls.clear();
			bestScreenStartTimes.clear();
			bestScreenStartButtonStates.clear();
			bestScreenEndTimes.clear();
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
		previousScore = 0;
		currentLevel = 1;
		screenScores = new ArrayList<Long>();
		screenStartTimes = new ArrayList<Long>();
		screenEndTimes = new ArrayList<Long>();
		screenStartButtonStates = new ArrayList<Integer>();
		screenStartTimes.add(firstUsableCycle);
		screenStartTimes = screenStartTimes;
		screenStartButtonStates.add(0);
		screenStartButtonStates = screenStartButtonStates;
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
		System.out.println("Level = " + screenScores.size());
		System.out.println("Game score = " + gameScore());
	}
	
	private int getLevel()
	{
		return ((SaveAndUpdateMaxValuePort)cpu.getMem().getLayout()[0x401]).getMaxValue();
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
		cpu.getMem().getLayout()[0x2e] = new NotifyChangesPort(this, clock); //Lives remaining
		cpu.getMem().getLayout()[0x401] = new SaveAndUpdateMaxValuePort(this, clock); //Level (0 doesn't count)
	}
	
	public void setDone(long totalTime)
	{
		if (!startedDone && !done)
		{
			pause();
			System.out.println("Done");
			startedDone = true;
			this.totalTime = totalTime;
			long screenScore = partialScore();
			screenScores.add(screenScore);
			screenEndTimes.add(totalTime);
			score += screenScore;
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
				int key = randVal % 4;

				if (key == 0)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_RIGHT, old);
					}
					
					key =  KeyEvent.VK_LEFT;
				} else if (key == 1)
				{
					if (modType == 1)
					{
						old = addEvent(cycle, false, KeyEvent.VK_LEFT, old);
					}
					
					key =  KeyEvent.VK_RIGHT;
				} else if (key == 2)
				{	
					key =  KeyEvent.VK_S;
				} else if (key == 3)
				{
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
	
	private int getButtonStates()
	{
		int retval = 0;
		if (gui.getA())
		{
			retval |= 0x80;
		}
		
		if (gui.getB())
		{
			retval |= 0x40;
		}
		
		if (gui.getLeft())
		{
			retval |= 0x08;
		}
		
		if (gui.getRight())
		{
			retval |= 0x04;
		}
		
		return retval;
	}
	
	public synchronized void progress(long cycle)
	{
		pause();
		
		//We got pinged because of a level change or loss of life
		if (cycle >= firstUsableCycle)
		{
			int level = getLevel();
			if (level > currentLevel)
			{
				//new level 
				System.out.println("Finished level");
				currentLevel = level;
				System.out.println("Level is now " + level);
				long screenScore = finishedScreenScore(cycle);
				screenScores.add(screenScore);
				screenEndTimes.add(cycle);
				screenStartTimes.add(cycle);
				screenStartButtonStates.add(getButtonStates());
				score += screenScore;
			}
			else if (cpu.getMem().getLayout()[0x2e].read() == 0)
			{
				setDone(cycle);
			}
		}
		
		cont();
	}
	
	private long finishedScreenScore(long cycle)
	{
		long seconds = (long)((cycle - screenStartTimes.get(screenStartTimes.size() - 1)) / 5369317.5);
		System.out.println("Level took " + seconds + "s");
		if (seconds > 255)
		{
			seconds = 255;
		}
		
		seconds = 255 - seconds;
		seconds <<= 24;
		long lives = cpu.getMem().read(0x2e);
		System.out.println("Remaining lives = " + lives);
		lives <<= 32;
		
		long gameScore = gameScore();
		long delta = gameScore - previousScore;
		if (delta < 0)
		{
			delta = 0;
		}
		
		System.out.println("Score in this level = " + delta);
		previousScore = gameScore;
		return lives + seconds + delta;
	}
	
	private long partialScore()
	{
		System.out.println("Processing partial screen");
		long seconds = 0;
		long lives = 0;
		
		long gameScore = gameScore();
		long delta = gameScore - previousScore;
		if (delta < 0)
		{
			delta = 0;
		}
		
		System.out.println("Score in this level = " + delta);
		return lives + seconds + delta;
	}
	
	private long gameScore()
	{
		long retval = 0;
		int val = cpu.getMem().getLayout()[0x44a].read();
		if (val != 0x27)
		{
			retval += val;
		}
		
		val = cpu.getMem().getLayout()[0x449].read();
		if (val != 0x27)
		{
			retval += (val * 10);
		}
		
		val = cpu.getMem().getLayout()[0x448].read();
		if (val != 0x27)
		{
			retval += (val * 100);
		}
		
		val = cpu.getMem().getLayout()[0x447].read();
		if (val != 0x27)
		{
			retval += (val * 1000);
		}
		
		val = cpu.getMem().getLayout()[0x446].read();
		if (val != 0x27)
		{
			retval += (val * 10000);
		}
		
		val = cpu.getMem().getLayout()[0x445].read();
		if (val != 0x27)
		{
			retval += (val * 100000);
		}
		
		return retval;
	}
	
	private boolean processScreenResults(ArrayList<ControllerEvent> events)
	{
		boolean retval = false;
		for (int i = 0; i < screenScores.size(); ++i)
		{
			if (i < bestScreenScores.size())
			{
				if (screenScores.get(i) > bestScreenScores.get(i))
				{
					retval = true;
					System.out.println("Screen " + i + " had a new best score of " + screenScores.get(i) + " old best was " + bestScreenScores.get(i));
					bestScreenScores.set(i, screenScores.get(i));
					long start = screenStartTimes.get(i);
					long end = screenEndTimes.get(i);
			
					bestScreenControls.set(i, getControls(start, end, events));
					bestScreenStartTimes.set(i, start);
					bestScreenEndTimes.set(i, end);
					bestScreenStartButtonStates.set(i, screenStartButtonStates.get(i));
				}
			}
			else
			{
				retval = true;
				System.out.println("Screen " + i + " was never played before. Got a score of " + screenScores.get(i));
				bestScreenScores.add(screenScores.get(i));
				long start = screenStartTimes.get(i);
				long end = screenEndTimes.get(i);
				
				bestScreenControls.add(getControls(start, end, events));
				bestScreenStartTimes.add(start);
				bestScreenEndTimes.add(end);
				bestScreenStartButtonStates.add(screenStartButtonStates.get(i));
			}
		}
		
		return retval;
	}
	
	private ArrayList<ControllerEvent> getControls(long start, long end, ArrayList<ControllerEvent> events)
	{
		ArrayList<ControllerEvent> retval = new ArrayList<ControllerEvent>();
		for (ControllerEvent event : events)
		{
			if (event.getCycle() < start)
			{
				continue;
			} else if (event.getCycle() <= end)
			{
				retval.add(event);
			} else 
			{
				break;
			}
		}
		
		return retval;
	}
	
	private ArrayList<ControllerEvent> combineBest(ArrayList<ControllerEvent> eventList)
	{
		ArrayList<ControllerEvent> retval = new ArrayList<ControllerEvent>();
		for (ControllerEvent event : eventList)
		{
			if (event.getCycle() < firstUsableCycle)
			{
				retval.add(event);
			}
			else
			{
				break;
			}
		}
		
		long currentTime = firstUsableCycle;
		long push = 0;
		for (int i = 0; i < bestScreenScores.size(); ++i)
		{
			push = currentTime - bestScreenStartTimes.get(i);
			
			retval = forceButtonStates(bestScreenStartButtonStates.get(i), bestScreenStartTimes.get(i) + push, retval);
			for (ControllerEvent event : bestScreenControls.get(i))
			{
				retval.add(new ControllerEvent(event.getCycle() + push, event.getDown(), event.getCode()));
			}
			
			currentTime = bestScreenEndTimes.get(i) + push;
			retval = allButtonsOff(currentTime, retval);
		}
		
		return retval;
	}
	
	private ArrayList<ControllerEvent> forceButtonStates(int state, long cycle, ArrayList<ControllerEvent> events)
	{
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 7), KeyEvent.VK_S));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 6), KeyEvent.VK_A));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 5), KeyEvent.VK_UP));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 4), KeyEvent.VK_DOWN));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 3), KeyEvent.VK_LEFT));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 2), KeyEvent.VK_RIGHT));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 1), KeyEvent.VK_ENTER));
		events.add(new ControllerEvent(cycle, Utils.getBit(state, 0), KeyEvent.VK_SPACE));
		return events;
	}
	
	private ArrayList<ControllerEvent> allButtonsOff(long cycle, ArrayList<ControllerEvent> events)
	{
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_S));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_A));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_UP));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_DOWN));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_LEFT));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_RIGHT));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_ENTER));
		events.add(new ControllerEvent(cycle, false, KeyEvent.VK_SPACE));
		return events;
	}
	
	private ArrayList<ControllerEvent> addRandomEvents(ArrayList<ControllerEvent> events)
	{
		long first = events.get(events.size() - 1).getCycle() + 1;
		long totalModfiableTime = first;
		
		int numMods = (int)(totalModfiableTime / 500000);
		for (int i = 0; i < numMods; ++i)
		{
			int modType = Math.abs(ThreadLocalRandom.current().nextInt()) % 2;
			long offset = (long)(ThreadLocalRandom.current().nextDouble() * totalModfiableTime);
			long cycle = first + offset;
		
			int randVal = Math.abs(ThreadLocalRandom.current().nextInt());
			int key = randVal % 4;

			if (key == 0)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_RIGHT, events);
				}
				
				key =  KeyEvent.VK_LEFT;
			} else if (key == 1)
			{
				if (modType == 1)
				{
					events = addEvent(cycle, false, KeyEvent.VK_LEFT, events);
				}
				
				key =  KeyEvent.VK_RIGHT;
			} else if (key == 2)
			{	
				key =  KeyEvent.VK_S;
			} else if (key == 3)
			{	
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
			
			if (invalidState(state))
			{
				retval.addAll(fixState(state, event.getCycle()));
				state = fixedState(state);
			}
		}
		
		return retval;
	}
	
	private boolean invalidState(int state)
	{
		return Utils.getBit(state, 3) && Utils.getBit(state, 2);
	}
	
	private ArrayList<ControllerEvent> fixState(int state, long cycle)
	{
		ArrayList<ControllerEvent> retval = new ArrayList<ControllerEvent>();
		if (Utils.getBit(state, 3))
		{
			if (Utils.getBit(state, 2))
			{
				retval.add(new ControllerEvent(cycle, false, KeyEvent.VK_RIGHT));
			}
		}
		
		return retval;
	}
	
	private int fixedState(int state)
	{
		if (Utils.getBit(state, 3) && Utils.getBit(state, 2))
		{
			state = Utils.clearBit(state, 2);
		}
		
		return state;
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
