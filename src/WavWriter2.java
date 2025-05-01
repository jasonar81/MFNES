//Takes a wav file and writes it as a .nes file that can be executed on the emulator
//Uses DPCM
//Won't play on other emulators because it requires format 3 (I made up) 
//and larger cartridges than NES2 supports
//(mostly because I want to add video capabilities)
//This could easily be changed to use standard NES2 format and mapper
//Which would enable 64MB of data, which is enough for almost 25 minutes
//of direct audio

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavWriter2 {
	private static int playbackFreq = 33144;
	
	public static void main(String[] args)
	{
		String inFilename = args[0];
		File inFile = new File(inFilename);
		if (!inFile.exists())
		{
			System.out.println("Input file does not exist!");
			return;
		}
		
		String outFilename = args[1];
		File outFile = new File(outFilename);
		
		try
		{
			FileOutputStream out = new FileOutputStream(outFile);
			WaveData data = WaveDecoder.decode(inFilename);
			data = data.resample(playbackFreq);
			
			writeHeader(out, data);
			writeCode(out, data);
			writeAudioData(out, data);
			
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static int numPrgBanks(WaveData data)
	{
		int retval = 1; //1 for code
		int deltaEncoded = data.samples() / 8 + 1;
		retval += (int)(deltaEncoded / 16324);
		retval += 1;
		return retval;
	}
	
	private static void writeHeader(FileOutputStream out, WaveData data) throws IOException
	{
		byte[] sig = new byte[] {0x4e, 0x45, 0x53, 0x1a};
		out.write(sig);
		
		int numChrBanks = 0;
		int numPrgBanks = numPrgBanks(data);
		out.write(numPrgBanks & 0xff);
		out.write(numChrBanks & 0xff);
		out.write(0xe0); //low part of mapper
		out.write(0xfc); //f is next part of mapper c is format 3
		out.write(0xff); //high mapper byte
		out.write(numPrgBanks >> 8);
		out.write(numChrBanks >> 8);
		out.write(0);
		out.write(0);
		out.write(0);
		out.write(0);
		out.write(0);
	}
	
	private static void writeAudioData(FileOutputStream out, WaveData data) throws IOException
	{
		double max = 0;
		double max2 = 0;
		double[] newData = new double[data.samples()];
		for (int i = 0; i < data.samples(); ++i)
		{
			double val = data.channel1()[i];
			if (Math.abs(val) > max)
			{
				max = Math.abs(val);
			}
			
			if (data.stereo())
			{
				val += data.channel2()[i];
				if (Math.abs(val) > max2)
				{
					max2 = Math.abs(val);
				}
			}
			else
			{
				max2 = max;
			}
			
			newData[i] = val;
		}
		
		for (int i = 0; i < newData.length; ++i)
		{
			newData[i] *= (max / max2);
		}
		
		byte[] scaledData = new byte[newData.length];
		for (int i = 0; i < newData.length; ++i)
		{
			int writeVal = (int)(newData[i] * 64 + 64);
			if (writeVal < 0)
			{
				writeVal = 0;
			}
			
			if (writeVal > 127)
			{
				writeVal = 127;
			}
			
			scaledData[i] = (byte)writeVal;
		}
		
		byte currentValue = 63;
		byte[] audioBanks = new byte[16384 * (numPrgBanks(data) - 1)];
		int position = 0;
		for (int i = 0; i < scaledData.length; i += 8)
		{
			int delta = 0;
			int counter = 0;
			int mask = 1;
			for (int j = i; j < scaledData.length; ++j) 
			{	
				if (scaledData[j] > currentValue)
				{
					delta += mask;
					
					if (currentValue <= 125)
					{
						currentValue += 2;
					}
				}
				else
				{
					if (currentValue >= 2)
					{
						currentValue -= 2;
					}
				}
				
				++counter;
				mask <<= 1;
				if (counter == 8)
				{
					audioBanks[position++] = (byte)delta;
					break;
				}
			}
			
			if (position % 4096 == 4081)
			{
				for (int j = 0; j < 9; ++j)
				{
					audioBanks[position++] = (byte)0;
				}
				
				audioBanks[position++] = (byte)0x00;
				audioBanks[position++] = (byte)0x80; //NMI vector = 0x8000
				audioBanks[position++] = (byte)0x01;
				audioBanks[position++] = (byte)0x80; //RESET vector = 0x8001
				audioBanks[position++] = (byte)0x00;
				audioBanks[position++] = (byte)0x90; //IRQ vector = 0x9000
			}
		}
		
		for (; position < audioBanks.length; ++position)
		{
			if (position % 4096 == 4081)
			{
				for (int j = 0; j < 9; ++j)
				{
					audioBanks[position++] = (byte)0;
				}
				
				audioBanks[position++] = (byte)0x00;
				audioBanks[position++] = (byte)0x80; //NMI vector = 0x8000
				audioBanks[position++] = (byte)0x01;
				audioBanks[position++] = (byte)0x80; //RESET vector = 0x8001
				audioBanks[position++] = (byte)0x00;
				audioBanks[position] = (byte)0x90; //IRQ vector = 0x9000
			}
		}
		
		out.write(audioBanks);
	}
	
	private static void writeCode(FileOutputStream out, WaveData data) throws IOException
	{
		int start = 0x8000;
		byte[] codeBank = new byte[16384];
		int numAudioBanks = numPrgBanks(data) - 1;
		codeBank[0x8000 - start] = (byte)0x40; //RTI
		
		//Disable NMI
		codeBank[0x8001 - start] = (byte)0xa9; //LDA
		codeBank[0x8002 - start] = (byte)0x00; //immediate
		codeBank[0x8003 - start] = (byte)0x8d; //STA
		codeBank[0x8004 - start] = (byte)0x00; 
		codeBank[0x8005 - start] = (byte)0x20; //Absolute
		
		//Disable IRQ
		codeBank[0x8006 - start] = (byte)0x78; //SEI
		
		//Enable DMC channel
		codeBank[0x8007 - start] = (byte)0xa9; //LDA
		codeBank[0x8008 - start] = (byte)0x10; //immediate
		codeBank[0x8009 - start] = (byte)0x8d; //STA
		codeBank[0x800a - start] = (byte)0x15; 
		codeBank[0x800b - start] = (byte)0x40; //Absolute
		
		//Store that we are on bank 1 and explicitly set bank 1
		codeBank[0x800c - start] = (byte)0xa9; //LDA
		codeBank[0x800d - start] = (byte)0x01; //immediate
		codeBank[0x800e - start] = (byte)0x85; //STA
		codeBank[0x800f - start] = (byte)0x00; //Zero page 
		codeBank[0x8010 - start] = (byte)0x8d; //STA
		codeBank[0x8011 - start] = (byte)0x00; 
		codeBank[0x8012 - start] = (byte)0x80; //Absolute
		codeBank[0x8013 - start] = (byte)0xa9; //LDA
		codeBank[0x8014 - start] = (byte)0x00; //immediate
		codeBank[0x8015 - start] = (byte)0x85; //STA
		codeBank[0x8016 - start] = (byte)0x01; //Zero page 
		codeBank[0x8017 - start] = (byte)0x8d; //STA
		codeBank[0x8018 - start] = (byte)0x00; 
		codeBank[0x8019 - start] = (byte)0xa0; //Absolute
		
		//Store total number of audio banks to play through
		codeBank[0x801a - start] = (byte)0xa9; //LDA
		codeBank[0x801b - start] = (byte)(numAudioBanks & 0xff); //immediate
		codeBank[0x801c - start] = (byte)0x85; //STA
		codeBank[0x801d - start] = (byte)0x02; //Zero page 
		codeBank[0x801e - start] = (byte)0xa9; //LDA
		codeBank[0x801f - start] = (byte)((numAudioBanks & 0xff00) >> 8); //immediate
		codeBank[0x8020 - start] = (byte)0x85; //STA
		codeBank[0x8021 - start] = (byte)0x03; //Zero page 
		
		//Set audio data pointer
		codeBank[0x8022 - start] = (byte)0xa9; //LDA
		codeBank[0x8023 - start] = (byte)0x00; //immediate
		codeBank[0x8024 - start] = (byte)0x85; //STA
		codeBank[0x8025 - start] = (byte)0x04; //Zero page 
		codeBank[0x8026 - start] = (byte)0xa9; //LDA
		codeBank[0x8027 - start] = (byte)0xc0; //immediate
		codeBank[0x8028 - start] = (byte)0x85; //STA
		codeBank[0x8029 - start] = (byte)0x05; //Zero page 
		
		//Set initial DMC channel value of 63
		codeBank[0x802a - start] = (byte)0xa9; //LDA
		codeBank[0x802b - start] = (byte)63; //immediate
		codeBank[0x802c - start] = (byte)0x8d; //STA
		codeBank[0x802d - start] = (byte)0x11; 
		codeBank[0x802e - start] = (byte)0x40; //Absolute
		
		//Set DMC channel settings (0x8f -> 0x4010)
		codeBank[0x802f - start] = (byte)0xa9; //LDA
		codeBank[0x8030 - start] = (byte)0x8f; //immediate
		codeBank[0x8031 - start] = (byte)0x8d; //STA
		codeBank[0x8032 - start] = (byte)0x10; 
		codeBank[0x8033 - start] = (byte)0x40; //Absolute
		
		//Write 0xC0 -> 0x4017 disable frame interrupts
		codeBank[0x8034 - start] = (byte)0xa9; //LDA
		codeBank[0x8035 - start] = (byte)0xc0; //immediate
		codeBank[0x8036 - start] = (byte)0x8d; //STA
		codeBank[0x8037 - start] = (byte)0x17; 
		codeBank[0x8038 - start] = (byte)0x40; //Absolute
		
		//Enable IRQ
		codeBank[0x8039 - start] = (byte)0x58; //CLI
		
		//Load first sample buffer
		codeBank[0x803a - start] = (byte)0xa9; //LDA
		codeBank[0x803b - start] = (byte)0x00; //immediate
		codeBank[0x803c - start] = (byte)0x8d; //STA
		codeBank[0x803d - start] = (byte)0x12; 
		codeBank[0x803e - start] = (byte)0x40; //Absolute
		
		//Set audio length
		codeBank[0x803f - start] = (byte)0xa9; //LDA
		codeBank[0x8040 - start] = (byte)0xff; //immediate
		codeBank[0x8041 - start] = (byte)0x8d; //STA
		codeBank[0x8042 - start] = (byte)0x13; 
		codeBank[0x8043 - start] = (byte)0x40; //Absolute
		
		//Endless loop
		codeBank[0x8044 - start] = (byte)0x4c; //JMP
		codeBank[0x8045 - start] = (byte)0x44; 
		codeBank[0x8046 - start] = (byte)0x80; //Absolute
		
		//IRQ routine @ 0x9000
		//Clear interrupt
		codeBank[0x9000 - start] = (byte)0xa9; //LDA
		codeBank[0x9001 - start] = (byte)0x10; //immediate
		codeBank[0x9002 - start] = (byte)0x8d; //STA
		codeBank[0x9003 - start] = (byte)0x15; 
		codeBank[0x9004 - start] = (byte)0x40; //Absolute
		
		//Update audio pointer
		codeBank[0x9005 - start] = (byte)0x18; //CLC
		codeBank[0x9006 - start] = (byte)0xa9; //LDA
		codeBank[0x9007 - start] = (byte)0x10; //immediate
		codeBank[0x9008 - start] = (byte)0x65; //ADC
		codeBank[0x9009 - start] = (byte)0x05; //zero page
		codeBank[0x900a - start] = (byte)0x85; //STA
		codeBank[0x900b - start] = (byte)0x05; //Zero page 
		codeBank[0x900c - start] = (byte)0xf0; //BEQ
		codeBank[0x900d - start] = (byte)0x7f; //relative
		int branchTarget = 0x900e + 0x7f; //Need to bank switch
		
		//Set audio address
		codeBank[0x900e - start] = (byte)0x18; //CLC
		codeBank[0x900f - start] = (byte)0xa9; //LDA
		codeBank[0x9010 - start] = (byte)0x40; //immediate
		codeBank[0x9011 - start] = (byte)0x65; //ADC
		codeBank[0x9012 - start] = (byte)0x04; //zero page
		codeBank[0x9013 - start] = (byte)0x85; //STA
		codeBank[0x9014 - start] = (byte)0x04; //Zero page
		codeBank[0x9015 - start] = (byte)0x8d; //STA
		codeBank[0x9016 - start] = (byte)0x12; 
		codeBank[0x9017 - start] = (byte)0x40; //Absolute
		
		//Set audio length
		codeBank[0x9018 - start] = (byte)0xa9; //LDA
		codeBank[0x9019 - start] = (byte)0xff; //immediate
		codeBank[0x901a - start] = (byte)0x8d; //STA
		codeBank[0x901b - start] = (byte)0x13; 
		codeBank[0x901c - start] = (byte)0x40; //Absolute
		codeBank[0x901d - start] = (byte)0x40; //RTI
	
		//Handle bank switch
		codeBank[branchTarget - start] = (byte)0x38; //SEC
		codeBank[branchTarget + 1 - start] = (byte)0xa9; //LDA
		codeBank[branchTarget + 2 - start] = (byte)0x00; //immediate
		codeBank[branchTarget + 3 - start] = (byte)0x65; //ADC
		codeBank[branchTarget + 4 - start] = (byte)0x00; //zero page
		codeBank[branchTarget + 5 - start] = (byte)0x85; //STA
		codeBank[branchTarget + 6 - start] = (byte)0x00; //Zero page
		codeBank[branchTarget + 7 - start] = (byte)0x8d; //STA
		codeBank[branchTarget + 8 - start] = (byte)0x00; 
		codeBank[branchTarget + 9 - start] = (byte)0x80; //Absolute
		codeBank[branchTarget + 10 - start] = (byte)0xa9; //LDA
		codeBank[branchTarget + 11 - start] = (byte)0x00; //immediate
		codeBank[branchTarget + 12 - start] = (byte)0x65; //ADC
		codeBank[branchTarget + 13 - start] = (byte)0x01; //zero page
		codeBank[branchTarget + 14 - start] = (byte)0x85; //STA
		codeBank[branchTarget + 15 - start] = (byte)0x01; //Zero page
		codeBank[branchTarget + 16 - start] = (byte)0x8d; //STA
		codeBank[branchTarget + 17 - start] = (byte)0x00; 
		codeBank[branchTarget + 18 - start] = (byte)0xa0; //Absolute
		codeBank[branchTarget + 19 - start] = (byte)0xa9; //LDA
		codeBank[branchTarget + 20 - start] = (byte)0xc0; //immediate
		codeBank[branchTarget + 21 - start] = (byte)0x85; //STA
		codeBank[branchTarget + 22 - start] = (byte)0x05; //Zero page 
		codeBank[branchTarget + 23 - start] = (byte)0x4c; //JMP
		codeBank[branchTarget + 24 - start] = (byte)0x0e; 
		codeBank[branchTarget + 25 - start] = (byte)0x90; //Absolute
		
		out.write(codeBank);
	}
}
