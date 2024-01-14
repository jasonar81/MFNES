
public class APU implements Runnable {
	private CPU cpu;
	private GUI gui;
	private Clock clock;
	private long previous = -3;
	private int audioEnableFlags = 0;
	private boolean dmcInterruptFlag = false;
	private boolean frameInterruptFlag = false;
	private int pulse1LengthCounter = 0;
	private int pulse2LengthCounter = 0;
	private int triangleLengthCounter = 0;
	private int noiseLengthCounter = 0;
	private int audioTimerRegister = 0;
	private int audioTimer = 0;
	private int qfCounter = 0;
	private int hfCounter = 0;
	private int audioCheck = 0;
	
	private boolean triangleCounterHold = false;
	private int triangleLinearCounterReloadValue = 0;
	private int triangleInternalTimer = 0;
	private int[] lengthLookup = new int[] {10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14, 12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30}; 
	private boolean linearCounterReloadFlag = false;
	private int triangleLinearCounter = 0;
	private int triangleInternalCounter = 0;
	private int triangleInternalIndex = 0;
	private int[] triangleLookup = new int[] {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	
	private int pulse1DutyCycle = 0;
	private int pulse2DutyCycle = 0;
	private boolean pulse1CounterHold = false;
	private boolean pulse2CounterHold = false;
	private int pulse1VolumeDivider = 0;
	private int pulse2VolumeDivider = 0;
	private boolean pulse1ConstantVol = false;
	private boolean pulse2ConstantVol = false;
	private int pulse1InternalTimer = 0;
	private int pulse2InternalTimer = 0;
	private int pulse1InternalIndex = 0;
	private int pulse2InternalIndex = 0;
	private int[] pulseLookup0 = new int[] {0, 1, 0, 0, 0, 0, 0, 0};
	private int[] pulseLookup1 = new int[] {0, 1, 1, 0, 0, 0, 0, 0}; 
	private int[] pulseLookup2 = new int[] {0, 1, 1, 1, 1, 0, 0, 0};
	private int[] pulseLookup3 = new int[] {1, 0, 0, 1, 1, 1, 1, 1};
	private int pulse1InternalCounter = 0;
	private int pulse2InternalCounter = 0;
	private int pulse1DividerCounter = 0;
	private int pulse2DividerCounter = 0;
	private int pulse1DecayCounter = 0;
	private int pulse2DecayCounter = 0;
	private boolean pulse1StartFlag = false;
	private boolean pulse2StartFlag = false;
	private boolean pulse1SweepEnable = false;
	private boolean pulse2SweepEnable = false;
	private boolean pulse1SweepNegate = false;
	private boolean pulse2SweepNegate = false;
	private int pulse1SweepShift = 0;
	private int pulse2SweepShift = 0;
	private int pulse1SweepReload = 0;
	private int pulse2SweepReload = 0;
	private int pulse1SweepCounter = 0;
	private int pulse2SweepCounter = 0;
	private boolean pulse1SweepReloadFlag = false;
	private boolean pulse2SweepReloadFlag = false;
	
	private int noiseDividerCounter = 0;
	private int noiseDecayCounter = 0;
	private boolean noiseStartFlag = false;
	private int noiseVolumeDivider = 0;
	private boolean noiseCounterHold = false;
	private boolean noiseConstantVol = false;
	private boolean noiseMode = false;
	private int noisePeriod = 0;
	private int[] noiseLookup = new int[] { 4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068};
	private int noiseReload = 0;
	private int noiseInternal = 1;
	
	private boolean dmcIrqEnabled = false;
	private boolean dmcLoop = false;
	private int dmcRate = 0;
	private int[] dmcLookup = new int[] {428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106,  84,  72,  54};
	private int dmcChannelValue = 0;
	private int dmcSampleAddress = 0;
	private int dmcStartSampleAddress = 0;
	private int dmcSampleLength = 0;
	private int dmcSamplesRemaining = 0;
	private int dmcBitsRemainingCounter = 8;
	private int dmcInternalCounter = 0;
	private int dmcSampleBufferValue = 0;
	private boolean dmcSampleBufferLoaded = false;
	private boolean dmcSilenceFlag = false;
	private int dmcOutputUnit = 0;
	private int dmcLoadCountdown = 0;
	
	private int pulse1Value;
	private int pulse2Value;
	private int triangleValue;
	
	private volatile boolean terminate = false;
	private volatile boolean reset = false;
	
	public APU(CPU cpu, GUI gui, Clock clock) 
	{
		this.cpu = cpu;
		this.gui = gui;
		this.clock = clock;
	}
	
	public void run()
	{
		while (!terminate)
		{
			long current = clock.getCpuExpectedCycle();
			for (long i = previous + 3; i <= current; i += 3)
			{
				runPerCycleAudioLogic(i);
			}
			previous = current;
			
			if (reset)
			{
				reset();
			}
		}
	}
	
	public void setReset()
	{
		reset = true;
	}
	
	public void terminate()
	{
		terminate = true;
	}
	
	public void setNoiseLengthCounter(int val)
	{
		noiseLengthCounter = val;
	}
	
	public void setDmcSampleAddress(int val)
	{
		dmcSampleAddress = 0xc000 + val * 64;
		dmcStartSampleAddress = dmcSampleAddress;
	}
	
	public void zeroDmcRemainingSamples()
	{
		dmcSamplesRemaining = 0;
	}
	
	public void setDmcSampleLength(int val)
	{
		dmcSampleLength = val * 16 + 1;
		dmcSamplesRemaining = dmcSampleLength;
	}
	
	public void setDmcChannelValue(int val)
	{
		dmcChannelValue = val;
	}
	
	public void setDmcIrqEnabled(boolean val)
	{
		dmcIrqEnabled = val;
	}
	
	public void setDmcLoop(boolean val)
	{
		dmcLoop = val;
	}
	
	public void setDmcRate(int val)
	{
		dmcRate = dmcLookup[val];
	}
	
	public void setNoiseReload(int val)
	{
		noiseReload = val;
	}
	
	public void setNoiseStartFlag()
	{
		noiseStartFlag = true;
	}
	
	public void setNoisePeriod(int val)
	{
		noisePeriod = noiseLookup[val];
		noiseReload = noisePeriod;
	}
	
	public void setNoiseMode(boolean val)
	{
		noiseMode = val;
	}
	
	public void setNoiseVolumeDivider(int val)
	{
		noiseVolumeDivider = val;
	}
	
	public void setNoiseHoldValue(boolean val)
	{
		noiseCounterHold = val;
	}
	
	public void setNoiseConstantVol(boolean val)
	{
		noiseConstantVol = val;
	}
	
	public void setPulse1SweepReloadFlag()
	{
		pulse1SweepReloadFlag = true;
	}
	
	public void setPulse2SweepReloadFlag()
	{
		pulse2SweepReloadFlag = true;
	}
	
	public void setPulse1SweepShift(int val)
	{
		pulse1SweepShift = val;
	}
	
	public void setPulse2SweepShift(int val)
	{
		pulse2SweepShift = val;
	}
	
	public void setPulse1SweepNegate(boolean val)
	{
		pulse1SweepNegate = val;
	}
	
	public void setPulse2SweepNegate(boolean val)
	{
		pulse2SweepNegate = val;
	}
	
	public void setPulse1SweepReload(int val)
	{
		pulse1SweepReload = val;
	}
	
	public void setPulse1SweepCounter(int val)
	{
		pulse1SweepCounter = val;
	}
	
	public void setPulse2SweepReload(int val)
	{
		pulse2SweepReload = val;
	}
	
	public void setPulse2SweepCounter(int val)
	{
		pulse2SweepCounter = val;
	}
	
	public void setPulse1SweepEnabled(boolean val)
	{
		pulse1SweepEnable = val;
	}
	
	public void setPulse2SweepEnabled(boolean val)
	{
		pulse2SweepEnable = val;
	}
	
	public void setPulse1StartFlag()
	{
		pulse1StartFlag = true;
	}
	
	public void setPulse2StartFlag()
	{
		pulse2StartFlag = true;
	}
	
	public void setPulse1InternalIndex(int val)
	{
		pulse1InternalIndex = val;
	}
	
	public void setPulse2InternalIndex(int val)
	{
		pulse2InternalIndex = val;
	}
	
	public void setPulse1LengthCounter(int val)
	{
		pulse1LengthCounter = val;
	}
	
	public void setPulse2LengthCounter(int val)
	{
		pulse2LengthCounter = val;
	}
	
	public int getPulse1InternalTimer()
	{
		return pulse1InternalTimer;
	}
	
	public int getPulse2InternalTimer()
	{
		return pulse2InternalTimer;
	}
	
	public void setPulse1InternalTimer(int val)
	{
		pulse1InternalTimer = val;
	}
	
	public void setPulse2InternalTimer(int val)
	{
		pulse2InternalTimer = val;
	}
	
	public void setPulse1DutyCycle(int val)
	{
		pulse1DutyCycle = val;
	}
	
	public void setPulse2DutyCycle(int val)
	{
		pulse2DutyCycle = val;
	}
	
	public void setPulse1HoldValue(boolean val)
	{
		pulse1CounterHold = val;
	}
	
	public void setPulse2HoldValue(boolean val)
	{
		pulse2CounterHold = val;
	}
	
	public void setPulse1ConstantVol(boolean val)
	{
		pulse1ConstantVol = val;
	}
	
	public void setPulse2ConstantVol(boolean val)
	{
		pulse2ConstantVol = val;
	}
	
	public void setPulse1VolumeDivider(int val)
	{
		pulse1VolumeDivider = val;
	}
	
	public void setPulse2VolumeDivider(int val)
	{
		pulse2VolumeDivider = val;
	}
	
	public void setLinearCounterReloadFlag(boolean val)
	{
		linearCounterReloadFlag = val;
	}
	
	public boolean getLinearCounterReloadFlag()
	{
		return linearCounterReloadFlag;
	}
	
	public int lengthCounterLookup(int val)
	{
		return lengthLookup[val];
	}
	
	public void setTriangleLengthCounter(int val)
	{
		triangleLengthCounter = val;
	}
	
	public int getTriangleInternalTimer()
	{
		return triangleInternalTimer;
	}
	
	public void setTriangleInternalTimer(int val)
	{
		triangleInternalTimer = val;
	}
	
	public void setTriangleHoldValue(boolean val)
	{
		triangleCounterHold = val;
	}
	
	public void setTriangleLinearCounterReloadValue(int val)
	{
		triangleLinearCounterReloadValue = val;
	}
	
	public void setAudioTimerRegister(int val)
	{
		audioTimerRegister = val;
	}
	
	public void setAudioTimer(int val)
	{
		audioTimer = val;
	}
	
	public void setQuarterFrameCounter(int val)
	{
		qfCounter = val;
	}
	
	public void setHalfFrameCounter(int val)
	{
		hfCounter = val;
	}
	
	public void setNextAudioTimerCheckpoint(int val)
	{
		audioCheck = val;
	}
	
	public void clearDmcInterruptFlag()
	{
		dmcInterruptFlag = false;
	}
	
	public void clearFrameInterruptFlag()
	{
		frameInterruptFlag = false;
	}
	
	public boolean getDmcInterruptFlag()
	{
		return dmcInterruptFlag;
	}
	
	public boolean getFrameInterruptFlag()
	{
		return frameInterruptFlag;
	}
	
	public int getPulse1LengthCounter()
	{
		return pulse1LengthCounter;
	}
	
	public int getPulse2LengthCounter()
	{
		return pulse2LengthCounter;
	}
	
	public int getTriangleLengthCounter()
	{
		return triangleLengthCounter;
	}
	
	public int getNoiseLengthCounter()
	{
		return noiseLengthCounter;
	}
	
	public int getDmcSamplesRemaining()
	{
		return dmcSamplesRemaining;
	}
	
	public void setAudioEnableFlags(int val)
	{
		audioEnableFlags = val;
	}
	
	public int getAudioEnableFlags()
	{
		return audioEnableFlags;
	}
	
	public void reset()
	{
		audioEnableFlags = 0;
		clearDmcInterruptFlag();
		clearFrameInterruptFlag();
		triangleInternalIndex = 0;
		dmcOutputUnit &= 0x03;
		previous = -1;
		reset = false;
	}
	
	public void runPerCycleAudioLogic(long cycle)
	{	
		pulse1Value = 0;
		int pt = pulse1InternalTimer;
		if (pulse1SweepEnable)
		{
			pt = (pt >> 3);
			if (pulse1SweepNegate)
			{
				pt *= -1;
				--pt;
			}
			
			pt += pulse1InternalTimer;
			pt &= 0xffff;
			
			if (pt > 0x7ff)
			{
				pt = 0;
			}
		}
		
		if (Utils.getBit(audioEnableFlags, 0) && pulse1LengthCounter > 0 && pt >= 8)
		{
			if (pulse1DutyCycle == 0)
			{
				pulse1Value = pulseLookup0[pulse1InternalIndex];
			} else if (pulse1DutyCycle == 1)
			{
				pulse1Value = pulseLookup1[pulse1InternalIndex];
			} else if (pulse1DutyCycle == 2)
			{
				pulse1Value = pulseLookup2[pulse1InternalIndex];
			} else
			{
				pulse1Value = pulseLookup3[pulse1InternalIndex];
			}
			
			++pulse1InternalCounter;
			if (pulse1InternalCounter > 2 * pt + 1)
			{
				pulse1InternalCounter = 0;
				++pulse1InternalIndex;
				pulse1InternalIndex &= 0x07;
			}
			
			if (pulse1ConstantVol)
			{
				pulse1Value *= pulse1VolumeDivider;
			}
			else
			{
				pulse1Value *= pulse1DecayCounter;
			}
		}
		
		pulse2Value = 0;
		pt = pulse2InternalTimer;
		if (pulse2SweepEnable)
		{
			pt = (pt >> 3);
			if (pulse2SweepNegate)
			{
				pt *= -1;
			}
			
			pt += pulse2InternalTimer;
			pt &= 0xffff;
			
			if (pt > 0x7ff)
			{
				pt = 0;
			}
		}
		
		if (Utils.getBit(audioEnableFlags, 1) && pulse2LengthCounter > 0 && pt >= 8)
		{
			if (pulse2DutyCycle == 0)
			{
				pulse2Value = pulseLookup0[pulse2InternalIndex];
			} else if (pulse2DutyCycle == 1)
			{
				pulse2Value = pulseLookup1[pulse2InternalIndex];
			} else if (pulse2DutyCycle == 2)
			{
				pulse2Value = pulseLookup2[pulse2InternalIndex];
			} else
			{
				pulse2Value = pulseLookup3[pulse2InternalIndex];
			}
			
			++pulse2InternalCounter;
			if (pulse2InternalCounter > 2 * pt + 1)
			{
				pulse2InternalCounter = 0;
				++pulse2InternalIndex;
				pulse2InternalIndex &= 0x07;
			}
			
			if (pulse2ConstantVol)
			{
				pulse2Value *= pulse2VolumeDivider;
			}
			else
			{
				pulse2Value *= pulse2DecayCounter;
			}
		}
		
		triangleValue = triangleLookup[triangleInternalIndex];
		if (Utils.getBit(audioEnableFlags, 2) && triangleLengthCounter > 0 && triangleLinearCounter > 0)
		{
			++triangleInternalCounter;
			if (triangleInternalCounter > triangleInternalTimer)
			{
				triangleInternalCounter = 0;
				++triangleInternalIndex;
				triangleInternalIndex &= 0x1f;
			}
		}
		
		++audioTimer;
		if (audioTimer == audioCheck)
		{
			if (audioCheck == 0)
			{
				audioCheck = 7457;
				++qfCounter;
				++hfCounter;
				decrementLengthCounters();
				updateLinearCounter();
				updateEnvelopes();
				updateSweeps();
			}
			else if (audioCheck == 7457)
			{
				audioCheck = 14913;
				++qfCounter;
				updateLinearCounter();
				updateEnvelopes();
			} else if (audioCheck == 14913)
			{
				audioCheck = 22371;
				++qfCounter;
				++hfCounter;
				decrementLengthCounters();
				updateLinearCounter();
				updateEnvelopes();
				updateSweeps();
			} else if (audioCheck == 22371)
			{
				++qfCounter;
				updateLinearCounter();
				updateEnvelopes();
				if (Utils.getBit(audioTimerRegister, 7))
				{
					audioCheck = 37281;
				}
				else
				{
					audioCheck = 29828;
				}
			} else if (audioCheck == 29828)
			{
				if (!Utils.getBit(audioTimerRegister, 6))
				{
					frameInterruptFlag = true;
					cpu.setIrq();
				}
				
				audioCheck = 29829;
			}
			else if (audioCheck == 29829)
			{
				if (!Utils.getBit(audioTimerRegister, 6))
				{
					frameInterruptFlag = true;
					cpu.setIrq();
				}
				
				audioCheck = 29830;
				++qfCounter;
				++hfCounter;
				decrementLengthCounters();
				updateLinearCounter();
				updateEnvelopes();
				updateSweeps();
			}
			else if (audioCheck == 29830)
			{
				if (!Utils.getBit(audioTimerRegister, 6))
				{
					frameInterruptFlag = true;
					cpu.setIrq();
				}
				
				qfCounter = 0;
				hfCounter = 0;
				audioCheck = 7457;
				audioTimer = 0;
			}
			else if (audioCheck == 37281)
			{
				audioCheck = 37282;
				++qfCounter;
				++hfCounter;
				decrementLengthCounters();
				updateLinearCounter();
				updateEnvelopes();
				updateSweeps();
			} else if (audioCheck == 37282)
			{
				qfCounter = 0;
				hfCounter = 0;
				audioCheck = 7457;
				audioTimer = 0;
			}
		}
		
		--noisePeriod;
		if (noisePeriod == 0)
		{
			boolean f = false;
			if (noiseMode)
			{
				boolean b0 = Utils.getBit(noiseInternal, 0);
				boolean b6 = Utils.getBit(noiseInternal, 6);
				f = ((b0 && !b6) || (!b0 && b6));
			}
			else
			{
				boolean b0 = Utils.getBit(noiseInternal, 0);
				boolean b1 = Utils.getBit(noiseInternal, 1);
				f = ((b0 && !b1) || (!b0 && b1));
			}
			
			noiseInternal >>= 1;
			if (f)
			{
				noiseInternal = Utils.setBit(noiseInternal, 14);
			}
			
			noisePeriod = noiseReload;
		}
		
		++dmcInternalCounter;
		if (dmcInternalCounter == dmcRate)
		{
			if (Utils.getBit(dmcOutputUnit, 0) && !dmcSilenceFlag)
			{
				if (dmcChannelValue <= 125)
				{
					dmcChannelValue += 2;
				}
			} else if (!Utils.getBit(dmcOutputUnit, 0) && !dmcSilenceFlag)
			{
				if (dmcChannelValue >= 2)
				{
					dmcChannelValue -= 2;
				}
			}
			
			dmcOutputUnit >>= 1;
			--dmcBitsRemainingCounter;
			dmcInternalCounter = 0;
			if (dmcBitsRemainingCounter == 0)
			{
				dmcBitsRemainingCounter = 8;
				if (!dmcSampleBufferLoaded)
				{
					dmcSilenceFlag = true;
				}
				else
				{
					dmcSampleBufferLoaded = false;
					dmcSilenceFlag = false;
					dmcOutputUnit = dmcSampleBufferValue;
				}
			}
		}
		
		if (dmcLoadCountdown != 0)
		{
			--dmcLoadCountdown;
			if (dmcLoadCountdown == 0)
			{
				dmcSampleBufferLoaded = true;
				dmcSampleBufferValue = cpu.getMem().read(dmcSampleAddress);
				--dmcSamplesRemaining;
				++dmcSampleAddress;
				if (dmcSampleAddress == 0x10000)
				{
					dmcSampleAddress = 0x8000;
				}
				
				if (dmcSamplesRemaining == 0)
				{
					if (dmcLoop)
					{
						dmcSamplesRemaining = dmcSampleLength;
						dmcSampleAddress = dmcStartSampleAddress;
					} else if (dmcIrqEnabled)
					{
						dmcInterruptFlag = true;
					}
				}
			}
		}
		
		if (Utils.getBit(audioEnableFlags, 4) && !dmcSampleBufferLoaded && dmcLoadCountdown == 0)
		{
			dmcLoadCountdown = 4;
		}
		
		if (dmcInterruptFlag)
		{
			cpu.setIrq();
		}
		
		if (cycle % 120 == 0)
		{
			doApuOutput();
		}
	}
	
	private void decrementLengthCounters()
	{
		if (Utils.getBit(audioEnableFlags, 0))
		{
			if (!pulse1CounterHold)
			{
				if (pulse1LengthCounter > 0)
				{
					--pulse1LengthCounter;
				}
			}
		}
		else
		{
			pulse1LengthCounter = 0;
		}
		
		if (Utils.getBit(audioEnableFlags, 1))
		{
			if (!pulse2CounterHold)
			{
				if (pulse2LengthCounter > 0)
				{
					--pulse2LengthCounter;
				}
			}
		}
		else
		{
			pulse2LengthCounter = 0;
		}
		
		if (Utils.getBit(audioEnableFlags, 2))
		{
			if (!triangleCounterHold)
			{
				if (triangleLengthCounter > 0)
				{
					--triangleLengthCounter;
				}
			}
		}
		else
		{
			triangleLengthCounter = 0;
		}
		
		if (Utils.getBit(audioEnableFlags, 3))
		{
			if (!noiseCounterHold)
			{
				if (noiseLengthCounter > 0)
				{
					--noiseLengthCounter;
				}
			}
		}
		else
		{
			noiseLengthCounter = 0;
		}
	}
	
	private void doApuOutput()
	{
		gui.writeAudioData(calculateSample());
	}
	
	private double calculateSample()
	{
		int pulse1 = getPulse1Value();
		int pulse2 = getPulse2Value();
		int triangle = getTriangleValue();
		int noise = getNoiseValue();
		int dmc = getDmcValue();
		
		double pulseOut = 0.0;
		double tndOut = 0.0;
		
		if (pulse1 != 0 || pulse2 != 0)
		{
			pulseOut = 95.88 / ((8128.0 / (pulse1 + pulse2)) + 100.0);
		}
		
		if (triangle != 0 || noise != 0 || dmc != 0)
		{
			double fraction = 1.0 / ((triangle / 8227.0) + (noise / 12241.0) + (dmc / 22638.0));
			tndOut = 159.79 / (fraction + 100.0);
		}
		
		return pulseOut + tndOut;
	}
	
	private int getPulse1Value()
	{
		return pulse1Value;
	}
	
	private int getPulse2Value()
	{
		return pulse2Value;
	}
	
	private int getTriangleValue()
	{	
		return triangleValue;
	}
	
	private int getNoiseValue()
	{
		int retval = 0;
		if (Utils.getBit(audioEnableFlags, 3) && noiseLengthCounter > 0)
		{
			retval = noiseInternal & 0x01;
			
			if (noiseConstantVol)
			{
				retval *= noiseVolumeDivider;
			}
			else
			{
				retval *= noiseDecayCounter;
			}
		}
		
		return retval;
	}
	
	private int getDmcValue()
	{
		int retval = 0;
		if (Utils.getBit(audioEnableFlags, 4))
		{
			retval = dmcChannelValue;
		}
		return retval;
	}
	
	private void updateLinearCounter()
	{
		if (linearCounterReloadFlag)
		{
			triangleLinearCounter = triangleLinearCounterReloadValue;
		}
		else
		{
			if (triangleLinearCounter > 0)
			{
				--triangleLinearCounter;
			}
		}
	}
	
	private void updateEnvelopes()
	{
		if (!pulse1StartFlag)
		{
			if (pulse1DividerCounter > 0)
			{
				--pulse1DividerCounter;
			}
			else
			{
				pulse1DividerCounter = pulse1VolumeDivider;
				if (pulse1DecayCounter > 0)
				{
					--pulse1DecayCounter;
				}
				else if (pulse1CounterHold)
				{
					pulse1DecayCounter = 15;
				}
			}
		}
		else
		{
			pulse1StartFlag = false;
			pulse1DecayCounter = 15;
			pulse1DividerCounter = pulse1VolumeDivider;
		}
		
		if (!pulse2StartFlag)
		{
			if (pulse2DividerCounter > 0)
			{
				--pulse2DividerCounter;
			}
			else
			{
				pulse2DividerCounter = pulse2VolumeDivider;
				if (pulse2DecayCounter > 0)
				{
					--pulse2DecayCounter;
				}
				else if (pulse2CounterHold)
				{
					pulse2DecayCounter = 15;
				}
			}
		}
		else
		{
			pulse2StartFlag = false;
			pulse2DecayCounter = 15;
			pulse2DividerCounter = pulse2VolumeDivider;
		}
		
		if (!noiseStartFlag)
		{
			if (noiseDividerCounter > 0)
			{
				--noiseDividerCounter;
			}
			else
			{
				noiseDividerCounter = noiseVolumeDivider;
				if (noiseDecayCounter > 0)
				{
					--noiseDecayCounter;
				}
				else if (noiseCounterHold)
				{
					noiseDecayCounter = 15;
				}
			}
		}
		else
		{
			noiseStartFlag = false;
			noiseDecayCounter = 15;
			noiseDividerCounter = noiseVolumeDivider;
		}
	}
	
	private void updateSweeps()
	{
		if (pulse1SweepCounter == 0)
		{
			if (pulse1SweepEnable && pulse1SweepShift != 0)
			{
				int pt = pulse1InternalTimer;
				pt = (pt >> 3);
				if (pulse1SweepNegate)
				{
					pt *= -1;
					--pt;
				}
				
				pt += pulse1InternalTimer;
				pt &= 0xffff;
				
				if (pt >= 8 && pt <= 0x7ff)
				{
					pulse1InternalTimer = pt;
				}
			}
			
			pulse1SweepCounter = pulse1SweepReload;
			pulse1SweepReloadFlag = false;
		} else if (pulse1SweepReloadFlag)
		{
			pulse1SweepCounter = pulse1SweepReload;
			pulse1SweepReloadFlag = false;
		} else
		{
			--pulse1SweepCounter;
		}
		
		if (pulse2SweepCounter == 0)
		{
			if (pulse2SweepEnable && pulse2SweepShift != 0)
			{
				int pt = pulse2InternalTimer;
				pt = (pt >> 3);
				if (pulse2SweepNegate)
				{
					pt *= -1;
				}
				
				pt += pulse2InternalTimer;
				pt &= 0xffff;
				
				if (pt >= 8 && pt <= 0x7ff)
				{
					pulse2InternalTimer = pt;
				}
			}
			
			pulse2SweepCounter = pulse2SweepReload;
			pulse2SweepReloadFlag = false;
		}
		else if (pulse2SweepReloadFlag)
		{
			pulse2SweepCounter = pulse2SweepReload;
			pulse2SweepReloadFlag = false;
		} else
		{
			--pulse2SweepCounter;
		}
	}
}
