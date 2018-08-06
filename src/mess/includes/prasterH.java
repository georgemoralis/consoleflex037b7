package mess.includes;

import static WIP.mame.osdependH.*;
import static WIP.arcadeflex.libc_v2.*;


public class prasterH {
    /***************************************************************************

    Pet's Rasterengine

    * first design goal, allow usage for two or more cathod ray tube in 1 driver
      use praster1 for first and praster2 for second video adapter
      if needed add praster3 and so on

    * allow implementation and usage of different rastering, caching
      strategies without change of video chip emulation

    * use same rastering, caching strategies for several chip with this driver

  ***************************************************************************/

  public static enum PRASTER_MODE {
          PRASTER_MONOTEXT, PRASTER_TEXT, PRASTER_GRAPHIC,
          PRASTER_GFXTEXT
  };
  
  /*
    text, monotext
    text.textsize.x number of char columns on screen
    text.textsize.y number of char lines on screen
    text.charsize.x text.charsize.y size of a char

    text.fontsize.y number of bytes in fontram per character
    text.vissize.x text.vissize.y size of the character bitmap used from fontram
  */

  /* basic structure for 1 cathod ray tube */
  public static class PRASTER {
          public int on;
          //void (*display_state)(struct _PRASTER *This); /* calls machine after rastering frame*/
          public PRASTER display_state;
      /* for rasterline, rastercolumn, and lightpen positions */
          public class struct_raytube {
                  //struct { int x, y; } size; /* Pixels */
                  public int size_x;
                  public int size_y;
                  //struct { int y; } current;
                  public int current_y;
                  //struct { int x, y; } screenpos;
                  public int screenpos_x;
                  public int screenpos_y;
                  
                  public int framecolor;
                  public int backgroundcolor;
          };
          
          public struct_raytube raytube = new struct_raytube();

          public class struct_display {
                  public int no_rastering; /* save time, and do not write to bitmap */
                  public osd_bitmap bitmap; /* output for rastered image */
  /*		struct { int x, y; } pos;    left upper position to be rendered in     */
  /*		struct { int x, y; } size;    size to be rendered    */
                  public char[] pens;
          };
          
          public struct_display display = new struct_display();

          public PRASTER_MODE mode; /* of the raster engine */

          public class struct_text {
                  public int reverse;
                  public int size_x, size_y;
                  public int charsize_x, charsize_y;
                  public int fontsize_y;
                  public int visiblesize_x, visiblesize_y;
                  public UBytePtr dirtybuffer;
          };
          
          public struct_text text = new struct_text();

          public class struct_graphic {
                  public int size_x, size_y;
          };
          
          public struct_graphic graphic = new struct_graphic();

          /* memory layout */
          public static class struct_memory {
                  public UBytePtr ram;
                  public int mask;
                  
                  public int videoram_offset;
                  public int videoram_mask;
                  public int videoram_size;
                  
                  public int colorram_offset;
                  public int colorram_mask;
                  public int colorram_size;
                  
                  public int fontram_offset;
                  public int fontram_mask;
                  public int fontram_size;
                  
          };
          
          public struct_memory memory = new struct_memory();

          public int linediff;
          public char[] monocolor=new char[2];

          public class struct_cursor {
                  public int on;
                  public int pos; /* position in text screen */
                  public int ybegin, yend; /* first charline filled with attr color, last charline */

                  public int blinking;
                  public int delay; /* blinkdelay in vertical retraces */
                  public int counter; /* delay counter */
                  public int displayed; /* cursor displayed */
          };
          
          public struct_cursor cursor = new struct_cursor();

          /* private area */
  };

  public static PRASTER raster1, raster2;

  
};
