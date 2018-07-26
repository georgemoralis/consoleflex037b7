/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess;

import static mess.eventlstH.*;
import static old.mame.cpuintrf.*;

import java.util.Vector;

public class eventlst
{
	
	/* current item */
	//static EVENT_LIST_ITEM *pCurrentItem;
        public static EVENT_LIST_ITEM pCurrentItem;
	/* number of items in buffer */
	static int NumEvents = 0;
	
	/* size of the buffer - used to prevent buffer overruns */
	static int TotalEvents = 0;
	
	/* the buffer */
	//static char *pEventListBuffer = NULL;
        public static EVENT_LIST_ITEM[] pEventListBuffer = null;
	
	/* Cycle count at last frame draw - used for timing offset calculations */
	static int LastFrameStartTime = 0;
	
	static int CyclesPerFrame=0;
	
	/* initialise */
	
	/* if the CPU is the controlling factor, the size of the buffer
	can be setup as:
	
	Number_of_CPU_Cycles_In_A_Frame/Minimum_Number_Of_Cycles_Per_Instruction */
	public static void EventList_Initialise(int NumEntries)
	{
	        /* stop memory leak if initialise accidently called twice */
		if (pEventListBuffer!=null){
			//free(pEventListBuffer);
                        pEventListBuffer = null;
                }
	
		//pEventListBuffer = malloc(sizeof(EVENT_LIST_ITEM)*NumEntries);
                pEventListBuffer = new EVENT_LIST_ITEM[NumEntries];
	
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
		//pCurrentItem = (EVENT_LIST_ITEM *)pEventListBuffer;
                pCurrentItem = null;
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
                        pEventListBuffer[NumEvents]=pCurrentItem;
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
	
	        if (CyclesPerFrame ==0)
	                CyclesPerFrame = cpu_getfperiod();	//totalcycles();	//_(int)(Machine.drv.cpu[0].cpu_clock / Machine.drv.frames_per_second);
	
	        if (NumEvents < TotalEvents)
	        {
	                pCurrentItem = new EVENT_LIST_ITEM();
                        /* setup item only if there is space in the buffer */
	                pCurrentItem.Event_ID = ID;
	                pCurrentItem.Event_Data = Data;
	
	                Time -= LastFrameStartTime;
	                if ((Time < 0) || ((Time == 0) && (NumEvents!=0))) 
	                        Time+= CyclesPerFrame;
	                pCurrentItem.Event_Time = Time;
	
	                //pCurrentItem++;
                        pEventListBuffer[NumEvents]=pCurrentItem;
	                NumEvents++;
	        }
	}
	
	/* get number of events */
	public static int EventList_NumEvents()
	{
		return NumEvents;
	}
	
	/* get first item in buffer */
	public static EVENT_LIST_ITEM EventList_GetFirstItem()
	{
		//return (EVENT_LIST_ITEM *)pEventListBuffer;
                return pEventListBuffer[0];
	}
}
