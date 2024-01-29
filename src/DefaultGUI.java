//The default controller, display, and audio provider

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DefaultGUI implements GUI, ComponentListener, KeyListener {
	protected JFrame frame;
	protected volatile BufferedImage img;
	protected volatile int[] imgData = new int[280 * 240];
	protected JPanel panel;
	protected BufferStrategy strategy;
	protected AtomicInteger width;
	protected AtomicInteger height;
	protected AtomicInteger x;
	protected AtomicInteger y;
	private Color black;
	protected AtomicBoolean swap;
	
	private volatile boolean left = false;
	private volatile boolean right = false;
	private volatile boolean up = false;
	private volatile boolean down = false;
	private volatile boolean select = false;
	private volatile boolean start = false;
	private volatile boolean a = false;
	private volatile boolean b = false;
	private volatile long totalBPresses = 0;
	
	protected SourceDataLine audio;
	private byte[] audioBuffer = new byte[8192];
	private boolean buffering = true;
	private int bufferingIndex = 0;
	private long framesSent = 0;
	
	private CPU cpu;
	private PrintWriter recordLog;
	private volatile boolean record;
	protected volatile boolean playback;
	private ArrayList<ControllerEvent> queue = new ArrayList<ControllerEvent>();
	private int queueIndex = 0;
	private long cycleOffset;
	protected Clock clock;
	
	protected volatile boolean terminate = false;
	protected AiAgent agent = null;
	
	public DefaultGUI()
	{
		swap = new AtomicBoolean(false);
		black = new Color(0, 0, 0);
		width = new AtomicInteger(960);
		height = new AtomicInteger(720);
		x = new AtomicInteger(0);
		y = new AtomicInteger(0);
		
		try
		{
			frame = new JFrame("My fucking NES emulator");
			frame.setLayout(null);
			
			frame.setIgnoreRepaint(true);
			
			if (frame.getContentPane() instanceof JComponent)
			{
				((JComponent)frame.getContentPane()).setOpaque(false);
			}
			else
			{
				frame.getContentPane().setBackground(new Color(0, 0, 0, 0));
			}
			
			frame.setSize(width.get(), height.get());
			Insets insets = frame.getInsets();
			int insetsWide = insets.left + insets.right;
			int insetsTall = insets.top + insets.bottom;
			
			frame.setSize(frame.getWidth() + insetsWide, frame.getHeight() + insetsTall);
			insets = frame.getInsets();
			x.set(insets.left);
			y.set(insets.top);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			
			frame.addComponentListener(this);
			frame.addKeyListener(this);
			panel = new JPanel();
			frame.add(panel);
			frame.createBufferStrategy(2);
			strategy = frame.getBufferStrategy();
			
			img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(280, 240);
		}
		catch(Exception e)
		{
			frame = null;
			panel = null;
			img = null;
			strategy = null;
		}
		
		audio = null;
		
		try
		{
			AudioFormat format = new AudioFormat(44744.3125f, 16, 1, true, false);
			audio = AudioSystem.getSourceDataLine(format);
			audio.open(format);
			audio.start();
		}
		catch(Exception e) {}
		
		swapBuffers();
	}

	@Override
	public void write(int x, int y, int rgb) {
		imgData[x + 12 + 280 * y] = rgb;
		imgData = imgData;
	}

	@Override
	public void swapBuffers() {
		while (swap.get()) 
		{
		}
		
		swap.set(true);
	}

	@Override
	public void run() {
		while (true)
		{
			if (terminate)
			{
				return;
			}
			
			Graphics g = null;
			
			if (strategy != null)
			{
				g = strategy.getDrawGraphics();
			}
			
			while (!swap.get()) {
				if (playback)
				{
					runPlayback(clock.getPpuExpectedCycle());
				}
				
				if (terminate)
				{
					return;
				}
			}
			
			if (g != null)
			{
				img.getRaster().setDataElements(0, 0, 280, 240, imgData);
				img = img;
				while (!g.drawImage(img, x.get(), y.get(), width.get(), height.get(), null)) 
				{
					if (playback)
					{
						runPlayback(clock.getPpuExpectedCycle());
					}
				}
			}
			
			swap.set(false);
			
			if (g != null)
			{
				g.dispose();
				if (!strategy.contentsLost())
				{
					strategy.show();
				}
			}
			
			if (playback)
			{
				runPlayback(clock.getPpuExpectedCycle());
			}
		}
	}
	
	protected void runPlayback(long cycle)
	{
		while (queueIndex < queue.size())
		{
			ControllerEvent head = queue.get(queueIndex);
			if (head.getCycle() + cycleOffset <= cycle)
			{
				processEvent(head);
				++queueIndex;
			}
			else
			{
				return;
			}
		}
		
		if (agent != null)
		{
			agent.setDone(clock.getPpuExpectedCycle());
		}
	}
	
	private void processEvent(ControllerEvent event)
	{
		boolean pressed = event.getDown();
		int code = event.getCode();
		if (code == KeyEvent.VK_UP)
		{
			up = pressed;
		} else if (code == KeyEvent.VK_DOWN)
		{
			down = pressed;
		} else if (code == KeyEvent.VK_LEFT)
		{
			left = pressed;
		} else if (code == KeyEvent.VK_RIGHT)
		{
			right = pressed;
		} else if (code == KeyEvent.VK_A)
		{
			if (b && !pressed)
			{
				++totalBPresses;
			}
			
			b = pressed;
		} else if (code == KeyEvent.VK_S)
		{
			 a = pressed;
		} else if (code == KeyEvent.VK_SPACE)
		{
			select = pressed;
		} else if (code == KeyEvent.VK_ENTER)
		{
			start = pressed;
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		Insets insets = frame.getInsets();
		int insetsWide = insets.left + insets.right;
		int insetsTall = insets.top + insets.bottom;
		width.set(frame.getWidth() - insetsWide);
		height.set(frame.getHeight() - insetsTall);
		x.set(insets.left);
		y.set(insets.top);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//STUPID FUCKING EVENT
	}

	@Override
	public void keyPressed(KeyEvent e) {
		long cycle = clock.getPpuExpectedCycle();
		if (!playback)
		{
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_UP)
			{
				up = true;
			} else if (code == KeyEvent.VK_DOWN)
			{
				down = true;
			} else if (code == KeyEvent.VK_LEFT)
			{
				left = true;
			} else if (code == KeyEvent.VK_RIGHT)
			{
				right = true;
			} else if (code == KeyEvent.VK_A)
			{
				b = true;
			} else if (code == KeyEvent.VK_S)
			{
				 a= true;
			} else if (code == KeyEvent.VK_SPACE)
			{
				select = true;
			} else if (code == KeyEvent.VK_ENTER)
			{
				start = true;
			}
			
			if (record)
			{
				recordLog.println(cycle + ",1," + code);
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (!playback)
		{
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_UP)
			{
				up = false;
			} else if (code == KeyEvent.VK_DOWN)
			{
				down = false;
			} else if (code == KeyEvent.VK_LEFT)
			{
				left = false;
			} else if (code == KeyEvent.VK_RIGHT)
			{
				right = false;
			} else if (code == KeyEvent.VK_A)
			{
				b = false;
			} else if (code == KeyEvent.VK_S)
			{
				 a = false;
			} else if (code == KeyEvent.VK_SPACE)
			{
				select = false;
			} else if (code == KeyEvent.VK_ENTER)
			{
				start = false;
			}
			
			if (record)
			{
				recordLog.println(clock.getPpuExpectedCycle() + ",0," + code);
			}
		}
	}

	@Override
	public boolean getA() {
		return a;
	}

	@Override
	public boolean getB() {
		return b;
	}

	@Override
	public boolean getSelect() {
		return select;
	}

	@Override
	public boolean getStart() {
		return start;
	}

	@Override
	public boolean getUp() {
		return up;
	}

	@Override
	public boolean getDown() {
		return down;
	}

	@Override
	public boolean getLeft() {
		return left;
	}

	@Override
	public boolean getRight() {
		return right;
	}

	@Override
	public void writeAudioData(double data) {
		if (audio != null)
		{
			int val = (int)(data * 65535);
			if (val > 65535)
			{
				val = 65535;
			}
			else if (val < 0)
			{
				val = 0;
			}
			
			val -= 32768;
			
			if (framesSent - audio.getLongFramePosition() < audioBuffer.length / 4)
			{
				buffering = true;
			}
		
			if (buffering)
			{
				audioBuffer[bufferingIndex + 1] = (byte)((val & 0xff00) >> 8);
				audioBuffer[bufferingIndex] = (byte)(val & 0xff);
				bufferingIndex += 2;
				
				if (bufferingIndex == audioBuffer.length)
				{
					buffering = false;
					bufferingIndex = 0;
					audio.write(audioBuffer, 0, audioBuffer.length);
					framesSent += (audioBuffer.length >> 1);
				}
			}
			else
			{
				audioBuffer[bufferingIndex + 1] = (byte)((val & 0xff00) >> 8);
				audioBuffer[bufferingIndex] = (byte)(val & 0xff);
				bufferingIndex += 2;
				
				if (bufferingIndex == 1024)
				{
					bufferingIndex = 0;
					audio.write(audioBuffer, 0, 1024);
					framesSent += 512;
				}
			}
		}
	}

	@Override
	public boolean getA2() {
		return false;
	}

	@Override
	public boolean getB2() {
		return false;
	}

	@Override
	public boolean getSelect2() {
		return false;
	}

	@Override
	public boolean getStart2() {
		return false;
	}

	@Override
	public boolean getUp2() {
		return false;
	}

	@Override
	public boolean getDown2() {
		return false;
	}

	@Override
	public boolean getLeft2() {
		return false;
	}

	@Override
	public boolean getRight2() {
		return false;
	}

	@Override
	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}

	@Override
	public void stopRecording() {
		if (record)
		{
			recordLog.close();
			record = false;
		}
	}

	@Override
	public void record(String filename) {
		try
		{
			FileWriter file = new FileWriter(filename);
			recordLog = new PrintWriter(file);
			record = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void playRecording(String filename) {
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
			playback = true;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setClock(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void terminate() {
		terminate = true;
		
		if (frame != null)
		{
			frame.setVisible(false);
			frame.dispose();
		}
	}

	@Override
	public void setEventList(ArrayList<ControllerEvent> events) {
		queue = events;
		playback = true;
	}
	
	public void setAgent(AiAgent agent)
	{
		this.agent = agent;
	}

	@Override
	public long getTotalBPresses() {
		return totalBPresses;
	}
	
	public int[] getDisplayData()
	{
		return imgData;
	}
}
