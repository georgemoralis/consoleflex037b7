package mame;

/**
 * ported to 0.37b7
 */
/**
 * Changelog 23/07/2018 (shadow) - Intial work for use with mess
 */
import static arcadeflex.libc.cstring.strlen;
import static WIP.mame.mame.Machine;
import static WIP.mame.osdependH.*;
import static mame.ui_text.*;
import static mame.ui_textH.*;
import static mame.datafile.load_driver_history;
import static old.mame.inptportH.IPT_UI_CANCEL;
import static old.mame.inptportH.IPT_UI_CONFIGURE;
import static old.mame.inptportH.IPT_UI_DOWN;
import static old.mame.inptportH.IPT_UI_SELECT;
import static old.mame.inptportH.IPT_UI_UP;
import static old.mame.input.input_ui_pressed;
import static old.mame.input.input_ui_pressed_repeat;
import static old.mame.usrintrf.*;
import static old.mame.usrintrfH.*;
import static WIP.mame.mame.*;
import static mame.cheat.cheat_menu;
import static old.arcadeflex.video.osd_clearbitmap;
import static old.arcadeflex.video.osd_mark_dirty;
import static old.mame.cpuintrf.machine_reset;
import static old.mame.drawgfx.*;
import static old.mame.drawgfxH.TRANSPARENCY_NONE;

public class usrintrf {

    /*TODO*///extern int bitmap_dirty;	/* set by osd_clearbitmap() */
/*TODO*///
/*TODO*////* Variables for stat menu */
/*TODO*///extern char build_version[];
/*TODO*///extern unsigned int dispensed_tickets;
/*TODO*///extern unsigned int coins[COIN_COUNTERS];
/*TODO*///extern unsigned int coinlockedout[COIN_COUNTERS];
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int setup_selected;
/*TODO*///static int osd_selected;
/*TODO*///static int jukebox_selected;
/*TODO*///static int single_step;
/*TODO*///static int trueorientation;
/*TODO*///static int orientation_count;
/*TODO*///
/*TODO*///
/*TODO*///void switch_ui_orientation(void)
/*TODO*///{
/*TODO*///	if (orientation_count == 0)
/*TODO*///	{
/*TODO*///		trueorientation = Machine->orientation;
/*TODO*///		Machine->orientation = Machine->ui_orientation;
/*TODO*///		set_pixel_functions();
/*TODO*///	}
/*TODO*///
/*TODO*///	orientation_count++;
/*TODO*///}
/*TODO*///
/*TODO*///void switch_true_orientation(void)
/*TODO*///{
/*TODO*///	orientation_count--;
/*TODO*///
/*TODO*///	if (orientation_count == 0)
/*TODO*///	{
/*TODO*///		Machine->orientation = trueorientation;
/*TODO*///		set_pixel_functions();
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void set_ui_visarea (int xmin, int ymin, int xmax, int ymax)
/*TODO*///{
/*TODO*///	int temp,w,h;
/*TODO*///
/*TODO*///	/* special case for vectors */
/*TODO*///	if(Machine->drv->video_attributes == VIDEO_TYPE_VECTOR)
/*TODO*///	{
/*TODO*///		if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			temp=xmin; xmin=ymin; ymin=temp;
/*TODO*///			temp=xmax; xmax=ymax; ymax=temp;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			w = Machine->drv->screen_height;
/*TODO*///			h = Machine->drv->screen_width;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			w = Machine->drv->screen_width;
/*TODO*///			h = Machine->drv->screen_height;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->ui_orientation & ORIENTATION_FLIP_X)
/*TODO*///		{
/*TODO*///			temp = w - xmin - 1;
/*TODO*///			xmin = w - xmax - 1;
/*TODO*///			xmax = temp ;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->ui_orientation & ORIENTATION_FLIP_Y)
/*TODO*///		{
/*TODO*///			temp = h - ymin - 1;
/*TODO*///			ymin = h - ymax - 1;
/*TODO*///			ymax = temp;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->ui_orientation & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			temp = xmin; xmin = ymin; ymin = temp;
/*TODO*///			temp = xmax; xmax = ymax; ymax = temp;
/*TODO*///		}
/*TODO*///
/*TODO*///	}
/*TODO*///	Machine->uiwidth = xmax-xmin+1;
/*TODO*///	Machine->uiheight = ymax-ymin+1;
/*TODO*///	Machine->uixmin = xmin;
/*TODO*///	Machine->uiymin = ymin;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///struct GfxElement *builduifont(void)
/*TODO*///{
/*TODO*///    static unsigned char fontdata6x8[] =
/*TODO*///	{
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x7c,0x80,0x98,0x90,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x44,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x98,0x88,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x24,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x88,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x24,0x64,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x90,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x44,0x64,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x30,0x48,0x84,0xb4,0xb4,0x84,0x48,0x30,0x30,0x48,0x84,0x84,0x84,0x84,0x48,0x30,
/*TODO*///		0x00,0xfc,0x84,0x8c,0xd4,0xa4,0xfc,0x00,0x00,0xfc,0x84,0x84,0x84,0x84,0xfc,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
/*TODO*///		0x80,0xc0,0xe0,0xf0,0xe0,0xc0,0x80,0x00,0x04,0x0c,0x1c,0x3c,0x1c,0x0c,0x04,0x00,
/*TODO*///		0x20,0x70,0xf8,0x20,0x20,0xf8,0x70,0x20,0x48,0x48,0x48,0x48,0x48,0x00,0x48,0x00,
/*TODO*///		0x00,0x00,0x30,0x68,0x78,0x30,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
/*TODO*///		0x70,0xd8,0xe8,0xe8,0xf8,0xf8,0x70,0x00,0x1c,0x7c,0x74,0x44,0x44,0x4c,0xcc,0xc0,
/*TODO*///		0x20,0x70,0xf8,0x70,0x70,0x70,0x70,0x00,0x70,0x70,0x70,0x70,0xf8,0x70,0x20,0x00,
/*TODO*///		0x00,0x10,0xf8,0xfc,0xf8,0x10,0x00,0x00,0x00,0x20,0x7c,0xfc,0x7c,0x20,0x00,0x00,
/*TODO*///		0xb0,0x54,0xb8,0xb8,0x54,0xb0,0x00,0x00,0x00,0x28,0x6c,0xfc,0x6c,0x28,0x00,0x00,
/*TODO*///		0x00,0x30,0x30,0x78,0x78,0xfc,0x00,0x00,0xfc,0x78,0x78,0x30,0x30,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x20,0x20,0x20,0x20,0x00,0x20,0x00,
/*TODO*///		0x50,0x50,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x50,0xf8,0x50,0xf8,0x50,0x00,0x00,
/*TODO*///		0x20,0x70,0xc0,0x70,0x18,0xf0,0x20,0x00,0x40,0xa4,0x48,0x10,0x20,0x48,0x94,0x08,
/*TODO*///		0x60,0x90,0xa0,0x40,0xa8,0x90,0x68,0x00,0x10,0x20,0x40,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x20,0x40,0x40,0x40,0x40,0x40,0x20,0x00,0x10,0x08,0x08,0x08,0x08,0x08,0x10,0x00,
/*TODO*///		0x20,0xa8,0x70,0xf8,0x70,0xa8,0x20,0x00,0x00,0x20,0x20,0xf8,0x20,0x20,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x60,0x00,0x00,0x00,0xf8,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x00,0x00,0x08,0x10,0x20,0x40,0x80,0x00,0x00,
/*TODO*///		0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,0x10,0x30,0x10,0x10,0x10,0x10,0x10,0x00,
/*TODO*///		0x70,0x88,0x08,0x10,0x20,0x40,0xf8,0x00,0x70,0x88,0x08,0x30,0x08,0x88,0x70,0x00,
/*TODO*///		0x10,0x30,0x50,0x90,0xf8,0x10,0x10,0x00,0xf8,0x80,0xf0,0x08,0x08,0x88,0x70,0x00,
/*TODO*///		0x70,0x80,0xf0,0x88,0x88,0x88,0x70,0x00,0xf8,0x08,0x08,0x10,0x20,0x20,0x20,0x00,
/*TODO*///		0x70,0x88,0x88,0x70,0x88,0x88,0x70,0x00,0x70,0x88,0x88,0x88,0x78,0x08,0x70,0x00,
/*TODO*///		0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x00,0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x60,
/*TODO*///		0x10,0x20,0x40,0x80,0x40,0x20,0x10,0x00,0x00,0x00,0xf8,0x00,0xf8,0x00,0x00,0x00,
/*TODO*///		0x40,0x20,0x10,0x08,0x10,0x20,0x40,0x00,0x70,0x88,0x08,0x10,0x20,0x00,0x20,0x00,
/*TODO*///		0x30,0x48,0x94,0xa4,0xa4,0x94,0x48,0x30,0x70,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x88,0x88,0xf0,0x00,0x70,0x88,0x80,0x80,0x80,0x88,0x70,0x00,
/*TODO*///		0xf0,0x88,0x88,0x88,0x88,0x88,0xf0,0x00,0xf8,0x80,0x80,0xf0,0x80,0x80,0xf8,0x00,
/*TODO*///		0xf8,0x80,0x80,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x80,0x98,0x88,0x88,0x70,0x00,
/*TODO*///		0x88,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,
/*TODO*///		0x08,0x08,0x08,0x08,0x88,0x88,0x70,0x00,0x88,0x90,0xa0,0xc0,0xa0,0x90,0x88,0x00,
/*TODO*///		0x80,0x80,0x80,0x80,0x80,0x80,0xf8,0x00,0x88,0xd8,0xa8,0x88,0x88,0x88,0x88,0x00,
/*TODO*///		0x88,0xc8,0xa8,0x98,0x88,0x88,0x88,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x08,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x88,0x88,0x88,0x00,0x70,0x88,0x80,0x70,0x08,0x88,0x70,0x00,
/*TODO*///		0xf8,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x88,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x88,0x88,0x88,0x88,0x88,0x50,0x20,0x00,0x88,0x88,0x88,0x88,0xa8,0xd8,0x88,0x00,
/*TODO*///		0x88,0x50,0x20,0x20,0x20,0x50,0x88,0x00,0x88,0x88,0x88,0x50,0x20,0x20,0x20,0x00,
/*TODO*///		0xf8,0x08,0x10,0x20,0x40,0x80,0xf8,0x00,0x30,0x20,0x20,0x20,0x20,0x20,0x30,0x00,
/*TODO*///		0x40,0x40,0x20,0x20,0x10,0x10,0x08,0x08,0x30,0x10,0x10,0x10,0x10,0x10,0x30,0x00,
/*TODO*///		0x20,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xfc,
/*TODO*///		0x40,0x20,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x70,0x08,0x78,0x88,0x78,0x00,
/*TODO*///		0x80,0x80,0xf0,0x88,0x88,0x88,0xf0,0x00,0x00,0x00,0x70,0x88,0x80,0x80,0x78,0x00,
/*TODO*///		0x08,0x08,0x78,0x88,0x88,0x88,0x78,0x00,0x00,0x00,0x70,0x88,0xf8,0x80,0x78,0x00,
/*TODO*///		0x18,0x20,0x70,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x70,
/*TODO*///		0x80,0x80,0xf0,0x88,0x88,0x88,0x88,0x00,0x20,0x00,0x20,0x20,0x20,0x20,0x20,0x00,
/*TODO*///		0x20,0x00,0x20,0x20,0x20,0x20,0x20,0xc0,0x80,0x80,0x90,0xa0,0xe0,0x90,0x88,0x00,
/*TODO*///		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0xf0,0xa8,0xa8,0xa8,0xa8,0x00,
/*TODO*///		0x00,0x00,0xb0,0xc8,0x88,0x88,0x88,0x00,0x00,0x00,0x70,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x00,0x00,0xf0,0x88,0x88,0xf0,0x80,0x80,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x08,
/*TODO*///		0x00,0x00,0xb0,0xc8,0x80,0x80,0x80,0x00,0x00,0x00,0x78,0x80,0x70,0x08,0xf0,0x00,
/*TODO*///		0x20,0x20,0x70,0x20,0x20,0x20,0x18,0x00,0x00,0x00,0x88,0x88,0x88,0x98,0x68,0x00,
/*TODO*///		0x00,0x00,0x88,0x88,0x88,0x50,0x20,0x00,0x00,0x00,0xa8,0xa8,0xa8,0xa8,0x50,0x00,
/*TODO*///		0x00,0x00,0x88,0x50,0x20,0x50,0x88,0x00,0x00,0x00,0x88,0x88,0x88,0x78,0x08,0x70,
/*TODO*///		0x00,0x00,0xf8,0x10,0x20,0x40,0xf8,0x00,0x08,0x10,0x10,0x20,0x10,0x10,0x08,0x00,
/*TODO*///		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x40,0x20,0x20,0x10,0x20,0x20,0x40,0x00,
/*TODO*///		0x00,0x68,0xb0,0x00,0x00,0x00,0x00,0x00,0x20,0x50,0x20,0x50,0xa8,0x50,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x20,0x20,0x40,0x0C,0x10,0x38,0x10,0x20,0x20,0xC0,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x28,0x28,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0xA8,0x00,
/*TODO*///		0x70,0xA8,0xF8,0x20,0x20,0x20,0x20,0x00,0x70,0xA8,0xF8,0x20,0x20,0xF8,0xA8,0x70,
/*TODO*///		0x20,0x50,0x88,0x00,0x00,0x00,0x00,0x00,0x44,0xA8,0x50,0x20,0x68,0xD4,0x28,0x00,
/*TODO*///		0x88,0x70,0x88,0x60,0x30,0x88,0x70,0x00,0x00,0x10,0x20,0x40,0x20,0x10,0x00,0x00,
/*TODO*///		0x78,0xA0,0xA0,0xB0,0xA0,0xA0,0x78,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x10,0x20,0x20,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x10,0x10,0x20,0x00,0x00,0x00,0x00,0x00,0x28,0x50,0x50,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x28,0x28,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x30,0x78,0x78,0x30,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x78,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xFC,0x00,0x00,0x00,0x00,
/*TODO*///		0x68,0xB0,0x00,0x00,0x00,0x00,0x00,0x00,0xF4,0x5C,0x54,0x54,0x00,0x00,0x00,0x00,
/*TODO*///		0x88,0x70,0x78,0x80,0x70,0x08,0xF0,0x00,0x00,0x40,0x20,0x10,0x20,0x40,0x00,0x00,
/*TODO*///		0x00,0x00,0x70,0xA8,0xB8,0xA0,0x78,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x50,0x88,0x88,0x50,0x20,0x20,0x20,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x00,0x20,0x20,0x20,0x20,0x20,0x00,
/*TODO*///		0x00,0x20,0x70,0xA8,0xA0,0xA8,0x70,0x20,0x30,0x48,0x40,0xE0,0x40,0x48,0xF0,0x00,
/*TODO*///		0x00,0x48,0x30,0x48,0x48,0x30,0x48,0x00,0x88,0x88,0x50,0xF8,0x20,0xF8,0x20,0x00,
/*TODO*///		0x20,0x20,0x20,0x00,0x20,0x20,0x20,0x00,0x78,0x80,0x70,0x88,0x70,0x08,0xF0,0x00,
/*TODO*///		0xD8,0xD8,0x00,0x00,0x00,0x00,0x00,0x00,0x30,0x48,0x94,0xA4,0xA4,0x94,0x48,0x30,
/*TODO*///		0x60,0x10,0x70,0x90,0x70,0x00,0x00,0x00,0x00,0x28,0x50,0xA0,0x50,0x28,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0xF8,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x78,0x00,0x00,0x00,0x00,
/*TODO*///		0x30,0x48,0xB4,0xB4,0xA4,0xB4,0x48,0x30,0x7C,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x60,0x90,0x90,0x60,0x00,0x00,0x00,0x00,0x20,0x20,0xF8,0x20,0x20,0x00,0xF8,0x00,
/*TODO*///		0x60,0x90,0x20,0x40,0xF0,0x00,0x00,0x00,0x60,0x90,0x20,0x90,0x60,0x00,0x00,0x00,
/*TODO*///		0x10,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x88,0x88,0x88,0xC8,0xB0,0x80,
/*TODO*///		0x78,0xD0,0xD0,0xD0,0x50,0x50,0x50,0x00,0x00,0x00,0x00,0x30,0x30,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x10,0x20,0x00,0x20,0x60,0x20,0x20,0x70,0x00,0x00,0x00,
/*TODO*///		0x20,0x50,0x20,0x00,0x00,0x00,0x00,0x00,0x00,0xA0,0x50,0x28,0x50,0xA0,0x00,0x00,
/*TODO*///		0x40,0x48,0x50,0x28,0x58,0xA8,0x38,0x08,0x40,0x48,0x50,0x28,0x44,0x98,0x20,0x3C,
/*TODO*///		0xC0,0x28,0xD0,0x28,0xD8,0xA8,0x38,0x08,0x20,0x00,0x20,0x40,0x80,0x88,0x70,0x00,
/*TODO*///		0x40,0x20,0x70,0x88,0xF8,0x88,0x88,0x00,0x10,0x20,0x70,0x88,0xF8,0x88,0x88,0x00,
/*TODO*///		0x70,0x00,0x70,0x88,0xF8,0x88,0x88,0x00,0x68,0xB0,0x70,0x88,0xF8,0x88,0x88,0x00,
/*TODO*///		0x50,0x00,0x70,0x88,0xF8,0x88,0x88,0x00,0x20,0x50,0x70,0x88,0xF8,0x88,0x88,0x00,
/*TODO*///		0x78,0xA0,0xA0,0xF0,0xA0,0xA0,0xB8,0x00,0x70,0x88,0x80,0x80,0x88,0x70,0x08,0x70,
/*TODO*///		0x40,0x20,0xF8,0x80,0xF0,0x80,0xF8,0x00,0x10,0x20,0xF8,0x80,0xF0,0x80,0xF8,0x00,
/*TODO*///		0x70,0x00,0xF8,0x80,0xF0,0x80,0xF8,0x00,0x50,0x00,0xF8,0x80,0xF0,0x80,0xF8,0x00,
/*TODO*///		0x40,0x20,0x70,0x20,0x20,0x20,0x70,0x00,0x10,0x20,0x70,0x20,0x20,0x20,0x70,0x00,
/*TODO*///		0x70,0x00,0x70,0x20,0x20,0x20,0x70,0x00,0x50,0x00,0x70,0x20,0x20,0x20,0x70,0x00,
/*TODO*///		0x70,0x48,0x48,0xE8,0x48,0x48,0x70,0x00,0x68,0xB0,0x88,0xC8,0xA8,0x98,0x88,0x00,
/*TODO*///		0x40,0x20,0x70,0x88,0x88,0x88,0x70,0x00,0x10,0x20,0x70,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x70,0x00,0x70,0x88,0x88,0x88,0x70,0x00,0x68,0xB0,0x70,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x50,0x00,0x70,0x88,0x88,0x88,0x70,0x00,0x00,0x88,0x50,0x20,0x50,0x88,0x00,0x00,
/*TODO*///		0x00,0x74,0x88,0x90,0xA8,0x48,0xB0,0x00,0x40,0x20,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x10,0x20,0x88,0x88,0x88,0x88,0x70,0x00,0x70,0x00,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x50,0x00,0x88,0x88,0x88,0x88,0x70,0x00,0x10,0xA8,0x88,0x50,0x20,0x20,0x20,0x00,
/*TODO*///		0x00,0x80,0xF0,0x88,0x88,0xF0,0x80,0x80,0x60,0x90,0x90,0xB0,0x88,0x88,0xB0,0x00,
/*TODO*///		0x40,0x20,0x70,0x08,0x78,0x88,0x78,0x00,0x10,0x20,0x70,0x08,0x78,0x88,0x78,0x00,
/*TODO*///		0x70,0x00,0x70,0x08,0x78,0x88,0x78,0x00,0x68,0xB0,0x70,0x08,0x78,0x88,0x78,0x00,
/*TODO*///		0x50,0x00,0x70,0x08,0x78,0x88,0x78,0x00,0x20,0x50,0x70,0x08,0x78,0x88,0x78,0x00,
/*TODO*///		0x00,0x00,0xF0,0x28,0x78,0xA0,0x78,0x00,0x00,0x00,0x70,0x88,0x80,0x78,0x08,0x70,
/*TODO*///		0x40,0x20,0x70,0x88,0xF8,0x80,0x70,0x00,0x10,0x20,0x70,0x88,0xF8,0x80,0x70,0x00,
/*TODO*///		0x70,0x00,0x70,0x88,0xF8,0x80,0x70,0x00,0x50,0x00,0x70,0x88,0xF8,0x80,0x70,0x00,
/*TODO*///		0x40,0x20,0x00,0x60,0x20,0x20,0x70,0x00,0x10,0x20,0x00,0x60,0x20,0x20,0x70,0x00,
/*TODO*///		0x20,0x50,0x00,0x60,0x20,0x20,0x70,0x00,0x50,0x00,0x00,0x60,0x20,0x20,0x70,0x00,
/*TODO*///		0x50,0x60,0x10,0x78,0x88,0x88,0x70,0x00,0x68,0xB0,0x00,0xF0,0x88,0x88,0x88,0x00,
/*TODO*///		0x40,0x20,0x00,0x70,0x88,0x88,0x70,0x00,0x10,0x20,0x00,0x70,0x88,0x88,0x70,0x00,
/*TODO*///		0x20,0x50,0x00,0x70,0x88,0x88,0x70,0x00,0x68,0xB0,0x00,0x70,0x88,0x88,0x70,0x00,
/*TODO*///		0x00,0x50,0x00,0x70,0x88,0x88,0x70,0x00,0x00,0x20,0x00,0xF8,0x00,0x20,0x00,0x00,
/*TODO*///		0x00,0x00,0x68,0x90,0xA8,0x48,0xB0,0x00,0x40,0x20,0x88,0x88,0x88,0x98,0x68,0x00,
/*TODO*///		0x10,0x20,0x88,0x88,0x88,0x98,0x68,0x00,0x70,0x00,0x88,0x88,0x88,0x98,0x68,0x00,
/*TODO*///		0x50,0x00,0x88,0x88,0x88,0x98,0x68,0x00,0x10,0x20,0x88,0x88,0x88,0x78,0x08,0x70,
/*TODO*///		0x80,0xF0,0x88,0x88,0xF0,0x80,0x80,0x80,0x50,0x00,0x88,0x88,0x88,0x78,0x08,0x70
/*TODO*///    };
/*TODO*///#if 0
/*TODO*///	static unsigned char fontdata6x8[] =
/*TODO*///	{
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x7c,0x80,0x98,0x90,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x44,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x98,0x88,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x64,0x24,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x88,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x24,0x64,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x7c,0x80,0x90,0x98,0x80,0xbc,0x80,0x7c,0xf8,0x04,0x44,0x64,0x04,0xf4,0x04,0xf8,
/*TODO*///		0x30,0x48,0x84,0xb4,0xb4,0x84,0x48,0x30,0x30,0x48,0x84,0x84,0x84,0x84,0x48,0x30,
/*TODO*///		0x00,0xfc,0x84,0x8c,0xd4,0xa4,0xfc,0x00,0x00,0xfc,0x84,0x84,0x84,0x84,0xfc,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
/*TODO*///		0x80,0xc0,0xe0,0xf0,0xe0,0xc0,0x80,0x00,0x04,0x0c,0x1c,0x3c,0x1c,0x0c,0x04,0x00,
/*TODO*///		0x20,0x70,0xf8,0x20,0x20,0xf8,0x70,0x20,0x48,0x48,0x48,0x48,0x48,0x00,0x48,0x00,
/*TODO*///		0x00,0x00,0x30,0x68,0x78,0x30,0x00,0x00,0x00,0x30,0x68,0x78,0x78,0x30,0x00,0x00,
/*TODO*///		0x70,0xd8,0xe8,0xe8,0xf8,0xf8,0x70,0x00,0x1c,0x7c,0x74,0x44,0x44,0x4c,0xcc,0xc0,
/*TODO*///		0x20,0x70,0xf8,0x70,0x70,0x70,0x70,0x00,0x70,0x70,0x70,0x70,0xf8,0x70,0x20,0x00,
/*TODO*///		0x00,0x10,0xf8,0xfc,0xf8,0x10,0x00,0x00,0x00,0x20,0x7c,0xfc,0x7c,0x20,0x00,0x00,
/*TODO*///		0xb0,0x54,0xb8,0xb8,0x54,0xb0,0x00,0x00,0x00,0x28,0x6c,0xfc,0x6c,0x28,0x00,0x00,
/*TODO*///		0x00,0x30,0x30,0x78,0x78,0xfc,0x00,0x00,0xfc,0x78,0x78,0x30,0x30,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x20,0x20,0x20,0x20,0x00,0x20,0x00,
/*TODO*///		0x50,0x50,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x50,0xf8,0x50,0xf8,0x50,0x00,0x00,
/*TODO*///		0x20,0x70,0xc0,0x70,0x18,0xf0,0x20,0x00,0x40,0xa4,0x48,0x10,0x20,0x48,0x94,0x08,
/*TODO*///		0x60,0x90,0xa0,0x40,0xa8,0x90,0x68,0x00,0x10,0x20,0x40,0x00,0x00,0x00,0x00,0x00,
/*TODO*///		0x20,0x40,0x40,0x40,0x40,0x40,0x20,0x00,0x10,0x08,0x08,0x08,0x08,0x08,0x10,0x00,
/*TODO*///		0x20,0xa8,0x70,0xf8,0x70,0xa8,0x20,0x00,0x00,0x20,0x20,0xf8,0x20,0x20,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x60,0x00,0x00,0x00,0xf8,0x00,0x00,0x00,0x00,
/*TODO*///		0x00,0x00,0x00,0x00,0x00,0x30,0x30,0x00,0x00,0x08,0x10,0x20,0x40,0x80,0x00,0x00,
/*TODO*///		0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,0x10,0x30,0x10,0x10,0x10,0x10,0x10,0x00,
/*TODO*///		0x70,0x88,0x08,0x10,0x20,0x40,0xf8,0x00,0x70,0x88,0x08,0x30,0x08,0x88,0x70,0x00,
/*TODO*///		0x10,0x30,0x50,0x90,0xf8,0x10,0x10,0x00,0xf8,0x80,0xf0,0x08,0x08,0x88,0x70,0x00,
/*TODO*///		0x70,0x80,0xf0,0x88,0x88,0x88,0x70,0x00,0xf8,0x08,0x08,0x10,0x20,0x20,0x20,0x00,
/*TODO*///		0x70,0x88,0x88,0x70,0x88,0x88,0x70,0x00,0x70,0x88,0x88,0x88,0x78,0x08,0x70,0x00,
/*TODO*///		0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x00,0x00,0x00,0x30,0x30,0x00,0x30,0x30,0x60,
/*TODO*///		0x10,0x20,0x40,0x80,0x40,0x20,0x10,0x00,0x00,0x00,0xf8,0x00,0xf8,0x00,0x00,0x00,
/*TODO*///		0x40,0x20,0x10,0x08,0x10,0x20,0x40,0x00,0x70,0x88,0x08,0x10,0x20,0x00,0x20,0x00,
/*TODO*///		0x30,0x48,0x94,0xa4,0xa4,0x94,0x48,0x30,0x70,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x88,0x88,0xf0,0x00,0x70,0x88,0x80,0x80,0x80,0x88,0x70,0x00,
/*TODO*///		0xf0,0x88,0x88,0x88,0x88,0x88,0xf0,0x00,0xf8,0x80,0x80,0xf0,0x80,0x80,0xf8,0x00,
/*TODO*///		0xf8,0x80,0x80,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x80,0x98,0x88,0x88,0x70,0x00,
/*TODO*///		0x88,0x88,0x88,0xf8,0x88,0x88,0x88,0x00,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,
/*TODO*///		0x08,0x08,0x08,0x08,0x88,0x88,0x70,0x00,0x88,0x90,0xa0,0xc0,0xa0,0x90,0x88,0x00,
/*TODO*///		0x80,0x80,0x80,0x80,0x80,0x80,0xf8,0x00,0x88,0xd8,0xa8,0x88,0x88,0x88,0x88,0x00,
/*TODO*///		0x88,0xc8,0xa8,0x98,0x88,0x88,0x88,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x80,0x80,0x80,0x00,0x70,0x88,0x88,0x88,0x88,0x88,0x70,0x08,
/*TODO*///		0xf0,0x88,0x88,0xf0,0x88,0x88,0x88,0x00,0x70,0x88,0x80,0x70,0x08,0x88,0x70,0x00,
/*TODO*///		0xf8,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x88,0x88,0x88,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x88,0x88,0x88,0x88,0x88,0x50,0x20,0x00,0x88,0x88,0x88,0x88,0xa8,0xd8,0x88,0x00,
/*TODO*///		0x88,0x50,0x20,0x20,0x20,0x50,0x88,0x00,0x88,0x88,0x88,0x50,0x20,0x20,0x20,0x00,
/*TODO*///		0xf8,0x08,0x10,0x20,0x40,0x80,0xf8,0x00,0x30,0x20,0x20,0x20,0x20,0x20,0x30,0x00,
/*TODO*///		0x40,0x40,0x20,0x20,0x10,0x10,0x08,0x08,0x30,0x10,0x10,0x10,0x10,0x10,0x30,0x00,
/*TODO*///		0x20,0x50,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0xfc,
/*TODO*///		0x40,0x20,0x10,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x70,0x08,0x78,0x88,0x78,0x00,
/*TODO*///		0x80,0x80,0xf0,0x88,0x88,0x88,0xf0,0x00,0x00,0x00,0x70,0x88,0x80,0x80,0x78,0x00,
/*TODO*///		0x08,0x08,0x78,0x88,0x88,0x88,0x78,0x00,0x00,0x00,0x70,0x88,0xf8,0x80,0x78,0x00,
/*TODO*///		0x18,0x20,0x70,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x70,
/*TODO*///		0x80,0x80,0xf0,0x88,0x88,0x88,0x88,0x00,0x20,0x00,0x20,0x20,0x20,0x20,0x20,0x00,
/*TODO*///		0x20,0x00,0x20,0x20,0x20,0x20,0x20,0xc0,0x80,0x80,0x90,0xa0,0xe0,0x90,0x88,0x00,
/*TODO*///		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x00,0x00,0x00,0xf0,0xa8,0xa8,0xa8,0xa8,0x00,
/*TODO*///		0x00,0x00,0xb0,0xc8,0x88,0x88,0x88,0x00,0x00,0x00,0x70,0x88,0x88,0x88,0x70,0x00,
/*TODO*///		0x00,0x00,0xf0,0x88,0x88,0xf0,0x80,0x80,0x00,0x00,0x78,0x88,0x88,0x78,0x08,0x08,
/*TODO*///		0x00,0x00,0xb0,0xc8,0x80,0x80,0x80,0x00,0x00,0x00,0x78,0x80,0x70,0x08,0xf0,0x00,
/*TODO*///		0x20,0x20,0x70,0x20,0x20,0x20,0x18,0x00,0x00,0x00,0x88,0x88,0x88,0x98,0x68,0x00,
/*TODO*///		0x00,0x00,0x88,0x88,0x88,0x50,0x20,0x00,0x00,0x00,0xa8,0xa8,0xa8,0xa8,0x50,0x00,
/*TODO*///		0x00,0x00,0x88,0x50,0x20,0x50,0x88,0x00,0x00,0x00,0x88,0x88,0x88,0x78,0x08,0x70,
/*TODO*///		0x00,0x00,0xf8,0x10,0x20,0x40,0xf8,0x00,0x08,0x10,0x10,0x20,0x10,0x10,0x08,0x00,
/*TODO*///		0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x40,0x20,0x20,0x10,0x20,0x20,0x40,0x00,
/*TODO*///		0x00,0x68,0xb0,0x00,0x00,0x00,0x00,0x00,0x20,0x50,0x20,0x50,0xa8,0x50,0x00,0x00,
/*TODO*///	};
/*TODO*///#endif
/*TODO*///
/*TODO*///	static struct GfxLayout fontlayout6x8 =
/*TODO*///	{
/*TODO*///		6,8,	/* 6*8 characters */
/*TODO*///		256,	/* 256 characters */
/*TODO*///		1,	/* 1 bit per pixel */
/*TODO*///		{ 0 },
/*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7 }, /* straightforward layout */
/*TODO*///		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
/*TODO*///		8*8 /* every char takes 8 consecutive bytes */
/*TODO*///	};
/*TODO*///	static struct GfxLayout fontlayout12x8 =
/*TODO*///	{
/*TODO*///		12,8,	/* 12*8 characters */
/*TODO*///		256,	/* 256 characters */
/*TODO*///		1,	/* 1 bit per pixel */
/*TODO*///		{ 0 },
/*TODO*///		{ 0,0, 1,1, 2,2, 3,3, 4,4, 5,5, 6,6, 7,7 }, /* straightforward layout */
/*TODO*///		{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
/*TODO*///		8*8 /* every char takes 8 consecutive bytes */
/*TODO*///	};
/*TODO*///	static struct GfxLayout fontlayout6x16 =
/*TODO*///	{
/*TODO*///		6,16,	/* 6*8 characters */
/*TODO*///		256,	/* 256 characters */
/*TODO*///		1,	/* 1 bit per pixel */
/*TODO*///		{ 0 },
/*TODO*///		{ 0, 1, 2, 3, 4, 5, 6, 7 }, /* straightforward layout */
/*TODO*///		{ 0*8,0*8, 1*8,1*8, 2*8,2*8, 3*8,3*8, 4*8,4*8, 5*8,5*8, 6*8,6*8, 7*8,7*8 },
/*TODO*///		8*8 /* every char takes 8 consecutive bytes */
/*TODO*///	};
/*TODO*///	static struct GfxLayout fontlayout12x16 =
/*TODO*///	{
/*TODO*///		12,16,	/* 12*16 characters */
/*TODO*///		256,	/* 256 characters */
/*TODO*///		1,	/* 1 bit per pixel */
/*TODO*///		{ 0 },
/*TODO*///		{ 0,0, 1,1, 2,2, 3,3, 4,4, 5,5, 6,6, 7,7 }, /* straightforward layout */
/*TODO*///		{ 0*8,0*8, 1*8,1*8, 2*8,2*8, 3*8,3*8, 4*8,4*8, 5*8,5*8, 6*8,6*8, 7*8,7*8 },
/*TODO*///		8*8 /* every char takes 8 consecutive bytes */
/*TODO*///	};
/*TODO*///
/*TODO*///	struct GfxElement *font;
/*TODO*///	static unsigned short colortable[2*2];	/* ASG 980209 */
/*TODO*///
/*TODO*///
/*TODO*///	switch_ui_orientation();
/*TODO*///
/*TODO*///	if ((Machine->drv->video_attributes & VIDEO_PIXEL_ASPECT_RATIO_MASK)
/*TODO*///			== VIDEO_PIXEL_ASPECT_RATIO_1_2)
/*TODO*///	{
/*TODO*///		if (Machine->gamedrv->flags & ORIENTATION_SWAP_XY)
/*TODO*///		{
/*TODO*///			font = decodegfx(fontdata6x8,&fontlayout6x16);
/*TODO*///			Machine->uifontwidth = 6;
/*TODO*///			Machine->uifontheight = 16;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			font = decodegfx(fontdata6x8,&fontlayout12x8);
/*TODO*///			Machine->uifontwidth = 12;
/*TODO*///			Machine->uifontheight = 8;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else if (Machine->uiwidth >= 420 && Machine->uiheight >= 420)
/*TODO*///	{
/*TODO*///		font = decodegfx(fontdata6x8,&fontlayout12x16);
/*TODO*///		Machine->uifontwidth = 12;
/*TODO*///		Machine->uifontheight = 16;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		font = decodegfx(fontdata6x8,&fontlayout6x8);
/*TODO*///		Machine->uifontwidth = 6;
/*TODO*///		Machine->uifontheight = 8;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (font)
/*TODO*///	{
/*TODO*///		/* colortable will be set at run time */
/*TODO*///		memset(colortable,0,sizeof(colortable));
/*TODO*///		font->colortable = colortable;
/*TODO*///		font->total_colors = 2;
/*TODO*///	}
/*TODO*///
/*TODO*///	switch_true_orientation();
/*TODO*///
/*TODO*///	return font;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
    /**
     * *************************************************************************
     *
     * Display text on the screen. If erase is 0, it superimposes the text on
     * the last frame displayed.
     *
     **************************************************************************
     */
    public static void displaytext(osd_bitmap bitmap, DisplayText[] dta, int erase, int update_screen) {
        if (erase != 0) {
            osd_clearbitmap(bitmap);
        }

        switch_ui_orientation();

        osd_mark_dirty(0, 0, Machine.uiwidth - 1, Machine.uiheight - 1, 1);
        /* ASG 971011 */
        for (DisplayText dt : dta) {
            if (dt.text != null) {
                int x, y;
                int c;

                x = dt.x;
                y = dt.y;
                c = 0;//dt.text;

                while (c < dt.text.length() && dt.text.charAt(c) != '\0') {
                    boolean wrapped = false;

                    if (dt.text.charAt(c) == '\n') {
                        x = dt.x;
                        y += Machine.uifontheight + 1;
                        wrapped = true;
                    } else if (dt.text.charAt(c) == ' ') {
                        /* don't try to word wrap at the beginning of a line (this would cause */
 /* an endless loop if a word is longer than a line) */
                        if (x != dt.x) {
                            int nextlen = 0;
                            int nc;

                            nc = c + 1;
                            while (nc < dt.text.length() && dt.text.charAt(nc) != '\0' && dt.text.charAt(nc) != ' ' && dt.text.charAt(nc) != '\n') {
                                nextlen += Machine.uifontwidth;
                                nc++;
                            }

                            /* word wrap */
                            if (x + Machine.uifontwidth + nextlen > Machine.uiwidth) {
                                x = dt.x;
                                y += Machine.uifontheight + 1;
                                wrapped = true;
                            }
                        }
                    }

                    if (!wrapped) {
                        drawgfx(bitmap, Machine.uifont, dt.text.charAt(c), dt.color, 0, 0, x + Machine.uixmin, y + Machine.uiymin, null, TRANSPARENCY_NONE, 0);
                        x += Machine.uifontwidth;
                    }

                    c++;
                }
            } else {
                break;
            }
        }
        switch_true_orientation();

        if (update_screen != 0) {
            update_video_and_audio();
        }
    }

    /*TODO*///
/*TODO*////* Writes messages on the screen. */
/*TODO*///static void ui_text_ex(struct osd_bitmap *bitmap,const char* buf_begin, const char* buf_end, int x, int y, int color)
/*TODO*///{
/*TODO*///	switch_ui_orientation();
/*TODO*///
/*TODO*///	for (;buf_begin != buf_end; ++buf_begin)
/*TODO*///	{
/*TODO*///		drawgfx(bitmap,Machine->uifont,*buf_begin,color,0,0,
/*TODO*///				x + Machine->uixmin,
/*TODO*///				y + Machine->uiymin, 0,TRANSPARENCY_NONE,0);
/*TODO*///		x += Machine->uifontwidth;
/*TODO*///	}
/*TODO*///
/*TODO*///	switch_true_orientation();
/*TODO*///}
/*TODO*///
/*TODO*////* Writes messages on the screen. */
/*TODO*///void ui_text(struct osd_bitmap *bitmap,const char *buf,int x,int y)
/*TODO*///{
/*TODO*///	ui_text_ex(bitmap, buf, buf + strlen(buf), x, y, UI_COLOR_NORMAL);
/*TODO*///}
/*TODO*///
/*TODO*///
    public static void ui_drawbox(osd_bitmap bitmap, int leftx, int topy, int width, int height) {
        char black, white;

        switch_ui_orientation();

        if (leftx < 0) {
            leftx = 0;
        }
        if (topy < 0) {
            topy = 0;
        }
        if (width > Machine.uiwidth) {
            width = Machine.uiwidth;
        }
        if (height > Machine.uiheight) {
            height = Machine.uiheight;
        }

        leftx += Machine.uixmin;
        topy += Machine.uiymin;

        black = Machine.uifont.colortable.read(0);
        white = Machine.uifont.colortable.read(1);

        plot_box.handler(bitmap, leftx, topy, width, 1, white);
        plot_box.handler(bitmap, leftx, topy + height - 1, width, 1, white);
        plot_box.handler(bitmap, leftx, topy, 1, height, white);
        plot_box.handler(bitmap, leftx + width - 1, topy, 1, height, white);
        plot_box.handler(bitmap, leftx + 1, topy + 1, width - 2, height - 2, black);

        switch_true_orientation();
    }

    public static void drawbar(osd_bitmap bitmap, int leftx, int topy, int width, int height, int percentage, int default_percentage) {
        char black, white;

        switch_ui_orientation();

        if (leftx < 0) {
            leftx = 0;
        }
        if (topy < 0) {
            topy = 0;
        }
        if (width > Machine.uiwidth) {
            width = Machine.uiwidth;
        }
        if (height > Machine.uiheight) {
            height = Machine.uiheight;
        }

        leftx += Machine.uixmin;
        topy += Machine.uiymin;

        black = Machine.uifont.colortable.read(0);
        white = Machine.uifont.colortable.read(1);

        plot_box.handler(bitmap, leftx + (width - 1) * default_percentage / 100, topy, 1, height / 8, white);

        plot_box.handler(bitmap, leftx, topy + height / 8, width, 1, white);

        plot_box.handler(bitmap, leftx, topy + height / 8, 1 + (width - 1) * percentage / 100, height - 2 * (height / 8), white);

        plot_box.handler(bitmap, leftx, topy + height - height / 8 - 1, width, 1, white);

        plot_box.handler(bitmap, leftx + (width - 1) * default_percentage / 100, topy + height - height / 8, 1, height / 8, white);

        switch_true_orientation();
    }

    /*TODO*///
/*TODO*////* Extract one line from a multiline buffer */
/*TODO*////* Return the characters number of the line, pbegin point to the start of the next line */
/*TODO*///static unsigned multiline_extract(const char** pbegin, const char* end, unsigned max)
/*TODO*///{
/*TODO*///	unsigned mac = 0;
/*TODO*///	const char* begin = *pbegin;
/*TODO*///	while (begin != end && mac < max)
/*TODO*///	{
/*TODO*///		if (*begin == '\n')
/*TODO*///		{
/*TODO*///			*pbegin = begin + 1; /* strip final space */
/*TODO*///			return mac;
/*TODO*///		}
/*TODO*///		else if (*begin == ' ')
/*TODO*///		{
/*TODO*///			const char* word_end = begin + 1;
/*TODO*///			while (word_end != end && *word_end != ' ' && *word_end != '\n')
/*TODO*///				++word_end;
/*TODO*///			if (mac + word_end - begin > max)
/*TODO*///			{
/*TODO*///				if (mac)
/*TODO*///				{
/*TODO*///					*pbegin = begin + 1;
/*TODO*///					return mac; /* strip final space */
/*TODO*///				} else {
/*TODO*///					*pbegin = begin + max;
/*TODO*///					return max;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			mac += word_end - begin;
/*TODO*///			begin = word_end;
/*TODO*///		} else {
/*TODO*///			++mac;
/*TODO*///			++begin;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (begin != end && (*begin == '\n' || *begin == ' '))
/*TODO*///		++begin;
/*TODO*///	*pbegin = begin;
/*TODO*///	return mac;
/*TODO*///}
/*TODO*///
/*TODO*////* Compute the output size of a multiline string */
/*TODO*///static void multiline_size(int* dx, int* dy, const char* begin, const char* end, unsigned max)
/*TODO*///{
/*TODO*///	unsigned rows = 0;
/*TODO*///	unsigned cols = 0;
/*TODO*///	while (begin != end)
/*TODO*///	{
/*TODO*///		unsigned len;
/*TODO*///		len = multiline_extract(&begin,end,max);
/*TODO*///		if (len > cols)
/*TODO*///			cols = len;
/*TODO*///		++rows;
/*TODO*///	}
/*TODO*///	*dx = cols * Machine->uifontwidth;
/*TODO*///	*dy = (rows-1) * 3*Machine->uifontheight/2 + Machine->uifontheight;
/*TODO*///}
/*TODO*///
/*TODO*////* Compute the output size of a multiline string with box */
/*TODO*///static void multilinebox_size(int* dx, int* dy, const char* begin, const char* end, unsigned max)
/*TODO*///{
/*TODO*///	multiline_size(dx,dy,begin,end,max);
/*TODO*///	*dx += Machine->uifontwidth;
/*TODO*///	*dy += Machine->uifontheight;
/*TODO*///}
/*TODO*///
/*TODO*////* Display a multiline string */
/*TODO*///static void ui_multitext_ex(struct osd_bitmap *bitmap, const char* begin, const char* end, unsigned max, int x, int y, int color)
/*TODO*///{
/*TODO*///	while (begin != end)
/*TODO*///	{
/*TODO*///		const char* line_begin = begin;
/*TODO*///		unsigned len = multiline_extract(&begin,end,max);
/*TODO*///		ui_text_ex(bitmap, line_begin, line_begin + len,x,y,color);
/*TODO*///		y += 3*Machine->uifontheight/2;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* Display a multiline string with box */
/*TODO*///static void ui_multitextbox_ex(struct osd_bitmap *bitmap,const char* begin, const char* end, unsigned max, int x, int y, int dx, int dy, int color)
/*TODO*///{
/*TODO*///	ui_drawbox(bitmap,x,y,dx,dy);
/*TODO*///	x += Machine->uifontwidth/2;
/*TODO*///	y += Machine->uifontheight/2;
/*TODO*///	ui_multitext_ex(bitmap,begin,end,max,x,y,color);
/*TODO*///}
/*TODO*///
/*TODO*///void ui_displaymenu(struct osd_bitmap *bitmap,const char **items,const char **subitems,char *flag,int selected,int arrowize_subitem)
/*TODO*///{
/*TODO*///	struct DisplayText dt[256];
/*TODO*///	int curr_dt;
/*TODO*///	const char *lefthilight = ui_getstring (UI_lefthilight);
/*TODO*///	const char *righthilight = ui_getstring (UI_righthilight);
/*TODO*///	const char *uparrow = ui_getstring (UI_uparrow);
/*TODO*///	const char *downarrow = ui_getstring (UI_downarrow);
/*TODO*///	const char *leftarrow = ui_getstring (UI_leftarrow);
/*TODO*///	const char *rightarrow = ui_getstring (UI_rightarrow);
/*TODO*///	int i,count,len,maxlen,highlen;
/*TODO*///	int leftoffs,topoffs,visible,topitem;
/*TODO*///	int selected_long;
/*TODO*///
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///	maxlen = 0;
/*TODO*///	highlen = Machine->uiwidth / Machine->uifontwidth;
/*TODO*///	while (items[i])
/*TODO*///	{
/*TODO*///		len = 3 + strlen(items[i]);
/*TODO*///		if (subitems && subitems[i])
/*TODO*///			len += 2 + strlen(subitems[i]);
/*TODO*///		if (len > maxlen && len <= highlen)
/*TODO*///			maxlen = len;
/*TODO*///		i++;
/*TODO*///	}
/*TODO*///	count = i;
/*TODO*///
/*TODO*///	visible = Machine->uiheight / (3 * Machine->uifontheight / 2) - 1;
/*TODO*///	topitem = 0;
/*TODO*///	if (visible > count) visible = count;
/*TODO*///	else
/*TODO*///	{
/*TODO*///		topitem = selected - visible / 2;
/*TODO*///		if (topitem < 0) topitem = 0;
/*TODO*///		if (topitem > count - visible) topitem = count - visible;
/*TODO*///	}
/*TODO*///
/*TODO*///	leftoffs = (Machine->uiwidth - maxlen * Machine->uifontwidth) / 2;
/*TODO*///	topoffs = (Machine->uiheight - (3 * visible + 1) * Machine->uifontheight / 2) / 2;
/*TODO*///
/*TODO*///	/* black background */
/*TODO*///	ui_drawbox(bitmap,leftoffs,topoffs,maxlen * Machine->uifontwidth,(3 * visible + 1) * Machine->uifontheight / 2);
/*TODO*///
/*TODO*///	selected_long = 0;
/*TODO*///	curr_dt = 0;
/*TODO*///	for (i = 0;i < visible;i++)
/*TODO*///	{
/*TODO*///		int item = i + topitem;
/*TODO*///
/*TODO*///		if (i == 0 && item > 0)
/*TODO*///		{
/*TODO*///			dt[curr_dt].text = uparrow;
/*TODO*///			dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(uparrow)) / 2;
/*TODO*///			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///			curr_dt++;
/*TODO*///		}
/*TODO*///		else if (i == visible - 1 && item < count - 1)
/*TODO*///		{
/*TODO*///			dt[curr_dt].text = downarrow;
/*TODO*///			dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(downarrow)) / 2;
/*TODO*///			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///			curr_dt++;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (subitems && subitems[item])
/*TODO*///			{
/*TODO*///				int sublen;
/*TODO*///				len = strlen(items[item]);
/*TODO*///				dt[curr_dt].text = items[item];
/*TODO*///				dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///				dt[curr_dt].x = leftoffs + 3*Machine->uifontwidth/2;
/*TODO*///				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///				curr_dt++;
/*TODO*///				sublen = strlen(subitems[item]);
/*TODO*///				if (sublen > maxlen-5-len)
/*TODO*///				{
/*TODO*///					dt[curr_dt].text = "...";
/*TODO*///					sublen = strlen(dt[curr_dt].text);
/*TODO*///					if (item == selected)
/*TODO*///						selected_long = 1;
/*TODO*///				} else {
/*TODO*///					dt[curr_dt].text = subitems[item];
/*TODO*///				}
/*TODO*///				/* If this item is flagged, draw it in inverse print */
/*TODO*///				dt[curr_dt].color = (flag && flag[item]) ? UI_COLOR_INVERSE : UI_COLOR_NORMAL;
/*TODO*///				dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1-sublen) - Machine->uifontwidth/2;
/*TODO*///				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///				curr_dt++;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dt[curr_dt].text = items[item];
/*TODO*///				dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///				dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * strlen(items[item])) / 2;
/*TODO*///				dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///				curr_dt++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	i = selected - topitem;
/*TODO*///	if (subitems && subitems[selected] && arrowize_subitem)
/*TODO*///	{
/*TODO*///		if (arrowize_subitem & 1)
/*TODO*///		{
/*TODO*///			int sublen;
/*TODO*///
/*TODO*///			len = strlen(items[selected]);
/*TODO*///
/*TODO*///			dt[curr_dt].text = leftarrow;
/*TODO*///			dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///
/*TODO*///			sublen = strlen(subitems[selected]);
/*TODO*///			if (sublen > maxlen-5-len)
/*TODO*///				sublen = strlen("...");
/*TODO*///
/*TODO*///			dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-2 - sublen) - Machine->uifontwidth/2 - 1;
/*TODO*///			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///			curr_dt++;
/*TODO*///		}
/*TODO*///		if (arrowize_subitem & 2)
/*TODO*///		{
/*TODO*///			dt[curr_dt].text = rightarrow;
/*TODO*///			dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///			dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1) - Machine->uifontwidth/2;
/*TODO*///			dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///			curr_dt++;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		dt[curr_dt].text = righthilight;
/*TODO*///		dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///		dt[curr_dt].x = leftoffs + Machine->uifontwidth * (maxlen-1) - Machine->uifontwidth/2;
/*TODO*///		dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///		curr_dt++;
/*TODO*///	}
/*TODO*///	dt[curr_dt].text = lefthilight;
/*TODO*///	dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///	dt[curr_dt].x = leftoffs + Machine->uifontwidth/2;
/*TODO*///	dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///	curr_dt++;
/*TODO*///
/*TODO*///	dt[curr_dt].text = 0;	/* terminate array */
/*TODO*///
/*TODO*///	displaytext(bitmap,dt,0,0);
/*TODO*///
/*TODO*///	if (selected_long)
/*TODO*///	{
/*TODO*///		int long_dx;
/*TODO*///		int long_dy;
/*TODO*///		int long_x;
/*TODO*///		int long_y;
/*TODO*///		unsigned long_max;
/*TODO*///
/*TODO*///		long_max = (Machine->uiwidth / Machine->uifontwidth) - 2;
/*TODO*///		multilinebox_size(&long_dx,&long_dy,subitems[selected],subitems[selected] + strlen(subitems[selected]), long_max);
/*TODO*///
/*TODO*///		long_x = Machine->uiwidth - long_dx;
/*TODO*///		long_y = topoffs + (i+1) * 3*Machine->uifontheight/2;
/*TODO*///
/*TODO*///		/* if too low display up */
/*TODO*///		if (long_y + long_dy > Machine->uiheight)
/*TODO*///			long_y = topoffs + i * 3*Machine->uifontheight/2 - long_dy;
/*TODO*///
/*TODO*///		ui_multitextbox_ex(bitmap,subitems[selected],subitems[selected] + strlen(subitems[selected]), long_max, long_x,long_y,long_dx,long_dy, UI_COLOR_NORMAL);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void ui_displaymessagewindow(struct osd_bitmap *bitmap,const char *text)
/*TODO*///{
/*TODO*///	struct DisplayText dt[256];
/*TODO*///	int curr_dt;
/*TODO*///	char *c,*c2;
/*TODO*///	int i,len,maxlen,lines;
/*TODO*///	char textcopy[2048];
/*TODO*///	int leftoffs,topoffs;
/*TODO*///	int maxcols,maxrows;
/*TODO*///
/*TODO*///	maxcols = (Machine->uiwidth / Machine->uifontwidth) - 1;
/*TODO*///	maxrows = (2 * Machine->uiheight - Machine->uifontheight) / (3 * Machine->uifontheight);
/*TODO*///
/*TODO*///	/* copy text, calculate max len, count lines, wrap long lines and crop height to fit */
/*TODO*///	maxlen = 0;
/*TODO*///	lines = 0;
/*TODO*///	c = (char *)text;
/*TODO*///	c2 = textcopy;
/*TODO*///	while (*c)
/*TODO*///	{
/*TODO*///		len = 0;
/*TODO*///		while (*c && *c != '\n')
/*TODO*///		{
/*TODO*///			*c2++ = *c++;
/*TODO*///			len++;
/*TODO*///			if (len == maxcols && *c != '\n')
/*TODO*///			{
/*TODO*///				/* attempt word wrap */
/*TODO*///				char *csave = c, *c2save = c2;
/*TODO*///				int lensave = len;
/*TODO*///
/*TODO*///				/* back up to last space or beginning of line */
/*TODO*///				while (*c != ' ' && *c != '\n' && c > text)
/*TODO*///					--c, --c2, --len;
/*TODO*///
/*TODO*///				/* if no space was found, hard wrap instead */
/*TODO*///				if (*c != ' ')
/*TODO*///					c = csave, c2 = c2save, len = lensave;
/*TODO*///				else
/*TODO*///					c++;
/*TODO*///
/*TODO*///				*c2++ = '\n'; /* insert wrap */
/*TODO*///				break;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (*c == '\n')
/*TODO*///			*c2++ = *c++;
/*TODO*///
/*TODO*///		if (len > maxlen) maxlen = len;
/*TODO*///
/*TODO*///		lines++;
/*TODO*///		if (lines == maxrows)
/*TODO*///			break;
/*TODO*///	}
/*TODO*///	*c2 = '\0';
/*TODO*///
/*TODO*///	maxlen += 1;
/*TODO*///
/*TODO*///	leftoffs = (Machine->uiwidth - Machine->uifontwidth * maxlen) / 2;
/*TODO*///	if (leftoffs < 0) leftoffs = 0;
/*TODO*///	topoffs = (Machine->uiheight - (3 * lines + 1) * Machine->uifontheight / 2) / 2;
/*TODO*///
/*TODO*///	/* black background */
/*TODO*///	ui_drawbox(bitmap,leftoffs,topoffs,maxlen * Machine->uifontwidth,(3 * lines + 1) * Machine->uifontheight / 2);
/*TODO*///
/*TODO*///	curr_dt = 0;
/*TODO*///	c = textcopy;
/*TODO*///	i = 0;
/*TODO*///	while (*c)
/*TODO*///	{
/*TODO*///		c2 = c;
/*TODO*///		while (*c && *c != '\n')
/*TODO*///			c++;
/*TODO*///
/*TODO*///		if (*c == '\n')
/*TODO*///		{
/*TODO*///			*c = '\0';
/*TODO*///			c++;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (*c2 == '\t')    /* center text */
/*TODO*///		{
/*TODO*///			c2++;
/*TODO*///			dt[curr_dt].x = (Machine->uiwidth - Machine->uifontwidth * (c - c2)) / 2;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			dt[curr_dt].x = leftoffs + Machine->uifontwidth/2;
/*TODO*///
/*TODO*///		dt[curr_dt].text = c2;
/*TODO*///		dt[curr_dt].color = UI_COLOR_NORMAL;
/*TODO*///		dt[curr_dt].y = topoffs + (3*i+1)*Machine->uifontheight/2;
/*TODO*///		curr_dt++;
/*TODO*///
/*TODO*///		i++;
/*TODO*///	}
/*TODO*///
/*TODO*///	dt[curr_dt].text = 0;	/* terminate array */
/*TODO*///
/*TODO*///	displaytext(bitmap,dt,0,0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void showcharset(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	char buf[80];
/*TODO*///	int bank,color,firstdrawn;
/*TODO*///	int palpage;
/*TODO*///	int changed;
/*TODO*///	int game_is_neogeo=0;
/*TODO*///	unsigned char *orig_used_colors=0;
/*TODO*///
/*TODO*///
/*TODO*///	if (palette_used_colors)
/*TODO*///	{
/*TODO*///		orig_used_colors = malloc(Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		if (!orig_used_colors) return;
/*TODO*///
/*TODO*///		memcpy(orig_used_colors,palette_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///	}
/*TODO*///
/*TODO*///	bank = -1;
/*TODO*///	color = 0;
/*TODO*///	firstdrawn = 0;
/*TODO*///	palpage = 0;
/*TODO*///
/*TODO*///	changed = 1;
/*TODO*///
/*TODO*///	do
/*TODO*///	{
/*TODO*///		int cpx,cpy,skip_chars;
/*TODO*///
/*TODO*///		if (bank >= 0)
/*TODO*///		{
/*TODO*///			cpx = Machine->uiwidth / Machine->gfx[bank]->width;
/*TODO*///			cpy = (Machine->uiheight - Machine->uifontheight) / Machine->gfx[bank]->height;
/*TODO*///			skip_chars = cpx * cpy;
/*TODO*///		}
/*TODO*///		else cpx = cpy = skip_chars = 0;
/*TODO*///
/*TODO*///		if (changed)
/*TODO*///		{
/*TODO*///			int lastdrawn=0;
/*TODO*///
/*TODO*///			osd_clearbitmap(bitmap);
/*TODO*///
/*TODO*///			/* validity chack after char bank change */
/*TODO*///			if (bank >= 0)
/*TODO*///			{
/*TODO*///				if (firstdrawn >= Machine->gfx[bank]->total_elements)
/*TODO*///				{
/*TODO*///					firstdrawn = Machine->gfx[bank]->total_elements - skip_chars;
/*TODO*///					if (firstdrawn < 0) firstdrawn = 0;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if(bank!=2 || !game_is_neogeo)
/*TODO*///			{
/*TODO*///				switch_ui_orientation();
/*TODO*///
/*TODO*///				if (bank >= 0)
/*TODO*///				{
/*TODO*///					int table_offs;
/*TODO*///					int flipx,flipy;
/*TODO*///
/*TODO*///					if (palette_used_colors)
/*TODO*///					{
/*TODO*///						memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///						table_offs = Machine->gfx[bank]->colortable - Machine->remapped_colortable
/*TODO*///								+ Machine->gfx[bank]->color_granularity * color;
/*TODO*///						for (i = 0;i < Machine->gfx[bank]->color_granularity;i++)
/*TODO*///							palette_used_colors[Machine->game_colortable[table_offs + i]] = PALETTE_COLOR_USED;
/*TODO*///						palette_recalc();	/* do it twice in case of previous overflow */
/*TODO*///						palette_recalc();	/*(we redraw the screen only when it changes) */
/*TODO*///					}
/*TODO*///
/*TODO*///#ifndef PREROTATE_GFX
/*TODO*///					flipx = (Machine->orientation ^ trueorientation) & ORIENTATION_FLIP_X;
/*TODO*///					flipy = (Machine->orientation ^ trueorientation) & ORIENTATION_FLIP_Y;
/*TODO*///
/*TODO*///					if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///					{
/*TODO*///						int t;
/*TODO*///						t = flipx; flipx = flipy; flipy = t;
/*TODO*///					}
/*TODO*///#else
/*TODO*///					flipx = 0;
/*TODO*///					flipy = 0;
/*TODO*///#endif
/*TODO*///
/*TODO*///					for (i = 0; i+firstdrawn < Machine->gfx[bank]->total_elements && i<cpx*cpy; i++)
/*TODO*///					{
/*TODO*///						drawgfx(bitmap,Machine->gfx[bank],
/*TODO*///								i+firstdrawn,color,  /*sprite num, color*/
/*TODO*///								flipx,flipy,
/*TODO*///								(i % cpx) * Machine->gfx[bank]->width + Machine->uixmin,
/*TODO*///								Machine->uifontheight + (i / cpx) * Machine->gfx[bank]->height + Machine->uiymin,
/*TODO*///								0,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///						lastdrawn = i+firstdrawn;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					int sx,sy,colors;
/*TODO*///
/*TODO*///					colors = Machine->drv->total_colors - 256 * palpage;
/*TODO*///					if (colors > 256) colors = 256;
/*TODO*///					if (palette_used_colors)
/*TODO*///					{
/*TODO*///						memset(palette_used_colors,PALETTE_COLOR_UNUSED,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///						memset(palette_used_colors+256*palpage,PALETTE_COLOR_USED,colors * sizeof(unsigned char));
/*TODO*///						palette_recalc();	/* do it twice in case of previous overflow */
/*TODO*///						palette_recalc();	/*(we redraw the screen only when it changes) */
/*TODO*///					}
/*TODO*///
/*TODO*///					for (i = 0;i < 16;i++)
/*TODO*///					{
/*TODO*///						char bf[40];
/*TODO*///
/*TODO*///						sx = 3*Machine->uifontwidth + (Machine->uifontwidth*4/3)*(i % 16);
/*TODO*///						sprintf(bf,"%X",i);
/*TODO*///						ui_text(bitmap,bf,sx,2*Machine->uifontheight);
/*TODO*///						if (16*i < colors)
/*TODO*///						{
/*TODO*///							sy = 3*Machine->uifontheight + (Machine->uifontheight)*(i % 16);
/*TODO*///							sprintf(bf,"%3X",i+16*palpage);
/*TODO*///							ui_text(bitmap,bf,0,sy);
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					for (i = 0;i < colors;i++)
/*TODO*///					{
/*TODO*///						sx = Machine->uixmin + 3*Machine->uifontwidth + (Machine->uifontwidth*4/3)*(i % 16);
/*TODO*///						sy = Machine->uiymin + 2*Machine->uifontheight + (Machine->uifontheight)*(i / 16) + Machine->uifontheight;
/*TODO*///						plot_box(bitmap,sx,sy,Machine->uifontwidth*4/3,Machine->uifontheight,Machine->pens[i + 256*palpage]);
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				switch_true_orientation();
/*TODO*///			}
/*TODO*///
/*TODO*///			if (bank >= 0)
/*TODO*///				sprintf(buf,"GFXSET %d COLOR %2X CODE %X-%X",bank,color,firstdrawn,lastdrawn);
/*TODO*///			else
/*TODO*///				strcpy(buf,"PALETTE");
/*TODO*///			ui_text(bitmap,buf,0,0);
/*TODO*///
/*TODO*///			changed = 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		update_video_and_audio();
/*TODO*///
/*TODO*///		if (code_pressed(KEYCODE_LCONTROL) || code_pressed(KEYCODE_RCONTROL))
/*TODO*///		{
/*TODO*///			skip_chars = cpx;
/*TODO*///		}
/*TODO*///		if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
/*TODO*///		{
/*TODO*///			skip_chars = 1;
/*TODO*///		}
/*TODO*///
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
/*TODO*///		{
/*TODO*///			if (bank+1 < MAX_GFX_ELEMENTS && Machine->gfx[bank + 1])
/*TODO*///			{
/*TODO*///				bank++;
/*TODO*/////				firstdrawn = 0;
/*TODO*///				changed = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
/*TODO*///		{
/*TODO*///			if (bank > -1)
/*TODO*///			{
/*TODO*///				bank--;
/*TODO*/////				firstdrawn = 0;
/*TODO*///				changed = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (code_pressed_memory_repeat(KEYCODE_PGDN,4))
/*TODO*///		{
/*TODO*///			if (bank >= 0)
/*TODO*///			{
/*TODO*///				if (firstdrawn + skip_chars < Machine->gfx[bank]->total_elements)
/*TODO*///				{
/*TODO*///					firstdrawn += skip_chars;
/*TODO*///					changed = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (256 * (palpage + 1) < Machine->drv->total_colors)
/*TODO*///				{
/*TODO*///					palpage++;
/*TODO*///					changed = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (code_pressed_memory_repeat(KEYCODE_PGUP,4))
/*TODO*///		{
/*TODO*///			if (bank >= 0)
/*TODO*///			{
/*TODO*///				firstdrawn -= skip_chars;
/*TODO*///				if (firstdrawn < 0) firstdrawn = 0;
/*TODO*///				changed = 1;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (palpage > 0)
/*TODO*///				{
/*TODO*///					palpage--;
/*TODO*///					changed = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_UP,6))
/*TODO*///		{
/*TODO*///			if (bank >= 0)
/*TODO*///			{
/*TODO*///				if (color < Machine->gfx[bank]->total_colors - 1)
/*TODO*///				{
/*TODO*///					color++;
/*TODO*///					changed = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (input_ui_pressed_repeat(IPT_UI_DOWN,6))
/*TODO*///		{
/*TODO*///			if (color > 0)
/*TODO*///			{
/*TODO*///				color--;
/*TODO*///				changed = 1;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///			osd_save_snapshot(bitmap);
/*TODO*///	} while (!input_ui_pressed(IPT_UI_SHOW_GFX) &&
/*TODO*///			!input_ui_pressed(IPT_UI_CANCEL));
/*TODO*///
/*TODO*///	/* clear the screen before returning */
/*TODO*///	osd_clearbitmap(bitmap);
/*TODO*///
/*TODO*///	if (palette_used_colors)
/*TODO*///	{
/*TODO*///		/* this should force a full refresh by the video driver */
/*TODO*///		memset(palette_used_colors,PALETTE_COLOR_TRANSPARENT,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		palette_recalc();
/*TODO*///		/* restore the game used colors array */
/*TODO*///		memcpy(palette_used_colors,orig_used_colors,Machine->drv->total_colors * sizeof(unsigned char));
/*TODO*///		free(orig_used_colors);
/*TODO*///	}
/*TODO*///
/*TODO*///	return;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///static void showtotalcolors(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	char *used;
/*TODO*///	int i,l,x,y,total;
/*TODO*///	unsigned char r,g,b;
/*TODO*///	char buf[40];
/*TODO*///
/*TODO*///
/*TODO*///	used = malloc(64*64*64);
/*TODO*///	if (!used) return;
/*TODO*///
/*TODO*///	for (i = 0;i < 64*64*64;i++)
/*TODO*///		used[i] = 0;
/*TODO*///
/*TODO*///	for (y = 0;y < bitmap->height;y++)
/*TODO*///	{
/*TODO*///		for (x = 0;x < bitmap->width;x++)
/*TODO*///		{
/*TODO*///			osd_get_pen(read_pixel(bitmap,x,y),&r,&g,&b);
/*TODO*///			r >>= 2;
/*TODO*///			g >>= 2;
/*TODO*///			b >>= 2;
/*TODO*///			used[64*64*r+64*g+b] = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	total = 0;
/*TODO*///	for (i = 0;i < 64*64*64;i++)
/*TODO*///		if (used[i]) total++;
/*TODO*///
/*TODO*///	switch_ui_orientation();
/*TODO*///
/*TODO*///	sprintf(buf,"%5d colors",total);
/*TODO*///	l = strlen(buf);
/*TODO*///	for (i = 0;i < l;i++)
/*TODO*///		drawgfx(bitmap,Machine->uifont,buf[i],total>256?UI_COLOR_INVERSE:UI_COLOR_NORMAL,0,0,Machine->uixmin+i*Machine->uifontwidth,Machine->uiymin,0,TRANSPARENCY_NONE,0);
/*TODO*///
/*TODO*///	switch_true_orientation();
/*TODO*///
/*TODO*///	free(used);
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///static int setdipswitches(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	const char *menu_item[128];
/*TODO*///	const char *menu_subitem[128];
/*TODO*///	struct InputPort *entry[128];
/*TODO*///	char flag[40];
/*TODO*///	int i,sel;
/*TODO*///	struct InputPort *in;
/*TODO*///	int total;
/*TODO*///	int arrowize;
/*TODO*///
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///
/*TODO*///	in = Machine->input_ports;
/*TODO*///
/*TODO*///	total = 0;
/*TODO*///	while (in->type != IPT_END)
/*TODO*///	{
/*TODO*///		if ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_NAME && input_port_name(in) != 0 &&
/*TODO*///				(in->type & IPF_UNUSED) == 0 &&
/*TODO*///				!(!options.cheat && (in->type & IPF_CHEAT)))
/*TODO*///		{
/*TODO*///			entry[total] = in;
/*TODO*///			menu_item[total] = input_port_name(in);
/*TODO*///
/*TODO*///			total++;
/*TODO*///		}
/*TODO*///
/*TODO*///		in++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (total == 0) return 0;
/*TODO*///
/*TODO*///	menu_item[total] = ui_getstring (UI_returntomain);
/*TODO*///	menu_item[total + 1] = 0;	/* terminate array */
/*TODO*///	total++;
/*TODO*///
/*TODO*///
/*TODO*///	for (i = 0;i < total;i++)
/*TODO*///	{
/*TODO*///		flag[i] = 0; /* TODO: flag the dip if it's not the real default */
/*TODO*///		if (i < total - 1)
/*TODO*///		{
/*TODO*///			in = entry[i] + 1;
/*TODO*///			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///					in->default_value != entry[i]->default_value)
/*TODO*///				in++;
/*TODO*///
/*TODO*///			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
/*TODO*///				menu_subitem[i] = ui_getstring (UI_INVALID);
/*TODO*///			else menu_subitem[i] = input_port_name(in);
/*TODO*///		}
/*TODO*///		else menu_subitem[i] = 0;	/* no subitem */
/*TODO*///	}
/*TODO*///
/*TODO*///	arrowize = 0;
/*TODO*///	if (sel < total - 1)
/*TODO*///	{
/*TODO*///		in = entry[sel] + 1;
/*TODO*///		while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///				in->default_value != entry[sel]->default_value)
/*TODO*///			in++;
/*TODO*///
/*TODO*///		if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
/*TODO*///			/* invalid setting: revert to a valid one */
/*TODO*///			arrowize |= 1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (((in-1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///					!(!options.cheat && ((in-1)->type & IPF_CHEAT)))
/*TODO*///				arrowize |= 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (sel < total - 1)
/*TODO*///	{
/*TODO*///		in = entry[sel] + 1;
/*TODO*///		while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///				in->default_value != entry[sel]->default_value)
/*TODO*///			in++;
/*TODO*///
/*TODO*///		if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
/*TODO*///			/* invalid setting: revert to a valid one */
/*TODO*///			arrowize |= 2;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if (((in+1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///					!(!options.cheat && ((in+1)->type & IPF_CHEAT)))
/*TODO*///				arrowize |= 2;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	ui_displaymenu(bitmap,menu_item,menu_subitem,flag,sel,arrowize);
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///		sel = (sel + 1) % total;
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///		sel = (sel + total - 1) % total;
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
/*TODO*///	{
/*TODO*///		if (sel < total - 1)
/*TODO*///		{
/*TODO*///			in = entry[sel] + 1;
/*TODO*///			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///					in->default_value != entry[sel]->default_value)
/*TODO*///				in++;
/*TODO*///
/*TODO*///			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
/*TODO*///				/* invalid setting: revert to a valid one */
/*TODO*///				entry[sel]->default_value = (entry[sel]+1)->default_value & entry[sel]->mask;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (((in+1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///						!(!options.cheat && ((in+1)->type & IPF_CHEAT)))
/*TODO*///					entry[sel]->default_value = (in+1)->default_value & entry[sel]->mask;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
/*TODO*///	{
/*TODO*///		if (sel < total - 1)
/*TODO*///		{
/*TODO*///			in = entry[sel] + 1;
/*TODO*///			while ((in->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///					in->default_value != entry[sel]->default_value)
/*TODO*///				in++;
/*TODO*///
/*TODO*///			if ((in->type & ~IPF_MASK) != IPT_DIPSWITCH_SETTING)
/*TODO*///				/* invalid setting: revert to a valid one */
/*TODO*///				entry[sel]->default_value = (entry[sel]+1)->default_value & entry[sel]->mask;
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (((in-1)->type & ~IPF_MASK) == IPT_DIPSWITCH_SETTING &&
/*TODO*///						!(!options.cheat && ((in-1)->type & IPF_CHEAT)))
/*TODO*///					entry[sel]->default_value = (in-1)->default_value & entry[sel]->mask;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///	{
/*TODO*///		if (sel == total - 1) sel = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		sel = -1;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///		sel = -2;
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*////* This flag is used for record OR sequence of key/joy */
/*TODO*////* when is !=0 the first sequence is record, otherwise the first free */
/*TODO*////* it's used byt setdefkeysettings, setdefjoysettings, setkeysettings, setjoysettings */
/*TODO*///static int record_first_insert = 1;
/*TODO*///
/*TODO*///static char menu_subitem_buffer[400][96];
/*TODO*///
/*TODO*///static int setdefcodesettings(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	const char *menu_item[400];
/*TODO*///	const char *menu_subitem[400];
/*TODO*///	struct ipd *entry[400];
/*TODO*///	char flag[400];
/*TODO*///	int i,sel;
/*TODO*///	struct ipd *in;
/*TODO*///	int total;
/*TODO*///	extern struct ipd inputport_defaults[];
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->input_ports == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	in = inputport_defaults;
/*TODO*///
/*TODO*///	total = 0;
/*TODO*///	while (in->type != IPT_END)
/*TODO*///	{
/*TODO*///		if (in->name != 0  && (in->type & ~IPF_MASK) != IPT_UNKNOWN && (in->type & IPF_UNUSED) == 0
/*TODO*///			&& !(!options.cheat && (in->type & IPF_CHEAT)))
/*TODO*///		{
/*TODO*///			entry[total] = in;
/*TODO*///			menu_item[total] = in->name;
/*TODO*///
/*TODO*///			total++;
/*TODO*///		}
/*TODO*///
/*TODO*///		in++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (total == 0) return 0;
/*TODO*///
/*TODO*///	menu_item[total] = ui_getstring (UI_returntomain);
/*TODO*///	menu_item[total + 1] = 0;	/* terminate array */
/*TODO*///	total++;
/*TODO*///
/*TODO*///	for (i = 0;i < total;i++)
/*TODO*///	{
/*TODO*///		if (i < total - 1)
/*TODO*///		{
/*TODO*///			seq_name(&entry[i]->seq,menu_subitem_buffer[i],sizeof(menu_subitem_buffer[0]));
/*TODO*///			menu_subitem[i] = menu_subitem_buffer[i];
/*TODO*///		} else
/*TODO*///			menu_subitem[i] = 0;	/* no subitem */
/*TODO*///		flag[i] = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel > SEL_MASK)   /* are we waiting for a new key? */
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///
/*TODO*///		menu_subitem[sel & SEL_MASK] = "    ";
/*TODO*///		ui_displaymenu(bitmap,menu_item,menu_subitem,flag,sel & SEL_MASK,3);
/*TODO*///
/*TODO*///		ret = seq_read_async(&entry[sel & SEL_MASK]->seq,record_first_insert);
/*TODO*///
/*TODO*///		if (ret >= 0)
/*TODO*///		{
/*TODO*///			sel &= 0xff;
/*TODO*///
/*TODO*///			if (ret > 0 || seq_get_1(&entry[sel]->seq) == CODE_NONE)
/*TODO*///			{
/*TODO*///				seq_set_1(&entry[sel]->seq,CODE_NONE);
/*TODO*///				ret = 1;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///
/*TODO*///			record_first_insert = ret != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///
/*TODO*///		return sel + 1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	ui_displaymenu(bitmap,menu_item,menu_subitem,flag,sel,0);
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///	{
/*TODO*///		sel = (sel + 1) % total;
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///	{
/*TODO*///		sel = (sel + total - 1) % total;
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///	{
/*TODO*///		if (sel == total - 1) sel = -1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			seq_read_async_start();
/*TODO*///
/*TODO*///			sel |= 1 << SEL_BITS;	/* we'll ask for a key */
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		sel = -1;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///		sel = -2;
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int setcodesettings(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	const char *menu_item[400];
/*TODO*///	const char *menu_subitem[400];
/*TODO*///	struct InputPort *entry[400];
/*TODO*///	char flag[400];
/*TODO*///	int i,sel;
/*TODO*///	struct InputPort *in;
/*TODO*///	int total;
/*TODO*///
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->input_ports == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	in = Machine->input_ports;
/*TODO*///
/*TODO*///	total = 0;
/*TODO*///	while (in->type != IPT_END)
/*TODO*///	{
/*TODO*///		if (input_port_name(in) != 0 && seq_get_1(&in->seq) != CODE_NONE && (in->type & ~IPF_MASK) != IPT_UNKNOWN)
/*TODO*///		{
/*TODO*///			entry[total] = in;
/*TODO*///			menu_item[total] = input_port_name(in);
/*TODO*///
/*TODO*///			total++;
/*TODO*///		}
/*TODO*///
/*TODO*///		in++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (total == 0) return 0;
/*TODO*///
/*TODO*///	menu_item[total] = ui_getstring (UI_returntomain);
/*TODO*///	menu_item[total + 1] = 0;	/* terminate array */
/*TODO*///	total++;
/*TODO*///
/*TODO*///	for (i = 0;i < total;i++)
/*TODO*///	{
/*TODO*///		if (i < total - 1)
/*TODO*///		{
/*TODO*///			seq_name(input_port_seq(entry[i]),menu_subitem_buffer[i],sizeof(menu_subitem_buffer[0]));
/*TODO*///			menu_subitem[i] = menu_subitem_buffer[i];
/*TODO*///
/*TODO*///			/* If the key isn't the default, flag it */
/*TODO*///			if (seq_get_1(&entry[i]->seq) != CODE_DEFAULT)
/*TODO*///				flag[i] = 1;
/*TODO*///			else
/*TODO*///				flag[i] = 0;
/*TODO*///
/*TODO*///		} else
/*TODO*///			menu_subitem[i] = 0;	/* no subitem */
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel > SEL_MASK)   /* are we waiting for a new key? */
/*TODO*///	{
/*TODO*///		int ret;
/*TODO*///
/*TODO*///		menu_subitem[sel & SEL_MASK] = "    ";
/*TODO*///		ui_displaymenu(bitmap,menu_item,menu_subitem,flag,sel & SEL_MASK,3);
/*TODO*///
/*TODO*///		ret = seq_read_async(&entry[sel & SEL_MASK]->seq,record_first_insert);
/*TODO*///
/*TODO*///		if (ret >= 0)
/*TODO*///		{
/*TODO*///			sel &= 0xff;
/*TODO*///
/*TODO*///			if (ret > 0 || seq_get_1(&entry[sel]->seq) == CODE_NONE)
/*TODO*///			{
/*TODO*///				seq_set_1(&entry[sel]->seq, CODE_DEFAULT);
/*TODO*///				ret = 1;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///
/*TODO*///			record_first_insert = ret != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		return sel + 1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	ui_displaymenu(bitmap,menu_item,menu_subitem,flag,sel,0);
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///	{
/*TODO*///		sel = (sel + 1) % total;
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///	{
/*TODO*///		sel = (sel + total - 1) % total;
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///	{
/*TODO*///		if (sel == total - 1) sel = -1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			seq_read_async_start();
/*TODO*///
/*TODO*///			sel |= 1 << SEL_BITS;	/* we'll ask for a key */
/*TODO*///
/*TODO*///			/* tell updatescreen() to clean after us (in case the window changes size) */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		sel = -1;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///		sel = -2;
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///
/*TODO*///		record_first_insert = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static int calibratejoysticks(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	char *msg;
/*TODO*///	char buf[2048];
/*TODO*///	int sel;
/*TODO*///	static int calibration_started = 0;
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///	if (calibration_started == 0)
/*TODO*///	{
/*TODO*///		osd_joystick_start_calibration();
/*TODO*///		calibration_started = 1;
/*TODO*///		strcpy (buf, "");
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel > SEL_MASK) /* Waiting for the user to acknowledge joystick movement */
/*TODO*///	{
/*TODO*///		if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		{
/*TODO*///			calibration_started = 0;
/*TODO*///			sel = -1;
/*TODO*///		}
/*TODO*///		else if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///		{
/*TODO*///			osd_joystick_calibrate();
/*TODO*///			sel &= 0xff;
/*TODO*///		}
/*TODO*///
/*TODO*///		ui_displaymessagewindow(bitmap,buf);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		msg = osd_joystick_calibrate_next();
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///		if (msg == 0)
/*TODO*///		{
/*TODO*///			calibration_started = 0;
/*TODO*///			osd_joystick_end_calibration();
/*TODO*///			sel = -1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			strcpy (buf, msg);
/*TODO*///			ui_displaymessagewindow(bitmap,buf);
/*TODO*///			sel |= 1 << SEL_BITS;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///		sel = -2;
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static int settraksettings(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	const char *menu_item[40];
/*TODO*///	const char *menu_subitem[40];
/*TODO*///	struct InputPort *entry[40];
/*TODO*///	int i,sel;
/*TODO*///	struct InputPort *in;
/*TODO*///	int total,total2;
/*TODO*///	int arrowize;
/*TODO*///
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->input_ports == 0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	in = Machine->input_ports;
/*TODO*///
/*TODO*///	/* Count the total number of analog controls */
/*TODO*///	total = 0;
/*TODO*///	while (in->type != IPT_END)
/*TODO*///	{
/*TODO*///		if (((in->type & 0xff) > IPT_ANALOG_START) && ((in->type & 0xff) < IPT_ANALOG_END)
/*TODO*///				&& !(!options.cheat && (in->type & IPF_CHEAT)))
/*TODO*///		{
/*TODO*///			entry[total] = in;
/*TODO*///			total++;
/*TODO*///		}
/*TODO*///		in++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (total == 0) return 0;
/*TODO*///
/*TODO*///	/* Each analog control has 3 entries - key & joy delta, reverse, sensitivity */
/*TODO*///
/*TODO*///#define ENTRIES 3
/*TODO*///
/*TODO*///	total2 = total * ENTRIES;
/*TODO*///
/*TODO*///	menu_item[total2] = ui_getstring (UI_returntomain);
/*TODO*///	menu_item[total2 + 1] = 0;	/* terminate array */
/*TODO*///	total2++;
/*TODO*///
/*TODO*///	arrowize = 0;
/*TODO*///	for (i = 0;i < total2;i++)
/*TODO*///	{
/*TODO*///		if (i < total2 - 1)
/*TODO*///		{
/*TODO*///			char label[30][40];
/*TODO*///			char setting[30][40];
/*TODO*///			int sensitivity,delta;
/*TODO*///			int reverse;
/*TODO*///
/*TODO*///			strcpy (label[i], input_port_name(entry[i/ENTRIES]));
/*TODO*///			sensitivity = IP_GET_SENSITIVITY(entry[i/ENTRIES]);
/*TODO*///			delta = IP_GET_DELTA(entry[i/ENTRIES]);
/*TODO*///			reverse = (entry[i/ENTRIES]->type & IPF_REVERSE);
/*TODO*///
/*TODO*///			strcat (label[i], " ");
/*TODO*///			switch (i%ENTRIES)
/*TODO*///			{
/*TODO*///				case 0:
/*TODO*///					strcat (label[i], ui_getstring (UI_keyjoyspeed));
/*TODO*///					sprintf(setting[i],"%d",delta);
/*TODO*///					if (i == sel) arrowize = 3;
/*TODO*///					break;
/*TODO*///				case 1:
/*TODO*///					strcat (label[i], ui_getstring (UI_reverse));
/*TODO*///					if (reverse)
/*TODO*///						sprintf(setting[i],ui_getstring (UI_on));
/*TODO*///					else
/*TODO*///						sprintf(setting[i],ui_getstring (UI_off));
/*TODO*///					if (i == sel) arrowize = 3;
/*TODO*///					break;
/*TODO*///				case 2:
/*TODO*///					strcat (label[i], ui_getstring (UI_sensitivity));
/*TODO*///					sprintf(setting[i],"%3d%%",sensitivity);
/*TODO*///					if (i == sel) arrowize = 3;
/*TODO*///					break;
/*TODO*///			}
/*TODO*///
/*TODO*///			menu_item[i] = label[i];
/*TODO*///			menu_subitem[i] = setting[i];
/*TODO*///
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///		else menu_subitem[i] = 0;	/* no subitem */
/*TODO*///	}
/*TODO*///
/*TODO*///	ui_displaymenu(bitmap,menu_item,menu_subitem,0,sel,arrowize);
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///		sel = (sel + 1) % total2;
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///		sel = (sel + total2 - 1) % total2;
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
/*TODO*///	{
/*TODO*///		if ((sel % ENTRIES) == 0)
/*TODO*///		/* keyboard/joystick delta */
/*TODO*///		{
/*TODO*///			int val = IP_GET_DELTA(entry[sel/ENTRIES]);
/*TODO*///
/*TODO*///			val --;
/*TODO*///			if (val < 1) val = 1;
/*TODO*///			IP_SET_DELTA(entry[sel/ENTRIES],val);
/*TODO*///		}
/*TODO*///		else if ((sel % ENTRIES) == 1)
/*TODO*///		/* reverse */
/*TODO*///		{
/*TODO*///			int reverse = entry[sel/ENTRIES]->type & IPF_REVERSE;
/*TODO*///			if (reverse)
/*TODO*///				reverse=0;
/*TODO*///			else
/*TODO*///				reverse=IPF_REVERSE;
/*TODO*///			entry[sel/ENTRIES]->type &= ~IPF_REVERSE;
/*TODO*///			entry[sel/ENTRIES]->type |= reverse;
/*TODO*///		}
/*TODO*///		else if ((sel % ENTRIES) == 2)
/*TODO*///		/* sensitivity */
/*TODO*///		{
/*TODO*///			int val = IP_GET_SENSITIVITY(entry[sel/ENTRIES]);
/*TODO*///
/*TODO*///			val --;
/*TODO*///			if (val < 1) val = 1;
/*TODO*///			IP_SET_SENSITIVITY(entry[sel/ENTRIES],val);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
/*TODO*///	{
/*TODO*///		if ((sel % ENTRIES) == 0)
/*TODO*///		/* keyboard/joystick delta */
/*TODO*///		{
/*TODO*///			int val = IP_GET_DELTA(entry[sel/ENTRIES]);
/*TODO*///
/*TODO*///			val ++;
/*TODO*///			if (val > 255) val = 255;
/*TODO*///			IP_SET_DELTA(entry[sel/ENTRIES],val);
/*TODO*///		}
/*TODO*///		else if ((sel % ENTRIES) == 1)
/*TODO*///		/* reverse */
/*TODO*///		{
/*TODO*///			int reverse = entry[sel/ENTRIES]->type & IPF_REVERSE;
/*TODO*///			if (reverse)
/*TODO*///				reverse=0;
/*TODO*///			else
/*TODO*///				reverse=IPF_REVERSE;
/*TODO*///			entry[sel/ENTRIES]->type &= ~IPF_REVERSE;
/*TODO*///			entry[sel/ENTRIES]->type |= reverse;
/*TODO*///		}
/*TODO*///		else if ((sel % ENTRIES) == 2)
/*TODO*///		/* sensitivity */
/*TODO*///		{
/*TODO*///			int val = IP_GET_SENSITIVITY(entry[sel/ENTRIES]);
/*TODO*///
/*TODO*///			val ++;
/*TODO*///			if (val > 255) val = 255;
/*TODO*///			IP_SET_SENSITIVITY(entry[sel/ENTRIES],val);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///	{
/*TODO*///		if (sel == total2 - 1) sel = -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		sel = -1;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///		sel = -2;
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///int showcopyright(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	int done;
/*TODO*///	char buf[1000];
/*TODO*///	char buf2[256];
/*TODO*///
/*TODO*///	strcpy (buf, ui_getstring(UI_copyright1));
/*TODO*///	strcat (buf, "\n\n");
/*TODO*///	sprintf(buf2, ui_getstring(UI_copyright2), Machine->gamedrv->description);
/*TODO*///	strcat (buf, buf2);
/*TODO*///	strcat (buf, "\n\n");
/*TODO*///	strcat (buf, ui_getstring(UI_copyright3));
/*TODO*///
/*TODO*///	ui_displaymessagewindow(bitmap,buf);
/*TODO*///
/*TODO*///	setup_selected = -1;////
/*TODO*///	done = 0;
/*TODO*///	do
/*TODO*///	{
/*TODO*///		update_video_and_audio();
/*TODO*///		if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		{
/*TODO*///			setup_selected = 0;////
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		if (keyboard_pressed_memory(KEYCODE_O) ||
/*TODO*///				input_ui_pressed(IPT_UI_LEFT))
/*TODO*///			done = 1;
/*TODO*///		if (done == 1 && (keyboard_pressed_memory(KEYCODE_K) ||
/*TODO*///				input_ui_pressed(IPT_UI_RIGHT)))
/*TODO*///			done = 2;
/*TODO*///	} while (done < 2);
/*TODO*///
/*TODO*///	setup_selected = 0;////
/*TODO*///	osd_clearbitmap(bitmap);
/*TODO*///	update_video_and_audio();
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///static int displaygameinfo(struct osd_bitmap *bitmap,int selected)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	char buf[2048];
/*TODO*///	char buf2[32];
/*TODO*///	int sel;
/*TODO*///
/*TODO*///
/*TODO*///	sel = selected - 1;
/*TODO*///
/*TODO*///
/*TODO*///	sprintf(buf,"%s\n%s %s\n\n%s:\n",Machine->gamedrv->description,Machine->gamedrv->year,Machine->gamedrv->manufacturer,
/*TODO*///		ui_getstring (UI_cpu));
/*TODO*///	i = 0;
/*TODO*///	while (i < MAX_CPU && Machine->drv->cpu[i].cpu_type)
/*TODO*///	{
/*TODO*///
/*TODO*///		if (Machine->drv->cpu[i].cpu_clock >= 1000000)
/*TODO*///			sprintf(&buf[strlen(buf)],"%s %d.%06d MHz",
/*TODO*///					cputype_name(Machine->drv->cpu[i].cpu_type),
/*TODO*///					Machine->drv->cpu[i].cpu_clock / 1000000,
/*TODO*///					Machine->drv->cpu[i].cpu_clock % 1000000);
/*TODO*///		else
/*TODO*///			sprintf(&buf[strlen(buf)],"%s %d.%03d kHz",
/*TODO*///					cputype_name(Machine->drv->cpu[i].cpu_type),
/*TODO*///					Machine->drv->cpu[i].cpu_clock / 1000,
/*TODO*///					Machine->drv->cpu[i].cpu_clock % 1000);
/*TODO*///
/*TODO*///		if (Machine->drv->cpu[i].cpu_type & CPU_AUDIO_CPU)
/*TODO*///		{
/*TODO*///			sprintf (buf2, " (%s)", ui_getstring (UI_sound_lc));
/*TODO*///			strcat(buf, buf2);
/*TODO*///		}
/*TODO*///
/*TODO*///		strcat(buf,"\n");
/*TODO*///
/*TODO*///		i++;
/*TODO*///	}
/*TODO*///
/*TODO*///	sprintf (buf2, "\n%s", ui_getstring (UI_sound));
/*TODO*///	strcat (buf, buf2);
/*TODO*///	if (Machine->drv->sound_attributes & SOUND_SUPPORTS_STEREO)
/*TODO*///		sprintf(&buf[strlen(buf)]," (%s)", ui_getstring (UI_stereo));
/*TODO*///	strcat(buf,":\n");
/*TODO*///
/*TODO*///	i = 0;
/*TODO*///	while (i < MAX_SOUND && Machine->drv->sound[i].sound_type)
/*TODO*///	{
/*TODO*///		if (sound_num(&Machine->drv->sound[i]))
/*TODO*///			sprintf(&buf[strlen(buf)],"%dx",sound_num(&Machine->drv->sound[i]));
/*TODO*///
/*TODO*///		sprintf(&buf[strlen(buf)],"%s",sound_name(&Machine->drv->sound[i]));
/*TODO*///
/*TODO*///		if (sound_clock(&Machine->drv->sound[i]))
/*TODO*///		{
/*TODO*///			if (sound_clock(&Machine->drv->sound[i]) >= 1000000)
/*TODO*///				sprintf(&buf[strlen(buf)]," %d.%06d MHz",
/*TODO*///						sound_clock(&Machine->drv->sound[i]) / 1000000,
/*TODO*///						sound_clock(&Machine->drv->sound[i]) % 1000000);
/*TODO*///			else
/*TODO*///				sprintf(&buf[strlen(buf)]," %d.%03d kHz",
/*TODO*///						sound_clock(&Machine->drv->sound[i]) / 1000,
/*TODO*///						sound_clock(&Machine->drv->sound[i]) % 1000);
/*TODO*///		}
/*TODO*///
/*TODO*///		strcat(buf,"\n");
/*TODO*///
/*TODO*///		i++;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///		sprintf(&buf[strlen(buf)],"\n%s\n", ui_getstring (UI_vectorgame));
/*TODO*///	else
/*TODO*///	{
/*TODO*///		int pixelx,pixely,tmax,tmin,rem;
/*TODO*///
/*TODO*///		pixelx = 4 * (Machine->visible_area.max_y - Machine->visible_area.min_y + 1);
/*TODO*///		pixely = 3 * (Machine->visible_area.max_x - Machine->visible_area.min_x + 1);
/*TODO*///
/*TODO*///		/* calculate MCD */
/*TODO*///		if (pixelx >= pixely)
/*TODO*///		{
/*TODO*///			tmax = pixelx;
/*TODO*///			tmin = pixely;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			tmax = pixely;
/*TODO*///			tmin = pixelx;
/*TODO*///		}
/*TODO*///		while ( (rem = tmax % tmin) )
/*TODO*///		{
/*TODO*///			tmax = tmin;
/*TODO*///			tmin = rem;
/*TODO*///		}
/*TODO*///		/* tmin is now the MCD */
/*TODO*///
/*TODO*///		pixelx /= tmin;
/*TODO*///		pixely /= tmin;
/*TODO*///
/*TODO*///		sprintf(&buf[strlen(buf)],"\n%s:\n", ui_getstring (UI_screenres));
/*TODO*///		sprintf(&buf[strlen(buf)],"%d x %d (%s) %f Hz\n",
/*TODO*///				Machine->visible_area.max_x - Machine->visible_area.min_x + 1,
/*TODO*///				Machine->visible_area.max_y - Machine->visible_area.min_y + 1,
/*TODO*///				(Machine->gamedrv->flags & ORIENTATION_SWAP_XY) ? "V" : "H",
/*TODO*///				Machine->drv->frames_per_second);
/*TODO*///#if 0
/*TODO*///		sprintf(&buf[strlen(buf)],"pixel aspect ratio %d:%d\n",
/*TODO*///				pixelx,pixely);
/*TODO*///		sprintf(&buf[strlen(buf)],"%d colors ",Machine->drv->total_colors);
/*TODO*///		if (Machine->gamedrv->flags & GAME_REQUIRES_16BIT)
/*TODO*///			strcat(buf,"(16-bit required)\n");
/*TODO*///		else if (Machine->drv->video_attributes & VIDEO_MODIFIES_PALETTE)
/*TODO*///			strcat(buf,"(dynamic)\n");
/*TODO*///		else strcat(buf,"(static)\n");
/*TODO*///#endif
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (sel == -1)
/*TODO*///	{
/*TODO*///		/* startup info, print MAME version and ask for any key */
/*TODO*///
/*TODO*///		sprintf (buf2, "\n\t%s ", ui_getstring (UI_mame));	/* \t means that the line will be centered */
/*TODO*///		strcat(buf, buf2);
/*TODO*///
/*TODO*///		strcat(buf,build_version);
/*TODO*///		sprintf (buf2, "\n\t%s", ui_getstring (UI_anykey));
/*TODO*///		strcat(buf,buf2);
/*TODO*///		ui_drawbox(bitmap,0,0,Machine->uiwidth,Machine->uiheight);
/*TODO*///		ui_displaymessagewindow(bitmap,buf);
/*TODO*///
/*TODO*///		sel = 0;
/*TODO*///		if (code_read_async() != CODE_NONE)
/*TODO*///			sel = -1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* menu system, use the normal menu keys */
/*TODO*///		strcat(buf,"\n\t");
/*TODO*///		strcat(buf,ui_getstring (UI_lefthilight));
/*TODO*///		strcat(buf," ");
/*TODO*///		strcat(buf,ui_getstring (UI_returntomain));
/*TODO*///		strcat(buf," ");
/*TODO*///		strcat(buf,ui_getstring (UI_righthilight));
/*TODO*///
/*TODO*///		ui_displaymessagewindow(bitmap,buf);
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///			sel = -1;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///			sel = -1;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///			sel = -2;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///int showgamewarnings(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	char buf[2048];
/*TODO*///
/*TODO*///	if (Machine->gamedrv->flags &
/*TODO*///			(GAME_NOT_WORKING | GAME_UNEMULATED_PROTECTION | GAME_WRONG_COLORS | GAME_IMPERFECT_COLORS |
/*TODO*///			  GAME_NO_SOUND | GAME_IMPERFECT_SOUND | GAME_NO_COCKTAIL))
/*TODO*///	{
/*TODO*///		int done;
/*TODO*///
/*TODO*///		strcpy(buf, ui_getstring (UI_knownproblems));
/*TODO*///		strcat(buf, "\n\n");
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_COMPUTER)
/*TODO*///		{
/*TODO*///			strcpy(buf, ui_getstring (UI_comp1));
/*TODO*///			strcat(buf, "\n\n");
/*TODO*///			strcat(buf, ui_getstring (UI_comp2));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_IMPERFECT_COLORS)
/*TODO*///		{
/*TODO*///			strcat(buf, ui_getstring (UI_imperfectcolors));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_WRONG_COLORS)
/*TODO*///		{
/*TODO*///			strcat(buf, ui_getstring (UI_wrongcolors));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_IMPERFECT_SOUND)
/*TODO*///		{
/*TODO*///			strcat(buf, ui_getstring (UI_imperfectsound));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_NO_SOUND)
/*TODO*///		{
/*TODO*///			strcat(buf, ui_getstring (UI_nosound));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & GAME_NO_COCKTAIL)
/*TODO*///		{
/*TODO*///			strcat(buf, ui_getstring (UI_nococktail));
/*TODO*///			strcat(buf, "\n");
/*TODO*///		}
/*TODO*///
/*TODO*///		if (Machine->gamedrv->flags & (GAME_NOT_WORKING | GAME_UNEMULATED_PROTECTION))
/*TODO*///		{
/*TODO*///			const struct GameDriver *maindrv;
/*TODO*///			int foundworking;
/*TODO*///
/*TODO*///			if (Machine->gamedrv->flags & GAME_NOT_WORKING)
/*TODO*///			{
/*TODO*///				strcpy(buf, ui_getstring (UI_brokengame));
/*TODO*///				strcat(buf, "\n");
/*TODO*///			}
/*TODO*///			if (Machine->gamedrv->flags & GAME_UNEMULATED_PROTECTION)
/*TODO*///			{
/*TODO*///				strcat(buf, ui_getstring (UI_brokenprotection));
/*TODO*///				strcat(buf, "\n");
/*TODO*///			}
/*TODO*///
/*TODO*///			if (Machine->gamedrv->clone_of && !(Machine->gamedrv->clone_of->flags & NOT_A_DRIVER))
/*TODO*///				maindrv = Machine->gamedrv->clone_of;
/*TODO*///			else maindrv = Machine->gamedrv;
/*TODO*///
/*TODO*///			foundworking = 0;
/*TODO*///			i = 0;
/*TODO*///			while (drivers[i])
/*TODO*///			{
/*TODO*///				if (drivers[i] == maindrv || drivers[i]->clone_of == maindrv)
/*TODO*///				{
/*TODO*///					if ((drivers[i]->flags & (GAME_NOT_WORKING | GAME_UNEMULATED_PROTECTION)) == 0)
/*TODO*///					{
/*TODO*///						if (foundworking == 0)
/*TODO*///						{
/*TODO*///							strcat(buf,"\n\n");
/*TODO*///							strcat(buf, ui_getstring (UI_workingclones));
/*TODO*///							strcat(buf,"\n\n");
/*TODO*///						}
/*TODO*///						foundworking = 1;
/*TODO*///
/*TODO*///						sprintf(&buf[strlen(buf)],"%s\n",drivers[i]->name);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				i++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		strcat(buf,"\n\n");
/*TODO*///		strcat(buf,ui_getstring (UI_typeok));
/*TODO*///
/*TODO*///		ui_displaymessagewindow(bitmap,buf);
/*TODO*///
/*TODO*///		done = 0;
/*TODO*///		do
/*TODO*///		{
/*TODO*///			update_video_and_audio();
/*TODO*///			if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///				return 1;
/*TODO*///			if (code_pressed_memory(KEYCODE_O) ||
/*TODO*///					input_ui_pressed(IPT_UI_LEFT))
/*TODO*///				done = 1;
/*TODO*///			if (done == 1 && (code_pressed_memory(KEYCODE_K) ||
/*TODO*///					input_ui_pressed(IPT_UI_RIGHT)))
/*TODO*///				done = 2;
/*TODO*///		} while (done < 2);
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	osd_clearbitmap(bitmap);
/*TODO*///
/*TODO*///	/* clear the input memory */
/*TODO*///	while (code_read_async() != CODE_NONE) {};
/*TODO*///
/*TODO*///	while (displaygameinfo(bitmap,0) == 1)
/*TODO*///	{
/*TODO*///		update_video_and_audio();
/*TODO*///	}
/*TODO*///
/*TODO*///	while (displayimageinfo(bitmap,0) == 1)
/*TODO*///	{
/*TODO*///		update_video_and_audio();
/*TODO*///	}
/*TODO*///
/*TODO*///	osd_clearbitmap(bitmap);
/*TODO*///	/* make sure that the screen is really cleared, in case autoframeskip kicked in */
/*TODO*///	update_video_and_audio();
/*TODO*///	update_video_and_audio();
/*TODO*///	update_video_and_audio();
/*TODO*///	update_video_and_audio();
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
    /* Word-wraps the text in the specified buffer to fit in maxwidth characters per line.
    The contents of the buffer are modified.
    Known limitations: Words longer than maxwidth cause the function to fail. */
    public static void wordwrap_text_buffer(String[] buffer, int maxwidth) {
        StringBuilder buf = new StringBuilder();
        buf.append(buffer[0]);
        int width = 0;
        int bi = 0;
        while (bi < buf.length() && buf.charAt(bi) != '\0') {
            if (buf.charAt(bi) == '\n') {
                bi++;
                width = 0;
                continue;
            }

            width++;

            if (width > maxwidth) {
                /* backtrack until a space is found */
                while (buf.charAt(bi) != ' ') {
                    bi--;
                    width--;
                }
                if (width < 1) {
                    return;
                    /* word too long */
                }

                /* replace space with a newline */
                buf.setCharAt(bi, '\n');
            } else {
                bi++;
            }
        }
        buffer[0] = buf.toString();
    }

    public static int count_lines_in_buffer(String buffer) {
        int lines = 0;
        char c = '\0';
        int bi = 0;
        while (bi < buffer.length()) {
            c = buffer.charAt(bi++);
            if (c == '\n') {
                lines++;
            }
        }
        return lines;
    }

    /* Display lines from buffer, starting with line 'scroll', in a width x height text window */
    public static void display_scroll_message(osd_bitmap bitmap, int[] scroll, int width, int height, String buf) {
        DisplayText[] dt = DisplayText.create(256);

        int curr_dt = 0;
        String uparrow = ui_getstring(UI_uparrow);
        String downarrow = ui_getstring(UI_downarrow);
        char textcopy[] = new char[2048];
        int copy_ptr = 0;
        int leftoffs, topoffs;
        int first = scroll[0];
        int buflines, showlines;
        int i;
        int bi = 0;

        /* draw box */
        leftoffs = (Machine.uiwidth - Machine.uifontwidth * (width + 1)) / 2;
        if (leftoffs < 0) {
            leftoffs = 0;
        }
        topoffs = (Machine.uiheight - (3 * height + 1) * Machine.uifontheight / 2) / 2;
        ui_drawbox(bitmap, leftoffs, topoffs, (width + 1) * Machine.uifontwidth, (3 * height + 1) * Machine.uifontheight / 2);

        buflines = count_lines_in_buffer(buf);
        if (first > 0) {
            if (buflines <= height) {
                first = 0;
            } else {
                height--;
                if (first > (buflines - height)) {
                    first = buflines - height;
                }
            }
            scroll[0] = first;
        }

        if (first != 0) {
            /* indicate that scrolling upward is possible */
            dt[curr_dt].text = uparrow;
            dt[curr_dt].color = UI_COLOR_NORMAL;
            dt[curr_dt].x = (Machine.uiwidth - Machine.uifontwidth * strlen(uparrow)) / 2;
            dt[curr_dt].y = topoffs + (3 * curr_dt + 1) * Machine.uifontheight / 2;
            curr_dt++;
        }

        if ((buflines - first) > height) {
            showlines = height - 1;
        } else {
            showlines = height;
        }

        /* skip to first line */
        while (first > 0) {
            char c;

            while (bi < buf.length()) {
                c = buf.charAt(bi++);
                if (c == '\n') {
                    first--;
                    break;
                }
            }
        }

        /* copy 'showlines' lines from buffer, starting with line 'first' */
        copy_ptr = 0;//copy = textcopy;
        for (i = 0; i < showlines; i++) {
            int copystart_ptr = copy_ptr; //char *copystart = copy;

            while (bi < buf.length() && buf.charAt(bi) != '\n') {
                textcopy[copy_ptr] = buf.charAt(bi);
                copy_ptr++;
                bi++;
            }
            textcopy[copy_ptr] = '\0';
            copy_ptr++;
            if (buf.charAt(bi) == '\n') {
                bi++;
            }

            if (textcopy[copystart_ptr] == '\t') /* center text */ {
                copystart_ptr++;
                dt[curr_dt].x = (Machine.uiwidth - Machine.uifontwidth * (copy_ptr - copystart_ptr)) / 2;
            } else {
                dt[curr_dt].x = leftoffs + Machine.uifontwidth / 2;
            }

            dt[curr_dt].text = new String(textcopy).substring(copystart_ptr);
            dt[curr_dt].color = UI_COLOR_NORMAL;
            dt[curr_dt].y = topoffs + (3 * curr_dt + 1) * Machine.uifontheight / 2;
            curr_dt++;
        }

        if (showlines == (height - 1)) {
            /* indicate that scrolling downward is possible */
            dt[curr_dt].text = downarrow;
            dt[curr_dt].color = UI_COLOR_NORMAL;
            dt[curr_dt].x = (Machine.uiwidth - Machine.uifontwidth * strlen(downarrow)) / 2;
            dt[curr_dt].y = topoffs + (3 * curr_dt + 1) * Machine.uifontheight / 2;
            curr_dt++;
        }

        dt[curr_dt].text = null;
        /* terminate array */

        displaytext(bitmap, dt, 0, 0);
    }
    static int[] hist_scroll = new int[1];
    static String[] hist_buf = new String[1];

    /* Display text entry for current driver from history.dat and mameinfo.dat. */
    public static int displayhistory(osd_bitmap bitmap, int selected) {
        int maxcols, maxrows;
        int sel;

        sel = selected - 1;

        maxcols = (Machine.uiwidth / Machine.uifontwidth) - 1;
        maxrows = (2 * Machine.uiheight - Machine.uifontheight) / (3 * Machine.uifontheight);
        maxcols -= 2;
        maxrows -= 8;

        if (hist_buf[0] == null) {
            /* allocate a buffer for the text */
            hist_buf[0] = "";
            /* try to load entry */
            if (load_driver_history(Machine.gamedrv, hist_buf, 8192) == 0) {
                hist_scroll[0] = 0;
                wordwrap_text_buffer(hist_buf, maxcols);
                hist_buf[0] += "\n\t";
                hist_buf[0] += ui_getstring(UI_lefthilight);
                hist_buf[0] += " ";
                hist_buf[0] += ui_getstring(UI_returntomain);
                hist_buf[0] += " ";
                hist_buf[0] += ui_getstring(UI_righthilight);
                hist_buf[0] += "\n";
            } else {
                hist_buf[0] = null;
            }
        }

        {
            if (hist_buf[0] != null) {
                display_scroll_message(bitmap, hist_scroll, maxcols, maxrows, hist_buf[0]);
            } else {
                String msg = "";

                msg += "\t";
                msg += ui_getstring(UI_historymissing);
                msg += "\n\n\t";
                msg += ui_getstring(UI_lefthilight);
                msg += " ";
                msg += ui_getstring(UI_returntomain);
                msg += " ";
                msg += ui_getstring(UI_righthilight);
                ui_displaymessagewindow(bitmap, msg);
            }

            if ((hist_scroll[0] > 0) && input_ui_pressed_repeat(IPT_UI_UP, 4) != 0) {
                if (hist_scroll[0] == 2) {
                    hist_scroll[0] = 0;
                    /* 1 would be the same as 0, but with arrow on top */
                } else {
                    hist_scroll[0]--;
                }
            }

            if (input_ui_pressed_repeat(IPT_UI_DOWN, 4) != 0) {
                if (hist_scroll[0] == 0) {
                    hist_scroll[0] = 2;
                    /* 1 would be the same as 0, but with arrow on top */
                } else {
                    hist_scroll[0]++;
                }
            }

            if (input_ui_pressed(IPT_UI_SELECT) != 0) {
                sel = -1;
            }

            if (input_ui_pressed(IPT_UI_CANCEL) != 0) {
                sel = -1;
            }

            if (input_ui_pressed(IPT_UI_CONFIGURE) != 0) {
                sel = -2;
            }
        }

        if (sel == -1 || sel == -2) {
            /* tell updatescreen() to clean after us */
            need_to_clear_bitmap = 1;

            /* force buffer to be recreated */
            if (hist_buf[0] != null) {
                hist_buf[0] = null;
            }
        }

        return sel + 1;

    }
    public static final int UI_SWITCH = 0;
    public static final int UI_DEFCODE = 1;
    public static final int UI_CODE = 2;
    public static final int UI_ANALOG = 3;
    public static final int UI_CALIBRATE = 4;
    public static final int UI_STATS = 5;
    public static final int UI_GAMEINFO = 6;
    public static final int UI_IMAGEINFO = 7;
    public static final int UI_FILEMANAGER = 8;
    public static final int UI_TAPECONTROL = 9;
    public static final int UI_HISTORY = 10;
    public static final int UI_CHEAT = 11;
    public static final int UI_RESET = 12;
    public static final int UI_MEMCARD = 13;
    public static final int UI_EXIT = 14;

    public static final int MAX_SETUPMENU_ITEMS = 20;
    public static String[] menu_item = new String[MAX_SETUPMENU_ITEMS];
    public static int[] menu_action = new int[MAX_SETUPMENU_ITEMS];
    public static int menu_total;

    public static void setup_menu_init() {
        menu_total = 0;

        menu_item[menu_total] = ui_getstring(UI_inputgeneral);
        menu_action[menu_total++] = UI_DEFCODE;
        menu_item[menu_total] = ui_getstring(UI_inputspecific);
        menu_action[menu_total++] = UI_CODE;
        menu_item[menu_total] = ui_getstring(UI_dipswitches);
        menu_action[menu_total++] = UI_SWITCH;
        /*TODO*///
/*TODO*///	/* Determine if there are any analog controls */
/*TODO*///	{
/*TODO*///		struct InputPort *in;
/*TODO*///		int num;
/*TODO*///
/*TODO*///		in = Machine->input_ports;
/*TODO*///
/*TODO*///		num = 0;
/*TODO*///		while (in->type != IPT_END)
/*TODO*///		{
/*TODO*///			if (((in->type & 0xff) > IPT_ANALOG_START) && ((in->type & 0xff) < IPT_ANALOG_END)
/*TODO*///					&& !(!options.cheat && (in->type & IPF_CHEAT)))
/*TODO*///				num++;
/*TODO*///			in++;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (num != 0)
/*TODO*///		{
/*TODO*///			menu_item[menu_total] = ui_getstring (UI_analogcontrols); menu_action[menu_total++] = UI_ANALOG;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Joystick calibration possible? */
/*TODO*///	if ((osd_joystick_needs_calibration()) != 0)
/*TODO*///	{
/*TODO*///		menu_item[menu_total] = ui_getstring (UI_calibrate); menu_action[menu_total++] = UI_CALIBRATE;
/*TODO*///	}
/*TODO*///

        menu_item[menu_total] = ui_getstring(UI_imageinfo);
        menu_action[menu_total++] = UI_IMAGEINFO;
        menu_item[menu_total] = ui_getstring(UI_filemanager);
        menu_action[menu_total++] = UI_FILEMANAGER;
        menu_item[menu_total] = ui_getstring(UI_tapecontrol);
        menu_action[menu_total++] = UI_TAPECONTROL;
        menu_item[menu_total] = ui_getstring(UI_history);
        menu_action[menu_total++] = UI_HISTORY;

        if (options.cheat != 0) {
            menu_item[menu_total] = ui_getstring(UI_cheat);
            menu_action[menu_total++] = UI_CHEAT;
        }

        menu_item[menu_total] = ui_getstring(UI_resetgame);
        menu_action[menu_total++] = UI_RESET;
        menu_item[menu_total] = ui_getstring(UI_returntogame);
        menu_action[menu_total++] = UI_EXIT;
        menu_item[menu_total] = null;
        /* terminate array */
    }

    static int menu_lastselected = 0;

    public static int setup_menu(osd_bitmap bitmap, int selected) {
        int sel, res = -1;

        if (selected == -1) {
            sel = menu_lastselected;
        } else {
            sel = selected - 1;
        }

        if (sel > SEL_MASK) {
            switch (menu_action[sel & SEL_MASK]) {
                case UI_SWITCH:
                    res = setdipswitches(bitmap, sel >> SEL_BITS);
                    break;
                case UI_DEFCODE:
                    res = setdefcodesettings(bitmap, sel >> SEL_BITS);
                    break;
                case UI_CODE:
                    res = setcodesettings(bitmap, sel >> SEL_BITS);
                    break;
                /*TODO*///			case UI_ANALOG:
/*TODO*///				res = settraksettings(bitmap, sel >> SEL_BITS);
/*TODO*///				break;
/*TODO*///			case UI_CALIBRATE:
/*TODO*///				res = calibratejoysticks(bitmap, sel >> SEL_BITS);
/*TODO*///				break;
/*TODO*///			case UI_IMAGEINFO:
/*TODO*///				res = displayimageinfo(bitmap, sel >> SEL_BITS);
/*TODO*///				break;
/*TODO*///			case UI_FILEMANAGER:
/*TODO*///				res = filemanager(bitmap, sel >> SEL_BITS);
/*TODO*///				break;
/*TODO*///			case UI_TAPECONTROL:
/*TODO*///				res = tapecontrol(bitmap, sel >> SEL_BITS);
/*TODO*///				break;
                case UI_HISTORY:
                    res = displayhistory(bitmap, sel >> SEL_BITS);
                    break;
                case UI_CHEAT:
                    res = cheat_menu(bitmap, sel >> SEL_BITS);
                    break;
            }

            if (res == -1) {
                menu_lastselected = sel;
                sel = -1;
            } else {
                sel = (sel & SEL_MASK) | (res << SEL_BITS);
            }

            return sel + 1;
        }

        ui_displaymenu(bitmap, menu_item, null, null, sel, 0);

        if ((input_ui_pressed_repeat(IPT_UI_DOWN, 8)) != 0) {
            sel = (sel + 1) % menu_total;
        }

        if ((input_ui_pressed_repeat(IPT_UI_UP, 8)) != 0) {
            sel = (sel + menu_total - 1) % menu_total;
        }

        if ((input_ui_pressed(IPT_UI_SELECT)) != 0) {
            switch (menu_action[sel]) {
                case UI_SWITCH:
                case UI_DEFCODE:
                case UI_CODE:
                case UI_ANALOG:
                case UI_CALIBRATE:
                case UI_GAMEINFO:
                case UI_IMAGEINFO:
                case UI_FILEMANAGER:
                case UI_TAPECONTROL:
                case UI_HISTORY:
                case UI_CHEAT:
                case UI_MEMCARD:
                    sel |= 1 << SEL_BITS;
                    /* tell updatescreen() to clean after us */
                    need_to_clear_bitmap = 1;
                    break;

                case UI_RESET:
                    machine_reset();
                    break;

                case UI_EXIT:
                    menu_lastselected = 0;
                    sel = -1;
                    break;
            }
        }

        if ((input_ui_pressed(IPT_UI_CANCEL) != 0
                || input_ui_pressed(IPT_UI_CONFIGURE) != 0)) {
            menu_lastselected = sel;
            sel = -1;
        }

        if (sel == -1) {
            /* tell updatescreen() to clean after us */
            need_to_clear_bitmap = 1;
        }

        return sel + 1;
    }

    /*TODO*///
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  start of On Screen Display handling
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///
/*TODO*///static void displayosd(struct osd_bitmap *bitmap,const char *text,int percentage,int default_percentage)
/*TODO*///{
/*TODO*///	struct DisplayText dt[2];
/*TODO*///	int avail;
/*TODO*///
/*TODO*///
/*TODO*///	avail = (Machine->uiwidth / Machine->uifontwidth) * 19 / 20;
/*TODO*///
/*TODO*///	ui_drawbox(bitmap,(Machine->uiwidth - Machine->uifontwidth * avail) / 2,
/*TODO*///			(Machine->uiheight - 7*Machine->uifontheight/2),
/*TODO*///			avail * Machine->uifontwidth,
/*TODO*///			3*Machine->uifontheight);
/*TODO*///
/*TODO*///	avail--;
/*TODO*///
/*TODO*///	drawbar(bitmap,(Machine->uiwidth - Machine->uifontwidth * avail) / 2,
/*TODO*///			(Machine->uiheight - 3*Machine->uifontheight),
/*TODO*///			avail * Machine->uifontwidth,
/*TODO*///			Machine->uifontheight,
/*TODO*///			percentage,default_percentage);
/*TODO*///
/*TODO*///	dt[0].text = text;
/*TODO*///	dt[0].color = UI_COLOR_NORMAL;
/*TODO*///	dt[0].x = (Machine->uiwidth - Machine->uifontwidth * strlen(text)) / 2;
/*TODO*///	dt[0].y = (Machine->uiheight - 2*Machine->uifontheight) + 2;
/*TODO*///	dt[1].text = 0; /* terminate array */
/*TODO*///	displaytext(bitmap,dt,0,0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static void onscrd_volume(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	char buf[20];
/*TODO*///	int attenuation;
/*TODO*///
/*TODO*///	if (increment)
/*TODO*///	{
/*TODO*///		attenuation = osd_get_mastervolume();
/*TODO*///		attenuation += increment;
/*TODO*///		if (attenuation > 0) attenuation = 0;
/*TODO*///		if (attenuation < -32) attenuation = -32;
/*TODO*///		osd_set_mastervolume(attenuation);
/*TODO*///	}
/*TODO*///	attenuation = osd_get_mastervolume();
/*TODO*///
/*TODO*///	sprintf(buf,"%s %3ddB", ui_getstring (UI_volume), attenuation);
/*TODO*///	displayosd(bitmap,buf,100 * (attenuation + 32) / 32,100);
/*TODO*///}
/*TODO*///
/*TODO*///static void onscrd_mixervol(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	static void *driver = 0;
/*TODO*///	char buf[40];
/*TODO*///	int volume,ch;
/*TODO*///	int doallchannels = 0;
/*TODO*///	int proportional = 0;
/*TODO*///
/*TODO*///
/*TODO*///	if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
/*TODO*///		doallchannels = 1;
/*TODO*///	if (!code_pressed(KEYCODE_LCONTROL) && !code_pressed(KEYCODE_RCONTROL))
/*TODO*///		increment *= 5;
/*TODO*///	if (code_pressed(KEYCODE_LALT) || code_pressed(KEYCODE_RALT))
/*TODO*///		proportional = 1;
/*TODO*///
/*TODO*///	if (increment)
/*TODO*///	{
/*TODO*///		if (proportional)
/*TODO*///		{
/*TODO*///			static int old_vol[MIXER_MAX_CHANNELS];
/*TODO*///			float ratio = 1.0;
/*TODO*///			int overflow = 0;
/*TODO*///
/*TODO*///			if (driver != Machine->drv)
/*TODO*///			{
/*TODO*///				driver = (void *)Machine->drv;
/*TODO*///				for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
/*TODO*///					old_vol[ch] = mixer_get_mixing_level(ch);
/*TODO*///			}
/*TODO*///
/*TODO*///			volume = mixer_get_mixing_level(arg);
/*TODO*///			if (old_vol[arg])
/*TODO*///				ratio = (float)(volume + increment) / (float)old_vol[arg];
/*TODO*///
/*TODO*///			for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
/*TODO*///			{
/*TODO*///				if (mixer_get_name(ch) != 0)
/*TODO*///				{
/*TODO*///					volume = ratio * old_vol[ch];
/*TODO*///					if (volume < 0 || volume > 100)
/*TODO*///						overflow = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if (!overflow)
/*TODO*///			{
/*TODO*///				for (ch = 0; ch < MIXER_MAX_CHANNELS; ch++)
/*TODO*///				{
/*TODO*///					volume = ratio * old_vol[ch];
/*TODO*///					mixer_set_mixing_level(ch,volume);
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			driver = 0; /* force reset of saved volumes */
/*TODO*///
/*TODO*///			volume = mixer_get_mixing_level(arg);
/*TODO*///			volume += increment;
/*TODO*///			if (volume > 100) volume = 100;
/*TODO*///			if (volume < 0) volume = 0;
/*TODO*///
/*TODO*///			if (doallchannels)
/*TODO*///			{
/*TODO*///				for (ch = 0;ch < MIXER_MAX_CHANNELS;ch++)
/*TODO*///					mixer_set_mixing_level(ch,volume);
/*TODO*///			}
/*TODO*///			else
/*TODO*///				mixer_set_mixing_level(arg,volume);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	volume = mixer_get_mixing_level(arg);
/*TODO*///
/*TODO*///	if (proportional)
/*TODO*///		sprintf(buf,"%s %s %3d%%", ui_getstring (UI_allchannels), ui_getstring (UI_relative), volume);
/*TODO*///	else if (doallchannels)
/*TODO*///		sprintf(buf,"%s %s %3d%%", ui_getstring (UI_allchannels), ui_getstring (UI_volume), volume);
/*TODO*///	else
/*TODO*///		sprintf(buf,"%s %s %3d%%",mixer_get_name(arg), ui_getstring (UI_volume), volume);
/*TODO*///	displayosd(bitmap,buf,volume,mixer_get_default_mixing_level(arg));
/*TODO*///}
/*TODO*///
/*TODO*///static void onscrd_brightness(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	char buf[20];
/*TODO*///	int brightness;
/*TODO*///
/*TODO*///
/*TODO*///	if (increment)
/*TODO*///	{
/*TODO*///		brightness = osd_get_brightness();
/*TODO*///		brightness += 5 * increment;
/*TODO*///		if (brightness < 0) brightness = 0;
/*TODO*///		if (brightness > 100) brightness = 100;
/*TODO*///		osd_set_brightness(brightness);
/*TODO*///	}
/*TODO*///	brightness = osd_get_brightness();
/*TODO*///
/*TODO*///	sprintf(buf,"%s %3d%%", ui_getstring (UI_brightness), brightness);
/*TODO*///	displayosd(bitmap,buf,brightness,100);
/*TODO*///}
/*TODO*///
/*TODO*///static void onscrd_gamma(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	char buf[20];
/*TODO*///	float gamma_correction;
/*TODO*///
/*TODO*///	if (increment)
/*TODO*///	{
/*TODO*///		gamma_correction = osd_get_gamma();
/*TODO*///
/*TODO*///		gamma_correction += 0.05 * increment;
/*TODO*///		if (gamma_correction < 0.5) gamma_correction = 0.5;
/*TODO*///		if (gamma_correction > 2.0) gamma_correction = 2.0;
/*TODO*///
/*TODO*///		osd_set_gamma(gamma_correction);
/*TODO*///	}
/*TODO*///	gamma_correction = osd_get_gamma();
/*TODO*///
/*TODO*///	sprintf(buf,"%s %1.2f", ui_getstring (UI_gamma), gamma_correction);
/*TODO*///	displayosd(bitmap,buf,100*(gamma_correction-0.5)/(2.0-0.5),100*(1.0-0.5)/(2.0-0.5));
/*TODO*///}
/*TODO*///
/*TODO*///static void onscrd_vector_intensity(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	char buf[30];
/*TODO*///	float intensity_correction;
/*TODO*///
/*TODO*///	if (increment)
/*TODO*///	{
/*TODO*///		intensity_correction = vector_get_intensity();
/*TODO*///
/*TODO*///		intensity_correction += 0.05 * increment;
/*TODO*///		if (intensity_correction < 0.5) intensity_correction = 0.5;
/*TODO*///		if (intensity_correction > 3.0) intensity_correction = 3.0;
/*TODO*///
/*TODO*///		vector_set_intensity(intensity_correction);
/*TODO*///	}
/*TODO*///	intensity_correction = vector_get_intensity();
/*TODO*///
/*TODO*///	sprintf(buf,"%s %1.2f", ui_getstring (UI_vectorintensity), intensity_correction);
/*TODO*///	displayosd(bitmap,buf,100*(intensity_correction-0.5)/(3.0-0.5),100*(1.5-0.5)/(3.0-0.5));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static void onscrd_overclock(struct osd_bitmap *bitmap,int increment,int arg)
/*TODO*///{
/*TODO*///	char buf[30];
/*TODO*///	double overclock;
/*TODO*///	int cpu, doallcpus = 0, oc;
/*TODO*///
/*TODO*///	if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
/*TODO*///		doallcpus = 1;
/*TODO*///	if (!code_pressed(KEYCODE_LCONTROL) && !code_pressed(KEYCODE_RCONTROL))
/*TODO*///		increment *= 5;
/*TODO*///	if( increment )
/*TODO*///	{
/*TODO*///		overclock = timer_get_overclock(arg);
/*TODO*///		overclock += 0.01 * increment;
/*TODO*///		if (overclock < 0.01) overclock = 0.01;
/*TODO*///		if (overclock > 2.0) overclock = 2.0;
/*TODO*///		if( doallcpus )
/*TODO*///			for( cpu = 0; cpu < cpu_gettotalcpu(); cpu++ )
/*TODO*///				timer_set_overclock(cpu, overclock);
/*TODO*///		else
/*TODO*///			timer_set_overclock(arg, overclock);
/*TODO*///	}
/*TODO*///
/*TODO*///	oc = 100 * timer_get_overclock(arg) + 0.5;
/*TODO*///
/*TODO*///	if( doallcpus )
/*TODO*///		sprintf(buf,"%s %s %3d%%", ui_getstring (UI_allcpus), ui_getstring (UI_overclock), oc);
/*TODO*///	else
/*TODO*///		sprintf(buf,"%s %s%d %3d%%", ui_getstring (UI_overclock), ui_getstring (UI_cpu), arg, oc);
/*TODO*///	displayosd(bitmap,buf,oc/2,100/2);
/*TODO*///}
/*TODO*///
/*TODO*///#define MAX_OSD_ITEMS 30
/*TODO*///static void (*onscrd_fnc[MAX_OSD_ITEMS])(struct osd_bitmap *bitmap,int increment,int arg);
/*TODO*///static int onscrd_arg[MAX_OSD_ITEMS];
/*TODO*///static int onscrd_total_items;
/*TODO*///
/*TODO*///static void onscrd_init(void)
/*TODO*///{
/*TODO*///	int item,ch;
/*TODO*///
/*TODO*///
/*TODO*///	item = 0;
/*TODO*///
/*TODO*///	if (Machine->sample_rate)
/*TODO*///	{
/*TODO*///		onscrd_fnc[item] = onscrd_volume;
/*TODO*///		onscrd_arg[item] = 0;
/*TODO*///		item++;
/*TODO*///
/*TODO*///		for (ch = 0;ch < MIXER_MAX_CHANNELS;ch++)
/*TODO*///		{
/*TODO*///			if (mixer_get_name(ch) != 0)
/*TODO*///			{
/*TODO*///				onscrd_fnc[item] = onscrd_mixervol;
/*TODO*///				onscrd_arg[item] = ch;
/*TODO*///				item++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (options.cheat)
/*TODO*///	{
/*TODO*///		for (ch = 0;ch < cpu_gettotalcpu();ch++)
/*TODO*///		{
/*TODO*///			onscrd_fnc[item] = onscrd_overclock;
/*TODO*///			onscrd_arg[item] = ch;
/*TODO*///			item++;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	onscrd_fnc[item] = onscrd_brightness;
/*TODO*///	onscrd_arg[item] = 0;
/*TODO*///	item++;
/*TODO*///
/*TODO*///	onscrd_fnc[item] = onscrd_gamma;
/*TODO*///	onscrd_arg[item] = 0;
/*TODO*///	item++;
/*TODO*///
/*TODO*///	if (Machine->drv->video_attributes & VIDEO_TYPE_VECTOR)
/*TODO*///	{
/*TODO*///		onscrd_fnc[item] = onscrd_vector_intensity;
/*TODO*///		onscrd_arg[item] = 0;
/*TODO*///		item++;
/*TODO*///	}
/*TODO*///
/*TODO*///	onscrd_total_items = item;
/*TODO*///}
/*TODO*///
/*TODO*///static int on_screen_display(struct osd_bitmap *bitmap, int selected)
/*TODO*///{
/*TODO*///	int increment,sel;
/*TODO*///	static int lastselected = 0;
/*TODO*///
/*TODO*///
/*TODO*///	if (selected == -1)
/*TODO*///		sel = lastselected;
/*TODO*///	else sel = selected - 1;
/*TODO*///
/*TODO*///	increment = 0;
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_LEFT,8))
/*TODO*///		increment = -1;
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_RIGHT,8))
/*TODO*///		increment = 1;
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_DOWN,8))
/*TODO*///		sel = (sel + 1) % onscrd_total_items;
/*TODO*///	if (input_ui_pressed_repeat(IPT_UI_UP,8))
/*TODO*///		sel = (sel + onscrd_total_items - 1) % onscrd_total_items;
/*TODO*///
/*TODO*///	(*onscrd_fnc[sel])(bitmap,increment,onscrd_arg[sel]);
/*TODO*///
/*TODO*///	lastselected = sel;
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
/*TODO*///	{
/*TODO*///		sel = -1;
/*TODO*///
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  end of On Screen Display handling
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///
/*TODO*///
/*TODO*///static void displaymessage(struct osd_bitmap *bitmap,const char *text)
/*TODO*///{
/*TODO*///	struct DisplayText dt[2];
/*TODO*///	int avail;
/*TODO*///
/*TODO*///
/*TODO*///	if (Machine->uiwidth < Machine->uifontwidth * strlen(text))
/*TODO*///	{
/*TODO*///		ui_displaymessagewindow(bitmap,text);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	avail = strlen(text)+2;
/*TODO*///
/*TODO*///	ui_drawbox(bitmap,(Machine->uiwidth - Machine->uifontwidth * avail) / 2,
/*TODO*///			Machine->uiheight - 3*Machine->uifontheight,
/*TODO*///			avail * Machine->uifontwidth,
/*TODO*///			2*Machine->uifontheight);
/*TODO*///
/*TODO*///	dt[0].text = text;
/*TODO*///	dt[0].color = UI_COLOR_NORMAL;
/*TODO*///	dt[0].x = (Machine->uiwidth - Machine->uifontwidth * strlen(text)) / 2;
/*TODO*///	dt[0].y = Machine->uiheight - 5*Machine->uifontheight/2;
/*TODO*///	dt[1].text = 0; /* terminate array */
/*TODO*///	displaytext(bitmap,dt,0,0);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static char messagetext[80];
/*TODO*///static int messagecounter;
/*TODO*///
/*TODO*///void CLIB_DECL usrintf_showmessage(const char *text,...)
/*TODO*///{
/*TODO*///	va_list arg;
/*TODO*///	va_start(arg,text);
/*TODO*///	vsprintf(messagetext,text,arg);
/*TODO*///	va_end(arg);
/*TODO*///	messagecounter = 2 * Machine->drv->frames_per_second;
/*TODO*///}
/*TODO*///
/*TODO*///void CLIB_DECL usrintf_showmessage_secs(int seconds, const char *text,...)
/*TODO*///{
/*TODO*///	va_list arg;
/*TODO*///	va_start(arg,text);
/*TODO*///	vsprintf(messagetext,text,arg);
/*TODO*///	va_end(arg);
/*TODO*///	messagecounter = seconds * Machine->drv->frames_per_second;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int handle_user_interface(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	static int show_profiler;
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	static int show_total_colors;
/*TODO*///#endif
/*TODO*///
/*TODO*///if (Machine->gamedrv->flags & GAME_COMPUTER)
/*TODO*///{
/*TODO*///	static int ui_active = 0, ui_toggle_key = 0;
/*TODO*///	static int ui_display_count = 4 * 60;
/*TODO*///
/*TODO*///	if( input_ui_pressed(IPT_UI_TOGGLE_UI) )
/*TODO*///	{
/*TODO*///		if( !ui_toggle_key )
/*TODO*///		{
/*TODO*///			ui_toggle_key = 1;
/*TODO*///			ui_active = !ui_active;
/*TODO*///			ui_display_count = 4 * 60;
/*TODO*///			bitmap_dirty = 1;
/*TODO*///		 }
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		ui_toggle_key = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( ui_active )
/*TODO*///	{
/*TODO*///		if( ui_display_count > 0 )
/*TODO*///		{
/*TODO*///			char text[] = "KBD: UI  (ScrLock)";
/*TODO*///			int x, x0 = Machine->uiwidth - sizeof(text) * Machine->uifont->width - 2;
/*TODO*///			int y0 = Machine->uiymin + Machine->uiheight - Machine->uifont->height - 2;
/*TODO*///			for( x = 0; text[x]; x++ )
/*TODO*///			{
/*TODO*///				drawgfx(bitmap,
/*TODO*///					Machine->uifont,text[x],0,0,0,
/*TODO*///					x0+x*Machine->uifont->width,
/*TODO*///					y0,0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///			if( --ui_display_count == 0 )
/*TODO*///				bitmap_dirty = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		if( ui_display_count > 0 )
/*TODO*///		{
/*TODO*///			char text[] = "KBD: EMU (ScrLock)";
/*TODO*///			int x, x0 = Machine->uiwidth - sizeof(text) * Machine->uifont->width - 2;
/*TODO*///			int y0 = Machine->uiymin + Machine->uiheight - Machine->uifont->height - 2;
/*TODO*///			for( x = 0; text[x]; x++ )
/*TODO*///			{
/*TODO*///				drawgfx(bitmap,
/*TODO*///					Machine->uifont,text[x],0,0,0,
/*TODO*///					x0+x*Machine->uifont->width,
/*TODO*///					y0,0,TRANSPARENCY_NONE,0);
/*TODO*///			}
/*TODO*///			if( --ui_display_count == 0 )
/*TODO*///				bitmap_dirty = 1;
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///	/* if the user pressed F12, save the screen to a file */
/*TODO*///	if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///		osd_save_snapshot(bitmap);
/*TODO*///
/*TODO*///	/* This call is for the cheat, it must be called once a frame */
/*TODO*///	if (options.cheat) DoCheat(bitmap);
/*TODO*///
/*TODO*///	/* if the user pressed ESC, stop the emulation */
/*TODO*///	/* but don't quit if the setup menu is on screen */
/*TODO*///	if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///	{
/*TODO*///		setup_selected = -1;
/*TODO*///		if (osd_selected != 0)
/*TODO*///		{
/*TODO*///			osd_selected = 0;	/* disable on screen display */
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (setup_selected != 0) setup_selected = setup_menu(bitmap, setup_selected);
/*TODO*///
/*TODO*///	if (!mame_debug && osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
/*TODO*///	{
/*TODO*///		osd_selected = -1;
/*TODO*///		if (setup_selected != 0)
/*TODO*///		{
/*TODO*///			setup_selected = 0; /* disable setup menu */
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if (osd_selected != 0) osd_selected = on_screen_display(bitmap, osd_selected);
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///	/* if the user pressed F3, reset the emulation */
/*TODO*///	if (input_ui_pressed(IPT_UI_RESET_MACHINE))
/*TODO*///		machine_reset();
/*TODO*///
/*TODO*///
/*TODO*///	if (single_step || input_ui_pressed(IPT_UI_PAUSE)) /* pause the game */
/*TODO*///	{
/*TODO*////*		osd_selected = 0;	   disable on screen display, since we are going   */
/*TODO*///							/* to change parameters affected by it */
/*TODO*///
/*TODO*///		if (single_step == 0)
/*TODO*///		{
/*TODO*///			osd_sound_enable(0);
/*TODO*///			osd_pause(1);
/*TODO*///		}
/*TODO*///
/*TODO*///		while (!input_ui_pressed(IPT_UI_PAUSE))
/*TODO*///		{
/*TODO*///			profiler_mark(PROFILER_VIDEO);
/*TODO*///			if (osd_skip_this_frame() == 0)
/*TODO*///			{
/*TODO*///				if (need_to_clear_bitmap || bitmap_dirty)
/*TODO*///				{
/*TODO*///					osd_clearbitmap(bitmap);
/*TODO*///					need_to_clear_bitmap = 0;
/*TODO*///					draw_screen(bitmap_dirty);
/*TODO*///					bitmap_dirty = 0;
/*TODO*///				}
/*TODO*///#ifdef MAME_DEBUG
/*TODO*////* keep calling vh_screenrefresh() while paused so we can stuff */
/*TODO*////* debug code in there */
/*TODO*///draw_screen(bitmap_dirty);
/*TODO*///#endif
/*TODO*///			}
/*TODO*///			profiler_mark(PROFILER_END);
/*TODO*///
/*TODO*///			if (input_ui_pressed(IPT_UI_SNAPSHOT))
/*TODO*///				osd_save_snapshot(bitmap);
/*TODO*///
/*TODO*///			if (setup_selected == 0 && input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///				return 1;
/*TODO*///
/*TODO*///			if (setup_selected == 0 && input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///			{
/*TODO*///				setup_selected = -1;
/*TODO*///				if (osd_selected != 0)
/*TODO*///				{
/*TODO*///					osd_selected = 0;	/* disable on screen display */
/*TODO*///					/* tell updatescreen() to clean after us */
/*TODO*///					need_to_clear_bitmap = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if (setup_selected != 0) setup_selected = setup_menu(bitmap, setup_selected);
/*TODO*///
/*TODO*///			if (!mame_debug && osd_selected == 0 && input_ui_pressed(IPT_UI_ON_SCREEN_DISPLAY))
/*TODO*///			{
/*TODO*///				osd_selected = -1;
/*TODO*///				if (setup_selected != 0)
/*TODO*///				{
/*TODO*///					setup_selected = 0; /* disable setup menu */
/*TODO*///					/* tell updatescreen() to clean after us */
/*TODO*///					need_to_clear_bitmap = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if (osd_selected != 0) osd_selected = on_screen_display(bitmap, osd_selected);
/*TODO*///
/*TODO*///			/* show popup message if any */
/*TODO*///			if (messagecounter > 0) displaymessage(bitmap, messagetext);
/*TODO*///
/*TODO*///			update_video_and_audio();
/*TODO*///		}
/*TODO*///
/*TODO*///		if (code_pressed(KEYCODE_LSHIFT) || code_pressed(KEYCODE_RSHIFT))
/*TODO*///			single_step = 1;
/*TODO*///		else
/*TODO*///		{
/*TODO*///			single_step = 0;
/*TODO*///			osd_pause(0);
/*TODO*///			osd_sound_enable(1);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* show popup message if any */
/*TODO*///	if (messagecounter > 0)
/*TODO*///	{
/*TODO*///		displaymessage(bitmap, messagetext);
/*TODO*///
/*TODO*///		if (--messagecounter == 0)
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	if (input_ui_pressed(IPT_UI_SHOW_PROFILER))
/*TODO*///	{
/*TODO*///		show_profiler ^= 1;
/*TODO*///		if (show_profiler)
/*TODO*///			profiler_start();
/*TODO*///		else
/*TODO*///		{
/*TODO*///			profiler_stop();
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///	if (input_ui_pressed(IPT_UI_SHOW_COLORS))
/*TODO*///	{
/*TODO*///		show_total_colors ^= 1;
/*TODO*///		if (show_total_colors == 0)
/*TODO*///			/* tell updatescreen() to clean after us */
/*TODO*///			need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///	if (show_total_colors) showtotalcolors(bitmap);
/*TODO*///#endif
/*TODO*///
/*TODO*///	if (show_profiler) profiler_show(bitmap);
/*TODO*///
/*TODO*///
/*TODO*///	/* if the user pressed F4, show the character set */
/*TODO*///	if (input_ui_pressed(IPT_UI_SHOW_GFX))
/*TODO*///	{
/*TODO*///		osd_sound_enable(0);
/*TODO*///
/*TODO*///		showcharset(bitmap);
/*TODO*///
/*TODO*///		osd_sound_enable(1);
/*TODO*///	}
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void init_user_interface(void)
/*TODO*///{
/*TODO*///	extern int snapno;	/* in common.c */
/*TODO*///
/*TODO*///	snapno = 0; /* reset snapshot counter */
/*TODO*///
/*TODO*///	setup_menu_init();
/*TODO*///	setup_selected = 0;
/*TODO*///
/*TODO*///	onscrd_init();
/*TODO*///	osd_selected = 0;
/*TODO*///
/*TODO*///	jukebox_selected = -1;
/*TODO*///
/*TODO*///	single_step = 0;
/*TODO*///
/*TODO*///	orientation_count = 0;
/*TODO*///}
/*TODO*///
/*TODO*///int onscrd_active(void)
/*TODO*///{
/*TODO*///	return osd_selected;
/*TODO*///}
/*TODO*///
/*TODO*///int setup_active(void)
/*TODO*///{
/*TODO*///	return setup_selected;
/*TODO*///}
/*TODO*///
/*TODO*///    
}
