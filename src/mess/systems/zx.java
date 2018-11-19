/***************************************************************************
	zx.c

    system driver
	Juergen Buchmueller <pullmoll@t-online.de>, Dec 1999

	TODO:
	Check the CPU clock, scanlines, cycles per scanlines value ect.
	It seems I'm very close to the real video timing now, but it
	still isn't perfect (see also vidhrdw/zx.c)

****************************************************************************/
package mess.systems;

import static WIP.mame.memoryH.*;
import static mame.commonH.*;
import static old.mame.inptportH.*;
import static old.mame.inputH.*;

import WIP.arcadeflex.fucPtr.VhConvertColorPromPtr;
import WIP.arcadeflex.libc_v2.UBytePtr;
import WIP.mame.sndintrfH.MachineSound;
import old.mame.drawgfxH.GfxDecodeInfo;
import old.mame.drawgfxH.GfxLayout;
import old.mame.drawgfxH.rectangle;
import old.mame.driverH.MachineCPU;
import old.mame.driverH.MachineDriver;
import static old.mame.cpuintrf.*;

import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.memoryH.*;
import static mess.messH.*;
import static old.mame.driverH.*;

import static old.sound.dacH.*;
import static old.sound.dac.*;
import static WIP.mame.sndintrfH.*;

import static old.mame.drawgfxH.*;

import static mess.machine.zx.*;
import static mess.vidhrdw.zx.*;
import static vidhrdw.generic.*;

public class zx {
	//#include "driver.h"
	//#include "vidhrdw/generic.h"
	//#include "cpu/z80/z80.h"
	//#include "includes/zx.h"
	
	public static int VERBOSE = 0;
	
	//#if VERBOSE
	//#define LOG(x)	if( errorlog ) fprintf x
	//#else
	//#define LOG(x)						   /* x */
	//#endif
	
	static MemoryReadAddress readmem_zx80[] =
	{
		new MemoryReadAddress(0x0000, 0x0fff, MRA_ROM),
		new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
		new MemoryReadAddress(0x8000, 0xffff, MRA_NOP),
		new MemoryReadAddress(-1)
	};
	
	static MemoryWriteAddress writemem_zx80[] =
	{
		new MemoryWriteAddress(0x0000, 0x0fff, MWA_ROM),
		new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
		new MemoryWriteAddress(0x8000, 0xffff, MWA_NOP),
		new MemoryWriteAddress(-1)
	};
	
	
	static MemoryReadAddress readmem_zx81[] =
	{
		new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
		new MemoryReadAddress(0x4000, 0x43ff, MRA_RAM),
		new MemoryReadAddress(0x8000, 0xffff, MRA_NOP),
		new MemoryReadAddress(-1)
	};
	
	static MemoryWriteAddress writemem_zx81[] = 
	{
		new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
		new MemoryWriteAddress(0x4000, 0x43ff, MWA_RAM),
		new MemoryWriteAddress(0x8000, 0xffff, MWA_NOP),
		new MemoryWriteAddress(-1)
	};
	
	static MemoryReadAddress readmem_pc8300[] =
	{
		new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
		new MemoryReadAddress(0x4000, 0x7fff, MRA_RAM),		   /* PC8300 comes with 16K RAM */
		new MemoryReadAddress(0x8000, 0xffff, MRA_NOP),
		new MemoryReadAddress(-1)
	};
	
	static MemoryWriteAddress writemem_pc8300[] =
	{
		new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
		new MemoryWriteAddress(0x4000, 0x7fff, MWA_RAM),		   /* PC8300 comes with 16K RAM */
		new MemoryWriteAddress(0x8000, 0xffff, MWA_NOP),
		new MemoryWriteAddress(-1)
	};
	
	static IOReadPort readport[] =
	{
		new IOReadPort(0x0000, 0xffff, zx_io_r),
		new IOReadPort(-1)
	};
	
	static IOWritePort writeport[] =
	{
		new IOWritePort(0x0000, 0xffff, zx_io_w),
		new IOWritePort(-1)
	};
	
	static MemoryReadAddress readmem_pow3000[] =
	{
		new MemoryReadAddress(0x0000, 0x1fff, MRA_ROM),
		new MemoryReadAddress(0x4000, 0x7fff, MRA_RAM),		   /* Power 3000 comes with 16K RAM */
		new MemoryReadAddress(0x8000, 0xffff, MRA_NOP),
		new MemoryReadAddress(-1)
	};
	
	static MemoryWriteAddress writemem_pow3000[] =
	{
		new MemoryWriteAddress(0x0000, 0x3fff, MWA_ROM),
		new MemoryWriteAddress(0x4000, 0x7fff, MWA_RAM),		   /* Power 3000 comes with 16K RAM */
		new MemoryWriteAddress(0x8000, 0xffff, MWA_NOP),
		new MemoryWriteAddress(-1)
	};
	
	static IOReadPort readport_pow3000[] =
	{
		new IOReadPort(0x0000, 0xffff, pow3000_io_r),
		new IOReadPort(-1)
	};
	
	
	static IOWritePort writeport_pow3000[] =
	{
		new IOWritePort(0x0000, 0xffff, zx_io_w),
		new IOWritePort(-1)
	};
	
	static InputPortPtr input_ports_zx80 = new InputPortPtr(){ public void handler() {
	
		PORT_START();						   /* IN0 */
		PORT_DIPNAME(0x80, 0x00, "16K RAM module");
		PORT_DIPSETTING(0x00, DEF_STR( "No" ) );
		PORT_DIPSETTING(0x80, DEF_STR( "Yes" ) );
		PORT_BIT(0x7e, 0x0f, IPT_UNUSED);
		PORT_BITX(0x01, 0x00, IPT_KEYBOARD | IPF_RESETCPU, "Reset", KEYCODE_F3, IP_JOY_NONE);
		
		PORT_START();							   /* IN1 KEY ROW 0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "Z  ", KEYCODE_Z, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "X  ", KEYCODE_X, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "C  ", KEYCODE_C, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "V  ", KEYCODE_V, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN2 KEY ROW 1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "A  ", KEYCODE_A, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "S  ", KEYCODE_S, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "D  ", KEYCODE_D, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "F  ", KEYCODE_F, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "G  ", KEYCODE_G, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN3 KEY ROW 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "Q  ", KEYCODE_Q, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "W  ", KEYCODE_W, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "E  ", KEYCODE_E, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "R  ", KEYCODE_R, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "T  ", KEYCODE_T, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN4 KEY ROW 3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "1  EDIT", KEYCODE_1, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "2  AND", KEYCODE_2, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "3  THEN", KEYCODE_3, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "4  TO", KEYCODE_4, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "5  LEFT", KEYCODE_5, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN5 KEY ROW 4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0  RUBOUT", KEYCODE_0, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9  GRAPHICS", KEYCODE_9, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "8  RIGHT", KEYCODE_8, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "7  UP", KEYCODE_7, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "6  DOWN", KEYCODE_6, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN6 KEY ROW 5 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "P  PRINT", KEYCODE_P, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "O  POKE", KEYCODE_O, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "I  INPUT", KEYCODE_I, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "U  IF", KEYCODE_U, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Y  RETURN", KEYCODE_Y, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN7 KEY ROW 6 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER", KEYCODE_ENTER, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "L  ", KEYCODE_L, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "K  ", KEYCODE_K, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "J  ", KEYCODE_J, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "H  ", KEYCODE_H, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN8 KEY ROW 7 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE Pound", KEYCODE_SPACE, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, ".  ,", KEYCODE_STOP, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "M  >", KEYCODE_M, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "N  <", KEYCODE_N, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "B  ", KEYCODE_B, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN9 special keys 1 */
		PORT_BIT(0x0f, IP_ACTIVE_LOW, IPT_UNUSED);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(LEFT)", KEYCODE_LEFT, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN10 special keys 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "(BACKSPACE)", KEYCODE_BACKSPACE, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "(GRAPHICS)", KEYCODE_LALT, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "(RIGHT)", KEYCODE_RIGHT, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "(UP)", KEYCODE_UP, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(DOWN)", KEYCODE_DOWN, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		//INPUT_PORTS_END
	}};
	
	static InputPortPtr input_ports_zx81 = new InputPortPtr(){ public void handler() {
		PORT_START();							   /* IN0 */
		PORT_DIPNAME(0x80, 0x00, "16K RAM module");
		PORT_DIPSETTING(0x00, DEF_STR("No"));
		PORT_DIPSETTING(0x80, DEF_STR("Yes"));
		PORT_BIT(0x7e, 0x0f, IPT_UNUSED);
		PORT_BITX(0x01, 0x00, IPT_KEYBOARD | IPF_RESETCPU, "Reset", KEYCODE_F3, IP_JOY_NONE);
		
		PORT_START();							   /* IN1 KEY ROW 0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "Z     :", KEYCODE_Z, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "X     ;", KEYCODE_X, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "C     ?", KEYCODE_C, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "V     /", KEYCODE_V, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN2 KEY ROW 1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "A     NEW     STOP", KEYCODE_A, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "S     SAVE    LPRINT", KEYCODE_S, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "D     DIM     SLOW", KEYCODE_D, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "F     FOR     FAST", KEYCODE_F, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "G     GOTO    LLIST", KEYCODE_G, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN3 KEY ROW 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "Q     PLOT    \"\"", KEYCODE_Q, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "W     UNPLOT  OR", KEYCODE_W, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "E     REM     STEP", KEYCODE_E, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "R     RUN     <=", KEYCODE_R, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "T     RAND    <>", KEYCODE_T, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN4 KEY ROW 3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "1     EDIT", KEYCODE_1, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "2     AND", KEYCODE_2, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "3     THEN", KEYCODE_3, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "4     TO", KEYCODE_4, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "5     LEFT", KEYCODE_5, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN5 KEY ROW 4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0     RUBOUT", KEYCODE_0, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9     GRAPHICS", KEYCODE_9, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "8     RIGHT", KEYCODE_8, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "7     UP", KEYCODE_7, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "6     DOWN", KEYCODE_6, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN6 KEY ROW 5 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "P     PRINT   \"", KEYCODE_P, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "O     POKE    )", KEYCODE_O, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "I     INPUT   (", KEYCODE_I, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "U     IF      $", KEYCODE_U, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Y     RETURN  >=", KEYCODE_Y, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN7 KEY ROW 6 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER FUNCTION", KEYCODE_ENTER, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "L     LET     =", KEYCODE_L, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "K     LIST    +", KEYCODE_K, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "J     LOAD    -", KEYCODE_J, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "H     GOSUB   **", KEYCODE_H, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN8 KEY ROW 7 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE Pound", KEYCODE_SPACE, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, ".     ,", KEYCODE_STOP, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "M     PAUSE   >", KEYCODE_M, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "N     NEXT    <", KEYCODE_N, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "B     SCROLL  *", KEYCODE_B, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN9 special keys 1 */
		PORT_BIT(0x0f, IP_ACTIVE_LOW, IPT_UNUSED);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(LEFT)", KEYCODE_LEFT, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN10 special keys 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "(BACKSPACE)", KEYCODE_BACKSPACE, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "(GRAPHICS)", KEYCODE_LALT, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "(RIGHT)", KEYCODE_RIGHT, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "(UP)", KEYCODE_UP, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(DOWN)", KEYCODE_DOWN, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		//INPUT_PORTS_END
	}};
	
	static InputPortPtr input_ports_pow3000 = new InputPortPtr(){ public void handler() {
	
		PORT_START();							   /* IN0 */
		PORT_BIT(0xfe, 0x0f, IPT_UNUSED);
		PORT_BITX(0x01, 0x00, IPT_KEYBOARD | IPF_RESETCPU, "Reset", KEYCODE_F3, IP_JOY_NONE);
		
		PORT_START();							   /* IN1 KEY ROW 0 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SHIFT", KEYCODE_LSHIFT, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "Z     CLS??", KEYCODE_Z, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "X     AUTO??", KEYCODE_X, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "C     ???", KEYCODE_C, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "V     LEFT", KEYCODE_V, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN2 KEY ROW 1 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "A     UP??", KEYCODE_A, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "S     DOWN??", KEYCODE_S, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "D     RIGHT", KEYCODE_D, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "F     ", KEYCODE_F, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "G     ", KEYCODE_G, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN3 KEY ROW 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "Q     ", KEYCODE_Q, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "W     ", KEYCODE_W, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "E     ", KEYCODE_E, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "R     ", KEYCODE_R, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "T     ", KEYCODE_T, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN4 KEY ROW 3 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "1     ", KEYCODE_1, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "2     ", KEYCODE_2, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "3     ", KEYCODE_3, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "4     ", KEYCODE_4, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "5     ", KEYCODE_5, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN5 KEY ROW 4 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "0     ", KEYCODE_0, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "9     ", KEYCODE_9, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "8     ", KEYCODE_8, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "7     ", KEYCODE_7, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "6     ", KEYCODE_6, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN6 KEY ROW 5 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "P     ", KEYCODE_P, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "O     ", KEYCODE_O, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "I     ", KEYCODE_I, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "U     ", KEYCODE_U, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "Y     ", KEYCODE_Y, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN7 KEY ROW 6 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "ENTER ", KEYCODE_ENTER, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "L     ", KEYCODE_L, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "K     ", KEYCODE_K, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "J     ", KEYCODE_J, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "H     ", KEYCODE_H, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN8 KEY ROW 7 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "SPACE ", KEYCODE_SPACE, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, ".     ", KEYCODE_STOP, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "M     ", KEYCODE_M, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "N     ", KEYCODE_N, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "B     ", KEYCODE_B, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN9 special keys 1 */
		PORT_BIT(0x0f, IP_ACTIVE_LOW, IPT_UNUSED);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(LEFT)", KEYCODE_LEFT, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		PORT_START();							   /* IN10 special keys 2 */
		PORT_BITX(0x01, IP_ACTIVE_LOW, IPT_KEYBOARD, "(UP)", KEYCODE_UP, IP_JOY_NONE);
		PORT_BITX(0x02, IP_ACTIVE_LOW, IPT_KEYBOARD, "(DOWN)", KEYCODE_DOWN, IP_JOY_NONE);
		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_KEYBOARD, "(RIGHT)", KEYCODE_RIGHT, IP_JOY_NONE);
		PORT_BITX(0x08, IP_ACTIVE_LOW, IPT_KEYBOARD, "(BACKSPACE)", KEYCODE_BACKSPACE, IP_JOY_NONE);
		PORT_BITX(0x10, IP_ACTIVE_LOW, IPT_KEYBOARD, "(GRAPHICS)", KEYCODE_LALT, IP_JOY_NONE);
		PORT_BIT(0xe0, IP_ACTIVE_LOW, IPT_UNUSED);
		
		//INPUT_PORTS_END
	}};
	
	static GfxLayout zx_pixel_layout = new GfxLayout(
		8, 1,							   /* 8x1 pixels */
		256,							   /* 256 codes */
		1,								   /* 1 bit per pixel */
		new int[]{0},							   /* no bitplanes */
		/* x offsets */
		new int[]{0, 1, 2, 3, 4, 5, 6, 7},
		/* y offsets */
		new int[]{0},
		8								   /* one byte per code */
	);
	
	static GfxLayout zx_char_layout = new GfxLayout(
		8, 8,							   /* 8x8 pixels */
		64,								   /* 64 codes */
		1,								   /* 1 bit per pixel */
		new int[]{0},							   /* no bitplanes */
		/* x offsets */
		new int[]{0, 1, 2, 3, 4, 5, 6, 7},
		/* y offsets */
		new int[]{0 * 8, 1 * 8, 2 * 8, 3 * 8, 4 * 8, 5 * 8, 6 * 8, 7 * 8},
		8 * 8							   /* eight bytes per code */
	);
	
	static GfxDecodeInfo zx80_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo(REGION_GFX1, 0x0000, zx_pixel_layout, 0, 2),
		new GfxDecodeInfo(REGION_CPU1, 0x0e00, zx_char_layout, 0, 2),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo zx81_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo(REGION_GFX1, 0x0000, zx_pixel_layout, 0, 2),
		new GfxDecodeInfo(REGION_CPU1, 0x1e00, zx_char_layout, 0, 2),
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static GfxDecodeInfo pc8300_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo(REGION_GFX1, 0x0000, zx_pixel_layout, 0, 2),
		new GfxDecodeInfo(REGION_GFX2, 0x0000, zx_char_layout, 0, 2),
		new GfxDecodeInfo(-1) /* end of array */
	};
	
	static GfxDecodeInfo pow3000_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo(REGION_GFX1, 0x0000, zx_pixel_layout, 0, 2),
		new GfxDecodeInfo(REGION_GFX2, 0x0000, zx_char_layout, 0, 2),
		new GfxDecodeInfo(-1) /* end of array */
	};
	
	static char zx80_palette[] =
	{
		255,255,255,	/* white */
		  0,  0,  0,	/* black */
	};
	
	static char zx81_palette[] =
	{
		255,255,255,	/* white */
		  0,  0,  0,	/* black */
	};
	
	static char ts1000_palette[] =
	{
		 64,244,244,	/* cyan */
		  0,  0,  0,	/* black */
	};
	
	static char zx_colortable[] =
	{
		0, 1,							   /* white on black */
		1, 0							   /* black on white */
	};
	
	
	/* Initialise the palette */
	//static void zx80_init_palette(char[] sys_palette, short[] sys_colortable, char[] color_prom)
	 public static VhConvertColorPromPtr zx80_init_palette = new VhConvertColorPromPtr() {
         public void handler(char[] sys_palette, char[] sys_colortable, UBytePtr color_prom) {
		//memcpy(sys_palette, zx80_palette, sizeof (zx80_palette));
		sys_palette = zx80_palette;
		//memcpy(sys_colortable, zx_colortable, sizeof (zx_colortable));
		sys_colortable = zx_colortable;
	}};
	
	
	public static VhConvertColorPromPtr zx81_init_palette = new VhConvertColorPromPtr() {
        public void handler(char[] sys_palette, char[] sys_colortable, UBytePtr color_prom) {
		//memcpy(sys_palette, zx81_palette, sizeof (zx81_palette));
		sys_palette = zx81_palette;
		//memcpy(sys_colortable, zx_colortable, sizeof (zx_colortable));
		sys_colortable = zx_colortable;
	}};
	
    public static VhConvertColorPromPtr ts1000_init_palette = new VhConvertColorPromPtr() {
            public void handler(char[] sys_palette, char[] sys_colortable, UBytePtr color_prom) {
		//memcpy(sys_palette, ts1000_palette, sizeof (ts1000_palette));
		sys_palette = ts1000_palette;
		//memcpy(sys_colortable, zx_colortable, sizeof (zx_colortable));
		sys_colortable = zx_colortable;
	}};
	
	static DACinterface dac_interface = new DACinterface 
	(
		1,								   /* number of DACs */
		new int[]{100}							   /* volume */
	);
	
	public static int CPU_CLOCK = 3227500;
	public static int CYCLES_PER_SCANLINE = 207;
	
	static MachineDriver machine_driver_zx80 = new MachineDriver
			(
				/* basic machine hardware */
				new MachineCPU[] {
					/* MachineCPU */
						/* basic machine hardware */
						new MachineCPU(
							CPU_Z80 | CPU_16BIT_PORT,
							CPU_CLOCK,
							readmem_zx80, writemem_zx80, readport, writeport,
							ignore_interrupt, 1, null, 0
						)
				},
				new Float(1.0 * CPU_CLOCK / 310 / CYCLES_PER_SCANLINE),
				0,								/* vblank handled by vidhrdw */
				1,
				zx80_init_machine,
				zx_shutdown_machine,
			
				/* video hardware */
				32 * 8, 						/* screen width (inc. blank/sync) */
				310,
				new rectangle(0*8, 32*8 - 1, 48, 310-32-1),	/* rectangle: visible_area */
				zx80_gfxdecodeinfo, 			/* graphics decode info */
				6, 4,							/* colors used for the characters */
				zx80_init_palette,				/* init palette */
			
				VIDEO_TYPE_RASTER,
				null,
				generic_bitmapped_vh_start,
				generic_vh_stop,
				zx_vh_screenrefresh,
			
				/* sound hardware */
				0,								   /* sh init */
				0,								   /* sh start */
				0,								   /* sh stop */
				0,								   /* sh update */
				/*TODO*/////new MachineSound[] {
				/* MachineSound */
		/*TODO*/////		new MachineSound(
		/*TODO*/////				SOUND_DAC,
		/*TODO*/////				dac_interface
		/*TODO*/////		)
		/*TODO*/////	}
	                null
			);
	
	
	static MachineDriver machine_driver_zx81 = new MachineDriver
			(
		/* basic machine hardware */
		new MachineCPU[] {
			/* MachineCPU */
				/* basic machine hardware */
				new MachineCPU(
						CPU_Z80 | CPU_16BIT_PORT,
						CPU_CLOCK,
						readmem_zx81, writemem_zx81, readport, writeport,
						ignore_interrupt, 1
				)
		},				
		
		new Float(1.0 * CPU_CLOCK / 310 / CYCLES_PER_SCANLINE),
		0,								/* vblank handled by vidhrdw */
		1,
		zx81_init_machine,
		zx_shutdown_machine,
	
		/* video hardware */
		32 * 8, 						/* screen width (inc. blank/sync) */
		310,							/* screen height (inc. blank/sync) */
		new rectangle(0*8, 32*8-1, 48, 310-32-1),	/* visible area (inc. some vertical overscan) */
		zx81_gfxdecodeinfo, 			/* graphics decode info */
		6, 4,							/* colors used for the characters */
		zx81_init_palette,				/* init palette */
	
		VIDEO_TYPE_RASTER,
		null,
		generic_bitmapped_vh_start,
		generic_vh_stop,
		zx_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		/*TODO*/////new MachineSound[] {
				/* MachineSound */
		/*TODO*/////		new MachineSound(
		/*TODO*/////				SOUND_DAC,
		/*TODO*/////				dac_interface
		/*TODO*/////		)
		/*TODO*/////	}
	                null
		);
	
	static MachineDriver machine_driver_ts1000  = new MachineDriver
			(
		/* basic machine hardware */
		new MachineCPU[] {
			/* MachineCPU */
				/* basic machine hardware */
				new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				CPU_CLOCK,
				readmem_zx81, writemem_zx81, readport, writeport,
				ignore_interrupt, 1
			)
		},
		new Float(1.0 * CPU_CLOCK / 262 / CYCLES_PER_SCANLINE),
		0,								/* vblank handled by vidhrdw */
		1,
		zx81_init_machine,
		zx_shutdown_machine,
	
		/* video hardware */
		32 * 8, 						/* screen width (inc. blank/sync) */
		262,							/* screen height (inc. blank/sync) */
		new rectangle(0*8, 32*8-1, 17, 262-15-1),	/* visible area (inc. some vertical overscan) */
		zx81_gfxdecodeinfo, 			/* graphics decode info */
		6, 4,							/* colors used for the characters */
		ts1000_init_palette,			/* init palette */
	
		VIDEO_TYPE_RASTER,
		null,
		generic_bitmapped_vh_start,
		generic_vh_stop,
		zx_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		/*TODO*/////new MachineSound[] {
		/* MachineSound */
		/*TODO*/////		new MachineSound(
		/*TODO*/////				SOUND_DAC,
		/*TODO*/////				dac_interface
		/*TODO*/////		)
		/*TODO*/////	}
		            null
		);
	
	static MachineDriver machine_driver_pc8300 = new MachineDriver (
				/* basic machine hardware */
				new MachineCPU[] {
				/* MachineCPU */
				/* basic machine hardware */
				new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				CPU_CLOCK,
				readmem_pc8300, writemem_pc8300, readport, writeport,
				ignore_interrupt, 1
					)
		},
		new Float(1.0 * CPU_CLOCK / 262 / CYCLES_PER_SCANLINE),
		0,								/* vblank handled by vidhrdw */
		1,
		pc8300_init_machine,
		zx_shutdown_machine,
	
		/* video hardware */
		32 * 8, 						/* screen width (inc. blank/sync) */
		262,							/* screen height (inc. blank/sync) */
		new rectangle(0*8, 32*8-1, 17, 262-15-1),	/* visible area (inc. some vertical overscan) */
		pc8300_gfxdecodeinfo,			/* graphics decode info */
		6, 4,							/* colors used for the characters */
		zx81_init_palette,				/* init palette */
	
		VIDEO_TYPE_RASTER,
		null,
		generic_bitmapped_vh_start,
		generic_vh_stop,
		zx_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		/*TODO*/////new MachineSound[] {
		/* MachineSound */
		/*TODO*/////		new MachineSound(
		/*TODO*/////				SOUND_DAC,
		/*TODO*/////				dac_interface
		/*TODO*/////		)
		/*TODO*/////	}
		            null
		);
	
	static MachineDriver machine_driver_pow3000 = new MachineDriver (
			/* basic machine hardware */
			new MachineCPU[] {
			/* MachineCPU */
			/* basic machine hardware */
			new MachineCPU(
				CPU_Z80 | CPU_16BIT_PORT,
				CPU_CLOCK,
				readmem_pow3000, writemem_pow3000, readport_pow3000, writeport_pow3000,
				ignore_interrupt, 1
			)
		},
		new Float(1.0 * CPU_CLOCK / 262 / CYCLES_PER_SCANLINE), 0,
		/* vblank handled by vidhrdw */
		1,
		pow3000_init_machine,
		zx_shutdown_machine,
	
		/* video hardware */
		32 * 8, 						/* screen width (inc. blank/sync) */
		262,							/* screen height (inc. blank/sync) */
		new rectangle(0*8, 32*8-1, 17, 262-15-1),	/* visible area (inc. some vertical overscan) */
		pow3000_gfxdecodeinfo,			/* graphics decode info */
		6, 4,							/* colors used for the characters */
		zx81_init_palette,				/* init palette */
	
		VIDEO_TYPE_RASTER,
		null,
		generic_bitmapped_vh_start,
		generic_vh_stop,
		zx_vh_screenrefresh,
	
		/* sound hardware */
		0, 0, 0, 0,
		/*TODO*/////new MachineSound[] {
		/* MachineSound */
		/*TODO*/////		new MachineSound(
		/*TODO*/////				SOUND_DAC,
		/*TODO*/////				dac_interface
		/*TODO*/////		)
		/*TODO*/////	}
		            null
		);
	
	static RomLoadPtr rom_zx80 = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("zx80.rom",    0x0000, 0x1000, 0x4c7fc597);
	    ROM_REGION(0x00100, REGION_GFX1);
		/* filled in by zx_init_driver */
	ROM_END(); }};
	
	
	static RomLoadPtr rom_aszmic = new RomLoadPtr(){ public void handler(){	
	    ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("aszmic.rom",  0x0000, 0x1000, 0x6c123536);
	    ROM_REGION(0x00100, REGION_GFX1);
	    /* filled in by zx_init_driver */
	ROM_END(); }};
	
	    static RomLoadPtr rom_zx81 = new RomLoadPtr(){ public void handler(){
		ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("zx81.rom",    0x0000, 0x2000, 0x4b1dd6eb);
		ROM_REGION(0x00100, REGION_GFX1);
		/* filled in by zx_init_driver */
	ROM_END(); }};
	
	static RomLoadPtr rom_ts1000 = new RomLoadPtr(){ public void handler(){	
		ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("zx81a.rom",  0x0000, 0x2000, 0x4b1dd6eb);
		ROM_REGION(0x00100, REGION_GFX1);
		/* filled in by zx_init_driver */
	ROM_END(); }};
	
	static RomLoadPtr rom_pc8300 = new RomLoadPtr(){ public void handler(){
		ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("8300_org.rom",0x0000, 0x2000, 0xa350f2b1);
		ROM_REGION(0x00100, REGION_GFX1);
		/* filled in by zx_init_driver */
		ROM_REGION(0x00200, REGION_GFX2);
		ROM_LOAD("8300_fnt.bin",0x0000, 0x0200, 0x6bd0408c);
	ROM_END(); }};
	
	static RomLoadPtr rom_pow3000 = new RomLoadPtr(){ public void handler(){
		ROM_REGION(0x10000, REGION_CPU1);
		ROM_LOAD("pow3000.rom", 0x0000, 0x2000, 0x8a49b2c3);
		ROM_REGION(0x00100, REGION_GFX1);
		/* filled in by zx_init_driver */
		ROM_REGION(0x00200, REGION_GFX2);
		ROM_LOAD("pow3000.chr", 0x0000, 0x0200, 0x1c42fe46);
	ROM_END(); }};
	
	public static IODevice io_zx80[] = {
            new IODevice(
	
			IO_CASSETTE,		/* type */
			1,					/* count */
			"80\0o\0",          /* file extensions */
			IO_RESET_ALL,		/* reset if file changed */
	        null,               /* id */
			zx_cassette_init,	/* init */
			zx_cassette_exit,	/* exit */
			null,				/* info */
			null,				/* open */
			null,				/* close */
			null,				/* status */
			null,				/* seek */
			null,				/* tell */
			null,				/* input */
			null,				/* output */
			null,				/* input_chunk */
			null				/* output_chunk */
		
		),
        new IODevice(IO_END)
	};
	
	public static IODevice io_zx81[] = {
			new IODevice(
			IO_CASSETTE,		/* type */
			1,					/* count */
			"81\0p\0",          /* file extensions */
			IO_RESET_ALL,		/* reset if file changed */
	        null,               /* id */
			zx_cassette_init,	/* init */
			zx_cassette_exit,	/* exit */
			null,				/* info */
			null,				/* open */
			null,				/* close */
			null,				/* status */
			null,				/* seek */
			null,				/* tell */
			null,				/* input */
			null,				/* output */
			null,				/* input_chunk */
			null				/* output_chunk */
		),
		new IODevice(IO_END)
	};
	
	public static IODevice io_aszmic[] = io_zx80;
	public static IODevice io_ts1000[] = io_zx81;
	public static IODevice io_pc8300[] = io_zx81;
	public static IODevice io_pow3000[] = io_zx81;
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	/*	   YEAR  NAME	   PARENT	 MACHINE   INPUT	 INIT	   COMPANY				FULLNAME */
	//COMPX (1980,   zx80,    0,		 zx80,	   zx80,	 zx,	   "Sinclair Research", "ZX-80", GAME_NOT_WORKING)
	public static GameDriver driver_zx80 = new GameDriver("1980", "zx80", "zx.java", rom_zx80, null, machine_driver_zx80, input_ports_zx80, null, io_zx80, "Sinclair Research", "ZX-80");
	//COMPX (1981,   aszmic,  zx80,	 zx80,	   zx80,	 zx,	   "Sinclair Research", "ZX.Aszmic", GAME_NOT_WORKING)
	public static GameDriver driver_aszmic = new GameDriver("1981", "aszmic", "zx.java", rom_aszmic, null, machine_driver_zx80, input_ports_zx80, null, io_aszmic, "Sinclair Research", "ZX.Aszmic");
	//COMPX (1981,   zx81,    0,		 zx81,	   zx81,	 zx,	   "Sinclair Research", "ZX-81", GAME_NOT_WORKING)
	public static GameDriver driver_zx81 = new GameDriver("1981", "zx81", "zx.java", rom_zx81, null, machine_driver_zx81, input_ports_zx81, null, io_zx81, "Sinclair Research", "ZX-81");
	//COMPX (1981,   ts1000,  zx81,	 ts1000,   zx81,	 zx,	   "Timex Sinclair",    "TS1000", GAME_NOT_WORKING | GAME_ALIAS)
	public static GameDriver driver_ts1000 = new GameDriver("1981", "ts1000", "zx.java", rom_ts1000, null, machine_driver_ts1000, input_ports_zx81, null, io_ts1000, "Timex Sinclair", "TS1000");
	//COMPX (198?,   pc8300,  zx81,	 pc8300,   zx81,	 zx,	   "Your Computer",     "PC8300", GAME_NOT_WORKING)
	public static GameDriver driver_pc8300 = new GameDriver("198?", "pc8300", "zx.java", rom_pc8300, null, machine_driver_pc8300, input_ports_zx81, null, io_pc8300, "Your Computer", "PC8300");
	//COMPX (198?,   pow3000, zx81,	 pow3000,  pow3000,  zx,	   "Creon Enterprises", "Power 3000", GAME_NOT_WORKING)
	public static GameDriver driver_pow3000 = new GameDriver("198?", "pow3000", "zx.java", rom_pow3000, null, machine_driver_pow3000, input_ports_pow3000, null, io_pow3000, "Creon Enterprises", "Power 3000");
	
}