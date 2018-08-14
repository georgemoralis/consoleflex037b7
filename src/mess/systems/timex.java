/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mess.systems;

import static sound.ay8910.*;
import static sound.ay8910H.*;
import static mess.machine.spectrum.*;
import static old.mame.inptportH.*;
import static old.mame.inptport.*;
import static old.mame.cpuintrf.*;
import static old.mame.common.*;
import old.mame.drawgfxH.*;
import static WIP.arcadeflex.fucPtr.*;
import WIP.mame.sndintrfH.MachineSound;

import static mame.commonH.REGION_CPU1;
import old.mame.drawgfxH.GfxDecodeInfo;
import old.mame.drawgfxH.rectangle;
import static old.mame.inputH.*;
import static old.mame.driverH.*;
import static mame.commonH.*;

import static mess.messH.*;
import static mess.machine.spectrum.*;
import static arcadeflex.libc.cstring.memset;

import WIP.arcadeflex.libc_v2.*;
import static old.arcadeflex.osdepend.logerror;
import static WIP.arcadeflex.libc.memcpy.*;
import static WIP.mame.memoryH.*;
import static WIP.mame.memory.*;

import static WIP.mame.sndintrfH.*;

import consoleflex.funcPtr.*;
import cpu.z80.z80H;

import static mess.includes.spectrumH.*;
import static mess.vidhrdw.spectrum.*;

import static mess.eventlst.*;
import static mess.eventlstH.*;
import static sound.speaker.*;
import sound.speakerH.Speaker_interface;
import static sound.wave.*;
import static sound.waveH.*;

import static mess.machine.nec765.*;
import static mess.includes.nec765H.*;
import static mess.includes.flopdrvH.*;
import static mess.machine.flopdrv.*;
import static mess.machine.dsk.*;
import static mess.mess.device_input;

import static mess.systems.spectrum.*;
import static mess.systems.spec128.*;

public class timex {
	public static int ts2068_port_ff_data = -1; /* Display enhancement control */
	public static int ts2068_port_f4_data = -1; /* Horizontal Select Register */
	public static UBytePtr ts2068_ram = null;
	
	public static InitMachinePtr ts2068_init_machine = new InitMachinePtr() { public void handler() 
	{
			//ts2068_ram = (UBytePtr )malloc(48*1024);
                        ts2068_ram = new UBytePtr(48*1024);
			//if(!ts2068_ram) return;
                        //memset(ts2068_ram, 0, 48*1024);
                        if(ts2068_ram==null) return;
			memset(ts2068_ram, 0, 48*1024);
	
			cpu_setbankhandler_r(1, MRA_BANK1);
			cpu_setbankhandler_r(2, MRA_BANK2);
			cpu_setbankhandler_r(3, MRA_BANK3);
			cpu_setbankhandler_r(4, MRA_BANK4);
			cpu_setbankhandler_r(5, MRA_BANK5);
			cpu_setbankhandler_r(6, MRA_BANK6);
			cpu_setbankhandler_r(7, MRA_BANK7);
			cpu_setbankhandler_r(8, MRA_BANK8);
	
			/* 0x0000-0x3fff always holds ROM */
			cpu_setbankhandler_w(9, MWA_ROM);
			cpu_setbankhandler_w(10, MWA_ROM);
			cpu_setbankhandler_w(11, MWA_BANK11);
			cpu_setbankhandler_w(12, MWA_BANK12);
			cpu_setbankhandler_w(13, MWA_BANK13);
			cpu_setbankhandler_w(14, MWA_BANK14);
			cpu_setbankhandler_w(15, MWA_BANK15);
			cpu_setbankhandler_w(16, MWA_BANK16);
	
			ts2068_port_ff_data = 0;
			ts2068_port_f4_data = 0;
			ts2068_update_memory();
	
			//spectrum_init_machine();
                        spectrum_init_machine.handler();
	} };
	
	public static InitMachinePtr tc2048_init_machine = new InitMachinePtr() { public void handler() 
	{
			ts2068_ram = new UBytePtr(48*1024);
			if(ts2068_ram==null) return;
                        memset(ts2068_ram, 0, 48*1024);
	
			cpu_setbankhandler_r(1, MRA_BANK1);
			cpu_setbankhandler_w(2, MWA_BANK2);
			cpu_setbank(1, ts2068_ram);
			cpu_setbank(2, ts2068_ram);
			ts2068_port_ff_data = 0;
	
			//spectrum_init_machine();
                        spectrum_init_machine.handler();
	} };
	
	public static StopMachinePtr ts2068_exit_machine = new StopMachinePtr() {
            
            public void handler() {
                if (ts2068_ram!=null){
                    //free(ts2068_ram);
                    ts2068_ram = null;
                    
                }
            }
        };
		
	static int ts2068_port_f4_r(int offset)
	{
			return ts2068_port_f4_data;
	}
	
	static void ts2068_port_f4_w(int offset, int data)
	{
			ts2068_port_f4_data = data;
			ts2068_update_memory();
	}
	
	static void ts2068_port_f5_w(int offset, int data)
	{
			AY8910_write_port_0_w.handler(0, data);
	}
	
	static int ts2068_port_f6_r(int offset)
	{
			/* TODO - Reading from register 14 reads the joystick ports
			   set bit 8 of address to read joystick #1
			   set bit 9 of address to read joystick #2
			   if both bits are set then OR values
			   Bit 0 up, 1 down, 2 left, 3 right, 7 fire active low. Other bits 1
			*/
			return AY8910_read_port_0_r.handler(0);                        
	}
	
	static void ts2068_port_f6_w(int offset, int data)
	{
			AY8910_control_port_0_w.handler(0, data);
	}
	
	static int ts2068_port_ff_r(int offset)
	{
			return ts2068_port_ff_data;            
	}
	
	static void ts2068_port_ff_w(int offset, int data)
	{
			/* Bits 0-2 Video Mode Select
			   Bits 3-5 64 column mode ink/paper selection
						(See ts2068_vh_screenrefresh for more info)
			   Bit	6	17ms Interrupt Inhibit
			   Bit	7	Cartridge (0) / EXROM (1) select
			*/
			ts2068_port_ff_data = data;
			ts2068_update_memory();
			logerror("Port %04x write %02x\n", offset, data);
	}
	
	static void tc2048_port_ff_w(int offset, int data)
	{
			ts2068_port_ff_data = data;
			logerror("Port %04x write %02x\n", offset, data);
	}
	
	
	/*******************************************************************
	 *
	 *		Bank switch between the 3 internal memory banks HOME, EXROM
	 *		and DOCK (Cartridges). The HOME bank contains 16K ROM in the
	 *		0-16K area and 48K RAM fills the rest. The EXROM contains 8K
	 *		ROM and can appear in every 8K segment (ie 0-8K, 8-16K etc).
	 *		The DOCK is empty and is meant to be occupied by cartridges
	 *		you can plug into the cartridge dock of the 2068.
	 *
	 *		The address space is divided into 8 8K chunks. Bit 0 of port
	 *		#f4 corresponds to the 0-8K chunk, bit 1 to the 8-16K chunk
	 *		etc. If the bit is 0 then the chunk is controlled by the HOME
	 *		bank. If the bit is 1 then the chunk is controlled by either
	 *		the DOCK or EXROM depending on bit 7 of port #ff. Note this
	 *		means that that the Z80 can't see chunks of the EXROM and DOCK
	 *		at the same time.
	 *
	 *******************************************************************/
	static void ts2068_update_memory()
	{
			//UBytePtr ChosenROM, *ExROM;
                        UBytePtr ChosenROM, ExROM;
	
			ExROM = new UBytePtr(memory_region(REGION_CPU1), 0x014000);
			if ((ts2068_port_f4_data & 0x01) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(1, ExROM);
							cpu_setbankhandler_r(1, MRA_BANK1);
							logerror("0000-1fff EXROM\n");
					}
					else
					{
							/* Cartridges not implemented so assume absent */
							cpu_setbankhandler_r(1, MRA_NOP);
							logerror("0000-1fff Cartridge\n");
					}
			}
			else
			{
					ChosenROM = new UBytePtr(memory_region(REGION_CPU1), 0x010000);
					cpu_setbank(1, ChosenROM);
					cpu_setbankhandler_r(1, MRA_BANK1);
					logerror("0000-1fff HOME\n");
			}
	
			if ((ts2068_port_f4_data & 0x02) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(2, ExROM);
							cpu_setbankhandler_r(2, MRA_BANK2);
							logerror("2000-3fff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(2, MRA_NOP);
							logerror("2000-3fff Cartridge\n");
					}
			}
			else
			{
					ChosenROM = new UBytePtr(memory_region(REGION_CPU1), 0x012000);
					cpu_setbank(2, ChosenROM);
					cpu_setbankhandler_r(2, MRA_BANK2);
					logerror("2000-3fff HOME\n");
			}
	
			if ((ts2068_port_f4_data & 0x04) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(3, ExROM);
							cpu_setbankhandler_r(3, MRA_BANK3);
							cpu_setbankhandler_w(11, MWA_ROM);
							logerror("4000-5fff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(3, MRA_NOP);
							cpu_setbankhandler_w(11, MWA_ROM);
							logerror("4000-5fff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(3, ts2068_ram);
					cpu_setbank(11, ts2068_ram);
					cpu_setbankhandler_r(3, MRA_BANK3);
					cpu_setbankhandler_w(11, MWA_BANK11);
					logerror("4000-5fff RAM\n");
			}
	
			if ((ts2068_port_f4_data & 0x08) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(4, ExROM);
							cpu_setbankhandler_r(4, MRA_BANK4);
							cpu_setbankhandler_w(12, MWA_ROM);
							logerror("6000-7fff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(4, MRA_NOP);
							cpu_setbankhandler_w(12, MWA_ROM);
							logerror("6000-7fff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(4, new UBytePtr(ts2068_ram, 0x2000));
					cpu_setbank(12, new UBytePtr(ts2068_ram, 0x2000));
					cpu_setbankhandler_r(4, MRA_BANK4);
					cpu_setbankhandler_w(12, MWA_BANK12);
					logerror("6000-7fff RAM\n");
			}
	
			if ((ts2068_port_f4_data & 0x10) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(5, ExROM);
							cpu_setbankhandler_r(5, MRA_BANK5);
							cpu_setbankhandler_w(13, MWA_ROM);
							logerror("8000-9fff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(5, MRA_NOP);
							cpu_setbankhandler_w(13, MWA_ROM);
							logerror("8000-9fff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(5, new UBytePtr(ts2068_ram, 0x4000));
					cpu_setbank(13, new UBytePtr(ts2068_ram, 0x4000));
					cpu_setbankhandler_r(5, MRA_BANK5);
					cpu_setbankhandler_w(13, MWA_BANK13);
					logerror("8000-9fff RAM\n");
			}
	
			if ((ts2068_port_f4_data & 0x20) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(6, ExROM);
							cpu_setbankhandler_r(6, MRA_BANK6);
							cpu_setbankhandler_w(14, MWA_ROM);
							logerror("a000-bfff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(6, MRA_NOP);
							cpu_setbankhandler_w(14, MWA_ROM);
							logerror("a000-bfff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(6, new UBytePtr(ts2068_ram, 0x6000));
					cpu_setbank(14, new UBytePtr(ts2068_ram, 0x6000));
					cpu_setbankhandler_r(6, MRA_BANK6);
					cpu_setbankhandler_w(14, MWA_BANK14);
					logerror("a000-bfff RAM\n");
			}
	
			if ((ts2068_port_f4_data & 0x40) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(7, ExROM);
							cpu_setbankhandler_r(7, MRA_BANK7);
							cpu_setbankhandler_w(15, MWA_ROM);
							logerror("c000-dfff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(7, MRA_NOP);
							cpu_setbankhandler_w(15, MWA_ROM);
							logerror("c000-dfff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(7, new UBytePtr(ts2068_ram, 0x8000));
					cpu_setbank(15, new UBytePtr(ts2068_ram, 0x8000));
					cpu_setbankhandler_r(7, MRA_BANK7);
					cpu_setbankhandler_w(15, MWA_BANK15);
					logerror("c000-dfff RAM\n");
			}
	
			if ((ts2068_port_f4_data & 0x80) != 0)
			{
					if ((ts2068_port_ff_data & 0x80) != 0)
					{
							cpu_setbank(8, ExROM);
							cpu_setbankhandler_r(8, MRA_BANK8);
							cpu_setbankhandler_w(16, MWA_ROM);
							logerror("e000-ffff EXROM\n");
					}
					else
					{
							cpu_setbankhandler_r(8, MRA_NOP);
							cpu_setbankhandler_w(16, MWA_ROM);
							logerror("e000-ffff Cartridge\n");
					}
			}
			else
			{
					cpu_setbank(8, new UBytePtr(ts2068_ram, 0xa000));
					cpu_setbank(16, new UBytePtr(ts2068_ram, 0xa000));
					cpu_setbankhandler_r(8, MRA_BANK8);
					cpu_setbankhandler_w(16, MWA_BANK16);
					logerror("e000-ffff RAM\n");
			}
	}
	
	static MemoryReadAddress ts2068_readmem[] ={
			new MemoryReadAddress( 0x0000, 0x1fff, MRA_BANK1 ),
			new MemoryReadAddress( 0x2000, 0x3fff, MRA_BANK2 ),
			new MemoryReadAddress( 0x4000, 0x5fff, MRA_BANK3 ),
			new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK4 ),
			new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK5 ),
			new MemoryReadAddress( 0xa000, 0xbfff, MRA_BANK6 ),
			new MemoryReadAddress( 0xc000, 0xdfff, MRA_BANK7 ),
			new MemoryReadAddress( 0xe000, 0xffff, MRA_BANK8 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress ts2068_writemem[] ={
			new MemoryWriteAddress( 0x0000, 0x1fff, MWA_BANK9 ),
			new MemoryWriteAddress( 0x2000, 0x3fff, MWA_BANK10 ),
			new MemoryWriteAddress( 0x4000, 0x5fff, MWA_BANK11 ),
			new MemoryWriteAddress( 0x6000, 0x7fff, MWA_BANK12 ),
			new MemoryWriteAddress( 0x8000, 0x9fff, MWA_BANK13 ),
			new MemoryWriteAddress( 0xa000, 0xbfff, MWA_BANK14 ),
			new MemoryWriteAddress( 0xc000, 0xdfff, MWA_BANK15 ),
			new MemoryWriteAddress( 0xe000, 0xffff, MWA_BANK16 ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	static MemoryReadAddress tc2048_readmem[] ={
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
			new MemoryReadAddress( 0x4000, 0xffff, MRA_BANK1 ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress tc2048_writemem[] ={
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
			new MemoryWriteAddress( 0x4000, 0xffff, MWA_BANK2 ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
        
	//READ_HANDLER ( ts2068_port_r )
	public static ReadHandlerPtr ts2068_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
			switch (offset & 0xff)
			{
					/* Note: keys only decoded on port #fe not all even ports so
					   ports #f4 & #f6 correctly read */
					case 0xf4: return ts2068_port_f4_r(offset);
					case 0xf6: return ts2068_port_f6_r(offset);
					case 0xff: return ts2068_port_ff_r(offset);
	
					case 0xfe: return spectrum_port_fe_r(offset);
					case 0x1f: return spectrum_port_1f_r(offset);
					case 0x7f: return spectrum_port_7f_r(offset);
					case 0xdf: return spectrum_port_df_r(offset);
			}
			logerror("Read from port: %04x\n", offset);
	
			return 0xff;
	}};
	
	//WRITE_HANDLER ( ts2068_port_w )
        public static WriteHandlerPtr ts2068_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	/* Ports #fd & #fc were reserved by Timex for bankswitching and are not used
	   by either the hardware or system software.
	   Port #fb is the Thermal printer port and works exactly as the Sinclair
	   Printer - ie not yet emulated.
	*/
			switch (offset & 0xff)
			{
					case 0xfe: spectrum_port_fe_w(offset,data); break;
					case 0xf4: ts2068_port_f4_w(offset,data); break;
					case 0xf5: ts2068_port_f5_w(offset,data); break;
					case 0xf6: ts2068_port_f6_w(offset,data); break;
					case 0xff: ts2068_port_ff_w(offset,data); break;
					default:
							logerror("Write %02x to Port: %04x\n", data, offset);
			}
	}};
	
	//READ_HANDLER ( tc2048_port_r )
        public static ReadHandlerPtr tc2048_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
	
			if ((offset & 1)==0)
					return spectrum_port_fe_r(offset);
			switch (offset & 0xff)
			{
					case 0xff: return ts2068_port_ff_r(offset);
					case 0x1f: return spectrum_port_1f_r(offset);
					case 0x7f: return spectrum_port_7f_r(offset);
					case 0xdf: return spectrum_port_df_r(offset);
			}
	
			logerror("Read from port: %04x\n", offset);
			return 0xff;
	}};
	
	//WRITE_HANDLER ( tc2048_port_w )
        public static WriteHandlerPtr tc2048_port_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
			if ((offset & 1)==0)
					spectrum_port_fe_w(offset,data);
			else if ((offset & 0xff)==0xff)
					tc2048_port_ff_w(offset,data);
			else
			{
					logerror("Write %02x to Port: %04x\n", data, offset);
			}
	}};
	
	static IOReadPort ts2068_readport[] ={
			new IOReadPort(0x0000, 0x0ffff, ts2068_port_r),
		new IOReadPort( -1 )
	};
	
	static IOWritePort ts2068_writeport[] ={
			new IOWritePort(0x0000, 0x0ffff, ts2068_port_w),
		new IOWritePort( -1 )
	};
	
	
	static IOReadPort tc2048_readport[] ={
			new IOReadPort(0x0000, 0x0ffff, tc2048_port_r),
		new IOReadPort( -1 )
	};
	
	static IOWritePort tc2048_writeport[] ={
			new IOWritePort(0x0000, 0x0ffff, tc2048_port_w),
		new IOWritePort( -1 )
	};
        
	static MachineDriver machine_driver_ts2068 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80|CPU_16BIT_PORT,
							3580000,		/* 3.58 Mhz */
							ts2068_readmem,ts2068_writemem,
							ts2068_readport,ts2068_writeport,
							spec_interrupt,1
			),
		},
			60, 2500,		/* frames per second, vblank duration */
		1,
			ts2068_init_machine,
			ts2068_exit_machine,
	
		/* video hardware */
			TS2068_SCREEN_WIDTH,			/* screen width */
			TS2068_SCREEN_HEIGHT,			/* screen height */
			new rectangle( 0, TS2068_SCREEN_WIDTH-1, 0, TS2068_SCREEN_HEIGHT-1),  /* visible_area */
		spectrum_gfxdecodeinfo, 			 /* graphics decode info */
		16, 256,							 /* colors used for the characters */
		spectrum_init_palette,				 /* initialise palette */
	
		VIDEO_TYPE_RASTER,
			ts2068_eof_callback,
			spectrum_128_vh_start,
			spectrum_128_vh_stop,
			ts2068_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
                new MachineSound[] {
                                /* +3 Ay-3-8912 sound */
                                new MachineSound(
                                                SOUND_AY8910,
                                                spectrum_128_ay_interface
                                ),
                                /* standard spectrum sound */
                                new MachineSound(
                                                SOUND_SPEAKER,
                                                spectrum_speaker_interface
                                ),
                                /*-----------------27/02/00 10:40-------------------
                                 cassette wave interface
                                --------------------------------------------------*/
                                new MachineSound(
                                                SOUND_WAVE,
                                                spectrum_wave_interface
                                )
                }
	);
	
	static MachineDriver machine_driver_tc2048 = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80|CPU_16BIT_PORT,
				3500000,		/* 3.5 Mhz */
							tc2048_readmem,tc2048_writemem,
							tc2048_readport,tc2048_writeport,
							spec_interrupt,1
			),
		},
		50, 2500,		/* frames per second, vblank duration */
		1,
			tc2048_init_machine,
			ts2068_exit_machine,
	
		/* video hardware */
			TS2068_SCREEN_WIDTH,			/* screen width */
			SPEC_SCREEN_HEIGHT, 			/* screen height */
			new rectangle( 0, TS2068_SCREEN_WIDTH-1, 0, SPEC_SCREEN_HEIGHT-1),  /* visible_area */
		spectrum_gfxdecodeinfo, 			 /* graphics decode info */
		16, 256,							 /* colors used for the characters */
		spectrum_init_palette,				 /* initialise palette */
	
		VIDEO_TYPE_RASTER,
			spectrum_eof_callback,
			spectrum_128_vh_start,
			spectrum_128_vh_stop,
			tc2048_vh_screenrefresh,
	
		/* sound hardware */
		0,0,0,0,
                new MachineSound[] {
                                /* standard spectrum sound */
                                new MachineSound(
                                                SOUND_SPEAKER,
                                                spectrum_speaker_interface
                               ),
                                /*-----------------27/02/00 10:40-------------------
                                cassette wave interface
                                --------------------------------------------------*/
                                new MachineSound(
                                               SOUND_WAVE,
                                               spectrum_wave_interface
                                )
                }
	);
        
	static RomLoadPtr rom_tc2048 = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x10000,REGION_CPU1);
			ROM_LOAD("tc2048.rom",0x0000,0x4000, 0xf1b5fa67);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ts2068 = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x16000,REGION_CPU1);
			ROM_LOAD("ts2068_h.rom",0x10000,0x4000, 0xbf44ec3f);
			ROM_LOAD("ts2068_x.rom",0x14000,0x2000, 0xae16233a);
	ROM_END(); }}; 
	
        //COMP ( 198?, tc2048,   spectrum, tc2048,		 spectrum, 0,			 "Timex of Portugal",    "TC2048" )
	public static GameDriver driver_tc2048 = new GameDriver("1983", "tc2048", "spectrum.java", rom_tc2048, null, machine_driver_tc2048, input_ports_spectrum, null, io_spectrum, "Timex of Portugal", "TC2048");
        //COMP ( 1983, ts2068,   spectrum, ts2068,		 spectrum, 0,			 "Timex Sinclair",       "TS2068" )
	public static GameDriver driver_ts2068 = new GameDriver("1983", "ts2068", "spectrum.java", rom_ts2068, null, machine_driver_ts2068, input_ports_spectrum, null, io_spectrum, "Timex Sinclair", "TS2068");
    
}
