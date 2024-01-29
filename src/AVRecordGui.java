import java.io.InputStream;
import java.util.ArrayList;

public class AVRecordGui extends RecordingGui {
	private volatile ArrayList<Double> audioData = new ArrayList<Double>();
	
	public AVRecordGui(String filename)
	{
		super(filename);
	}
	
	@Override
	protected void doEncoding()
	{
		super.doEncoding();
		double[] data = new double[audioData.size()];
		for (int i = 0; i < audioData.size(); ++i)
		{
			data[i] = audioData.get(i);
		}
		
		WaveData wav = new WaveData(data, 44744);
		WaveEncoder encoder = new WaveEncoder((byte)16, outputFilename + ".wav");
		
		try
		{
			encoder.encode(wav);
			
			//Rename output file to temp file
			System.out.println("Moving video file");
			String cmd = "mv " + outputFilename + " " + outputFilename + ".tmp.mp4";
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
			
			//Add audio track
			System.out.println("Adding audio track");
			cmd = "ffmpeg -y -i " + outputFilename + ".tmp.mp4 -i " + outputFilename + ".wav -map 0:v -map 1:a -c:v copy -shortest " + outputFilename;
			
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
			
			//Clean up temp file
			System.out.println("Removing temp file");
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
			
			System.out.println("Removing wav file");
			cmd = "rm -f " + outputFilename + ".wav";
			
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
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	@Override
	public void writeAudioData(double data) {
		audioData.add(data * 2.0 - 1.0);
		audioData = audioData;
	}
}
