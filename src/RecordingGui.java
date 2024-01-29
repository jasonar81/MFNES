//DefaultGui that can encode a recording

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class RecordingGui extends DefaultGUI {
	protected String outputFilename;
	private int frameNum = 0;
	private boolean ready = false;
	private ArrayList<File> files = new ArrayList<File>();
	
	public RecordingGui(String outputFilename)
	{
		super();
		this.outputFilename = outputFilename;
		frame = null;
		panel = null;
		strategy = null;
		audio = null;
		img = null;
		img = new BufferedImage(280, 240, BufferedImage.TYPE_INT_RGB);
		ready = true;
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
					doEncoding();
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
	
	@Override
	public void swapBuffers() {
		if (!ready)
		{
			return;
		}
		
		int index = outputFilename.lastIndexOf('/');
		String imageFilename;
		if (index == -1)
		{
			imageFilename = String.format("%08d", frameNum++) + outputFilename + ".jpg";
		}
		else
		{
			imageFilename = outputFilename.substring(0, index+1) + String.format("%08d", frameNum++) + outputFilename.substring(index+1) + ".jpg";
		}
		
		File outputFile = new File(imageFilename);
		files.add(outputFile);
		img.getRaster().setDataElements(0, 0, 280, 240, imgData);
		img = img;
		
		try {
			ImageIO.write(img, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void doEncoding()
	{
		String glob;
		int index = outputFilename.lastIndexOf('/');
		if (index == -1)
		{
			glob = "*" + outputFilename + ".jpg";
		}
		else
		{
			glob = outputFilename.substring(0, index+1) + "*" + outputFilename.substring(index+1) + ".jpg";
		}
		
		System.out.println("Doing encoding");
		String cmd = "ffmpeg -y -framerate 60 -pattern_type glob -i '" + glob + "' -c:v libx264 -pix_fmt yuv420p " + outputFilename + ".tmp.mp4"; 
		//System.out.println(cmd);
		Process p;
		
		try {
			p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();
			StreamEater stdoutEater = new StreamEater (stdout);
			StreamEater stderrEater = new StreamEater (stderr);
			stdoutEater.start();
			stderrEater.start();
			  
			while (true)
			{
				try
				{
					p.waitFor();
					break;
				}
				catch(Exception f) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Doing rescaling");
		cmd = "ffmpeg -y -i " + outputFilename + ".tmp.mp4" + " -vf scale=960:720 " + outputFilename;
		
		try {
			p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();
			StreamEater stdoutEater = new StreamEater (stdout);
			StreamEater stderrEater = new StreamEater (stderr);
			stdoutEater.start();
			stderrEater.start();
			  
			while (true)
			{
				try
				{
					p.waitFor();
					break;
				}
				catch(Exception f) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Removing temporary");
		cmd = "rm -f " + outputFilename + ".tmp.mp4";
		
		try {
			p = Runtime.getRuntime().exec(new String[]{"sh", "-c", cmd});
			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();
			StreamEater stdoutEater = new StreamEater (stdout);
			StreamEater stderrEater = new StreamEater (stderr);
			stdoutEater.start();
			stderrEater.start();
			  
			while (true)
			{
				try
				{
					p.waitFor();
					break;
				}
				catch(Exception f) {}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Removing jpgs");
		for (File file : files)
		{
			file.delete();
		}
	}
}
