/*
 * ported to v0.37b6
 * using automatic conversion tool v0.01
 */
package mess.vidhrdw;

import static WIP.arcadeflex.fucPtr.*;
import WIP.arcadeflex.libc_v2.UBytePtr;
import WIP.arcadeflex.libc_v2.UShortPtr;
import static WIP.mame.common.*;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static arcadeflex.libc.cstring.memset;
import static vidhrdw.generic.tmpbitmap;
import static old.arcadeflex.video.osd_free_bitmap;
import static mess.vidhrdw.smsvdpH.*;
import static old.mame.cpuintrfH.ASSERT_LINE;
import static old.arcadeflex.video.osd_skip_this_frame;
import static old.cpu.z80.z80H.Z80_IGNORE_INT;
import static old.mame.cpuintrfH.CLEAR_LINE;
import static old.mame.drawgfxH.TRANSPARENCY_NONE;
import static old.mame.cpuintrf.*;
import static old.mame.drawgfx.*;
import static old.mame.palette.*;
import static cpu.z80.z80.*;
import static old.arcadeflex.libc_old.sizeof;
import static WIP.arcadeflex.libc.memcpy.*;
import static mess.machine.nes._nes;
import static vidhrdw.generic.*;
import static old.arcadeflex.video.osd_alloc_bitmap;

public class nes {

    /*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	//#define BACKGROUND_OFF
/*TODO*///	//#define LOG_VIDEO
/*TODO*///	//#define LOG_COLOR
/*TODO*///	//#define DIRTY_BUFFERS
/*TODO*///	
    public static final int VIDEORAM_SIZE = 0x4000;
    public static final int SPRITERAM_SIZE = 0x100;
    public static final int VRAM_SIZE = 0x3c0;

    static int[] nes_vram = new int[8];
    /* Keep track of 8 .5k vram pages to speed things up */
    static int[] nes_vram_sprite = new int[8];
    /* Used only by mmc5 for now */
    static int[] dirtychar = new int[0x200];

    static int gfx_bank;

    static char[] nes_palette = new char[3 * 64];

    /*TODO*///	unsigned char line_priority[0x100];
/*TODO*///	
    /* Changed at runtime */
    static char nes_colortable[]
            = {
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,
                0, 1, 2, 3,};
    /*TODO*///	
/*TODO*///	unsigned short colortable_mono[] =
/*TODO*///	{
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///		0,1,2,3,
/*TODO*///	};
/*TODO*///	
/*TODO*///	#ifdef LOG_VIDEO
/*TODO*///	FILE *videolog;
/*TODO*///	#endif
/*TODO*///	#ifdef LOG_COLOR
/*TODO*///	FILE *colorlog;
/*TODO*///	#endif
/*TODO*///	
    public static VhConvertColorPromPtr nes_init_palette = new VhConvertColorPromPtr() {
        public void handler(char[] palette, char[] colortable, UBytePtr color_prom) {
            /* This routine builds a palette using a transformation from */
 /* the YUV (Y, B-Y, R-Y) to the RGB color space */

 /* The NES has a 64 color palette                        */
 /* 16 colors, with 4 luminance levels for each color     */
 /* The 16 colors circle around the YUV color space,      */
 /* It also returns a fake colortable, for the menus */
            int i, j;

            double R, G, B;

            double tint = .5;
            double hue = 332.0;
            double bright_adjust = 1.0;

            double brightness[][]
                    = {
                        {0.50, 0.75, 1.0, 1.0},
                        {0.29, 0.45, 0.73, 0.9},
                        {0, 0.24, 0.47, 0.77}
                    };

            double angle[] = {0, 240, 210, 180, 150, 120, 90, 60, 30, 0, 330, 300, 270, 0, 0, 0};
            int p_ptr = 0;

            /* loop through the 4 intensities */
            for (i = 0; i < 4; i++) {
                /* loop through the 16 colors */
                for (j = 0; j < 16; j++) {
                    double sat;
                    double y;
                    double rad;

                    switch (j) {
                        case 0:
                            sat = 0;
                            y = brightness[0][i];
                            break;
                        case 13:
                            sat = 0;
                            y = brightness[2][i];
                            break;
                        case 14:
                        case 15:
                            sat = 0;
                            y = 0;
                            break;
                        default:
                            sat = tint;
                            y = brightness[1][i];
                            break;
                    }

                    rad = Math.PI * ((angle[j] + hue) / 180.0);

                    y *= bright_adjust;

                    /* Transform to RGB */
                    R = (y + sat * Math.sin(rad)) * 255.0;
                    G = (y - (27 / 53) * sat * Math.sin(rad) + (10 / 53) * sat * Math.cos(rad)) * 255.0;
                    B = (y - sat * Math.cos(rad)) * 255.0;

                    /* Clipping, in case of saturation */
                    if (R < 0) {
                        R = 0;
                    }
                    if (R > 255) {
                        R = 255;
                    }
                    if (G < 0) {
                        G = 0;
                    }
                    if (G > 255) {
                        G = 255;
                    }
                    if (B < 0) {
                        B = 0;
                    }
                    if (B > 255) {
                        B = 255;
                    }

                    /* Round, and set the value */
                    palette[p_ptr] = (char) Math.floor(R + .5);
                    nes_palette[(i * 16 + j) * 3] = (char) (palette[p_ptr] & 0xFF);
                    p_ptr++;
                    palette[p_ptr] = (char) Math.floor(G + .5);
                    nes_palette[(i * 16 + j) * 3 + 1] = (char) (palette[p_ptr] & 0xFF);
                    p_ptr++;
                    palette[p_ptr] = (char) Math.floor(B + .5);
                    nes_palette[(i * 16 + j) * 3 + 2] = (char) (palette[p_ptr] & 0xFF);
                    p_ptr++;
                }
            }

            memcpy(colortable, nes_colortable, sizeof(nes_colortable));
        }
    };

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr nes_vh_start = new VhStartPtr() {
        public int handler() {
            int i;

            /* We must clear the videoram on startup */
            if ((videoram = new UBytePtr(VIDEORAM_SIZE)) == null) {
                return 1;
            }

            /* We use an offscreen bitmap that's 4 times as large as the visible one */
            if ((tmpbitmap = osd_alloc_bitmap(2 * 32 * 8, 2 * 30 * 8, Machine.scrbitmap.depth)) == null) {
                videoram = null;
                osd_free_bitmap(tmpbitmap);
                return 1;
            }

            /* sprite RAM must be clear on startup */
            if ((spriteram = new UBytePtr(SPRITERAM_SIZE)) == null) {
                videoram = null;
                osd_free_bitmap(tmpbitmap);
                return 1;
            }
            /* Mark all chars as 'clean' */
            for (i = 0; i < 0x200; i++) {
                dirtychar[i] = 0;
            }

            if (_nes.chr_chunks[0] == 0) {
                gfx_bank = 1;
            } else {
                gfx_bank = 0;
            }

            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr nes_vh_stop = new VhStopPtr() {
        public void handler() {
            videoram = null;
            spriteram = null;
            osd_free_bitmap(tmpbitmap);

        }
    };

    /**
     * *************************************************************************
     *
     * Draw the current scanline
     *
     **************************************************************************
     */
    /*TODO*///	
/*TODO*///	void nes_vh_renderscanline (int scanline)
/*TODO*///	{
/*TODO*///		int x;
/*TODO*///		int i;
/*TODO*///		int start_x;
/*TODO*///		int tile_index;
/*TODO*///		UINT8 scroll_x_coarse;
/*TODO*///		UINT8 scroll_y_coarse;
/*TODO*///		UINT8 scroll_y_fine;
/*TODO*///		UINT8 color_mask;
/*TODO*///		int total_elements;
/*TODO*///		const unsigned short *paldata_1;
/*TODO*///	//	const UBytePtr sd_1;
/*TODO*///	
/*TODO*///	#ifdef BIG_SCREEN
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		profiler_mark(PROFILER_VIDEO);
/*TODO*///	
/*TODO*///		if (osd_skip_this_frame ())
/*TODO*///			goto draw_nothing;
/*TODO*///	
/*TODO*///		if ((PPU_Control1 & 0x01) != 0)
/*TODO*///			color_mask = 0xf0;
/*TODO*///		else
/*TODO*///			color_mask = 0xff;
/*TODO*///	
/*TODO*///	#ifndef COLOR_INTENSITY
/*TODO*///		/* Set the background color */
/*TODO*///		memset (Machine.scrbitmap.line[scanline], Machine.pens[PPU_background_color & color_mask], 0x100);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		for (i = 0; i < 0x100; i ++)
/*TODO*///		{
/*TODO*///			/* Clear the priority buffer for this line */
/*TODO*///			line_priority[i] = 0;
/*TODO*///	
/*TODO*///			/* Set the background color */
/*TODO*///	//		plot_pixel (Machine.scrbitmap, i, scanline, Machine.pens[PPU_background_color]);
/*TODO*///	#ifdef COLOR_INTENSITY
/*TODO*///			((UINT16 *) Machine.scrbitmap.line[scanline])[i] = Machine.pens[PPU_background_color & color_mask];
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Clear sprite count for this line */
/*TODO*///		PPU_Status &= ~PPU_status_8sprites;
/*TODO*///	
/*TODO*///		/* If the background is off, don't draw it */
/*TODO*///	#ifndef BACKGROUND_OFF
/*TODO*///		if (!(PPU_Control1 & PPU_c1_background))
/*TODO*///	#endif
/*TODO*///		{
/*TODO*///			goto draw_sprites;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Determine where in the nametable to start drawing from, based on the current scanline and scroll regs */
/*TODO*///		scroll_x_coarse = PPU_refresh_data & 0x1f;
/*TODO*///		scroll_y_coarse = (PPU_refresh_data & 0x3e0) >> 5;
/*TODO*///		scroll_y_fine = (PPU_refresh_data & 0x7000) >> 12;
/*TODO*///	
/*TODO*///		start_x = (PPU_X_fine ^ 0x07) - 7;
/*TODO*///	
/*TODO*///		x = scroll_x_coarse;
/*TODO*///		tile_index = ((PPU_refresh_data & 0xc00) | 0x2000) + scroll_y_coarse * 32;
/*TODO*///	
/*TODO*///	#ifdef LOG_VIDEO
/*TODO*///	if ((scanline == 0) && (videolog)) fprintf (videolog, "\n");
/*TODO*///	if (videolog != 0) fprintf (videolog, "%03d: ", scanline);
/*TODO*///	#endif
/*TODO*///	#ifdef LOG_COLOR
/*TODO*///	if ((scanline == 0) && (colorlog)) fprintf (colorlog, "\n");
/*TODO*///	if (colorlog != 0) fprintf (colorlog, "%03d: ", scanline);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		total_elements = Machine.gfx[gfx_bank].total_elements;
/*TODO*///	
/*TODO*///	//	sd_1 = Machine.gfx[gfx_bank].gfxdata;
/*TODO*///		if ((PPU_Control1 & 0x01) != 0)
/*TODO*///			paldata_1 = colortable_mono;
/*TODO*///		else
/*TODO*///			paldata_1 = Machine.gfx[0].colortable;
/*TODO*///	
/*TODO*///		/* Draw the 32 or 33 tiles that make up a line */
/*TODO*///		while (start_x < 256)
/*TODO*///		{
/*TODO*///			int color_byte;
/*TODO*///			int color_bits;
/*TODO*///			int pos;
/*TODO*///			int index1;
/*TODO*///			int page, address;
/*TODO*///			int index2;
/*TODO*///	
/*TODO*///			index1 = tile_index + x;
/*TODO*///	
/*TODO*///			/* Figure out which byte in the color table to use */
/*TODO*///			pos = ((index1 & 0x380) >> 4) | ((index1 & 0x1f) >> 2);
/*TODO*///	
/*TODO*///			/* TODO: this only needs calculating every 2nd tile - link it to "x" */
/*TODO*///			page = (index1 & 0x0c00) >> 10;
/*TODO*///			address = 0x3c0 + pos;
/*TODO*///			color_byte = ppu_page[page][address];
/*TODO*///	
/*TODO*///			/* Figure out which bits in the color table to use */
/*TODO*///			color_bits = ((index1 & 0x40) >> 4) + (index1 & 0x02);
/*TODO*///	
/*TODO*///			address = index1 & 0x3ff;
/*TODO*///			index2 = nes_vram[(ppu_page[page][address] >> 6) | PPU_tile_page] + (ppu_page[page][address] & 0x3f);
/*TODO*///	
/*TODO*///	#if 0
/*TODO*///	if ((nes_vram[(ppu_page[page][address] >> 6) | PPU_tile_page] + (ppu_page[page][address] & 0x3f) == 0xfd) || (nes_vram[(ppu_page[page][address] >> 6) | PPU_tile_page] + (ppu_page[page][address] & 0x3f) == 0xfe))
/*TODO*///		index2 = rand() & 0xff;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef MMC5_VRAM
/*TODO*///			/* Use the extended bits if necessary */
/*TODO*///			if ((MMC5_vram_control & 0x01) != 0)
/*TODO*///			{
/*TODO*///				index2 |= (MMC5_vram[address] & 0x3f) << 8;
/*TODO*///			}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef LOG_VIDEO
/*TODO*///	if (videolog != 0) fprintf (videolog, "%02x ", ppu_page[page][address]);
/*TODO*///	#endif
/*TODO*///	#ifdef LOG_COLOR
/*TODO*///	//if (colorlog != 0) fprintf (colorlog, "%02x ", (color_byte >> color_bits) & 0x03);
/*TODO*///	//if (colorlog != 0) fprintf (colorlog, "%02x ", sd[i]);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///			{
/*TODO*///				const unsigned short *paldata;
/*TODO*///				const UBytePtr sd;
/*TODO*///				UBytePtr bm;
/*TODO*///				int start;
/*TODO*///	
/*TODO*///	//			paldata = &Machine.gfx[gfx_bank].colortable[4 * (((color_byte >> color_bits) & 0x03)/* % 8*/)];
/*TODO*///				paldata = &paldata_1[4 * (((color_byte >> color_bits) & 0x03))];
/*TODO*///				bm = Machine.scrbitmap.line[scanline] + start_x;
/*TODO*///	//			sd = &Machine.gfx[gfx_bank].gfxdata[start * Machine.gfx[gfx_bank].width];
/*TODO*///				start = (index2 % total_elements) * 8 + scroll_y_fine;
/*TODO*///				sd = &Machine.gfx[gfx_bank].gfxdata[start * 8];
/*TODO*///	//			sd = &sd_1[start * 8];
/*TODO*///	
/*TODO*///	#ifdef LOG_COLOR
/*TODO*///	//if (colorlog != 0) fprintf (colorlog, "%02x ", (color_byte >> color_bits) & 0x03);
/*TODO*///	if (colorlog != 0) fprintf (colorlog, "%02x ", sd[i]);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///				for (i = 0; i < 8; i ++)
/*TODO*///				{
/*TODO*///					if (start_x + i >= 0)
/*TODO*///					{
/*TODO*///						if (sd[i])
/*TODO*///						{
/*TODO*///							plot_pixel (Machine.scrbitmap, start_x+i, scanline, paldata[sd[i]]);
/*TODO*///							line_priority[start_x + i] |= 0x02;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				if (*ppu_latch)
/*TODO*///				{
/*TODO*///					(*ppu_latch)((PPU_tile_page << 10) | (ppu_page[page][address] << 4));
/*TODO*///				}
/*TODO*///	
/*TODO*///			}
/*TODO*///	
/*TODO*///			start_x += 8;
/*TODO*///	
/*TODO*///			/* Move to next tile over and toggle the horizontal name table if necessary */
/*TODO*///			x ++;
/*TODO*///			if (x > 31)
/*TODO*///			{
/*TODO*///				x = 0;
/*TODO*///				tile_index ^= 0x400;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	#ifdef LOG_VIDEO
/*TODO*///	if (videolog != 0) fprintf (videolog, "\n");
/*TODO*///	#endif
/*TODO*///	#ifdef LOG_COLOR
/*TODO*///	if (colorlog != 0) fprintf (colorlog, "\n");
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* If the left 8 pixels for the background are off, blank 'em */
/*TODO*///		/* TODO: handle this properly, along with sprite clipping */
/*TODO*///		if (!(PPU_Control1 & PPU_c1_background_L8))
/*TODO*///		{
/*TODO*///			memset (Machine.scrbitmap.line[scanline], Machine.pens[PPU_background_color & color_mask], 0x08);
/*TODO*///		}
/*TODO*///	
/*TODO*///	draw_sprites:
/*TODO*///		/* If sprites are hidden in the leftmost column, fake a priority flag to mask them */
/*TODO*///		if (!(PPU_Control1 & PPU_c1_sprites_L8))
/*TODO*///		{
/*TODO*///			for (i = 0; i < 8; i ++)
/*TODO*///				line_priority[i] |= 0x01;
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* If sprites are on, draw them */
/*TODO*///		if ((PPU_Control1 & PPU_c1_sprites) != 0)
/*TODO*///		{
/*TODO*///			render_sprites (scanline);
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* Does the user not want to see the top/bottom 8 lines? */
/*TODO*///		if ((readinputport(7) & 0x01) && ((scanline < 8) || (scanline > 231)))
/*TODO*///		{
/*TODO*///			/* Clear this line if we're not drawing it */
/*TODO*///	#ifdef COLOR_INTENSITY
/*TODO*///			for (i = 0; i < 0x100; i ++)
/*TODO*///				((UINT16 *) Machine.scrbitmap.line[scanline])[i] = Machine.pens[0x3f & color_mask];
/*TODO*///	#else
/*TODO*///			memset (Machine.scrbitmap.line[scanline], Machine.pens[0x3f & color_mask], 0x100);
/*TODO*///	#endif
/*TODO*///		}
/*TODO*///	
/*TODO*///	draw_nothing:
/*TODO*///		/* Increment the fine y-scroll */
/*TODO*///		PPU_refresh_data += 0x1000;
/*TODO*///	
/*TODO*///		/* If it's rolled, increment the coarse y-scroll */
/*TODO*///		if ((PPU_refresh_data & 0x8000) != 0)
/*TODO*///		{
/*TODO*///			UINT16 tmp;
/*TODO*///			tmp = (PPU_refresh_data & 0x03e0) + 0x20;
/*TODO*///			PPU_refresh_data &= 0x7c1f;
/*TODO*///			/* Handle bizarro scrolling rollover at the 30th (not 32nd) vertical tile */
/*TODO*///			if (tmp == 0x03c0)
/*TODO*///			{
/*TODO*///				PPU_refresh_data ^= 0x0800;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				PPU_refresh_data |= (tmp & 0x03e0);
/*TODO*///			}
/*TODO*///	    }
/*TODO*///	
/*TODO*///		profiler_mark(PROFILER_END);
/*TODO*///	}
/*TODO*///	
/*TODO*///	static void render_sprites (int scanline)
/*TODO*///	{
/*TODO*///		int x,y;
/*TODO*///		int tile, i, index1, page;
/*TODO*///		int pri;
/*TODO*///	
/*TODO*///		int flipx, flipy, color;
/*TODO*///		int size;
/*TODO*///		int spriteCount;
/*TODO*///	
/*TODO*///		/* Determine if the sprites are 8x8 or 8x16 */
/*TODO*///		size = (PPU_Control0 & PPU_c0_sprite_size) ? 16 : 8;
/*TODO*///	
/*TODO*///		spriteCount = 0;
/*TODO*///	
/*TODO*///		for (i = 0; i < 0x100; i += 4)
/*TODO*///	//	for (i = 0xfc; i >= 0; i -= 4)
/*TODO*///		{
/*TODO*///			y = spriteram.read(i)+ 1;
/*TODO*///	
/*TODO*///			/* If the sprite isn't visible, skip it */
/*TODO*///			if ((y + size <= scanline) || (y > scanline)) continue;
/*TODO*///	
/*TODO*///			x    = spriteram.read(i+3);
/*TODO*///			tile = spriteram.read(i+1);
/*TODO*///			color = (spriteram.read(i+2)& 0x03) + 4;
/*TODO*///			pri = spriteram.read(i+2)& 0x20;
/*TODO*///			flipx = spriteram.read(i+2)& 0x40;
/*TODO*///			flipy = spriteram.read(i+2)& 0x80;
/*TODO*///	
/*TODO*///			if (size == 16)
/*TODO*///			{
/*TODO*///				/* If it's 8x16 and odd-numbered, draw the other half instead */
/*TODO*///				if ((tile & 0x01) != 0)
/*TODO*///				{
/*TODO*///					tile &= ~0x01;
/*TODO*///					tile |= 0x100;
/*TODO*///				}
/*TODO*///				/* Note that the sprite page value has no effect on 8x16 sprites */
/*TODO*///				page = tile >> 6;
/*TODO*///			}
/*TODO*///			else page = (tile >> 6) | PPU_sprite_page;
/*TODO*///	
/*TODO*///	//		if (Mapper == 5)
/*TODO*///	//			index1 = nes_vram_sprite[page] + (tile & 0x3f);
/*TODO*///	//		else
/*TODO*///				index1 = nes_vram[page] + (tile & 0x3f);
/*TODO*///	
/*TODO*///	//if (priority == 0)
/*TODO*///	{
/*TODO*///			if (*ppu_latch)
/*TODO*///			{
/*TODO*///	//			if ((tile == 0x1fd) || (tile == 0x1fe)) Debugger ();
/*TODO*///				(*ppu_latch)((PPU_sprite_page << 10) | ((tile & 0xff) << 4));
/*TODO*///			}
/*TODO*///	//		continue;
/*TODO*///	}
/*TODO*///	
/*TODO*///			{
/*TODO*///				int sprite_line;
/*TODO*///				int drawn = 0;
/*TODO*///				const unsigned short *paldata;
/*TODO*///				const UBytePtr sd;
/*TODO*///				UBytePtr bm;
/*TODO*///				int start;
/*TODO*///	
/*TODO*///				sprite_line = scanline - y;
/*TODO*///				if (flipy != 0) sprite_line = (size-1)-sprite_line;
/*TODO*///	
/*TODO*///	if ((i == 0) /*&& (spriteram.read(i+2)& 0x20)*/)
/*TODO*///	{
/*TODO*///	//	if (y2 == 0)
/*TODO*///	//		logerror ("sprite 0 (%02x/%02x) tile: %04x, bank: %d, color: %02x, flags: %02x\n", x, y, index1, bank, color, spriteram.read(i+2));
/*TODO*///	//	color = rand() & 0xff;
/*TODO*///	//	if (y == 0xc0)
/*TODO*///	//		Debugger ();
/*TODO*///	}
/*TODO*///	
/*TODO*///				paldata = &Machine.gfx[gfx_bank].colortable[4 * color];
/*TODO*///				start = (index1 % Machine.gfx[gfx_bank].total_elements) * 8 + sprite_line;
/*TODO*///				bm = Machine.scrbitmap.line[scanline] + x;
/*TODO*///				sd = &Machine.gfx[gfx_bank].gfxdata[start * Machine.gfx[gfx_bank].width];
/*TODO*///	
/*TODO*///				if (pri != 0)
/*TODO*///				{
/*TODO*///					/* Draw the low-priority sprites */
/*TODO*///					int j;
/*TODO*///	
/*TODO*///					if (flipx != 0)
/*TODO*///					{
/*TODO*///						for (j = 0; j < 8; j ++)
/*TODO*///						{
/*TODO*///							/* Is this pixel non-transparent? */
/*TODO*///							if (sd[7-j])
/*TODO*///							{
/*TODO*///								/* Has the background (or another sprite) already been drawn here? */
/*TODO*///								if (!line_priority [x+j])
/*TODO*///								{
/*TODO*///									/* No, draw */
/*TODO*///									plot_pixel (Machine.scrbitmap, x+j, scanline, paldata[sd[7-j]]);
/*TODO*///									drawn = 1;
/*TODO*///								}
/*TODO*///								/* Indicate that a sprite was drawn at this location, even if it's not seen */
/*TODO*///								line_priority [x+j] |= 0x01;
/*TODO*///	
/*TODO*///								/* Set the "sprite 0 hit" flag if appropriate */
/*TODO*///								if (i == 0) PPU_Status |= PPU_status_sprite0_hit;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						for (j = 0; j < 8; j ++)
/*TODO*///						{
/*TODO*///							/* Is this pixel non-transparent? */
/*TODO*///							if (sd[j])
/*TODO*///							{
/*TODO*///								/* Has the background (or another sprite) already been drawn here? */
/*TODO*///								if (!line_priority [x+j])
/*TODO*///								{
/*TODO*///									plot_pixel (Machine.scrbitmap, x+j, scanline, paldata[sd[j]]);
/*TODO*///									drawn = 1;
/*TODO*///								}
/*TODO*///								/* Indicate that a sprite was drawn at this location, even if it's not seen */
/*TODO*///								line_priority [x+j] |= 0x01;
/*TODO*///	
/*TODO*///								/* Set the "sprite 0 hit" flag if appropriate */
/*TODO*///								if (i == 0) PPU_Status |= PPU_status_sprite0_hit;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					/* Draw the high-priority sprites */
/*TODO*///					int j;
/*TODO*///	
/*TODO*///					if (flipx != 0)
/*TODO*///					{
/*TODO*///						for (j = 0; j < 8; j ++)
/*TODO*///						{
/*TODO*///							/* Is this pixel non-transparent? */
/*TODO*///							if (sd[7-j])
/*TODO*///							{
/*TODO*///								/* Has another sprite been drawn here? */
/*TODO*///								if (!(line_priority[x+j] & 0x01))
/*TODO*///								{
/*TODO*///									/* No, draw */
/*TODO*///									plot_pixel (Machine.scrbitmap, x+j, scanline, paldata[sd[7-j]]);
/*TODO*///									line_priority [x+j] |= 0x01;
/*TODO*///									drawn = 1;
/*TODO*///								}
/*TODO*///	
/*TODO*///								/* Set the "sprite 0 hit" flag if appropriate */
/*TODO*///								if ((i == 0) && (line_priority[x+j] & 0x02))
/*TODO*///									PPU_Status |= PPU_status_sprite0_hit;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						for (j = 0; j < 8; j ++)
/*TODO*///						{
/*TODO*///							/* Is this pixel non-transparent? */
/*TODO*///							if (sd[j])
/*TODO*///							{
/*TODO*///								/* Has another sprite been drawn here? */
/*TODO*///								if (!(line_priority[x+j] & 0x01))
/*TODO*///								{
/*TODO*///									/* No, draw */
/*TODO*///									plot_pixel (Machine.scrbitmap, x+j, scanline, paldata[sd[j]]);
/*TODO*///									line_priority [x+j] |= 0x01;
/*TODO*///									drawn = 1;
/*TODO*///								}
/*TODO*///	
/*TODO*///								/* Set the "sprite 0 hit" flag if appropriate */
/*TODO*///								if ((i == 0) && (line_priority[x+j] & 0x02))
/*TODO*///									PPU_Status |= PPU_status_sprite0_hit;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///	
/*TODO*///				if (drawn != 0)
/*TODO*///				{
/*TODO*///					/* If there are more than 8 sprites on this line, set the flag */
/*TODO*///					spriteCount ++;
/*TODO*///					if (spriteCount == 8)
/*TODO*///					{
/*TODO*///						PPU_Status |= PPU_status_8sprites;
/*TODO*///						logerror ("> 8 sprites (%d), scanline: %d\n", spriteCount, scanline);
/*TODO*///	
/*TODO*///						/* The real NES only draws up to 8 sprites - the rest should be invisible */
/*TODO*///						if ((readinputport (7) & 0x02) == 0)
/*TODO*///							break;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
    public static WriteHandlerPtr nes_vh_sprite_dma_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///		UBytePtr RAM = memory_region(REGION_CPU1);
/*TODO*///	
/*TODO*///		memcpy (spriteram, &RAM[data * 0x100], 0x100);
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///	#ifdef macintosh
/*TODO*///		if (data >= 0x40) SysBeep (0);
/*TODO*///	#endif
/*TODO*///	#endif
        }
    };
    /*TODO*///	
/*TODO*///	void draw_sight(int playerNum, int x_center, int y_center)
/*TODO*///	{
/*TODO*///		int x,y;
/*TODO*///		UINT16 color;
/*TODO*///	
/*TODO*///		if (playerNum == 2)
/*TODO*///			color = Machine.pens[0]; /* grey */
/*TODO*///		else
/*TODO*///			color = Machine.pens[0x30]; /* white */
/*TODO*///	
/*TODO*///	    if (x_center<2)   x_center=2;
/*TODO*///	    if (x_center>253) x_center=253;
/*TODO*///	
/*TODO*///	    if (y_center<2)   y_center=2;
/*TODO*///	    if (y_center>253) y_center=253;
/*TODO*///	
/*TODO*///		for(y = y_center-5; y < y_center+6; y++)
/*TODO*///			if((y >= 0) && (y < 256))
/*TODO*///				plot_pixel (Machine.scrbitmap, x_center, y, color);
/*TODO*///	
/*TODO*///		for(x = x_center-5; x < x_center+6; x++)
/*TODO*///			if((x >= 0) && (x < 256))
/*TODO*///				plot_pixel (Machine.scrbitmap, x, y_center, color);
/*TODO*///	}
/*TODO*///	
    /* This routine is called at the start of vblank to refresh the screen */
    public static VhUpdatePtr nes_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            throw new UnsupportedOperationException("Not supported yet.");
            /*TODO*///	#ifdef BIG_SCREEN
/*TODO*///		int page;
/*TODO*///		int offs;
/*TODO*///		int Size;
/*TODO*///		int i;
/*TODO*///		int page1, address1;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		if (readinputport (2) == 0x01) /* zapper on port 1 */
/*TODO*///		{
/*TODO*///			draw_sight (1, readinputport (3), readinputport (4));
/*TODO*///		}
/*TODO*///		else if (readinputport (2) == 0x10) /* zapper on port 2 */
/*TODO*///		{
/*TODO*///			draw_sight (1, readinputport (3), readinputport (4));
/*TODO*///		}
/*TODO*///		else if (readinputport (2) == 0x11) /* zapper on both ports */
/*TODO*///		{
/*TODO*///			draw_sight (1, readinputport (3), readinputport (4));
/*TODO*///			draw_sight (2, readinputport (5), readinputport (6));
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* if this is a disk system game, check for the flip-disk key */
/*TODO*///		if (nes.mapper == 20)
/*TODO*///		{
/*TODO*///			if (readinputport (11) & 0x01)
/*TODO*///			{
/*TODO*///				while (readinputport (11) & 0x01) { update_input_ports (); };
/*TODO*///	
/*TODO*///				nes_fds.current_side ++;
/*TODO*///				if (nes_fds.current_side > nes_fds.sides)
/*TODO*///					nes_fds.current_side = 0;
/*TODO*///	
/*TODO*///				if (nes_fds.current_side == 0)
/*TODO*///				{
/*TODO*///					usrintf_showmessage ("No disk inserted.");
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					usrintf_showmessage ("Disk set to side %d", nes_fds.current_side);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///	
/*TODO*///	#ifndef BIG_SCREEN
/*TODO*///		/* If we're using the scanline engine, the screen has already been drawn */
/*TODO*///		return;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef BIG_SCREEN /* This is all debugging code */
/*TODO*///		/* for every character in the Video RAM, check if it has been modified */
/*TODO*///		/* since last time and update it accordingly. */
/*TODO*///		for (offs = 0; offs < VRAM_SIZE; offs++)
/*TODO*///		{
/*TODO*///			/* Do page 1 */
/*TODO*///			page = 0x2000;
/*TODO*///	#ifndef BIG_SCREEN
/*TODO*///			if (dirtybuffer[offs])
/*TODO*///	#endif
/*TODO*///			{
/*TODO*///				int sx,sy;
/*TODO*///				int index1, index2, index3;
/*TODO*///	
/*TODO*///				int pos, color;
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///	#ifdef DIRTY_BUFFERS
/*TODO*///				dirtybuffer[offs] = 0;
/*TODO*///	#endif
/*TODO*///				sx = offs % 32;
/*TODO*///				sy = offs / 32;
/*TODO*///	
/*TODO*///				index2 = page + offs;
/*TODO*///	
/*TODO*///				/* Figure out which byte in the color table to use */
/*TODO*///				pos = (index2 & 0x380) >> 4;
/*TODO*///				pos += (index2 & 0x1f) >> 2;
/*TODO*///				page1 = (index2 & 0x0c00) >> 10;
/*TODO*///				address1 = 0x3c0 + pos;
/*TODO*///				color = ppu_page[page1][address1];
/*TODO*///	
/*TODO*///				/* Figure out which bits in the color table to use */
/*TODO*///				index1 = ((index2 & 0x40) >> 4) + (index2 & 0x02);
/*TODO*///	
/*TODO*///				address1 = index2 & 0x3ff;
/*TODO*///				index3 = nes_vram[(ppu_page[page1][address1] >> 6) | PPU_tile_page] + (ppu_page[page1][address1] & 0x3f);
/*TODO*///	
/*TODO*///				if (use_vram[index3])
/*TODO*///					bank = 1;
/*TODO*///				else
/*TODO*///					bank = 0;
/*TODO*///	
/*TODO*///				drawgfx(tmpbitmap, Machine.gfx[bank],
/*TODO*///					index3,
/*TODO*///					(color >> index1) & 0x03,
/*TODO*///					0,0,
/*TODO*///					8*sx,8*sy,
/*TODO*///					0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* Do page 2 - drawn to the right of page 1 */
/*TODO*///			page += 0x400;
/*TODO*///	#ifndef BIG_SCREEN
/*TODO*///			if (dirtybuffer2[offs])
/*TODO*///	#endif
/*TODO*///			{
/*TODO*///				int sx,sy;
/*TODO*///				int index1, index2, index3;
/*TODO*///	
/*TODO*///				int pos, color;
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///	#ifdef DIRTY_BUFFERS
/*TODO*///				dirtybuffer2[offs] = 0;
/*TODO*///	#endif
/*TODO*///				sx = offs % 32;
/*TODO*///				sy = offs / 32;
/*TODO*///	
/*TODO*///				index2 = page + offs;
/*TODO*///	
/*TODO*///				/* Figure out which byte in the color table to use */
/*TODO*///				pos = (index2 & 0x380) >> 4;
/*TODO*///				pos += (index2 & 0x1f) >> 2;
/*TODO*///				page1 = (index2 & 0x0c00) >> 10;
/*TODO*///				address1 = 0x3c0 + pos;
/*TODO*///				color = ppu_page[page1][address1];
/*TODO*///	
/*TODO*///				/* Figure out which bits in the color table to use */
/*TODO*///				index1 = ((index2 & 0x40) >> 4) + (index2 & 0x02);
/*TODO*///	
/*TODO*///				address1 = index2 & 0x3ff;
/*TODO*///				index3 = nes_vram[(ppu_page[page1][address1] >> 6) | PPU_tile_page] + (ppu_page[page1][address1] & 0x3f);
/*TODO*///	
/*TODO*///				if (use_vram[index3])
/*TODO*///					bank = 1;
/*TODO*///				else
/*TODO*///					bank = 0;
/*TODO*///	
/*TODO*///				drawgfx(tmpbitmap, Machine.gfx[bank],
/*TODO*///					index3,
/*TODO*///					(color >> index1) & 0x03,
/*TODO*///					0,0,
/*TODO*///					32*8 + 8*sx,8*sy,
/*TODO*///					0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* Do page 3 - drawn below page 1 */
/*TODO*///			page += 0x400;
/*TODO*///	#ifndef BIG_SCREEN
/*TODO*///			if (dirtybuffer3[offs])
/*TODO*///	#endif
/*TODO*///			{
/*TODO*///				int sx,sy;
/*TODO*///				int index1, index2, index3;
/*TODO*///	
/*TODO*///				int pos, color;
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///	#ifdef DIRTY_BUFFERS
/*TODO*///				dirtybuffer3[offs] = 0;
/*TODO*///	#endif
/*TODO*///				sx = offs % 32;
/*TODO*///				sy = offs / 32;
/*TODO*///	
/*TODO*///				index2 = page + offs;
/*TODO*///	
/*TODO*///				/* Figure out which byte in the color table to use */
/*TODO*///				pos = (index2 & 0x380) >> 4;
/*TODO*///				pos += (index2 & 0x1f) >> 2;
/*TODO*///				page1 = (index2 & 0x0c00) >> 10;
/*TODO*///				address1 = 0x3c0 + pos;
/*TODO*///				color = ppu_page[page1][address1];
/*TODO*///	
/*TODO*///				/* Figure out which bits in the color table to use */
/*TODO*///				index1 = ((index2 & 0x40) >> 4) + (index2 & 0x02);
/*TODO*///	
/*TODO*///				address1 = index2 & 0x3ff;
/*TODO*///				index3 = nes_vram[(ppu_page[page1][address1] >> 6) | PPU_tile_page] + (ppu_page[page1][address1] & 0x3f);
/*TODO*///	
/*TODO*///				if (use_vram[index3])
/*TODO*///					bank = 1;
/*TODO*///				else
/*TODO*///					bank = 0;
/*TODO*///	
/*TODO*///				drawgfx(tmpbitmap, Machine.gfx[bank],
/*TODO*///					index3,
/*TODO*///					(color >> index1) & 0x03,
/*TODO*///					0,0,
/*TODO*///					8*sx, 30*8 + 8*sy,
/*TODO*///					0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///	
/*TODO*///			/* Do page 4 - drawn to the right and below page 1 */
/*TODO*///			page += 0x400;
/*TODO*///	#ifndef BIG_SCREEN
/*TODO*///			if (dirtybuffer4[offs])
/*TODO*///	#endif
/*TODO*///			{
/*TODO*///				int sx,sy;
/*TODO*///				int index1, index2, index3;
/*TODO*///	
/*TODO*///				int pos, color;
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///	#ifdef DIRTY_BUFFERS
/*TODO*///				dirtybuffer4[offs] = 0;
/*TODO*///	#endif
/*TODO*///				sx = offs % 32;
/*TODO*///				sy = offs / 32;
/*TODO*///	
/*TODO*///				index2 = page + offs;
/*TODO*///	
/*TODO*///				/* Figure out which byte in the color table to use */
/*TODO*///				pos = (index2 & 0x380) >> 4;
/*TODO*///				pos += (index2 & 0x1f) >> 2;
/*TODO*///				page1 = (index2 & 0x0c00) >> 10;
/*TODO*///				address1 = 0x3c0 + pos;
/*TODO*///				color = ppu_page[page1][address1];
/*TODO*///	
/*TODO*///				/* Figure out which bits in the color table to use */
/*TODO*///				index1 = ((index2 & 0x40) >> 4) + (index2 & 0x02);
/*TODO*///	
/*TODO*///				address1 = index2 & 0x3ff;
/*TODO*///				index3 = nes_vram[(ppu_page[page1][address1] >> 6) | PPU_tile_page] + (ppu_page[page1][address1] & 0x3f);
/*TODO*///	
/*TODO*///				if (use_vram[index3])
/*TODO*///					bank = 1;
/*TODO*///				else
/*TODO*///					bank = 0;
/*TODO*///	
/*TODO*///				drawgfx(tmpbitmap, Machine.gfx[bank],
/*TODO*///					index3,
/*TODO*///					(color >> index1) & 0x03,
/*TODO*///					0,0,
/*TODO*///					32*8 + 8*sx, 30*8 + 8*sy,
/*TODO*///					0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	
/*TODO*///		/* TODO: take into account the currently selected page or one-page mode, plus scrolling */
/*TODO*///	#if 0
/*TODO*///		/* 1 */
/*TODO*///		scrollx = (Machine.drv.screen_width - PPU_Scroll_X) & 0xff;
/*TODO*///		if (PPU_Scroll_Y != 0)
/*TODO*///			scrolly = (Machine.drv.screen_height - PPU_Scroll_Y) & 0xff;
/*TODO*///		else scrolly = 0;
/*TODO*///		copyscrollbitmap (bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
/*TODO*///		/* 2 */
/*TODO*///		scrollx += Machine.drv.screen_width;
/*TODO*///		copyscrollbitmap (bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
/*TODO*///		/* 4 */
/*TODO*///		scrolly += Machine.drv.screen_height;
/*TODO*///		copyscrollbitmap (bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
/*TODO*///		/* 3 */
/*TODO*///		scrollx = (Machine.drv.screen_width - PPU_Scroll_X);
/*TODO*///		copyscrollbitmap (bitmap,tmpbitmap,1,&scrollx,1,&scrolly,&Machine.visible_area,TRANSPARENCY_NONE,0);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	#ifdef BIG_SCREEN
/*TODO*///		/* copy the character mapped graphics */
/*TODO*///		copybitmap(bitmap,tmpbitmap,0,0,0,0,&Machine.visible_area,TRANSPARENCY_NONE,0);
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* Now draw the sprites */
/*TODO*///	
/*TODO*///		/* Determine if the sprites are 8x8 or 8x16 */
/*TODO*///		Size = (PPU_Control0 & 0x20) ? 16 : 8;
/*TODO*///	
/*TODO*///		for (i = 0xfc; i >= 0; i -= 4)
/*TODO*///		{
/*TODO*///			int y, tile, index1;
/*TODO*///	
/*TODO*///	
/*TODO*///			y = spriteram.read(i)+ 1; /* TODO: is the +1 hack needed? see if PPU_Scroll_Y of 255 has any effect */
/*TODO*///	
/*TODO*///			/* If the sprite isn't visible, skip it */
/*TODO*///			if (y > BOTTOM_VISIBLE_SCANLINE) continue;
/*TODO*///	
/*TODO*///			tile = spriteram.read(i+1);
/*TODO*///	
/*TODO*///			if (Size == 16)
/*TODO*///			{
/*TODO*///				/* If it's 8x16 and odd-numbered, draw the other half instead */
/*TODO*///				if ((tile & 0x01) != 0)
/*TODO*///				{
/*TODO*///					tile &= 0xfe;
/*TODO*///					tile |= 0x100;
/*TODO*///				}
/*TODO*///				/* Note that the sprite page value has no effect on 8x16 sprites */
/*TODO*///				page = tile >> 6;
/*TODO*///			}
/*TODO*///			else page = (tile >> 6) | PPU_sprite_page;
/*TODO*///	
/*TODO*///			/* TODO: add code to draw 8x16 sprites */
/*TODO*///			index1 = nes_vram[page] + (tile & 0x3f);
/*TODO*///			{
/*TODO*///				int bank;
/*TODO*///	
/*TODO*///				if (CHR_Rom == 0)
/*TODO*///					bank = 1;
/*TODO*///				else bank = 0;
/*TODO*///	
/*TODO*///				if (spriteram.read(i+2)& 0x20)
/*TODO*///					/* Draw sprites with the priority bit set behind the background */
/*TODO*///					drawgfx (bitmap, Machine.gfx[bank],
/*TODO*///						index1,
/*TODO*///						(spriteram.read(i+2)& 0x03) + 4,
/*TODO*///						spriteram.read(i+2)& 0x40,spriteram.read(i+2)& 0x80,
/*TODO*///						spriteram.read(i+3),y,
/*TODO*///						&Machine.visible_area,TRANSPARENCY_THROUGH, PPU_background_color);
/*TODO*///				else
/*TODO*///					drawgfx (bitmap, Machine.gfx[bank],
/*TODO*///						index1,
/*TODO*///						(spriteram.read(i+2)& 0x03) + 4,
/*TODO*///						spriteram.read(i+2)& 0x40,spriteram.read(i+2)& 0x80,
/*TODO*///						spriteram.read(i+3),y,
/*TODO*///						&Machine.visible_area,TRANSPARENCY_PEN, 0);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	#endif
        }
    };
}
