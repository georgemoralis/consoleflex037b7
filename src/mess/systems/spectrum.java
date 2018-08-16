/***************************************************************************
		SWRITE_HANDLERpectrum/Inves/TK90X etc. memory map:

	CPU:
		0000-3fff ROM
		4000-ffff RAM

		Spectrum 128/+2/+2a/+3 memory map:

		CPU:
				0000-3fff Banked ROM/RAM (banked rom only on 128/+2)
				4000-7fff Banked RAM
				8000-bfff Banked RAM
				c000-ffff Banked RAM

		TS2068 memory map: (Can't have both EXROM and DOCK active)
		The 8K EXROM can be loaded into multiple pages.

	CPU:
				0000-1fff	  ROM / EXROM / DOCK (Cartridge)
				2000-3fff	  ROM / EXROM / DOCK
				4000-5fff \
				6000-7fff  \
				8000-9fff  |- RAM / EXROM / DOCK
				a000-bfff  |
				c000-dfff  /
				e000-ffff /


Interrupts:

Changes:

29/1/2000		KT - Implemented initial +3 emulation
30/1/2000		KT - Improved input port decoding for reading
					 and therefore correct keyboard handling for Spectrum and +3
31/1/2000		KT - Implemented buzzer sound for Spectrum and +3.
					 Implementation copied from Paul Daniel's Jupiter driver.
					 Fixed screen display problems with dirty chars.
					 Added support to load .Z80 snapshots. 48k support so far.
13/2/2000		KT - Added Interface II, Kempston, Fuller and Mikrogen joystick support
17/2/2000		DJR - Added full key descriptions and Spectrum+ keys.
				Fixed Spectrum +3 keyboard problems.
17/2/2000		KT - Added tape loading from WAV/Changed from DAC to generic speaker code
18/2/2000		KT - Added tape saving to WAV
27/2/2000		KT - Took DJR's changes and added my changes.
27/2/2000		KT - Added disk image support to Spectrum +3 driver.
27/2/2000		KT - Added joystick I/O code to the Spectrum +3 I/O handler.
14/3/2000		DJR - Tape handling dipswitch.
26/3/2000		DJR - Snapshot files are now classifed as snapshots not cartridges.
04/4/2000		DJR - Spectrum 128 / +2 Support.
13/4/2000		DJR - +4 Support (unofficial 48K hack).
13/4/2000		DJR - +2a Support (rom also used in +3 models).
13/4/2000		DJR - TK90X, TK95 and Inves support (48K clones).
21/4/2000		DJR - TS2068 and TC2048 support (TC2048 Supports extra video
				modes but doesn't have bank switching or sound chip).
09/5/2000		DJR - Spectrum +2 (France, Spain), +3 (Spain).
17/5/2000		DJR - Dipswitch to enable/disable disk drives on +3 and clones.
27/6/2000		DJR - Changed 128K/+3 port decoding (sound now works in Zub 128K).
06/8/2000		DJR - Fixed +3 Floppy support

Notes:

128K emulation is not perfect - the 128K machines crash and hang while
running quite a lot of games.
The TK90X and TK95 roms output 0 to port #df on start up.
The purpose of this port is unknown (probably display mode as TS2068) and
thus is not emulated.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
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

/*TODO*/////import static mess.machine.wd17xx.h.*;

public class spectrum
{
	
	/* +3 hardware */
	
	/*-----------------27/02/00 10:42-------------------
	 bit 7-5: not used
	 bit 4: Ear output/Speaker
	 bit 3: MIC/Tape Output
	 bit 2-0: border colour
	--------------------------------------------------*/
	
	static int PreviousFE = 0;
        
        public static int quickload = 0;
	
	public static void spectrum_port_fe_w (int offset,int data)
	{
		int Changed;
	
		Changed = PreviousFE^data;
	
		/* border colour changed? */
		if ((Changed & 0x07)!=0)
		{
			/* yes - send event */
			EventList_AddItemOffset(0x0fe, data & 0x07, cpu_getcurrentcycles());
		}
	
		if ((Changed & (1<<4))!=0)
		{
			/* DAC output state */
			speaker_level_w(0,(data>>4) & 0x01);
		}
	
		if ((Changed & (1<<3))!=0)
		{
			// Sounds while saving
                        speaker_level_w(0,(data>>3) & 0x01);
                        /*-----------------27/02/00 10:41-------------------
			 write cassette data
			--------------------------------------------------*/
			/*TODO*/////device_output(IO_CASSETTE, 0, (data & (1<<3)) ? -32768: 32767);
		}
	
		PreviousFE = data;
	}
	
	
	
	//extern extern extern 
	
	/* Initialisation values used when determining which model is being emulated.
	   48K	   Spectrum doesn't use either port.
	   128K/+2 Bank switches with port 7ffd only.
	   +3/+2a  Bank switches with both ports. */
        
	static MemoryReadAddress spectrum_readmem[] ={
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0x4000, 0x57ff, spectrum_characterram_r ),
		new MemoryReadAddress( 0x5800, 0x5aff, spectrum_colorram_r ),
		new MemoryReadAddress( 0x5b00, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};
	
	static MemoryWriteAddress spectrum_writemem[] ={
		new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( 0x4000, 0x57ff, spectrum_characterram_w ),
		new MemoryWriteAddress( 0x5800, 0x5aff, spectrum_colorram_w ),
		new MemoryWriteAddress( 0x5b00, 0xffff, MWA_RAM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
	
	/* KT: more accurate keyboard reading */
	/* DJR: Spectrum+ keys added */
	static int spectrum_port_fe_r(int offset)
	{
	   int lines = offset>>8;
	   int data = 0xff;
	
	   int cs_extra1 = readinputport(8)  & 0x1f;
	   int cs_extra2 = readinputport(9)  & 0x1f;
	   int cs_extra3 = readinputport(10) & 0x1f;
	   int ss_extra1 = readinputport(11) & 0x1f;
	   int ss_extra2 = readinputport(12) & 0x1f;
	
	   /* Caps - V */
	   if ((lines & 1)==0)
	   {
			data &= readinputport(0);
			/* CAPS for extra keys */
			if (cs_extra1 != 0x1f || cs_extra2 != 0x1f || cs_extra3 != 0x1f)
				data &= ~0x01;
	   }
	
	   /* A - G */
	   if ((lines & 2)==0)
			data &= readinputport(1);
	
	   /* Q - T */
	   if ((lines & 4)==0)
			data &= readinputport(2);
	
	   /* 1 - 5 */
	   if ((lines & 8)==0)
			data &= readinputport(3) & cs_extra1;
	
	   /* 6 - 0 */
	   if ((lines & 16)==0)
			data &= readinputport(4) & cs_extra2;
	
	   /* Y - P */
	   if ((lines & 32)==0)
			data &= readinputport(5) & ss_extra1;
	
	   /* H - Enter */
	   if ((lines & 64)==0)
			data &= readinputport(6);
	
	   /* B - Space */
	   if ((lines & 128)==0)
	   {
			data &= readinputport(7) & cs_extra3 & ss_extra2;
			/* SYMBOL SHIFT for extra keys */
			if (ss_extra1 != 0x1f || ss_extra2 != 0x1f)
				data &= ~0x02;
	   }
	
	   data |= (0xe0); /* Set bits 5-7 - as reset above */
	
		 /*-----------------27/02/00 10:46-------------------
			cassette input from wav
		 --------------------------------------------------*/
		/*TODO*///// cassette emulation
                if (device_input(IO_CASSETTE, 0)>255 )
		{
                    data &= ~0x40;
		}
	
	   /* Issue 2 Spectrums default to having bits 5, 6 & 7 set.
		  Issue 3 Spectrums default to having bits 5 & 7 set and bit 6 reset. */
	   if ((readinputport(16) & 0x80) != 0)
			data ^= (0x40);
	
	   return data;
	}
	
	/* kempston joystick interface */
	static int spectrum_port_1f_r(int offset)
	{
	  return readinputport(13) & 0x1f;
	}
	
	/* fuller joystick interface */
	static int spectrum_port_7f_r(int offset)
	{
	  return readinputport(14) | (0xff^0x8f);
	}
	
	/* mikrogen joystick interface */
	static int spectrum_port_df_r(int offset)
	{
	  return readinputport(15) | (0xff^0x1f);
	}
	
	
        //READ_HANDLER ( spectrum_port_r )
        public static ReadHandlerPtr spectrum_port_r = new ReadHandlerPtr() {
            public int handler(int offset) {
            {
                
                            
                            if ((offset & 1)==0)
                                    return spectrum_port_fe_r(offset);

                            if ((offset & 0xff)==0x1f)
                                    return spectrum_port_1f_r(offset);

                            if ((offset & 0xff)==0x7f)
                                    return spectrum_port_7f_r(offset);

                            if ((offset & 0xff)==0xdf)
                                    return spectrum_port_df_r(offset);

                            logerror("Read from port: %04x\n", offset);

                            return 0xff;
                }
            }};
	
	//WRITE_HANDLER ( spectrum_port_w )
        public static WriteHandlerPtr spectrum_port_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
	    if ((offset & 1)==0)
				spectrum_port_fe_w(offset,data);
			else
			{
				logerror("Write %02x to Port: %04x\n", data, offset);
			}
	}};
	
	
	/* KT: Changed it to this because the ports are not decoded fully.
	The function decodes the ports appropriately */
	static IOReadPort spectrum_readport[] ={
		new IOReadPort(0x0000, 0xffff, spectrum_port_r),
		new IOReadPort( -1 )
	};
	
	/* KT: Changed it to this because the ports are not decoded fully.
	The function decodes the ports appropriately */
	static IOWritePort spectrum_writeport[] ={
			new IOWritePort(0x0000, 0xffff, spectrum_port_w),
		new IOWritePort( -1 )
	};
	
	static GfxLayout spectrum_charlayout = new GfxLayout(
		8,8,
		256,
		1,						/* 1 bits per pixel */
	
		new int[] { 0 },					/* no bitplanes; 1 bit per pixel */
	
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0, 8*256, 16*256, 24*256, 32*256, 40*256, 48*256, 56*256 },
	
		8				/* every char takes 1 consecutive byte */
	);
	
	static GfxDecodeInfo spectrum_gfxdecodeinfo[] ={
		new GfxDecodeInfo( 0, 0x0, spectrum_charlayout, 0, 0x80 ),
		new GfxDecodeInfo( 0, 0x0, spectrum_charlayout, 0, 0x80 ),
		new GfxDecodeInfo( 0, 0x0, spectrum_charlayout, 0, 0x80 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static InputPortPtr input_ports_spectrum = new InputPortPtr(){ public void handler() { 
		PORT_START();  /* 0xFEFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "CAPS SHIFT",                       KEYCODE_LSHIFT,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "Z  COPY    :      LN       BEEP",  KEYCODE_Z,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "X  CLEAR   Pound  EXP      INK",   KEYCODE_X,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "C  CONT    ?      LPRINT   PAPER", KEYCODE_C,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "V  CLS     /      LLIST    FLASH", KEYCODE_V,  IP_JOY_NONE );
	
		PORT_START();  /* 0xFDFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "A  NEW     STOP   READ     ~",  KEYCODE_A,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "S  SAVE    NOT    RESTORE  |",  KEYCODE_S,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "D  DIM     STEP   DATA     \\", KEYCODE_D,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "F  FOR     TO     SGN      {",  KEYCODE_F,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "G  GOTO    THEN   ABS      }",  KEYCODE_G,  IP_JOY_NONE );
	
		PORT_START();  /* 0xFBFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "Q  PLOT    <=     SIN      ASN",    KEYCODE_Q,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "W  DRAW    <>     COS      ACS",    KEYCODE_W,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "E  REM     >=     TAN      ATN",    KEYCODE_E,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "R  RUN     <      INT      VERIFY", KEYCODE_R,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "T  RAND    >      RND      MERGE",  KEYCODE_T,  IP_JOY_NONE );
	
			/* interface II uses this port for joystick */
		PORT_START();  /* 0xF7FE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "1          !      BLUE     DEF FN", KEYCODE_1,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "2          @      RED      FN",     KEYCODE_2,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "3          #      MAGENTA  LINE",   KEYCODE_3,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "4          $      GREEN    OPEN#",  KEYCODE_4,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "5          %      CYAN     CLOSE#", KEYCODE_5,  IP_JOY_NONE );
	
			/* protek clashes with interface II! uses 5 = left, 6 = down, 7 = up, 8 = right, 0 = fire */
		PORT_START();  /* 0xEFFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0          _      BLACK    FORMAT", KEYCODE_0,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9          );              POINT",  KEYCODE_9,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "8          (               CAT",    KEYCODE_8,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "7          '      WHITE    ERASE",  KEYCODE_7,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "6          &      YELLOW   MOVE",   KEYCODE_6,  IP_JOY_NONE );
	
		PORT_START();  /* 0xDFFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "P  PRINT   \"      TAB      (c);", KEYCODE_P,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "O  POKE    ;      PEEK     OUT", KEYCODE_O,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "I  INPUT   AT     CODE     IN",  KEYCODE_I,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "U  IF      OR     CHR$     ]",   KEYCODE_U,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Y  RETURN  AND    STR$     [",   KEYCODE_Y,  IP_JOY_NONE );
	
		PORT_START();  /* 0xBFFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER",                              KEYCODE_ENTER,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "L  LET     =      USR      ATTR",    KEYCODE_L,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "K  LIST    +      LEN      SCREEN$", KEYCODE_K,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "J  LOAD    -      VAL      VAL$",    KEYCODE_J,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "H  GOSUB   ^      SQR      CIRCLE",  KEYCODE_H,  IP_JOY_NONE );
	
		PORT_START();  /* 0x7FFE */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE",                              KEYCODE_SPACE,   IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "SYMBOL SHIFT",                       KEYCODE_RSHIFT,  IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "M  PAUSE   .      PI       INVERSE", KEYCODE_M,  IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "N  NEXT    ,      INKEY$   OVER",    KEYCODE_N,  IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "B  BORDER  *      BIN      BRIGHT",  KEYCODE_B,  IP_JOY_NONE );
	
			PORT_START();  /* Spectrum+ Keys (set CAPS + 1-5) */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "EDIT          (CAPS + 1);",  KEYCODE_F1,         IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "CAPS LOCK     (CAPS + 2);",  KEYCODE_CAPSLOCK,   IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "TRUE VID      (CAPS + 3);",  KEYCODE_F2,         IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "INV VID       (CAPS + 4);",  KEYCODE_F3,         IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Cursor left   (CAPS + 5);",  KEYCODE_LEFT,       IP_JOY_NONE );
			PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
	
			PORT_START();  /* Spectrum+ Keys (set CAPS + 6-0) */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "DEL           (CAPS + 0);",  KEYCODE_BACKSPACE,  IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "GRAPH         (CAPS + 9);",  KEYCODE_LALT,       IP_JOY_NONE );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "Cursor right  (CAPS + 8);",  KEYCODE_RIGHT,      IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "Cursor up     (CAPS + 7);",  KEYCODE_UP,         IP_JOY_NONE );
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Cursor down   (CAPS + 6);",  KEYCODE_DOWN,       IP_JOY_NONE );
			PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
	
			PORT_START();  /* Spectrum+ Keys (set CAPS + SPACE and CAPS + SYMBOL */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "BREAK",                     KEYCODE_PAUSE,      IP_JOY_NONE );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "EXT MODE",                  KEYCODE_LCONTROL,   IP_JOY_NONE );
			PORT_BIT(0xfc, IP_ACTIVE_LOW, IPT_UNUSED);
	
			PORT_START();  /* Spectrum+ Keys (set SYMBOL SHIFT + O/P */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "\"", KEYCODE_F4,  IP_JOY_NONE );
	/*		  PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "\"", KEYCODE_QUOTE,  IP_JOY_NONE );*/
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, ";", KEYCODE_COLON,  IP_JOY_NONE );
			PORT_BIT(0xfc, IP_ACTIVE_LOW, IPT_UNUSED);
	
			PORT_START();  /* Spectrum+ Keys (set SYMBOL SHIFT + N/M */
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, ".", KEYCODE_STOP,   IP_JOY_NONE );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, ",", KEYCODE_COMMA,  IP_JOY_NONE );
			PORT_BIT(0xf3, IP_ACTIVE_LOW, IPT_UNUSED);
	
			PORT_START();  /* Kempston joystick interface */
			PORT_BITX(0x01, IP_ACTIVE_HIGH, IPT_KEYBOARD, "KEMPSTON JOYSTICK RIGHT",     IP_KEY_NONE,    JOYCODE_1_RIGHT );
			PORT_BITX(0x02, IP_ACTIVE_HIGH, IPT_KEYBOARD, "KEMPSTON JOYSTICK LEFT",      IP_KEY_NONE,   JOYCODE_1_LEFT );
			PORT_BITX(0x04, IP_ACTIVE_HIGH, IPT_KEYBOARD, "KEMPSTON JOYSTICK DOWN",         IP_KEY_NONE,        JOYCODE_1_DOWN );
			PORT_BITX(0x08, IP_ACTIVE_HIGH, IPT_KEYBOARD, "KEMPSTON JOYSTICK UP",         IP_KEY_NONE,        JOYCODE_1_UP);
			PORT_BITX(0x10, IP_ACTIVE_HIGH, IPT_KEYBOARD, "KEMPSTON JOYSTICK FIRE",         IP_KEY_NONE,        JOYCODE_1_BUTTON1 );
	
			PORT_START();  /* Fuller joystick interface */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "FULLER JOYSTICK UP",     IP_KEY_NONE,    JOYCODE_1_UP );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "FULLER JOYSTICK DOWN",      IP_KEY_NONE,   JOYCODE_1_DOWN );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "FULLER JOYSTICK LEFT",         IP_KEY_NONE,        JOYCODE_1_LEFT );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "FULLER JOYSTICK RIGHT",         IP_KEY_NONE,        JOYCODE_1_RIGHT);
			PORT_BITX(0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "FULLER JOYSTICK FIRE",         IP_KEY_NONE,        JOYCODE_1_BUTTON1);
	
			PORT_START();  /* Mikrogen joystick interface */
			PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "MIKROGEN JOYSTICK UP",     IP_KEY_NONE,    JOYCODE_1_UP );
			PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "MIKROGEN JOYSTICK DOWN",      IP_KEY_NONE,   JOYCODE_1_DOWN );
			PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "MIKROGEN JOYSTICK RIGHT",         IP_KEY_NONE,        JOYCODE_1_RIGHT );
			PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "MIKROGEN JOYSTICK LEFT",         IP_KEY_NONE,        JOYCODE_1_LEFT);
			PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "MIKROGEN JOYSTICK FIRE",         IP_KEY_NONE,        JOYCODE_1_BUTTON1);
	
	
			PORT_START(); 
			PORT_BITX(0x8000, IP_ACTIVE_HIGH, IPT_KEYBOARD, "Quickload", KEYCODE_F8, IP_JOY_NONE);
			PORT_DIPNAME(0x80, 0x00, "Hardware Version");
			PORT_DIPSETTING(0x00, "Issue 2" );
			PORT_DIPSETTING(0x80, "Issue 3" );
			PORT_DIPNAME(0x40, 0x00, "End of .TAP action");
			PORT_DIPSETTING(0x00, "Disable .TAP support" );
			PORT_DIPSETTING(0x40, "Rewind tape to start (to reload earlier levels);" );
			PORT_DIPNAME(0x20, 0x00, "+3/+2a etc. Disk Drive");
			PORT_DIPSETTING(0x00, "Enabled" );
			PORT_DIPSETTING(0x20, "Disabled" );
			PORT_BIT(0x1f, IP_ACTIVE_LOW, IPT_UNUSED);
	
	INPUT_PORTS_END(); }}; 
	
	static char[] spectrum_palette =
        {
		0x00, 0x00, 0x00, 0x00, 0x00, 0xbf,
		0xbf, 0x00, 0x00, 0xbf, 0x00, 0xbf,
		0x00, 0xbf, 0x00, 0x00, 0xbf, 0xbf,
		0xbf, 0xbf, 0x00, 0xbf, 0xbf, 0xbf,
	
		0x00, 0x00, 0x00, 0x00, 0x00, 0xff,
		0xff, 0x00, 0x00, 0xff, 0x00, 0xff,
		0x00, 0xff, 0x00, 0x00, 0xff, 0xff,
		0xff, 0xff, 0x00, 0xff, 0xff, 0xff,
        };
	
	//static unsigned short spectrum_colortable[128*2] = {
        static char[] spectrum_colortable = 
        {
		0,0, 0,1, 0,2, 0,3, 0,4, 0,5, 0,6, 0,7,
		1,0, 1,1, 1,2, 1,3, 1,4, 1,5, 1,6, 1,7,
		2,0, 2,1, 2,2, 2,3, 2,4, 2,5, 2,6, 2,7,
		3,0, 3,1, 3,2, 3,3, 3,4, 3,5, 3,6, 3,7,
		4,0, 4,1, 4,2, 4,3, 4,4, 4,5, 4,6, 4,7,
		5,0, 5,1, 5,2, 5,3, 5,4, 5,5, 5,6, 5,7,
		6,0, 6,1, 6,2, 6,3, 6,4, 6,5, 6,6, 6,7,
		7,0, 7,1, 7,2, 7,3, 7,4, 7,5, 7,6, 7,7,
	
		 8,8,  8,9,  8,10,	8,11,  8,12,  8,13,  8,14,	8,15,
		 9,8,  9,9,  9,10,	9,11,  9,12,  9,13,  9,14,	9,15,
		10,8, 10,9, 10,10, 10,11, 10,12, 10,13, 10,14, 10,15,
		11,8, 11,9, 11,10, 11,11, 11,12, 11,13, 11,14, 11,15,
		12,8, 12,9, 12,10, 12,11, 12,12, 12,13, 12,14, 12,15,
		13,8, 13,9, 13,10, 13,11, 13,12, 13,13, 13,14, 13,15,
		14,8, 14,9, 14,10, 14,11, 14,12, 14,13, 14,14, 14,15,
		15,8, 15,9, 15,10, 15,11, 15,12, 15,13, 15,14, 15,15
	};
	
        /* Initialise the palette */
	//static void spectrum_init_palette(UBytePtr sys_palette, unsigned short *sys_colortable,const UBytePtr color_prom)
        public static VhConvertColorPromPtr spectrum_init_palette = new VhConvertColorPromPtr() {
            public void handler(char[] sys_palette, char[] sys_colortable, UBytePtr color_prom) {
                //memcpy(sys_palette,spectrum_palette,sizeof(spectrum_palette));
                memcpy(sys_palette,spectrum_palette,spectrum_palette.length);
                //memcpy(sys_colortable,spectrum_colortable,sizeof(spectrum_colortable));
                memcpy(sys_colortable,spectrum_colortable,spectrum_colortable.length);
            }
        };
        
	
	public static InterruptPtr spec_interrupt = new InterruptPtr() { public int handler() 
	{
                	
			//if (!quickload && (readinputport(16) & 0x8000))
                        if ( (quickload!=1) && ((readinputport(16) & 0x8000)!=0))
			{
					spec_quick_open.handler(0, 0, null);
                                        
					quickload = 1;
                                        //return quickload;
			}
			else
					quickload = 0;
	
			return interrupt.handler();
                        
	} };
	
	static  Speaker_interface spectrum_speaker_interface= new Speaker_interface
	(
	 1,
	 new int[]{50}
        );
	
	static Wave_interface spectrum_wave_interface= new Wave_interface
	(
		1,	  /* number of cassette drives = number of waves to mix */
		new int[]{25}	/* default mixing level */
        );
	
	static MachineDriver machine_driver_spectrum = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80|CPU_16BIT_PORT,
				3500000,		/* 3.5 Mhz */
				spectrum_readmem,spectrum_writemem,
				spectrum_readport,spectrum_writeport,
				spec_interrupt,1
			),
		},
		50, 2500,		/* frames per second, vblank duration */
		1,
		spectrum_init_machine,
		spectrum_shutdown_machine,
	
		/* video hardware */
		SPEC_SCREEN_WIDTH,			/* screen width */
		SPEC_SCREEN_HEIGHT, 			/* screen height */
		new rectangle( 0, SPEC_SCREEN_WIDTH-1, 0, SPEC_SCREEN_HEIGHT-1),  /* visible_area */
		spectrum_gfxdecodeinfo, 			 /* graphics decode info */
		16, 256,					/* colors used for the characters */
		spectrum_init_palette,				 /* initialise palette */
	
		VIDEO_TYPE_RASTER,
		spectrum_eof_callback,
		spectrum_vh_start,
		spectrum_vh_stop,
		spectrum_vh_screenrefresh,
	
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
 	
        /****************************************************************************************************/
        /* BETADISK/TR-DOS disc controller emulation */
        /* microcontroller KR1818VG93 is a russian wd179x clone */
        
        /*
        DRQ (D6) and INTRQ (D7).
        DRQ - signal showing request of data by microcontroller
        INTRQ - signal of completion of execution of command.
        */

        static int betadisk_status;
        static int betadisk_active;
        
        /*TODO*/////static void (*betadisk_memory_update)(void);

        //static OPBASE_HANDLER(betadisk_opbase_handler)
        public static opbase_handlerPtr betadisk_opbase_handler=new opbase_handlerPtr() {            
            public int handler (int address){

                int pc;

                //pc = activecpu_get_pc();
                pc = cpu_get_reg(z80H.Z80_PC);

                if ((pc & 0xc000)!=0x0000)
                {
                        /* outside rom area */
                        betadisk_active = 0;

                        /*TODO*/////betadisk_memory_update();
                }
                else
                {
                        /* inside rom area, switch on betadisk */
                //	betadisk_active = 1;

                //	betadisk_memory_update();
                }


                return pc & 0x0ffff;
        }};

        static void betadisk_wd179x_callback(int state)
        {
                /*TODO*/////switch (state)
                /*TODO*/////{
                /*TODO*/////        case WD179X_DRQ_SET:
                /*TODO*/////        {
                /*TODO*/////                betadisk_status |= (1<<6);
                /*TODO*/////        }
                /*TODO*/////        break;

                /*TODO*/////        case WD179X_DRQ_CLR:
                /*TODO*/////        {
                /*TODO*/////                betadisk_status &=~(1<<6);
                /*TODO*/////        }
                /*TODO*/////        break;

                /*TODO*/////        case WD179X_IRQ_SET:
                /*TODO*/////        {
                /*TODO*/////                betadisk_status |= (1<<7);
                /*TODO*/////        }
                /*TODO*/////        break;

                /*TODO*/////        case WD179X_IRQ_CLR:
                /*TODO*/////        {
                /*TODO*/////                betadisk_status &=~(1<<7);
                /*TODO*/////        }
                /*TODO*/////        break;
                /*TODO*/////}
        }

        /* these are active only when betadisk is enabled */
        //static WRITE8_HANDLER(betadisk_w)
        public static WriteHandlerPtr betadisk_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
        

                if ((betadisk_active) != 0)
                {

                }
        }};


        /* these are active only when betadisk is enabled */
        //static  READ8_HANDLER(betadisk_r)
        public static ReadHandlerPtr betadisk_r = new ReadHandlerPtr() {
            public int handler(int offset) {
                if ((betadisk_active) != 0)
                {
                        /* decoding of these ports might be wrong - to be checked! */
                        if ((offset & 0x01f)==0x01f)
                        {
                                switch (offset & 0x0ff)
                                {

                                }
                        }

                }

                return 0x0ff;
        }};

        static void betadisk_init()
        {
                betadisk_active = 0;
                betadisk_status = 0x03f;
                /*TODO*/////wd179x_init(WD_TYPE_179X,&betadisk_wd179x_callback);
        }
        
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static RomLoadPtr rom_spectrum = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD("spectrum.rom", 0x0000, 0x4000, 0xddee531f);
                //ROM_LOAD("spectrum.rom", 0x0000, 0x4000, 0xb96a36be);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specbusy = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD("48-busy.rom", 0x0000, 0x4000, 0x1511cddb);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specgrot = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD("48-groot.rom", 0x0000, 0x4000, 0xabf18c45);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specimc = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD("48-imc.rom", 0x0000, 0x4000, 0xd1be99ee);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_speclec = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000,REGION_CPU1);
		ROM_LOAD("80-lec.rom", 0x0000, 0x4000, 0x5b5c92b1);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specpls4 = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x10000,REGION_CPU1);
			ROM_LOAD("plus4.rom",0x0000,0x4000, 0x7e0f47cb);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tk90x = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x10000,REGION_CPU1);
			ROM_LOAD("tk90x.rom",0x0000,0x4000, 0x3e785f6f);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tk95 = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x10000,REGION_CPU1);
			ROM_LOAD("tk95.rom",0x0000,0x4000, 0x17368e07);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_inves = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x10000,REGION_CPU1);
			ROM_LOAD("inves.rom",0x0000,0x4000, 0x8ff7a4d1);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specp2fr = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x18000,REGION_CPU1);
			ROM_LOAD("plus2fr0.rom",0x10000,0x4000, 0xc684c535);
			ROM_LOAD("plus2fr1.rom",0x14000,0x4000, 0xf5e509c5);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_specp2sp = new RomLoadPtr(){ public void handler(){ 
			ROM_REGION(0x18000,REGION_CPU1);
			ROM_LOAD("plus2sp0.rom",0x10000,0x4000, 0xe807d06e);
			ROM_LOAD("plus2sp1.rom",0x14000,0x4000, 0x41981d4b);
	ROM_END(); }}; 
	
	static IODevice IODEVICE_SPEC_QUICK = 
            new IODevice(
                    IO_QUICKLOAD,		/* type */
                    1,					/* count */
                    "scr\0",       /* file extensions */
                    IO_RESET_ALL,		/* reset if file changed */
                    null,	/* id */
                    spec_quick_init,	/* init */
                    spec_quick_exit, /* exit */
                    null, /* info */
                    spec_quick_open, /* open */
                    null, /* close */
                    null, /* status */
                    null, /* seek */
                    null, /* tell */
                    null, /* input */
                    null, /* output */
                    null, /* input_chunk */
                    null /* output_chunk */                
                );
                
        
        
        static IODevice io_spectrum[] = {
            new IODevice(
                IO_SNAPSHOT,		/* type */
		1,					/* count */
		"sna\0z80\0",       /* file extensions */
		IO_RESET_ALL,		/* reset if file changed */
		spectrum_rom_id,	/* id */
		spectrum_rom_load,	/* init */
		//spectrum_rom_exit,	/* exit */
                null, /* exit */
                null, /* info */
                null, /* open */
                null, /* close */
                null, /* status */
                null, /* seek */
                null, /* tell */
                null, /* input */
                null, /* output */
                null, /* input_chunk */
                null /* output_chunk */
                ),
                IODEVICE_SPEC_QUICK,
		new IODevice(
                IO_CASSETTE,		/* type */
		1,					/* count */
		"wav\0tap\0",       /* file extensions */
		IO_RESET_NONE,		/* reset if file changed */
		null,	/* id */
		spectrum_cassette_init,	/* init */
		spectrum_cassette_exit,	/* exit */
                wave_info,			/* info */						
                wave_open,			/* open */						
                wave_close, 		/* close */ 					
                wave_status,		/* status */					
                wave_seek,			/* seek */						
                wave_tell,			/* tell */						
                wave_input, 		/* input */ 					
                wave_output,		/* output */					
                wave_input_chunk,	/* input_chunk */				
                wave_output_chunk	/* output_chunk */
                ),
                new IODevice(IO_END)
        };
        
	
	/*#define io_spec128	io_spectrum
	#define io_spec128s io_spectrum
	#define io_specpls2 io_spectrum
	#define io_specbusy io_spectrum
	#define io_specgrot io_spectrum
	#define io_specimc	io_spectrum
	#define io_speclec	io_spectrum
	#define io_specpls4 io_spectrum
	#define io_inves	io_spectrum
	#define io_tk90x	io_spectrum
	#define io_tk95 	io_spectrum
	#define io_tc2048	io_spectrum
	#define io_ts2068	io_spectrum
	#define io_specpl2a io_specpls3
	#define io_specp2fr io_spectrum
	#define io_specp2sp io_spectrum
	#define io_specp3sp io_specpls3
	#define io_specpl3e io_specpls3*/
	
	/*         YEAR  NAME      PARENT        MACHINE                 INPUT     INIT      COMPANY   FULLNAME */
        // COMP ( 1982, , 0,		 spectrum,		 spectrum, 0,			 "",    "" );
        public static GameDriver driver_spectrum = new GameDriver("1982", "spectrum", "spectrum.java", rom_spectrum, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Sinclair Research", "ZX Spectrum");
        
        // COMPX( 2000, specpls4, spectrum, spectrum,		 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum +4", GAME_COMPUTER_MODIFIED );
        public static GameDriver driver_specpls4 = new GameDriver("2000", "specpls4", "spectrum.java", rom_specpls4, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum +4");
		
	//COMPX( 1994, specbusy, spectrum, spectrum,		 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum (BusySoft Upgrade)", GAME_COMPUTER_MODIFIED )
	public static GameDriver driver_specbusy = new GameDriver("1994", "specbusy", "spectrum.java", rom_specbusy, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum (BusySoft Upgrade)");
        
        //COMPX( ????, specgrot, spectrum, spectrum,		 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum (De Groot's Upgrade)", GAME_COMPUTER_MODIFIED )
	public static GameDriver driver_specgrot = new GameDriver("????", "specgrot", "spectrum.java", rom_specgrot, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum (De Groot's Upgrade)");
        
        //COMPX( 1985, specimc,  spectrum, spectrum,		 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum (Collier's Upgrade)", GAME_COMPUTER_MODIFIED )
	public static GameDriver driver_specimc = new GameDriver("1985", "specimc", "spectrum.java", rom_specimc, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum (Collier's Upgrade)");
        
        //COMPX( 1987, speclec,  spectrum, spectrum,		 spectrum, 0,			 "Amstrad plc",          "ZX Spectrum (LEC Upgrade)", GAME_COMPUTER_MODIFIED )
	public static GameDriver driver_speclec = new GameDriver("1987", "speclec", "spectrum.java", rom_speclec, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Amstrad plc", "ZX Spectrum (LEC Upgrade)");
        
        //COMP ( 1986, inves,    spectrum, spectrum,		 spectrum, 0,			 "Investronica",         "Inves Spectrum 48K+" )
	public static GameDriver driver_inves = new GameDriver("1986", "inves", "spectrum.java", rom_inves, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Investronica", "Inves Spectrum 48K+");
        
        //COMP ( 1985, tk90x,    spectrum, spectrum,		 spectrum, 0,			 "Micro Digital",        "TK90x Color Computer" )
	public static GameDriver driver_tk90x = new GameDriver("1985", "tk90x", "spectrum.java", rom_tk90x, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Micro Digital", "TK90x Color Computer");

        //COMP ( 1986, tk95,	   spectrum, spectrum,		 spectrum, 0,			 "Micro Digital",        "TK95 Color Computer" )
	public static GameDriver driver_tk95 = new GameDriver("1986", "tk95", "spectrum.java", rom_tk95, null, machine_driver_spectrum, input_ports_spectrum, null, io_spectrum, "Micro Digital", "TK95 Color Computer");

}

