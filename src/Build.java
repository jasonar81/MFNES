import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Build 
{
	private static int livesAddr = 0; // = 3
	private static int levelAddr = 1; // = 1
	private static int paddleLeftXAddr = 2; // = 88
	private static int paddleRightXAddr = 3; // = 119
	private static int paddleTopYAddr = 4; // = 208
	private static int paddleBottomYAddr = 5; // = 215
	private static int ballXAddr1 = 6; // = 00
	private static int ballXAddr2 = 7; // = 103
	private static int ballYAddr1 = 8; // = 00
	private static int ballYAddr2 = 9; // = 205
	private static int blocksRemainingInLevelAddr = 10;
	private static int ballVectorX1Addr = 11; // = 0
	private static int ballVectorX2Addr = 12; // = 1
	private static int nmiFlagAddr = 13; // = 0
	private static int timerAddr = 14; // = 0
	private static int twoByteCounterLowAddr = 15; // = 0
	private static int twoByteCounterHighAddr = 16; // = 0
	private static int buttonsAddr = 17; // = 0
	private static int lineNumberAddr = 18; // = 0
	private static int scratchAddr = 19; // = 0
	private static int twoByte2Low = 20; // = 0
	private static int twoByte2High = 21; // = 0
	private static int ballVectorY1Addr = 22; // = 0
	private static int ballVectorY2Addr = 23; // = 1
	private static int ballSpeed1Addr = 24; // = 0 
	private static int ballSpeed2Addr = 25; // = 1 
	private static int ballFrozenAddr = 26; // = 1
	private static int ballFrozenTimerAddr = 27; // = 255
	private static int deathFlagAddr = 28;
	private static int powerupAddr = 29;
	private static int upDownFlagAddr = 30; // = 1
	private static int upDownFlag2Addr = 31; // = 1
	private static int ball2XAddr1 = 32; // = 0
	private static int ball2XAddr2 = 33; // = 0
	private static int ball2YAddr1 = 34; // = 0
	private static int ball2YAddr2 = 35; // = 0
	private static int ball2VectorX1Addr = 36; // = 0
	private static int ball2VectorX2Addr = 37; // = 0
	private static int ball2VectorY1Addr = 38; // = 0
	private static int ball2VectorY2Addr = 39; // = 0
	private static int ball1AngleAddr = 40; // = 14
	private static int ball2AngleAddr = 41; // = 0
	private static int leftRightFlagAddr = 42; // = 0
	private static int leftRightFlag2Addr = 43; // = 0
	private static int multIn1Low = 44;
	private static int multIn1High = 45;
	private static int multIn2Low = 46;
	private static int multIn2High = 47;
	private static int multOut1 = 48;
	private static int multOut2 = 49;
	private static int multOut3 = 50;
	private static int multOut4 = 51;
	private static int isBall1 = 52;
	private static int isUpDown = 53;
	private static int mult8Bit1 = 54;
	private static int mult8Bit2 = 55;
	private static int scratch2Addr = 56;
	private static int mult8BitResultLow = 57;
	private static int mult8BitResultHigh = 58;
	private static int totalBlocksBroken = 59;
	private static int totalBlocksBroken2 = 60;
	private static int breakBlockAddr = 61;
	private static int didBreakBlock = 62;
	private static int blockHitInFrame = 63;
	private static int blockHitInFrame2 = 64;
	private static int blockHitInFrame3 = 65;
	
	private static byte LDA_IMMEDIATE = (byte)0xa9;
	private static byte LDA_ZEROPAGE = (byte)0xa5;
	private static byte LDA_ABSOLUTE = (byte)0xad;
	private static byte LDA_INDIRECTY = (byte)0xb1;
	private static byte STA_ABSOLUTE = (byte)0x8d;
	private static byte STA_ZEROPAGE = (byte)0x85;
	private static byte STA_INDIRECTY = (byte)0x91;
	private static byte LDX_ZEROPAGE = (byte)0xa6;
	private static byte LDX_IMMEDIATE = (byte)0xa2;
	private static byte STX_ABSOLUTE = (byte)0x8e;
	private static byte STX_ZEROPAGE = (byte)0x86;
	private static byte LDY_IMMEDIATE = (byte)0xa0;
	private static byte STY_ZEROPAGE = (byte)0x84;
	private static byte AND_ZEROPAGE = (byte)0x25;
	private static byte AND_IMMEDIATE = (byte)0x29;
	private static byte LSR_ACCUMULATOR = (byte)0x4a;
	private static byte LSR_ZEROPAGE = (byte)0x46;
	private static byte ROL_ZEROPAGE = (byte)0x26;
	private static byte ADC_ZEROPAGE = (byte)0x65;
	private static byte ADC_IMMEDIATE = (byte)0x69;
	private static byte CPY_IMMEDIATE = (byte)0xc0;
	private static byte CMP_IMMEDIATE = (byte)0xc9;
	private static byte CMP_ZEROPAGE = (byte)0xc5;
	private static byte CPX_IMMEDIATE = (byte)0xe0;
	private static byte SBC_ZEROPAGE = (byte)0xe5;
	private static byte SBC_IMMEDIATE = (byte)0xe9;
	private static byte ASL_ACCUMULATOR = (byte)0x0a;
	private static byte ROR_ACCUMULATOR = (byte)0x6a;
	private static byte ROR_ZEROPAGE = (byte)0x66;
	private static byte EOR_IMMEDIATE = (byte)0x49;
	private static byte SEI = (byte)0x78;
	private static byte RTI = (byte)0x40;
	private static byte BEQ = (byte)0xf0;
	private static byte BNE = (byte)0xd0;
	private static byte BCC = (byte)0x90;
	private static byte BPL = (byte)0x10;
	private static byte BMI = (byte)0x30;
	private static byte BCS = (byte)0xb0;
	private static byte DEC_ZEROPAGE = (byte)0xc6;
	private static byte DEX = (byte)0xca;
	private static byte INX = (byte)0xe8;
	private static byte INY = (byte)0xc8;
	private static byte INC_ZEROPAGE = (byte)0xe6;
	private static byte CLC = (byte)0x18;
	private static byte SEC = (byte)0x38;
	private static byte TAX = (byte)0xaa;
	private static byte TYA = (byte)0x98;
	private static byte TXA = (byte)0x8a;
	private static byte TXS = (byte)0x9a;
	private static byte TAY = (byte)0xa8;
	private static byte PHP = (byte)0x08;
	private static byte PHA = (byte)0x48;
	private static byte PLP = (byte)0x28;
	private static byte PLA = (byte)0x68;
	
	private static Random rand = new Random(42);
	
	public static void main(String[] args)
	{
		try
		{
			ArrayList<Subroutine> subroutines = new ArrayList<Subroutine>();
			subroutines.add(reset());
			subroutines.add(doMain());
			subroutines.add(irq());
			subroutines.add(disableVideo());
			subroutines.add(enableVideo());
			subroutines.add(waitForNmi());
			subroutines.add(doWait());
			subroutines.add(blankNametable());
			subroutines.add(waitForStart());
			subroutines.add(nmi());
			subroutines.add(readController());
			subroutines.add(setupTitleScreen());
			subroutines.add(setAllPalette1());
			subroutines.add(writeText());
			subroutines.add(writeTextLine());
			subroutines.add(setupLevelScreen());
			subroutines.add(clearTextMemory());
			subroutines.add(setupWalls());
			subroutines.add(loadLevel());
			subroutines.add(loadLevelLine());
			subroutines.add(copyLevelToRam());
			subroutines.add(loadSprites());
			subroutines.add(initGameVariables());
			subroutines.add(beatGameScreen());
			subroutines.add(playLevel());
			subroutines.add(updateBallAndPaddle());
			subroutines.add(oamDma());
			subroutines.add(deathCheck());
			subroutines.add(handleWalls());
			subroutines.add(handlePaddle());
			subroutines.add(updateBall1());
			subroutines.add(updateBall2());
			subroutines.add(handleBricks());
			subroutines.add(multiply());
			subroutines.add(handleBall1Left());
			subroutines.add(handleBall1Right());
			subroutines.add(handleBall1Up());
			subroutines.add(handleBall1Down());
			subroutines.add(handleBall2Left());
			subroutines.add(handleBall2Right());
			subroutines.add(handleBall2Up());
			subroutines.add(handleBall2Down());
			subroutines.add(breakBlock());
			subroutines.add(breakBlock2());
			subroutines.add(spawnBall2());
			subroutines.add(updateBlockInVram());
			subroutines.add(multiply8Bit());
			subroutines.add(setupGameOverScreen());
			
			byte[] rom = new byte[16 + 32768 + 8192];
			writeHeader(rom);
			
			localRelocations(subroutines);
			writeEpilogues(subroutines);
			int firstFreeAddress = assignAddresses(subroutines);
			if (firstFreeAddress >= 0xf600)
			{
				System.out.println("ROM is too big. First free address was " + String.format("0x%04X", firstFreeAddress));
				System.exit(-1);
			}
			
			fixUpRelocations(subroutines);
			writePrgRom(rom, subroutines);
			writeLevelData(rom);
			writeAngleTable(rom);
			writeChrRom(rom, args[0]);
			fixUpInterrupts(rom, subroutines);
			writeOutputFile(rom, "time_waster.nes");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void writeOutputFile(byte[] rom, String filename) throws IOException
	{
		File outputFile = new File(filename);
		try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
		    outputStream.write(rom);
		}
	}
	
	private static Subroutine spawnBall2()
	{
		Subroutine sub = new Subroutine("spawnBall2");
		ArrayList<Byte> data = sub.getData();
		//copy ball1 but flip up/down and left/right
		//ball2XAddr1 = 32; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		//ball2XAddr2 = 33; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		//ball2YAddr1 = 34; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		//ball2YAddr2 = 35; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		//ball2VectorX1Addr = 36; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		
		//ball2VectorX2Addr = 37; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		
		//ball2VectorY1Addr = 38; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		
		//ball2VectorY2Addr = 39; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		
		//ball1AngleAddr = 40; // = 14
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//flip up/down left/right
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BEQ);
		sub.branchTo("setUp");
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("doLeftRight");
		
		sub.setLabel("setUp");
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(INC_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		
		sub.setLabel("doLeftRight");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BEQ);
		sub.branchTo("setLeft");
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("setLeft");
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(INC_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine updateBlockInVram()
	{
		Subroutine sub = new Subroutine("updateBlockInVram");
		ArrayList<Byte> data = sub.getData();
		
		//offset in 11x16 terms is in the low two byte counter
		//figure out row, which is divided by 11 plus 2
		data.add(LDA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("divLoop");
		data.add(CMP_IMMEDIATE);
		data.add((byte)11);
		data.add(BCC);
		sub.branchTo("divDone");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)11);
		data.add(INX);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("divLoop");
		
		sub.setLabel("divDone");
		data.add(INX);
		data.add(INX);
		data.add(TAY);
		
		//Y has remainder after div by 11
		//X has row in nametable
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x20);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2High);
		
		//each row offsets 32 bytes from 0x2000
		data.add(INX);
		sub.setLabel("multLoop");
		data.add(DEX);
		data.add(BEQ);
		sub.branchTo("multDone");
		data.add(LDA_IMMEDIATE);
		data.add((byte)32);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByte2High);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2High);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("multLoop");
		
		//the remainder after division is offset into row add 1 to that
		//multiply that by 2 
		sub.setLabel("multDone");
		data.add(INY);
		data.add(TYA);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//and add to VRAM address
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByte2High);
		
		//We are outside of vlbank so wait till next cycle
		sub.callRoutine("waitForNmi");
		
		//Set two bytes at this address
		data.add(STA_ABSOLUTE);
		data.add((byte)0x06);
		data.add((byte)0x20);
		data.add(LDA_ZEROPAGE);
		data.add((byte)twoByte2Low);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x06);
		data.add((byte)0x20);
		
		//Get first value
		data.add(LDY_IMMEDIATE);
		data.add((byte)0);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(BEQ);
		sub.branchTo("zeroIt"); 
		data.add(ASL_ACCUMULATOR);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20);
		
		//Second value
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("zeroIt");
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine breakBlock()
	{
		Subroutine sub = new Subroutine("breakBlock");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)didBreakBlock);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		//A has offset from 0x500
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		
		sub.setLabel("dupCheckDone");
		
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x05);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDY_IMMEDIATE);
		data.add((byte)0);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		
		//Block still exists
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BNE);
		sub.branchTo("trySlot2");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("saveDone");
		
		sub.setLabel("trySlot2");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BNE);
		sub.branchTo("trySlot3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("saveDone");
		
		sub.setLabel("branchToDone3");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		
		sub.setLabel("trySlot3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		
		//Is it a powerup?
		sub.setLabel("saveDone");
		data.add(CMP_IMMEDIATE);
		data.add((byte)4);
		data.add(BNE);
		sub.branchTo("notPowerup1");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		
		//fix paddle dimensions
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)47);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)191);
		sub.setLabel("fixPaddleLoop");
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(BCS);
		sub.branchTo("doneFixingPaddle");
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("fixPaddleLoop");
		
		//clear block type 4 sprite and branch to do bounce
		sub.setLabel("doneFixingPaddle");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x14);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x15);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x16);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x17);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x18);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x19);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1a);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1b);
		data.add((byte)0x06);
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDoBounce");
		
		sub.setLabel("notPowerup1");
		data.add(CMP_IMMEDIATE);
		data.add((byte)5);
		data.add(BNE);
		sub.branchTo("notPowerup2");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)2);
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		
		//fix paddle dimensions
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)31);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		
		//clear block type 5 sprite and branch to done or doBounce
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1c);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1d);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1e);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1f);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x20);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x21);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x22);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x23);
		data.add((byte)0x06);
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("branchToDoBounce");
		data.add(BCC);
		sub.branchTo("doBounce");
		
		sub.setLabel("notPowerup2");
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BNE);
		sub.branchTo("notPowerup");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		
		//clear block type 6 sprite and spawn ball 2 (toggle both flags) 
		//and branch to do bounce
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x24);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x25);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x26);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x27);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x28);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x29);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2a);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2b);
		data.add((byte)0x06);
		
		sub.callRoutine("spawnBall2");
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("doBounce");
		
		sub.setLabel("notPowerup");
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(BNE);
		sub.branchTo("updateBlock");
		data.add(DEC_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)totalBlocksBroken);
		data.add(BNE);
		sub.branchTo("updateBlock");
		data.add(INC_ZEROPAGE);
		data.add((byte)totalBlocksBroken2);
		
		//clear BG tile in VRAM
		sub.setLabel("updateBlock");
		sub.callRoutine("updateBlockInVram");
		
		sub.setLabel("doBounce");
		data.add(LDA_ZEROPAGE);
		data.add((byte)powerupAddr);
		data.add(CMP_IMMEDIATE);
		data.add((byte)2);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(INC_ZEROPAGE);
		data.add((byte)didBreakBlock);
		
		//Toggle correct flag for correct ball
		data.add(LDA_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(BEQ);
		sub.branchTo("ball2");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(BEQ);
		sub.branchTo("ball1LeftRight");
		
		//ball1 up/down
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BEQ);
		sub.branchTo("setBall1Up");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("setBall1Up");
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("ball1LeftRight");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BEQ);
		sub.branchTo("setBall1Left");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("setBall1Left");
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("ball2");
		data.add(LDA_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(BEQ);
		sub.branchTo("ball2LeftRight");
		
		//ball1 up/down
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(BEQ);
		sub.branchTo("setBall2Up");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("setBall2Up");
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("ball2LeftRight");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(BEQ);
		sub.branchTo("setBall2Left");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("setBall2Left");
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(CLC);
		data.add(BCC);

		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine breakBlock2()
	{
		Subroutine sub = new Subroutine("breakBlock2");
		ArrayList<Byte> data = sub.getData();
		
		//The same as breakBlock, but it never bounces the ball
		data.add(LDA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		//A has offset from 0x500
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BEQ);
		sub.branchTo("dupCheckDone");
		data.add(CMP_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		
		sub.setLabel("dupCheckDone");
		
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x05);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDY_IMMEDIATE);
		data.add((byte)0);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		
		//Block still exists
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BNE);
		sub.branchTo("trySlot2");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("saveDone");
		
		sub.setLabel("trySlot2");
		data.add(LDX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)0xff);
		data.add(BNE);
		sub.branchTo("trySlot3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("saveDone");
		
		sub.setLabel("branchToDone3");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		
		sub.setLabel("trySlot3");
		data.add(LDX_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		
		//Is it a powerup?
		sub.setLabel("saveDone");
		data.add(CMP_IMMEDIATE);
		data.add((byte)4);
		data.add(BNE);
		sub.branchTo("notPowerup1");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		
		//fix paddle dimensions
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)47);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)191);
		sub.setLabel("fixPaddleLoop");
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(BCS);
		sub.branchTo("doneFixingPaddle");
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("fixPaddleLoop");
		
		//clear block type 4 sprite and branch to do bounce
		sub.setLabel("doneFixingPaddle");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x14);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x15);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x16);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x17);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x18);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x19);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1a);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1b);
		data.add((byte)0x06);
		
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		
		sub.setLabel("notPowerup1");
		data.add(CMP_IMMEDIATE);
		data.add((byte)5);
		data.add(BNE);
		sub.branchTo("notPowerup2");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)2);
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		
		//fix paddle dimensions
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)31);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		
		//clear block type 5 sprite and branch to done
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1c);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1d);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1e);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1f);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x20);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x21);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x22);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x23);
		data.add((byte)0x06);
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("branchToDone2");
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("notPowerup2");
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BNE);
		sub.branchTo("notPowerup");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		
		//clear block type 6 sprite and spawn ball 2 (toggle both flags) 
		//and branch to do bounce
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x24);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x25);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x26);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x27);
		data.add((byte)0x06);
		
		data.add(STA_ABSOLUTE);
		data.add((byte)0x28);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x29);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2a);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2b);
		data.add((byte)0x06);
		
		sub.callRoutine("spawnBall2");
		
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("notPowerup");
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(BNE);
		sub.branchTo("updateBlock");
		data.add(DEC_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)totalBlocksBroken);
		data.add(BNE);
		sub.branchTo("updateBlock");
		data.add(INC_ZEROPAGE);
		data.add((byte)totalBlocksBroken2);
		
		//clear BG tile in VRAM
		sub.setLabel("updateBlock");
		sub.callRoutine("updateBlockInVram");

		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine handleBall1Left()
	{
		Subroutine sub = new Subroutine("handleBall1Left");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what col of bricks we could possibly be hitting from the right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		
		//Divide by 16
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Save col in Y
		data.add(TAY);
		
		//Then isolate the (max) 2 possible rows
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x07);
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//A has first row
		//Y has col
		//Compute offset from 0x500
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(INC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		//If scratch2 does not contain 15, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)15);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("done");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(INC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall2Left()
	{
		Subroutine sub = new Subroutine("handleBall2Left");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what col of bricks we could possibly be hitting from the right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		
		//Divide by 16
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Save col in Y
		data.add(TAY);
		
		//Then isolate the (max) 2 possible rows
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x07);
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//A has first row
		//Y has col
		//Compute offset from 0x500
		
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(INC_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		//If scratch2 does not contain 15, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)15);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("done");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(INC_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall1Right()
	{
		Subroutine sub = new Subroutine("handleBall1Right");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what col of bricks we could possibly be hitting from the left
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)29);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)29);
		
		//Then divide by 16
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Add one
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		
		//This tells us which col of blocks starting from the left (counting from zero)
		//Copy to Y
		data.add(TAY);
		
		//Then isolate the (max) 2 possible rows
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x07);
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		//If scratch2 does not contain 15, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)15);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("done");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall2Right()
	{
		Subroutine sub = new Subroutine("handleBall2Right");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what col of bricks we could possibly be hitting from the left
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)29);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)29);
		
		//Then divide by 16
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Add one
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		
		//This tells us which col of blocks starting from the left (counting from zero)
		//Copy to Y
		data.add(TAY);
		
		//Then isolate the (max) 2 possible rows
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x07);
		data.add(CMP_IMMEDIATE);
		data.add((byte)6);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		//If scratch2 does not contain 15, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratch2Addr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)15);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR); //A has row
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("done");
		
		//Calculate offset from 0x500 in A
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit1);
		data.add(LDA_IMMEDIATE);
		data.add((byte)11);
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8Bit2);
		sub.callRoutine("multiply8Bit");
		data.add(LDA_ZEROPAGE);
		data.add((byte)mult8BitResultLow);
		data.add(STY_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall1Down()
	{
		Subroutine sub = new Subroutine("handleBall1Down");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what row of bricks we could possibly be hitting from the top
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)21);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)21);
		
		//Then divide by 8
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Add one
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		
		//This tells us which row of blocks starting from the top (counting from zero)
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Convert to row offset from 0x500
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("loop");
		data.add(DEX);
		data.add(BEQ);
		sub.branchTo("loopDone");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(BCC);
		sub.branchTo("loop");
		
		//Then isolate the (max) 2 possible bricks - row offset is in A
		sub.setLabel("loopDone");
		data.add(TAX); //row offset is in X
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x0f);
		data.add(CMP_IMMEDIATE);
		data.add((byte)14);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		//If scratch does not contain 10, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)10);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall2Down()
	{
		Subroutine sub = new Subroutine("handleBall2Down");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what row of bricks we could possibly be hitting from the top
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)21);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)21);
		
		//Then divide by 8
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		//Add one
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		
		//This tells us which row of blocks starting from the top (counting from zero)
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Convert to row offset from 0x500
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("loop");
		data.add(DEX);
		data.add(BEQ);
		sub.branchTo("loopDone");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(BCC);
		sub.branchTo("loop");
		
		//Then isolate the (max) 2 possible bricks - row offset is in A
		sub.setLabel("loopDone");
		data.add(TAX); //row offset is in X
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x0f);
		data.add(CMP_IMMEDIATE);
		data.add((byte)14);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		//If scratch does not contain 10, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)10);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall1Up()
	{
		Subroutine sub = new Subroutine("handleBall1Up");
		ArrayList<Byte> data = sub.getData();
		
		//First isolate what row of bricks we could possibly be hitting from the bottom
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		
		//Then divide by 8
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Convert to row offset from 0x500
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("loop");
		data.add(DEX);
		data.add(BEQ);
		sub.branchTo("loopDone");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(BCC);
		sub.branchTo("loop");
		
		//Then isolate the (max) 2 possible bricks - row offset is in A
		sub.setLabel("loopDone");
		data.add(TAX); //row offset is in X
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x0f);
		data.add(CMP_IMMEDIATE);
		data.add((byte)14);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(INC_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		//If scratch does not contain 10, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)10);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(INC_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBall2Up()
	{
		Subroutine sub = new Subroutine("handleBall2Up");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		
		//Then divide by 8
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("branchToDone");
		
		//Convert to row offset from 0x500
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("loop");
		data.add(DEX);
		data.add(BEQ);
		sub.branchTo("loopDone");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)11);
		data.add(BCC);
		sub.branchTo("loop");
		
		//Then isolate the (max) 2 possible bricks - row offset is in A
		sub.setLabel("loopDone");
		data.add(TAX); //row offset is in X
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)16);
		data.add(BCS);
		sub.branchTo("dataOk");
		data.add(LDA_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("dataOk");
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)16);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(AND_IMMEDIATE);
		data.add((byte)0x0f);
		data.add(CMP_IMMEDIATE);
		data.add((byte)14);
		data.add(BCC);
		sub.branchTo("onlyOne"); //branch if we can only be hitting one brick
		data.add(BCS);
		sub.branchTo("theresTwo");
		
		sub.setLabel("branchToDone2");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		sub.setLabel("theresTwo");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("noBreak");
		data.add(INC_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		//If scratch does not contain 10, we have to check next block too
		sub.setLabel("noBreak");
		data.add(LDX_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)10);
		data.add(BEQ);
		sub.branchTo("branchToDone");
		
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock2");
		sub.setLabel("branchToDone");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		
		sub.setLabel("onlyOne");
		data.add(LDA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(LSR_ACCUMULATOR);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr); //Offset in row of first
		data.add(TXA);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		
		//A has offset from 0x500
		//Break that block if it still exists
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)isBall1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)isUpDown);
		data.add(STA_ZEROPAGE);
		data.add((byte)breakBlockAddr);
		sub.callRoutine("breakBlock");
		data.add(LDX_ZEROPAGE);
		data.add((byte)didBreakBlock);
		data.add(BEQ);
		sub.branchTo("done");
		data.add(INC_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine handleBricks()
	{
		Subroutine sub = new Subroutine("handleBricks");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ZEROPAGE);
		data.add((byte)blockHitInFrame);
		data.add(STA_ZEROPAGE);
		data.add((byte)blockHitInFrame2);
		data.add(STA_ZEROPAGE);
		data.add((byte)blockHitInFrame3);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BNE);
		sub.branchTo("up1");
		sub.callRoutine("handleBall1Down");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("leftRightCheck");
		sub.setLabel("up1");
		sub.callRoutine("handleBall1Up");
		sub.setLabel("leftRightCheck");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BNE);
		sub.branchTo("left1");
		sub.callRoutine("handleBall1Right");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("ball2Check");
		sub.setLabel("left1");
		sub.callRoutine("handleBall1Left");
		sub.setLabel("ball2Check");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)ball2XAddr2);
		data.add(BEQ);
		sub.branchTo("done");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(BNE);
		sub.branchTo("up2");
		sub.callRoutine("handleBall2Down");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("leftRightCheck2");
		sub.setLabel("up2");
		sub.callRoutine("handleBall2Up");
		sub.setLabel("leftRightCheck2");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(BNE);
		sub.branchTo("left2");
		sub.callRoutine("handleBall2Right");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("done");
		sub.setLabel("left2");
		sub.callRoutine("handleBall2Left");
		
		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine updateBall1()
	{
		Subroutine sub = new Subroutine("updateBall1");
		ArrayList<Byte> data = sub.getData();
		//We want to multiply speed by X vector
		//To get our X update 
		//This is a 16 bit by 16 bit multiplication
		//resulting in a 32 bit product
		//but we can throw out the top and bottom 8 bits of the result
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1High);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2High);
		
		sub.callRoutine("multiply");
		
		//multOut+1 and +2 has X update
		//We need to know if this is left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BNE);
		sub.branchTo("left");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		data.add(BCC);
		sub.branchTo("yUpdate");
		
		//subtraction
		sub.setLabel("left");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		//And y looks the same 
		sub.setLabel("yUpdate");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1High);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2High);
		
		sub.callRoutine("multiply");
		
		//multOut+1 and +2 has Y update
		//We need to know if this is up or down
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BNE);
		sub.branchTo("up");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		data.add(BCC);
		sub.branchTo("done");
		
		//subtraction
		sub.setLabel("up");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine multiply8Bit()
	{
		Subroutine sub = new Subroutine("multiply8Bit");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0); //Accumulator is top byte of result - inited to zero
		data.add(STA_ZEROPAGE);
		data.add((byte)mult8BitResultLow); //Also init the rest to zero
		data.add(LDX_IMMEDIATE);
		data.add((byte)8); //8 bits to process
		sub.setLabel("l1");
		data.add(LSR_ZEROPAGE);
		data.add((byte)mult8Bit2); 
        data.add(BCC);
        sub.branchTo("l2"); //skip add if the bit was zero
        data.add(CLC);
        data.add(ADC_ZEROPAGE);
        data.add((byte)mult8Bit1);
        sub.setLabel("l2");
        data.add(ROR_ACCUMULATOR);
        data.add(ROR_ZEROPAGE);
        data.add((byte)mult8BitResultLow);
        data.add(DEX);
        data.add(BNE);
        sub.branchTo("l1");
        data.add(STA_ZEROPAGE);
        data.add((byte)mult8BitResultHigh);
		return sub;
	}
	
	private static Subroutine multiply()
	{
		Subroutine sub = new Subroutine("multiply");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE);
		data.add((byte)0); //Accumulator is top byte of result - inited to zero
		data.add(STA_ZEROPAGE);
		data.add((byte)multOut3); //Also init the rest to zero
		data.add(STA_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)multOut1);
		data.add(LDX_IMMEDIATE);
		data.add((byte)16); //16 bits to process
		sub.setLabel("l1");
		data.add(LSR_ZEROPAGE);
		data.add((byte)multIn2High); //Get low bit of high byte
		data.add(ROR_ZEROPAGE);
		data.add((byte)multIn2Low); //Shift the whole 2 byte number right one and gets low bit
        data.add(BCC);
        sub.branchTo("l2"); //skip add if the bit was zero
        data.add(TAY); //copy high byte of result
        data.add(CLC);
        data.add(LDA_ZEROPAGE);
        data.add((byte)multIn1Low); //Just give up trying to understand
        data.add(ADC_ZEROPAGE);
        data.add((byte)multOut3);
        data.add(ADC_ZEROPAGE);
        data.add((byte)multOut2);
        data.add(STA_ZEROPAGE);
        data.add((byte)multOut3);
        data.add(TYA);
        data.add(ADC_ZEROPAGE);
        data.add((byte)multIn1High);
        sub.setLabel("l2");
        data.add(ROR_ACCUMULATOR);
        data.add(ROR_ZEROPAGE);
        data.add((byte)multOut3);
        data.add(ROR_ZEROPAGE);
        data.add((byte)multOut2);
        data.add(ROR_ZEROPAGE);
        data.add((byte)multOut1);
        data.add(DEX);
        data.add(BNE);
        sub.branchTo("l1");
        data.add(STA_ZEROPAGE);
        data.add((byte)multOut4);
		return sub;
	}
	
	private static Subroutine updateBall2()
	{
		Subroutine sub = new Subroutine("updateBall2");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BEQ);
		sub.branchTo("done");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1High);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2High);
		
		sub.callRoutine("multiply");
		
		//multOut+1 and +2 has X update
		//We need to know if this is left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(BNE);
		sub.branchTo("left");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		data.add(BCC);
		sub.branchTo("yUpdate");
		
		//subtraction
		sub.setLabel("left");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		//And y looks the same 
		sub.setLabel("yUpdate");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn1High);
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2Low);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)multIn2High);
		
		sub.callRoutine("multiply");
		
		//multOut+1 and +2 has Y update
		//We need to know if this is up or down
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(BNE);
		sub.branchTo("up");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(ADC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		data.add(BCC);
		sub.branchTo("done");
		
		//subtraction
		sub.setLabel("up");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(SBC_ZEROPAGE);
		data.add((byte)multOut3);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine handlePaddle()
	{
		Subroutine sub = new Subroutine("handlePaddle");
		ArrayList<Byte> data = sub.getData();
		
		//ball 1
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BNE);
		sub.branchTo("branchToCheckBall23");
		
		//ball is moving down, so it might hit the paddle
		//if paddleTopY - 4 < ballY2
		//and ballY2 - 8 < paddleTopY
		//and paddleLeftX - 4 <  ballX2
		//and paddleRightX  >= ballX2
		//then we have a hit
		//We don't let the ball hit the side of the paddle for simplicity
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)4);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(BCS);
		sub.branchTo("branchToCheckBall23");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)8);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(BCS);
		sub.branchTo("branchToCheckBall23");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)4);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(BCS);
		sub.branchTo("branchToCheckBall23");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(BCC);
		sub.branchTo("branchToCheckBall23");
		
		//We have a hit
		//Set up flag
		//And figure out angle, vector, and left/right flag
		data.add(INC_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(LDA_ZEROPAGE);
		data.add((byte)powerupAddr);
		data.add(CMP_IMMEDIATE);
		data.add((byte)1);
		data.add(BEQ);
		sub.branchTo("widePaddle");
		
		//There are 34 ways the ball can hit a normal paddle
		//which one is it?
		//It's really 17 options + whether the angle goes left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)2);
		
		//Now we have a number 0 - 33
		//Need to convert it to left/right and lookup table index
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		data.add(BMI);
		sub.branchTo("left");
		
		//Set right
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		sub.setLabel("branchToCheckBall23");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToCheckBall22");
		
		//Set left
		sub.setLabel("left");
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//We have a number -17 - -1
		//Need to fix
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		data.add(CLC);
		sub.setLabel("branchToCheckBall22");
		data.add(BCC);
		sub.branchTo("branchToCheckBall2");
		
		//Wide paddle
		sub.setLabel("widePaddle");
		//There are 50 ways the ball can hit a normal paddle
		//which one is it?
		//It's really 25 options + whether the angle goes left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)2);
		
		//Now we have a number 0 - 49
		//Need to convert it to left/right and lookup table index
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)25);
		data.add(BMI);
		sub.branchTo("left2");
		
		//Set right
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		data.add(CLC);
		sub.setLabel("branchToCheckBall2");
		data.add(BCC);
		sub.branchTo("checkBall2");
		
		//Set left
		sub.setLabel("left2");
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//We have a number -25 - -1
		//Need to fix
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		
		//ball2
		sub.setLabel("checkBall2");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BEQ);
		sub.branchTo("branchToDone3");
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(BNE);
		sub.branchTo("branchToDone3");
		
		//ball exists and is moving down, so it might hit the paddle
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)4);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(BCS);
		sub.branchTo("branchToDone3");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)8);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(BCS);
		sub.branchTo("branchToDone3");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)4);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BCS);
		sub.branchTo("branchToDone3");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BCC);
		sub.branchTo("branchToDone3");
		
		//We have a hit
		//Set up flag
		//And figure out angle, vector, and left/right flag
		data.add(INC_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(LDA_ZEROPAGE);
		data.add((byte)powerupAddr);
		data.add(CMP_IMMEDIATE);
		data.add((byte)1);
		data.add(BEQ);
		sub.branchTo("widePaddle2");
		
		//There are 34 ways the ball can hit a normal paddle
		//which one is it?
		//It's really 17 options + whether the angle goes left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)2);
		
		//Now we have a number 0 - 33
		//Need to convert it to left/right and lookup table index
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)17);
		data.add(BMI);
		sub.branchTo("left3");
		
		//Set right
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		sub.setLabel("branchToDone3");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToDone2");
		
		//Set left
		sub.setLabel("left3");
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//We have a number -17 - -1
		//Need to fix
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		data.add(CLC);
		sub.setLabel("branchToDone2");
		data.add(BCC);
		sub.branchTo("branchToDone");
		
		//Wide paddle
		sub.setLabel("widePaddle2");
		//There are 50 ways the ball can hit a normal paddle
		//which one is it?
		//It's really 25 options + whether the angle goes left or right
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)2);
		
		//Now we have a number 0 - 49
		//Need to convert it to left/right and lookup table index
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)25);
		data.add(BMI);
		sub.branchTo("left4");
		
		//Set right
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		data.add(CLC);
		sub.setLabel("branchToDone");
		data.add(BCC);
		sub.branchTo("done");
		
		//Set left
		sub.setLabel("left4");
		data.add(LDX_IMMEDIATE);
		data.add((byte)1);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//We have a number -25 - -1
		//Need to fix
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(SEC);
		data.add(SBC_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(SEC);
		data.add(SBC_IMMEDIATE);
		data.add((byte)1);
		
		//Set angle
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//Set vector
		data.add(ASL_ACCUMULATOR);
		data.add(ASL_ACCUMULATOR);
		data.add(TAY);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xf6);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(INY);
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		
		sub.setLabel("done");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine handleWalls()
	{
		Subroutine sub = new Subroutine("handleWalls");
		ArrayList<Byte> data = sub.getData();
		
		//ball 1 - left wall
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCS);
		sub.branchTo("noHit1");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BEQ);
		sub.branchTo("noHit1");
		
		//We have a hit, change the balls direction
		data.add(DEC_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//ball 2 - left wall
		sub.setLabel("noHit1");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BEQ);
		sub.branchTo("noHit2");
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCS);
		sub.branchTo("noHit2");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(BEQ);
		sub.branchTo("noHit2");
		
		//We have a hit - change the balls direction
		data.add(DEC_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//ball 1 - right wall
		sub.setLabel("noHit2");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)189);
		data.add(BCC);
		sub.branchTo("noHit3");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(BNE);
		sub.branchTo("noHit3");
		
		//We have a hit - change the balls direction
		data.add(INC_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		
		//ball 2 - right wall
		sub.setLabel("noHit3");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)189);
		data.add(BCC);
		sub.branchTo("noHit4");
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(BNE);
		sub.branchTo("noHit4");
		
		//We have a hit - change the balls direction
		data.add(INC_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//ball 1 - top wall
		sub.setLabel("noHit4");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCS);
		sub.branchTo("noHit5");
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(BEQ);
		sub.branchTo("noHit5");
		
		//We have a hit, change the balls direction
		data.add(DEC_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		
		//ball 2 - top wall
		sub.setLabel("noHit5");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(BEQ);
		sub.branchTo("noHit6");
		data.add(CMP_IMMEDIATE);
		data.add((byte)17);
		data.add(BCS);
		sub.branchTo("noHit6");
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(BEQ);
		sub.branchTo("noHit6");
		
		//We have a hit - change the balls direction
		data.add(DEC_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		
		sub.setLabel("noHit6");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine deathCheck()
	{
		Subroutine sub = new Subroutine("deathCheck");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE);
		data.add((byte)230);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(BCS);
		sub.branchTo("branchToNotDead");
		
		//check ball 2
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BEQ);
		sub.branchTo("dead");
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)230);
		data.add(CMP_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(BCC);
		sub.branchTo("dead");
		
		//Move ball 2 to ball 1 and branch to notDead
		//ball2AngleAddr = 41; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		data.add(LDX_IMMEDIATE);
		data.add((byte)0);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		
		//ball2VectorX1Addr = 36; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		
		//ball2VectorX2Addr = 37; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		
		//ball2VectorY1Addr
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		
		//ball2VectorY2Addr
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		
		//ball2XAddr1 
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		
		//ball2XAddr2
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		
		//ball2YAddr1 
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		
		//ball2YAddr2
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(STX_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		
		//upDownFlag2Addr = 31; // = 1
		data.add(LDA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		
		//leftRightFlag2Addr = 43; // = 0
		data.add(LDA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		data.add(STX_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		sub.setLabel("branchToNotDead");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("notDead");
		
		//Reset variables
		sub.setLabel("dead");
		//paddleLeftXAddr = 2; // = 88
		data.add(LDA_IMMEDIATE);
		data.add((byte)88);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		//paddleRightXAddr = 3; // = 119
		data.add(LDA_IMMEDIATE);
		data.add((byte)119);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		//paddleTopYAddr = 4; // = 208
		data.add(LDA_IMMEDIATE);
		data.add((byte)208);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		//paddleBottomYAddr = 5; // = 215
		data.add(LDA_IMMEDIATE);
		data.add((byte)215);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		//ballXAddr1 = 6; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		//ballXAddr2 = 7; // = 103
		data.add(LDA_IMMEDIATE);
		data.add((byte)103);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		//ballYAddr1 = 8; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		//ballYAddr2 = 9; // = 205
		data.add(LDA_IMMEDIATE);
		data.add((byte)205);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		//ballVectorX1Addr = 11; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		//ballVectorX2Addr = 12; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		//ballVectorY1Addr = 22; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		//ballVectorY2Addr = 23; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		//ballFrozenAddr = 26; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		//ballFrozenTimerAddr = 27; // = 255
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenTimerAddr);
		//deathFlagAddr
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)deathFlagAddr);
		//powerupAddr
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		//upDownFlagAddr = 30; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		//upDownFlag2Addr = 31; // = 1
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		//ball2XAddr1 = 32; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		//ball2XAddr2 = 33; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		//ball2YAddr1 = 34; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		//ball2YAddr2 = 35; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		//ball2VectorX1Addr = 36; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		//ball2VectorX2Addr = 37; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		//ball2VectorY1Addr = 38; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		//ball2VectorY2Addr = 39; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		//ball1AngleAddr = 40; // = 14
		data.add(LDA_IMMEDIATE);
		data.add((byte)14);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		//ball2AngleAddr = 41; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		//leftRightFlagAddr
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		//leftRightFlag2Addr
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		sub.setLabel("notDead");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine oamDma()
	{
		Subroutine sub = new Subroutine("oamDma");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x03);
		data.add((byte)0x20); //Set OAM DMA address to zero
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x14);
		data.add((byte)0x40); //Init DMA transfer from 0x600
		return sub;
	}
	
	private static Subroutine updateBallAndPaddle()
	{
		Subroutine sub = new Subroutine("updateBallAndPaddle");
		ArrayList<Byte> data = sub.getData();
		//At 0x600 write these bytes
		//207, 0, 1, 88 - paddle 600
		//207, 0, 1, 96 - paddle 604
		//207, 0, 1, 104 - paddle 608
		//207, 0, 1, 112 - paddle 60c
		//204, 1, 1, 103 - ball 610
		//8 bytes powerup 1 614
		//4 bytes powerup 2 61c
		//4 bytes powerup 3 624
		//paddle extension 1 62c
		//paddle extension 2 630
		//ball 2 634
		
		//private static int paddleLeftXAddr = 2; // = 88
		//private static int paddleRightXAddr = 3; // = 119
		//private static int paddleTopYAddr = 4; // = 208
		//private static int paddleBottomYAddr = 5; // = 215
		//private static int ballXAddr1 = 6; // = 00
		//private static int ballXAddr2 = 7; // = 103
		//private static int ballYAddr1 = 8; // = 00
		//private static int ballYAddr2 = 9; // = 205
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x03);
		data.add((byte)0x06);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x06);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0b);
		data.add((byte)0x06);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0f);
		data.add((byte)0x06);
		data.add(LDX_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(DEX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x00);
		data.add((byte)0x06);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x04);
		data.add((byte)0x06);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x08);
		data.add((byte)0x06);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x0c);
		data.add((byte)0x06);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x13);
		data.add((byte)0x06);
		data.add(LDX_ZEROPAGE);
		data.add((byte)ballYAddr2);
		data.add(DEX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x10);
		data.add((byte)0x06);
		
		//Check for wide paddle
		data.add(LDA_ZEROPAGE);
		data.add((byte)powerupAddr);
		data.add(CMP_IMMEDIATE);
		data.add((byte)1);
		data.add(BEQ);
		sub.branchTo("widePaddle");
		
		//Blank these 2 sprites
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2c);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2d);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2e);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2f);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x30);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x31);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x32);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x33);
		data.add((byte)0x06);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("widePaddleDone");
		
		//Turn on 2 more sprites for the paddle
		//example: 207, 0, 1, 88
		sub.setLabel("widePaddle");
		data.add(LDX_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(DEX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x2c);
		data.add((byte)0x06);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x30);
		data.add((byte)0x06);
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)0x20);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x33);
		data.add((byte)0x06);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)0x08);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2f);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2d);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x31);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2e);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x32);
		data.add((byte)0x06);
		
		//Handle second ball
		sub.setLabel("widePaddleDone");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(BNE);
		sub.branchTo("have2ndBall");
		
		//Blank this sprite
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x34);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x35);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x36);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x37);
		data.add((byte)0x06);
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("secondBallDone");
		
		//Display second ball
		sub.setLabel("have2ndBall");
		data.add(LDX_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		data.add(DEX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x34);
		data.add((byte)0x06);
		data.add(LDA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x37);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x35);
		data.add((byte)0x06);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x36);
		data.add((byte)0x06);
		
		sub.setLabel("secondBallDone");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine playLevel()
	{
		Subroutine sub = new Subroutine("playLevel");
		ArrayList<Byte> data = sub.getData();
		
		//It's just a loop that only does work during VBlank
		sub.callRoutine("waitForNmi");
		
		//Get controller state
		sub.callRoutine("readController");
		
		//If A or B are pressed unfreeze ball
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x80); //immediate - mask
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed1");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		sub.setLabel("notPressed1");
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x40); //immediate - mask
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed2");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		sub.setLabel("notPressed2");
		
		//Otherwise if the ball freeze timer is not zero
		//decrement it
		//and if it becomes zero, unfreeze the ball
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballFrozenTimerAddr);
		data.add(BEQ);
		sub.branchTo("timerCheckDone");
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballFrozenTimerAddr);
		data.add(BNE);
		sub.branchTo("timerCheckDone");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		
		//If we are frozen, only worry about moving left and right
		sub.setLabel("timerCheckDone");
		data.add(LDA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		
		data.add(BEQ);
		sub.branchTo("weAreLive");
		
		//paddle and ball move together
		//Only left and right allowed while frozen
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x02); //immediate - mask for left
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed3");
		
		//private static int paddleLeftXAddr = 2; // = 88
		//private static int paddleRightXAddr = 3; // = 119
		//private static int paddleTopYAddr = 4; // = 208
		//private static int paddleBottomYAddr = 5; // = 215
		//private static int ballXAddr1 = 6; // = 00
		//private static int ballXAddr2 = 7; // = 103
		//private static int ballYAddr1 = 8; // = 00
		//private static int ballYAddr2 = 9; // = 205
		
		//Move paddle and ball left 1 if leftX > 17
		data.add(LDA_IMMEDIATE);
		data.add((byte)17);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(BCS);
		sub.branchTo("notPressed3");
		sub.setLabel("pressed3");
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(DEC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		sub.setLabel("notPressed3");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x01); //immediate - mask for right
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed4");
		
		//Move paddle and ball right 1 if rightX <= 189
		data.add(LDA_IMMEDIATE);
		data.add((byte)189);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(BCC);
		sub.branchTo("notPressed4");
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		data.add(INC_ZEROPAGE);
		data.add((byte)ballXAddr2);
		
		//Do OAM DMA and loop
		sub.setLabel("notPressed4");
		sub.callRoutine("updateBallAndPaddle");
		sub.callRoutine("oamDma");
		sub.jump("playLevel");
		
		//Else we are in live play
		sub.setLabel("weAreLive");
		
		data.add(LDA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)31);
		data.add(LDX_ZEROPAGE);
		data.add((byte)powerupAddr);
		data.add(CPX_IMMEDIATE);
		data.add((byte)1);
		data.add(BNE);
		sub.branchTo("writeRightBound");
		data.add(ADC_IMMEDIATE);
		data.add((byte)16);
		
		sub.setLabel("writeRightBound");
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		
		//Update paddle position
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x02); //immediate - mask for left
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed5");
		
		//Move paddle left 1 if leftX > 17
		data.add(LDA_IMMEDIATE);
		data.add((byte)17);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(BCS);
		sub.branchTo("notPressed5");
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		
		sub.setLabel("notPressed5");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x01); //immediate - mask for right
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed6");
		
		//Move paddle right 1 if rightX <= 189
		data.add(LDA_IMMEDIATE);
		data.add((byte)189);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(BCC);
		sub.branchTo("notPressed6");
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		
		sub.setLabel("notPressed6");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x04); //immediate - mask for down
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed7");
		
		//Move paddle down 1 if bottomY <= 214
		data.add(LDA_IMMEDIATE);
		data.add((byte)214);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		data.add(BCC);
		sub.branchTo("notPressed7");
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(INC_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		
		sub.setLabel("notPressed7");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x08); //immediate - mask for up
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ); //branch if not pressed
		sub.branchTo("notPressed8");
		
		//Move paddle up 1 if paddleTopY > 160
		data.add(LDA_IMMEDIATE);
		data.add((byte)160);
		data.add(CMP_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(BCS);
		sub.branchTo("notPressed8");
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		
		//Update ball 1 position
		sub.setLabel("notPressed8");
		sub.callRoutine("updateBall1");
		
		//Update ball 2 position
		sub.callRoutine("updateBall2");
		
		sub.callRoutine("updateBallAndPaddle");
		sub.callRoutine("oamDma");
		
		//Check for death and reset necessary variables
		sub.callRoutine("deathCheck");
		data.add(LDA_ZEROPAGE);
		data.add((byte)deathFlagAddr);
		data.add(BEQ);
		sub.branchTo("notDead");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)deathFlagAddr);
		data.add(DEC_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BEQ);
		sub.branchTo("returnToCaller");
		sub.jump("playLevel");
		
		//Check for ball/wall collision
		sub.setLabel("notDead");
		sub.callRoutine("handleWalls");
		
		//Check for ball/paddle collision
		sub.callRoutine("handlePaddle");
		
		//Check for ball/brick collision
		//including updating BG and sprites and powerup effects and getting new powerups
		sub.callRoutine("handleBricks");
		
		//Check for level completion
		data.add(LDA_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		data.add(BEQ);
		sub.branchTo("returnToCaller");
		
		//Do OAM DMA and loop
		sub.setLabel("loopEnd");
		sub.jump("playLevel");
		sub.setLabel("returnToCaller");
		data.add(CLC);
		
		return sub;
	}
	
	private static Subroutine initGameVariables()
	{
		Subroutine sub = new Subroutine("initGameVariables");
		ArrayList<Byte> data = sub.getData();
		//livesAddr = 0; // = 3
		data.add(LDA_IMMEDIATE);
		data.add((byte)3);
		data.add(STA_ZEROPAGE);
		data.add((byte)livesAddr);
		//paddleLeftXAddr = 2; // = 88
		data.add(LDA_IMMEDIATE);
		data.add((byte)88);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		//paddleRightXAddr = 3; // = 119
		data.add(LDA_IMMEDIATE);
		data.add((byte)119);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		//paddleTopYAddr = 4; // = 208
		data.add(LDA_IMMEDIATE);
		data.add((byte)208);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		//paddleBottomYAddr = 5; // = 215
		data.add(LDA_IMMEDIATE);
		data.add((byte)215);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		//ballXAddr1 = 6; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		//ballXAddr2 = 7; // = 103
		data.add(LDA_IMMEDIATE);
		data.add((byte)103);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		//ballYAddr1 = 8; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		//ballYAddr2 = 9; // = 205
		data.add(LDA_IMMEDIATE);
		data.add((byte)205);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		//blocksRemainingInLevelAddr = 10; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		//ballVectorX1Addr = 11; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		//ballVectorX2Addr = 12; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		//nmiFlagAddr = 13; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)nmiFlagAddr);
		//timerAddr = 14; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)timerAddr);
		//twoByteCounterLowAddr = 15; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		//twoByteCounterHighAddr = 16; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		//buttonsAddr = 17; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)buttonsAddr);
		//lineNumberAddr = 18; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		//scratchAddr = 19; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		//twoByte2Low = 20; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2Low);
		//twoByte2High = 21; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByte2High);
		//ballVectorY1Addr = 22; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		//ballVectorY2Addr = 23; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		//ballSpeed1Addr = 24; // = 176
		data.add(LDA_IMMEDIATE);
		data.add((byte)176);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		//ballSpeed2Addr = 25; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		//ballFrozenAddr = 26; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)totalBlocksBroken);
		data.add(STA_ZEROPAGE);
		data.add((byte)totalBlocksBroken2);
		return sub;
	}
	
	private static Subroutine loadSprites()
	{
		Subroutine sub = new Subroutine("loadSprites");
		ArrayList<Byte> data = sub.getData();
		//At 0x600 write these bytes
		//207, 0, 1, 88
		//207, 0, 1, 96
		//207, 0, 1, 104
		//207, 0, 1, 112
		//204, 1, 1, 103
		data.add(LDA_IMMEDIATE);
		data.add((byte)207);
		data.add(STA_ABSOLUTE);
		data.add((byte)0);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x01);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x02);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)88);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x03);
		data.add((byte)0x06);
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)207);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x04);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x05);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x06);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)96);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x06);
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)207);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x08);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x09);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0a);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)104);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0b);
		data.add((byte)0x06);
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)207);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0c);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0d);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0e);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)112);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x0f);
		data.add((byte)0x06);
		
		data.add(LDA_IMMEDIATE);
		data.add((byte)204);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x10);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x11);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x12);
		data.add((byte)0x06);
		data.add(LDA_IMMEDIATE);
		data.add((byte)103);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x13);
		data.add((byte)0x06);
		
		//Fill out to 256 bytes with 0xff
		//236 bytes
		data.add(LDY_IMMEDIATE);
		data.add((byte)0);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x14);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x06);
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		sub.setLabel("loop");
		data.add(STA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr);
		data.add(INY);
		data.add(CPY_IMMEDIATE);
		data.add((byte)236);
		data.add(BNE);
		sub.branchTo("loop");
		return sub;
	}
	
	private static Subroutine setupWalls()
	{
		Subroutine sub = new Subroutine("setupWalls");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x20); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x21); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//Now do left and right
		int vramAddress = 0x2041;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		//Repeat 26 times
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		vramAddress += 9;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//right
		vramAddress += 23;
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress >> 8)); //immediate - vram address high
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)(vramAddress & 0xff)); //immediate - vram address low
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x07); //immediate - tile number
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		return sub;
	}
	
	private static void writeLevelData(byte[] rom)
	{
		for (int i = 0; i < 8; ++i)
		{
			int address = 0xf700 - 0x8000 + 0x100 * i;
			int difficulty = 77 + 50 * i;
			byte[] level = generateLevel(difficulty);
			System.arraycopy(level, 0, rom, 16 + address, 256);
		}
	}
	
	private static byte[] generateLevel(int difficulty)
	{
		byte[] level = new byte[256];
		int maxSlots = 11 * 16;
		int usedSlots = 0;
		
		int total = 0;
		while (total < difficulty)
		{
			int location = Math.abs(rand.nextInt()) % maxSlots;
			if (location < 0)
			{
				continue;
			}
			
			int type = Math.abs(rand.nextInt()) % 3 + 1;
			if (type < 0)
			{
				continue;
			}
			
			int existing = level[location];
			if (type > existing)
			{
				if (existing == 0)
				{
					++usedSlots;
					if (usedSlots > 173)
					{
						--usedSlots;
						continue;
					}
				}
				
				level[location] = (byte)type;
				total += (type - existing);
			}
		}
		
		int location = Math.abs(rand.nextInt()) % maxSlots;
		while (location < 0 || level[location] != 0)
		{
			location = Math.abs(rand.nextInt()) % maxSlots;
		}
		
		level[location] = 4;
		
		location = Math.abs(rand.nextInt()) % maxSlots;
		while (location < 0 || level[location] != 0)
		{
			location = Math.abs(rand.nextInt()) % maxSlots;
		}
		
		level[location] = 5;
		
		location = Math.abs(rand.nextInt()) % maxSlots;
		while (location < 0 || level[location] != 0)
		{
			location = Math.abs(rand.nextInt()) % maxSlots;
		}
		
		level[location] = 6;
		
		return level;
	}
	
	private static void writeAngleTable(byte[] rom)
	{
		int address = 0xf600 - 0x8000 + 16;
		for (int i = 0; i < 26; ++i)
		{
			double x = Math.sin(Math.toRadians(3.0 * (i+1))) * Math.sqrt(2.0);
			double y = Math.cos(Math.toRadians(3.0 * (i+1))) * Math.sqrt(2.0);
			
			//x fraction
			//x whole
			//y fraction
			//y whole
			if (x >= 1.0)
			{
				rom[address + 1] = 1;
				x -= 1.0;
			}
			
			if (y >= 1.0)
			{
				rom[address + 3] = 1;
				y -= 1.0;
			}
			
			byte f = (byte)((int)(256 * x));
			rom[address] = f;
			
			f = (byte)((int)(256 * y));
			rom[address + 2] = f;
			
			address += 4;
		}
	}
	
	private static Subroutine writeTextLine()
	{
		Subroutine sub = new Subroutine("writeTextLine");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x42); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x20); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(BEQ);
		sub.branchTo("foundLine");
		sub.setLabel("loop");
		data.add(CLC);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x20);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr); //Incrememnt nametable pointer low byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr); //and high byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("loop");
		
		//Set video address
		sub.setLabel("foundLine");
		data.add(LDA_ZEROPAGE); //LDA
		data.add((byte)twoByteCounterHighAddr);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_ZEROPAGE); //LDA
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		
		//Figure out source address
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x02); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(BEQ);
		sub.branchTo("foundLine2");
		sub.setLabel("loop2");
		data.add(CLC);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x1c);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr); //Incrememnt source pointer low byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr); //and high byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("loop2");
		
		//Write 28 bytes
		sub.setLabel("foundLine2");
		data.add(LDY_IMMEDIATE);
		data.add((byte)0x00);
		sub.setLabel("loop3");
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr); //Get byte to copy
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20); //Write to video memory
		data.add(INY);
		data.add(CPY_IMMEDIATE);
		data.add((byte)0x1c); //Are we done?
		data.add(BNE);
		sub.branchTo("loop3");
		
		//Zero scrolls
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		
		return sub;
	}
	
	private static Subroutine writeText()
	{
		Subroutine sub = new Subroutine("writeText");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x19); //immediate
		data.add(STA_ZEROPAGE);
		data.add((byte)lineNumberAddr); //Set line to 25
		sub.setLabel("loop");
		sub.callRoutine("writeTextLine");
		data.add(DEC_ZEROPAGE);
		data.add((byte)lineNumberAddr); //Decrement line number
		data.add(BNE);
		sub.branchTo("loop");
		sub.callRoutine("writeTextLine");
		return sub;
	}
	
	private static Subroutine loadLevel()
	{
		Subroutine sub = new Subroutine("loadLevel");
		ArrayList<Byte> data = sub.getData();
		
		//paddleLeftXAddr = 2; // = 88
		data.add(LDA_IMMEDIATE);
		data.add((byte)88);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleLeftXAddr);
		//paddleRightXAddr = 3; // = 119
		data.add(LDA_IMMEDIATE);
		data.add((byte)119);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleRightXAddr);
		//paddleTopYAddr = 4; // = 208
		data.add(LDA_IMMEDIATE);
		data.add((byte)208);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleTopYAddr);
		//paddleBottomYAddr = 5; // = 215
		data.add(LDA_IMMEDIATE);
		data.add((byte)215);
		data.add(STA_ZEROPAGE);
		data.add((byte)paddleBottomYAddr);
		//ballXAddr1 = 6; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr1);
		//ballXAddr2 = 7; // = 103
		data.add(LDA_IMMEDIATE);
		data.add((byte)103);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballXAddr2);
		//ballYAddr1 = 8; // = 00
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr1);
		//ballYAddr2 = 9; // = 205
		data.add(LDA_IMMEDIATE);
		data.add((byte)205);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballYAddr2);
		//ballVectorX1Addr = 11; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX1Addr);
		//ballVectorX2Addr = 12; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorX2Addr);
		//ballVectorY1Addr = 22; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY1Addr);
		//ballVectorY2Addr = 23; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballVectorY2Addr);
		//ballFrozenAddr = 26; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenAddr);
		//ballFrozenTimerAddr = 27; // = 255
		data.add(LDA_IMMEDIATE);
		data.add((byte)0xff);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballFrozenTimerAddr);
		//deathFlagAddr
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)deathFlagAddr);
		//powerupAddr
		data.add(STA_ZEROPAGE);
		data.add((byte)powerupAddr);
		//upDownFlagAddr = 30; // = 1
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlagAddr);
		//upDownFlag2Addr = 31; // = 1
		data.add(STA_ZEROPAGE);
		data.add((byte)upDownFlag2Addr);
		//ball2XAddr1 = 32; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr1);
		//ball2XAddr2 = 33; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2XAddr2);
		//ball2YAddr1 = 34; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr1);
		//ball2YAddr2 = 35; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2YAddr2);
		//ball2VectorX1Addr = 36; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX1Addr);
		//ball2VectorX2Addr = 37; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorX2Addr);
		//ball2VectorY1Addr = 38; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY1Addr);
		//ball2VectorY2Addr = 39; // = 0
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2VectorY2Addr);
		//ball1AngleAddr = 40; // = 14
		data.add(LDA_IMMEDIATE);
		data.add((byte)14);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball1AngleAddr);
		//ball2AngleAddr = 41; // = 0
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)ball2AngleAddr);
		//leftRightFlagAddr
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlagAddr);
		//leftRightFlag2Addr
		data.add(STA_ZEROPAGE);
		data.add((byte)leftRightFlag2Addr);
		
		//Add 80 to the 2 byte ball speed field
		data.add(CLC);
		data.add(LDA_IMMEDIATE);
		data.add((byte)80);
		data.add(ADC_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballSpeed1Addr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(ADC_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		data.add(STA_ZEROPAGE);
		data.add((byte)ballSpeed2Addr);
		
		//Zero blocks remaining
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		
		sub.callRoutine("loadSprites");
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ZEROPAGE);
		data.add((byte)lineNumberAddr); //Set line to 15
		sub.setLabel("loop");
		sub.callRoutine("loadLevelLine");
		data.add(DEC_ZEROPAGE);
		data.add((byte)lineNumberAddr); //Decrement line number
		data.add(BNE);
		sub.branchTo("loop");
		sub.callRoutine("loadLevelLine");
		sub.callRoutine("oamDma");
		sub.callRoutine("copyLevelToRam");
		
		//Zero scrolls
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		
		//Write 0x1e to 0x2001
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x1e); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x01);
		data.add((byte)0x20); //absolute
		return sub;
	}
	
	private static Subroutine copyLevelToRam()
	{
		//Level goes at 0x500
		//Sprites at 0x600
		Subroutine sub = new Subroutine("copyLevelToRam");
		ArrayList<Byte> data = sub.getData();
		//Figure out source address
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xf6); //immediate
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)levelAddr); //level 1 at 0xf700
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		
		//Target address
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByte2Low); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByte2High); //zeropage
		
		//Copy
		data.add(LDY_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("loop");
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr); //Get byte
		data.add(STA_INDIRECTY);
		data.add((byte)twoByte2Low); //Copy
		data.add(INY);
		data.add(CPY_IMMEDIATE);
		data.add((byte)176);
		data.add(BNE);
		sub.branchTo("loop");
		return sub;
	}
	
	private static Subroutine loadLevelLine()
	{
		Subroutine sub = new Subroutine("loadLevelLine");
		ArrayList<Byte> data = sub.getData();
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x42); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x20); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(BEQ);
		sub.branchTo("foundLine");
		sub.setLabel("loop");
		data.add(CLC);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x20);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr); //Incrememnt nametable pointer low byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr); //and high byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("loop");
		
		//Set video address
		sub.setLabel("foundLine");
		data.add(LDA_ZEROPAGE); //LDA
		data.add((byte)twoByteCounterHighAddr);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_ZEROPAGE); //LDA
		data.add((byte)twoByteCounterLowAddr);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		
		//Figure out source address
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xf6); //immediate
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)levelAddr); //level 1 at 0xf700
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(BEQ);
		sub.branchTo("foundLine2");
		sub.setLabel("loop2");
		data.add(CLC);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x0b);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr); //Incrememnt source pointer low byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterLowAddr);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0x00);
		data.add(ADC_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr); //and high byte
		data.add(STA_ZEROPAGE);
		data.add((byte)twoByteCounterHighAddr);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("loop2");
		
		//Draw up to 11 blocks
		sub.setLabel("foundLine2");
		data.add(LDY_IMMEDIATE);
		data.add((byte)0x00);
		sub.setLabel("loop3");
		data.add(LDA_INDIRECTY);
		data.add((byte)twoByteCounterLowAddr); //Get block type
		data.add(BEQ);
		sub.branchTo("noBlock");
		data.add(CMP_IMMEDIATE);
		data.add((byte)0x04); //Is it a sprite and not BG?
		data.add(BCS);
		sub.branchTo("sprites");
		
		//Increment number of blocks in level
		data.add(INC_ZEROPAGE);
		data.add((byte)blocksRemainingInLevelAddr);
		
		//Add accumulator to itself and decrement
		data.add(STA_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)scratchAddr);
		data.add(TAX);
		data.add(DEX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20); //Write to video memory
		data.add(INX);
		data.add(STX_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20); //Write to video memory
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("nextBlock");
		
		sub.setLabel("noBlock");
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20); //Write to video memory
		data.add(INX);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x07);
		data.add((byte)0x20); //Write to video memory
		
		sub.setLabel("nextBlock");
		data.add(INY);
		data.add(CPY_IMMEDIATE);
		data.add((byte)0x0b); //Are we done?
		data.add(BNE);
		sub.branchTo("loop3");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("branchToEnd");
		
		//It's a powerup block, put the right stuff in sprite mem
		sub.setLabel("sprites");
		data.add(CMP_IMMEDIATE);
		data.add((byte)4);
		data.add(BEQ);
		sub.branchTo("block4");
		data.add(CMP_IMMEDIATE);
		data.add((byte)5);
		data.add(BEQ);
		sub.branchTo("block5");
		//It's block 6
		//OAM is at 0x600
		//First 2 slots are ball (x1) and paddle (x4)
		//So block 6 is at 0x624 - 0x62c
		//Need X and Y position
		//Y = line number * 8 + 15 (because it's Y - 1)
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(INX);
		sub.setLabel("mult8");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult8");
		data.add(ADC_IMMEDIATE);
		data.add((byte)7);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x24);
		data.add((byte)0x06); //Set y position (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x28);
		data.add((byte)0x06); //Set y position (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)6);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x25);
		data.add((byte)0x06); //Set tile number (left)
		data.add(LDA_IMMEDIATE);
		data.add((byte)7);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x29);
		data.add((byte)0x06); //Set tile number (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x26);
		data.add((byte)0x06); //Set palette (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2a);
		data.add((byte)0x06); //Set palette (right)
		//X = Yreg * 16 + 16
		data.add(TYA);
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("mult16");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)16);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult16");
		data.add(STA_ABSOLUTE);
		data.add((byte)0x27);
		data.add((byte)0x06); //Set X position (left)
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x2b);
		data.add((byte)0x06); //Set X position (right)
		data.add(CLC);
		sub.setLabel("backToNextBlock2");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(BCC);
		sub.branchTo("noBlock");
		
		sub.setLabel("branchToEnd");
		data.add(BCC);
		sub.branchTo("end");
		
		//It's block 5
		//OAM is at 0x600
		//First 2 slots are ball (x1) and paddle (x4)
		//So block 5 is at 0x621c - 0x624
		//Need X and Y position
		//Y = line number * 8 + 15 (because it's Y - 1)
		sub.setLabel("block5");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(INX);
		sub.setLabel("mult82");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult82");
		data.add(ADC_IMMEDIATE);
		data.add((byte)7);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1c);
		data.add((byte)0x06); //Set y position (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x20);
		data.add((byte)0x06); //Set y position (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)4);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1d);
		data.add((byte)0x06); //Set tile number (left)
		data.add(LDA_IMMEDIATE);
		data.add((byte)5);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x21);
		data.add((byte)0x06); //Set tile number (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1e);
		data.add((byte)0x06); //Set palette (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x22);
		data.add((byte)0x06); //Set palette (right)
		//X = Yreg * 16 + 16
		data.add(TYA);
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("mult162");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)16);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult162");
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1f);
		data.add((byte)0x06); //Set X position (left)
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x23);
		data.add((byte)0x06); //Set X position (right)
		data.add(CLC);
		sub.setLabel("backToNextBlock");
		data.add(BCC);
		sub.branchTo("backToNextBlock2");
		
		//It's block 4
		//OAM is at 0x600
		//First 2 slots are ball (x1) and paddle (x4)
		//So block 5 is at 0x614 - 0x61c
		//Need X and Y position
		//Y = line number * 8 + 15 (because it's Y - 1)
		sub.setLabel("block4");
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(LDX_ZEROPAGE);
		data.add((byte)lineNumberAddr);
		data.add(INX);
		sub.setLabel("mult83");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult83");
		data.add(ADC_IMMEDIATE);
		data.add((byte)7);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x14);
		data.add((byte)0x06); //Set y position (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x18);
		data.add((byte)0x06); //Set y position (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)2);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x15);
		data.add((byte)0x06); //Set tile number (left)
		data.add(LDA_IMMEDIATE);
		data.add((byte)3);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x19);
		data.add((byte)0x06); //Set tile number (right)
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x16);
		data.add((byte)0x06); //Set palette (left)
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1a);
		data.add((byte)0x06); //Set palette (right)
		//X = Yreg * 16 + 16
		data.add(TYA);
		data.add(TAX);
		data.add(INX);
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		sub.setLabel("mult163");
		data.add(CLC);
		data.add(ADC_IMMEDIATE);
		data.add((byte)16);
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("mult163");
		data.add(STA_ABSOLUTE);
		data.add((byte)0x17);
		data.add((byte)0x06); //Set X position (left)
		data.add(ADC_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ABSOLUTE);
		data.add((byte)0x1b);
		data.add((byte)0x06); //Set X position (right)
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("backToNextBlock");
		sub.setLabel("end");
		data.add(CLC);
		return sub;
	}
	
	private static Subroutine setupTitleScreen()
	{
		Subroutine sub = new Subroutine("setupTitleScreen");
		sub.callRoutine("setAllPalette1");
		sub.callRoutine("clearTextMemory"); //set to 0x20
		//0x200 - 0x500 is text data
		//There are 26 possible lines
		//Each line can hold 28 possible characters
		//We want text on lines 7, 8, 12 (counting from 1)
		generateCenteredText(sub, 6, "TIME WASTER:");
		generateCenteredText(sub, 7, "THE GAME");
		generateCenteredText(sub, 11, "PRESS START");
		sub.callRoutine("writeText");
		return sub;
	}
	
	private static Subroutine beatGameScreen()
	{
		Subroutine sub = new Subroutine("beatGameScreen");
		sub.callRoutine("setAllPalette1");
		sub.callRoutine("clearTextMemory"); //set to 0x20
		//0x200 - 0x500 is text data
		//There are 26 possible lines
		//Each line can hold 28 possible characters
		generateCenteredText(sub, 6, "AS WE ALL KNOW");
		generateCenteredText(sub, 7, "ALL GOOD GAMES MUST END");
		generateCenteredText(sub, 11, "AND YOU MADE IT");
		generateCenteredText(sub, 12, "TO THE END OF THIS ONE");
		sub.callRoutine("writeText");
		return sub;
	}
	
	private static Subroutine clearTextMemory()
	{
		Subroutine sub = new Subroutine("clearTextMemory");
		generateCenteredText(sub, 0, "                            ");
		generateCenteredText(sub, 1, "                            ");
		generateCenteredText(sub, 2, "                            ");
		generateCenteredText(sub, 3, "                            ");
		generateCenteredText(sub, 4, "                            ");
		generateCenteredText(sub, 5, "                            ");
		generateCenteredText(sub, 6, "                            ");
		generateCenteredText(sub, 7, "                            ");
		generateCenteredText(sub, 8, "                            ");
		generateCenteredText(sub, 9, "                            ");
		generateCenteredText(sub, 10, "                            ");
		generateCenteredText(sub, 11, "                            ");
		generateCenteredText(sub, 12, "                            ");
		generateCenteredText(sub, 13, "                            ");
		generateCenteredText(sub, 14, "                            ");
		generateCenteredText(sub, 15, "                            ");
		generateCenteredText(sub, 16, "                            ");
		generateCenteredText(sub, 17, "                            ");
		generateCenteredText(sub, 18, "                            ");
		generateCenteredText(sub, 19, "                            ");
		generateCenteredText(sub, 20, "                            ");
		generateCenteredText(sub, 21, "                            ");
		generateCenteredText(sub, 22, "                            ");
		generateCenteredText(sub, 23, "                            ");
		generateCenteredText(sub, 24, "                            ");
		generateCenteredText(sub, 25, "                            ");
		return sub;
	}
	
	private static Subroutine setupLevelScreen()
	{
		Subroutine sub = new Subroutine("setupLevelScreen");
		sub.callRoutine("setAllPalette1");
		sub.callRoutine("clearTextMemory");
		int nextOffsetInLine = generateCenteredText(sub, 12, "LEVEL ");
		
		int address = 0x200 + 12 * 28 + nextOffsetInLine;
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate - character 0
		data.add(CLC);
		data.add(ADC_ZEROPAGE);
		data.add((byte)levelAddr);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)(address & 0xff));
		data.add((byte)(address >> 8)); //absolute
		sub.callRoutine("writeText");
		return sub;
	}
	
	private static Subroutine setupGameOverScreen()
	{
		Subroutine sub = new Subroutine("setupGameOverScreen");
		sub.callRoutine("setAllPalette1");
		sub.callRoutine("clearTextMemory");
		generateCenteredText(sub, 12, "SO SAD. YOU LOSE.");
		sub.callRoutine("writeText");
		return sub;
	}
	
	private static int generateCenteredText(Subroutine sub, int line, String text)
	{
		ArrayList<Byte> data = sub.getData();
		int offset = (28 - text.length()) / 2;
		
		for (char c : text.toCharArray())
		{
			byte b = (byte)c;
			int address = 0x200 + line * 28 + offset++;
			data.add(LDA_IMMEDIATE); //LDA
			data.add(b); //immediate
			data.add(STA_ABSOLUTE); //STA
			data.add((byte)(address & 0xff));
			data.add((byte)(address >> 8)); //absolute
		}
		
		return offset;
	}
	
	private static Subroutine waitForStart()
	{
		Subroutine sub = new Subroutine("waitForStart");
		ArrayList<Byte> data = sub.getData();
		sub.setLabel("loop");
		sub.callRoutine("readController");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x10); //immediate - mask for start
		data.add(AND_ZEROPAGE); 
		data.add((byte)buttonsAddr); //zeropage
		data.add(BEQ);
		sub.branchTo("loop");
		return sub;
	}
	
	private static Subroutine readController()
	{
		Subroutine sub = new Subroutine("readController");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x01); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x16);
		data.add((byte)0x40); //absolute - strobe the controller
		data.add(STA_ZEROPAGE);
		data.add((byte)buttonsAddr); //Will be used as flag that we've read all buttons
		data.add(LSR_ACCUMULATOR); //A is now 0
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x16);
		data.add((byte)0x40); //absolute - set controller for reading
		sub.setLabel("loop");
		data.add(LDA_ABSOLUTE); //LDA
		data.add((byte)0x16);
		data.add((byte)0x40); //read button
		data.add(LSR_ACCUMULATOR); //bit 0 (button status) -> carry
		data.add(ROL_ZEROPAGE);
		data.add((byte)buttonsAddr); //shift data into buttons and check loop flag
		data.add(BCC);
		sub.branchTo("loop");
		return sub;
	}
	
	private static Subroutine doWait()
	{
		Subroutine sub = new Subroutine("wait");
		ArrayList<Byte> data = sub.getData();
		sub.setLabel("loop");
		sub.callRoutine("waitForNmi");
		data.add(DEC_ZEROPAGE); //DEC
		data.add((byte)timerAddr); //zeropage
		data.add(BNE); //BNE
		sub.branchTo("loop");
		
		return sub;
	}
	
	private static Subroutine nmi()
	{
		Subroutine sub = new Subroutine("NMI");
		ArrayList<Byte> data = sub.getData();
		data.add(PHP);
		data.add(PHA);
		
		//Zero scrolls
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
				
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x01); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)nmiFlagAddr); //zeropage
		
		data.add(PLA);
		data.add(PLP);
		data.add(RTI);
		return sub;
	}
	
	private static Subroutine waitForNmi()
	{
		Subroutine sub = new Subroutine("waitForNmi");
		ArrayList<Byte> data = sub.getData();
		//Zero scrolls
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
				
		sub.setLabel("loop");
		data.add(LDA_ZEROPAGE); //LDA
		data.add((byte)nmiFlagAddr); //zeropage
		data.add(BEQ); //BEQ
		sub.branchTo("loop");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)nmiFlagAddr); //zeropage
		return sub;
	}
	
	private static Subroutine irq()
	{
		Subroutine sub = new Subroutine("IRQ");
		ArrayList<Byte> data = sub.getData();
		data.add(RTI); //RTI
		return sub;
	}
	
	private static Subroutine disableVideo()
	{
		Subroutine sub = new Subroutine("disableVideo");
		ArrayList<Byte> data = sub.getData();
		
		//Write 0x06 to 0x2001
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x06); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x01);
		data.add((byte)0x20); //absolute
		return sub;
	}
	
	private static Subroutine enableVideo()
	{
		Subroutine sub = new Subroutine("enableVideo");
		ArrayList<Byte> data = sub.getData();
		
		sub.callRoutine("waitForNmi");

		//Zero scrolls
		data.add(LDA_IMMEDIATE);
		data.add((byte)0);
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x05);
		data.add((byte)0x20); //absolute
		
		//Write 0x0e to 0x2001
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0e); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x01);
		data.add((byte)0x20); //absolute
		return sub;
	}
	
	private static Subroutine doMain()
	{
		Subroutine sub = new Subroutine("main");
		ArrayList<Byte> data = sub.getData();
		
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupTitleScreen");
		
		//Enable NMI
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x88); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x00); 
		data.add((byte)0x20); //Absolute
		sub.callRoutine("enableVideo");
		
		sub.callRoutine("waitForStart");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)1);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		
		sub.callRoutine("loadLevel");
		
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont1");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont1");
		
		//Level 2
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)2);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont2");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont2");
		
		//Level 3
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)3);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont3");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont3");
		
		//Level 4
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)4);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont4");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont4");
		
		//Level 5
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)5);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont5");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont5");
		
		//Level 6
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)6);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont6");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont6");
		
		//Level 7
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)7);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont7");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont7");
		
		//Level 8
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		
		//Set level
		data.add(LDA_IMMEDIATE);
		data.add((byte)8);
		data.add(STA_ZEROPAGE);
		data.add((byte)levelAddr);
		sub.callRoutine("setupLevelScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupWalls");
		sub.callRoutine("loadLevel");
		sub.callRoutine("playLevel");
		data.add(LDA_ZEROPAGE);
		data.add((byte)livesAddr);
		data.add(BNE);
		sub.branchTo("cont8");
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("setupGameOverScreen");
		sub.callRoutine("enableVideo");
		
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		
		sub.callRoutine("wait");
		sub.jump("RESET");
		sub.setLabel("cont8");
		
		sub.callRoutine("disableVideo");
		sub.callRoutine("blankNametable");
		sub.callRoutine("beatGameScreen");
		sub.callRoutine("enableVideo");
		
		sub.setLabel("infinite");
		//Load 255 for timer
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)timerAddr); //zeropage
		sub.callRoutine("wait");
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("infinite");
		
		return sub;
	}
	
	private static void writePrgRom(byte[] rom, ArrayList<Subroutine> subroutines)
	{
		for (Subroutine subroutine : subroutines)
		{
			int address = 16 + subroutine.getAddress() - 0x8000;
			for (int i = 0; i < subroutine.getData().size(); ++i)
			{
				rom[address++] = subroutine.getData().get(i);
			}
		}
	}
	
	private static void writeHeader(byte[] rom)
	{
		rom[0] = 0x4e; 
		rom[1] = 0x45;
		rom[2] = 0x53;
		rom[3] = 0x1a;
		rom[4] = 0x02; //32KB PRG-ROM
		rom[5] = 0x01; //8KB CHR-ROM
		rom[6] = 0x01; //vertically mirrored
		rom[7] = 0x00; //No mapper
		rom[8] = 0x00;
		rom[9] = 0x00;
		rom[10] = 0x00;
		rom[11] = 0x00;
		rom[12] = 0x00;
		rom[13] = 0x00;
		rom[14] = 0x00;
		rom[15] = 0x00;
	}
	
	private static void writeChrRom(byte[] rom, String filename) throws Exception
	{
		File file = new File(filename);
        FileInputStream fl = new FileInputStream(file); 
        int length = (int)file.length();
        byte[] arr = new byte[length]; 
        fl.read(arr); 
        fl.close(); 
        
        System.arraycopy(arr, 0, rom, 16 + 32768, length);
        
        //3 main colors of blocks
        //1 - left half block 1
        rom[16 + 32768 + 16] = (byte)0x00;
        rom[16 + 32768 + 17] = (byte)0x7f;
        rom[16 + 32768 + 18] = (byte)0x7f;
        rom[16 + 32768 + 19] = (byte)0x7f;
        rom[16 + 32768 + 20] = (byte)0x7f;
        rom[16 + 32768 + 21] = (byte)0x7f;
        rom[16 + 32768 + 22] = (byte)0x7f;
        rom[16 + 32768 + 23] = (byte)0x00;
        rom[16 + 32768 + 24] = (byte)0x00;
        rom[16 + 32768 + 25] = (byte)0x00;
        rom[16 + 32768 + 26] = (byte)0x00;
        rom[16 + 32768 + 27] = (byte)0x00;
        rom[16 + 32768 + 28] = (byte)0x00;
        rom[16 + 32768 + 29] = (byte)0x00;
        rom[16 + 32768 + 30] = (byte)0x00;
        rom[16 + 32768 + 31] = (byte)0x00;
        
        //2  - right half block 1
        rom[16 + 32768 + 32] = (byte)0x00;
        rom[16 + 32768 + 33] = (byte)0xfe;
        rom[16 + 32768 + 34] = (byte)0xfe;
        rom[16 + 32768 + 35] = (byte)0xfe;
        rom[16 + 32768 + 36] = (byte)0xfe;
        rom[16 + 32768 + 37] = (byte)0xfe;
        rom[16 + 32768 + 38] = (byte)0xfe;
        rom[16 + 32768 + 39] = (byte)0x00;
        rom[16 + 32768 + 40] = (byte)0x00;
        rom[16 + 32768 + 41] = (byte)0x00;
        rom[16 + 32768 + 42] = (byte)0x00;
        rom[16 + 32768 + 43] = (byte)0x00;
        rom[16 + 32768 + 44] = (byte)0x00;
        rom[16 + 32768 + 45] = (byte)0x00;
        rom[16 + 32768 + 46] = (byte)0x00;
        rom[16 + 32768 + 47] = (byte)0x00;
        
        //3 - left half block 2
        rom[16 + 32768 + 48] = (byte)0x00;
        rom[16 + 32768 + 49] = (byte)0x00;
        rom[16 + 32768 + 50] = (byte)0x00;
        rom[16 + 32768 + 51] = (byte)0x00;
        rom[16 + 32768 + 52] = (byte)0x00;
        rom[16 + 32768 + 53] = (byte)0x00;
        rom[16 + 32768 + 54] = (byte)0x00;
        rom[16 + 32768 + 55] = (byte)0x00;
        rom[16 + 32768 + 56] = (byte)0x00;
        rom[16 + 32768 + 57] = (byte)0x7f;
        rom[16 + 32768 + 58] = (byte)0x7f;
        rom[16 + 32768 + 59] = (byte)0x7f;
        rom[16 + 32768 + 60] = (byte)0x7f;
        rom[16 + 32768 + 61] = (byte)0x7f;
        rom[16 + 32768 + 62] = (byte)0x7f;
        rom[16 + 32768 + 63] = (byte)0x00;
        
        //4  - right half block 2
        rom[16 + 32768 + 64] = (byte)0x00;
        rom[16 + 32768 + 65] = (byte)0x00;
        rom[16 + 32768 + 66] = (byte)0x00;
        rom[16 + 32768 + 67] = (byte)0x00;
        rom[16 + 32768 + 68] = (byte)0x00;
        rom[16 + 32768 + 69] = (byte)0x00;
        rom[16 + 32768 + 70] = (byte)0x00;
        rom[16 + 32768 + 71] = (byte)0x00;
        rom[16 + 32768 + 72] = (byte)0x00;
        rom[16 + 32768 + 73] = (byte)0xfe;
        rom[16 + 32768 + 74] = (byte)0xfe;
        rom[16 + 32768 + 75] = (byte)0xfe;
        rom[16 + 32768 + 76] = (byte)0xfe;
        rom[16 + 32768 + 77] = (byte)0xfe;
        rom[16 + 32768 + 78] = (byte)0xfe;
        rom[16 + 32768 + 79] = (byte)0x00;
        
        //5 - left half block 3
        rom[16 + 32768 + 80] = (byte)0x00;
        rom[16 + 32768 + 81] = (byte)0x7f;
        rom[16 + 32768 + 82] = (byte)0x7f;
        rom[16 + 32768 + 83] = (byte)0x7f;
        rom[16 + 32768 + 84] = (byte)0x7f;
        rom[16 + 32768 + 85] = (byte)0x7f;
        rom[16 + 32768 + 86] = (byte)0x7f;
        rom[16 + 32768 + 87] = (byte)0x00;
        rom[16 + 32768 + 88] = (byte)0x00;
        rom[16 + 32768 + 89] = (byte)0x7f;
        rom[16 + 32768 + 90] = (byte)0x7f;
        rom[16 + 32768 + 91] = (byte)0x7f;
        rom[16 + 32768 + 92] = (byte)0x7f;
        rom[16 + 32768 + 93] = (byte)0x7f;
        rom[16 + 32768 + 94] = (byte)0x7f;
        rom[16 + 32768 + 95] = (byte)0x00;
        
        //6  - right half block 3
        rom[16 + 32768 + 96] = (byte)0x00;
        rom[16 + 32768 + 97] = (byte)0xfe;
        rom[16 + 32768 + 98] = (byte)0xfe;
        rom[16 + 32768 + 99] = (byte)0xfe;
        rom[16 + 32768 + 100] = (byte)0xfe;
        rom[16 + 32768 + 101] = (byte)0xfe;
        rom[16 + 32768 + 102] = (byte)0xfe;
        rom[16 + 32768 + 103] = (byte)0x00;
        rom[16 + 32768 + 104] = (byte)0x00;
        rom[16 + 32768 + 105] = (byte)0xfe;
        rom[16 + 32768 + 106] = (byte)0xfe;
        rom[16 + 32768 + 107] = (byte)0xfe;
        rom[16 + 32768 + 108] = (byte)0xfe;
        rom[16 + 32768 + 109] = (byte)0xfe;
        rom[16 + 32768 + 110] = (byte)0xfe;
        rom[16 + 32768 + 111] = (byte)0x00;
        
        //7 - wall sections - intended for use with palette 1
        rom[16 + 32768 + 112] = (byte)0xff;
        rom[16 + 32768 + 113] = (byte)0xff;
        rom[16 + 32768 + 114] = (byte)0xff;
        rom[16 + 32768 + 115] = (byte)0xff;
        rom[16 + 32768 + 116] = (byte)0xff;
        rom[16 + 32768 + 117] = (byte)0xff;
        rom[16 + 32768 + 118] = (byte)0xff;
        rom[16 + 32768 + 119] = (byte)0xff;
        rom[16 + 32768 + 120] = (byte)0x00;
        rom[16 + 32768 + 121] = (byte)0x00;
        rom[16 + 32768 + 122] = (byte)0x00;
        rom[16 + 32768 + 123] = (byte)0x00;
        rom[16 + 32768 + 124] = (byte)0x00;
        rom[16 + 32768 + 125] = (byte)0x00;
        rom[16 + 32768 + 126] = (byte)0x00;
        rom[16 + 32768 + 127] = (byte)0x00;
        
        //In sprite pattern table
        //0 - First comes the paddle - it's just 4 of these in a row - needs palette 1
        rom[16 + 32768 + 4096] = (byte)0x00;
        rom[16 + 32768 + 4097] = (byte)0x00;
        rom[16 + 32768 + 4098] = (byte)0x00;
        rom[16 + 32768 + 4099] = (byte)0x00;
        rom[16 + 32768 + 4100] = (byte)0x00;
        rom[16 + 32768 + 4101] = (byte)0x00;
        rom[16 + 32768 + 4102] = (byte)0x00;
        rom[16 + 32768 + 4103] = (byte)0x00;
        rom[16 + 32768 + 4104] = (byte)0xff;
        rom[16 + 32768 + 4105] = (byte)0xff;
        rom[16 + 32768 + 4106] = (byte)0xff;
        rom[16 + 32768 + 4107] = (byte)0xff;
        rom[16 + 32768 + 4108] = (byte)0xff;
        rom[16 + 32768 + 4109] = (byte)0xff;
        rom[16 + 32768 + 4110] = (byte)0x00;
        rom[16 + 32768 + 4111] = (byte)0x00;
        
        //1 - Then the ball - also palette 1
        rom[16 + 32768 + 4112] = (byte)0x00;
        rom[16 + 32768 + 4113] = (byte)0x00;
        rom[16 + 32768 + 4114] = (byte)0x00;
        rom[16 + 32768 + 4115] = (byte)0x00;
        rom[16 + 32768 + 4116] = (byte)0x00;
        rom[16 + 32768 + 4117] = (byte)0x00;
        rom[16 + 32768 + 4118] = (byte)0x00;
        rom[16 + 32768 + 4119] = (byte)0x00;
        rom[16 + 32768 + 4120] = (byte)0xe0;
        rom[16 + 32768 + 4121] = (byte)0xe0;
        rom[16 + 32768 + 4122] = (byte)0xe0;
        rom[16 + 32768 + 4123] = (byte)0x00;
        rom[16 + 32768 + 4124] = (byte)0x00;
        rom[16 + 32768 + 4125] = (byte)0x00;
        rom[16 + 32768 + 4126] = (byte)0x00;
        rom[16 + 32768 + 4127] = (byte)0x00;
        
        //Then the 4 special color blocks - palette 0 
        //2 - left half special block 1/4
        rom[16 + 32768 + 4128] = (byte)0x00;
        rom[16 + 32768 + 4129] = (byte)0x7f;
        rom[16 + 32768 + 4130] = (byte)0x7f;
        rom[16 + 32768 + 4131] = (byte)0x7f;
        rom[16 + 32768 + 4132] = (byte)0x7f;
        rom[16 + 32768 + 4133] = (byte)0x7f;
        rom[16 + 32768 + 4134] = (byte)0x7f;
        rom[16 + 32768 + 4135] = (byte)0x00;
        rom[16 + 32768 + 4136] = (byte)0x00;
        rom[16 + 32768 + 4137] = (byte)0x00;
        rom[16 + 32768 + 4138] = (byte)0x00;
        rom[16 + 32768 + 4139] = (byte)0x00;
        rom[16 + 32768 + 4140] = (byte)0x00;
        rom[16 + 32768 + 4141] = (byte)0x00;
        rom[16 + 32768 + 4142] = (byte)0x00;
        rom[16 + 32768 + 4143] = (byte)0x00;
        
        //3  - right half special block 1/4
        rom[16 + 32768 + 4144] = (byte)0x00;
        rom[16 + 32768 + 4145] = (byte)0xfe;
        rom[16 + 32768 + 4146] = (byte)0xfe;
        rom[16 + 32768 + 4147] = (byte)0xfe;
        rom[16 + 32768 + 4148] = (byte)0xfe;
        rom[16 + 32768 + 4149] = (byte)0xfe;
        rom[16 + 32768 + 4150] = (byte)0xfe;
        rom[16 + 32768 + 4151] = (byte)0x00;
        rom[16 + 32768 + 4152] = (byte)0x00;
        rom[16 + 32768 + 4153] = (byte)0x00;
        rom[16 + 32768 + 4154] = (byte)0x00;
        rom[16 + 32768 + 4155] = (byte)0x00;
        rom[16 + 32768 + 4156] = (byte)0x00;
        rom[16 + 32768 + 4157] = (byte)0x00;
        rom[16 + 32768 + 4158] = (byte)0x00;
        rom[16 + 32768 + 4159] = (byte)0x00;
        
        //4 - left half special block 2
        rom[16 + 32768 + 4160] = (byte)0x00;
        rom[16 + 32768 + 4161] = (byte)0x00;
        rom[16 + 32768 + 4162] = (byte)0x00;
        rom[16 + 32768 + 4163] = (byte)0x00;
        rom[16 + 32768 + 4164] = (byte)0x00;
        rom[16 + 32768 + 4165] = (byte)0x00;
        rom[16 + 32768 + 4166] = (byte)0x00;
        rom[16 + 32768 + 4167] = (byte)0x00;
        rom[16 + 32768 + 4168] = (byte)0x00;
        rom[16 + 32768 + 4169] = (byte)0x7f;
        rom[16 + 32768 + 4170] = (byte)0x7f;
        rom[16 + 32768 + 4171] = (byte)0x7f;
        rom[16 + 32768 + 4172] = (byte)0x7f;
        rom[16 + 32768 + 4173] = (byte)0x7f;
        rom[16 + 32768 + 4174] = (byte)0x7f;
        rom[16 + 32768 + 4175] = (byte)0x00;
        
        //5  - right half special block 2
        rom[16 + 32768 + 4176] = (byte)0x00;
        rom[16 + 32768 + 4177] = (byte)0x00;
        rom[16 + 32768 + 4178] = (byte)0x00;
        rom[16 + 32768 + 4179] = (byte)0x00;
        rom[16 + 32768 + 4180] = (byte)0x00;
        rom[16 + 32768 + 4181] = (byte)0x00;
        rom[16 + 32768 + 4182] = (byte)0x00;
        rom[16 + 32768 + 4183] = (byte)0x00;
        rom[16 + 32768 + 4184] = (byte)0x00;
        rom[16 + 32768 + 4185] = (byte)0xfe;
        rom[16 + 32768 + 4186] = (byte)0xfe;
        rom[16 + 32768 + 4187] = (byte)0xfe;
        rom[16 + 32768 + 4188] = (byte)0xfe;
        rom[16 + 32768 + 4189] = (byte)0xfe;
        rom[16 + 32768 + 4190] = (byte)0xfe;
        rom[16 + 32768 + 4191] = (byte)0x00;
        
        //6 - left half special block 3
        rom[16 + 32768 + 4192] = (byte)0x00;
        rom[16 + 32768 + 4193] = (byte)0x7f;
        rom[16 + 32768 + 4194] = (byte)0x7f;
        rom[16 + 32768 + 4195] = (byte)0x7f;
        rom[16 + 32768 + 4196] = (byte)0x7f;
        rom[16 + 32768 + 4197] = (byte)0x7f;
        rom[16 + 32768 + 4198] = (byte)0x7f;
        rom[16 + 32768 + 4199] = (byte)0x00;
        rom[16 + 32768 + 4200] = (byte)0x00;
        rom[16 + 32768 + 4201] = (byte)0x7f;
        rom[16 + 32768 + 4202] = (byte)0x7f;
        rom[16 + 32768 + 4203] = (byte)0x7f;
        rom[16 + 32768 + 4204] = (byte)0x7f;
        rom[16 + 32768 + 4205] = (byte)0x7f;
        rom[16 + 32768 + 4206] = (byte)0x7f;
        rom[16 + 32768 + 4207] = (byte)0x00;
        
        //7  - right half special block 3
        rom[16 + 32768 + 4208] = (byte)0x00;
        rom[16 + 32768 + 4209] = (byte)0xfe;
        rom[16 + 32768 + 4210] = (byte)0xfe;
        rom[16 + 32768 + 4211] = (byte)0xfe;
        rom[16 + 32768 + 4212] = (byte)0xfe;
        rom[16 + 32768 + 4213] = (byte)0xfe;
        rom[16 + 32768 + 4214] = (byte)0xfe;
        rom[16 + 32768 + 4215] = (byte)0x00;
        rom[16 + 32768 + 4216] = (byte)0x00;
        rom[16 + 32768 + 4217] = (byte)0xfe;
        rom[16 + 32768 + 4218] = (byte)0xfe;
        rom[16 + 32768 + 4219] = (byte)0xfe;
        rom[16 + 32768 + 4220] = (byte)0xfe;
        rom[16 + 32768 + 4221] = (byte)0xfe;
        rom[16 + 32768 + 4222] = (byte)0xfe;
        rom[16 + 32768 + 4223] = (byte)0x00;
	}
	
	private static void fixUpInterrupts(byte[] rom, ArrayList<Subroutine> subroutines)
	{
		for (Subroutine subroutine : subroutines)
		{
			if (subroutine.getName().equals("RESET"))
			{
				int address = subroutine.getAddress();
				rom[16 + 0xfffc - 0x8000] = (byte)(address & 0xff);
				rom[16 + 0xfffd - 0x8000] = (byte)(address >> 8);
			} else if (subroutine.getName().equals("IRQ"))
			{
				int address = subroutine.getAddress();
				rom[16 + 0xfffe - 0x8000] = (byte)(address & 0xff);
				rom[16 + 0xffff - 0x8000] = (byte)(address >> 8);
			} else if (subroutine.getName().equals("NMI"))
			{
				int address = subroutine.getAddress();
				rom[16 + 0xfffa - 0x8000] = (byte)(address & 0xff);
				rom[16 + 0xfffb - 0x8000] = (byte)(address >> 8);
			}
		}
	}
	
	private static int assignAddresses(ArrayList<Subroutine> subroutines)
	{
		int address = 0x8000;
		for (Subroutine subroutine : subroutines)
		{
			subroutine.setAddress(address);
			address += subroutine.getData().size();
		}
		
		return address;
	}
	
	private static void localRelocations(ArrayList<Subroutine> subroutines)
	{
		for (Subroutine subroutine : subroutines)
		{
			subroutine.doLocalRelocations();
		}
	}
	
	private static void fixUpRelocations(ArrayList<Subroutine> subroutines)
	{
		HashMap<String, Integer> labelToAddress = new HashMap<String, Integer>();
		for (Subroutine subroutine : subroutines)
		{
			labelToAddress.put(subroutine.getName(), subroutine.getAddress());
			System.out.println(subroutine.getName() + " - " + String.format("0x%04X", subroutine.getAddress()));
		}
		
		for (Subroutine subroutine : subroutines)
		{
			ArrayList<String> labels = subroutine.getReferenceLabels();
			ArrayList<Integer> offsets = subroutine.getReferenceOffsets();
			for (int i = 0; i < labels.size(); ++i)
			{
				Integer addressObj = labelToAddress.get(labels.get(i));
				if (addressObj == null)
				{
					System.out.println("Could not find external label " + labels.get(i));
					System.exit(-1);
				}
				
				int address = addressObj;
				subroutine.getData().set(offsets.get(i), (byte)(address & 0xff));
				subroutine.getData().set(offsets.get(i) + 1, (byte)(address >> 8));
			}
		}
	}
	
	private static void writeEpilogues(ArrayList<Subroutine> subroutines)
	{
		for (Subroutine subroutine : subroutines)
		{
			subroutine.writeEpilogue();
		}
	}
	
	private static Subroutine reset()
	{
		Subroutine sub = new Subroutine("RESET");
		ArrayList<Byte> data = sub.getData();
		
		//Disable NMI
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x00); 
		data.add((byte)0x20); //Absolute
		
		//Disable IRQ
		data.add(SEI); //SEI
		
		//Disable video
		sub.callRoutine("disableVideo");
		
		//Reset latch
		data.add(LDA_ABSOLUTE);
		data.add((byte)0x02); 
		data.add((byte)0x20); //Absolute
		
		//Set black background
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x3f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//Set the rest of the palette for the background
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x11); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x16); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x1a); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//Fill the rest with white
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//Set palette for sprites
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x3f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x11); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x14); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x21); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x24); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x0f); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x27); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x30); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//Set stack pointer
		data.add(LDX_IMMEDIATE);
		data.add((byte)0xff);
		data.add(TXS);
		
		sub.callRoutine("initGameVariables");
		
		//jump main
		sub.jump("main");
		return sub;
	}
	
	private static Subroutine setAllPalette1()
	{
		Subroutine sub = new Subroutine("setAllPalette1");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x23); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xc0); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xff); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		return sub;
	}
	
	private static Subroutine blankNametable()
	{
		Subroutine sub = new Subroutine("blankNametable");
		ArrayList<Byte> data = sub.getData();
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0xc0); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x03); //immediate
		data.add(STA_ZEROPAGE); //STA
		data.add((byte)twoByteCounterHighAddr); //zeropage
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x20); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x06);
		data.add((byte)0x20); //absolute
		sub.setLabel("loop");
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		//DEC low addr and loop to STA $2007 if not zero
		data.add(DEC_ZEROPAGE); //DEC
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(BNE); //BNE
		sub.branchTo("loop");
		
		//if zero and high is zero -> move on to attribute bytes
		data.add(LDX_ZEROPAGE); //LDX
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(BEQ); //BEQ
		sub.branchTo("attribute");
		
		//else run once, dec low addr and dec high addr and loop to STA $2007
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(DEC_ZEROPAGE); //DEC
		data.add((byte)twoByteCounterLowAddr); //zeropage
		data.add(DEC_ZEROPAGE); //DEC
		data.add((byte)twoByteCounterHighAddr); //zeropage
		data.add(CLC);
		data.add(BCC);
		sub.branchTo("loop");
		
		//Now we need to set attribute bytes
		//0x3f 0x0f x 6 0xcf
		sub.setLabel("attribute");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x15); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x55); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x55); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		data.add(LDX_IMMEDIATE);
		data.add((byte)0x06);
		sub.setLabel("loop2");
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x11); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x55); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x55); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		data.add(DEX);
		data.add(BNE);
		sub.branchTo("loop2");
		
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x01); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x00); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		data.add(LDA_IMMEDIATE); //LDA
		data.add((byte)0x05); //immediate
		data.add(STA_ABSOLUTE); //STA
		data.add((byte)0x07);
		data.add((byte)0x20); //absolute
		
		return sub;
	}
	
}
