//Takes a video file and writes it as a .nes file that can be executed on the emulator
//Uses DPCM
//Won't play on other emulators because it requires format 3 (I made up) 
//and larger cartridges than NES2 supports

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.imageio.ImageIO;

public class VideoWriter {
	private static int playbackFreq = 33144;
	private static int numFrames = 0;
	private static int[] colorLookup = new int[64];
	private static HashMap<Integer, Byte> reverseLookup = new HashMap<Integer, Byte>();
	private static int quality = 13;
	
	private static int frameNumber = 0;
	
	private static HashMap<Integer, Integer> debugTable = new HashMap<Integer, Integer>();
	
	static
	{
		colorLookup[0x00] = makeRGB(0x6b, 0x6b, 0x6b);
		colorLookup[0x01] = makeRGB(0, 0x1b, 0x87);
		colorLookup[0x02] = makeRGB(0x21, 0, 0x9a);
		colorLookup[0x03] = makeRGB(0x40, 0, 0x8c);
		colorLookup[0x04] = makeRGB(0x60, 0, 0x67);
		colorLookup[0x05] = makeRGB(0x64, 0, 0x1e);
		colorLookup[0x06] = makeRGB(0x59, 0x08, 0);
		colorLookup[0x07] = makeRGB(0x46, 0x16, 0);
		colorLookup[0x08] = makeRGB(0x26, 0x36, 0);
		colorLookup[0x09] = makeRGB(0, 0x45, 0);
		colorLookup[0x0a] = makeRGB(0, 0x47, 0x08);
		colorLookup[0x0b] = makeRGB(0, 0x42, 0x1d);
		colorLookup[0x0c] = makeRGB(0, 0x36, 0x59);
		colorLookup[0x0d] = makeRGB(0, 0, 0);
		colorLookup[0x0e] = makeRGB(0, 0, 0);
		colorLookup[0x0f] = makeRGB(0, 0, 0);
		colorLookup[0x10] = makeRGB(0xb4, 0xb4, 0xb4);
		colorLookup[0x11] = makeRGB(0x15, 0x55, 0xce);
		colorLookup[0x12] = makeRGB(0x43, 0x37, 0xea);
		colorLookup[0x13] = makeRGB(0x71, 0x24, 0xda);
		colorLookup[0x14] = makeRGB(0x9c, 0x1a, 0xb6);
		colorLookup[0x15] = makeRGB(0xaa, 0x11, 0x64);
		colorLookup[0x16] = makeRGB(0xa8, 0x2e, 0);
		colorLookup[0x17] = makeRGB(0x87, 0x4b, 0);
		colorLookup[0x18] = makeRGB(0x66, 0x6b, 0);
		colorLookup[0x19] = makeRGB(0x21, 0x83, 0);
		colorLookup[0x1a] = makeRGB(0, 0x8a, 0);
		colorLookup[0x1b] = makeRGB(0, 0x81, 0x44);
		colorLookup[0x1c] = makeRGB(0, 0x76, 0x91);
		colorLookup[0x1d] = makeRGB(0, 0, 0);
		colorLookup[0x1e] = makeRGB(0, 0, 0);
		colorLookup[0x1f] = makeRGB(0, 0, 0);
		colorLookup[0x20] = makeRGB(0xff, 0xff, 0xff);
		colorLookup[0x21] = makeRGB(0x63, 0xaf, 0xff);
		colorLookup[0x22] = makeRGB(0x82, 0x96, 0xff);
		colorLookup[0x23] = makeRGB(0xc0, 0x7d, 0xfe);
		colorLookup[0x24] = makeRGB(0xe9, 0x77, 0xff);
		colorLookup[0x25] = makeRGB(0xf5, 0x72, 0xcd);
		colorLookup[0x26] = makeRGB(0xf4, 0x88, 0x6b);
		colorLookup[0x27] = makeRGB(0xdd, 0xa0, 0x29);
		colorLookup[0x28] = makeRGB(0xbd, 0xbd, 0x0a);
		colorLookup[0x29] = makeRGB(0x89, 0xd2, 0x0e);
		colorLookup[0x2a] = makeRGB(0x5c, 0xde, 0x3e);
		colorLookup[0x2b] = makeRGB(0x4b, 0xd8, 0x86);
		colorLookup[0x2c] = makeRGB(0x4d, 0xcf, 0xd2);
		colorLookup[0x2d] = makeRGB(0x50, 0x50, 0x50);
		colorLookup[0x2e] = makeRGB(0, 0, 0);
		colorLookup[0x2f] = makeRGB(0, 0, 0);
		colorLookup[0x30] = makeRGB(0xff, 0xff, 0xff);
		colorLookup[0x31] = makeRGB(0xbe, 0xe1, 0xff);
		colorLookup[0x32] = makeRGB(0xd4, 0xd4, 0xff);
		colorLookup[0x33] = makeRGB(0xe3, 0xca, 0xff);
		colorLookup[0x34] = makeRGB(0xf0, 0xc9, 0xff);
		colorLookup[0x35] = makeRGB(0xff, 0xc6, 0xe3);
		colorLookup[0x36] = makeRGB(0xff, 0xce, 0xc9);
		colorLookup[0x37] = makeRGB(0xf4, 0xdc, 0xaf);
		colorLookup[0x38] = makeRGB(0xeb, 0xa5, 0xa1);
		colorLookup[0x39] = makeRGB(0xd2, 0xef, 0xa2);
		colorLookup[0x3a] = makeRGB(0xbe, 0xf4, 0xb5);
		colorLookup[0x3b] = makeRGB(0xb8, 0xf1, 0xd0);
		colorLookup[0x3c] = makeRGB(0xb8, 0xed, 0xf1);
		colorLookup[0x3d] = makeRGB(0xbd, 0xbd, 0xbd);
		colorLookup[0x3e] = makeRGB(0, 0, 0);
		colorLookup[0x3f] = makeRGB(0, 0, 0);
		
		reverseLookup.put(0, (byte)0x0f);
		for (int i = 0; i < 64; ++i)
		{
			if (colorLookup[i] != 0)
			{
				reverseLookup.put(colorLookup[i], (byte)i);
			}
		}
	}
	
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
		
		if (args.length == 3)
		{
			quality = Integer.parseInt(args[2]);
		}
		
		try
		{
			FileOutputStream out = new FileOutputStream(outFile);
			
			//Make wav file of audio from the video
			String cmd = "ffmpeg -y -i " + inFilename + " -map a " + inFilename + ".wav";
			Utils.executeCommand(cmd);
			
			WaveData data = WaveDecoder.decode(inFilename + ".wav");
			data = data.resample(playbackFreq);
			
			//Delete wav file
			cmd = "rm -f " + inFilename + ".wav";
			Utils.executeCommand(cmd);
			
			//Make sure the video is 4:3
			cmd = "ffmpeg -y -i " + inFilename + " -vf scale=960:720 " + inFilename + ".tmp.mp4";
			Utils.executeCommand(cmd);
			
			//Get all the frames of the video as png images
			cmd = "ffmpeg -y -i " + inFilename + ".tmp.mp4 -r 60 -s 280x240 frame%06d.png";
			Utils.executeCommand(cmd);
			
			//Delete temp video
			cmd = "rm -f " + inFilename + ".tmp.mp4";
			Utils.executeCommand(cmd);
			
			int i = 0;
			while (true)
			{
				String filename = System.getProperty("user.dir") + "/frame" + String.format("%06d", i+1) + ".png";
				File file = new File(filename);
				if (!file.exists())
				{
					break;
				}
				
				++i;
			}
			
			numFrames = i;
			System.out.println("Number of frames = " + numFrames);
		
			writeHeader(out, data);
			writeCode(out, data);
			writeAudioData(out, data);
			writeVideoData(out);
			
			out.close();
			
			for (i = 0; i < numFrames; ++i)
			{
				String filename = System.getProperty("user.dir") + "/frame" + String.format("%06d", i+1) + ".png";
				File file = new File(filename);
				file.delete();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static int makeRGB(int r, int g, int b)
	{
		return (r << 16) + (g << 8) + b;
	}
	
	private static int numPrgBanks(WaveData data)
	{
		int retval = 1; //1 for code
		int deltaEncoded = data.samples() / 8 + 1;
		retval += (int)(deltaEncoded / 16324);
		retval += 1;
		return retval;
	}
	
	private static int numChrBanks()
	{
		int retval = numFrames / 2;
		if (numFrames % 2 == 1)
		{
			++retval;
		}
		
		return retval;
	}
	
	private static void writeHeader(FileOutputStream out, WaveData data) throws IOException
	{
		byte[] sig = new byte[] {0x4e, 0x45, 0x53, 0x1a};
		out.write(sig);
		
		int numChrBanks = numChrBanks();
		if (numChrBanks % 2 == 1)
		{
			++numChrBanks;
		}
		
		numChrBanks *= 3; //They are 12k banks
		numChrBanks /= 2;
		
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
	
	private static void writeVideoData(FileOutputStream out) throws IOException
	{
		for (int i = 0; i < numFrames; i += 2)
		{
			System.out.println(i + "/" + numFrames);
			writeTwoFrames(i+1, out);
		}
		
		if (numFrames % 2 == 1)
		{
			writeLastFrame(out);
		}
		
		if (numChrBanks() % 2 == 1)
		{
			writeLastFrame(out);
		}
	}
	
	private static void writeTwoFrames(int startFrame, FileOutputStream out) throws IOException
	{
		String filename1 = System.getProperty("user.dir") + "/frame" + String.format("%06d", startFrame) + ".png";
		String filename2 = System.getProperty("user.dir") + "/frame" + String.format("%06d", startFrame+1) + ".png";
		BufferedImage frame1 = ImageIO.read(new File(filename1));
		BufferedImage frame2 = ImageIO.read(new File(filename2));
	
		int[] rgbArray1 = getCroppedRgbArray(frame1);
		int[] rgbArray2 = getCroppedRgbArray(frame2);
		
		rgbArray1 = reduceColors(rgbArray1);
		rgbArray2 = reduceColors(rgbArray2);
		
		rgbArray1 = convertToTiles(rgbArray1);
		rgbArray2 = convertToTiles(rgbArray2);
		
		//BufferedImage out1 = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
		//BufferedImage out2 = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
		//out1.getRaster().setDataElements(0, 0, 256, 240, rgbArray1);
		//out2.getRaster().setDataElements(0, 0, 256, 240, rgbArray2);
		//String filename = System.getProperty("user.dir") + "/" + String.format("%04d", frameNumber++) + ".png";
		//ImageIO.write(out1, "png", new File(filename));
		//filename = System.getProperty("user.dir") + "/" + String.format("%04d", frameNumber++) + ".png";
		//ImageIO.write(out2, "png", new File(filename));
		
		int[] palette1 = getPalette(rgbArray1);
		int[] palette2 = getPalette(rgbArray2);
		
		out.write(generateTwoFrames(rgbArray1, rgbArray2, palette1, palette2));
	}
	
	private static ArrayList<Integer> getRgbTile(int[] frame, int tileNum)
	{
		ArrayList<Integer> retval = new ArrayList<Integer>();
		int x = tileNum % 32;
		int y = tileNum / 32;
		x *= 8;
		y *= 8;
		
		for (int i = y; i < y + 8 && i < 240; ++i)
		{
			for (int j = x; j < x + 8; ++j)
			{
				retval.add(frame[j + i * 256]);
			}
		}
		
		return retval;
	}
	
	private static int[] reduceColors(int[] rgbArray)
	{
		//The screen can only have 13 unique colors
		//Reduce it now so that we have a smaller search space later
		HashMap<Integer, Integer> colorsToCounts = new HashMap<Integer, Integer>();
		HashMap<Integer, ArrayList<Integer>> colorsToPositions = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < rgbArray.length; ++i)
		{
			int color = rgbArray[i];
			if (colorsToCounts.containsKey(color))
			{
				colorsToCounts.put(color, colorsToCounts.get(color) + 1);
				colorsToPositions.get(color).add(i);
			}
			else
			{
				colorsToCounts.put(color, 1);
				ArrayList<Integer> positions = new ArrayList<Integer>();
				positions.add(i);
				colorsToPositions.put(color, positions);
			}
		}
		
		while (colorsToCounts.size() > quality)
		{
			long minDistance = Long.MAX_VALUE;
			int key1 = 0;
			int key2 = 0;
			for (int color1 : colorsToCounts.keySet())
			{
				for (int color2 : colorsToCounts.keySet())
				{
					if (color1 != color2)
					{
						long distance = computeDistance(color1, color2);
						if (distance < minDistance)
						{
							minDistance = distance;
							key1 = color1;
							key2 = color2;
						}
					}
				}
			}
			
			int count1 = colorsToCounts.get(key1);
			int count2 = colorsToCounts.get(key2);
			if (count1 < count2)
			{
				//Replace all key1's with key2's
				ArrayList<Integer> positions = colorsToPositions.get(key1);
				int sourcePosition = colorsToPositions.get(key2).get(0);
				for (int destPosition : positions)
				{
					rgbArray[destPosition] = rgbArray[sourcePosition];
				}
				
				colorsToPositions.remove(key1);
				for (int position : positions)
				{
					colorsToPositions.get(key2).add(position);
				}
				
				int currentCount = colorsToCounts.get(key2);
				int newCount = currentCount + colorsToCounts.get(key1);
				colorsToCounts.remove(key1);
				colorsToCounts.put(key2, newCount);
			}
			else
			{
				//Replace all key2's with key1's
				ArrayList<Integer> positions = colorsToPositions.get(key2);
				int sourcePosition = colorsToPositions.get(key1).get(0);
				for (int destPosition : positions)
				{
					rgbArray[destPosition] = rgbArray[sourcePosition];
				}
				
				colorsToPositions.remove(key2);
				for (int position : positions)
				{
					colorsToPositions.get(key1).add(position);
				}
				
				int currentCount = colorsToCounts.get(key1);
				int newCount = currentCount + colorsToCounts.get(key2);
				colorsToCounts.remove(key2);
				colorsToCounts.put(key1, newCount);
			}
		}
		
		return rgbArray;
	}
	
	private static int[] convertToTiles(int[] rgbArray)
	{
		//The screen can only have 255 unique 8x8 tiles when there are 960 of them
		//so we need to make ones that are close, the same
		//Get unique tiles and counts
		HashMap<ArrayList<Integer>, Integer> tilesToCounts = new HashMap<ArrayList<Integer>, Integer>();
		HashMap<ArrayList<Integer>, ArrayList<Integer>> tilesToPositions = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
		for (int i = 0; i < 960; ++i)
		{
			ArrayList<Integer> rgbTile = getRgbTile(rgbArray, i);
			if (tilesToCounts.containsKey(rgbTile))
			{
				tilesToCounts.put(rgbTile, tilesToCounts.get(rgbTile) + 1);
				tilesToPositions.get(rgbTile).add(i);
			}
			else
			{
				tilesToCounts.put(rgbTile, 1);
				ArrayList<Integer> positions = new ArrayList<Integer>();
				positions.add(i);
				tilesToPositions.put(rgbTile, positions);
			}
		}
		
		while (tilesToCounts.size() > 255)
		{
			long minDistance = Long.MAX_VALUE;
			ArrayList<Integer> key1 = null;
			ArrayList<Integer> key2 = null;
			for (ArrayList<Integer> rgbTile1 : tilesToCounts.keySet())
			{
				for (ArrayList<Integer> rgbTile2 : tilesToCounts.keySet())
				{
					if (!rgbTile1.equals(rgbTile2))
					{
						long distance = tileDifference(rgbTile1, rgbTile2);
						if (distance < minDistance)
						{
							minDistance = distance;
							key1 = rgbTile1;
							key2 = rgbTile2;
						}
					}
				}
			}
			
			int count1 = tilesToCounts.get(key1);
			int count2 = tilesToCounts.get(key2);
			if (count1 < count2)
			{
				//Replace all key1's with key2's
				ArrayList<Integer> positions = tilesToPositions.get(key1);
				int sourcePosition = tilesToPositions.get(key2).get(0);
				for (int destPosition : positions)
				{
					replaceTile(destPosition, sourcePosition, rgbArray);
				}
				
				tilesToPositions.remove(key1);
				for (int position : positions)
				{
					tilesToPositions.get(key2).add(position);
				}
				
				int currentCount = tilesToCounts.get(key2);
				int newCount = currentCount + tilesToCounts.get(key1);
				tilesToCounts.remove(key1);
				tilesToCounts.put(key2, newCount);
			}
			else
			{
				//Replace all key2's with key1's
				ArrayList<Integer> positions = tilesToPositions.get(key2);
				int sourcePosition = tilesToPositions.get(key1).get(0);
				for (int destPosition : positions)
				{
					replaceTile(destPosition, sourcePosition, rgbArray);
				}
				
				tilesToPositions.remove(key2);
				for (int position : positions)
				{
					tilesToPositions.get(key1).add(position);
				}
				
				int currentCount = tilesToCounts.get(key1);
				int newCount = currentCount + tilesToCounts.get(key2);
				tilesToCounts.remove(key2);
				tilesToCounts.put(key1, newCount);
			}
		}
		
		return rgbArray;
	}
	
	private static void replaceTile(int t1, int t2, int[] rgbArray)
	{
		int x1 = (t1 * 8) % 256;
		int y1 = (t1 / 32) * 8;
		int x2 = (t2 * 8) % 256;
		int y2 = (t2 / 32) * 8;
		
		for (int i = x1; i < x1 + 8; ++i)
		{
			int x = x2 + (i - x1);
			for (int j = y1; j < y1 + 8; ++j)
			{
				int y = y2 + (j - y1);
				rgbArray[i + j * 256] = rgbArray[x + y * 256];
			}
		}
	}
	
	private static long tileDifference(ArrayList<Integer> tile1, ArrayList<Integer> tile2)
	{
		long retval = 0;
		for (int i = 0; i < tile1.size(); ++i)
		{
			retval += computeDistance(tile1.get(i), tile2.get(i));
		}

		return retval;
	}
	
	private static byte[] generateTwoFrames(int[] rgb1, int[] rgb2, int[] p1, int[] p2) throws IOException
	{
		byte[] retval = new byte[4096*3];
		int attributeOffset = 0x2000 + 32 * 30;
		int paletteOffset = 0x2800;
		HashMap<ArrayList<Byte>, Byte> tiles = new HashMap<ArrayList<Byte>, Byte>();
		
		for (int superTileY = 0; superTileY < 8; ++superTileY)
		{
			for (int superTileX = 0; superTileX < 8; ++superTileX)
			{
				byte attributeByte = generateAttributeByte(superTileX, superTileY, rgb1, p1);
				retval[attributeOffset++] = attributeByte;
				
				for (int subTileY = 0; subTileY < 2; ++subTileY)
				{
					for (int subTileX = 0; subTileX < 2; ++ subTileX)
					{
						int[] myColors = getSubTileColors(subTileX, subTileY, attributeByte, p1);
						int paletteNum = getSubTilePaletteNum(subTileX, subTileY, attributeByte, p1);
						for (int subSubTileY = 0; subSubTileY < 2; ++ subSubTileY)
						{
							for (int subSubTileX = 0; subSubTileX < 2; ++ subSubTileX)
							{
								ArrayList<Byte> tile = generateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb1, myColors, paletteNum);
								
								if (tile.size() == 16)
								{
									if (tiles.containsKey(tile))
									{
										retval[generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
									else
									{
										if (tiles.size() == 256)
										{
											tile = getAlternateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb1, myColors, paletteNum, tiles);
										}
										else
										{
											tiles.put(tile, (byte)tiles.size());
										}
										
										retval[generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
								}
							}
						}
					}
				}
			}
		}
		
		writeTileData(retval, 0, tiles);
		writePaletteData(retval, paletteOffset, p1);
		
		compareRgbArray(rgb1, 0, retval);
		
		attributeOffset = 0x2400 + 32 * 30;
		paletteOffset = 0x2810;
		tiles.clear();
		
		for (int superTileY = 0; superTileY < 8; ++superTileY)
		{
			for (int superTileX = 0; superTileX < 8; ++superTileX)
			{
				byte attributeByte = generateAttributeByte(superTileX, superTileY, rgb2, p2);
				retval[attributeOffset++] = attributeByte;
				
				for (int subTileY = 0; subTileY < 2; ++subTileY)
				{
					for (int subTileX = 0; subTileX < 2; ++ subTileX)
					{
						int[] myColors = getSubTileColors(subTileX, subTileY, attributeByte, p2);
						int paletteNum = getSubTilePaletteNum(subTileX, subTileY, attributeByte, p2);
						for (int subSubTileY = 0; subSubTileY < 2; ++ subSubTileY)
						{
							for (int subSubTileX = 0; subSubTileX < 2; ++ subSubTileX)
							{
								ArrayList<Byte> tile = generateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb2, myColors, paletteNum);
								
								if (tile.size() == 16)
								{
									if (tiles.containsKey(tile))
									{
										retval[0x400 + generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
									else
									{
										if (tiles.size() == 256)
										{
											tile = getAlternateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb2, myColors, paletteNum, tiles);
										}
										else
										{
											tiles.put(tile, (byte)tiles.size());
										}
										
										retval[0x400 + generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
								}
							}
						}
					}
				}
			}
		}
		
		writeTileData(retval, 1, tiles);
		writePaletteData(retval, paletteOffset, p2);
		
		compareRgbArray(rgb2, 1, retval);
		/*
		BufferedImage out1 = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
		BufferedImage out2 = new BufferedImage(256, 240, BufferedImage.TYPE_INT_RGB);
		out1.getRaster().setDataElements(0, 0, 256, 240, rgb1);
		out2.getRaster().setDataElements(0, 0, 256, 240, rgb2);
		String filename = System.getProperty("user.dir") + "/" + String.format("%04d", frameNumber++) + ".png";
		ImageIO.write(out1, "png", new File(filename));
		filename = System.getProperty("user.dir") + "/" + String.format("%04d", frameNumber++) + ".png";
		ImageIO.write(out2, "png", new File(filename));
		*/
		
		return retval;
	}
	
	private static void compareRgbArray(int[] frame, int frameNum, byte[] bank)
	{
		//DEBUG
		byte[] palette = new byte[16];
		if (frameNum == 0)
		{
			System.arraycopy(bank, 0x2800, palette, 0, 16);
		}
		else
		{
			System.arraycopy(bank, 0x2810, palette, 0, 16);
		}
		
		int base = 0x2000;
		if (frameNum == 1)
		{
			base += 0x400;
		}
		
		for (int tileY = 0; tileY < 30; ++tileY)
		{
			for (int tileX = 0; tileX < 32; ++tileX)
			{
				//Get nametable byte
				byte nametable = bank[base + tileX + 32 * tileY];
				
				//Get attribute byte
				int attributeX = tileX / 4;
				int attributeY = tileY / 4;
				int attribute = bank[base + 960 + attributeX + attributeY * 8];
				
				int shift = 0;
				if (tileX % 4 < 2)
				{
					if (tileY % 4 < 2)
					{
						shift = 0;
					}
					else
					{
						shift = 4;
					}
				}
				else
				{
					if (tileY % 4 < 2) 
					{
						shift = 2; 
					}
					else
					{
						shift = 6;
					}
				}
				
				int paletteNum = ((attribute >> shift) & 0x03);
				
				for (int i = 0; i < 8; ++i)
				{
					for (int j = 0; j < 8; ++j)
					{
						//Get pattern data for pixel
						int pattern = 0;
						int pattern2 = 0;
						int patternBase = 0;
						if (frameNum == 1)
						{
							patternBase = 0x1000;
						}
						
						pattern = bank[patternBase + 16 * Byte.toUnsignedInt(nametable) + i];
						pattern2 = bank[patternBase + 16 * Byte.toUnsignedInt(nametable) + i + 8];
						pattern = ((pattern >> (7 - j)) & 0x01);
						pattern += (((pattern2 >> (7 -j)) & 0x01) << 1);
						
						int color = 0;
						if (pattern == 0)
						{
							color = 0;
						}
						else
						{
							color = 4 * paletteNum + pattern;
						}
						
						int x = tileX * 8 + j;
						int y = tileY * 8 + i;
						if ( y < 240)
						{
							if (frame[x + 256 * y] != colorLookup[palette[color]])
							{
								System.out.println("At (" + x + "," + y + ") expected color " + findColorInPalette(frame[x + 256 * y], palette) + " but had color " + color);
								System.out.println("Debug table says " + debugTable.get(x + 256 * y));
							}
						}
					}
				}
			}
		}
	}
	
	private static int findColorInPalette(int color, byte[] palette)
	{
		int globalIndex = reverseLookup.get(color);
		for (int i = 0; i < 16; ++i)
		{
			if (palette[i] == globalIndex)
			{
				return i;
			}
		}
		
		return -1;
	}
	
	private static byte[] generateOneFrame(int[] rgb1, int[] p1)
	{
		byte[] retval = new byte[4096*3];
		int attributeOffset = 8192 + 960;
		int paletteOffset = 0x2800;
		HashMap<ArrayList<Byte>, Byte> tiles = new HashMap<ArrayList<Byte>, Byte>();
		
		for (int superTileY = 0; superTileY < 8; ++superTileY)
		{
			for (int superTileX = 0; superTileX < 8; ++superTileX)
			{
				byte attributeByte = generateAttributeByte(superTileX, superTileY, rgb1, p1);
				retval[attributeOffset++] = attributeByte;
				
				for (int subTileY = 0; subTileY < 2; ++subTileY)
				{
					for (int subTileX = 0; subTileX < 2; ++ subTileX)
					{
						int[] myColors = getSubTileColors(subTileX, subTileY, attributeByte, p1);
						int paletteNum = getSubTilePaletteNum(subTileX, subTileY, attributeByte, p1);
						for (int subSubTileY = 0; subSubTileY < 2; ++ subSubTileY)
						{
							for (int subSubTileX = 0; subSubTileX < 2; ++ subSubTileX)
							{
								ArrayList<Byte> tile = generateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb1, myColors, paletteNum);
								
								if (tile.size() == 16)
								{
									if (tiles.containsKey(tile))
									{
										retval[generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
									else
									{
										if (tiles.size() == 256)
										{
											tile = getAlternateTile(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY, rgb1, myColors, paletteNum, tiles);
										}
										else
										{
											tiles.put(tile, (byte)tiles.size());
										}
										
										retval[generateNametableAddress(superTileX, superTileY, subTileX, subTileY, subSubTileX, subSubTileY)] = tiles.get(tile);
									}
								}
							}
						}
					}
				}
			}
		}
		
		writeTileData(retval, 0, tiles);
		writePaletteData(retval, paletteOffset, p1);
		
		return retval;
	}
	
	private static void writePaletteData(byte[] retval, int offset, int[] palette)
	{
	
		retval[offset] = reverseLookup.get(palette[0]);
		
		retval[offset + 1] = reverseLookup.get(palette[1]);
		retval[offset + 2] = reverseLookup.get(palette[2]);
		retval[offset + 3] = reverseLookup.get(palette[3]);
		
		retval[offset + 5] = reverseLookup.get(palette[4]);
		retval[offset + 6] = reverseLookup.get(palette[5]);
		retval[offset + 7] = reverseLookup.get(palette[6]);
		
		retval[offset + 9] = reverseLookup.get(palette[7]);
		retval[offset + 10] = reverseLookup.get(palette[8]);
		retval[offset + 11] = reverseLookup.get(palette[9]);
		
		retval[offset + 13] = reverseLookup.get(palette[10]);
		retval[offset + 14] = reverseLookup.get(palette[11]);
		retval[offset + 15] = reverseLookup.get(palette[12]);
	}
	
	private static ArrayList<Byte> generateTile(int superX, int superY, int tileX, int tileY, int subX, int subY, int[] frame, int[] myColors, int paletteNum)
	{
		ArrayList<Byte> retval = new ArrayList<Byte>();
		
		for (int i = 0; i < 16; ++i)
		{
			retval.add((byte)0);
		}
		
		int x = 4 * superX;
		int y = 4 * superY;
		x += 2 * tileX;
		y += 2 * tileY;
		x += subX;
		y += subY;
		
		x *= 8;
		y *= 8;
		
		if (y >= 240)
		{
			return new ArrayList<Byte>();
		}
		
		for (int i = y; i < y + 8 && i < 240; ++i)
		{
			int byte1 = 0;
			int byte2 = 0;
			for (int j = x; j < x + 8; ++j)
			{
				byte1 <<= 1;
				byte2 <<= 1;
				int color = getClosestColorIndex(frame[j + 256 * i], myColors);
				if ((color & 0x01) != 0)
				{
					byte1 += 1;
				}
				
				if ((color & 0x02) != 0)
				{
					byte2 += 1;
				}
				
				frame[j + 256 * i] = myColors[color];
				
				//DEBUG
				if (color == 0)
				{
					paletteNum = 0;
				}
				debugTable.put(j + 256 * i, color + 4 * paletteNum);
			}
			
			retval.set(i - y, (byte)byte1);
			retval.set(i - y + 8, (byte)byte2);
		}
		
		return retval;
	}
	
	private static ArrayList<Byte> getAlternateTile(int superX, int superY, int tileX, int tileY, int subX, int subY, int[] frame, int[] myColors, int paletteNum, HashMap<ArrayList<Byte>, Byte> tiles)
	{
		int x = 4 * superX;
		int y = 4 * superY;
		x += 2 * tileX;
		y += 2 * tileY;
		x += subX;
		y += subY;
		
		x *= 8;
		y *= 8;
		
		if (y >= 240)
		{
			return new ArrayList<Byte>();
		}
		
		long minDistance = Long.MAX_VALUE;
		ArrayList<Byte> minTile = null;
		
		for (ArrayList<Byte> tile : tiles.keySet())
		{
			long distance = 0;
			for (int i = y; i < y + 8 && i < 240; ++i)
			{
				for (int j = x; j < x + 8; ++j)
				{
					byte pattern1 = tile.get(i - y);
					byte pattern2 = tile.get(i - y + 8);
					pattern1 >>= (7 - (j - x));
					pattern1 &= 0x01;
					pattern1 += (((pattern2 >> (7 - (j - x))) & 0x01) << 1);
					distance += computeDistance(frame[j + 256 * i], myColors[pattern1]);
				}
			}
			
			if (distance < minDistance)
			{
				minDistance = distance;
				minTile = tile;
			}
		}
		
		for (int i = y; i < y + 8 && i < 240; ++i)
		{
			for (int j = x; j < x + 8; ++j)
			{
				byte pattern1 = minTile.get(i - y);
				byte pattern2 = minTile.get(i - y + 8);
				pattern1 >>= (7 - (j - x));
				pattern1 &= 0x01;
				pattern1 += (((pattern2 >> (7 - (j - x))) & 0x01) << 1);
				
				frame[j + 256 * i] = myColors[pattern1];
				
				//DEBUG
				if (pattern1 == 0)
				{
					paletteNum = 0;
				}
				debugTable.put(j + 256 * i, pattern1 + 4 * paletteNum);
			}
		}
		
		return minTile;
	}
	
	private static int getClosestColorIndex(int color, int[] choices)
	{
		long minDistance = Long.MAX_VALUE;
		int minColor = 0;
		for (int choice : choices)
		{
			long distance = computeDistance(color, choice);
			if (distance < minDistance)
			{
				minDistance = distance;
				minColor = choice;
			}
		}
		
		for (int i = 0; i < choices.length; ++i)
		{
			if (minColor == choices[i])
			{
				return i;
			}
		}
		
		return 0;
	}
	
	private static byte generateAttributeByte(int superTileX, int superTileY, int [] frame, int[] palette)
	{
		long loss1 = 0;
		long loss2 = 0;
		long loss3 = 0;
		long loss4 = 0;
		
		int[] c1 = new int[4];
		int[] c2 = new int[4];
		int[] c3 = new int[4];
		int[] c4 = new int[4];
		c1[0] = palette[0];
		c1[1] = palette[1];
		c1[2] = palette[2];
		c1[3] = palette[3];
		c2[0] = palette[0];
		c2[1] = palette[4];
		c2[2] = palette[5];
		c2[3] = palette[6];
		c3[0] = palette[0];
		c3[1] = palette[7];
		c3[2] = palette[8];
		c3[3] = palette[9];
		c4[0] = palette[0];
		c4[1] = palette[10];
		c4[2] = palette[11];
		c4[3] = palette[12]; 
		
		int x = 32 * superTileX;
		int y = 32 * superTileY;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				if (y + j >= 240)
				{
					break;
				}
				
				loss1 += computeDistance(frame[x + i + (y + j) * 256], c1);
				loss2 += computeDistance(frame[x + i + (y + j) * 256], c2);
				loss3 += computeDistance(frame[x + i + (y + j) * 256], c3);
				loss4 += computeDistance(frame[x + i + (y + j) * 256], c4);
			}
		}
		
		int retval = 0;
		retval += getBest(loss1, loss2, loss3, loss4);
		
		x += 16;
		loss1 = 0;
		loss2 = 0;
		loss3 = 0;
		loss4 = 0;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				if (y + j >= 240)
				{
					break;
				}
				
				loss1 += computeDistance(frame[x + i + (y + j) * 256], c1);
				loss2 += computeDistance(frame[x + i + (y + j) * 256], c2);
				loss3 += computeDistance(frame[x + i + (y + j) * 256], c3);
				loss4 += computeDistance(frame[x + i + (y + j) * 256], c4);
			}
		}
		
		retval += (getBest(loss1, loss2, loss3, loss4) << 2);
		
		x -= 16;
		y += 16;
		loss1 = 0;
		loss2 = 0;
		loss3 = 0;
		loss4 = 0;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				if (y + j >= 240)
				{
					break;
				}
				
				loss1 += computeDistance(frame[x + i + (y + j) * 256], c1);
				loss2 += computeDistance(frame[x + i + (y + j) * 256], c2);
				loss3 += computeDistance(frame[x + i + (y + j) * 256], c3);
				loss4 += computeDistance(frame[x + i + (y + j) * 256], c4);
			}
		}
		
		retval += (getBest(loss1, loss2, loss3, loss4) << 4);
		
		x += 16;
		loss1 = 0;
		loss2 = 0;
		loss3 = 0;
		loss4 = 0;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				if (y + j >= 240)
				{
					break;
				}
				
				loss1 += computeDistance(frame[x + i + (y + j) * 256], c1);
				loss2 += computeDistance(frame[x + i + (y + j) * 256], c2);
				loss3 += computeDistance(frame[x + i + (y + j) * 256], c3);
				loss4 += computeDistance(frame[x + i + (y + j) * 256], c4);
			}
		}
		
		retval += (getBest(loss1, loss2, loss3, loss4) << 6);
		return (byte)retval;
	}
	
	private static int getBest(long l1, long l2, long l3, long l4)
	{
		long min = Math.min(Math.min(Math.min(l1, l2), l3), l4);
		if (min == l1)
		{
			return 0;
		}
		
		if (min == l2)
		{
			return 1;
		}
		
		if (min == l3)
		{
			return 2;
		}
		
		return 3;
	}
	
	private static int[] getSubTileColors(int subTileX, int subTileY, byte attributeByte, int[] palette)
	{
		int pNum = 0;
		int[] retval = new int[4];
		retval[0] = palette[0];
		
		if (subTileX == 0)
		{
			if (subTileY == 0)
			{
				pNum = (0x03 & Byte.toUnsignedInt(attributeByte));
			}
			else
			{
				pNum = (0x03 & (Byte.toUnsignedInt(attributeByte) >> 4));
			}
		}
		else
		{
			if (subTileY == 0)
			{
				pNum = (0x03 & Byte.toUnsignedInt(attributeByte) >> 2);
			}
			else
			{
				pNum = (0x03 & (Byte.toUnsignedInt(attributeByte) >> 6));
			}
		}
		
		retval[1] = palette[3 * pNum + 1];
		retval[2] = palette[3 * pNum + 2];
		retval[3] = palette[3 * pNum + 3];
		
		return retval;
	}
	
	//DEBUG
	private static int getSubTilePaletteNum(int subTileX, int subTileY, byte attributeByte, int[] palette)
	{
		int pNum = 0;
		int[] retval = new int[4];
		retval[0] = palette[0];
		
		if (subTileX == 0)
		{
			if (subTileY == 0)
			{
				pNum = (0x03 & Byte.toUnsignedInt(attributeByte));
			}
			else
			{
				pNum = (0x03 & (Byte.toUnsignedInt(attributeByte) >> 4));
			}
		}
		else
		{
			if (subTileY == 0)
			{
				pNum = (0x03 & Byte.toUnsignedInt(attributeByte) >> 2);
			}
			else
			{
				pNum = (0x03 & (Byte.toUnsignedInt(attributeByte) >> 6));
			}
		}
		
		return pNum;
	}
	
	private static int generateNametableAddress(int superX, int superY, int tileX, int tileY, int subX, int subY)
	{
		int x = 4 * superX;
		int y = 4 * superY;
		x += 2 * tileX;
		y += 2 * tileY;
		x += subX;
		y += subY;
		
		return 0x2000 + x + 32 * y;
	}
	
	private static void writeTileData(byte[] retval, int bank, HashMap<ArrayList<Byte>, Byte> tiles)
	{
		for (Map.Entry<ArrayList<Byte>, Byte> entry : tiles.entrySet())
		{
			int offset = bank * 0x1000 + Byte.toUnsignedInt(entry.getValue()) * 16;
			for (Byte b : entry.getKey())
			{
				retval[offset++] = b;
			}
		}
	}
	
	private static void writeLastFrame(FileOutputStream out) throws IOException
	{
		BufferedImage frame1 = ImageIO.read(new File("frame" + String.format("%06d", numFrames) + ".png"));
		int[] rgbArray1 = getCroppedRgbArray(frame1);
		int[] palette1 = getPalette(rgbArray1);
		out.write(generateOneFrame(rgbArray1, palette1));
	}
	
	private static int[] getPalette(int[] frame)
	{
		int[] retval = new int[13];
		//Get most common color and make that background
		int bgColor = mostCommonColor(frame);
		retval[0] = bgColor;
		
		//Then find the 3 colors that minimize the loss with the bg
		int[] next3 = getTop3Colors(frame, bgColor);
		retval[1] = next3[0];
		retval[2] = next3[1];
		retval[3] = next3[2];
		
		//Then go 16x16 and find the minimization of choosing existing or 3 new colors
		int[] availableColors = getUsedColors(frame, bgColor);
		int[] choice1 = new int[4];
		choice1[0] = bgColor;
		choice1[1] = next3[0];
		choice1[2] = next3[1];
		choice1[3] = next3[2];
		next3 = getNext3Colors(frame, choice1, bgColor, availableColors);
		retval[4] = next3[0];
		retval[5] = next3[1];
		retval[6] = next3[2];
		
		int[] choice2 = new int[4];
		choice2[0] = bgColor;
		choice2[1] = next3[0];
		choice2[2] = next3[1];
		choice2[3] = next3[2];
		next3 = getNext3Colors(frame, choice1, choice2, bgColor, availableColors);
		retval[7] = next3[0];
		retval[8] = next3[1];
		retval[9] = next3[2];
		
		int[] choice3 = new int[4];
		choice3[0] = bgColor;
		choice3[1] = next3[0];
		choice3[2] = next3[1];
		choice3[3] = next3[2];
		next3 = getNext3Colors(frame, choice1, choice2, choice3, bgColor, availableColors);
		retval[10] = next3[0];
		retval[11] = next3[1];
		retval[12] = next3[2];
		
		return retval;
	}
	
	private static int[] getUsedColors(int[] frame, int bgColor)
	{
		HashSet<Integer> used = new HashSet<Integer>();
		for (int color : frame)
		{
			if (color != bgColor)
			{
				used.add(color);
			}
		}
		
		int[] retval = new int[used.size()];
		int i = 0;
		for (int color : used)
		{
			retval[i++] = color;
		}
		
		return retval;
	}
	
	private static ArrayList<int[]> generatePaletteChoices(int bgColor, int[] availableColors)
	{
		ArrayList<int[]> retval = new ArrayList<int[]>();
		for (int i = 0; i < availableColors.length - 2; ++i)
		{
			for (int j = i + 1; j < availableColors.length - 1; ++j)
			{
				for (int k = j + 1; k < availableColors.length; ++k)
				{
					int[] choice = new int[4];
					choice[0] = availableColors[i];
					choice[1] = availableColors[j];
					choice[2] = availableColors[k];
					choice[3] = bgColor;
					retval.add(choice);
				}
			}
		}
		
		return retval;
	}
	
	private static int[] getNext3Colors(int[] frame, int[] palette1, int bgColor, int[] availableColors)
	{
		ArrayList<int[]> choices = generatePaletteChoices(bgColor, availableColors);
		long minLoss = Long.MAX_VALUE;
		int[] minChoice = new int[4];
		minChoice[0] = bgColor;
		minChoice[1] = bgColor;
		minChoice[2] = bgColor;
		minChoice[3] = bgColor;
		
		for (int[] choice : choices)
		{
			long loss = 0;
			for (int i = 0; i < 16; ++i)
			{
				for (int j = 0; j < 15; ++j)
				{
					loss += minLossForTile(i, j, palette1, choice, frame);
				}
			}
			
			if (loss < minLoss)
			{
				minLoss = loss;
				minChoice = choice;
			}
		}
		
		return minChoice;
	}
	
	private static int[] getNext3Colors(int[] frame, int[] palette1, int[] palette2, int bgColor, int[] availableColors)
	{
		ArrayList<int[]> choices = generatePaletteChoices(bgColor, availableColors);
		long minLoss = Long.MAX_VALUE;
		int[] minChoice = new int[4];
		minChoice[0] = bgColor;
		minChoice[1] = bgColor;
		minChoice[2] = bgColor;
		minChoice[3] = bgColor;
		
		for (int[] choice : choices)
		{
			long loss = 0;
			for (int i = 0; i < 16; ++i)
			{
				for (int j = 0; j < 15; ++j)
				{
					loss += minLossForTile(i, j, palette1, palette2, choice, frame);
				}
			}
			
			if (loss < minLoss)
			{
				minLoss = loss;
				minChoice = choice;
			}
		}
		
		return minChoice;
	}
	
	private static int[] getNext3Colors(int[] frame, int[] palette1, int[] palette2, int[] palette3, int bgColor, int[] availableColors)
	{
		ArrayList<int[]> choices = generatePaletteChoices(bgColor, availableColors);
		long minLoss = Long.MAX_VALUE;
		int[] minChoice = new int[4];
		minChoice[0] = bgColor;
		minChoice[1] = bgColor;
		minChoice[2] = bgColor;
		minChoice[3] = bgColor;
		
		for (int[] choice : choices)
		{
			long loss = 0;
			for (int i = 0; i < 16; ++i)
			{
				for (int j = 0; j < 15; ++j)
				{
					loss += minLossForTile(i, j, palette1, palette2, palette3, choice, frame);
				}
			}
			
			if (loss < minLoss)
			{
				minLoss = loss;
				minChoice = choice;
			}
		}
		
		return minChoice;
	}
	
	private static long minLossForTile(int x, int y, int[] c1, int[] c2, int[] frame)
	{
		long loss1 = 0;
		long loss2 = 0;
		x = 16 * x;
		y = 16 * y;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{	
				int color = frame[x + i + (y + j) * 256];
				loss1 += computeDistance(color, c1);
				loss2 += computeDistance(color, c2);
			}
		}
		
		return Math.min(loss1, loss2);
	}
	
	private static long minLossForTile(int x, int y, int[] c1, int[] c2, int[] c3, int[] frame)
	{
		long loss1 = 0;
		long loss2 = 0;
		long loss3 = 0;
		x = 16 * x;
		y = 16 * y;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				int color = frame[x + i + (y + j) * 256];
				loss1 += computeDistance(color, c1);
				loss2 += computeDistance(color, c2);
				loss3 += computeDistance(color, c3);
			}
		}
		
		return Math.min(Math.min(loss1, loss2), loss3);
	}
	
	private static long minLossForTile(int x, int y, int[] c1, int[] c2, int[] c3, int[] c4, int[] frame)
	{
		long loss1 = 0;
		long loss2 = 0;
		long loss3 = 0;
		long loss4 = 0;
		x = 16 * x;
		y = 16 * y;
		for (int i = 0; i < 16; ++i)
		{
			for (int j = 0; j < 16; ++j)
			{
				int color = frame[x + i + (y + j) * 256];
				loss1 += computeDistance(color, c1);
				loss2 += computeDistance(color, c2);
				loss3 += computeDistance(color, c3);
				loss4 += computeDistance(color, c4);
			}
		}
		
		return Math.min(Math.min(Math.min(loss1, loss2), loss3), loss4);
	}
	
	private static int[] getTop3Colors(int[] frame, int bgColor)
	{
		int[] retval = new int[3];
		retval[0] = bgColor;
		retval[1] = bgColor;
		retval[2] = bgColor;
		
		HashMap<Integer, Integer> colorToCount = new HashMap<Integer, Integer>();
		for (int color : frame)
		{
			if (color != bgColor)
			{
				if (!colorToCount.containsKey(color))
				{
					colorToCount.put(color, 1);
				}
				else
				{
					colorToCount.put(color, colorToCount.get(color) + 1);
				}
			}
		}
		
		int maxCount = 0;
		int maxColor = 0;
		for (Map.Entry<Integer, Integer> entry : colorToCount.entrySet())
		{
			if (entry.getValue() > maxCount)
			{
				maxCount = entry.getValue();
				maxColor = entry.getKey();
			}
		}
		
		if (maxCount > 0)
		{
			retval[0] = maxColor;
			colorToCount.remove(maxColor);
		}
		
		maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : colorToCount.entrySet())
		{
			if (entry.getValue() > maxCount)
			{
				maxCount = entry.getValue();
				maxColor = entry.getKey();
			}
		}
		
		if (maxCount > 0)
		{
			retval[1] = maxColor;
			colorToCount.remove(maxColor);
		}
		
		maxCount = 0;
		for (Map.Entry<Integer, Integer> entry : colorToCount.entrySet())
		{
			if (entry.getValue() > maxCount)
			{
				maxCount = entry.getValue();
				maxColor = entry.getKey();
			}
		}
		
		if (maxCount > 0)
		{
			retval[2] = maxColor;
			colorToCount.remove(maxColor);
		}
		
		return retval;
	}
	
	private static int mostCommonColor(int[] frame)
	{
		HashMap<Integer, Integer> colorToCount = new HashMap<Integer, Integer>();
		for (int color : frame)
		{
			if (!colorToCount.containsKey(color))
			{
				colorToCount.put(color, 1);
			}
			else
			{
				colorToCount.put(color, colorToCount.get(color) + 1);
			}
		}
		
		int maxCount = 0;
		int maxColor = 0;
		for (Map.Entry<Integer, Integer> entry : colorToCount.entrySet())
		{
			if (entry.getValue() > maxCount)
			{
				maxCount = entry.getValue();
				maxColor = entry.getKey();
			}
		}
		
		return maxColor;
	}
	
	private static int[] getCroppedRgbArray(BufferedImage frame)
	{
		int[] retval = new int[256*240];
		int i = 0;
		for (int y = 0; y < 240; ++y)
		{
			for (int x = 12; x < 268; ++x)
			{
				retval[i] = (frame.getRGB(x, y) & 0xffffff);
				retval[i] = convertToClosest(retval[i]);
				++i;
			}
		}
		
		return retval;
	}
	
	private static int convertToClosest(int color)
	{
		long distance = Long.MAX_VALUE;
		int minColor = 0;
		for (int i = 0; i < 64; ++i)
		{
			long temp = computeDistance(color, colorLookup[i]);
			if (temp < distance)
			{
				distance = temp;
				minColor = colorLookup[i];
			}
		}
		
		return minColor;
	}
	
	private static long computeDistance(int color1, int color2)
	{
		int r1 = (color1 >> 16);
		int g1 = ((color1 >> 8) & 0xff);
		int b1 = (color1 & 0xff);
		int r2 = (color2 >> 16);
		int g2 = ((color2 >> 8) & 0xff);
		int b2 = (color2 & 0xff);
		
		return (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
	}
	
	private static long computeDistance(int color1, int[] choices)
	{
		long minDistance = Long.MAX_VALUE;
		for (int choice : choices)
		{
			long distance = computeDistance(color1, choice);
			if (distance < minDistance)
			{
				minDistance = distance;
			}
		}
		
		return minDistance;
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
				audioBanks[position++] = (byte)0xa0; //NMI vector = 0xa000
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
				audioBanks[position++] = (byte)0xa0; //NMI vector = 0xa000
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
		
		//Set video bank 0 / frame 0
		codeBank[0x8044 - start] = (byte)0xa9; //LDA
		codeBank[0x8045 - start] = (byte)0x00; //immediate
		codeBank[0x8046 - start] = (byte)0x85; //STA
		codeBank[0x8047 - start] = (byte)0x06; //Zero page 
		codeBank[0x8048 - start] = (byte)0x85; //STA
		codeBank[0x8049 - start] = (byte)0x07; //Zero page 
		codeBank[0x804a - start] = (byte)0x85; //STA
		codeBank[0x804b - start] = (byte)0x08; //Zero page 
		
		//Disable video, enable NMI
		//Write 0 -> 0x2001
		//Write 0x80 -> 0x2000
		codeBank[0x804c - start] = (byte)0x8d; //STA
		codeBank[0x804d - start] = (byte)0x01; 
		codeBank[0x804e - start] = (byte)0x20; //Absolute
		codeBank[0x804f - start] = (byte)0xa9; //LDA
		codeBank[0x8050 - start] = (byte)0x80; //immediate
		codeBank[0x8051 - start] = (byte)0x8d; //STA
		codeBank[0x8052 - start] = (byte)0x00; 
		codeBank[0x8053 - start] = (byte)0x20; //Absolute
		
		//Endless loop
		codeBank[0x8054 - start] = (byte)0x4c; //JMP
		codeBank[0x8055 - start] = (byte)0x54; 
		codeBank[0x8056 - start] = (byte)0x80; //Absolute
		
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
		
		//NMI routine
		//Enable interrupts
		codeBank[0xa000 - start] = (byte)0x58; //CLI
		
		//Swap bank if needed
		codeBank[0xa001 - start] = (byte)0xa5; //LDA
		codeBank[0xa002 - start] = (byte)0x08; //zero page
		codeBank[0xa003 - start] = (byte)0xf0; //BEQ
		codeBank[0xa004 - start] = (byte)0x03; //relative
		codeBank[0xa005 - start] = (byte)0x4c; //JMP
		codeBank[0xa006 - start] = (byte)0x0b; 
		codeBank[0xa007 - start] = (byte)0xa0; //Absolute
		codeBank[0xa008 - start] = (byte)0x4c; //JMP
		codeBank[0xa009 - start] = (byte)0x00; 
		codeBank[0xa00a - start] = (byte)0xb0; //Absolute
		
		//Set palette data 
		//Palette data is at 0x2810
		int offset = 0xa00b;
		for (int i = 0; i < 16; ++i)
		{
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)0x28; //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)(0x10+i); //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xae; //LDX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xae; //LDX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)0x3f; //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)(0x00 + i); //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0x8e; //STX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
		}
			
		//Write 0x2000
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x91; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x00; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
		
		//Write 0x2001
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x1e; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x01; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
		
		//Write 0x2005
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x00; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x05; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute4
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x05; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
		
		//Set frame to zero and increment bank
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x00; //immediate
		codeBank[offset++ - start] = (byte)0x85; //STA
		codeBank[offset++ - start] = (byte)0x08; //zero page
		codeBank[offset++ - start] = (byte)0x38; //SEC
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x00; //immediate
		codeBank[offset++ - start] = (byte)0x65; //ADC
		codeBank[offset++ - start] = (byte)0x06; //zero page
		codeBank[offset++ - start] = (byte)0x85; //STA
		codeBank[offset++ - start] = (byte)0x06; //Zero page
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x00; //immediate
		codeBank[offset++ - start] = (byte)0x65; //ADC
		codeBank[offset++ - start] = (byte)0x07; //zero page
		codeBank[offset++ - start] = (byte)0x85; //STA
		codeBank[offset++ - start] = (byte)0x07; //Zero page
		
		//return
		codeBank[offset++ - start] = (byte)0x40; //RTI
		
		//Handle frame zero @ 0xb000
		//Load bank number from 06-07 and swap bank
		codeBank[0xb000 - start] = (byte)0xa5; //LDA
		codeBank[0xb001 - start] = (byte)0x06; //zero page
		codeBank[0xb002 - start] = (byte)0x8d; //STA
		codeBank[0xb003 - start] = (byte)0x00; 
		codeBank[0xb004 - start] = (byte)0xc0; //Absolute
		codeBank[0xb005 - start] = (byte)0xa5; //LDA
		codeBank[0xb006 - start] = (byte)0x07; //zero page
		codeBank[0xb007 - start] = (byte)0x8d; //STA
		codeBank[0xb008 - start] = (byte)0x00; 
		codeBank[0xb009 - start] = (byte)0xe0; //Absolute
		
		//Set palette data
		//Palette data is at 0x2800
		offset = 0xb00a;
		for (int i = 0; i < 16; ++i)
		{
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)0x28; //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)(0x00+i); //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xae; //LDX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xae; //LDX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)0x3f; //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0xa9; //LDA
			codeBank[offset++ - start] = (byte)(0x00 + i); //immediate
			codeBank[offset++ - start] = (byte)0x8d; //STA
			codeBank[offset++ - start] = (byte)0x06; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
			codeBank[offset++ - start] = (byte)0x8e; //STX
			codeBank[offset++ - start] = (byte)0x07; 
			codeBank[offset++ - start] = (byte)0x20; //Absolute
		}
		
		//Write 0x2000
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x80; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x00; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
		
		//Write 0x2001
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x1e; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x01; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
		
		//Write 0x2005
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x00; //immediate
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x05; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute4
		codeBank[offset++ - start] = (byte)0x8d; //STA
		codeBank[offset++ - start] = (byte)0x05; 
		codeBank[offset++ - start] = (byte)0x20; //Absolute
				
		//Set frame to 1
		codeBank[offset++ - start] = (byte)0xa9; //LDA
		codeBank[offset++ - start] = (byte)0x01; //immediate
		codeBank[offset++ - start] = (byte)0x85; //STA
		codeBank[offset++ - start] = (byte)0x08; //zero page
		
		//return
		codeBank[offset++ - start] = (byte)0x40; //RTI
		
		out.write(codeBank);
	}
}
