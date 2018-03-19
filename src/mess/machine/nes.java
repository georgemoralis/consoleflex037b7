/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.mame.mame.Machine;
import static consoleflex.funcPtr.*;
import static mess.messH.*;
import static mess.mess.*;
import static old.mame.inptport.*;
import static mess.vidhrdw.tms9928a.*;
import static old.arcadeflex.osdepend.*;
import static mess.osdepend.fileio.*;
import static arcadeflex.libc.cstring.*;
import static WIP.mame.osdependH.*;
import static mame.commonH.*;
import static old.mame.common.*;
import static WIP.arcadeflex.libc.memcpy.*;
import static WIP.mame.memoryH.*;
import static old.arcadeflex.libc_old.SEEK_SET;
import static old.arcadeflex.libc_old.printf;
import static WIP.arcadeflex.libc.memcpy.*;
import static old.arcadeflex.libc_old.strcmp;
import static mess.includes.nesH.*;
import static mess.systems.nes.*;
import static WIP.mame.memory.*;
import static mess.machine.nes_mmc.*;

public class nes {

    /*TODO*///	
/*TODO*///	/* Uncomment this to dump reams of ppu state info to the errorlog */
/*TODO*///	//#define LOG_PPU
/*TODO*///	
/*TODO*///	/* Uncomment this to dump info about the inputs to the errorlog */
/*TODO*///	//#define LOG_JOY
/*TODO*///	
/*TODO*///	/* Uncomment this to generate prg chunk files when the cart is loaded */
/*TODO*///	//#define SPLIT_PRG
/*TODO*///	
    public static final int BATTERY_SIZE =0x2000;
    static String battery_name = "";
    public static char[] battery_data=new char[BATTERY_SIZE];
/*TODO*///	
/*TODO*///	void nes_vh_renderscanline (int scanline);
/*TODO*///	
/*TODO*///	struct ppu_struct ppu;
    public static nes_struct _nes = new nes_struct();
    /*TODO*///	struct fds_struct nes_fds;
/*TODO*///	
    static int ppu_scanlines_per_frame;
    /*TODO*///	
/*TODO*///	UINT8 *ppu_page[4];
/*TODO*///	
/*TODO*///	int current_scanline;
    static char[] use_vram = new char[512];
    /*TODO*///	
/*TODO*///	/* PPU Variables */
/*TODO*///	int PPU_Control0;		// $2000
/*TODO*///	int PPU_Control1;		// $2001
/*TODO*///	int PPU_Status;			// $2002
/*TODO*///	int PPU_Sprite_Addr;	// $2003
/*TODO*///	
/*TODO*///	UINT8 PPU_X_fine;
/*TODO*///	
/*TODO*///	UINT16 PPU_address;		// $2006
/*TODO*///	UINT8 PPU_address_latch;
/*TODO*///	UINT16 PPU_refresh_data;
/*TODO*///	UINT16 PPU_refresh_latch;
/*TODO*///	
/*TODO*///	int PPU_tile_page;
/*TODO*///	int PPU_sprite_page;
/*TODO*///	int PPU_background_color;
/*TODO*///	int PPU_add;
/*TODO*///	
/*TODO*///	static UINT8 PPU_data_latch;
/*TODO*///	
/*TODO*///	static char	PPU_toggle;
/*TODO*///	
    static int[]/*UINT32*/ in_0 = new int[3];
    static int[]/*UINT32*/ in_1 = new int[3];
    static int/*UINT32*/ in_0_shift;
    static int/*UINT32*/ in_1_shift;

    /*TODO*///	void nes_ppu_w (int offset, int data);
/*TODO*///	
/*TODO*///	/* local prototypes */
/*TODO*///	static void ppu_reset (struct ppu_struct *ppu_);
    public static InitDriverPtr init_nes_core = new InitDriverPtr() {
        public void handler() {
            //throw new UnsupportedOperationException("Not supported yet.");
            		/* We set these here in case they weren't set in the cart loader */
		_nes.rom = memory_region(REGION_CPU1);
		_nes.vrom = memory_region(REGION_GFX1);
		_nes.vram = memory_region(REGION_GFX2);
		_nes.wram = memory_region(REGION_USER1);
	
		battery_ram = _nes.wram;
	
/*TODO*///		/* Set up the memory handlers for the mapper */
/*TODO*///		switch (nes.mapper)
/*TODO*///		{
/*TODO*///			case 20:
/*TODO*///				nes.slow_banking = 0;
/*TODO*///				install_mem_read_handler(0, 0x4030, 0x403f, fds_r);
/*TODO*///				install_mem_read_handler(0, 0x6000, 0xdfff, MRA_RAM);
/*TODO*///				install_mem_read_handler(0, 0xe000, 0xffff, MRA_ROM);
/*TODO*///	
/*TODO*///				install_mem_write_handler(0, 0x4020, 0x402f, fds_w);
/*TODO*///				install_mem_write_handler(0, 0x6000, 0xdfff, MWA_RAM);
/*TODO*///				install_mem_write_handler(0, 0xe000, 0xffff, MWA_ROM);
/*TODO*///				break;
/*TODO*///			case 40:
/*TODO*///				nes.slow_banking = 1;
/*TODO*///				/* Game runs code in between banks, so we do things different */
/*TODO*///				install_mem_read_handler(0, 0x6000, 0x7fff, MRA_RAM);
/*TODO*///				install_mem_read_handler(0, 0x8000, 0xffff, MRA_ROM);
/*TODO*///	
/*TODO*///				install_mem_write_handler(0, 0x6000, 0x7fff, nes_mid_mapper_w);
/*TODO*///				install_mem_write_handler(0, 0x8000, 0xffff, nes_mapper_w);
/*TODO*///				break;
/*TODO*///			default:
				_nes.u8_slow_banking = 0;
				install_mem_read_handler(0, 0x6000, 0x7fff, MRA_BANK5);
				install_mem_read_handler(0, 0x8000, 0x9fff, MRA_BANK1);
				install_mem_read_handler(0, 0xa000, 0xbfff, MRA_BANK2);
				install_mem_read_handler(0, 0xc000, 0xdfff, MRA_BANK3);
				install_mem_read_handler(0, 0xe000, 0xffff, MRA_BANK4);
				cpu_setbankhandler_r (1, MRA_BANK1);
				cpu_setbankhandler_r (2, MRA_BANK2);
				cpu_setbankhandler_r (3, MRA_BANK3);
				cpu_setbankhandler_r (4, MRA_BANK4);
				cpu_setbankhandler_r (5, MRA_BANK5);
	
				install_mem_write_handler(0, 0x6000, 0x7fff, nes_mid_mapper_w);
				install_mem_write_handler(0, 0x8000, 0xffff, nes_mapper_w);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Set up the mapper callbacks */
/*TODO*///		{
/*TODO*///			int i = 0;
/*TODO*///	
/*TODO*///			while (mmc_list[i].iNesMapper != -1)
/*TODO*///			{
/*TODO*///				if (mmc_list[i].iNesMapper == nes.mapper)
/*TODO*///				{
/*TODO*///					mmc_write_low = mmc_list[i].mmc_write_low;
/*TODO*///					mmc_read_low = mmc_list[i].mmc_read_low;
/*TODO*///					mmc_write_mid = mmc_list[i].mmc_write_mid;
/*TODO*///					mmc_write = mmc_list[i].mmc_write;
/*TODO*///					ppu_latch = mmc_list[i].ppu_latch;
/*TODO*///					mmc_irq = mmc_list[i].mmc_irq;
/*TODO*///					break;
/*TODO*///				}
/*TODO*///				i ++;
/*TODO*///			}
/*TODO*///			if (mmc_list[i].iNesMapper == -1)
/*TODO*///			{
/*TODO*///				printf ("Mapper %d is not yet supported, defaulting to no mapper.\n",nes.mapper);
/*TODO*///				mmc_write_low = mmc_write_mid = mmc_write = NULL;
/*TODO*///				mmc_read_low = NULL;
/*TODO*///				ppu_latch = NULL;
/*TODO*///				mmc_irq = NULL;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
		/* Load a battery file, but only if there's no trainer since they share */
		/* overlapping memory. */
		if (_nes.u8_trainer!=0) return;
	
		/* We need this because battery ram is loaded before the */
		/* memory subsystem is set up. When this routine is called */
		/* everything is ready, so we can just copy over the data */
		/* we loaded before. */
		memcpy (battery_ram, battery_data, BATTERY_SIZE);
        }
    };

    public static InitDriverPtr init_nes = new InitDriverPtr() {
        public void handler() {
            ppu_scanlines_per_frame = NTSC_SCANLINES_PER_FRAME;
            init_nes_core.handler();
        }
    };

    public static InitDriverPtr init_nespal = new InitDriverPtr() {
        public void handler() {
            ppu_scanlines_per_frame = PAL_SCANLINES_PER_FRAME;
            init_nes_core.handler();
        }
    };

    public static InitMachinePtr nes_init_machine = new InitMachinePtr() {
        public void handler() {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		current_scanline = 0;
/*TODO*///	
/*TODO*///		ppu_reset (&ppu);
/*TODO*///	
/*TODO*///		/* Some carts have extra RAM and require it on at startup, e.g. Metroid */
/*TODO*///		nes.mid_ram_enable = 1;
/*TODO*///	
/*TODO*///		/* Reset the mapper variables. Will also mark the char-gen ram as dirty */
/*TODO*///		mapper_reset (nes.mapper);
/*TODO*///	
/*TODO*///		/* Reset the serial input ports */
/*TODO*///		in_0_shift = 0;
/*TODO*///		in_1_shift = 0;
        }
    };
    public static StopMachinePtr nes_stop_machine = new StopMachinePtr() {
        public void handler() {

            /* Write out the battery file if necessary */
            if (_nes.u8_battery != 0) {
                throw new UnsupportedOperationException("Not supported yet.");
                /*TODO*///			void *f;
/*TODO*///	
/*TODO*///			f = osd_fopen(battery_name,0,OSD_FILETYPE_NVRAM,1);
/*TODO*///			if (f != 0)
/*TODO*///			{
/*TODO*///				osd_fwrite(f,battery_ram,BATTERY_SIZE);
/*TODO*///				osd_fclose (f);
/*TODO*///			}
            }
        }
    };
    /*TODO*///	
/*TODO*///	void ppu_reset (struct ppu_struct *_ppu)
/*TODO*///	{
/*TODO*///		/* Reset PPU variables */
/*TODO*///		PPU_Control0 = PPU_Control1 = PPU_Status = 0;
/*TODO*///		PPU_address_latch = 0;
/*TODO*///		PPU_data_latch = 0;
/*TODO*///		PPU_address = PPU_Sprite_Addr = 0;
/*TODO*///		PPU_tile_page = PPU_sprite_page = PPU_background_color = 0;
/*TODO*///	
/*TODO*///		PPU_add = 1;
/*TODO*///		PPU_background_color = 0;
/*TODO*///		PPU_toggle = 0;
/*TODO*///	
/*TODO*///		/* Reset mirroring */
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		if (1 != 0)
/*TODO*///	#else
/*TODO*///		if (nes.four_screen_vram)
/*TODO*///	#endif
/*TODO*///		{
/*TODO*///			ppu_page[0] = &(videoram.read(0x2000));
/*TODO*///			ppu_page[1] = &(videoram.read(0x2400));
/*TODO*///			ppu_page[2] = &(videoram.read(0x2800));
/*TODO*///			ppu_page[3] = &(videoram.read(0x2c00));
/*TODO*///		}
/*TODO*///		else switch (nes.hard_mirroring)
/*TODO*///		{
/*TODO*///			case 0: ppu_mirror_h(); break;
/*TODO*///			case 1: ppu_mirror_v(); break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
    public static ReadHandlerPtr nes_IN0_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int dip;
            int retVal;

            /* Some games expect bit 6 to be set because the last entry on the data bus shows up */
 /* in the unused upper 3 bits, so typically a read from $4016 leaves 0x40 there. */
            retVal = 0x40;

            retVal |= ((in_0[0] >> in_0_shift) & 0x01);

            /* Check the fake dip to see what's connected */
            dip = readinputport(2);

            switch (dip & 0x0f) {
                case 0x01: /* zapper */ {
                    char pix;
                    retVal |= 0x08;
                    /* no sprite hit */

 /* If button 1 is pressed, indicate the light gun trigger is pressed */
                    retVal |= ((in_0[0] & 0x01) << 4);

                    /* Look at the screen and see if the cursor is over a bright pixel */
                    pix = Machine.scrbitmap.line[in_0[2]].read(in_0[1]);
                    if ((pix == Machine.pens[0x20]) || (pix == Machine.pens[0x30])
                            || (pix == Machine.pens[0x33]) || (pix == Machine.pens[0x34])) {
                        retVal &= ~0x08;
                        /* sprite hit */
                    }
                }
                break;
                case 0x02:
                    /* multitap */
 /* Handle data line 1's serial output */
                    //			retVal |= ((in_0[1] >> in_0_shift) & 0x01) << 1;
                    break;
            }

            in_0_shift++;
            return retVal;
        }
    };

    public static ReadHandlerPtr nes_IN1_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int dip;
            int retVal;

            /* Some games expect bit 6 to be set because the last entry on the data bus shows up */
 /* in the unused upper 3 bits, so typically a read from $4017 leaves 0x40 there. */
            retVal = 0x40;

            /* Handle data line 0's serial output */
            retVal |= ((in_1[0] >> in_1_shift) & 0x01);

            /* Check the fake dip to see what's connected */
            dip = readinputport(2);

            switch (dip & 0xf0) {
                case 0x10: /* zapper */ {
                    char pix;
                    retVal |= 0x08;
                    /* no sprite hit */

 /* If button 1 is pressed, indicate the light gun trigger is pressed */
                    retVal |= ((in_1[0] & 0x01) << 4);

                    /* Look at the screen and see if the cursor is over a bright pixel */
                    pix = Machine.scrbitmap.line[in_1[2]].read(in_1[1]);
                    if ((pix == Machine.pens[0x20]) || (pix == Machine.pens[0x30])
                            || (pix == Machine.pens[0x33]) || (pix == Machine.pens[0x34])) {
                        retVal &= ~0x08;
                        /* sprite hit */
                    }
                }
                break;
                case 0x20:
                    /* multitap */
 /* Handle data line 1's serial output */
                    //			retVal |= ((in_1[1] >> in_1_shift) & 0x01) << 1;
                    break;
                case 0x30:
                    /* arkanoid dial */
 /* Handle data line 2's serial output */
                    retVal |= ((in_1[2] >> in_1_shift) & 0x01) << 3;

                    /* Handle data line 3's serial output - bits are reversed */
                    //			retVal |= ((in_1[3] >> in_1_shift) & 0x01) << 4;
                    retVal |= ((in_1[3] << in_1_shift) & 0x80) >> 3;
                    break;
            }

            in_1_shift++;
            return retVal;
        }
    };

    public static WriteHandlerPtr nes_IN0_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int dip;

            if ((data & 0x01) != 0) {
                return;
            }

            /* Toggling bit 0 high then low resets both controllers */
            in_0_shift = 0;
            in_1_shift = 0;

            in_0[0] = readinputport(0);

            /* Check the fake dip to see what's connected */
            dip = readinputport(2);

            switch (dip & 0x0f) {
                case 0x01:
                    /* zapper */
                    in_0[1] = readinputport(3);
                    /* x-axis */
                    in_0[2] = readinputport(4);
                    /* y-axis */
                    break;

                case 0x02:
                    /* multitap */
                    in_0[0] |= (readinputport(8) << 8);
                    in_0[0] |= (0x08 << 16);
                    /* OR in the 4-player adapter id, channel 0 */

 /* Optional: copy the data onto the second channel */
                    //			in_0[1] = in_0[0];
                    //			in_0[1] |= (0x04 << 16); /* OR in the 4-player adapter id, channel 1 */
                    break;
            }

            in_1[0] = readinputport(1);

            switch (dip & 0xf0) {
                case 0x10:
                    /* zapper */
                    if ((dip & 0x01) != 0) {
                        /* zapper is also on port 1, use 2nd player analog inputs */
                        in_1[1] = readinputport(5);
                        /* x-axis */
                        in_1[2] = readinputport(6);
                        /* y-axis */
                    } else {
                        in_1[1] = readinputport(3);
                        /* x-axis */
                        in_1[2] = readinputport(4);
                        /* y-axis */
                    }
                    break;

                case 0x20:
                    /* multitap */
                    in_1[0] |= (readinputport(9) << 8);
                    in_1[0] |= (0x04 << 16);
                    /* OR in the 4-player adapter id, channel 0 */

 /* Optional: copy the data onto the second channel */
                    //			in_1[1] = in_1[0];
                    //			in_1[1] |= (0x08 << 16); /* OR in the 4-player adapter id, channel 1 */
                    break;

                case 0x30:
                    /* arkanoid dial */
                    in_1[3] = (((readinputport(10) & 0xFF) + 0x52) ^ 0xff) & 0xFF;
                    //			in_1[3] = readinputport (10) ^ 0xff;
                    //			in_1[3] = 0x02;

                    /* Copy the joypad data onto the third channel */
                    in_1[2] = in_1[0] /*& 0x01*/;
                    break;
            }
        }
    };

    public static WriteHandlerPtr nes_IN1_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

        }
    };

    public static InterruptPtr nes_interrupt = new InterruptPtr() {
        public int handler() {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		static int vblank_started = 0;
/*TODO*///		int ret;
/*TODO*///	
/*TODO*///		ret = M6502_INT_NONE;
/*TODO*///	
/*TODO*///		/* See if a mapper generated an irq */
/*TODO*///	    if (*mmc_irq != NULL) ret = (*mmc_irq)(current_scanline);
/*TODO*///	
/*TODO*///		if (current_scanline <= BOTTOM_VISIBLE_SCANLINE)
/*TODO*///		{
/*TODO*///			/* If background or sprites are enabled, copy the ppu address latch */
/*TODO*///			if ((PPU_Control1 & 0x18) != 0)
/*TODO*///			{
/*TODO*///				/* Copy only the scroll x-coarse and the x-overflow bit */
/*TODO*///				PPU_refresh_data &= ~0x041f;
/*TODO*///				PPU_refresh_data |= (PPU_refresh_latch & 0x041f);
/*TODO*///			}
/*TODO*///	
/*TODO*///	#ifdef NEW_SPRITE_HIT
/*TODO*///			/* If we're not rendering this frame, fake the sprite hit */
/*TODO*///			if (osd_skip_this_frame())
/*TODO*///	#endif
/*TODO*///				if ((current_scanline == spriteram.read(0)+ 7) && (PPU_Control1 & 0x10))
/*TODO*///				{
/*TODO*///					PPU_Status |= PPU_status_sprite0_hit;
/*TODO*///	#ifdef LOG_PPU
/*TODO*///					logerror ("Sprite 0 hit, scanline: %d\n", current_scanline);
/*TODO*///	#endif
/*TODO*///				}
/*TODO*///	
/*TODO*///			/* Render this scanline if appropriate */
/*TODO*///			if ((PPU_Control1 & 0x18) /*&& !osd_skip_this_frame()*/)
/*TODO*///			{
/*TODO*///				nes_vh_renderscanline (current_scanline);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Has the vblank started? */
/*TODO*///		else if (current_scanline == BOTTOM_VISIBLE_SCANLINE+1)
/*TODO*///		{
/*TODO*///	   		logerror("** Vblank started\n");
/*TODO*///	
/*TODO*///			/* Note: never reset the toggle to the scroll/address latches on vblank */
/*TODO*///	
/*TODO*///			/* VBlank in progress, set flag */
/*TODO*///			PPU_Status |= PPU_status_vblank;
/*TODO*///		}
/*TODO*///	
/*TODO*///		else if (current_scanline == NMI_SCANLINE)
/*TODO*///		{
/*TODO*///			/* Check if NMIs are enabled on vblank */
/*TODO*///			if ((PPU_Control0 & PPU_c0_NMI) != 0) ret = M6502_INT_NMI;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Increment the scanline pointer & check to see if it's rolled */
/*TODO*///		if ( ++ current_scanline == ppu_scanlines_per_frame)
/*TODO*///		{
/*TODO*///			/* vblank is over, start at top of screen again */
/*TODO*///			current_scanline = 0;
/*TODO*///			vblank_started = 0;
/*TODO*///	
/*TODO*///			/* Clear the vblank & sprite hit flag */
/*TODO*///			PPU_Status &= ~(PPU_status_vblank | PPU_status_sprite0_hit);
/*TODO*///	
/*TODO*///			/* If background or sprites are enabled, copy the ppu address latch */
/*TODO*///			if ((PPU_Control1 & 0x18) != 0)
/*TODO*///				PPU_refresh_data = PPU_refresh_latch;
/*TODO*///	
/*TODO*///	//if ((PPU_refresh_data & 0x400) != 0) Debugger ();
/*TODO*///	
/*TODO*///	   		logerror("** New frame\n");
/*TODO*///	
/*TODO*///			/* TODO: verify - this code assumes games with chr chunks won't generate chars on the fly */
/*TODO*///			/* Pinbot seems to use both VROM and VRAM */
/*TODO*///			if ((nes.chr_chunks == 0) || (nes.mapper == 119))
/*TODO*///			{
/*TODO*///				int i;
/*TODO*///	
/*TODO*///				/* Decode any dirty characters */
/*TODO*///				for (i = 0; i < 0x200; i ++)
/*TODO*///					if (dirtychar[i])
/*TODO*///					{
/*TODO*///						decodechar(Machine.gfx[1], i, nes.vram, Machine.drv.gfxdecodeinfo[1].gfxlayout);
/*TODO*///						dirtychar[i] = 0;
/*TODO*///						use_vram[i] = 1;
/*TODO*///					}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		if ((ret != M6502_INT_NONE))
/*TODO*///		{
/*TODO*///	    	logerror("--- scanline %d", current_scanline);
/*TODO*///	    	if (ret == M6502_INT_IRQ)
/*TODO*///	    		logerror(" IRQ\n");
/*TODO*///	    	else logerror(" NMI\n");
/*TODO*///	    }
/*TODO*///	
/*TODO*///		return ret;
        }
    };

    public static ReadHandlerPtr nes_ppu_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		UINT8 retVal=0;
/*TODO*///	/*
/*TODO*///	    |  $2002  | PPU Status Register (R)                                  |
/*TODO*///	    |         |   %vhs-----                                              |
/*TODO*///	    |         |               v = VBlank Occurance                       |
/*TODO*///	    |         |                      1 = In VBlank                       |
/*TODO*///	    |         |               h = Sprite #0 Occurance                    |
/*TODO*///	    |         |                      1 = VBlank has hit Sprite #0        |
/*TODO*///	    |         |               s = Scanline Sprite Count                  |
/*TODO*///	    |         |                      0 = 8 or less sprites on the        |
/*TODO*///	    |         |                          current scanline                |
/*TODO*///	    |         |                      1 = More than 8 sprites on the      |
/*TODO*///	    |         |                          current scanline                |
/*TODO*///	*/
/*TODO*///		switch (offset & 0x07)
/*TODO*///		{
/*TODO*///			case 0: case 1: case 3: case 5: case 6:
/*TODO*///				retVal = 0x00;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 2:
/*TODO*///				retVal = PPU_Status;
/*TODO*///	
/*TODO*///				/* This is necessary: see W&W1, Gi Joe Atlantis */
/*TODO*///				PPU_toggle = 0;
/*TODO*///	
/*TODO*///				/* Note that we don't clear the vblank flag - this is correct. */
/*TODO*///				/* Many games would break if we did: Dragon Warrior 3, GI Joe Atlantis */
/*TODO*///				/* are two. */
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 4:
/*TODO*///				retVal = spriteram.read(PPU_Sprite_Addr);
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	//	logerror("PPU read (%02x), data: %02x, pc: %04x\n", offset, retVal, cpu_get_pc ());
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 7:
/*TODO*///				retVal = PPU_data_latch;
/*TODO*///	
/*TODO*///	            if (*ppu_latch != NULL) (*ppu_latch)(PPU_address & 0x3fff);
/*TODO*///	
/*TODO*///				if ((PPU_address >= 0x2000) && (PPU_address <= 0x3fef))
/*TODO*///					PPU_data_latch = ppu_page[(PPU_address & 0xc00) >> 10][PPU_address & 0x3ff];
/*TODO*///				else
/*TODO*///					PPU_data_latch = videoram.read(PPU_address & 0x3fff);
/*TODO*///	
/*TODO*///				/* TODO: this is a bit of a hack, needed to get Argus, ASO, etc to work */
/*TODO*///				/* but, B-Wings, submath (j) seem to use this location differently... */
/*TODO*///				if (nes.chr_chunks && (PPU_address & 0x3fff) < 0x2000)
/*TODO*///				{
/*TODO*///					int vrom_loc;
/*TODO*///	
/*TODO*///					vrom_loc = (nes_vram[(PPU_address & 0x1fff) >> 10] * 16) + (PPU_address & 0x3ff);
/*TODO*///					PPU_data_latch = nes.vrom [vrom_loc];
/*TODO*///				}
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror("PPU read (%02x), data: %02x, ppu_addr: %04x, pc: %04x\n", offset, retVal, PPU_address, cpu_get_pc ());
/*TODO*///	#endif
/*TODO*///				PPU_address += PPU_add;
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return retVal;
        }
    };

    public static WriteHandlerPtr nes_ppu_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///	
/*TODO*///		switch (offset & 0x07)
/*TODO*///		{
/*TODO*///	/*
/*TODO*///	    |  $2000  | PPU Control Register #1 (W)                              |
/*TODO*///	    |         |   %vMsbpiNN                                              |
/*TODO*///	    |         |               v = Execute NMI on VBlank                  |
/*TODO*///	    |         |                      1 = Enabled                         |
/*TODO*///	    |         |               M = PPU Selection (unused)                 |
/*TODO*///	    |         |                      0 = Master                          |
/*TODO*///	    |         |                      1 = Slave                           |
/*TODO*///	    |         |               s = Sprite Size                            |
/*TODO*///	    |         |                      0 = 8x8                             |
/*TODO*///	    |         |                      1 = 8x16                            |
/*TODO*///	    |         |               b = Background Pattern Table Address       |
/*TODO*///	    |         |                      0 = $0000 (VRAM)                    |
/*TODO*///	    |         |                      1 = $1000 (VRAM)                    |
/*TODO*///	    |         |               p = Sprite Pattern Table Address           |
/*TODO*///	    |         |                      0 = $0000 (VRAM)                    |
/*TODO*///	    |         |                      1 = $1000 (VRAM)                    |
/*TODO*///	    |         |               i = PPU Address Increment                  |
/*TODO*///	    |         |                      0 = Increment by 1                  |
/*TODO*///	    |         |                      1 = Increment by 32                 |
/*TODO*///	    |         |              NN = Name Table Address                     |
/*TODO*///	    |         |                     00 = $2000 (VRAM)                    |
/*TODO*///	    |         |                     01 = $2400 (VRAM)                    |
/*TODO*///	    |         |                     10 = $2800 (VRAM)                    |
/*TODO*///	    |         |                     11 = $2C00 (VRAM)                    |
/*TODO*///	    |         |                                                          |
/*TODO*///	    |         | NOTE: Bit #6 (M) has no use, as there is only one (1)    |
/*TODO*///	    |         |       PPU installed in all forms of the NES and Famicom. |
/*TODO*///	*/
/*TODO*///			case 0: /* PPU Control 0 */
/*TODO*///				PPU_Control0 = data;
/*TODO*///	
/*TODO*///				PPU_refresh_latch &= ~0x0c00;
/*TODO*///				PPU_refresh_latch |= (data & 0x03) << 10;
/*TODO*///	
/*TODO*///				/* The char ram bank points either 0x0000 or 0x1000 (page 0 or page 4) */
/*TODO*///				PPU_tile_page = (PPU_Control0 & PPU_c0_chr_select) >> 2;
/*TODO*///				PPU_sprite_page = (PPU_Control0 & PPU_c0_spr_select) >> 1;
/*TODO*///	
/*TODO*///				if ((PPU_Control0 & PPU_c0_inc) != 0)
/*TODO*///					PPU_add = 32;
/*TODO*///				else
/*TODO*///					PPU_add = 1;
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	#if 0
/*TODO*///				logerror("------ scanline: %d -------\n", current_scanline);
/*TODO*///				logerror("PPU_w Name table: %04x\n", (PPU_refresh_latch & 0xc00) | 0x2000);
/*TODO*///				logerror("PPU_w tile page: %04x\n", PPU_tile_page);
/*TODO*///				logerror("PPU_w sprite page: %04x\n", PPU_sprite_page);
/*TODO*///				logerror("---------------------------\n");
/*TODO*///	#endif
/*TODO*///				logerror("W PPU_Control0: %02x\n", PPU_Control0);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///	/*
/*TODO*///	    |  $2001  | PPU Control Register #2 (W)                              |
/*TODO*///	    |         |   %fffpcsit                                              |
/*TODO*///	    |         |             fff = Full Background Colour                 |
/*TODO*///	    |         |                    000 = Black                           |
/*TODO*///	    |         |                    001 = Red                             |
/*TODO*///	    |         |                    010 = Blue                            |
/*TODO*///	    |         |                    100 = Green                           |
/*TODO*///	    |         |               p = Sprite Visibility                      |
/*TODO*///	    |         |                      1 = Display                         |
/*TODO*///	    |         |               c = Background Visibility                  |
/*TODO*///	    |         |                      1 = Display                         |
/*TODO*///	    |         |               s = Sprite Clipping                        |
/*TODO*///	    |         |                      0 = Sprites not displayed in left   |
/*TODO*///	    |         |                          8-pixel column                  |
/*TODO*///	    |         |                      1 = No clipping                     |
/*TODO*///	    |         |               i = Background Clipping                    |
/*TODO*///	    |         |                      0 = Background not displayed in     |
/*TODO*///	    |         |                          left 8-pixel column             |
/*TODO*///	    |         |                      1 = No clipping                     |
/*TODO*///	    |         |               t = Display Type                           |
/*TODO*///	    |         |                      0 = Colour display                  |
/*TODO*///	    |         |                      1 = Mono-type (B&W) display         |
/*TODO*///	*/
/*TODO*///			case 1: /* PPU Control 1 */
/*TODO*///				/* If color intensity has changed, change all the pens */
/*TODO*///				if ((data & 0xe0) != (PPU_Control1 & 0xe0))
/*TODO*///				{
/*TODO*///	#ifdef COLOR_INTENSITY
/*TODO*///					int i;
/*TODO*///	
/*TODO*///					for (i = 0; i <= 0x1f; i ++)
/*TODO*///					{
/*TODO*///						UINT8 oldColor = videoram.read(i+0x3f00);
/*TODO*///	
/*TODO*///						Machine.gfx[0].colortable[i] = Machine.pens[oldColor + (data & 0xe0)*2];
/*TODO*///					}
/*TODO*///	#else
/*TODO*///	#if 0
/*TODO*///					int i;
/*TODO*///					double r_mod, g_mod, b_mod;
/*TODO*///	
/*TODO*///					switch ((data & 0xe0) >> 5)
/*TODO*///					{
/*TODO*///						case 0: r_mod = 1.0; g_mod = 1.0; b_mod = 1.0; break;
/*TODO*///						case 1: r_mod = 1.24; g_mod = .915; b_mod = .743; break;
/*TODO*///						case 2: r_mod = .794; g_mod = 1.09; b_mod = .882; break;
/*TODO*///						case 3: r_mod = .905; g_mod = 1.03; b_mod = 1.28; break;
/*TODO*///						case 4: r_mod = .741; g_mod = .987; b_mod = 1.0; break;
/*TODO*///						case 5: r_mod = 1.02; g_mod = .908; b_mod = .979; break;
/*TODO*///						case 6: r_mod = 1.02; g_mod = .98; b_mod = .653; break;
/*TODO*///						case 7: r_mod = .75; g_mod = .75; b_mod = .75; break;
/*TODO*///					}
/*TODO*///					for (i = 0; i < 64; i ++)
/*TODO*///						palette_change_color (i,
/*TODO*///							(double) nes_palette[3*i] * r_mod,
/*TODO*///							(double) nes_palette[3*i+1] * g_mod,
/*TODO*///							(double) nes_palette[3*i+2] * b_mod);
/*TODO*///	#endif
/*TODO*///	#endif
/*TODO*///				}
/*TODO*///				PPU_Control1 = data;
/*TODO*///	#ifdef LOG_PPU
/*TODO*///				logerror("W PPU_Control1: %02x\n", PPU_Control1);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///			case 2: /* PPU Status */
/*TODO*///	#ifdef LOG_PPU
/*TODO*///				logerror("W PPU_Status: %02x\n", data);
/*TODO*///	#endif
/*TODO*///				break;
/*TODO*///			case 3: /* PPU Sprite Memory Address */
/*TODO*///				PPU_Sprite_Addr = data;
/*TODO*///				break;
/*TODO*///			case 4: /* PPU Sprite Data */
/*TODO*///				spriteram.write(PPU_Sprite_Addr,data);
/*TODO*///				PPU_Sprite_Addr ++;
/*TODO*///				PPU_Sprite_Addr &= 0xff;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 5:
/*TODO*///				if (PPU_toggle != 0)
/*TODO*///				/* (second write) */
/*TODO*///				{
/*TODO*///					PPU_refresh_latch &= ~0x03e0;
/*TODO*///					PPU_refresh_latch |= (data & 0xf8) << 2;
/*TODO*///	
/*TODO*///					PPU_refresh_latch &= ~0x7000;
/*TODO*///					PPU_refresh_latch |= (data & 0x07) << 12;
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	logerror ("write ppu scroll latch (Y): %02x (scanline: %d) refresh_latch: %04x\n", data, current_scanline, PPU_refresh_latch);
/*TODO*///	#endif
/*TODO*///				}
/*TODO*///				/* (first write) */
/*TODO*///				else
/*TODO*///				{
/*TODO*///					PPU_refresh_latch &= ~0x1f;
/*TODO*///					PPU_refresh_latch |= (data & 0xf8) >> 3;
/*TODO*///	
/*TODO*///					PPU_X_fine = data & 0x07;
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	logerror ("write ppu scroll latch (X): %02x (scanline: %d) refresh_latch: %04x\n", data, current_scanline, PPU_refresh_latch);
/*TODO*///	#endif
/*TODO*///				}
/*TODO*///				PPU_toggle = !PPU_toggle;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 6: /* PPU Address Register */
/*TODO*///				/* PPU Memory Adress */
/*TODO*///				if (PPU_toggle != 0)
/*TODO*///				{
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	//if (current_scanline <= BOTTOM_VISIBLE_SCANLINE)
/*TODO*///		logerror ("write ppu address (low): %02x (%d)\n", data, current_scanline);
/*TODO*///	#endif
/*TODO*///					PPU_address = (PPU_address_latch << 8) | data;
/*TODO*///	
/*TODO*///					PPU_refresh_latch &= ~0x00ff;
/*TODO*///					PPU_refresh_latch |= data;
/*TODO*///					PPU_refresh_data = PPU_refresh_latch;
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///	#ifdef LOG_PPU
/*TODO*///	//if (current_scanline <= BOTTOM_VISIBLE_SCANLINE)
/*TODO*///		logerror ("write ppu address (high): %02x (%d)\n", data, current_scanline);
/*TODO*///	#endif
/*TODO*///					PPU_address_latch = data;
/*TODO*///	
/*TODO*///					if (data != 0x3f) /* TODO: remove this hack! */
/*TODO*///					{
/*TODO*///						PPU_refresh_latch &= ~0xff00;
/*TODO*///						PPU_refresh_latch |= (data & 0x3f) << 8;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				PPU_toggle = !PPU_toggle;
/*TODO*///				break;
/*TODO*///	
/*TODO*///			case 7: /* PPU I/O Register */
/*TODO*///	
/*TODO*///				if ((current_scanline <= BOTTOM_VISIBLE_SCANLINE) /*&& (PPU_Control1 & 0x18)*/)
/*TODO*///				{
/*TODO*///	//				logerror("*** PPU write during hblank (%d) ",  current_scanline);
/*TODO*///				}
/*TODO*///				Write_PPU (data);
/*TODO*///				break;
/*TODO*///			}
        }
    };
    /*TODO*///	
/*TODO*///	void ppu_mirror_h (void)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror: horizontal\n");
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[0] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[1] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[2] = &(videoram.read(0x2400));
/*TODO*///		ppu_page[3] = &(videoram.read(0x2400));
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ppu_mirror_v (void)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror: vertical\n");
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[0] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[1] = &(videoram.read(0x2400));
/*TODO*///		ppu_page[2] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[3] = &(videoram.read(0x2400));
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ppu_mirror_low (void)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror: $2000\n");
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[0] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[1] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[2] = &(videoram.read(0x2000));
/*TODO*///		ppu_page[3] = &(videoram.read(0x2000));
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ppu_mirror_high (void)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror: $2400\n");
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[0] = &(videoram.read(0x2400));
/*TODO*///		ppu_page[1] = &(videoram.read(0x2400));
/*TODO*///		ppu_page[2] = &(videoram.read(0x2400));
/*TODO*///		ppu_page[3] = &(videoram.read(0x2400));
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ppu_mirror_custom (int page, int address)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///		address = (address << 10) | 0x2000;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror custom, page: %d, address: %04x\n", page, address);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[page] = &(videoram.read(address));
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ppu_mirror_custom_vrom (int page, int address)
/*TODO*///	{
/*TODO*///		if (nes.four_screen_vram) return;
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("mirror custom vrom, page: %d, address: %04x\n", page, address);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef NO_MIRRORING
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		ppu_page[page] = &(nes.vrom[address]);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	static void Write_PPU (int data)
/*TODO*///	{
/*TODO*///		int tempAddr = PPU_address & 0x3fff;
/*TODO*///	
/*TODO*///	    if (*ppu_latch != NULL) (*ppu_latch)(tempAddr);
/*TODO*///	
/*TODO*///	#ifdef LOG_PPU
/*TODO*///		logerror ("   Write_PPU %04x: %02x\n", tempAddr, data);
/*TODO*///	#endif
/*TODO*///		if (tempAddr < 0x2000)
/*TODO*///		{
/*TODO*///			/* This ROM writes to the character gen portion of VRAM */
/*TODO*///			dirtychar[tempAddr >> 4] = 1;
/*TODO*///			nes.vram[tempAddr] = data;
/*TODO*///			videoram.write(tempAddr,data);
/*TODO*///	
/*TODO*///			if (nes.chr_chunks != 0)
/*TODO*///				logerror("****** PPU write to vram with CHR_ROM - %04x:%02x!\n", tempAddr, data);
/*TODO*///			goto end;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* The only valid background colors are writes to 0x3f00 and 0x3f10 */
/*TODO*///		/* and even then, they are mirrors of each other. */
/*TODO*///		/* As usual, some games attempt to write values > the number of colors so we must mask the data. */
/*TODO*///		if (tempAddr >= 0x3f00)
/*TODO*///		{
/*TODO*///			videoram.write(tempAddr,data);
/*TODO*///			data &= 0x3f;
/*TODO*///	
/*TODO*///			if ((tempAddr & 0x03) != 0)
/*TODO*///			{
/*TODO*///	#ifdef COLOR_INTENSITY
/*TODO*///				Machine.gfx[0].colortable[tempAddr & 0x1f] = Machine.pens[data + (PPU_Control1 & 0xe0)*2];
/*TODO*///				colortable_mono[tempAddr & 0x1f] = Machine.pens[(data & 0xf0) + (PPU_Control1 & 0xe0)*2];
/*TODO*///	#else
/*TODO*///				Machine.gfx[0].colortable[tempAddr & 0x1f] = Machine.pens[data];
/*TODO*///				colortable_mono[tempAddr & 0x1f] = Machine.pens[data & 0xf0];
/*TODO*///	#endif
/*TODO*///			}
/*TODO*///	
/*TODO*///			if ((tempAddr & 0x0f) == 0)
/*TODO*///			{
/*TODO*///				int i;
/*TODO*///	
/*TODO*///				PPU_background_color = data;
/*TODO*///				for (i = 0; i < 0x20; i += 0x04)
/*TODO*///				{
/*TODO*///	#ifdef COLOR_INTENSITY
/*TODO*///					Machine.gfx[0].colortable[i] = Machine.pens[data + (PPU_Control1 & 0xe0)*2];
/*TODO*///					colortable_mono[i] = Machine.pens[(data & 0xf0) + (PPU_Control1 & 0xe0)*2];
/*TODO*///	#else
/*TODO*///					Machine.gfx[0].colortable[i] = Machine.pens[data];
/*TODO*///					colortable_mono[i] = Machine.pens[data & 0xf0];
/*TODO*///	#endif
/*TODO*///				}
/*TODO*///			}
/*TODO*///			goto end;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* everything else */
/*TODO*///		else
/*TODO*///		{
/*TODO*///			/* Writes to $3000-$3eff are mirrors of $2000-$2eff, used by e.g. Trojan */
/*TODO*///			int page = (PPU_address & 0x0c00) >> 10;
/*TODO*///			int address = PPU_address & 0x3ff;
/*TODO*///	
/*TODO*///			ppu_page[page][address] = data;
/*TODO*///		}
/*TODO*///	
/*TODO*///	end:
/*TODO*///		PPU_address += PPU_add;
/*TODO*///	}
/*TODO*///	
/*TODO*///	extern struct GfxLayout nes_charlayout;

    public static io_initPtr nes_load_rom = new io_initPtr() {
        public int handler(int id) {
            /*TODO*///		const char *mapinfo;
            int mapint1 = 0, mapint2 = 0, mapint3 = 0, mapint4 = 0, goodcrcinfo = 0;
            Object romfile;
            char[] magic = new char[4];
            char[] skank = new char[8];
            int local_options = 0;
            char m[] = new char[1];
            int i;

            if ((device_filename(IO_CARTSLOT, id) == null) && (id == 0)) {
                //		printf("NES requires cartridge!\n");
                return INIT_FAILED;
            } else {
                battery_name = device_filename(IO_CARTSLOT, id);

                /* Strip off file extension if it exists */
                if (battery_name.lastIndexOf('.') != -1) {
                    battery_name = battery_name.substring(0, battery_name.lastIndexOf('.'));

                }
                logerror("battery name (minus extension): %s\n", battery_name);
            }

            if ((romfile = image_fopen(IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0)) == null) {
                logerror("image_fopen failed in nes_load_rom.\n");
                return 1;
            }

            /* Verify the file is in iNES format */
            osd_fread(romfile, magic, 4);

            if ((magic[0] != 'N')
                    || (magic[1] != 'E')
                    || (magic[2] != 'S')) {
                logerror("BAD section hit during LOAD ROM.\n");
                osd_fclose(romfile);
                return 1;
            }
            /*TODO*///		mapinfo = device_extrainfo(IO_CARTSLOT,id);
/*TODO*///		if (mapinfo != 0)
/*TODO*///		{
/*TODO*///			if (4 == sscanf(mapinfo,"%d %d %d %d",&mapint1,&mapint2,&mapint3,&mapint4))
/*TODO*///			{
/*TODO*///				nes.mapper = mapint1;
/*TODO*///				local_options = mapint2;
/*TODO*///				nes.prg_chunks = mapint3;
/*TODO*///				nes.chr_chunks = mapint4;
/*TODO*///				logerror("NES.CRC info: %d %d %d %d\n",mapint1,mapint2,mapint3,mapint4);
/*TODO*///				goodcrcinfo = 1;
/*TODO*///			} else 
/*TODO*///			{
/*TODO*///				logerror("NES: [%s], Invalid mapinfo found\n",mapinfo);
/*TODO*///			}
/*TODO*///		} else
/*TODO*///		{
/*TODO*///			logerror("NES: No extrainfo found\n");
/*TODO*///		}
            if (goodcrcinfo == 0) {
                osd_fread(romfile, _nes.prg_chunks, 1);
                osd_fread(romfile, _nes.chr_chunks, 1);
                /* Read the first ROM option byte (offset 6) */
                osd_fread(romfile, m, 1);

                /* Interpret the iNES header flags */
                _nes.mapper = (char) ((m[0] & 0xf0) >> 4);
                local_options = m[0] & 0x0f;

                /* Read the second ROM option byte (offset 7) */
                osd_fread(romfile, m, 1);

                /* Check for skanky headers */
                osd_fread(romfile, skank, 8);

                /* If the header has junk in the unused bytes, assume the extra mapper byte is also invalid */
 /* We only check the first 4 unused bytes for now */
                for (i = 0; i < 4; i++) {
                    logerror("%02x ", skank[i]);
                    if (skank[i] != 0x00) {
                        logerror("(skank: %d)", i);
                        //				m = 0;
                    }
                }
                logerror("\n");

                _nes.mapper = (char) (_nes.mapper | (m[0] & 0xf0));
            }

            _nes.u8_hard_mirroring = local_options & 0x01;
            _nes.u8_battery = local_options & 0x02;
            _nes.u8_trainer = local_options & 0x04;
            _nes.u8_four_screen_vram = local_options & 0x08;

            if (_nes.u8_battery != 0) {
                logerror("-- Battery found\n");
            }
            if (_nes.u8_trainer != 0) {
                logerror("-- Trainer found\n");
            }
            if (_nes.u8_four_screen_vram != 0) {
                logerror("-- 4-screen VRAM\n");
            }

            /* Free the regions that were allocated by the ROM loader */
            free_memory_region(REGION_CPU1);
            free_memory_region(REGION_GFX1);

            /* Allocate them again with the proper size */
            if (new_memory_region(REGION_CPU1, 0x10000 + (_nes.prg_chunks[0] + 1) * 0x4000) != 0
                    || new_memory_region(REGION_GFX1, (_nes.chr_chunks[0] + 1) * 0x2000) != 0) {
                printf("Memory allocation failed reading roms!\n");
                logerror("BAD section hit during LOAD ROM.\n");
                osd_fclose(romfile);
                return 1;
            }

            _nes.rom = memory_region(REGION_CPU1);
            _nes.vrom = memory_region(REGION_GFX1);
            _nes.vram = memory_region(REGION_GFX2);
            _nes.wram = memory_region(REGION_USER1);

            /* Position past the header */
            osd_fseek(romfile, 16, SEEK_SET);

            /* Load the 0x200 byte trainer at 0x7000 if it exists */
            if (_nes.u8_trainer != 0) {
                /*TODO*///			osd_fread (romfile, &_nes.wram[0x1000], 0x200);
                throw new UnsupportedOperationException("Not supported yet.");

            }

            /* Read in the program chunks */
            if (_nes.prg_chunks[0] == 1) {
                osd_fread(romfile, _nes.rom, 0x14000, 0x4000);
                /* Mirror this bank into $8000 */
                memcpy(_nes.rom, 0x10000, _nes.rom, 0x14000, 0x4000);
            } else {
                throw new UnsupportedOperationException("Not supported yet.");
                /*TODO*///			osd_fread (romfile, &_nes.rom[0x10000], 0x4000 * _nes.prg_chunks);
            }

            logerror("**\n");
            logerror("Mapper: %d\n", _nes.mapper);
            logerror("PRG chunks: %02x, size: %06x\n", _nes.prg_chunks[0], 0x4000 * _nes.prg_chunks[0]);

            /* Read in any chr chunks */
            if (_nes.chr_chunks[0] > 0) {
                osd_fread(romfile, _nes.vrom, 0x2000 * _nes.chr_chunks[0]);

                /* Mark each char as not existing in VRAM */
                for (i = 0; i < 512; i++) {
                    use_vram[i] = 0;
                }
                /* Calculate the total number of characters to decode */
                nes_charlayout.total = _nes.chr_chunks[0] * 512;
                if (_nes.mapper == 2) {
                    printf("Warning: VROM has been found in VRAM-based mapper. Either the mapper is set wrong or the ROM image is incorrect.\n");
                }
            } else {
                /* Mark each char as existing in VRAM */
                for (i = 0; i < 512; i++) {
                    use_vram[i] = 1;
                }
                nes_charlayout.total = 512;
            }

            logerror("CHR chunks: %02x, size: %06x\n", _nes.chr_chunks, 0x4000 * _nes.chr_chunks[0]);
            logerror("**\n");

            /* Attempt to load a battery file for this ROM. If successful, we */
 /* must wait until later to move it to the system memory. */
            if (_nes.u8_battery != 0) {
                throw new UnsupportedOperationException("Not supported yet.");
                /*TODO*///			void *f;
/*TODO*///	
/*TODO*///			f = osd_fopen (battery_name, 0, OSD_FILETYPE_NVRAM, 0);
/*TODO*///			if (f != 0)
/*TODO*///			{
/*TODO*///				osd_fread (f, battery_data, BATTERY_SIZE);
/*TODO*///				osd_fclose (f);
/*TODO*///			}
/*TODO*///			else
/*TODO*///				memset (battery_data, 0, BATTERY_SIZE);
            }

            osd_fclose(romfile);
            return 0;

        }
    };

    public static io_partialcrcPtr nes_partialcrc = new io_partialcrcPtr() {
        public int/*UINT32*/ handler(UBytePtr buf,/*unsigned*/ int size) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///	UINT32 crc;
/*TODO*///	if (size < 17) return 0;
/*TODO*///	crc = (UINT32) crc32(0L,&buf[16],size-16);
/*TODO*///	logerror("NES Partial CRC: %08lx %d\n",crc,size);
/*TODO*///	return crc;
        }
    };
    public static io_idPtr nes_id_rom = new io_idPtr() {
        public int handler(int id) {
            Object romfile;
            char[] magic = new char[4];
            int retval;

            if ((romfile = image_fopen(IO_CARTSLOT, id, OSD_FILETYPE_IMAGE_R, 0)) == null) {
                return 0;
            }

            retval = 1;
            /* Verify the file is in iNES format */
            osd_fread(romfile, magic, 4);
            if ((magic[0] != 'N')
                    || (magic[1] != 'E')
                    || (magic[2] != 'S')) {
                retval = 0;
            }

            osd_fclose(romfile);
            return retval;
        }
    };
    /*TODO*///	
/*TODO*///	int nes_load_disk (int id)
/*TODO*///	{
/*TODO*///	 	FILE *diskfile;
/*TODO*///		unsigned char magic[4];
/*TODO*///	
/*TODO*///		if (!device_filename(IO_FLOPPY,id)) return INIT_FAILED;
/*TODO*///	
/*TODO*///		if (!(diskfile = image_fopen (IO_FLOPPY, id, OSD_FILETYPE_IMAGE_R, 0)))
/*TODO*///		{
/*TODO*///			logerror("image_fopen failed in nes_load_disk for [%s].\n",device_filename(IO_FLOPPY,id));
/*TODO*///				return 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* See if it has a fucking redundant header on it */
/*TODO*///		osd_fread (diskfile, magic, 4);
/*TODO*///		if ((magic[0] == 'F') &&
/*TODO*///			(magic[1] == 'D') &&
/*TODO*///			(magic[2] == 'S'))
/*TODO*///		{
/*TODO*///			/* Skip past the fucking redundant header */
/*TODO*///			osd_fseek (diskfile, 0x10, SEEK_SET);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			/* otherwise, point to the start of the image */
/*TODO*///			osd_fseek (diskfile, 0, SEEK_SET);
/*TODO*///	
/*TODO*///		/* clear some of the cart variables we don't use */
/*TODO*///		nes.trainer = 0;
/*TODO*///		nes.battery = 0;
/*TODO*///		nes.prg_chunks = nes.chr_chunks = 0;
/*TODO*///	
/*TODO*///		nes.mapper = 20;
/*TODO*///		nes.four_screen_vram = 0;
/*TODO*///		nes.hard_mirroring = 0;
/*TODO*///	
/*TODO*///		nes_fds.sides = 0;
/*TODO*///		nes_fds.data = NULL;
/*TODO*///	
/*TODO*///		/* read in all the sides */
/*TODO*///		while (!osd_feof (diskfile))
/*TODO*///		{
/*TODO*///			nes_fds.sides ++;
/*TODO*///			nes_fds.data = realloc (nes_fds.data, nes_fds.sides * 65500);
/*TODO*///			osd_fread (diskfile, nes_fds.data + ((nes_fds.sides-1) * 65500), 65500);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* adjust for eof */
/*TODO*///		nes_fds.sides --;
/*TODO*///		nes_fds.data = realloc (nes_fds.data, nes_fds.sides * 65500);
/*TODO*///	
/*TODO*///		logerror ("Number of sides: %d", nes_fds.sides);
/*TODO*///	
/*TODO*///		osd_fclose (diskfile);
/*TODO*///		return 0;
/*TODO*///	
/*TODO*///	//bad:
/*TODO*///		logerror("BAD section hit during disk load.\n");
/*TODO*///		if (diskfile != 0) osd_fclose (diskfile);
/*TODO*///		return 1;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void nes_exit_disk (int id)
/*TODO*///	{
/*TODO*///		/* TODO: should write out changes here as well */
/*TODO*///		free (nes_fds.data);
/*TODO*///		nes_fds.data = NULL;
/*TODO*///	}
/*TODO*///	
}
