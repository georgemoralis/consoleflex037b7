/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.includes;

public class amstradH
{
	
	//#define AMSTRAD_VIDEO_USE_EVENT_LIST
	
	
	//int amstrad_floppy_init(int);
	
	
	//int amstrad_snapshot_load(int);
	//int amstrad_snapshot_id(int);
	//void amstrad_snapshot_exit(int);
	
	//int amstrad_floppy_load(int);
	//int amstrad_floppy_id(int);
	//void amstrad_floppy_exit(int);
	
	
	
	//extern int amstrad_cassette_init(int id);
	//extern void amstrad_cassette_exit(int id);
	
	
	
	/* On the Amstrad, any part of the 64k memory can be access by the video
	hardware (GA and CRTC - the CRTC specifies the memory address to access,
	and the GA fetches 2 bytes of data for each 1us cycle.
	
	The Z80 must also access the same ram.
	
	To maintain the screen display, the Z80 is halted on each memory access.
	
	The result is that timing for opcodes, appears to fall into a nice pattern,
	where the time for each opcode can be measured in NOP cycles. NOP cycles is
	the name I give to the time taken for one NOP command to execute.
	
	This happens to be 1us.
	
	From measurement, there are 64 NOPs per line, with 312 lines per screen.
	This gives a total of 19968 NOPs per frame. */
	
	/* number of us cycles per frame (measured) */
	public static final int AMSTRAD_US_PER_FRAME	= 19968;
	public static final int AMSTRAD_FPS		= 50;
	
	
	
	/* These are the measured visible screen dimensions in CRTC characters.
	50 CRTC chars in X, 35 CRTC chars in Y (8 lines per char assumed) */
	public static final int AMSTRAD_SCREEN_WIDTH	= (50*16);
	public static final int AMSTRAD_SCREEN_HEIGHT	= (35*8);
	public static final int AMSTRAD_MONITOR_SCREEN_WIDTH	= (64*16);
	public static final int AMSTRAD_MONITOR_SCREEN_HEIGHT	= (39*8);
	
	//#if AMSTRAD_VIDEO_USE_EVENT_LIST || 1
	/* codes for eventlist */
        public static final int EVENT_LIST_CODE_GA_COLOUR=0;
	public static final int EVENT_LIST_CODE_GA_MODE=1;
	public static final int EVENT_LIST_CODE_CRTC_WRITE=2;
	public static final int EVENT_LIST_CODE_CRTC_INDEX_WRITE=3;
        
	public static enum AMSTRAD_VIDEO_USE_EVENT_LIST
	{
		/* change pen colour with gate array */
		EVENT_LIST_CODE_GA_COLOUR,
		/* change mode with gate array */
		EVENT_LIST_CODE_GA_MODE,
		/* change CRTC register data */
		EVENT_LIST_CODE_CRTC_WRITE,
		/* change CRTC register selection */
	        EVENT_LIST_CODE_CRTC_INDEX_WRITE
	};
	//#endif
	
	//void amstrad_vh_execute_crtc_cycles(int crtc_execute_cycles);
	//void amstrad_vh_update_colour(int,int);
	//void amstrad_vh_update_mode(int);
	
	/* update interrupt timer */
	/* if start of vsync sound, wait to reset interrupt counter 2 hsyncs later */
	}
