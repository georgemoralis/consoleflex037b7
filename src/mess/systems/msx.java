package mess.systems;

import static old.mame.inptportH.*;
import static old.mame.inptport.*;
import static old.mame.cpuintrf.*;
import static old.mame.common.*;
import old.mame.drawgfxH.*;
import static WIP.arcadeflex.fucPtr.*;
import WIP.mame.sndintrfH.MachineSound;
import old.mame.drawgfxH.GfxDecodeInfo;
import old.mame.drawgfxH.rectangle;
import static old.mame.inputH.*;
import static old.mame.driverH.*;
import static mame.commonH.*;

import static mess.messH.*;
import static mess.machine.spectrum.*;
import static arcadeflex.libc.cstring.memset;

import WIP.arcadeflex.fucPtr.RomLoadPtr;
import WIP.arcadeflex.fucPtr.VhStopPtr;
import WIP.arcadeflex.fucPtr.VhUpdatePtr;
import WIP.arcadeflex.libc_v2.*;
import static old.arcadeflex.osdepend.logerror;
import static WIP.arcadeflex.libc.memcpy.*;
import static WIP.mame.memoryH.*;
import static WIP.mame.memory.*;

import static WIP.mame.sndintrfH.*;
import static WIP.sound.sccH.*;
import static WIP.sound.scc.*;
import static old.sound.dacH.*;
import static old.sound.dac.*;

import consoleflex.funcPtr.*;
import cpu.z80.z80H;
import mess.messH.GameDriver;
import mess.messH.IODevice;

import static sound.ay8910.*;
import static sound.ay8910H.*;

import sound._2413intfH.YM2413interface;
import static sound.wave.*;
import static sound.waveH.*;
import static old.sound.CustomSound.*;

import WIP.mame.osdependH.osd_bitmap;
import WIP.mame.sndintrfH.CustomSound_interface;

import static mess.vidhrdw.tms9928aH.*;
import static mess.vidhrdw.tms9928a.*;

import static mess.machine.msxH.*;
import static mess.machine.msx.*;

import static mess.machine._8255ppiH.*;
import static mess.machine._8255ppi.*;



/*
** msx.c : driver for MSX1
**
** Todo:
** - Add support for other MSX models (br,fr,de,ru etc.)
*/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 

public class msx
{
	
	MSX msx1;
	
	static MemoryReadAddress readmem[] =
	{
	    new MemoryReadAddress( 0x0000, 0x1fff, MRA_BANK1 ),
	    new MemoryReadAddress( 0x2000, 0x3fff, MRA_BANK2 ),
	    new MemoryReadAddress( 0x4000, 0x5fff, MRA_BANK3 ),
	    new MemoryReadAddress( 0x6000, 0x7fff, MRA_BANK4 ),
	    new MemoryReadAddress( 0x8000, 0x9fff, MRA_BANK5 ),
	    new MemoryReadAddress( 0xa000, 0xbfff, MRA_BANK6 ),
	    new MemoryReadAddress( 0xc000, 0xdfff, MRA_BANK7 ),
	    new MemoryReadAddress( 0xe000, 0xffff, MRA_BANK8 ),
	        new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress writemem[] =
	{
	    new MemoryWriteAddress( 0x0000, 0x3fff, msx_writemem0 ),
	    new MemoryWriteAddress( 0x4000, 0x7fff, msx_writemem1 ),
	    new MemoryWriteAddress( 0x8000, 0xbfff, msx_writemem2 ),
	    new MemoryWriteAddress( 0xc000, 0xffff, msx_writemem3 ),
	        new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static IOReadPort readport[] =
	{
	    new IOReadPort( 0x90, 0x91, msx_printer_r ),
	    new IOReadPort( 0xa0, 0xa7, msx_psg_r ),
	    new IOReadPort( 0xa8, 0xab, ppi8255_0_r ),
	    new IOReadPort( 0x98, 0x99, msx_vdp_r ),
	        new IOReadPort( -1 )  /* end of table */
	};
	
	static IOWritePort writeport[] =
	{
	    new IOWritePort( 0x7c, 0x7d, msx_fmpac_w ),
	    new IOWritePort( 0x90, 0x91, msx_printer_w ),
	    new IOWritePort( 0xa0, 0xa7, msx_psg_w ),
	    new IOWritePort( 0xa8, 0xab, ppi8255_0_w ),
	    new IOWritePort( 0x98, 0x99, msx_vdp_w ),
	        new IOWritePort( -1 )  /* end of table */
	};
	
	
	static InputPortPtr input_ports_msx = new InputPortPtr(){ public void handler() { 
	 PORT_START();  /* 0 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0 )", KEYCODE_0, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "1 !", KEYCODE_1, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "2 @", KEYCODE_2, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "3 #", KEYCODE_3, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "4 $", KEYCODE_4, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "5 %", KEYCODE_5, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "6 ^", KEYCODE_6, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "7 &", KEYCODE_7, IP_JOY_NONE);
	
	 PORT_START();  /* 1 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "8 *", KEYCODE_8, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9 (", KEYCODE_9, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "- _", KEYCODE_MINUS, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "= +", KEYCODE_EQUALS, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "\\ |", KEYCODE_BACKSLASH, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "[ {", KEYCODE_OPENBRACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "] }", KEYCODE_CLOSEBRACE, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "; :", KEYCODE_COLON, IP_JOY_NONE);
	
	 PORT_START();  /* 2 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "' \"", KEYCODE_QUOTE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "` ~", KEYCODE_TILDE, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, ", <", KEYCODE_COMMA, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, ". >", KEYCODE_STOP, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "/ ?", KEYCODE_SLASH, IP_JOY_NONE);
	  /*TODO*///PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "Dead Key", KEYCODE_NONE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "a A", KEYCODE_A, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "b B", KEYCODE_B, IP_JOY_NONE);
	
	 PORT_START();  /* 3 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "c C", KEYCODE_C, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "d D", KEYCODE_D, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "e E", KEYCODE_E, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "f F", KEYCODE_F, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "g G", KEYCODE_G, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "h H", KEYCODE_H, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "i I", KEYCODE_I, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "j J", KEYCODE_J, IP_JOY_NONE);
	
	 PORT_START();  /* 4 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "k K", KEYCODE_K, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "l L", KEYCODE_L, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "m M", KEYCODE_M, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "n N", KEYCODE_N, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "o O", KEYCODE_O, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "p P", KEYCODE_P, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "q Q", KEYCODE_Q, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "r R", KEYCODE_R, IP_JOY_NONE);
	
	 PORT_START();  /* 5 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "s S", KEYCODE_S, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "t T", KEYCODE_T, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "u U", KEYCODE_U, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "v V", KEYCODE_V, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "w W", KEYCODE_W, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "x X", KEYCODE_X, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "y Y", KEYCODE_Y, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "z Z", KEYCODE_Z, IP_JOY_NONE);
	
	 PORT_START();  /* 6 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "CTRL", KEYCODE_LCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "GRAPH", KEYCODE_PGUP, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "CAPS", KEYCODE_CAPSLOCK, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "CODE", KEYCODE_PGDN, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "F1", KEYCODE_F1, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "F2", KEYCODE_F2, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "F3", KEYCODE_F3, IP_JOY_NONE);
	
	 PORT_START();  /* 7 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "F4", KEYCODE_F4, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "F5", KEYCODE_F5, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "ESC", KEYCODE_ESC, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "TAB", KEYCODE_TAB, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "STOP", KEYCODE_RCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "BACKSPACE", KEYCODE_BACKSPACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "SELECT", KEYCODE_END, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER", KEYCODE_ENTER, IP_JOY_NONE);
	
	 PORT_START();  /* 8 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE", KEYCODE_SPACE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "HOME", KEYCODE_HOME, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "INSERT", KEYCODE_INSERT, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "DEL", KEYCODE_DEL, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "LEFT", KEYCODE_LEFT, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "UP", KEYCODE_UP, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "DOWN", KEYCODE_DOWN, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "RIGHT", KEYCODE_RIGHT, IP_JOY_NONE);
	
	 PORT_START();  /* 9 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	
	 PORT_START();  /* 10 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_msxuk = new InputPortPtr(){ public void handler() { 
	 PORT_START();  /* 0 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0 )", KEYCODE_0, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "1 !", KEYCODE_1, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "2 @", KEYCODE_2, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "3 #", KEYCODE_3, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "4 $", KEYCODE_4, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "5 %", KEYCODE_5, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "6 ^", KEYCODE_6, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "7 &", KEYCODE_7, IP_JOY_NONE);
	
	 PORT_START();  /* 1 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "8 *", KEYCODE_8, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9 (", KEYCODE_9, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "- _", KEYCODE_MINUS, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "= +", KEYCODE_EQUALS, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "\\ |", KEYCODE_BACKSLASH, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "[ {", KEYCODE_OPENBRACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "] }", KEYCODE_CLOSEBRACE, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "; :", KEYCODE_COLON, IP_JOY_NONE);
	
	 PORT_START();  /* 2 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "' \"", KEYCODE_QUOTE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "POUND STERLING ~", KEYCODE_TILDE, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, ", <", KEYCODE_COMMA, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, ". >", KEYCODE_STOP, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "/ ?", KEYCODE_SLASH, IP_JOY_NONE);
	  /*TODO*///PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "Dead Key", KEYCODE_NONE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "a A", KEYCODE_A, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "b B", KEYCODE_B, IP_JOY_NONE);
	
	 PORT_START();  /* 3 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "c C", KEYCODE_C, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "d D", KEYCODE_D, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "e E", KEYCODE_E, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "f F", KEYCODE_F, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "g G", KEYCODE_G, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "h H", KEYCODE_H, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "i I", KEYCODE_I, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "j J", KEYCODE_J, IP_JOY_NONE);
	
	 PORT_START();  /* 4 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "k K", KEYCODE_K, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "l L", KEYCODE_L, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "m M", KEYCODE_M, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "n N", KEYCODE_N, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "o O", KEYCODE_O, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "p P", KEYCODE_P, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "q Q", KEYCODE_Q, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "r R", KEYCODE_R, IP_JOY_NONE);
	
	 PORT_START();  /* 5 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "s S", KEYCODE_S, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "t T", KEYCODE_T, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "u U", KEYCODE_U, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "v V", KEYCODE_V, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "w W", KEYCODE_W, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "x X", KEYCODE_X, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "y Y", KEYCODE_Y, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "z Z", KEYCODE_Z, IP_JOY_NONE);
	
	 PORT_START();  /* 6 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "CTRL", KEYCODE_LCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "GRAPH", KEYCODE_PGUP, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "CAPS", KEYCODE_CAPSLOCK, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "CODE", KEYCODE_PGDN, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "F1", KEYCODE_F1, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "F2", KEYCODE_F2, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "F3", KEYCODE_F3, IP_JOY_NONE);
	
	 PORT_START();  /* 7 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "F4", KEYCODE_F4, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "F5", KEYCODE_F5, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "ESC", KEYCODE_ESC, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "TAB", KEYCODE_TAB, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "STOP", KEYCODE_RCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "BACKSPACE", KEYCODE_BACKSPACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "SELECT", KEYCODE_END, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER", KEYCODE_ENTER, IP_JOY_NONE);
	
	 PORT_START();  /* 8 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE", KEYCODE_SPACE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "HOME", KEYCODE_HOME, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "INSERT", KEYCODE_INSERT, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "DEL", KEYCODE_DEL, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "LEFT", KEYCODE_LEFT, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "UP", KEYCODE_UP, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "DOWN", KEYCODE_DOWN, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "RIGHT", KEYCODE_RIGHT, IP_JOY_NONE);
	
	 PORT_START();  /* 9 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	
	 PORT_START();  /* 10 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_msxj = new InputPortPtr(){ public void handler() { 
	 PORT_START();  /* 0 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0", KEYCODE_0, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "1 !", KEYCODE_1, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "2 \"", KEYCODE_2, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "3 #", KEYCODE_3, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "4 $", KEYCODE_4, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "5 %", KEYCODE_5, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "6 &", KEYCODE_6, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "7 '", KEYCODE_7, IP_JOY_NONE);
	
	 PORT_START();  /* 1 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "8 (", KEYCODE_8, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9 )", KEYCODE_9, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "- =", KEYCODE_MINUS, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "^ ~", KEYCODE_EQUALS, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Yen |", KEYCODE_BACKSLASH, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "@ `", KEYCODE_OPENBRACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "[ }", KEYCODE_CLOSEBRACE, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "; +", KEYCODE_COLON, IP_JOY_NONE);
	
	 PORT_START();  /* 2 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, ": *", KEYCODE_QUOTE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "] }", KEYCODE_TILDE, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, ", <", KEYCODE_COMMA, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, ". >", KEYCODE_STOP, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "/ ?", KEYCODE_SLASH, IP_JOY_NONE);
	  /*TODO*///PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "  _", KEYCODE_NONE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "a A", KEYCODE_A, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "b B", KEYCODE_B, IP_JOY_NONE);
	
	 PORT_START();  /* 3 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "c C", KEYCODE_C, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "d D", KEYCODE_D, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "e E", KEYCODE_E, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "f F", KEYCODE_F, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "g G", KEYCODE_G, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "h H", KEYCODE_H, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "i I", KEYCODE_I, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "j J", KEYCODE_J, IP_JOY_NONE);
	
	 PORT_START();  /* 4 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "k K", KEYCODE_K, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "l L", KEYCODE_L, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "m M", KEYCODE_M, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "n N", KEYCODE_N, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "o O", KEYCODE_O, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "p P", KEYCODE_P, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "q Q", KEYCODE_Q, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "r R", KEYCODE_R, IP_JOY_NONE);
	
	 PORT_START();  /* 5 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "s S", KEYCODE_S, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "t T", KEYCODE_T, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "u U", KEYCODE_U, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "v V", KEYCODE_V, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "w W", KEYCODE_W, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "x X", KEYCODE_X, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "y Y", KEYCODE_Y, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "z Z", KEYCODE_Z, IP_JOY_NONE);
	
	 PORT_START();  /* 6 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "CTRL", KEYCODE_LCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "GRAPH", KEYCODE_PGUP, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "CAPS", KEYCODE_CAPSLOCK, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "KANA", KEYCODE_PGDN, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "F1", KEYCODE_F1, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "F2", KEYCODE_F2, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "F3", KEYCODE_F3, IP_JOY_NONE);
	
	 PORT_START();  /* 7 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "F4", KEYCODE_F4, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "F5", KEYCODE_F5, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "ESC", KEYCODE_ESC, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "TAB", KEYCODE_TAB, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "STOP", KEYCODE_RCONTROL, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "BACKSPACE", KEYCODE_BACKSPACE, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "SELECT", KEYCODE_END, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER", KEYCODE_ENTER, IP_JOY_NONE);
	
	 PORT_START();  /* 8 */
	  PORT_BITX (0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE", KEYCODE_SPACE, IP_JOY_NONE);
	  PORT_BITX (0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "HOME", KEYCODE_HOME, IP_JOY_NONE);
	  PORT_BITX (0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "INSERT", KEYCODE_INSERT, IP_JOY_NONE);
	  PORT_BITX (0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "DEL", KEYCODE_DEL, IP_JOY_NONE);
	  PORT_BITX (0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "LEFT", KEYCODE_LEFT, IP_JOY_NONE);
	  PORT_BITX (0x20, IP_ACTIVE_LOW, IPT_KEYBOARD, "UP", KEYCODE_UP, IP_JOY_NONE);
	  PORT_BITX (0x40, IP_ACTIVE_LOW, IPT_KEYBOARD, "DOWN", KEYCODE_DOWN, IP_JOY_NONE);
	  PORT_BITX (0x80, IP_ACTIVE_LOW, IPT_KEYBOARD, "RIGHT", KEYCODE_RIGHT, IP_JOY_NONE);
	
	 PORT_START();  /* 9 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	
	 PORT_START();  /* 10 */
	  PORT_BIT (0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_UP | IPF_PLAYER2);
	  PORT_BIT (0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN | IPF_PLAYER2);
	  PORT_BIT (0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT | IPF_PLAYER2);
	  PORT_BIT (0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_PLAYER2);
	  PORT_BIT (0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2);
	  PORT_BIT (0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2);
	  PORT_BITX (0xc0, IP_ACTIVE_LOW, IPT_UNUSED, DEF_STR( "Unused"), IP_KEY_NONE, IP_JOY_NONE);
	INPUT_PORTS_END(); }}; 
	
	static GfxDecodeInfo gfxdecodeinfo[] =
	{
	        new GfxDecodeInfo( -1 ) /* end of array */
	};
	//extern READ_HANDLER ( msx_psg_read_port_a );
	
	static AY8910interface ay8910_interface = new AY8910interface
	(
	    1,  /* 1 chip */
	    1789773,    /* 1.7897725 MHz */
	    new int[] { 10 },
	    new ReadHandlerPtr[] { msx_psg_port_a_r },
	    new ReadHandlerPtr[] { msx_psg_port_b_r },
	    new WriteHandlerPtr[] { msx_psg_port_a_w },
	    new WriteHandlerPtr[] { msx_psg_port_b_w }
	);
	
	static CustomSound_interface scc_custom_interface = new CustomSound_interface
	(
	     SCC_sh_start,
	     null, null
	);
	
	static DACinterface dac_interface = new DACinterface
	(
	    1,
	    new int[] { 10 }
	);
	
	static YM2413interface ym2413_interface = new YM2413interface
	(
	    1,                      /* 1 chip */
	    3579545,                /* 3.57Mhz.. ? */
	    new int[]{ 10 }        /* Volume */
	    //{ 0 }                   /* IRQ handler */
	);
	
	static Wave_interface wave_interface = new Wave_interface
	(
	    1,              /* number of waves */
	    new int[]{ 25 }          /* mixing levels */
	);
	
	
	public static InterruptPtr msx_interrupt = new InterruptPtr() { public int handler() 
	{
	    TMS9928A_interrupt();
	    return ignore_interrupt.handler();
	} };
	
	public static VhStartPtr msx_vh_start = new VhStartPtr() { public int handler() 
	{
	    return TMS9928A_start(TMS99x8A, 0x4000);
	} };
	
	public static VhStopPtr msx_vh_stop = new VhStopPtr() {
        public void handler() {
            TMS9928A_stop();
        }
    };
    
    public static VhUpdatePtr msx_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            TMS9928A_refresh(bitmap, full_refresh);
        }
    };
	
	static MachineDriver machine_driver_msx = new MachineDriver
	(
	    /* basic machine hardware */
	    new MachineCPU[] {
	        new MachineCPU(
	            CPU_Z80,
	            3579545,    /* 3.579545 Mhz */
	            readmem,writemem,readport,writeport,
	            msx_interrupt,1
	        )
	    },
	    60, DEFAULT_REAL_60HZ_VBLANK_DURATION,
	    1,
	    msx_ch_reset, /* init_machine */
	    msx_ch_stop, /* stop_machine */
	
	    /* video hardware */
	    32*8, 24*8, new rectangle( 0*8, 32*8-1, 0*8, 24*8-1 ),
	    gfxdecodeinfo,
	    TMS9928A_PALETTE_SIZE,TMS9928A_COLORTABLE_SIZE,
	    tms9928A_init_palette,
	
	    VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_TYPE_RASTER,
	    null,
	    msx_vh_start,
	    msx_vh_stop,
	    msx_vh_screenrefresh,
	
	    /* sound hardware */
	    0,0,0,0,
	    /*new MachineSound[] {
	        new MachineSound(
	            SOUND_AY8910,
	            ay8910_interface
	        ),
	        new MachineSound(
	            SOUND_CUSTOM,
	            scc_custom_interface
	        ),
	        new MachineSound(
	            SOUND_DAC,
	            dac_interface
	        ),
	        new MachineSound(
	            SOUND_YM2413,
	            ym2413_interface
	        ),
	        new MachineSound(
	            SOUND_WAVE,
	            wave_interface
	        )
	    }*/
	    null
	);
	
	static MachineDriver machine_driver_msx_pal = new MachineDriver
	(
	    /* basic machine hardware */
	    new MachineCPU[] {
	        new MachineCPU(
	            CPU_Z80,
	            3579545,    /* 3.579545 Mhz */
	            readmem,writemem,readport,writeport,
	            msx_interrupt,1
	        )
	    },
	    50, DEFAULT_REAL_60HZ_VBLANK_DURATION,
	    1,
	    msx_ch_reset, /* init_machine */
	    msx_ch_stop, /* stop_machine */
	
	    /* video hardware */
	    32*8, 24*8, new rectangle( 0*8, 32*8-1, 0*8, 24*8-1 ),
	    gfxdecodeinfo,
	    TMS9928A_PALETTE_SIZE,TMS9928A_COLORTABLE_SIZE,
	    tms9928A_init_palette,
	
	    VIDEO_MODIFIES_PALETTE | VIDEO_UPDATE_BEFORE_VBLANK | VIDEO_TYPE_RASTER,
	    null,
	    msx_vh_start,
	    msx_vh_stop,
	    msx_vh_screenrefresh,
	
	    /* sound hardware */
	    0,0,0,0,
	    /*new MachineSound[] {
	        new MachineSound(
	            SOUND_AY8910,
	            ay8910_interface
	        ),
	        new MachineSound(
	            SOUND_CUSTOM,
	            scc_custom_interface
	        ),
	        new MachineSound(
	            SOUND_DAC,
	            dac_interface
	        ),
	        new MachineSound(
	            SOUND_YM2413,
	            ym2413_interface
	        ),
	        new MachineSound(
	            SOUND_WAVE,
	            wave_interface
	        )
	    }*/
	    null
	);
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/

	static RomLoadPtr rom_msx = new RomLoadPtr(){ public void handler(){
	    ROM_REGION (0x10000, REGION_CPU1);
	    //ROM_LOAD ("msx.rom", 0x0000, 0x8000, 0x94ee12f3);
	    ROM_LOAD ("msx.rom", 0x0000, 0x8000, 0x8205795e);
	    
	    ROM_END(); }};
	
	static RomLoadPtr rom_msxj = new RomLoadPtr(){ public void handler(){
	    ROM_REGION (0x10000, REGION_CPU1);
	    ROM_LOAD ("msxj.rom", 0x0000, 0x8000, 0xee229390);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_msxuk = new RomLoadPtr(){ public void handler(){
	    ROM_REGION (0x10000, REGION_CPU1);
	    ROM_LOAD ("msxuk.rom", 0x0000, 0x8000, 0xe9ccd789);
	ROM_END(); }}; 
	
	static RomLoadPtr rom_msxkr = new RomLoadPtr(){ public void handler(){
	    ROM_REGION (0x10000, REGION_CPU1);
	    ROM_LOAD ("msxkr.rom", 0x0000, 0x8000, 0x3ab0cd3b);
	    ROM_LOAD_OPTIONAL ("msxhan.rom", 0x8000, 0x4000, 0x97478efb);
	ROM_END(); }}; 
	
	static IODevice io_msx[] = {
			new IODevice(
					IO_CARTSLOT,                /* type */
					MSX_MAX_CARTS,              /* count */
					"rom\0",                    /* file extensions */
					IO_RESET_ALL,				/* reset if file changed */
					msx_id_rom,                 /* id */
					msx_load_rom,               /* init */
					msx_exit_rom,               /* exit */
					null,                       /* info */
					null,                       /* open */
					null,                       /* close */
					null,                       /* status */
					null,                       /* seek */
					null,                       /* tell */
					null,                       /* input */
					null,                       /* output */
					null,                       /* input_chunk */
					null                        /* output_chunk */
		),
			/*TODO*///	new IODevice(
			/*TODO*///            IO_CASSETTE,		/* type */
			/*TODO*///	1,					/* count */
			/*TODO*///	"wav\0",       /* file extensions */
			/*TODO*///	IO_RESET_NONE,		/* reset if file changed */
			/*TODO*///	null,	/* id */
			/*TODO*///	msx_cassette_init,	/* init */
			/*TODO*///	msx_cassette_exit,	/* exit */
			/*TODO*///             wave_info,			/* info */						
			/*TODO*///             wave_open,			/* open */						
			/*TODO*///             wave_close, 		/* close */ 					
			/*TODO*///            wave_status,		/* status */					
			/*TODO*///             wave_seek,			/* seek */						
			/*TODO*///            wave_tell,			/* tell */						
			/*TODO*///            wave_input, 		/* input */ 					
			/*TODO*///            wave_output,		/* output */					
			/*TODO*///            wave_input_chunk,	/* input_chunk */				
			/*TODO*///            wave_output_chunk	/* output_chunk */
			/*TODO*///            ),		
	    new IODevice(IO_END)
	};
	
	static  IODevice[] io_msxj=io_msx;
	static  IODevice[] io_msxkr=io_msx;
	static  IODevice[] io_msxuk=io_msx;
	
	/*    YEAR  NAME      PARENT    MACHINE   INPUT     INIT      COMPANY   FULLNAME */
	//COMP( 1983, msx, 0, msx_pal, msx, msx, "ASCII & Microsoft", "MSX1" )
	public static GameDriver driver_msx = new GameDriver("1983", "msx", "msx.java", rom_msx, null, machine_driver_msx, input_ports_msx, null, io_msx, "ASCII & Microsoft", "MSX1");
	//COMP( 1983, msxj, msx, msx, msxj, msx, "ASCII & Microsoft", "MSX1 (Japan)" )
	public static GameDriver driver_msxj = new GameDriver("1983", "msxj", "msx.java", rom_msxj, null, machine_driver_msx, input_ports_msx, null, io_msx, "ASCII & Microsoft", "MSX1 (Japan)");
	//COMP( 1983, msxkr, msx, msx, msxj, msx, "ASCII & Microsoft", "MSX1 (Korean)" )
	public static GameDriver driver_msxkr = new GameDriver("1983", "msxkr", "msx.java", rom_msxkr, null, machine_driver_msx, input_ports_msx, null, io_msx, "ASCII & Microsoft", "MSX1 (Korean)");
	//COMP( 1983, msxuk, msx, msx_pal, msxuk, msx, "ASCII & Microsoft", "MSX1 (UK)" )
	public static GameDriver driver_msxuk = new GameDriver("1983", "msxuk", "msx.java", rom_msxuk, null, machine_driver_msx, input_ports_msx, null, io_msx, "ASCII & Microsoft", "MSX1 (UK)");
}
