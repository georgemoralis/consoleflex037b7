/***************************************************************************

  Pet's Rasterengine

***************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

import static mess.includes.prasterH.*;
import static WIP.mame.osdependH.*;
import static WIP.mame.mame.Machine;
import static old.mame.drawgfxH.*;
import static old.mame.drawgfx.*;
import static WIP.arcadeflex.libc_v2.*;
import static old.arcadeflex.video.*;
import static WIP.arcadeflex.fucPtr.*;
import static old.mame.palette.*;

public class praster
{
	
	public static int VERBOSE_DBG = 1;
        
        public static int praster_draw_character = 8;
        public static int praster_draw_bytecode  = 8;
	public static int praster_draw_pixel     = 8;
	
        
        //public static 
	
	
	/* todo update praster_raster_monotext, praster_raster_graphic */
	
	//PRASTER raster1, raster2= new PRASTER();
        	
	/* struct to be initialised with the functions for the colordepth */
	/*TODO*////static struct {
	/*TODO*////	void (*draw_character)(struct osd_bitmap *bitmap,int ybegin, int yend,
	/*TODO*////							UINT8 *font, int y, int x, UINT16 *color);
	/*TODO*////	void (*draw_bytecode)(struct osd_bitmap *bitmap, UINT8 code,
	/*TODO*////						  int y, int x, UINT16 *color);
	/*TODO*////	void (*draw_pixel)(struct osd_bitmap *bitmap, int y, int x, UINT16 color);
	/*TODO*////} praster = { NULL };
        
        public static void draw_character(osd_bitmap bitmap,int ybegin, int yend, UBytePtr font, int y, int x, char[] color){
            if (praster_draw_character == 8){
                praster_draw_character8(bitmap, ybegin, yend, font, y, x, color);
            } else {
                praster_draw_character16(bitmap, ybegin, yend, font, y, x, color);
            }
        }
        
        public static void draw_bytecode(osd_bitmap bitmap, int code, int y, int x, char[] color){
            if (praster_draw_bytecode == 8){
                praster_draw_bytecode8(bitmap, code, y, x, color);
            } else {
                praster_draw_bytecode16(bitmap, code, y, x, color);
            }
        }

        public static void draw_pixel(osd_bitmap bitmap, int y, int x, int color){
            if (praster_draw_pixel == 8){
                praster_draw_pixel8(bitmap, y, x, color);
            } else {
                praster_draw_pixel16(bitmap, y, x, color);
            }
        }
	
	static void praster_videoram_w(PRASTER This, int offset, int data)
	{
		offset=This.memory.mask;
		if (This.memory.ram.read(offset)!=data) {
		   This.memory.ram.write(offset, data);
		   if ((offset>=This.memory.videoram_offset)
			   &&(offset<This.memory.videoram_offset+This.memory.videoram_size) )
			   This.text.dirtybuffer.write(offset, 1);
		   if ( (offset>=This.memory.colorram_offset)
				&&(offset<This.memory.colorram_offset+This.memory.colorram_size))
			   This.text.dirtybuffer.write(offset-This.memory.colorram_offset, 1);
		}
	}
	
	static int praster_videoram_r(PRASTER This, int offset)
	{
		return This.memory.ram.read(offset&This.memory.mask);
	}
	
	public static	void praster_draw_pixel8(osd_bitmap bitmap, int y, int x, int color)
	{
		bitmap.line[y].write(x, color);
                //plot_pixel.handler(bitmap, y, x, color);
	}
	
	public static	void praster_draw_pixel16(osd_bitmap bitmap, int y, int x, int color)
	{
		//*((short *) bitmap.line[y] + x) = color;
                bitmap.line[y].write(x, color);
	}
	
	public static void praster_draw_bytecode8(osd_bitmap bitmap, int code, int y, int x, char[] color)
	{
		bitmap.line[y].write(x, color[code >> 7]);
		bitmap.line[y].write(1 + x, color[(code >> 6) & 1]);
		bitmap.line[y].write(2 + x, color[(code >> 5) & 1]);
		bitmap.line[y].write(3 + x, color[(code >> 4) & 1]);
		bitmap.line[y].write(4 + x, color[(code >> 3) & 1]);
		bitmap.line[y].write(5 + x, color[(code >> 2) & 1]);
		bitmap.line[y].write(6 + x, color[(code >> 1) & 1]);
		bitmap.line[y].write(7 + x, color[code & 1]);
	}
	
	public static void praster_draw_bytecode16(osd_bitmap bitmap, int code, int y, int x, char[] color)
	{
		bitmap.line[y].write(x, color[code >> 7]);
		bitmap.line[y].write(1 + x, color[(code >> 6) & 1]);
		bitmap.line[y].write(2 + x, color[(code >> 5) & 1]);
		bitmap.line[y].write(3 + x, color[(code >> 4) & 1]);
		bitmap.line[y].write(4 + x, color[(code >> 3) & 1]);
		bitmap.line[y].write(5 + x, color[(code >> 2) & 1]);
		bitmap.line[y].write(6 + x, color[(code >> 1) & 1]);
		bitmap.line[y].write(7 + x, color[code & 1]);
	}
	
	public static void praster_draw_character8(osd_bitmap bitmap, int ybegin, int yend,
		UBytePtr font, int y, int x, char[] color)
	{
		int i, code;
		for (i = ybegin; i <= yend; i++)
			{
				code=font.read(i);
				bitmap.line[y + i].write(x, color[code >> 7]);
				bitmap.line[y + i].write(1 + x, color[(code >> 6) & 1]);
				bitmap.line[y + i].write(2 + x, color[(code >> 5) & 1]);
				bitmap.line[y + i].write(3 + x, color[(code >> 4) & 1]);
				bitmap.line[y + i].write(4 + x, color[(code >> 3) & 1]);
				bitmap.line[y + i].write(5 + x, color[(code >> 2) & 1]);
				bitmap.line[y + i].write(6 + x, color[(code >> 1) & 1]);
				bitmap.line[y + i].write(7 + x, color[code & 1]);
			}
	}
	
	public static void praster_draw_character16(osd_bitmap bitmap, int ybegin, int yend,
			UBytePtr font, int y, int x, char[] color)
	{
		int i, code;
		for (i = ybegin; i <= yend; i++)
			{
				code=font.read(i);
				bitmap.line[y + i].write( x, color[code >> 7]);
				bitmap.line[y + i].write( 1 + x, color[(code >> 6) & 1]);
				bitmap.line[y + i].write( 2 + x, color[(code >> 5) & 1]);
				bitmap.line[y + i].write( 3 + x, color[(code >> 4) & 1]);
				bitmap.line[y + i].write( 4 + x, color[(code >> 3) & 1]);
				bitmap.line[y + i].write( 5 + x, color[(code >> 2) & 1]);
				bitmap.line[y + i].write( 6 + x, color[(code >> 1) & 1]);
				bitmap.line[y + i].write( 7 + x, color[code & 1]);
			}
	}
	
	static void praster_markdirty(PRASTER This, int px, int py)
	{
		int t1, t2;
		if (px<This.raytube.screenpos_x) return ;
		if (py<This.raytube.screenpos_y) return ;
		t1 = (px-This.raytube.screenpos_x)/This.text.charsize_x;
		if (t1>=This.text.size_x) return ;
		t2 = (py-This.raytube.screenpos_y)/This.text.charsize_y;
		if (t2>=This.text.size_y) return ;
		This.text.dirtybuffer.write(t2*This.text.size_x+t1, 1);
	}
	
	void praster_draw_text (PRASTER This, char[] text, int y)
	{
		int x, x0, y2, width = (Machine.visible_area.max_x - Machine.visible_area.min_x) / Machine.uifont.width;
	
		if (text[0] != 0)
		{
			x = (text.length);
			y -= Machine.uifont.height * ((x + width - 1) / width);
			y2 = y + Machine.uifont.height;
			x = 0;
			while ((text[x]) != 0)
			{
				for (x0 = Machine.visible_area.min_x;
					 ((text[x]!=0) && (x0 < (Machine.visible_area.max_x -
								 Machine.uifont.width)));
					 x++, x0 += Machine.uifont.width)
				{
					drawgfx (This.display.bitmap, Machine.uifont,
							 text[x], 0, 0, 0, x0, y2, null,
							 TRANSPARENCY_NONE, 0);
					/* i hope its enough to mark the chars under the four edge as
					   dirty */
					praster_markdirty(This, x0, y2);
					praster_markdirty(This, x0+width-1, y2);
					praster_markdirty(This, x0, y2+Machine.uifont.height-1);
					praster_markdirty(This, x0+width-1, y2+Machine.uifont.height-1);
				}
				y2 += Machine.uifont.height;
			}
		}
	}
	
	static void praster_init (PRASTER This)
	{
		if (praster_draw_character==8) {
			if (Machine.color_depth == 8)
				{
					praster_draw_character=8;
					praster_draw_bytecode=8;
					praster_draw_pixel=8;
				}
			else
				{
					praster_draw_character=16;
					praster_draw_bytecode=16;
					praster_draw_pixel=16;
				}
		}
		This.on=1;
	}
	
	static void praster_update(PRASTER This)
	{
		//memset(raster2.text.dirtybuffer, 1, raster2.text.size_x*raster2.text.size_y);
                raster2.text.dirtybuffer = new UBytePtr(raster2.text.size_x*raster2.text.size_y);
	}
	
	static void praster_cursor_update(PRASTER This)
	{
		if (This.cursor.pos<This.text.size_x*This.text.size_y)
			This.text.dirtybuffer.write(This.cursor.pos, 1);
	}
	
	static int praster_raster_interrupt (PRASTER This)
	{
		This.raytube.current_y++;
		if (This.raytube.current_y>=This.raytube.size_y) {
			This.raytube.current_y=0;
			if ((This.cursor.on !=0) && (This.cursor.blinking != 0)) {
				This.cursor.counter++;
				if (This.cursor.counter>=This.cursor.delay) {
					This.cursor.counter=0;
					This.cursor.displayed=(This.cursor.displayed==1) ? 0 : 1;
					This.text.dirtybuffer.write(This.cursor.pos, 1);
				}
			}
		}
		return 0;
	}
	
	static void praster_raster_monotext (PRASTER This)
	{
		int i, j, k, sy, sx, ty, tx, code;
	
		for (i = 0, ty=0, sy = This.raytube.screenpos_y;
			 (ty<This.text.size_y)&&(sy<Machine.visible_area.max_y);
			 ty++, sy+=This.text.charsize_y, i+=This.linediff+This.text.size_x) {
			for (sx = This.raytube.screenpos_x, tx=0, j=i;
				 (tx<This.text.size_x)&&(sx<Machine.visible_area.max_x);
				 tx++, sx+=This.text.charsize_x, j++ ) {
				if (This.text.dirtybuffer.read(j) != 0) {
					code=This.memory.ram.read((This.memory.videoram_offset+j)&This.memory.videoram_mask);
					praster.draw_character
						(This.display.bitmap,0, This.text.visiblesize_y-1,
						 new UBytePtr(This.memory.ram,((This.memory.fontram_offset+
						 code*This.text.fontsize_y)
						 & This.memory.fontram_mask)),
						 sy, sx, This.monocolor);
					if ((This.cursor.on != 0)&&(This.cursor.pos==j)
						&&((This.cursor.blinking!=1)||(This.cursor.displayed!=0)) ) {
						for (k=This.cursor.ybegin;
							 (k<This.text.charsize_y)&&(k<=This.cursor.yend); k++) {
							praster.draw_bytecode(This.display.bitmap, 0xff, sy+k, sx,
												  This.monocolor);
						}
					}
					/*osd_mark_dirty (sx, sy, sx+7, sy+7, 0); */
					This.text.dirtybuffer.write(j, 0);
				}
			}
		}
	}
	
	static void praster_raster_text (PRASTER This)
	{
		int i, j, k, sy, sx, tx, ty, code, attr;
		char[] color=new char[2];
	
		for (i = 0, ty=0, sy = This.raytube.screenpos_y;
			 (ty<This.text.size_y)&&(sy<Machine.visible_area.max_y);
			 ty++, sy+=This.text.charsize_y, i+=This.linediff+This.text.size_x) {
			for (sx = This.raytube.screenpos_x, tx=0, j=i;
				 (tx<This.text.size_x)&&(sx<Machine.visible_area.max_x);
				 tx++, sx+=This.text.charsize_x, j++ ) {
				if ((This.text.dirtybuffer.read(j)) != 0) {
					code=This.memory.ram.read((This.memory.videoram_offset+j)
										 &This.memory.videoram_mask);
					attr=This.memory.ram.read((This.memory.colorram_offset+j)
										 &This.memory.colorram_mask);
					color[0]=This.display.pens[(attr>>4)&7];
					color[1]=This.display.pens[attr&0x0f];
					if ((attr & 0x80) != 0) code|=0x100;
					praster.draw_character
						(This.display.bitmap,0, This.text.visiblesize_y-1,
						 new UBytePtr(This.memory.ram, ((This.memory.fontram_offset+code*This.text.fontsize_y)& This.memory.fontram_mask)),
						 sy, sx, color);
					if ((This.cursor.on == 1)&&(This.cursor.pos==j)
						&&((This.cursor.blinking != 1)||(This.cursor.displayed==1)) ) {
						for (k=This.cursor.ybegin;
							 (k<This.text.charsize_y)&&(k<=This.cursor.yend); k++) {
							praster.draw_bytecode(This.display.bitmap, 0xff, sy+k, sx,
												  color);
						}
					}
					/*osd_mark_dirty (sx, sy, sx+7, sy+7, 0); */
					This.text.dirtybuffer.write(j, 0);
				}
			}
		}
	}
	
	static void praster_raster_gfxtext (PRASTER This)
	{
		int i, j, k, l, sy, sx, tx, ty, code;
	
		for (i = 0, ty=0, sy = This.raytube.screenpos_y;
			 (ty<This.text.size_y)&&(sy<Machine.visible_area.max_y);
			 ty++, sy+=This.text.charsize_y, i+=This.linediff+This.text.size_x) {
			for (sx = This.raytube.screenpos_x, tx=0, j=i;
				 (tx<This.text.size_x)&&(sx<Machine.visible_area.max_x);
				 tx++, sx+=This.text.charsize_x, j++ ) {
				if (This.text.dirtybuffer.read(j) != 0) {
					code=This.memory.ram.read((This.memory.videoram_offset+j)
										 &This.memory.videoram_mask)
						+This.memory.fontram_offset/This.text.fontsize_y;
	
					drawgfx (This.display.bitmap, Machine.gfx[0], code,
							 0, 0, 0, sx, sy, null, TRANSPARENCY_NONE, 0);
	
	//#if 0
					praster.draw_character
						(This.display.bitmap,0, This.text.visiblesize_y-1,
						 new UBytePtr(This.memory.ram, ((This.memory.fontram_offset+
											code*This.text.fontsize_y)
										   & This.memory.fontram_mask)),
						 sy, sx, This.display.pens);
	//#endif
					if ((This.cursor.on==1)&&(This.cursor.pos==j)
						&&((This.cursor.blinking==0)||(This.cursor.displayed==1)) ) {
						for (k=This.cursor.ybegin;
							 (k<This.text.charsize_y)&&(k<=This.cursor.yend); k++) {
							praster.draw_bytecode(This.display.bitmap, 0xff, sy+k, sx,
												  This.display.pens);
						}
					}
					if (This.text.charsize_x>This.text.visiblesize_x) {
						for (k=0;k<This.text.charsize_y;k++) {
							for (l=This.text.visiblesize_x;
								 l<This.text.visiblesize_x;l++) {
								praster.draw_pixel(This.display.bitmap, sy+k,
												  sx+l, This.display.pens[0]);
							}
						}
					}
					if (This.text.charsize_y>This.text.visiblesize_y) {
						for (k=This.text.visiblesize_y;
							 k<This.text.charsize_y;k++) {
							for (l=0;
								 l<This.text.visiblesize_x;l++) {
								praster.draw_pixel(This.display.bitmap, sy+k,
												  sx+l, This.display.pens[0]);
							}
						}
					}
					osd_mark_dirty (sx, sy, sx+This.text.charsize_x-1, sy+This.text.charsize_y-1, 0);
					This.text.dirtybuffer.write(j, 0);
				}
			}
		}
	}
	
	static void praster_raster_graphic (PRASTER This)
	{
		int i, sy, sx, tx, ty, code;
	
		for (i = 0, ty = 0, sy=This.raytube.screenpos_y;
			 (ty<This.text.size_y)&&(sy<Machine.visible_area.max_y);
			 sy++, ty++, i+=This.linediff ) {
			for (sx = This.raytube.screenpos_x, tx = 0;
				 (tx<This.text.size_x)&&(sx<Machine.visible_area.max_x);
				 tx+=8, sx+=8) {
				if (This.text.dirtybuffer.read(i) != 0) {
					code=This.memory.ram.read((This.memory.videoram_offset+i)
										 &This.memory.videoram_mask);
					praster.draw_bytecode (This.display.bitmap,
										   code, sy, sx, This.monocolor);
					This.text.dirtybuffer.write(i, 0);
				}
			}
		}
	}
	
	public static VhStartPtr praster_vh_start = new VhStartPtr() { public int handler() 
	{
		//if ((raster2.text.dirtybuffer=(UINT8*)malloc(0x10000))==0) return 1;
            
		raster1.display.pens=raster2.display.pens=Machine.pens;
		raster2.display.bitmap=Machine.scrbitmap;
	    return 0;
	} };
	
	public static VhStopPtr praster_vh_stop = new VhStopPtr() { public void handler() 
	{
	} };
	
	public static InterruptPtr praster_vretrace_irq = new InterruptPtr() { public int handler() 
	{
		return 0;
	} };
	
	
	public static InterruptPtr praster_raster_irq = new InterruptPtr() { public int handler() 
	{
		if ((raster1.on) != 0) {
			praster_raster_interrupt(raster1);
		}
		if ((raster2.on) != 0) {
			praster_raster_interrupt(raster2);
		}
		return 0;
	} };
	
	public static VhUpdatePtr praster_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
	{
		rectangle vis=new rectangle();
                
		if (raster2.display.no_rastering != 0) return;
	
		if( palette_recalc() != null)
			full_refresh = 1;
	
		if (full_refresh != 0) {
			//memset(raster2.text.dirtybuffer, 1, raster2.text.size_x*raster2.text.size_y);
                        raster2.text.dirtybuffer = new UBytePtr(raster2.text.size_x*raster2.text.size_y);
                }
	
		if (raster2.mode==PRASTER_MODE.PRASTER_MONOTEXT){
                    praster_raster_monotext(raster2);
                } else if (raster2.mode==PRASTER_MODE.PRASTER_TEXT) {
                    praster_raster_text(raster2);
                } else if (raster2.mode==PRASTER_MODE.PRASTER_GFXTEXT) {
                    praster_raster_gfxtext(raster2);
                } else if (raster2.mode==PRASTER_MODE.PRASTER_GRAPHIC) {
                    praster_raster_graphic(raster2);
                }
                                
		if (raster2.raytube.screenpos_x>0) {
			vis.min_x=0;
			vis.max_x=raster2.raytube.screenpos_x-1;
			vis.min_y=0;
			vis.max_y=Machine.visible_area.max_y;
			fillbitmap(raster2.display.bitmap, raster2.raytube.framecolor,vis);
		}
		if (raster2.raytube.screenpos_x+raster2.text.size_x*raster2.text.charsize_x<
			Machine.visible_area.max_x) {
			vis.min_x=raster2.raytube.screenpos_x
				+raster2.text.size_x*raster2.text.charsize_x;
			vis.max_x=Machine.visible_area.max_x;
			vis.min_y=0;
			vis.max_y=Machine.visible_area.max_y;
			fillbitmap(raster2.display.bitmap, raster2.raytube.framecolor,vis);
		}
		if (raster2.raytube.screenpos_y>0) {
			vis.min_y=0;
			vis.max_y=raster2.raytube.screenpos_y-1;
			vis.min_x=0;
			vis.max_x=Machine.visible_area.max_x;
			fillbitmap(raster2.display.bitmap, raster2.raytube.framecolor,vis);
		}
		if (raster2.raytube.screenpos_y+raster2.text.size_y*raster2.text.charsize_y<
			Machine.visible_area.max_y) {
			vis.min_y=raster2.raytube.screenpos_y
				+raster2.text.size_y*raster2.text.charsize_y;
			vis.max_y=Machine.visible_area.max_y;
			vis.min_x=0;
			vis.max_x=Machine.visible_area.max_x;
			fillbitmap(raster2.display.bitmap, raster2.raytube.framecolor,vis);
		}
		if (raster2.display_state != null) raster2.display_state=raster2;
	} };
	
	public static void praster_1_init () { praster_init(raster1); }
	public static void praster_2_init () { praster_init(raster2); }
	
	//extern WRITE_HANDLER ( praster_1_videoram_w )
        public static WriteHandlerPtr praster_1_videoram_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
                praster_videoram_w(raster1, offset, data);
        }};
	        
	//extern WRITE_HANDLER ( praster_2_videoram_w )
	public static WriteHandlerPtr praster_2_videoram_w = new WriteHandlerPtr() {
            public void handler(int offset, int data) {
                praster_videoram_w(raster2, offset, data);
        }};
	
	//extern READ_HANDLER ( praster_1_videoram_r )
	public static ReadHandlerPtr praster_1_videoram_r = new ReadHandlerPtr() {
            public int handler(int offset) {
                return praster_videoram_r(raster1, offset);
        }};
	
        //extern READ_HANDLER ( praster_2_videoram_r )
        public static ReadHandlerPtr praster_2_videoram_r = new ReadHandlerPtr() {
            public int handler(int offset) {
                return praster_videoram_r(raster2, offset);
        }};
	
	public static void praster_1_update()
	{ 
            praster_update(raster1); 
        }
	
        public static void praster_2_update()
	{ 
            praster_update(raster2); 
        }
	
        public static void praster_1_cursor_update()
	{ 
            praster_cursor_update(raster1); 
        }
	
        public static void praster_2_cursor_update()
	{ 
            praster_cursor_update(raster2); 
        }
}
