//Ye old 6502 without BCD support

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class CPU implements Runnable {
	private Clock clock;
	private Memory mem;
	private volatile int pc;
	private long cycle;
	private volatile int p;
	private volatile int a;
	private volatile int x;
	private volatile int y;
	private volatile int s;
	private AtomicBoolean debugHold;
	private AtomicBoolean terminate;
	private AtomicInteger breakAddress;
	private AtomicBoolean nmi;
	private AtomicBoolean reset;
	private AtomicBoolean stepFlag;
	private AtomicBoolean irq;
	private PPU ppu;
	private PrintStream log;
	private volatile PrintStream out;
	private long overage = 0;
	private boolean inNmi = false;
	private GUI gui;
	private APU apu;
	
	public static volatile boolean LOG = false;
	
	enum AddressingMode {
		ZEROPAGE,
		ABSOLUTE,
		INDIRECTX,
		INDIRECTY,
		IMMEDIATE,
		ACCUMULATOR,
		ZEROPAGEX,
		ABSOLUTEX,
		ABSOLUTEY,
		ABSOLUTEX_WRITE,
		ABSOLUTEY_WRITE,
		INDIRECTY_WRITE,
		IMPLIED,
		RELATIVE,
		INDIRECT,
		ZEROPAGEY,
	}
	
	public CPU(Clock clock, Memory mem, PPU ppu, GUI gui)
	{
		this.gui = gui;
		debugHold = new AtomicBoolean(false);
		terminate = new AtomicBoolean(false);
		breakAddress = new AtomicInteger(0x10000);
		reset = new AtomicBoolean(false);
		nmi = new AtomicBoolean(false);
		stepFlag = new AtomicBoolean(false);
		irq = new AtomicBoolean(false);
		this.clock = clock;
		this.mem = mem;
		this.ppu = ppu;
		p = 0x34;
		a = x = y = s = 0;
		
		try {
			log = new PrintStream("log");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setApu(APU apu)
	{
		this.apu = apu;
	}
	
	public APU getApu()
	{
		return apu;
	}
	
	public Memory getMem()
	{
		return mem;
	}
	
	public void setNmi()
	{
		nmi.set(true);
	}
	
	public void setReset()
	{
		reset.set(true);
	}
	
	public void setIrq()
	{
		irq.set(true);
	}
	
	public void seta(int val)
	{
		a = val;
	}
	
	public void setx(int val)
	{
		x = val;
	}
	
	public void sety(int val)
	{
		y = val;
	}
	
	public void sets(int val)
	{
		s = val;
	}
	
	public void setp(int val)
	{
		p = val;
	}
	
	public void setpc(int val)
	{
		pc = val;
	}
	
	public void debugHold(boolean val)
	{
		debugHold.set(val);
	}
	
	public void terminate()
	{
		terminate.set(true);
	}
	
	public void setBreakAddress(int addr)
	{
		breakAddress.set(addr);
	}
	
	public void step()
	{
		stepFlag.set(true);
	}
	
	public boolean stepInProgress()
	{
		return stepFlag.get();
	}
	
	public void setupCart(Cartridge cart)
	{
		mem.setupCart(cart);
	}
	
	public void setupCart(Cartridge cart, String filename)
	{
		mem.setupCart(cart, filename);
	}
	
	public void readAddress(int addr)
	{
		synchronized(System.out) 
		{
			System.out.println("Value at address " + String.format("0x%04X", addr) + " is " + String.format("0x%02X", mem.read(addr)));
		}
	}
	
	public void writeAddress(int addr, int val)
	{
		mem.write(addr, val);
	}
	
	public void print()
	{
		synchronized(System.out) 
		{
			System.out.println("PC = " + String.format("0x%04X", pc));
			System.out.println("A = " + String.format("0x%02X", a));
			System.out.println("X = " + String.format("0x%02X", x));
			System.out.println("Y = " + String.format("0x%02X", y));
			System.out.println("SP = " + String.format("0x%02X", s));
			System.out.println("FLAGS = " + String.format("0x%02X", p));
		}
	}

	@Override
	public void run() {
		setReset();
		cycle = 0;
		while (true)
		{
			if (LOG)
			{
				synchronized(System.out) 
				{
					out = log;
					printInstruction();
				}
			}
			
			long stepCycles = 0;
			if (terminate.get())
			{
				return;
			}
			
			if (pc == breakAddress.get())
			{
				debugHold.set(true);
				ppu.debugHold(true);
				synchronized(System.out) 
				{
					System.out.println("Breakpoint hit");
					out = System.out;
					printInstruction();
					System.out.print("NES> ");
				}
			}
			
			while (debugHold.get()) 
			{
				if (stepFlag.get())
				{
					stepCycles = cycle;
					break;
				}
				
				try
				{
					Thread.sleep(1);
				}
				catch(Exception e) {}
			}
			
			if (reset.get())
			{
				reset();
				reset.set(false);
			}
			else if (nmi.get())
			{
				if (!inNmi)
				{
					inNmi = true;
					nmi();
				}
				
				nmi.set(false);
				mem.saveRam();
				inNmi = false;
			}
			else if (irq.get())
			{
				if (!getI())
				{
					irq();
				}
				
				irq.set(false);
			}
			else
			{
				executeInstruction();
			}
			
			stepFlag.set(false);
			if (stepCycles != 0)
			{
				ppu.step(cycle - stepCycles);
			}
		}
	}
	
	private void executeInstruction()
	{
		int opcode = memRead(pc);
		
		switch(opcode)
		{
		case 0x00:
			brk();
			break;
		case 0x01:
			ora(AddressingMode.INDIRECTX);
			break;
		case 0x02:
			halt();
			break;
		case 0x03:
			slo(AddressingMode.INDIRECTX);
			break;
		case 0x04:
			nop(AddressingMode.ZEROPAGE);
			break;
		case 0x05:
			ora(AddressingMode.ZEROPAGE);
			break;
		case 0x06:
			asl(AddressingMode.ZEROPAGE);
			break;
		case 0x07:
			slo(AddressingMode.ZEROPAGE);
			break;
		case 0x08:
			php();
			break;
		case 0x09:
			ora(AddressingMode.IMMEDIATE);
			break;
		case 0x0a:
			asl(AddressingMode.ACCUMULATOR);
			break;
		case 0x0b:
			anc(AddressingMode.IMMEDIATE);
			break;
		case 0x0c:
			nop(AddressingMode.ABSOLUTE);
			break;
		case 0x0d:
			ora(AddressingMode.ABSOLUTE);
			break;
		case 0x0e:
			asl(AddressingMode.ABSOLUTE);
			break;
		case 0x0f:
			slo(AddressingMode.ABSOLUTE);
			break;
		case 0x10:
			bpl(AddressingMode.RELATIVE);
			break;
		case 0x11:
			ora(AddressingMode.INDIRECTY);
			break;
		case 0x12:
			halt();
			break;
		case 0x13:
			slo(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x14:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0x15:
			ora(AddressingMode.ZEROPAGEX);
			break;
		case 0x16:
			asl(AddressingMode.ZEROPAGEX);
			break;
		case 0x17:
			slo(AddressingMode.ZEROPAGEX);
			break;
		case 0x18:
			clc();
			break;
		case 0x19:
			ora(AddressingMode.ABSOLUTEY);
			break;
		case 0x1a:
			nop(AddressingMode.IMPLIED);
			break;
		case 0x1b:
			slo(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x1c:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0x1d:
			ora(AddressingMode.ABSOLUTEX);
			break;
		case 0x1e:
			asl(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x1f:
			slo(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x20:
			jsr(AddressingMode.ABSOLUTE);
			break;
		case 0x21:
			and(AddressingMode.INDIRECTX);
			break;
		case 0x22:
			halt();
			break;
		case 0x23:
			rla(AddressingMode.INDIRECTX);
			break;
		case 0x24:
			bit(AddressingMode.ZEROPAGE);
			break;
		case 0x25:
			and(AddressingMode.ZEROPAGE);
			break;
		case 0x26:
			rol(AddressingMode.ZEROPAGE);
			break;
		case 0x27:
			rla(AddressingMode.ZEROPAGE);
			break;
		case 0x28:
			plp();
			break;
		case 0x29:
			and(AddressingMode.IMMEDIATE);
			break;
		case 0x2a:
			rol(AddressingMode.ACCUMULATOR);
			break;
		case 0x2b:
			anc(AddressingMode.IMMEDIATE);
			break;
		case 0x2c:
			bit(AddressingMode.ABSOLUTE);
			break;
		case 0x2d:
			and(AddressingMode.ABSOLUTE);
			break;
		case 0x2e:
			rol(AddressingMode.ABSOLUTE);
			break;
		case 0x2f:
			rla(AddressingMode.ABSOLUTE);
			break;
		case 0x30:
			bmi(AddressingMode.RELATIVE);
			break;
		case 0x31:
			and(AddressingMode.INDIRECTY);
			break;
		case 0x32:
			halt();
			break;
		case 0x33:
			rla(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x34:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0x35:
			and(AddressingMode.ZEROPAGEX);
			break;
		case 0x36:
			rol(AddressingMode.ZEROPAGEX);
			break;
		case 0x37:
			rla(AddressingMode.ZEROPAGEX);
			break;
		case 0x38:
			sec();
			break;
		case 0x39:
			and(AddressingMode.ABSOLUTEY);
			break;
		case 0x3a:
			nop(AddressingMode.IMPLIED);
			break;
		case 0x3b:
			rla(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x3c:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0x3d:
			and(AddressingMode.ABSOLUTEX);
			break;
		case 0x3e:
			rol(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x3f:
			rla(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x40:
			rti();
			break;
		case 0x41:
			eor(AddressingMode.INDIRECTX);
			break;
		case 0x42:
			halt();
			break;
		case 0x43:
			sre(AddressingMode.INDIRECTX);
			break;
		case 0x44:
			nop(AddressingMode.ZEROPAGE);
			break;
		case 0x45:
			eor(AddressingMode.ZEROPAGE);
			break;
		case 0x46:
			lsr(AddressingMode.ZEROPAGE);
			break;
		case 0x47:
			sre(AddressingMode.ZEROPAGE);
			break;
		case 0x48:
			pha();
			break;
		case 0x49:
			eor(AddressingMode.IMMEDIATE);
			break;
		case 0x4a:
			lsr(AddressingMode.ACCUMULATOR);
			break;
		case 0x4b:
			alr(AddressingMode.IMMEDIATE);
			break;
		case 0x4c:
			jmp(AddressingMode.ABSOLUTE);
			break;
		case 0x4d:
			eor(AddressingMode.ABSOLUTE);
			break;
		case 0x4e:
			lsr(AddressingMode.ABSOLUTE);
			break;
		case 0x4f:
			sre(AddressingMode.ABSOLUTE);
			break;
		case 0x50:
			bvc(AddressingMode.RELATIVE);
			break;
		case 0x51:
			eor(AddressingMode.INDIRECTY);
			break;
		case 0x52:
			halt();
			break;
		case 0x53:
			sre(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x54:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0x55:
			eor(AddressingMode.ZEROPAGEX);
			break;
		case 0x56:
			lsr(AddressingMode.ZEROPAGEX);
			break;
		case 0x57:
			sre(AddressingMode.ZEROPAGEX);
			break;
		case 0x58:
			cli();
			break;
		case 0x59:
			eor(AddressingMode.ABSOLUTEY);
			break;
		case 0x5a:
			nop(AddressingMode.IMPLIED);
			break;
		case 0x5b:
			sre(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x5c:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0x5d:
			eor(AddressingMode.ABSOLUTEX);
			break;
		case 0x5e:
			lsr(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x5f:
			sre(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x60:
			rts();
			break;
		case 0x61:
			adc(AddressingMode.INDIRECTX);
			break;
		case 0x62:
			halt();
			break;
		case 0x63:
			rra(AddressingMode.INDIRECTX);
			break;
		case 0x64:
			nop(AddressingMode.ZEROPAGE);
			break;
		case 0x65:
			adc(AddressingMode.ZEROPAGE);
			break;
		case 0x66:
			ror(AddressingMode.ZEROPAGE);
			break;
		case 0x67:
			rra(AddressingMode.ZEROPAGE);
			break;
		case 0x68:
			pla();
			break;
		case 0x69:
			adc(AddressingMode.IMMEDIATE);
			break;
		case 0x6a:
			ror(AddressingMode.ACCUMULATOR);
			break;
		case 0x6b:
			unstable("0x6b");
			break;
		case 0x6c:
			jmp(AddressingMode.INDIRECT);
			break;
		case 0x6d:
			adc(AddressingMode.ABSOLUTE);
			break;
		case 0x6e:
			ror(AddressingMode.ABSOLUTE);
			break;
		case 0x6f:
			rra(AddressingMode.ABSOLUTE);
			break;
		case 0x70:
			bvs(AddressingMode.RELATIVE);
			break;
		case 0x71:
			adc(AddressingMode.INDIRECTY);
			break;
		case 0x72:
			halt();
			break;
		case 0x73:
			rra(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x74:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0x75:
			adc(AddressingMode.ZEROPAGEX);
			break;
		case 0x76:
			ror(AddressingMode.ZEROPAGEX);
			break;
		case 0x77:
			rra(AddressingMode.ZEROPAGEX);
			break;
		case 0x78:
			sei();
			break;
		case 0x79:
			adc(AddressingMode.ABSOLUTEY);
			break;
		case 0x7a:
			nop(AddressingMode.IMPLIED);
			break;
		case 0x7b:
			rra(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x7c:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0x7d:
			adc(AddressingMode.ABSOLUTEX);
			break;
		case 0x7e:
			ror(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x7f:
			rra(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x80:
			nop(AddressingMode.IMMEDIATE);
			break;
		case 0x81:
			sta(AddressingMode.INDIRECTX);
			break;
		case 0x82:
			nop(AddressingMode.IMMEDIATE);
			break;
		case 0x83:
			sax(AddressingMode.INDIRECTX);
			break;
		case 0x84:
			sty(AddressingMode.ZEROPAGE);
			break;
		case 0x85:
			sta(AddressingMode.ZEROPAGE);
			break;
		case 0x86:
			stx(AddressingMode.ZEROPAGE);
			break;
		case 0x87:
			sax(AddressingMode.ZEROPAGE);
			break;
		case 0x88:
			dey();
			break;
		case 0x89:
			nop(AddressingMode.IMMEDIATE);
			break;
		case 0x8a:
			txa();
			break;
		case 0x8b:
			unstable("0x8b");
			break;
		case 0x8c:
			sty(AddressingMode.ABSOLUTE);
			break;
		case 0x8d:
			sta(AddressingMode.ABSOLUTE);
			break;
		case 0x8e:
			stx(AddressingMode.ABSOLUTE);
			break;
		case 0x8f:
			sax(AddressingMode.ABSOLUTE);
			break;
		case 0x90:
			bcc(AddressingMode.RELATIVE);
			break;
		case 0x91:
			sta(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x92:
			halt();
			break;
		case 0x93:
			unstable("0x93");
			break;
		case 0x94:
			sty(AddressingMode.ZEROPAGEX);
			break;
		case 0x95:
			sta(AddressingMode.ZEROPAGEX);
			break;
		case 0x96:
			stx(AddressingMode.ZEROPAGEY);
			break;
		case 0x97:
			sax(AddressingMode.ZEROPAGEY);
			break;
		case 0x98:
			tya();
			break;
		case 0x99:
			sta(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x9a:
			txs();
			break;
		case 0x9b:
			unstable("0x9b");
			break;
		case 0x9c:
			unstable("0x9c");
			break;
		case 0x9d:
			sta(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x9e:
			unstable("0x9e");
			break;
		case 0x9f:
			unstable("0x9f");
			break;
		case 0xa0:
			ldy(AddressingMode.IMMEDIATE);
			break;
		case 0xa1:
			lda(AddressingMode.INDIRECTX);
			break;
		case 0xa2:
			ldx(AddressingMode.IMMEDIATE);
			break;
		case 0xa3:
			lax(AddressingMode.INDIRECTX);
			break;
		case 0xa4:
			ldy(AddressingMode.ZEROPAGE);
			break;
		case 0xa5:
			lda(AddressingMode.ZEROPAGE);
			break;
		case 0xa6:
			ldx(AddressingMode.ZEROPAGE);
			break;
		case 0xa7:
			lax(AddressingMode.ZEROPAGE);
			break;
		case 0xa8:
			tay();
			break;
		case 0xa9:
			lda(AddressingMode.IMMEDIATE);
			break;
		case 0xaa:
			tax();
			break;
		case 0xab:
			unstable("0xab");
			break;
		case 0xac:
			ldy(AddressingMode.ABSOLUTE);
			break;
		case 0xad:
			lda(AddressingMode.ABSOLUTE);
			break;
		case 0xae:
			ldx(AddressingMode.ABSOLUTE);
			break;
		case 0xaf:
			lax(AddressingMode.ABSOLUTE);
			break;
		case 0xb0:
			bcs(AddressingMode.RELATIVE);
			break;
		case 0xb1:
			lda(AddressingMode.INDIRECTY);
			break;
		case 0xb2:
			halt();
			break;
		case 0xb3:
			lax(AddressingMode.INDIRECTY);
			break;
		case 0xb4:
			ldy(AddressingMode.ZEROPAGEX);
			break;
		case 0xb5:
			lda(AddressingMode.ZEROPAGEX);
			break;
		case 0xb6:
			ldx(AddressingMode.ZEROPAGEY);
			break;
		case 0xb7:
			lax(AddressingMode.ZEROPAGEY);
			break;
		case 0xb8:
			clv();
			break;
		case 0xb9:
			lda(AddressingMode.ABSOLUTEY);
			break;
		case 0xba:
			tsx();
			break;
		case 0xbb:
			las(AddressingMode.ABSOLUTEY);
			break;
		case 0xbc:
			ldy(AddressingMode.ABSOLUTEX);
			break;
		case 0xbd:
			lda(AddressingMode.ABSOLUTEX);
			break;
		case 0xbe:
			ldx(AddressingMode.ABSOLUTEY);
			break;
		case 0xbf:
			lax(AddressingMode.ABSOLUTEY);
			break;
		case 0xc0:
			cpy(AddressingMode.IMMEDIATE);
			break;
		case 0xc1:
			cmp(AddressingMode.INDIRECTX);
			break;
		case 0xc2:
			nop(AddressingMode.IMMEDIATE);
			break;
		case 0xc3:
			dcp(AddressingMode.INDIRECTX);
			break;
		case 0xc4:
			cpy(AddressingMode.ZEROPAGE);
			break;
		case 0xc5:
			cmp(AddressingMode.ZEROPAGE);
			break;
		case 0xc6:
			dec(AddressingMode.ZEROPAGE);
			break;
		case 0xc7:
			dcp(AddressingMode.ZEROPAGE);
			break;
		case 0xc8:
			iny();
			break;
		case 0xc9:
			cmp(AddressingMode.IMMEDIATE);
			break;
		case 0xca:
			dex();
			break;
		case 0xcb:
			sbx(AddressingMode.IMMEDIATE);
			break;
		case 0xcc:
			cpy(AddressingMode.ABSOLUTE);
			break;
		case 0xcd:
			cmp(AddressingMode.ABSOLUTE);
			break;
		case 0xce:
			dec(AddressingMode.ABSOLUTE);
			break;
		case 0xcf:
			dcp(AddressingMode.ABSOLUTE);
			break;
		case 0xd0:
			bne(AddressingMode.RELATIVE);
			break;
		case 0xd1:
			cmp(AddressingMode.INDIRECTY);
			break;
		case 0xd2:
			halt();
			break;
		case 0xd3:
			dcp(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0xd4:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0xd5:
			cmp(AddressingMode.ZEROPAGEX);
			break;
		case 0xd6:
			dec(AddressingMode.ZEROPAGEX);
			break;
		case 0xd7:
			dcp(AddressingMode.ZEROPAGEX);
			break;
		case 0xd8:
			cld();
			break;
		case 0xd9:
			cmp(AddressingMode.ABSOLUTEY);
			break;
		case 0xda:
			nop(AddressingMode.IMPLIED);
			break;
		case 0xdb:
			dcp(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0xdc:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0xdd:
			cmp(AddressingMode.ABSOLUTEX);
			break;
		case 0xde:
			dec(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xdf:
			dcp(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xe0:
			cpx(AddressingMode.IMMEDIATE);
			break;
		case 0xe1:
			sbc(AddressingMode.INDIRECTX);
			break;
		case 0xe2:
			nop(AddressingMode.IMMEDIATE);
			break;
		case 0xe3:
			isc(AddressingMode.INDIRECTX);
			break;
		case 0xe4:
			cpx(AddressingMode.ZEROPAGE);
			break;
		case 0xe5:
			sbc(AddressingMode.ZEROPAGE);
			break;
		case 0xe6:
			inc(AddressingMode.ZEROPAGE);
			break;
		case 0xe7:
			isc(AddressingMode.ZEROPAGE);
			break;
		case 0xe8:
			inx();
			break;
		case 0xe9:
			sbc(AddressingMode.IMMEDIATE);
			break;
		case 0xea:
			nop(AddressingMode.IMPLIED);
			break;
		case 0xeb:
			sbc(AddressingMode.IMMEDIATE);
			break;
		case 0xec:
			cpx(AddressingMode.ABSOLUTE);
			break;
		case 0xed:
			sbc(AddressingMode.ABSOLUTE);
			break;
		case 0xee:
			inc(AddressingMode.ABSOLUTE);
			break;
		case 0xef:
			isc(AddressingMode.ABSOLUTE);
			break;
		case 0xf0:
			beq(AddressingMode.RELATIVE);
			break;
		case 0xf1:
			sbc(AddressingMode.INDIRECTY);
			break;
		case 0xf2:
			halt();
			break;
		case 0xf3:
			isc(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0xf4:
			nop(AddressingMode.ZEROPAGEX);
			break;
		case 0xf5:
			sbc(AddressingMode.ZEROPAGEX);
			break;
		case 0xf6:
			inc(AddressingMode.ZEROPAGEX);
			break;
		case 0xf7:
			isc(AddressingMode.ZEROPAGEX);
			break;
		case 0xf8:
			sed();
			break;
		case 0xf9:
			sbc(AddressingMode.ABSOLUTEY);
			break;
		case 0xfa:
			nop(AddressingMode.IMPLIED);
			break;
		case 0xfb:
			isc(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0xfc:
			nop(AddressingMode.ABSOLUTEX);
			break;
		case 0xfd:
			sbc(AddressingMode.ABSOLUTEX);
			break;
		case 0xfe:
			inc(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xff:
			isc(AddressingMode.ABSOLUTEX_WRITE);
			break;
		default:
			halt();	
		}
	}
	
	public void printInstructionToScreen()
	{
		synchronized(System.out)
		{
			out = System.out;
			printInstruction();
		}
	}
	
	private void printInstruction()
	{
		int opcode = mem.read(pc);
		out.print(String.format("0x%04X", pc) + "(" + String.format("0x%02X", opcode) +"): ");
		
		switch(opcode)
		{
		case 0x00:
			brk_print();
			break;
		case 0x01:
			ora_print(AddressingMode.INDIRECTX);
			break;
		case 0x02:
			halt_print();
			break;
		case 0x03:
			slo_print(AddressingMode.INDIRECTX);
			break;
		case 0x04:
			nop_print(AddressingMode.ZEROPAGE);
			break;
		case 0x05:
			ora_print(AddressingMode.ZEROPAGE);
			break;
		case 0x06:
			asl_print(AddressingMode.ZEROPAGE);
			break;
		case 0x07:
			slo_print(AddressingMode.ZEROPAGE);
			break;
		case 0x08:
			php_print();
			break;
		case 0x09:
			ora_print(AddressingMode.IMMEDIATE);
			break;
		case 0x0a:
			asl_print(AddressingMode.ACCUMULATOR);
			break;
		case 0x0b:
			anc_print(AddressingMode.IMMEDIATE);
			break;
		case 0x0c:
			nop_print(AddressingMode.ABSOLUTE);
			break;
		case 0x0d:
			ora_print(AddressingMode.ABSOLUTE);
			break;
		case 0x0e:
			asl_print(AddressingMode.ABSOLUTE);
			break;
		case 0x0f:
			slo_print(AddressingMode.ABSOLUTE);
			break;
		case 0x10:
			bpl_print(AddressingMode.RELATIVE);
			break;
		case 0x11:
			ora_print(AddressingMode.INDIRECTY);
			break;
		case 0x12:
			halt_print();
			break;
		case 0x13:
			slo_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x14:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x15:
			ora_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x16:
			asl_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x17:
			slo_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x18:
			clc_print();
			break;
		case 0x19:
			ora_print(AddressingMode.ABSOLUTEY);
			break;
		case 0x1a:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0x1b:
			slo_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x1c:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x1d:
			ora_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x1e:
			asl_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x1f:
			slo_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x20:
			jsr_print(AddressingMode.ABSOLUTE);
			break;
		case 0x21:
			and_print(AddressingMode.INDIRECTX);
			break;
		case 0x22:
			halt_print();
			break;
		case 0x23:
			rla_print(AddressingMode.INDIRECTX);
			break;
		case 0x24:
			bit_print(AddressingMode.ZEROPAGE);
			break;
		case 0x25:
			and_print(AddressingMode.ZEROPAGE);
			break;
		case 0x26:
			rol_print(AddressingMode.ZEROPAGE);
			break;
		case 0x27:
			rla_print(AddressingMode.ZEROPAGE);
			break;
		case 0x28:
			plp_print();
			break;
		case 0x29:
			and_print(AddressingMode.IMMEDIATE);
			break;
		case 0x2a:
			rol_print(AddressingMode.ACCUMULATOR);
			break;
		case 0x2b:
			anc_print(AddressingMode.IMMEDIATE);
			break;
		case 0x2c:
			bit_print(AddressingMode.ABSOLUTE);
			break;
		case 0x2d:
			and_print(AddressingMode.ABSOLUTE);
			break;
		case 0x2e:
			rol_print(AddressingMode.ABSOLUTE);
			break;
		case 0x2f:
			rla_print(AddressingMode.ABSOLUTE);
			break;
		case 0x30:
			bmi_print(AddressingMode.RELATIVE);
			break;
		case 0x31:
			and_print(AddressingMode.INDIRECTY);
			break;
		case 0x32:
			halt_print();
			break;
		case 0x33:
			rla_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x34:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x35:
			and_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x36:
			rol_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x37:
			rla_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x38:
			sec_print();
			break;
		case 0x39:
			and_print(AddressingMode.ABSOLUTEY);
			break;
		case 0x3a:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0x3b:
			rla_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x3c:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x3d:
			and_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x3e:
			rol_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x3f:
			rla_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x40:
			rti_print();
			break;
		case 0x41:
			eor_print(AddressingMode.INDIRECTX);
			break;
		case 0x42:
			halt_print();
			break;
		case 0x43:
			sre_print(AddressingMode.INDIRECTX);
			break;
		case 0x44:
			nop_print(AddressingMode.ZEROPAGE);
			break;
		case 0x45:
			eor_print(AddressingMode.ZEROPAGE);
			break;
		case 0x46:
			lsr_print(AddressingMode.ZEROPAGE);
			break;
		case 0x47:
			sre_print(AddressingMode.ZEROPAGE);
			break;
		case 0x48:
			pha_print();
			break;
		case 0x49:
			eor_print(AddressingMode.IMMEDIATE);
			break;
		case 0x4a:
			lsr_print(AddressingMode.ACCUMULATOR);
			break;
		case 0x4b:
			alr_print(AddressingMode.IMMEDIATE);
			break;
		case 0x4c:
			jmp_print(AddressingMode.ABSOLUTE);
			break;
		case 0x4d:
			eor_print(AddressingMode.ABSOLUTE);
			break;
		case 0x4e:
			lsr_print(AddressingMode.ABSOLUTE);
			break;
		case 0x4f:
			sre_print(AddressingMode.ABSOLUTE);
			break;
		case 0x50:
			bvc_print(AddressingMode.RELATIVE);
			break;
		case 0x51:
			eor_print(AddressingMode.INDIRECTY);
			break;
		case 0x52:
			halt_print();
			break;
		case 0x53:
			sre_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x54:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x55:
			eor_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x56:
			lsr_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x57:
			sre_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x58:
			cli_print();
			break;
		case 0x59:
			eor_print(AddressingMode.ABSOLUTEY);
			break;
		case 0x5a:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0x5b:
			sre_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x5c:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x5d:
			eor_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x5e:
			lsr_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x5f:
			sre_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x60:
			rts_print();
			break;
		case 0x61:
			adc_print(AddressingMode.INDIRECTX);
			break;
		case 0x62:
			halt_print();
			break;
		case 0x63:
			rra_print(AddressingMode.INDIRECTX);
			break;
		case 0x64:
			nop_print(AddressingMode.ZEROPAGE);
			break;
		case 0x65:
			adc_print(AddressingMode.ZEROPAGE);
			break;
		case 0x66:
			ror_print(AddressingMode.ZEROPAGE);
			break;
		case 0x67:
			rra_print(AddressingMode.ZEROPAGE);
			break;
		case 0x68:
			pla_print();
			break;
		case 0x69:
			adc_print(AddressingMode.IMMEDIATE);
			break;
		case 0x6a:
			ror_print(AddressingMode.ACCUMULATOR);
			break;
		case 0x6b:
			unstable_print("0x6b");
			break;
		case 0x6c:
			jmp_print(AddressingMode.INDIRECT);
			break;
		case 0x6d:
			adc_print(AddressingMode.ABSOLUTE);
			break;
		case 0x6e:
			ror_print(AddressingMode.ABSOLUTE);
			break;
		case 0x6f:
			rra_print(AddressingMode.ABSOLUTE);
			break;
		case 0x70:
			bvs_print(AddressingMode.RELATIVE);
			break;
		case 0x71:
			adc_print(AddressingMode.INDIRECTY);
			break;
		case 0x72:
			halt_print();
			break;
		case 0x73:
			rra_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x74:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x75:
			adc_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x76:
			ror_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x77:
			rra_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x78:
			sei_print();
			break;
		case 0x79:
			adc_print(AddressingMode.ABSOLUTEY);
			break;
		case 0x7a:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0x7b:
			rra_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x7c:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x7d:
			adc_print(AddressingMode.ABSOLUTEX);
			break;
		case 0x7e:
			ror_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x7f:
			rra_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x80:
			nop_print(AddressingMode.IMMEDIATE);
			break;
		case 0x81:
			sta_print(AddressingMode.INDIRECTX);
			break;
		case 0x82:
			nop_print(AddressingMode.IMMEDIATE);
			break;
		case 0x83:
			sax_print(AddressingMode.INDIRECTX);
			break;
		case 0x84:
			sty_print(AddressingMode.ZEROPAGE);
			break;
		case 0x85:
			sta_print(AddressingMode.ZEROPAGE);
			break;
		case 0x86:
			stx_print(AddressingMode.ZEROPAGE);
			break;
		case 0x87:
			sax_print(AddressingMode.ZEROPAGE);
			break;
		case 0x88:
			dey_print();
			break;
		case 0x89:
			nop_print(AddressingMode.IMMEDIATE);
			break;
		case 0x8a:
			txa_print();
			break;
		case 0x8b:
			unstable_print("0x8b");
			break;
		case 0x8c:
			sty_print(AddressingMode.ABSOLUTE);
			break;
		case 0x8d:
			sta_print(AddressingMode.ABSOLUTE);
			break;
		case 0x8e:
			stx_print(AddressingMode.ABSOLUTE);
			break;
		case 0x8f:
			sax_print(AddressingMode.ABSOLUTE);
			break;
		case 0x90:
			bcc_print(AddressingMode.RELATIVE);
			break;
		case 0x91:
			sta_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0x92:
			halt_print();
			break;
		case 0x93:
			unstable_print("0x93");
			break;
		case 0x94:
			sty_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x95:
			sta_print(AddressingMode.ZEROPAGEX);
			break;
		case 0x96:
			stx_print(AddressingMode.ZEROPAGEY);
			break;
		case 0x97:
			sax_print(AddressingMode.ZEROPAGEY);
			break;
		case 0x98:
			tya_print();
			break;
		case 0x99:
			sta_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0x9a:
			txs_print();
			break;
		case 0x9b:
			unstable_print("0x9b");
			break;
		case 0x9c:
			unstable_print("0x9c");
			break;
		case 0x9d:
			sta_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0x9e:
			unstable_print("0x9e");
			break;
		case 0x9f:
			unstable_print("0x9f");
			break;
		case 0xa0:
			ldy_print(AddressingMode.IMMEDIATE);
			break;
		case 0xa1:
			lda_print(AddressingMode.INDIRECTX);
			break;
		case 0xa2:
			ldx_print(AddressingMode.IMMEDIATE);
			break;
		case 0xa3:
			lax_print(AddressingMode.INDIRECTX);
			break;
		case 0xa4:
			ldy_print(AddressingMode.ZEROPAGE);
			break;
		case 0xa5:
			lda_print(AddressingMode.ZEROPAGE);
			break;
		case 0xa6:
			ldx_print(AddressingMode.ZEROPAGE);
			break;
		case 0xa7:
			lax_print(AddressingMode.ZEROPAGE);
			break;
		case 0xa8:
			tay_print();
			break;
		case 0xa9:
			lda_print(AddressingMode.IMMEDIATE);
			break;
		case 0xaa:
			tax_print();
			break;
		case 0xab:
			unstable_print("0xab");
			break;
		case 0xac:
			ldy_print(AddressingMode.ABSOLUTE);
			break;
		case 0xad:
			lda_print(AddressingMode.ABSOLUTE);
			break;
		case 0xae:
			ldx_print(AddressingMode.ABSOLUTE);
			break;
		case 0xaf:
			lax_print(AddressingMode.ABSOLUTE);
			break;
		case 0xb0:
			bcs_print(AddressingMode.RELATIVE);
			break;
		case 0xb1:
			lda_print(AddressingMode.INDIRECTY);
			break;
		case 0xb2:
			halt_print();
			break;
		case 0xb3:
			lax_print(AddressingMode.INDIRECTY);
			break;
		case 0xb4:
			ldy_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xb5:
			lda_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xb6:
			ldx_print(AddressingMode.ZEROPAGEY);
			break;
		case 0xb7:
			lax_print(AddressingMode.ZEROPAGEY);
			break;
		case 0xb8:
			clv_print();
			break;
		case 0xb9:
			lda_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xba:
			tsx_print();
			break;
		case 0xbb:
			las_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xbc:
			ldy_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xbd:
			lda_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xbe:
			ldx_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xbf:
			lax_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xc0:
			cpy_print(AddressingMode.IMMEDIATE);
			break;
		case 0xc1:
			cmp_print(AddressingMode.INDIRECTX);
			break;
		case 0xc2:
			nop_print(AddressingMode.IMMEDIATE);
			break;
		case 0xc3:
			dcp_print(AddressingMode.INDIRECTX);
			break;
		case 0xc4:
			cpy_print(AddressingMode.ZEROPAGE);
			break;
		case 0xc5:
			cmp_print(AddressingMode.ZEROPAGE);
			break;
		case 0xc6:
			dec_print(AddressingMode.ZEROPAGE);
			break;
		case 0xc7:
			dcp_print(AddressingMode.ZEROPAGE);
			break;
		case 0xc8:
			iny_print();
			break;
		case 0xc9:
			cmp_print(AddressingMode.IMMEDIATE);
			break;
		case 0xca:
			dex_print();
			break;
		case 0xcb:
			sbx_print(AddressingMode.IMMEDIATE);
			break;
		case 0xcc:
			cpy_print(AddressingMode.ABSOLUTE);
			break;
		case 0xcd:
			cmp_print(AddressingMode.ABSOLUTE);
			break;
		case 0xce:
			dec_print(AddressingMode.ABSOLUTE);
			break;
		case 0xcf:
			dcp_print(AddressingMode.ABSOLUTE);
			break;
		case 0xd0:
			bne_print(AddressingMode.RELATIVE);
			break;
		case 0xd1:
			cmp_print(AddressingMode.INDIRECTY);
			break;
		case 0xd2:
			halt_print();
			break;
		case 0xd3:
			dcp_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0xd4:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xd5:
			cmp_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xd6:
			dec_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xd7:
			dcp_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xd8:
			cld_print();
			break;
		case 0xd9:
			cmp_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xda:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0xdb:
			dcp_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0xdc:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xdd:
			cmp_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xde:
			dec_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xdf:
			dcp_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xe0:
			cpx_print(AddressingMode.IMMEDIATE);
			break;
		case 0xe1:
			sbc_print(AddressingMode.INDIRECTX);
			break;
		case 0xe2:
			nop_print(AddressingMode.IMMEDIATE);
			break;
		case 0xe3:
			isc_print(AddressingMode.INDIRECTX);
			break;
		case 0xe4:
			cpx_print(AddressingMode.ZEROPAGE);
			break;
		case 0xe5:
			sbc_print(AddressingMode.ZEROPAGE);
			break;
		case 0xe6:
			inc_print(AddressingMode.ZEROPAGE);
			break;
		case 0xe7:
			isc_print(AddressingMode.ZEROPAGE);
			break;
		case 0xe8:
			inx_print();
			break;
		case 0xe9:
			sbc_print(AddressingMode.IMMEDIATE);
			break;
		case 0xea:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0xeb:
			sbc_print(AddressingMode.IMMEDIATE);
			break;
		case 0xec:
			cpx_print(AddressingMode.ABSOLUTE);
			break;
		case 0xed:
			sbc_print(AddressingMode.ABSOLUTE);
			break;
		case 0xee:
			inc_print(AddressingMode.ABSOLUTE);
			break;
		case 0xef:
			isc_print(AddressingMode.ABSOLUTE);
			break;
		case 0xf0:
			beq_print(AddressingMode.RELATIVE);
			break;
		case 0xf1:
			sbc_print(AddressingMode.INDIRECTY);
			break;
		case 0xf2:
			halt_print();
			break;
		case 0xf3:
			isc_print(AddressingMode.INDIRECTY_WRITE);
			break;
		case 0xf4:
			nop_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xf5:
			sbc_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xf6:
			inc_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xf7:
			isc_print(AddressingMode.ZEROPAGEX);
			break;
		case 0xf8:
			sed_print();
			break;
		case 0xf9:
			sbc_print(AddressingMode.ABSOLUTEY);
			break;
		case 0xfa:
			nop_print(AddressingMode.IMPLIED);
			break;
		case 0xfb:
			isc_print(AddressingMode.ABSOLUTEY_WRITE);
			break;
		case 0xfc:
			nop_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xfd:
			sbc_print(AddressingMode.ABSOLUTEX);
			break;
		case 0xfe:
			inc_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		case 0xff:
			isc_print(AddressingMode.ABSOLUTEX_WRITE);
			break;
		default:
			halt_print();	
		}
	}
	
	private void unstable(String name)
	{
		synchronized(System.out) 
		{
			System.out.println("Unstable opcode " + name + " was executed");
		}
		
		while (true) {
			if (terminate.get())
			{
				return;
			}
			
			if (reset.get())
			{
				break;
			}
		}
	}
	
	private void unstable_print(String name)
	{
		out.println("UNSTABLE (" + name + ")");
	}
	
	private void lda(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		a = val;
		setNegative(a);
		setZero(a);
	}
	
	private void lda_print(AddressingMode mode)
	{
		out.println("LDA " + printOperands(mode));
	}
	
	private void las(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		a = val & s;
		x = a;
		s = a;
		setNegative(a);
		setZero(a);
	}
	
	private void las_print(AddressingMode mode)
	{
		out.println("LAS " + printOperands(mode));
	}
	
	private void sta(AddressingMode mode)
	{
		int addr = getAddr(mode);
		memWrite(addr, a);
	}
	
	private void sta_print(AddressingMode mode)
	{
		out.println("STA " + printOperands(mode));
	}
	
	private void sax(AddressingMode mode)
	{
		int addr = getAddr(mode);
		memWrite(addr, a & x);
	}
	
	private void sax_print(AddressingMode mode)
	{
		out.println("SAX " + printOperands(mode));
	}
	
	private void stx(AddressingMode mode)
	{
		int addr = getAddr(mode);
		memWrite(addr, x);
	}
	
	private void stx_print(AddressingMode mode)
	{
		out.println("STX " + printOperands(mode));
	}
	
	private void sty(AddressingMode mode)
	{
		int addr = getAddr(mode);
		memWrite(addr, y);
	}
	
	private void sty_print(AddressingMode mode)
	{
		out.println("STY " + printOperands(mode));
	}
	
	private void adc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int result = a + val;
		if (getCarry())
		{
			++result;
		}
		
		setZero(result & 0xff);
		setCarry(result);
		setNegative(result & 0xff);
		setOverflow(a, val, result & 0xff);
		a = result & 0xff;
	}
	
	private void adc_print(AddressingMode mode)
	{
		out.println("ADC " + printOperands(mode));
	}
	
	private void sbc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		val = (~val) & 0xff;
		int result = a + val;
		if (getCarry())
		{
			++result;
		}
		
		setZero(result & 0xff);
		setCarry(result);
		setNegative(result & 0xff);
		setOverflow(a, val, result & 0xff);
		a = result & 0xff;
	}
	
	private void sbc_print(AddressingMode mode)
	{
		out.println("SBC " + printOperands(mode));
	}
	
	private void dcp(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int newVal = val - 1;
		newVal &= 0xff;
		memWrite(addr, val);
		memWrite(addr, newVal);
		int result = a - newVal;
		setZero(result & 0xff);
		result += 256;
		setCarry(result);
		setNegative(result);
	}
	
	private void dcp_print(AddressingMode mode)
	{
		out.println("DCP " + printOperands(mode));
	}
	
	private void sbx(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int result = (a & x) - val;
		setZero(result & 0xff);
		result += 256;
		setCarry(result);
		setNegative(result);
		x = result & 0xff;
	}
	
	private void sbx_print(AddressingMode mode)
	{
		out.println("SBX " + printOperands(mode));
	}
	
	private void cmp(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int result = a - val;
		setZero(result & 0xff);
		result += 256;
		setCarry(result);
		setNegative(result);
	}
	
	private void cmp_print(AddressingMode mode)
	{
		out.println("CMP " + printOperands(mode));
	}
	
	private void cpx(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int result = x - val;
		setZero(result & 0xff);
		result += 256;
		setCarry(result);
		setNegative(result);
	}
	
	private void cpx_print(AddressingMode mode)
	{
		out.println("CPX " + printOperands(mode));
	}
	
	private void cpy(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		int result = y - val;
		setZero(result & 0xff);
		result += 256;
		setCarry(result);
		setNegative(result);
	}
	
	private void cpy_print(AddressingMode mode)
	{
		out.println("CPY " + printOperands(mode));
	}
	
	private void sei()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		setI();
	}
	
	private void sei_print()
	{
		out.println("SEI ");
	}
	
	private void cli()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		clearI();
	}
	
	private void cli_print()
	{
		out.println("CLI ");
	}
	
	private void sec()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		setCarry(0x100);
	}
	
	private void sec_print()
	{
		out.println("SEC ");
	}
	
	private void sed()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		setDecimal();
	}
	
	private void sed_print()
	{
		out.println("SED ");
	}
	
	private void cld()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		clearDecimal();
	}
	
	private void cld_print()
	{
		out.println("CLD ");
	}
	
	private void clv()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		setOverflow(0, 0, 0);
	}
	
	private void clv_print()
	{
		out.println("CLV ");
	}
	
	private void lax(AddressingMode mode)
	{
		int addr = getAddr(mode);
		x = readAddr(addr, mode);
		a = x;
		setNegative(x);
		setZero(x);
	}
	
	private void lax_print(AddressingMode mode)
	{
		out.println("LAX " + printOperands(mode));
	}
	
	private void ldx(AddressingMode mode)
	{
		int addr = getAddr(mode);
		x = readAddr(addr, mode);
		setNegative(x);
		setZero(x);
	}
	
	private void ldx_print(AddressingMode mode)
	{
		out.println("LDX " + printOperands(mode));
	}
	
	private void ldy(AddressingMode mode)
	{
		int addr = getAddr(mode);
		y = readAddr(addr, mode);
		setNegative(y);
		setZero(y);
	}
	
	private void ldy_print(AddressingMode mode)
	{
		out.println("LDY " + printOperands(mode));
	}
	
	private void bit(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int val = readAddr(addr, mode);
		setNegative(val);
		int result = val & a;
		setZero(result);
		
		if ((val & 0x40) != 0)
		{
			setOverflow(0, 0, 0xff);
		} else 
		{
			setOverflow(0, 0, 0);
		}
	}
	
	private void bit_print(AddressingMode mode)
	{
		out.println("BIT " + printOperands(mode));
	}
	
	private void jsr(AddressingMode mode)
	{
		int addr = getAddr(mode);
		--pc;
		pc &= 0xffff;
		stackPush(pc >> 8);
		stackPush(pc & 0xff);
		pc = addr;
		incrementCycle();
	}
	
	private void jsr_print(AddressingMode mode)
	{
		out.println("JSR " + printOperands(mode));
	}
	
	private void rts()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		int low = stackPop();
		int high = stackPop();
		pc = (Utils.makeUnsigned(low, high) + 1) & 0xffff;
		incrementCycle(2);
	}
	
	private void rts_print()
	{
		out.println("RTS ");
	}
	
	private void clc()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		setCarry(0);
	}
	
	private void clc_print()
	{
		out.println("CLC ");
	}
	
	private void bcs(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (getCarry())
		{
			branch(addr);
		}
	}
	
	private void bcs_print(AddressingMode mode)
	{
		out.println("BCS " + printOperands(mode));
	}
	
	private void bcc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (!getCarry())
		{
			branch(addr);
		}
	}
	
	private void bcc_print(AddressingMode mode)
	{
		out.println("BCC " + printOperands(mode));
	}
	
	private void beq(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (getZero())
		{
			branch(addr);
		}
	}
	
	private void beq_print(AddressingMode mode)
	{
		out.println("BEQ " + printOperands(mode));
	}
	
	private void bne(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (!getZero())
		{
			branch(addr);
		}
	}
	
	private void bne_print(AddressingMode mode)
	{
		out.println("BNE " + printOperands(mode));
	}
	
	private void bpl(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (!getNegative())
		{
			branch(addr);
		}
	}
	
	private void bpl_print(AddressingMode mode)
	{
		out.println("BPL " + printOperands(mode));
	}
	
	private void bvs(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (getOverflow())
		{
			branch(addr);
		}
	}
	
	private void bvs_print(AddressingMode mode)
	{
		out.println("BVS " + printOperands(mode));
	}
	
	private void bvc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (!getOverflow())
		{
			branch(addr);
		}
	}
	
	private void bvc_print(AddressingMode mode)
	{
		out.println("BVC " + printOperands(mode));
	}
	
	private void bmi(AddressingMode mode)
	{
		int addr = getAddr(mode);
		if (getNegative())
		{
			branch(addr);
		}
	}
	
	private void bmi_print(AddressingMode mode)
	{
		out.println("BMI " + printOperands(mode));
	}
	
	private void jmp(AddressingMode mode)
	{
		int addr = getAddr(mode);
		pc = addr;
	}
	
	private void jmp_print(AddressingMode mode)
	{
		out.println("JMP " + printOperands(mode));
	}
	
	private void brk()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		++pc;
		pc &= 0xffff;
		stackPush(pc >> 8);
		stackPush(pc & 0xff);
		stackPush(p | 0x30);
		setI();
		int low = memRead(0xfffe);
		int high = memRead(0xffff);
		pc = Utils.makeUnsigned(low, high);
	}
	
	private void brk_print()
	{
		out.println("BRK ");
	}
	
	private void reset()
	{
		if (LOG)
		{
			log.println("RESET");
		}
		
		mem.write(0x4017, ((Register4017)mem.getLayout()[0x4017]).getLastValue());
		incrementCycle(3);
		s -= 3;
		s &= 0xff;
		setI();
		int low = memRead(0xfffc);
		int high = memRead(0xfffd);
		pc = Utils.makeUnsigned(low, high);
		incrementCycle(3);
	}
	
	private void nmi()
	{
		if (LOG)
		{
			log.println("NMI");
		}
		
		stackPush(pc >> 8);
		stackPush(pc & 0xff);
		stackPush((p & 0xef) | 0x20);
		setI();
		int low = memRead(0xfffa);
		int high = memRead(0xfffb);
		pc = Utils.makeUnsigned(low, high);
		incrementCycle(3);
	}
	
	private void irq()
	{
		if (LOG)
		{
			log.println("IRQ");
		}
		
		stackPush(pc >> 8);
		stackPush(pc & 0xff);
		stackPush((p & 0xef) | 0x20);
		setI();
		int low = memRead(0xfffe);
		int high = memRead(0xffff);
		pc = Utils.makeUnsigned(low, high);
		incrementCycle(3);
	}
	
	private void anc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		a = a & arg;
		setNegative(a);
		setZero(a);
		int newA = a << 1;
		setCarry(newA);
	}
	
	private void anc_print(AddressingMode mode)
	{
		out.println("ANC " + printOperands(mode));
	}
	
	private void asl(AddressingMode mode)
	{
		if (mode == AddressingMode.ACCUMULATOR)
		{
			++pc;
			pc &= 0xffff;
			memRead(pc);
			int newA = a << 1;
			a = newA & 0xff;
			setNegative(a);
			setZero(a);
			setCarry(newA);
		}
		else
		{
			int addr = getAddr(mode);
			int arg = readAddr(addr, mode);
			int newArg = arg << 1;
			memWrite(addr, arg);
			memWrite(addr, newArg & 0xff);
			setNegative(newArg & 0xff);
			setZero(newArg & 0xff);
			setCarry(newArg);
		}
	}
	
	private void asl_print(AddressingMode mode)
	{
		out.println("ASL " + printOperands(mode));
	}
	
	private void dec(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		int newArg = arg - 1;
		newArg &= 0xff;
		memWrite(addr, arg);
		memWrite(addr, newArg);
		setNegative(newArg);
		setZero(newArg);
	}
	
	private void dec_print(AddressingMode mode)
	{
		out.println("DEC " + printOperands(mode));
	}
	
	private void isc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		int newArg = (arg + 1) & 0xff;
		memWrite(addr, arg);
		memWrite(addr, newArg);
		
		newArg = (~newArg) & 0xff;
		int result = a + newArg;
		if (getCarry())
		{
			++result;
		}
		
		setZero(result & 0xff);
		setCarry(result);
		setNegative(result & 0xff);
		setOverflow(a, newArg, result & 0xff);
		a = result & 0xff;
	}
	
	private void isc_print(AddressingMode mode)
	{
		out.println("ISC " + printOperands(mode));
	}
	
	private void inc(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		int newArg = (arg + 1) & 0xff;
		memWrite(addr, arg);
		memWrite(addr, newArg);
		setNegative(newArg);
		setZero(newArg);
	}
	
	private void inc_print(AddressingMode mode)
	{
		out.println("INC " + printOperands(mode));
	}
	
	private void iny()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		++y;
		y &= 0xff;
		setZero(y);
		setNegative(y);
	}
	
	private void iny_print()
	{
		out.println("INY ");
	}
	
	private void dey()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		--y;
		y &= 0xff;
		setZero(y);
		setNegative(y);
	}
	
	private void dey_print()
	{
		out.println("DEY ");
	}
	
	private void inx()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		++x;
		x &= 0xff;
		setZero(x);
		setNegative(x);
	}
	
	private void inx_print()
	{
		out.println("INX ");
	}
	
	private void dex()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		--x;
		x &= 0xff;
		setZero(x);
		setNegative(x);
	}
	
	private void dex_print()
	{
		out.println("DEX ");
	}
	
	private void rol(AddressingMode mode)
	{
		if (mode == AddressingMode.ACCUMULATOR)
		{
			++pc;
			pc &= 0xffff;
			memRead(pc);
			boolean c = getCarry();
			int newA = a << 1;
			if (c)
			{
				++newA;
			}
			
			a = newA & 0xff;
			setNegative(a);
			setZero(a);
			setCarry(newA);
		}
		else
		{
			int addr = getAddr(mode);
			int arg = readAddr(addr, mode);
			boolean c = getCarry();
			int newArg = arg << 1;
			if (c)
			{
				++newArg;
			}
			
			memWrite(addr, arg);
			memWrite(addr, newArg & 0xff);
			setNegative(newArg & 0xff);
			setZero(newArg & 0xff);
			setCarry(newArg);
		}
	}
	
	private void rol_print(AddressingMode mode)
	{
		out.println("ROL " + printOperands(mode));
	}
	
	private void lsr(AddressingMode mode)
	{
		if (mode == AddressingMode.ACCUMULATOR)
		{
			++pc;
			pc &= 0xffff;
			memRead(pc);
			int newA = a >> 1;
			
			if ((a & 0x01) != 0)
			{
				setCarry(0x100);
			}
			else
			{
				setCarry(0);
			}
			
			a = newA;
			setNegative(a);
			setZero(a);
		}
		else
		{
			int addr = getAddr(mode);
			int arg = readAddr(addr, mode);
			
			int newArg = arg >> 1;
			memWrite(addr, arg);
			memWrite(addr, newArg);
			setNegative(newArg);
			setZero(newArg);
			
			if ((arg & 0x01) != 0)
			{
				setCarry(0x100);
			}
			else
			{
				setCarry(0);
			}
		}
	}
	
	private void lsr_print(AddressingMode mode)
	{
		out.println("LSR " + printOperands(mode));
	}
	
	private void rra(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		
		int newArg = arg >> 1;
		if (getCarry())
		{
			newArg += 0x80;
		}
		
		memWrite(addr, arg);
		memWrite(addr, newArg);
		
		if ((arg & 0x01) != 0)
		{
			setCarry(0x100);
		}
		else
		{
			setCarry(0);
		}
		
		int result = a + newArg;
		if (getCarry())
		{
			++result;
		}
		
		setZero(result);
		setCarry(result);
		setNegative(result);
		setOverflow(a, newArg, result);
		a = result;
	}
	
	private void rra_print(AddressingMode mode)
	{
		out.println("RRA " + printOperands(mode));
	}
	
	private void ror(AddressingMode mode)
	{
		if (mode == AddressingMode.ACCUMULATOR)
		{
			++pc;
			pc &= 0xffff;
			memRead(pc);
			int newA = a >> 1;
			if (getCarry())
			{
				newA += 0x80;
			}
			
			if ((a & 0x01) != 0)
			{
				setCarry(0x100);
			}
			else
			{
				setCarry(0);
			}
			
			a = newA;
			setNegative(a);
			setZero(a);
		}
		else
		{
			int addr = getAddr(mode);
			int arg = readAddr(addr, mode);
			
			int newArg = arg >> 1;
			if (getCarry())
			{
				newArg += 0x80;
			}
			
			memWrite(addr, arg);
			memWrite(addr, newArg);
			setNegative(newArg);
			setZero(newArg);
			
			if ((arg & 0x01) != 0)
			{
				setCarry(0x100);
			}
			else
			{
				setCarry(0);
			}
		}
	}
	
	private void ror_print(AddressingMode mode)
	{
		out.println("ROR " + printOperands(mode));
	}
	
	private void ora(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		a = a | arg;
		setNegative(a);
		setZero(a);
	}
	
	private void ora_print(AddressingMode mode)
	{
		out.println("ORA " + printOperands(mode));
	}
	
	private void and(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		a = a & arg;
		setNegative(a);
		setZero(a);
	}
	
	private void and_print(AddressingMode mode)
	{
		out.println("AND " + printOperands(mode));
	}
	
	private void alr(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		arg = a & arg;
		int newArg = arg >> 1;
		setNegative(newArg);
		setZero(newArg);
		
		if ((arg & 0x01) != 0)
		{
			setCarry(0x100);
		}
		else
		{
			setCarry(0);
		}
	}
	
	private void alr_print(AddressingMode mode)
	{
		out.println("ALR " + printOperands(mode));
	}
	
	private void eor(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		a = a ^ arg;
		setNegative(a);
		setZero(a);
	}
	
	private void eor_print(AddressingMode mode)
	{
		out.println("EOR " + printOperands(mode));
	}
	
	private void php()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		int val = (p | 0x30);
		stackPush(val);
	}
	
	private void php_print()
	{
		out.println("PHP ");
	}
	
	private void pha()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		stackPush(a);
	}
	
	private void pha_print()
	{
		out.println("PHA ");
	}
	
	private void plp()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		p = stackPop() & 0xcf;
		incrementCycle();
	}
	
	private void plp_print()
	{
		out.println("PLP ");
	}
	
	private void pla()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		a = stackPop();
		setZero(a);
		setNegative(a);
		incrementCycle();
	}
	
	private void pla_print()
	{
		out.println("PLA ");
	}
	
	private void tax()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		x = a;
		setNegative(x);
		setZero(x);
	}
	
	private void tax_print()
	{
		out.println("TAX ");
	}
	
	private void tay()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		y = a;
		setNegative(y);
		setZero(y);
	}
	
	private void tay_print()
	{
		out.println("TAY ");
	}
	
	private void txa()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		a = x;
		setNegative(a);
		setZero(a);
	}
	
	private void txa_print()
	{
		out.println("TXA ");
	}
	
	private void tya()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		a = y;
		setNegative(a);
		setZero(a);
	}
	
	private void tya_print()
	{
		out.println("TYA ");
	}
	
	private void txs()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		s = x;
	}
	
	private void txs_print()
	{
		out.println("TXS ");
	}
	
	private void tsx()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		x = s;
		setNegative(x);
		setZero(x);
	}
	
	private void tsx_print()
	{
		out.println("TSX ");
	}
	
	private void rti()
	{
		++pc;
		pc &= 0xffff;
		memRead(pc);
		p = stackPop() & 0xcf;
		int low = stackPop();
		int high = stackPop();
		pc = Utils.makeUnsigned(low, high);
		incrementCycle();
	}
	
	private void rti_print()
	{
		out.println("RTI ");
	}
	
	private void slo(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		
		int newArg = arg << 1;
		memWrite(addr, arg);
		memWrite(addr, newArg & 0xff);
		a = a | (newArg & 0xff);
		setNegative(a);
		setZero(a);
		setCarry(newArg);
	}
	
	private void slo_print(AddressingMode mode)
	{
		out.println("SLO " + printOperands(mode));
	}
	
	private void sre(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		
		int newArg = arg >> 1;
		memWrite(addr, arg);
		memWrite(addr, newArg);
		a = a ^ newArg;
		setNegative(a);
		setZero(a);
		
		if ((arg & 0x01) != 0)
		{
			setCarry(0x100);
		}
		else
		{
			setCarry(0);
		}
	}
	
	private void sre_print(AddressingMode mode)
	{
		out.println("SRE " + printOperands(mode));
	}
	
	private void rla(AddressingMode mode)
	{
		int addr = getAddr(mode);
		int arg = readAddr(addr, mode);
		boolean c = getCarry();
		
		int newArg = arg << 1;
		if (c)
		{
			++newArg;
		}
		
		memWrite(addr, arg);
		memWrite(addr, newArg & 0xff);
		a = a & (newArg & 0xff);
		setNegative(a);
		setZero(a);
		setCarry(newArg);
	}
	
	private void rla_print(AddressingMode mode)
	{
		out.println("RLA " + printOperands(mode));
	}
	
	private void nop(AddressingMode mode)
	{
		if (mode == AddressingMode.IMPLIED)
		{
			++pc;
			pc &= 0xffff;
			incrementCycle();
		}
		else
		{
			int addr = getAddr(mode);
			readAddr(addr, mode);
		}
	}
	
	private void nop_print(AddressingMode mode)
	{
		out.println("NOP " + printOperands(mode));
	}
	
	private void halt()
	{
		synchronized(System.out) 
		{
			System.out.println("CPU halted");
		}
		
		while (true) 
		{
			if (reset.get())
			{
				break;
			}
			
			if (terminate.get())
			{
				return;
			}
		}
	}
	
	private void halt_print()
	{
		out.println("HALT ");
	}
	
	private int readAddr(int addr, AddressingMode mode)
	{
		if (mode == AddressingMode.IMMEDIATE)
		{
			return addr;
		}
		
		return memRead(addr);
	}
	
	private int getAddr(AddressingMode mode)
	{
		int low, high, addr, arg, newAddr;
		int plus1 = (pc + 1) & 0xffff;
		int plus2 = 0;
		switch(mode)
		{
		case ABSOLUTE:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			pc += 3;
			pc &= 0xffff;
			return addr;
		case INDIRECT:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			low = memRead(addr);
			high = memRead(addr+1);
			pc += 3;
			pc &= 0xffff;
			return Utils.makeUnsigned(low, high);
		case ABSOLUTEX:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			newAddr = (addr + x) & 0xffff;
			readOops(addr, newAddr);
			pc += 3;
			pc &= 0xffff;
			return newAddr;
		case ABSOLUTEY:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			newAddr = (addr + y) & 0xffff;
			readOops(addr, newAddr);
			pc += 3;
			pc &= 0xffff;
			return newAddr;
		case ABSOLUTEX_WRITE:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			newAddr = (addr + x) & 0xffff;
			memRead((newAddr & 0xff) + (addr & 0xff00));
			pc += 3;
			pc &= 0xffff;
			return newAddr;
		case ABSOLUTEY_WRITE:
			plus2 = (pc + 2) & 0xffff;
			low = memRead(plus1); //2nd cycle
			high = memRead(plus2); //3rd cycle
			addr = Utils.makeUnsigned(low, high);
			newAddr = (addr + y) & 0xffff;
			memRead((newAddr & 0xff) + (addr & 0xff00));
			pc += 3;
			pc &= 0xffff;
			return newAddr;
		case IMMEDIATE:
			addr = memRead(plus1); //2nd cycle
			pc += 2;
			pc &= 0xffff;
			return addr;
		case INDIRECTX:
			arg = memRead(plus1);
			pc += 2;
			pc &= 0xffff;
			return indirectXAddress(arg);
		case INDIRECTY:
			arg = memRead(plus1);
			pc += 2;
			pc &= 0xffff;
			return indirectYAddress(arg);
		case INDIRECTY_WRITE:
			arg = memRead(plus1);
			pc += 2;
			pc &= 0xffff;
			return indirectYAddressWrite(arg);
		case ZEROPAGE:
			addr = memRead(plus1); //2nd cycle
			pc += 2;
			pc &= 0xffff;
			return addr;
		case ZEROPAGEX:
			arg = memRead(plus1);
			addr = (arg + x) & 0xff;
			pc += 2;
			pc &= 0xffff;
			incrementCycle();
			return addr;
		case ZEROPAGEY:
			arg = memRead(plus1);
			addr = (arg + y) & 0xff;
			pc += 2;
			pc &= 0xffff;
			incrementCycle();
			return addr;
		case RELATIVE:
			arg = memRead(plus1);
			pc += 2;
			pc &= 0xffff;
			return (pc + (byte)arg) & 0xffff;
		default:
			break;
		}
		
		return 0;
	}
	
	private String printOperands(AddressingMode mode)
	{
		Integer byte1 = null;
		Integer byte2 = null;
		String type = null;
		int plus1 = (pc + 1) & 0xffff;
		int plus2 = (pc + 2) & 0xffff;
		switch(mode)
		{
		case ABSOLUTE:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "ABSOLUTE";
			break;
		case INDIRECT:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "INDIRECT";
			break;
		case ABSOLUTEX:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "ABSOLUTEX";
			break;
		case ABSOLUTEY:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "ABSOLUTEY";
			break;
		case ABSOLUTEX_WRITE:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "ABSOLUTEX";
			break;
		case ABSOLUTEY_WRITE:
			byte1 = mem.read(plus1); 
			byte2 = mem.read(plus2);
			type = "ABSOLUTEY";
			break;
		case IMMEDIATE:
			byte1 = mem.read(plus1); 
			type = "IMMEDIATE";
			break;
		case INDIRECTX:
			byte1 = mem.read(plus1); 
			type = "INDIRECTX";
			break;
		case INDIRECTY:
			byte1 = mem.read(plus1); 
			type = "INDIRECTY";
			break;
		case INDIRECTY_WRITE:
			byte1 = mem.read(plus1); 
			type = "INDIRECTY";
			break;
		case ZEROPAGE:
			byte1 = mem.read(plus1); 
			type = "ZEROPAGE";
			break;
		case ZEROPAGEX:
			byte1 = mem.read(plus1); 
			type = "ZEROPAGEX";
			break;
		case ZEROPAGEY:
			byte1 = mem.read(plus1); 
			type = "ZEROPAGEY";
			break;
		case RELATIVE:
			byte1 = mem.read(plus1); 
			type = "RELATIVE";
			break;
		case ACCUMULATOR:
			type = "ACCUMULATOR";
		default:
			break;
		}
		
		String retval = "";
		if (byte1 != null)
		{
			retval += String.format("0x%02X", byte1) + " ";
		}
		
		if (byte2 != null)
		{
			retval += String.format("0x%02X", byte2) + " ";
		}
		
		if (type != null)
		{
			retval += ("(" + type + ")");
		}
		
		return retval;
	}
	
	private void readOops(int addr1, int addr2)
	{
		if ((addr1 & 0xff00) != (addr2 & 0xff00))
		{
			incrementCycle();
		}
	}
	
	private int indirectXAddress(int arg)
	{
		int low = memRead((arg + x) & 0xff);
		int high = memRead((arg + x + 1) & 0xff);
		incrementCycle();
		return Utils.makeUnsigned(low, high);
	}
	
	private int indirectYAddress(int arg)
	{
		int low = memRead(arg);
		int high = memRead((arg + 1) & 0xff);
		int addr = Utils.makeUnsigned(low, high);
		int newAddr = (addr + y) & 0xffff;
		readOops(addr, newAddr);
		return newAddr;
	}
	
	private int indirectYAddressWrite(int arg)
	{
		int low = memRead(arg);
		int high = memRead((arg + 1) & 0xff);
		int addr = Utils.makeUnsigned(low, high);
		int newAddr = (addr + y) & 0xffff;
		memRead((newAddr & 0xff) + (addr & 0xff00));
		return newAddr;
	}
	
	public int memRead(int address)
	{
		int b = mem.read(address);
		incrementCycle();
		return b;
	}
	
	private void memWrite(int address, int val)
	{
		mem.write(address, val);
		incrementCycle();
	}
	
	public void incrementCycle(int count)
	{
		for (int i = 0; i < count; ++i)
		{
			incrementCycle();
		}
	}
	
	public void incrementCycle()
	{
		cycle += 3;
		clock.setCpuExpectedCycle(cycle);
		
		if (!stepFlag.get())
		{
			long expected = clock.getPpuExpectedCycle();
			while (expected < cycle) 
			{
				if (terminate.get())
				{
					return;
				}
				
				expected = clock.getPpuExpectedCycle();
			}
			
			if (expected - cycle > 3)
			{
				System.out.println(expected - cycle);
			}
		}
		
		if (overage > 0)
		{
			overage -= 3;
			return;
		}
		
		long current = clock.cycle();
		while (current < cycle) {
			current = clock.cycle();
		}
		
		overage += (current - cycle);
	}
	
	private void branch(int addr)
	{
		readOops(pc, addr);
		pc = addr;
		incrementCycle();
	}
	
	private void stackPush(int val)
	{
		memWrite(0x100 + s, val);
		--s;
		s &= 0xff;
	}
	
	private int stackPop()
	{
		++s;
		s &= 0xff;
		return memRead(0x100 + s);
	}
	
	private void setNegative(int val)
	{
		if ((val & 0x80) != 0)
		{
			p |= 0x80;
		} else {
			p &= 0x7f;
		}
	}
	
	private boolean getNegative()
	{
		return (p & 0x80) != 0;
	}
	
	private void setZero(int val)
	{
		if (val == 0)
		{
			p |= 0x02;
		} else {
			p &= 0xfd;
		}
	}
	
	private boolean getZero()
	{
		return (p & 0x02) != 0;
	}
	
	private void setCarry(int val)
	{
		if (val > 0xff)
		{
			p |= 0x01;
		} else {
			p &= 0xfe;
		}
	}
	
	private boolean getCarry()
	{
		return (p & 0x01) != 0;
	}
	
	private void setOverflow(int val1, int val2, int result)
	{
		boolean overflow = (~(val1 ^ val2) & (val1 ^ result) & 0x80) != 0;
		
		if (overflow)
		{
			p |= 0x40;
		} else
		{
			p &= 0xbf;
		}
	}
	
	private boolean getOverflow()
	{
		return (p & 0x40) != 0;
	}
	
	private void setI()
	{
		p |= 0x04;
	}
	
	private void clearI()
	{
		p &= 0xfb;
	}
	
	private boolean getI()
	{
		return (p & 0x04) != 0;
	}
	
	private void setDecimal()
	{
		p |= 0x08;
	}
	
	private void clearDecimal()
	{
		p &= 0xf7;
	}
}
