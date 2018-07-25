/***************************************************************************
        Border engine:

        Functions for drawing multi-coloured screen borders using the
        Event List processing.

Changes:

28/05/2000 DJR - Initial implementation.
08/06/2000 DJR - Now only uses events with the correct ID value.
28/06/2000 DJR - draw_border now uses full_refresh flag.

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.vidhrdw;

import static mess.eventlst.*;
import static mess.eventlstH.*;
import WIP.mame.osdependH.osd_bitmap;

import static WIP.mame.mame.Machine;
import static old.mame.cpuintrf.*;
import static old.arcadeflex.osdepend.logerror;
import static old.mame.drawgfxH.*;
import static old.mame.drawgfx.*;

public class border
{
	
    /* Last border colour output in the previous frame */
    static int CurrBorderColor = 0;

    static int LastDisplayedBorderColor = -1; /* Negative value indicates redraw */

    /* Force the border to be redrawn on the next frame */
    public static void force_border_redraw ()
    {
            LastDisplayedBorderColor = -1;
    }

    /* Set the last border colour to have been displayed. Used when loading snap
       shots and to record the last colour change in a frame that was skipped. */
    public static void set_last_border_color (int NewColor)
    {
            CurrBorderColor = NewColor;
    }

    public static void draw_border(osd_bitmap bitmap,
            int full_refresh,               /* Full refresh flag */
            int TopBorderLines,             /* Border lines before actual screen */
            int ScreenLines,                /* Screen height in pixels */
            int BottomBorderLines,          /* Border lines below screen */
            int LeftBorderPixels,           /* Border pixels to the left of each screen line */
            int ScreenPixels,               /* Width of actual screen in pixels */
            int RightBorderPixels,          /* Border pixels to the right of each screen line */
            int LeftBorderCycles,           /* Cycles taken to draw left border of each scan line */
            int ScreenCycles,               /* Cycles taken to draw screen data part of each scan line */
            int RightBorderCycles,          /* Cycles taken to draw right border of each scan line */
            int HorizontalRetraceCycles,    /* Cycles taken to return to LHS of CRT after each scan line */
            int VRetraceTime,               /* Cycles taken before start of first border line */
            int EventID)                    /* Event ID of border messages */
    {
            EVENT_LIST_ITEM pItem;
            int TotalScreenHeight = TopBorderLines+ScreenLines+BottomBorderLines;
            int TotalScreenWidth = LeftBorderPixels+ScreenPixels+RightBorderPixels;
            int DisplayCyclesPerLine = LeftBorderCycles+ScreenCycles+RightBorderCycles;
            int CyclesPerLine = DisplayCyclesPerLine+HorizontalRetraceCycles;
            int CyclesSoFar = 0;
            int NumItems, CurrItem = 0, NextItem;
            int Count, ScrX, NextScrX, ScrY;
            rectangle r=new rectangle();

            pItem = EventList_GetFirstItem();
            NumItems = EventList_NumEvents();
            
            if (NumItems != 0)
            {
                            //int CyclesPerFrame = (int)(Machine.drv.cpu[0].cpu_clock / Machine.screen[0].refresh);
                            int CyclesPerFrame = (int)(Machine.drv.cpu[0].cpu_clock / Machine.drv.frames_per_second);
                            logerror ("Event count = %ld, curr cycle = %ld, total cycles = %ld \n", NumItems, cpu_getcurrentcycles(), CyclesPerFrame);
            }
            /*for (Count = 0; Count < NumItems; Count++)
            {
                    logerror ("Event no %05d, ID = %04x, data = %04x, time = %ld\n", Count, pItem[Count].Event_ID, pItem[Count].Event_Data, (long) pItem[Count].Event_Time);
            }*/

            /* Find the first and second events with the correct ID */
            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID))
                            CurrItem++;
            NextItem = CurrItem + 1;
            while ((NextItem < NumItems) && (pEventListBuffer[NextItem].Event_ID != EventID))
                            NextItem++;

            /* Single border colour */
            if ((CurrItem < NumItems) && (NextItem >= NumItems))
                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;

            if ((NextItem >= NumItems) && (CurrBorderColor==LastDisplayedBorderColor) && (full_refresh ==0))
            {
                            /* Do nothing if border colour has not changed */
            }
            else if (NextItem >= NumItems)
            {
                            /* Single border colour - this is not strictly correct as the
                                    colour change may have occurred midway through the frame
                                    or after the last visible border line however the whole
                                    border would be redrawn in the correct colour during the
                                    next frame anyway! */
                            r.min_x = 0;
                            r.max_x = TotalScreenWidth-1;
                            r.min_y = 0;
                            r.max_y = TopBorderLines-1;
                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);

                            r.min_x = 0;
                            r.max_x = LeftBorderPixels-1;
                            r.min_y = TopBorderLines;
                            r.max_y = TopBorderLines+ScreenLines-1;
                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);

                            r.min_x = LeftBorderPixels+ScreenPixels;
                            r.max_x = TotalScreenWidth-1;
                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);

                            r.min_x = 0;
                            r.max_x = TotalScreenWidth-1;
                            r.min_y = TopBorderLines+ScreenLines;
                            r.max_y = TotalScreenHeight-1;
                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);

                            logerror ("Setting border colour to %d (Last = %d, Full Refresh = %d)\n", CurrBorderColor, LastDisplayedBorderColor, full_refresh);
                            LastDisplayedBorderColor = CurrBorderColor;
            }
            else
            {
                            /* Multiple border colours */

                            /* Process entries before first displayed line */
                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time <= VRetraceTime))
                            {
                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                            do {
                                                            CurrItem++;
                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                            }

                            /* Draw top border */
                            CyclesSoFar = VRetraceTime;
                            for (ScrY = 0; ScrY < TopBorderLines; ScrY++)
                            {
                                            r.min_x = 0;
                                            r.min_y = r.max_y = ScrY;
                                            if ((CurrItem >= NumItems) || (pEventListBuffer[CurrItem].Event_Time >= (CyclesSoFar+DisplayCyclesPerLine)))
                                            {
                                                            /* Single colour on line */
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }
                                            else
                                            {
                                                            /* Multiple colours on a line */
                                                            //ScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                            Float f = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                            ScrX = f.intValue();
                                                            
                                                            r.max_x = ScrX-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));

                                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time < (CyclesSoFar+DisplayCyclesPerLine)))
                                                            {
                                                                            //NextScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                                            Float f2 = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                                            NextScrX = f2.intValue();
                                                                            r.min_x = ScrX;
                                                                            r.max_x = NextScrX-1;
                                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                                            ScrX = NextScrX;
                                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                                            do {
                                                                                            CurrItem++;
                                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                                            }
                                                            r.min_x = ScrX;
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }

                                            /* Process colour changes during horizontal retrace */
                                            CyclesSoFar+= CyclesPerLine;
                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time <= CyclesSoFar))
                                            {
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                            }
                            }

                            /* Draw left and right borders next to screen lines */
                            for (ScrY = TopBorderLines; ScrY < (TopBorderLines+ScreenLines); ScrY++)
                            {
                                            /* Draw left hand border */
                                            r.min_x = 0;
                                            r.min_y = r.max_y = ScrY;

                                            if ((CurrItem >= NumItems) || (pEventListBuffer[CurrItem].Event_Time >= (CyclesSoFar+LeftBorderCycles)))
                                            {
                                                            /* Single colour */
                                                            r.max_x = LeftBorderPixels-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }
                                            else
                                            {
                                                            /* Multiple colours */
                                                            //ScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)LeftBorderPixels / (float)LeftBorderCycles;
                                                            Float f3 = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)LeftBorderPixels / (float)LeftBorderCycles;
                                                            ScrX = f3.intValue();
                                        
                                                            r.max_x = ScrX-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));

                                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time < (CyclesSoFar+LeftBorderCycles)))
                                                            {
                                                                            //NextScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)LeftBorderPixels / (float)LeftBorderCycles;
                                                                            Float f4 = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)LeftBorderPixels / (float)LeftBorderCycles;
                                                                            NextScrX = f4.intValue();
                                                                
                                                                            r.min_x = ScrX;
                                                                            r.max_x = NextScrX-1;
                                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                                            ScrX = NextScrX;
                                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                                            do {
                                                                                            CurrItem++;
                                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                                            }
                                                            r.min_x = ScrX;
                                                            r.max_x = LeftBorderPixels-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }

                                            /* Process colour changes during screen draw */
                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time <= (CyclesSoFar+LeftBorderCycles+ScreenCycles)))
                                            {
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                            }

                                            /* Draw right hand border */
                                            r.min_x = LeftBorderPixels+ScreenPixels;
                                            if ((CurrItem >= NumItems) || (pEventListBuffer[CurrItem].Event_Time >= (CyclesSoFar+DisplayCyclesPerLine)))
                                            {
                                                            /* Single colour */
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }
                                            else
                                            {
                                                            /* Multiple colours */
                                                            //ScrX = LeftBorderPixels + ScreenPixels + (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)RightBorderPixels / (float)RightBorderCycles;
                                                            Float f5 = (pEventListBuffer[CurrItem].Event_Time  - CyclesSoFar) * (float)RightBorderPixels / (float)RightBorderCycles;
                                                            ScrX = LeftBorderPixels + ScreenPixels + f5.intValue();
                                        
                                                            r.max_x = ScrX-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));

                                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time < (CyclesSoFar+DisplayCyclesPerLine)))
                                                            {
                                                                            //NextScrX = LeftBorderPixels + ScreenPixels + (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)RightBorderPixels / (float)RightBorderCycles;
                                                                            Float f6 = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)RightBorderPixels / (float)RightBorderCycles;
                                                                            NextScrX = LeftBorderPixels + ScreenPixels + f6.intValue();
                                                                            
                                                                            r.min_x = ScrX;
                                                                            r.max_x = NextScrX-1;
                                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                                            ScrX = NextScrX;
                                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                                            do {
                                                                                            CurrItem++;
                                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                                            }
                                                            r.min_x = ScrX;
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }

                                            /* Process colour changes during horizontal retrace */
                                            CyclesSoFar+= CyclesPerLine;
                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time <= CyclesSoFar))
                                            {
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                            }
                            }

                            /* Draw bottom border */
                            for (ScrY = TopBorderLines+ScreenLines; ScrY < TotalScreenHeight; ScrY++)
                            {
                                            r.min_x = 0;
                                            r.min_y = r.max_y = ScrY;
                                            if ((CurrItem >= NumItems) || (pEventListBuffer[CurrItem].Event_Time >= (CyclesSoFar+DisplayCyclesPerLine)))
                                            {
                                                            /* Single colour on line */
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }
                                            else
                                            {
                                                            /* Multiple colours on a line */
                                                            //ScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                            Float f7 = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                            ScrX = f7.intValue();
                                        
                                                            r.max_x = ScrX-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));

                                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time < (CyclesSoFar+DisplayCyclesPerLine)))
                                                            {
                                                                            //NextScrX = (int)(pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                                            Float f8 = (pEventListBuffer[CurrItem].Event_Time - CyclesSoFar) * (float)TotalScreenWidth / (float)DisplayCyclesPerLine;
                                                                            NextScrX = f8.intValue();
                                                                            
                                                                            r.min_x = ScrX;
                                                                            r.max_x = NextScrX-1;
                                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                                                            ScrX = NextScrX;
                                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                                            do {
                                                                                            CurrItem++;
                                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                                            }
                                                            r.min_x = ScrX;
                                                            r.max_x = TotalScreenWidth-1;
                                                            fillbitmap(bitmap, Machine.pens[CurrBorderColor], r);
                                            }

                                            /* Process colour changes during horizontal retrace */
                                            CyclesSoFar+= CyclesPerLine;
                                            while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_Time <= CyclesSoFar))
                                            {
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                                            do {
                                                                            CurrItem++;
                                                            } while ((CurrItem < NumItems) && (pEventListBuffer[CurrItem].Event_ID != EventID));
                                            }
                            }

                            /* Process colour changes after last displayed line */
                            while (CurrItem < NumItems)
                            {
                                            if (pEventListBuffer[CurrItem].Event_ID == EventID)
                                                            CurrBorderColor = pEventListBuffer[CurrItem].Event_Data;
                                            CurrItem++;
                            }

                            /* Set value to ensure redraw on next frame */
                            LastDisplayedBorderColor = -1;

                            logerror ("Multi coloured border drawn (last colour = %d)\n", CurrBorderColor);
            }

            /* Assume all other routines have processed their data from the list */
            EventList_Reset();
            //EventList_SetOffsetStartTime ( TIME_TO_CYCLES(0,cpu_getscanline()*cpu_getscanlineperiod()) );
            EventList_SetOffsetStartTime ( cpu_getcurrentcycles() );
    }
}
