//Takes a wav file and writes it as a .nes file that can be executed on the emulator
//Uses direct value setting
//Could be changed to use DPCM, but quality would be worse (but program size would be smaller)
//Won't play on other emulators because it requires format 3 (I made up) 
//and larger cartridges than NES2 supports
//(mostly because I want to add video capabilities)
//This could easily be changed to use standard NES2 format and mapper
//Which would enable 64MB of data, which is enough for almost 25 minutes
//of direct audio

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavWriter {
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
			
			writeHeader(out, data);
			writeAudioData(out, data);
			writeCode(out, data);
			
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void writeHeader(FileOutputStream out, WaveData data) throws IOException
	{
		byte[] sig = new byte[] {0x4e, 0x45, 0x53, 0x1a};
		out.write(sig);
		
		int numChrBanks = 0;
		int numPrgBanks = 2 + (data.samples() / 16384);
		out.write(numPrgBanks & 0xff);
		out.write(numChrBanks & 0xff);
		out.write(0xf0); //fine if only using $2000 and FFFF mapper
		out.write(0xfc); //ff is next part of mapper c is format 3
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
		
		int i = 0;
		for (; i < newData.length; ++i)
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
			
			out.write(writeVal);
		}
		
		out.write(0);
		i++;
		while ((i % 16384) != 0)
		{
			++i;
			out.write(0);
		}
	}
	
	private static void writeCode(FileOutputStream out, WaveData data) throws IOException
	{
		int start = 0xc000;
		byte[] codeBank = new byte[16384];
		int numAudioBanks = 1 + (data.samples() / 16384);
		int rate = data.rate();
		double nanosPerAudioTick = 1000000000.0 / rate;
		double nanosPerCpuTick = 1000000000.0 / (5369317.5 / 3);
		double cpuTicksPerAudioTick = nanosPerAudioTick / nanosPerCpuTick;
		int cpuCyclesBetweenSamples = (int)cpuTicksPerAudioTick;
		
		codeBank[0xc000 - start] = (byte)0x40; //RTI
		codeBank[0xfffa - start] = (byte)0x00;
		codeBank[0xfffb - start] = (byte)0xc0; //NMI vector = 0xc000
		codeBank[0xfffc - start] = (byte)0x01;
		codeBank[0xfffd - start] = (byte)0xc0; //RESET vector = 0xc001
		codeBank[0xfffe - start] = (byte)0x00;
		codeBank[0xffff - start] = (byte)0xc0; //IRQ vector = 0xc000
		
		//Disable NMI
		codeBank[0xc001 - start] = (byte)0xa9; //LDA
		codeBank[0xc002 - start] = (byte)0x00; //immediate
		codeBank[0xc003 - start] = (byte)0x8d; //STA
		codeBank[0xc004 - start] = (byte)0x00; 
		codeBank[0xc005 - start] = (byte)0x20; //Absolute
		
		//Disable IRQ
		codeBank[0xc006 - start] = (byte)0x78; //SEI
		
		//Enable direct DMC channel access
		codeBank[0xc007 - start] = (byte)0xa9; //LDA
		codeBank[0xc008 - start] = (byte)0x10; //immediate
		codeBank[0xc009 - start] = (byte)0x8d; //STA
		codeBank[0xc00a - start] = (byte)0x15; 
		codeBank[0xc00b - start] = (byte)0x40; //Absolute
		
		//Store that we are on bank 0 and explicitly set bank 0
		codeBank[0xc00c - start] = (byte)0xa9; //LDA
		codeBank[0xc00d - start] = (byte)0x00; //immediate
		codeBank[0xc00e - start] = (byte)0x85; //STA
		codeBank[0xc00f - start] = (byte)0x00; //Zero page 
		codeBank[0xc010 - start] = (byte)0x85; //STA
		codeBank[0xc011 - start] = (byte)0x01; //Zero page 
		codeBank[0xc012 - start] = (byte)0x8d; //STA
		codeBank[0xc013 - start] = (byte)0x00; 
		codeBank[0xc014 - start] = (byte)0x80; //Absolute
		codeBank[0xc015 - start] = (byte)0x8d; //STA
		codeBank[0xc016 - start] = (byte)0x00; 
		codeBank[0xc017 - start] = (byte)0xa0; //Absolute
		
		//Store total number of audio banks to play through
		codeBank[0xc018 - start] = (byte)0xa9; //LDA
		codeBank[0xc019 - start] = (byte)(numAudioBanks & 0xff); //immediate
		codeBank[0xc01a - start] = (byte)0x85; //STA
		codeBank[0xc01b - start] = (byte)0x02; //Zero page 
		codeBank[0xc01c - start] = (byte)0xa9; //LDA
		codeBank[0xc01d - start] = (byte)((numAudioBanks & 0xff00) >> 8); //immediate
		codeBank[0xc01e - start] = (byte)0x85; //STA
		codeBank[0xc01f - start] = (byte)0x03; //Zero page 
		
		//Set audio data pointer
		codeBank[0xc020 - start] = (byte)0xa9; //LDA
		codeBank[0xc021 - start] = (byte)0x00; //immediate
		codeBank[0xc022 - start] = (byte)0x85; //STA
		codeBank[0xc023 - start] = (byte)0x04; //Zero page 
		codeBank[0xc024 - start] = (byte)0xa9; //LDA
		codeBank[0xc025 - start] = (byte)0x80; //immediate
		codeBank[0xc026 - start] = (byte)0x85; //STA
		codeBank[0xc027 - start] = (byte)0x05; //Zero page 
		
		//Zero y register
		codeBank[0xc028 - start] = (byte)0xa0; //LDY
		codeBank[0xc029 - start] = (byte)0x00; //immediate
		
		//Setup x register
		codeBank[0xc02a - start] = (byte)0xa2; //LDX
		codeBank[0xc02b - start] = (byte)0xC0; //immediate
		
		//Main loop
		//Load sample
		int loopStart = 0xc02c;
		codeBank[0xc02c - start] = (byte)0xb1; //LDA
		codeBank[0xc02d - start] = (byte)0x04; //indirect,y
		
		//Play sample
		codeBank[0xc02e - start] = (byte)0x8d; //STA
		codeBank[0xc02f - start] = (byte)0x11; 
		codeBank[0xc030 - start] = (byte)0x40; //Absolute
		
		int timeToKill = cpuCyclesBetweenSamples;
		timeToKill -= 9; //Cycles needed for load and play
		
		//Increment audio pointer
		codeBank[0xc031 - start] = (byte)0xc8; //INY
		codeBank[0xc032 - start] = (byte)0xf0; //BEQ
		codeBank[0xc033 - start] = (byte)0x7f; //relative
		int branchTarget = 0xc034 + 0x7f;
		
		timeToKill -= 4; //used so far based on not taking the previous jump
		timeToKill -= 3; //Because we have to do the jump still
		int offset = 0xc034;
		for (int i = 0; i < timeToKill; i += 2)
		{
			codeBank[offset - start] = (byte)0xea; //NOP
			++offset;
		}
		
		codeBank[offset - start] = (byte)0x4c; //JMP
		codeBank[offset + 1 - start] = (byte)(loopStart & 0xff);
		codeBank[offset + 2 - start] = (byte)((loopStart >> 8) & 0xff); //absolute
		
		//Handle page crossing
		//+1 for branch
		codeBank[branchTarget - start] = (byte)0xe6; //INC (5)
		codeBank[branchTarget + 1 - start] = (byte)0x05; //zero page
		codeBank[branchTarget + 2 - start] = (byte)0xe4; //CPX (3)
		codeBank[branchTarget + 3 - start] = (byte)0x05; //zero page
		codeBank[branchTarget + 4 - start] = (byte)0xf0; //BEQ (2)
		codeBank[branchTarget + 5 - start] = (byte)0x7f; //relative
		int branchTarget2 = branchTarget + 6 + 0x7f;
		timeToKill -= 11;
		
		offset = branchTarget + 6;
		for (int i = 0; i < timeToKill; i += 2)
		{
			codeBank[offset - start] = (byte)0xea; //NOP
			++offset;
		}
		
		codeBank[offset - start] = (byte)0x4c; //JMP
		codeBank[offset + 1 - start] = (byte)(loopStart & 0xff);
		codeBank[offset + 2 - start] = (byte)((loopStart >> 8) & 0xff); //absolute
		
		//Handle bank crossing
		//We may only have 10 cycles left, so we may be a little late
		//We may have to do some short cycles to catch up
		//We need to increment 0x0000 - 0x0001
		//And compare it to 0x0002 - 0x0003
		//If equal we are done
		//Else
		//We need to set 0x0005 back to 0x80
		//And we need to load the new bank
		//+2 for branch taken and page crossing
		codeBank[branchTarget2 - start] = (byte)0x38; //SEC (2)
		codeBank[branchTarget2 + 1 - start] = (byte)0xa9; //LDA (2)
		codeBank[branchTarget2 + 2 - start] = (byte)0x00; //immediate
		codeBank[branchTarget2 + 3 - start] = (byte)0x65; //ADC (3)
		codeBank[branchTarget2 + 4 - start] = (byte)0x00; //zero page
		codeBank[branchTarget2 + 5 - start] = (byte)0x85; //STA (3)
		codeBank[branchTarget2 + 6 - start] = (byte)0x00; //zero page
		codeBank[branchTarget2 + 7 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget2 + 8 - start] = (byte)0x00; 
		codeBank[branchTarget2 + 9 - start] = (byte)0x80; //absolute
		codeBank[branchTarget2 + 10 - start] = (byte)0x08; //PHP (3)
		codeBank[branchTarget2 + 11 - start] = (byte)0xc5; //CMP (3)
		codeBank[branchTarget2 + 12 - start] = (byte)0x02; //zero page
		codeBank[branchTarget2 + 13 - start] = (byte)0xf0; //BEQ (2)
		codeBank[branchTarget2 + 14 - start] = (byte)0x7f; //relative
		int branchTarget3 = branchTarget2 + 15 + 0x7f;
		
		codeBank[branchTarget2 + 15 - start] = (byte)0x28; //PLP (4)
		codeBank[branchTarget2 + 16 - start] = (byte)0xa9; //LDA (2)
		codeBank[branchTarget2 + 17 - start] = (byte)0x00; //immediate
		codeBank[branchTarget2 + 18 - start] = (byte)0x65; //ADC (3)
		codeBank[branchTarget2 + 19 - start] = (byte)0x01; //zero page
		codeBank[branchTarget2 + 20 - start] = (byte)0x85; //STA (3)
		codeBank[branchTarget2 + 21 - start] = (byte)0x01; //zero page
		codeBank[branchTarget2 + 22 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget2 + 23 - start] = (byte)0x00; 
		codeBank[branchTarget2 + 24 - start] = (byte)0xa0; //absolute
		
		codeBank[branchTarget2 + 25 - start] = (byte)0xa9; //LDA (2)
		codeBank[branchTarget2 + 26 - start] = (byte)0x80; //immediate
		codeBank[branchTarget2 + 27 - start] = (byte)0x85; //STA (3)
		codeBank[branchTarget2 + 28 - start] = (byte)0x05; //zero page
		
		//Load sample
		codeBank[branchTarget2 + 29 - start] = (byte)0xb1; //LDA (5)
		codeBank[branchTarget2 + 30 - start] = (byte)0x04; //indirect,y
		
		//Play sample
		codeBank[branchTarget2 + 31 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget2 + 32 - start] = (byte)0x11; 
		codeBank[branchTarget2 + 33 - start] = (byte)0x40; //Absolute
		
		//Time to kill already paid for final branch back to main loop
		timeToKill += cpuCyclesBetweenSamples; //Move on to next sample
		timeToKill -= 54; //Cost of the above
		
		//Increment
		codeBank[branchTarget2 + 34 - start] = (byte)0xc8; //INY (2)
		
		//Load sample
		codeBank[branchTarget2 + 35 - start] = (byte)0xb1; //LDA (5)
		codeBank[branchTarget2 + 36 - start] = (byte)0x04; //indirect,y
		
		//Play sample
		codeBank[branchTarget2 + 37 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget2 + 38 - start] = (byte)0x11; 
		codeBank[branchTarget2 + 39 - start] = (byte)0x40; //Absolute
		
		//Increment
		codeBank[branchTarget2 + 40 - start] = (byte)0xc8; //INY (2)
		
		timeToKill += cpuCyclesBetweenSamples; //Move on to next sample
		timeToKill -= 13;
		
		offset = branchTarget2 + 41;
		for (int i = 0; i < timeToKill; i += 2)
		{
			codeBank[offset - start] = (byte)0xea; //NOP
			++offset;
		}
		
		codeBank[offset - start] = (byte)0x4c; //JMP
		codeBank[offset + 1 - start] = (byte)(loopStart & 0xff);
		codeBank[offset + 2 - start] = (byte)((loopStart >> 8) & 0xff); //absolute
		
		//The low byte of the new bank matched the low byte of the last bank
		//+1
		codeBank[branchTarget3 - start] = (byte)0x28; //PLP (4)
		codeBank[branchTarget3 + 1 - start] = (byte)0xa9; //LDA (2)
		codeBank[branchTarget3 + 2 - start] = (byte)0x00; //immediate
		codeBank[branchTarget3 + 3 - start] = (byte)0x65; //ADC (3)
		codeBank[branchTarget3 + 4 - start] = (byte)0x01; //zero page
		codeBank[branchTarget3 + 5 - start] = (byte)0x85; //STA (3)
		codeBank[branchTarget3 + 6 - start] = (byte)0x01; //zero page
		codeBank[branchTarget3 + 7 - start] = (byte)0xc5; //CMP (3)
		codeBank[branchTarget3 + 8 - start] = (byte)0x03; //zero page
		codeBank[branchTarget3 + 9 - start] = (byte)0xf0; //BEQ (2)
		codeBank[branchTarget3 + 10 - start] = (byte)0xfe; //relative - infinite loop if we are done
		codeBank[branchTarget3 + 11 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget3 + 12 - start] = (byte)0x00; 
		codeBank[branchTarget3 + 13 - start] = (byte)0xa0; //absolute
		
		codeBank[branchTarget3 + 14 - start] = (byte)0xa9; //LDA (2)
		codeBank[branchTarget3 + 15 - start] = (byte)0x80; //immediate
		codeBank[branchTarget3 + 16 - start] = (byte)0x85; //STA (3)
		codeBank[branchTarget3 + 17 - start] = (byte)0x05; //zero page
		
		//Load sample
		codeBank[branchTarget3 + 18 - start] = (byte)0xb1; //LDA (5)
		codeBank[branchTarget3 + 19 - start] = (byte)0x04; //indirect,y
		
		//Play sample
		codeBank[branchTarget3 + 20 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget3 + 21 - start] = (byte)0x11; 
		codeBank[branchTarget3 + 22 - start] = (byte)0x40; //Absolute
		
		//Time to kill already paid for final branch back to main loop
		timeToKill += cpuCyclesBetweenSamples; //Move on to next sample
		timeToKill -= 60; //Cost of the above
		
		//Increment
		codeBank[branchTarget3 + 23 - start] = (byte)0xc8; //INY (2)
		
		//Load sample
		codeBank[branchTarget3 + 24 - start] = (byte)0xb1; //LDA (5)
		codeBank[branchTarget3 + 25 - start] = (byte)0x04; //indirect,y
		
		//Play sample
		codeBank[branchTarget3 + 26 - start] = (byte)0x8d; //STA (4)
		codeBank[branchTarget3 + 27 - start] = (byte)0x11; 
		codeBank[branchTarget3 + 28 - start] = (byte)0x40; //Absolute
		
		//Increment
		codeBank[branchTarget3 + 29 - start] = (byte)0xc8; //INY (2)
		
		timeToKill += cpuCyclesBetweenSamples; //Move on to next sample
		timeToKill -= 13;
		
		offset = branchTarget3 + 30;
		for (int i = 0; i < timeToKill; i += 2)
		{
			codeBank[offset - start] = (byte)0xea; //NOP
			++offset;
		}
		
		codeBank[offset - start] = (byte)0x4c; //JMP
		codeBank[offset + 1 - start] = (byte)(loopStart & 0xff);
		codeBank[offset + 2 - start] = (byte)((loopStart >> 8) & 0xff); //absolute
		
		out.write(codeBank);
	}
}
