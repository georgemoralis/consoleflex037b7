/***************************************************************************

  amstrad.c.c

  Functions to emulate the video hardware of the amstrad CPC.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

import static mess.vidhrdw.crtc6845.*;
import static mess.vidhrdw.m6845H.*;
import static mess.vidhrdw.m6845.*;
import static WIP.mame.osdependH.*;
import static mess.machine.amstrad.*;
import static mess.includes.amstradH.*;
import static WIP.arcadeflex.fucPtr.*;
import static WIP.arcadeflex.libc_v2.*;
import static mess.systems.amstrad.*;
import static old.mame.cpuintrf.*;
import static old.mame.drawgfxH.*;
import static WIP.mame.drawgfx.*;
import static old.mame.drawgfx.*;
import static WIP.mame.common.*;
import static old.arcadeflex.video.*;

import static mess.eventlstH.*;
import static mess.eventlst.*;

import static WIP.mame.mame.*;

import static WIP.arcadeflex.libc.memcpy.memcpy;

public class amstrad
{
	
      /***************************************************************************
        Start the video hardware emulation.
      ***************************************************************************/


      /*************************************************************************/
      /* Amstrad CPC 

      The Amstrad CPC has a fixed palette of 27 colours generated from 3 levels of Red, 
      Green and Blue.

      The hardware allows selection of 32 colours, but these extra colours are copies
      of existing colours.
      */ 
        
        
      static char[] amstrad_colour_table =
      {
              0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
              16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
              29, 30, 31
      };
      
      static crtc6845_state amstrad_vidhrdw_6845_state;

      char[] amstrad_palette =
      {
              0x080, 0x080, 0x080,			   /* white */
              0x080, 0x080, 0x080,			   /* white */
              0x000, 0x0ff, 0x080,			   /* sea green */
              0x0ff, 0x0ff, 0x080,			   /* pastel yellow */
              0x000, 0x000, 0x080,			   /* blue */
              0x0ff, 0x000, 0x080,			   /* purple */
              0x000, 0x080, 0x080,			   /* cyan */
              0x0ff, 0x080, 0x080,			   /* pink */
              0x0ff, 0x000, 0x080,			   /* purple */
              0x0ff, 0x0ff, 0x080,			   /* pastel yellow */
              0x0ff, 0x0ff, 0x000,			   /* bright yellow */
              0x0ff, 0x0ff, 0x0ff,			   /* bright white */
              0x0ff, 0x000, 0x000,			   /* bright red */
              0x0ff, 0x000, 0x0ff,			   /* bright magenta */
              0x0ff, 0x080, 0x000,			   /* orange */
              0x0ff, 0x080, 0x0ff,			   /* pastel magenta */
              0x000, 0x000, 0x080,			   /* blue */
              0x000, 0x0ff, 0x080,			   /* sea green */
              0x000, 0x0ff, 0x000,			   /* bright green */
              0x000, 0x0ff, 0x0ff,			   /* bright cyan */
              0x000, 0x000, 0x000,			   /* black */
              0x000, 0x000, 0x0ff,			   /* bright blue */
              0x000, 0x080, 0x000,			   /* green */
              0x000, 0x080, 0x0ff,			   /* sky blue */
              0x080, 0x000, 0x080,			   /* magenta */
              0x080, 0x0ff, 0x080,			   /* pastel green */
              0x080, 0x0ff, 0x080,			   /* lime */
              0x080, 0x0ff, 0x0ff,			   /* pastel cyan */
              0x080, 0x000, 0x000,			   /* Red */
              0x080, 0x000, 0x0ff,			   /* mauve */
              0x080, 0x080, 0x000,			   /* yellow */
              0x080, 0x080, 0x0ff,			   /* pastel blue */
      };


      /* Initialise the palette */
      void amstrad_cpc_init_palette(char[] sys_palette, char[] sys_colortable, char[] color_prom)
      {
              memcpy(sys_palette, amstrad_palette, sizeof (amstrad_palette));
              
              memcpy(sys_colortable, amstrad_colour_table, sizeof (amstrad_colour_table));
      }

      /*************************************************************************/
      /* KC Compact

      The palette is defined by a colour rom. The only rom dump that exists (from the KC-Club webpage)
      is 2K, which seems correct. In this rom the same 32 bytes of data is repeated throughout the rom.

      When a I/O write is made to "Gate Array" to select the colour, Bit 7 and 6 are used by the 
      "Gate Array" to define the function, bit 7 = 0, bit 6 = 1. In the  Amstrad CPC, bits 4..0 
      define the hardware colour number, but in the KC Compact, it seems bits 5..0 
      define the hardware colour number allowing 64 colours to be chosen.

      It is possible therefore that the colour rom could be reprogrammed, so that other colour
      selections could be chosen allowing 64 different colours to be used. But this has not been tested
      and co

      colour rom byte:

      Bit Function 
      7 not used 
      6 not used 
      5,4 Green value
      3,2 Red value
      1,0 Blue value

      Green value, Red value, Blue value: 0 = 0%, 01/10 = 50%, 11 = 100%.
      The 01 case is not used, it is unknown if this produces a different amount of colour.
      */ 

      char kccomp_get_colour_element(int colour_value)
      {
              switch (colour_value)
              {
                      case 0:
                              return 0x00;
                      case 1:
                              return 0x60;
                      case 2:
                              return 0x60;
                      case 3:
                              return 0x0ff;
              }

              return 0x0ff;
      }


      /* the colour rom has the same 32 bytes repeated, but it might be possible to put a new rom in
      with different data and be able to select the other entries - not tested on a real kc compact yet
      and not supported by this driver */
      void kccomp_init_palette(char[] sys_palette, char[] sys_colortable, char[] color_prom)
      {
              int i;
              int rgb_index = 0;

              /*TODO*/////color_prom = color_prom+0x018000;

              for (i=0; i<32; i++)
              {
                      /*TODO*/////sys_colortable[i] = i;
                      sys_palette[rgb_index] = kccomp_get_colour_element((color_prom[i]>>2) & 0x03);
                      rgb_index++;
                      sys_palette[rgb_index] = kccomp_get_colour_element((color_prom[i]>>4) & 0x03);
                      rgb_index++;
                      sys_palette[rgb_index] = kccomp_get_colour_element((color_prom[i]>>0) & 0x03);
                      rgb_index++;
              }
      }


      /********************************************
      Amstrad Plus

      The Amstrad Plus has a 4096 colour palette
      *********************************************/


      void amstrad_plus_init_palette(char[] palette, char[] colortable,char[] color_prom) 
      {
              int i;

              for ( i = 0; i < 0x1000; i++ ) 
              {
                      int r, g, b;

                      r = ( i >> 8 ) & 0x0f;
                      g = ( i >> 4 ) & 0x0f;
                      b = i & 0x0f;

                      r = ( r << 4 ) | ( r );
                      g = ( g << 4 ) | ( g );
                      b = ( b << 4 ) | ( b );

                      palette[i] = (char) r;
                      palette[i+1] = (char) g;
                      palette[i+2] = (char) b;

                      colortable[i] = (char)i;
              }
      }


      /* this contains the colours in Machine->pens form.*/
      /* this is updated from the eventlist and reflects the current state
      of the render colours - these may be different to the current colour palette values */
      /* colours can be changed at any time and will take effect immediatly */
      static int[] amstrad_render_colours=new int[17];

      //#ifndef AMSTRAD_VIDEO_EVENT_LIST
      static osd_bitmap	amstrad_bitmap;
      //#endif

      /* the mode is re-loaded at each HSYNC */
      /* current mode to render */
      static int amstrad_render_mode;

      static int amstrad_vsync;

      /* current programmed mode */
      static int amstrad_current_mode;

      static int[] Mode0Lookup=new int[256];
      static int[] Mode1Lookup=new int[256];
      static int[] Mode3Lookup=new int[256];

      static void amstrad_init_lookups()
      {
              int i;

              for (i=0; i<256; i++)
              {
                      int pen;

                      pen = (
                              ((i & (1<<7))>>(7-0)) |
                              ((i & (1<<3))>>(3-1)) |
                              ((i & (1<<5))>>(5-2)) |
                              ((i & (1<<1))<<2)
                              );

                      Mode0Lookup[i] = pen;

                      Mode3Lookup[i] = pen & 0x03;

                      pen = (
                              ( ( (i & (1<<7)) >>7) <<0) |
                              ( ( (i & (1<<3)) >>3) <<1)
                              );

                      Mode1Lookup[i] = pen;

              }
      }

      //extern unsigned char *Amstrad_Memory;

      static int x_screen_offset=0;

      /* there are about 21 lines of monitor retrace */
      static int y_screen_offset=-21;

      static int amstrad_HSync=0;
      static int amstrad_VSync=0;
      static int amstrad_Character_Row=0;
      static int amstrad_DE=0;


      //static unsigned char *amstrad_Video_RAM;
      static UBytePtr amstrad_display;
      //static struct mame_bitmap *amstrad_bitmap;

      static int x_screen_pos;
      static int y_screen_pos;

      static int draw_function=0;
      
      public static void amstrad_draw_screen_enabled()
      {
              osd_bitmap bitmap = amstrad_bitmap;
              int x = x_screen_pos;
              int y = y_screen_pos;

              int ma, ra;
              int addr;
              int byte1, byte2;

              ma = crtc6845_memory_address_r(0);
              ra = crtc6845_row_address_r(0);

              /* calc mem addr to fetch data from
              based on ma, and ra */
              addr = (((ma>>(4+8)) & 0x03)<<14) |
                              ((ra & 0x07)<<11) |
                              ((ma & 0x03ff)<<1);

              /* amstrad fetches two bytes per CRTC clock. */
              byte1 = Amstrad_Memory.read(addr);
              byte2 = Amstrad_Memory.read(addr+1);

          /* depending on the mode! */
              switch (amstrad_render_mode)		
              {

                      /* mode 0 - low resolution - 16 colours */
                      case 0:
                      {
                              int cpcpen,messpen;
                              int data;

                              data = byte1;

                              {
                                      cpcpen = Mode0Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];

                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                                      data = data<<1;

                                      cpcpen = Mode0Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];

                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;                
                              }

                              data = byte2;

                              {
                                      cpcpen = Mode0Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;



                                      data = data<<1;

                                      cpcpen = Mode0Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;


                              }
                      }
                      break;

              /* mode 1 - medium resolution - 4 colours */
                      case 1:
                      {
                              int i;
                              int cpcpen;
                              int messpen;
                              int data;

                              data = byte1;

                              for (i=0; i<4; i++)
                              {
                                      cpcpen = Mode1Lookup[data & 0x0ff];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                                      data = data<<1;
                              }

                              data = byte2;

                              for (i=0; i<4; i++)
                              {
                                      cpcpen = Mode1Lookup[data & 0x0ff];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                                      data = data<<1;
                              }

                      }
                      break;

                      /* mode 2: high resolution - 2 colours */
                      case 2:
                      {
                              int i;
                              int Data = (byte1<<8) | byte2;
                              int cpcpen,messpen;

                              for (i=0; i<16; i++)
                              {
                                      cpcpen = (Data>>15) & 0x01;
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                                      Data = Data<<1;

                              }

                      }
                      break;

                      /* undocumented mode. low resolution - 4 colours */
                      case 3:
                      {
                              int cpcpen,messpen;
                              int data;

                              data = byte1;

                              {
                                      cpcpen = Mode3Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];

                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      data = data<<1;

                                      cpcpen = Mode3Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];

                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                              }

                              data = byte2;

                              {
                                      cpcpen = Mode3Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;


                                      data = data<<1;

                                      cpcpen = Mode3Lookup[data];
                                      messpen = amstrad_render_colours[cpcpen];
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;
                                      plot_pixel.handler(bitmap,x,y,messpen);
                                      x++;

                              }


                      }
                      break;

                      default:
                              break;
              }


      }

      public static void amstrad_draw_screen_disabled()
      {
              osd_bitmap bitmap = amstrad_bitmap;
              int x = x_screen_pos;
              int y = y_screen_pos;
              int border_colour;

              border_colour = amstrad_render_colours[16];	

              plot_box.handler(bitmap,x,y,16,1,border_colour);
      }


      /* Select the Function to draw the screen area */
      public static void amstrad_Set_VideoULA_DE()
      {
              if (amstrad_DE != 0)
              {
                      draw_function=1;
              } 
              else 
              {
                      draw_function=0;
              }
      }


      /************************************************************************
       * amstrad 6845 Outputs to Video ULA
       ************************************************************************/

      // called when the 6845 changes the character row
      public static void amstrad_Set_Character_Row(int offset, int data)
      {
              amstrad_Character_Row=data;
              amstrad_Set_VideoULA_DE();
      }

      /* the horizontal screen position on the display is determined
      by the length of the HSYNC and the position of the hsync */
      public static void amstrad_Set_HSync(int offset, int data)
      {
      //	if (amstrad_rendering)
      //	{

                      /* hsync changed state? */
                      if ((amstrad_HSync^data)!=0)
                      {

                                      if (data!=0)
                                      {
                                                                      /* start of hsync */
      //#ifndef AMSTRAD_VIDEO_EVENT_LIST
      //                amstrad_interrupt_timer_update();

      //#endif
                                              /* set new render mode */
                                              amstrad_render_mode = amstrad_current_mode;
                                      }
                                              else
                                              {
                                                              /* end of hsync */
                                                              y_screen_pos+=1;

                                                              if (y_screen_pos>312)
                                                              {
                                                                              y_screen_pos = 0;
                                                              }

                                                              if ((y_screen_pos>=0) && (y_screen_pos<AMSTRAD_SCREEN_HEIGHT))
                                                              {
                                                                              x_screen_pos=x_screen_offset;
                                                                              amstrad_display= new UBytePtr((amstrad_bitmap.line[y_screen_pos]), (x_screen_pos<<1));
                                                              }
                                              }
                      }
      //	}


              amstrad_HSync=data;
      }

      public static void amstrad_Set_VSync(int offset, int data)
      {

              amstrad_vsync = data;

      //        logerror("%d\r\n",amstrad_vsync);

          /* vsync changed state? */
          if ((amstrad_VSync^data)!=0)
              {
              if (data!=0)
              {
              //		if (amstrad_rendering)
              //		{
                                      y_screen_pos=y_screen_offset;

                                 if ((y_screen_pos>=0) && (y_screen_pos<=AMSTRAD_SCREEN_HEIGHT))
                                 {
                                              amstrad_display=new UBytePtr((amstrad_bitmap.line[y_screen_pos]),(x_screen_pos<<1));
                                      }
              //		}
              //		else
              //		{
                                 /* setup interrupt counter reset */
      //				 amstrad_interrupt_timer_trigger_reset_by_vsync();
              //		}
                      }
         }


              amstrad_VSync=data;

      }

      // called when the 6845 changes the Display Enabled
      public static void amstrad_Set_DE(int offset, int data)
      {
              amstrad_DE=data;
              amstrad_Set_VideoULA_DE();
      }


      /* The cursor is not used on Amstrad. The CURSOR signal is available on the Expansion port
      for other hardware to use, but as far as I know it is not used by anything. */

      /* use this when rendering */

      static crtc6845_interface amstrad6845= new crtc6845_interface() {

          @Override
          public void out_MA_func(int offset, int data) {
              //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

          @Override
          public void out_RA_func(int offset, int data) {
              amstrad_Set_Character_Row(offset, data);
          }

          @Override
          public void out_HS_func(int offset, int data) {
              amstrad_Set_HSync(offset, data);
          }

          @Override
          public void out_VS_func(int offset, int data) {
              amstrad_Set_VSync(offset, data);
          }

          @Override
          public void out_DE_func(int offset, int data) {
              amstrad_Set_DE(offset, data);
          }

          @Override
          public void out_CR_func(int offset, int data) {
              //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
          }

      };

      /* update the amstrad colours */
      public static void amstrad_vh_update_colour(int PenIndex, int hw_colour_index)
      {
              amstrad_render_colours[PenIndex] = Machine.pens[hw_colour_index];
      }

      /* update mode */
      public static void amstrad_vh_update_mode(int Mode)
      {
              amstrad_current_mode = Mode;
      }

      /* execute crtc_execute_cycles of crtc */
      public static void amstrad_vh_execute_crtc_cycles(int crtc_execute_cycles)
      {
          while (crtc_execute_cycles>0)
              {
                      /* check that we are on the emulated screen area. */
                      if ((x_screen_pos>=0) && (x_screen_pos<AMSTRAD_SCREEN_WIDTH) && (y_screen_pos>=0) && (y_screen_pos<AMSTRAD_SCREEN_HEIGHT))
                      {
                              /* render the screen */
                              draw_function();
                      }

              /* Move the CRT Beam on one 6845 character distance */
              x_screen_pos=x_screen_pos+16; 

      /*		if (x_screen_pos>800)
                      {
                              x_screen_pos = 0;
                      }
      */
                      amstrad_display=new UBytePtr(amstrad_display, 16); 


                      /* Clock the 6845 */
                      crtc6845_clock();
                      crtc_execute_cycles--;
              }
      }

      /************************************************************************
       * amstrad_vh_screenrefresh
       * resfresh the amstrad video screen
       ************************************************************************/
      public static VhUpdatePtr amstrad_vh_screenrefresh = new VhUpdatePtr() { public void handler(osd_bitmap bitmap,int full_refresh) 
      {
      //#ifndef AMSTRAD_VIDEO_EVENT_LIST
              rectangle rect=new rectangle();

              rect.min_x = 0;
              rect.max_x = AMSTRAD_SCREEN_WIDTH-1;
              rect.min_y = 0;
              rect.max_y = AMSTRAD_SCREEN_HEIGHT-1;


              copybitmap(bitmap, amstrad_bitmap, 0,0,0,0,rect, TRANSPARENCY_NONE,0); 

      /*#else
              int c;


              int crtc_execute_cycles;
              int num_cycles_remaining;
              EVENT_LIST_ITEM *pItem;
              int NumItemsRemaining;
              int previous_time;

              amstrad_rendering = 1;
              crtc6845_set_state(0, &amstrad_vidhrdw_6845_state);

              previous_time = 0;
              num_cycles_remaining = cycles_currently_ran()>>2;	//get19968; //cpu_getfperiod();

              amstrad_bitmap=bitmap;
              amstrad_display = amstrad_bitmap->line[0];
              c=0;

              // video_refresh is set if any of the 6845 or Video ULA registers are changed
              // this then forces a full screen redraw


              pItem = EventList_GetFirstItem();
              NumItemsRemaining = EventList_NumEvents();

              do
              {
                      if (NumItemsRemaining==0)
                      {
                              crtc_execute_cycles = num_cycles_remaining;
                              num_cycles_remaining = 0;
                      }
                      else
                      {
                              int time_delta;

                              
                              time_delta = pItem->Event_Time - previous_time;

                              crtc_execute_cycles = time_delta/4;

                              num_cycles_remaining -= time_delta/4;

              }

                      amstrad_vh_execute_crtc_cycles(crtc_execute_cycles);

                      if (NumItemsRemaining!=0)
                      {
                              switch ((pItem->Event_ID>>6) & 0x03)
                              {
                                      case EVENT_LIST_CODE_GA_COLOUR:
                                      {
                                              int PenIndex = pItem->Event_ID & 0x03f;
                                              int Colour = pItem->Event_Data;

                                              amstrad_vh_update_colour(PenIndex, Colour);
                                      }
                                      break;

                                      case EVENT_LIST_CODE_GA_MODE:
                                      {
                                              amstrad_vh_update_mode(pItem->Event_Data);
                                      }
                                      break;

                                      case EVENT_LIST_CODE_CRTC_INDEX_WRITE:
                                      {
                                              
                                              crtc6845_address_w(0,pItem->Event_Data);
                                      }
                                      break;

                                      case EVENT_LIST_CODE_CRTC_WRITE:
                                      {
                                              crtc6845_register_w(0, pItem->Event_Data);
                                      }
                                      break;

                                      default:
                                              break;
                              }

                              
                              previous_time = pItem->Event_Time;
                              pItem++;
                              NumItemsRemaining--;		
                      }
              }
              while (num_cycles_remaining>0);

          
          EventList_Reset();
          EventList_SetOffsetStartTime ( cycles_currently_ran() );

              crtc6845_get_state(0, &amstrad_vidhrdw_6845_state);
              amstrad_rendering = 0;
      #endif*/
      }};


      /************************************************************************
       * amstrad_vh_start
       * Initialize the amstrad video emulation
       ************************************************************************/
      public static VhStartPtr amstrad_vh_start = new VhStartPtr() { public int handler() 
      {
              int i;

              amstrad_init_lookups();

              crtc6845_start();
              crtc6845_config(amstrad6845);
              crtc6845_reset(0);
              crtc6845_get_state(0, amstrad_vidhrdw_6845_state);

              draw_function=0;


              /* 64 us Per Line, 312 lines (PAL) = 19968 */
          amstrad_render_mode = 0;
          amstrad_current_mode = 0;
          for (i=0; i<17; i++)
          {
                      amstrad_vh_update_colour(i, 0x014);
          }

      //#ifdef AMSTRAD_VIDEO_EVENT_LIST
      //        amstrad_rendering = 0;
      //        EventList_Initialise(19968);
      //#else
              amstrad_bitmap = bitmap_alloc_depth(AMSTRAD_SCREEN_WIDTH, AMSTRAD_SCREEN_HEIGHT,16);
              amstrad_display = amstrad_bitmap.line[0];
      //#endif

              return 0;

      }};

      /************************************************************************
       * amstrad_vh_stop
       * Shutdown the amstrad video emulation
       ************************************************************************/

      public static VhStopPtr amstrad_vh_stop = new VhStopPtr() { public void handler() 
      {
              crtc6845_stop();
      //#ifdef AMSTRAD_VIDEO_EVENT_LIST
      //        EventList_Finish();
      //#else
              if (amstrad_bitmap!=null)
              {
                      bitmap_free(amstrad_bitmap);
                      amstrad_bitmap = null;
              }
      //#endif

      }};

    public static void draw_function() {
        if (draw_function==0){
            amstrad_draw_screen_disabled();
        } else {
            amstrad_draw_screen_enabled();
        }
    }

}
