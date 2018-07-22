/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mame;

import static mame.eventlstH.*;
import static old.mame.cpuintrf.*;

import java.util.Vector;

public class eventlst
{
	
	/* current item */
	//static EVENT_LIST_ITEM *pCurrentItem;
        public static EVENT_LIST_ITEM pCurrentItem;
        
	/* number of items in buffer */
	public static int NumEvents = 0;
	
	/* size of the buffer - used to prevent buffer overruns */
	public static int TotalEvents = 0;
	
	/* the buffer */
	//static char *pEventListBuffer = NULL;
        //static EVENT_LIST_ITEM[] pEventListBuffer = null;
        public static Vector pEventListBuffer = new Vector();
        //static int currentItemCount = 0;
	
	/* Cycle count at last frame draw - used for timing offset calculations */
	public static int LastFrameStartTime = 0;
	
	public static int CyclesPerFrame=0;
	
	/* initialise */
	
	/* if the CPU is the controlling factor, the size of the buffer
	can be setup as:
	
	Number_of_CPU_Cycles_In_A_Frame/Minimum_Number_Of_Cycles_Per_Instruction */
	public static void EventList_Initialise(int NumEntries)
	{
	        /* stop memory leak if initialise accidently called twice */
		if (pEventListBuffer!=null){
			//free(pEventListBuffer);
                        pEventListBuffer=null;
                }
	
		//pEventListBuffer = malloc(sizeof(EVENT_LIST_ITEM)*NumEntries);
                //pEventListBuffer = new EVENT_LIST_ITEM[NumEntries];
                pEventListBuffer = new Vector();
	
		if (pEventListBuffer!=null)
		{
			EventList_Reset();
	                TotalEvents = NumEntries;
	//		return 1;
		}
	        else
	                TotalEvents = 0;
	//	return 0;
	}
	
	/* free event buffer */
	public static void EventList_Finish()
	{
		if (pEventListBuffer!=null)
		{
			//free(pEventListBuffer);
			pEventListBuffer = null;
		}
	        TotalEvents = 0;
		CyclesPerFrame = 0;
	}
	
	/* reset the change list */
	public static void EventList_Reset()
	{
		NumEvents = 0;
		//pCurrentItem = (EVENT_LIST_ITEM) pEventListBuffer;
                //currentItemCount = 0;
                //pCurrentItem = pEventListBuffer[currentItemCount];
                if (pEventListBuffer.size() > 0)
                    pCurrentItem = (EVENT_LIST_ITEM) pEventListBuffer.get(0);
                else
                    pCurrentItem = new EVENT_LIST_ITEM();
	}
	
	
	/* add an event to the buffer */
	public static void EventList_AddItem(int ID, int Data, int Time)
	{
	        if (NumEvents < TotalEvents)
	        {
	                /* setup item only if there is space in the buffer */
	                pCurrentItem.Event_ID = ID;
	                pCurrentItem.Event_Data = Data;
	                pCurrentItem.Event_Time = Time;
	
	                //pCurrentItem++;
                        pEventListBuffer.add( pCurrentItem );
                        
	                NumEvents++;
	        }
	}
	
	/* set the start time for use with EventList_AddItemOffset usually this will
	   be cpu_getcurrentcycles() at the time that the screen is being refreshed */
	public static void EventList_SetOffsetStartTime(int StartTime)
	{
	        LastFrameStartTime = StartTime;
	}
	
	/* add an event to the buffer with a time index offset from a specified time */
	public static void EventList_AddItemOffset(int ID, int Data, int Time)
	{
	
	        //if (!CyclesPerFrame)
                if (CyclesPerFrame ==0)
	                CyclesPerFrame = cpu_getfperiod();	//totalcycles();	//_(int)(Machine.drv.cpu[0].cpu_clock / Machine.drv.frames_per_second);
	
	        if (NumEvents < TotalEvents)
	        {
	                /* setup item only if there is space in the buffer */
	                pCurrentItem.Event_ID = ID;
	                pCurrentItem.Event_Data = Data;
	
	                Time -= LastFrameStartTime;
                        if ( ((Time < 0) || (Time == 0)) && (NumEvents !=0) )
                            Time+= CyclesPerFrame;
	                
                        pCurrentItem.Event_Time = Time;
	
	                //pCurrentItem++;
                        pEventListBuffer.add( pCurrentItem );
	                NumEvents++;
	        }
	}
	
	/* get number of events */
	public static int EventList_NumEvents()
	{
		return NumEvents;
	}
	
	/* get first item in buffer */
	//EVENT_LIST_ITEM *EventList_GetFirstItem(void)
        public static EVENT_LIST_ITEM EventList_GetFirstItem()
	{
		//return (EVENT_LIST_ITEM *)pEventListBuffer;
                //return pEventListBuffer[0];
                if ((pEventListBuffer !=null) && (pEventListBuffer.size()>0)){
                    if (pEventListBuffer.get(0) != null)
                        return (EVENT_LIST_ITEM) pEventListBuffer.get(0);
                }
                 
                return null;
	}
}
