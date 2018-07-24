/***************************************************************************

	machine/nec765.c

	Functions to emulate a NEC765/Intel 8272 compatible floppy disk controller

	Code by Kevin Thacker.

	TODO:

    - overrun condition
	- Scan Commands
	- crc error in id field and crc error in data field errors
	- disc not present, and no sectors on track for data, deleted data, write, write deleted,
		read a track etc
        - end of cylinder condition - almost working, needs fixing  with
                PCW and PC drivers

***************************************************************************/
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import static old.mame.timer.*;
import static old.mame.timerH.*;
import static old.arcadeflex.osdepend.logerror;

public class nec765
{	
        public static final int NEC765_COMMAND_PHASE_FIRST_BYTE = 0;
	public static final int NEC765_COMMAND_PHASE_BYTES      = 1;
	public static final int NEC765_RESULT_PHASE             = 2;
	public static final int NEC765_EXECUTION_PHASE_READ     = 3;
	public static final int NEC765_EXECUTION_PHASE_WRITE    = 4;
        
        public static enum NEC765_PHASE {
            NEC765_COMMAND_PHASE_FIRST_BYTE,
	    NEC765_COMMAND_PHASE_BYTES,
	    NEC765_RESULT_PHASE,
	    NEC765_EXECUTION_PHASE_READ,
	    NEC765_EXECUTION_PHASE_WRITE
        };
        
	
	/* uncomment the following line for verbose information */
	//#define VERBOSE
	
	/* uncomment this to not allow end of cylinder "error" */
	/*TODO*/////#define NO_END_OF_CYLINDER
	
	/*TODO*/////#ifdef VERBOSE
	/* uncomment the following line for super-verbose information i.e. data
	transfer bytes */
	//#define SUPER_VERBOSE
	/*TODO*/////#endif
	
	
	
	/* state of nec765 Interrupt (INT) output */
	public static int NEC765_INT = 0x02;
	/* data rate for floppy discs (MFM data) */
	public static int NEC765_DATA_RATE = 32;
	/* state of nec765 terminal count input*/
	public static int NEC765_TC = 0x04;
	
	public static int NEC765_DMA_MODE = 0x08;
	
	public static int NEC765_SEEK_OPERATION_IS_RECALIBRATE = 0x01;
	
	public static int NEC765_SEEK_ACTIVE = 0x010;
	/* state of nec765 DMA DRQ output */
	public static int NEC765_DMA_DRQ = 0x020;
	/* state of nec765 FDD READY input */
	public static int NEC765_FDD_READY = 0x040;
	
	public static int NEC765_RESET = 0x080;
	
	public static class NEC765
	{
		long	sector_counter;
		/* version of fdc to emulate */
		int version;
		/* main status register */
		char    FDC_main;
		/* data register */
		char	nec765_data_reg;
	
		char c,h,r,n;
	
		int sector_id;
	
		int data_type;
	
	        char[] format_data=new char[4];
	
		NEC765_PHASE    nec765_phase;
		int[]           nec765_command_bytes=new int[16];
		int[]           nec765_result_bytes=new int[16];
		int             nec765_transfer_bytes_remaining;
		int             nec765_transfer_bytes_count;
		int[]           nec765_status=new int[4];
		/* present cylinder number per drive */
		int[]    pcn=new int[4];
		
		/* drive being accessed. drive outputs from fdc */
		int    drive;
		/* side being accessed: side output from fdc */
		int	side;
	
		
		/* step rate time in us */
		long	srt_in_ms;
	
		int	ncn;
	
	//	unsigned int    nec765_id_index;
		/*TODO*/////char *execution_phase_data;
		int	nec765_flags;
	
	//	unsigned char specify[2];
	//	unsigned char perpendicular_mode[1];
	
		int command;
	
		/*TODO*/////void *seek_timer;
		/*TODO*/////void *timer;
		int timer_type;
	};
	
	//static void nec765_setup_data_request(unsigned char Data);
	//static static static static static 
	static NEC765 fdc;
	static char[] nec765_data_buffer=new char[32*1024];
	
	
	/*TODO*/////static nec765_interface nec765_iface;
	
	
	static int nec765_cmd_size[] = {
		1,1,9,3,2,9,9,2,1,9,2,1,9,6,1,3,
		1,9,1,1,1,1,9,1,1,9,1,1,1,9,1,1
	};
	
	static void nec765_setup_drive_and_side()
	{
		// drive index nec765 sees
		fdc.drive = fdc.nec765_command_bytes[1] & 0x03;
		// side index nec765 sees
		fdc.side = (fdc.nec765_command_bytes[1]>>2) & 0x01;
	}
	
	
	/* setup status register 0 based on data in status register 1 and 2 */
	static void nec765_setup_st0()
	{
		/* clear completition status bits, drive bits and side bits */
		fdc.nec765_status[0] &= ~((1<<7) | (1<<6) | (1<<2) | (1<<1) | (1<<0));
		/* fill in drive */
		fdc.nec765_status[0] |= fdc.drive | (fdc.side<<2);
	
		/* fill in completion status bits based on bits in st0, st1, st2 */
		/* no error bits set */
		if ((fdc.nec765_status[1] | fdc.nec765_status[2])==0)
		{
			return;
		}
	
		fdc.nec765_status[0] |= 0x040;
	}
	
	
	static int nec765_n_to_bytes(int n)
	{
		/* 0. 128 bytes, 1.256 bytes, 2.512 bytes etc */
	    /* data_size = ((1<<(N+7)) */
	    return 1<<(n+7);
	}
	
	static void nec765_set_data_request()
	{
		fdc.FDC_main |= 0x080;
	}
	
	static void nec765_clear_data_request()
	{
		fdc.FDC_main &= ~0x080;
	}
	
	static void nec765_seek_complete()
	{
			/* tested on Amstrad CPC */
	
			/* if a seek is done without drive connected: */
			/*  abnormal termination of command,
				seek complete, 
				not ready
			*/
	
			/* if a seek is done with drive connected, but disc missing: */
			/* abnormal termination of command,
				seek complete,
				not ready */
	
			/* if a seek is done with drive connected and disc in drive */
			/* seek complete */
	
	
			/* On the PC however, it appears that recalibrates and seeks can be performed without
			a disc in the drive. */
	
			/* Therefore, the above output is dependant on the state of the drive */
	
			/* In the Amstrad CPC, the drive select is provided by the NEC765. A single port is also
			assigned for setting the drive motor state. The motor state controls the motor of the selected
			drive */
	
			/* On the PC the drive can be selected with the DIGITAL OUTPUT REGISTER, and the motor of each
			of the 4 possible drives is also settable using the same register */
		
			/* Assumption for PC: (NOT TESTED - NEEDS VERIFICATION) */
	
			/* If a seek is done without drive connected: */
			/* abnormal termination of command,
				seek complete,
				fault
				*/
	
			/* if a seek is done with drive connected, but disc missing: */
			/* seek complete */
			
			/* if a seek is done with drive connected and disc in drive: */
			/* seek complete */
	
		/* On Amstrad CPC:
			If drive not connected, or drive connected but disc not in drive, not ready! 
			If drive connected and drive motor on, ready!
		   On PC:
		    Drive is always ready!
	
		In 37c78 docs, the ready bits of the nec765 are marked as unused.
		This indicates it is always ready!!!!!
		*/
	
		/*TODO*/////fdc.pcn[fdc.drive] = fdc.ncn;
	
		/* drive ready? */
		/*TODO*/////if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY))
		/*TODO*/////{
			/* yes */
	
			/* recalibrate? */
		/*TODO*/////	if ((fdc.nec765_flags != 0) && (NEC765_SEEK_OPERATION_IS_RECALIBRATE != 0))
		/*TODO*/////	{
				/* yes */
	
				/* at track 0? */
		/*TODO*/////		if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_HEAD_AT_TRACK_0))
		/*TODO*/////		{
					/* yes. Seek complete */
		/*TODO*/////			fdc.nec765_status[0] = 0x020;
		/*TODO*/////		}
		/*TODO*/////		else
		/*TODO*/////		{
					/* no, track 0 failed after 77 steps */
		/*TODO*/////			fdc.nec765_status[0] = 0x040 | 0x020 | 0x010;
		/*TODO*/////		}
		/*TODO*/////	}
		/*TODO*/////	else
		/*TODO*/////	{
				/* no, seek */
	
				/* seek complete */
		/*TODO*/////		fdc.nec765_status[0] = 0x020;
		/*TODO*/////	}
		/*TODO*/////}
		/*TODO*/////else
		/*TODO*/////{
			/* abnormal termination, not ready */
		/*TODO*/////	fdc.nec765_status[0] = 0x040 | 0x020 | 0x08;		
		/*TODO*/////}
	
		/* set drive and side */
		/*TODO*/////fdc.nec765_status[0] |= fdc.drive | (fdc.side<<2);
	
		/*TODO*/////nec765_set_int(1);
	
		/*TODO*/////fdc.nec765_flags &= ~NEC765_SEEK_ACTIVE;
	}
	
	/*TODO*/////public static timer_callback nec765_seek_timer_callback = new timer_callback() { public void handler(int param) 
	/*TODO*/////{
			/* seek complete */
			/*TODO*/////nec765_seek_complete();
	
			/*TODO*/////if (fdc.seek_timer)
			/*TODO*/////{
			/*TODO*/////	timer_reset(fdc.seek_timer, TIME_NEVER);
			/*TODO*/////}
	/*TODO*/////} };
	/*TODO*/////public static timer_callback nec765_timer_callback = new timer_callback() { public void handler(int param) 
	/*TODO*/////{
		/* type 0 = data transfer mode in execution phase */
		/*TODO*/////if (fdc.timer_type==0)
		/*TODO*/////{
			/* set data request */
		/*TODO*/////	nec765_set_data_request();
	
		/*TODO*/////	fdc.timer_type = 4;
			
		/*TODO*/////	if (!((fdc.nec765_flags !=0) & (NEC765_DMA_MODE != 0)))
		/*TODO*/////	{
				/*TODO*/////if (fdc.timer)
				/*TODO*/////{
					// for pcw
				/*TODO*/////	timer_reset(fdc.timer, TIME_IN_USEC(27));
				/*TODO*/////}
		/*TODO*/////	}
		/*TODO*/////	else
		/*TODO*/////	{
		/*TODO*/////		nec765_timer_callback(fdc.timer_type);
		/*TODO*/////	}
		/*TODO*/////}
		/*TODO*/////else
		/*TODO*/////if (fdc.timer_type==2)
		/*TODO*/////{
			/* result phase begin */
	
			/* generate a int for specific commands */
		/*TODO*/////	switch (fdc.command)
		/*TODO*/////	{
				/* read a track */
		/*TODO*/////		case 2:
				/* write data */
		/*TODO*/////		case 5:
				/* read data */
		/*TODO*/////		case 6:
				/* write deleted data */
		/*TODO*/////		case 9:
				/* read id */
		/*TODO*/////		case 10:
				/* read deleted data */
		/*TODO*/////		case 12:
				/* format at track */
		/*TODO*/////		case 13:
				/* scan equal */
		/*TODO*/////		case 17:
				/* scan low or equal */
		/*TODO*/////		case 19:
				/* scan high or equal */
		/*TODO*/////		case 29:
		/*TODO*/////		{
					/*TODO*/////nec765_set_int(1);
		/*TODO*/////		}
		/*TODO*/////		break;
	
		/*TODO*/////		default:
		/*TODO*/////			break;
		/*TODO*/////	}
	
		/*TODO*/////	nec765_set_data_request();
	
			/*TODO*/////if (fdc.timer)
			/*TODO*/////{
			/*TODO*/////	timer_reset(fdc.timer, TIME_NEVER);
			/*TODO*/////}
		/*TODO*/////}
		/*TODO*/////else
		/*TODO*/////if (fdc.timer_type == 4)
		/*TODO*/////{
			/* if in dma mode, a int is not generated per byte. If not in  DMA mode
			a int is generated per byte */
		/*TODO*/////	if ((fdc.nec765_flags != 0) & (NEC765_DMA_MODE != 0))
		/*TODO*/////	{
		/*TODO*/////		nec765_set_dma_drq(1);
		/*TODO*/////	}
		/*TODO*/////	else
		/*TODO*/////	{
				/*TODO*/////if (fdc.FDC_main & (1<<7))
				/*TODO*/////{
					/* set int to indicate data is ready */
				/*TODO*/////	nec765_set_int(1);
				/*TODO*/////}
		/*TODO*/////	}
	
			/*TODO*/////if (fdc.timer)
			/*TODO*/////{
			/*TODO*/////	timer_reset(fdc.timer, TIME_NEVER);
			/*TODO*/////}
		/*TODO*/////}
	/*TODO*/////} };
	
	/* after (32-27) the DRQ is set, then 27 us later, the int is set.
	I don't know if this is correct, but it is required for the PCW driver.
	In this driver, the first NMI calls the handler function, furthur NMI's are
	effectively disabled by reading the data before the NMI int can be set.
	*/
	
	/* setup data request */
	static void nec765_setup_timed_data_request(int bytes)
	{
		/* setup timer to trigger in NEC765_DATA_RATE us */
		fdc.timer_type = 0;
		/*TODO*/////if (fdc.timer)
		/*TODO*/////{
			/* disable the timer */
		/*TODO*/////	timer_remove(fdc.timer);	//timer_enable(fdc.timer, 0);
		/*TODO*/////	fdc.timer = 0;
		/*TODO*/////}
	
		if (!((fdc.nec765_flags != 0) & (NEC765_DMA_MODE == 1)))
		{
			/*TODO*/////fdc.timer = timer_set(TIME_IN_USEC(32-27)	/*NEC765_DATA_RATE)*bytes*/, 0, nec765_timer_callback);
		}
		else
		{
			/*TODO*/////nec765_timer_callback(fdc.timer_type);
		}
	}
	
	/* setup result data request */
	static void nec765_setup_timed_result_data_request()
	{
		fdc.timer_type = 2;
		/*TODO*/////if (fdc.timer)
		/*TODO*/////{
			/* disable the timer */
		/*TODO*/////	timer_remove(fdc.timer);
		/*TODO*/////	fdc.timer = 0;
		/*TODO*/////}
		/*TODO*/////if (!((fdc.nec765_flags != 0) & (NEC765_DMA_MODE != 0)))
		/*TODO*/////{
		/*TODO*/////	fdc.timer = timer_set(TIME_IN_USEC(NEC765_DATA_RATE)*2, 0, nec765_timer_callback);
		/*TODO*/////}
		/*TODO*/////else
		/*TODO*/////{
		/*TODO*/////	nec765_timer_callback(fdc.timer_type);
		/*TODO*/////}
	}
	
	
	/* sets up a timer to issue a seek complete in signed_tracks time */
	static void nec765_setup_timed_int(int signed_tracks)
	{
		/*TODO*/////if (fdc.seek_timer)
		/*TODO*/////{
			/* disable the timer */
		/*TODO*/////	timer_remove(fdc.seek_timer);	
		/*TODO*/////	fdc.seek_timer = 0;
		/*TODO*/////}
	
		/* setup timer to signal after seek time is complete */
		/*TODO*/////fdc.seek_timer = timer_pulse(TIME_IN_MSEC(fdc.srt_in_ms*abs(signed_tracks)), 0, nec765_seek_timer_callback);
	}
	
	static void nec765_seek_setup(int is_recalibrate)
	{
		int signed_tracks=0;
		
		fdc.nec765_flags |= NEC765_SEEK_ACTIVE;
		fdc.FDC_main |= (1<<fdc.drive);
	
		if (is_recalibrate != 0)
		{
			/* head cannot be specified with recalibrate */
			fdc.nec765_command_bytes[1] &=~0x04;
		}
	
		nec765_setup_drive_and_side();
	
		/* recalibrate command? */
		if (is_recalibrate != 0)
		{
			fdc.nec765_flags |= NEC765_SEEK_OPERATION_IS_RECALIBRATE;
	
			fdc.ncn = 0;
	
			/* if drive is already at track 0, or drive is not ready */
			/*TODO*/////if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_HEAD_AT_TRACK_0) || 
			/*TODO*/////	(!floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY)))
			/*TODO*/////{
				/* seek completed */
			/*TODO*/////	nec765_seek_complete();
			/*TODO*/////}
			/*TODO*/////else
			/*TODO*/////{
				/* is drive present? */
			/*TODO*/////	if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_PRESENT))
			/*TODO*/////	{
					/* yes - calculate real number of tracks to seek */
	
			/*TODO*/////		int current_track;
		
					/* get current track */
			/*TODO*/////		current_track = floppy_drive_get_current_track(fdc.drive);
	
					/* get number of tracks to seek */
			/*TODO*/////		signed_tracks = -current_track;
			/*TODO*/////	}
			/*TODO*/////	else
			/*TODO*/////	{
					/* no, seek 77 tracks and then stop */
			/*TODO*/////		signed_tracks = -77;
			/*TODO*/////	}
	
				if (signed_tracks!=0)
				{
					/* perform seek - if drive isn't present it will not do anything */
				/*TODO*/////	floppy_drive_seek(fdc.drive, signed_tracks);
				
					nec765_setup_timed_int(signed_tracks);
				}
				else
				{
					nec765_seek_complete();
				}
			/*TODO*/////}
		}
		else
		{
	
			fdc.nec765_flags &= ~NEC765_SEEK_OPERATION_IS_RECALIBRATE;
	
			fdc.ncn = fdc.nec765_command_bytes[2];
	
			/* get signed tracks */
			signed_tracks = fdc.ncn - fdc.pcn[fdc.drive];
	
			/* if no tracks to seek, or drive is not ready, seek is complete */
			/*TODO*/////if ((signed_tracks==0) || (!floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY)))
			/*TODO*/////{
			/*TODO*/////	nec765_seek_complete();
			/*TODO*/////}
			/*TODO*/////else
			/*TODO*/////{
				/* perform seek - if drive isn't present it will not do anything */
			/*TODO*/////	floppy_drive_seek(fdc.drive, signed_tracks);
	
				/* seek complete - issue an interrupt */
			/*TODO*/////	nec765_setup_timed_int(signed_tracks);
			/*TODO*/////}
		}
	
	    nec765_idle();
	
	}
	
	
	
	/*TODO*/////static void     nec765_setup_execution_phase_read(char *ptr, int size)
	/*TODO*/////{
	//        fdc.FDC_main |=0x080;                       /* DRQ */
	/*TODO*/////        fdc.FDC_main |= 0x040;                     /* FDC.CPU */
	//		fdc.flags |= NEC765_FLAGS_DATA_TRANSFER_STARTED;
	
	/*TODO*/////        fdc.nec765_transfer_bytes_count = 0;
	/*TODO*/////        fdc.nec765_transfer_bytes_remaining = size;
	/*TODO*/////        fdc.execution_phase_data = ptr;
	/*TODO*/////        fdc.nec765_phase = NEC765_EXECUTION_PHASE_READ;
	
			/* setup a data request with first byte */
	//		fdc.nec765_data_reg = fdc.execution_phase_data[fdc.nec765_transfer_bytes_count];
	//		fdc.nec765_transfer_bytes_count++;
	//		fdc.nec765_transfer_bytes_remaining--;
	/*TODO*/////		nec765_setup_timed_data_request(1);
	/*TODO*/////}
	
	/*TODO*/////static void     nec765_setup_execution_phase_write(char *ptr, int size)
	/*TODO*/////{
	//        fdc.FDC_main |=0x080;                       /* DRQ */
	/*TODO*/////        fdc.FDC_main &= ~0x040;                     /* FDC.CPU */
	
	/*TODO*/////        fdc.nec765_transfer_bytes_count = 0;
	/*TODO*/////        fdc.nec765_transfer_bytes_remaining = size;
	/*TODO*/////       fdc.execution_phase_data = ptr;
	/*TODO*/////        fdc.nec765_phase = NEC765_EXECUTION_PHASE_WRITE;
	
			/* setup a data request with first byte */
	/*TODO*/////		nec765_setup_timed_data_request(1);
	/*TODO*/////}
	
	
	static void     nec765_setup_result_phase(int byte_count)
	{
		//fdc.nec765_flags &= ~NEC765_TC;
	
			fdc.FDC_main |= 0x040;                     /* FDC.CPU */
	        fdc.FDC_main &= ~0x020;                    /* not execution phase */
	
	        fdc.nec765_transfer_bytes_count = 0;
	        fdc.nec765_transfer_bytes_remaining = byte_count;
	        /*TODO*/////fdc.nec765_phase = NEC765_RESULT_PHASE;
	
			nec765_setup_timed_result_data_request();
	}
	
	public static void nec765_idle()
	{
		//fdc.nec765_flags &= ~NEC765_TC;
	
	    fdc.FDC_main &= ~0x040;                     /* CPU.FDC */
	    fdc.FDC_main &= ~0x020;                    /* not execution phase */
	    fdc.FDC_main &= ~0x010;                     /* not busy */
	    /*TODO*/////fdc.nec765_phase = NEC765_COMMAND_PHASE_FIRST_BYTE;
	
		nec765_set_data_request();
	}
	
	/* set int output */
	public static void	nec765_set_int(int state)
	{
		fdc.nec765_flags &= ~NEC765_INT;
	
		if (state != 0)
		{
			fdc.nec765_flags |= NEC765_INT;
		}
	
		/*TODO*/////if (nec765_iface.interrupt)
		/*TODO*/////	nec765_iface.interrupt((fdc.nec765_flags & NEC765_INT));
	}
	
	/* set dma request output */
	public static void	nec765_set_dma_drq(int state)
	{
		fdc.nec765_flags &= ~NEC765_DMA_DRQ;
	
		if (state != 0)
		{
			fdc.nec765_flags |= NEC765_DMA_DRQ;
		}
	
		/*TODO*/////if (nec765_iface.dma_drq)
		/*TODO*/////	nec765_iface.dma_drq((fdc.nec765_flags & NEC765_DMA_DRQ), (fdc.FDC_main & (1<<6)));
	}
	
	/*TODO*/////void    nec765_init(nec765_interface *iface, int version)
	/*TODO*/////{
	/*TODO*/////	fdc.version = version;
	/*TODO*/////		fdc.timer = 0;	//timer_set(TIME_NEVER, 0, nec765_timer_callback);
	/*TODO*/////		fdc.seek_timer = 0;
	/*TODO*/////	memset(&nec765_iface, 0, sizeof(nec765_interface));
	
	/*TODO*/////        if (iface != 0)
	/*TODO*/////        {
	/*TODO*/////                memcpy(&nec765_iface, iface, sizeof(nec765_interface));
	/*TODO*/////        }
	
	/*TODO*/////		fdc.nec765_flags &= NEC765_FDD_READY;
	
	/*TODO*/////		nec765_reset(0);
	/*TODO*/////}
	
	
	/* terminal count input */
	void	nec765_set_tc_state(int state)
	{
		int old_state;
	
		old_state = fdc.nec765_flags;
	
		/* clear drq */
		nec765_set_dma_drq(0);
	
		fdc.nec765_flags &= ~NEC765_TC;
		if (state != 0)
		{
			fdc.nec765_flags |= NEC765_TC;
		}
	
		/* changed state? */
		if (((fdc.nec765_flags^old_state) & NEC765_TC)!=0)
		{
			/* now set? */
			if ((fdc.nec765_flags & NEC765_TC)!=0)
			{
				/* yes */
				/*TODO*/////if (fdc.timer)
				/*TODO*/////{
				/*TODO*/////	if (fdc.timer_type==0)
				/*TODO*/////	{
				/*TODO*/////		timer_remove(fdc.timer);
				/*TODO*/////		fdc.timer = 0;
				/*TODO*/////	}
				/*TODO*/////}
	
	//#ifdef NO_END_OF_CYLINDER
	/*TODO*/////                        nec765_continue_command();
	//#else
	/*TODO*/////                        nec765_update_state();
	//#endif
			}
		}
	}
	
	/*TODO*/////READ_HANDLER(nec765_status_r)
	/*TODO*/////{
	/*TODO*/////	return fdc.FDC_main;
	/*TODO*/////}
	
	
	/* control mark handling code */
	
	/* if SK==1, and we are executing a read data command, and a deleted data mark is found,
	skip it.
	if SK==1, and we are executing a read deleted data command, and a data mark is found,
	skip it. */
	
	static int nec765_read_skip_sector()
	{
		/* skip set? */
		if ((fdc.nec765_command_bytes[0] & (1<<5))!=0)
		{
			/* read data? */
			if (fdc.command == 0x06)
			{
				/* did we just find a sector with deleted data mark? */
				/*TODO*/////if (fdc.data_type == NEC765_DAM_DELETED_DATA)
				/*TODO*/////{
					/* skip it */
				/*TODO*/////	return 1;
				/*TODO*/////}
			}
			/* deleted data? */
			else 
			if (fdc.command == 0x0c)
			{
				/* did we just find a sector with data mark ? */
				/*TODO*/////if (fdc.data_type == NEC765_DAM_DATA)
				/*TODO*/////{
					/* skip it */
				/*TODO*/////	return 1;
				/*TODO*/////}
			}
		}
	
		/* do not skip */
		return 0;
	}
	
	/* this is much closer to how the nec765 actually gets sectors */
	/* used by read data, read deleted data, write data, write deleted data */
	/* What the nec765 does:
	
	  - get next sector id from disc
	  - if sector id matches id specified in command, it will
		search for next data block and read data from it.
	
	  - if the index is seen twice while it is searching for a sector, then the sector cannot be found
	*/
	
	/*TODO*/////static void nec765_get_next_id(chrn_id *id)
	/*TODO*/////{
		/* get next id from disc */
	/*TODO*/////	floppy_drive_get_next_id(fdc.drive, fdc.side,id);
	
	/*TODO*/////	fdc.sector_id = id.data_id;
	
		/* set correct data type */
	/*TODO*/////	fdc.data_type = NEC765_DAM_DATA;
	/*TODO*/////	if (id.flags & ID_FLAG_DELETED_DATA)
	/*TODO*/////	{
	/*TODO*/////		fdc.data_type = NEC765_DAM_DELETED_DATA;
	/*TODO*/////	}
	/*TODO*/////}
	
	static int nec765_get_matching_sector()
	{
		/* number of times we have seen index hole */
		int index_count = 0;
	
		/* get sector id's */
		do
	    {
		/*TODO*/////	chrn_id id;
	
		/*TODO*/////	nec765_get_next_id(&id);
	
			/* tested on Amstrad CPC - All bytes must match, otherwise
			a NO DATA error is reported */
		/*TODO*/////	if (id.R == fdc.nec765_command_bytes[4])
		/*TODO*/////	{
		/*TODO*/////		if (id.C == fdc.nec765_command_bytes[2])
		/*TODO*/////		{
		/*TODO*/////			if (id.H == fdc.nec765_command_bytes[3])
		/*TODO*/////			{
		/*TODO*/////				if (id.N == fdc.nec765_command_bytes[5])
		/*TODO*/////				{
							/* end of cylinder is set if:
							1. sector data is read completely (i.e. no other errors occur like
							no data.
							2. sector being read is same specified by EOT
							3. terminal count is not received */
		/*TODO*/////					if (fdc.nec765_command_bytes[4]==fdc.nec765_command_bytes[6])
		/*TODO*/////					{
								/* set end of cylinder */
		/*TODO*/////						fdc.nec765_status[1] |= NEC765_ST1_END_OF_CYLINDER;
		/*TODO*/////					}
	
		/*TODO*/////					return 1;
		/*TODO*/////				}
		/*TODO*/////			}
		/*TODO*/////		}
		/*TODO*/////		else
		/*TODO*/////		{
					/* the specified sector ID was found, however, the C value specified
					in the read/write command did not match the C value read from the disc */
	
					/* no data - checked on Amstrad CPC */
		/*TODO*/////			fdc.nec765_status[1] |= NEC765_ST1_NO_DATA;
					/* bad C value */
		/*TODO*/////			fdc.nec765_status[2] |= NEC765_ST2_WRONG_CYLINDER;
	
		/*TODO*/////			if (id.C == 0x0ff)
		/*TODO*/////			{
						/* the C value is 0x0ff which indicates a bad track in the IBM soft-sectored
						format */
		/*TODO*/////				fdc.nec765_status[2] |= NEC765_ST2_BAD_CYLINDER;
		/*TODO*/////			}
	
		/*TODO*/////			return 0;
		/*TODO*/////		}
		/*TODO*/////	}
	
			 /* index set? */
		/*TODO*/////	if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_INDEX))
		/*TODO*/////	{
		/*TODO*/////		index_count++;
		/*TODO*/////	}
	   
		} while (index_count!=2);
	
		/* no data - specified sector ID was not found */
	    /*TODO*/////fdc.nec765_status[1] |= NEC765_ST1_NO_DATA;
	  
		return 0;
	}
	
	static void nec765_read_complete()
	{
	
	/* causes problems!!! - need to fix */
	//#ifdef NO_END_OF_CYLINDER
	        /* set end of cylinder */
	/*TODO*/////        fdc.nec765_status[1] &= ~NEC765_ST1_END_OF_CYLINDER;
	//#else
		/* completed read command */
	
		/* end of cylinder is set when:
		 - a whole sector has been read
		 - terminal count input is not set
		 - AND the the sector specified by EOT was read
		 */
		
		/* if end of cylinder is set, and we did receive a terminal count, then clear it */
		if ((fdc.nec765_flags & NEC765_TC)!=0)
		{
			/* set end of cylinder */
		/*TODO*/////	fdc.nec765_status[1] &= ~NEC765_ST1_END_OF_CYLINDER;
		}
	//#endif
	
	/*TODO*/////	nec765_setup_st0();
	
	    fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	    fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	    fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
	    fdc.nec765_result_bytes[3] = fdc.nec765_command_bytes[2]; /* C */
	    fdc.nec765_result_bytes[4] = fdc.nec765_command_bytes[3]; /* H */
	    fdc.nec765_result_bytes[5] = fdc.nec765_command_bytes[4]; /* R */
	    fdc.nec765_result_bytes[6] = fdc.nec765_command_bytes[5]; /* N */
	
	    nec765_setup_result_phase(7);
	}
	
	static void     nec765_read_data()
	{
	
	/*TODO*/////	if (!(floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY)))
	/*TODO*/////	{
	/*TODO*/////        fdc.nec765_status[0] = 0x0c0 | (1<<4) | fdc.drive | (fdc.side<<2);
	/*TODO*/////        fdc.nec765_status[1] = 0x00;
	/*TODO*/////        fdc.nec765_status[2] = 0x00;
	
	/*TODO*/////        fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	/*TODO*/////        fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	/*TODO*/////        fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
	/*TODO*/////        fdc.nec765_result_bytes[3] = fdc.nec765_command_bytes[2]; /* C */
	/*TODO*/////        fdc.nec765_result_bytes[4] = fdc.nec765_command_bytes[3]; /* H */
	/*TODO*/////        fdc.nec765_result_bytes[5] = fdc.nec765_command_bytes[4]; /* R */
	/*TODO*/////        fdc.nec765_result_bytes[6] = fdc.nec765_command_bytes[5]; /* N */
	/*TODO*/////		nec765_setup_result_phase(7);
	/*TODO*/////		return;
	/*TODO*/////	}
	
		/* find a sector to read data from */
	/*TODO*/////	{
			int found_sector_to_read;
	
			found_sector_to_read = 0;
			/* check for finished reading sectors */
			do
			{
				/* get matching sector */
				if ((nec765_get_matching_sector() != 0))
				{
	
					/* skip it? */
					if ((nec765_read_skip_sector() != 0))
					{
						/* yes */
	
						/* check that we haven't finished reading all sectors */
					/*TODO*/////	if ((nec765_sector_count_complete() != 0))
					/*TODO*/////	{
							/* read complete */
					/*TODO*/////		nec765_read_complete();
					/*TODO*/////		return;
					/*TODO*/////	}
	
						/* read not finished */
	
						/* increment sector count */
					/*TODO*/////	nec765_increment_sector();
					}
					else
					{
						/* found a sector to read */
						found_sector_to_read = 1;
					}
				}
				else
				{
					/* error in finding sector */
					nec765_read_complete();
				/*TODO*/////	return;
				}
			}
			while (found_sector_to_read==0);
		}	
			
		{
			int data_size;
	
			data_size = nec765_n_to_bytes(fdc.nec765_command_bytes[5]);
	
			/*TODO*/////floppy_drive_read_sector_data(fdc.drive, fdc.side, fdc.sector_id,nec765_data_buffer,data_size);
	
	        /*TODO*/////nec765_setup_execution_phase_read(nec765_data_buffer, data_size);
		/*TODO*/////}
	}
	
	
	static void     nec765_format_track()
	{
		/* write protected? */
            	/*TODO*/////if (floppy_drive_get_flag_state(fdc.drive,FLOPPY_DRIVE_DISK_WRITE_PROTECTED))
		/*TODO*/////{
		/*TODO*/////	fdc.nec765_status[1] |= NEC765_ST1_NOT_WRITEABLE;
	
		/*TODO*/////	nec765_setup_st0();
		/*TODO*/////	/* TODO: Check result is correct */
		/*TODO*/////	fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
                /*TODO*/////       fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
                /*TODO*/////        fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
		/*TODO*/////	fdc.nec765_result_bytes[3] = fdc.format_data[0];
		/*TODO*/////	fdc.nec765_result_bytes[4] = fdc.format_data[1];
		/*TODO*/////	fdc.nec765_result_bytes[5] = fdc.format_data[2];
		/*TODO*/////	fdc.nec765_result_bytes[6] = fdc.format_data[3];
		/*TODO*/////	nec765_setup_result_phase(7);
	
		/*TODO*/////	return;
		/*TODO*/////}
	
	    /*TODO*/////nec765_setup_execution_phase_write(&fdc.format_data[0], 4);
	}
	
	static void     nec765_read_a_track()
	{
		int data_size;
	
		/* SKIP not allowed with this command! */
	
		/* get next id */
		/*TODO*/////chrn_id id;
	
		/*TODO*/////nec765_get_next_id(&id);
	
	        /* TO BE CONFIRMED! */
	        /* check id from disc */
	        /*TODO*/////if (id.C==fdc.nec765_command_bytes[2])
	        /*TODO*/////{
	        /*TODO*/////    if (id.H==fdc.nec765_command_bytes[3])
	        /*TODO*/////    {
	        /*TODO*/////        if (id.R==fdc.nec765_command_bytes[4])
	        /*TODO*/////        {
	        /*TODO*/////            if (id.N==fdc.nec765_command_bytes[5])
	        /*TODO*/////            {
	                        /* if ID found, then no data is not set */
	                        /* otherwise no data will remain set */
	        /*TODO*/////                fdc.nec765_status[1] &=~NEC765_ST1_NO_DATA;
	        /*TODO*/////            }
	        /*TODO*/////        }
	        /*TODO*/////    }
	        /*TODO*/////}
	
	
	        /*TODO*/////data_size = nec765_n_to_bytes(id.N);
		
		/*TODO*/////floppy_drive_read_sector_data(fdc.drive, fdc.side, fdc.sector_id,nec765_data_buffer,data_size);
	
		/*TODO*/////nec765_setup_execution_phase_read(nec765_data_buffer, data_size);
	}
	
	static int              nec765_just_read_last_sector_on_track()
	{
		/*TODO*/////if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_INDEX))
		/*TODO*/////	return 1;
	
		return 0;
	
	
	}
	
	static void nec765_write_complete()
	{
		nec765_setup_st0();
	
	    fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	    fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	    fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
	    fdc.nec765_result_bytes[3] = fdc.nec765_command_bytes[2]; /* C */
	    fdc.nec765_result_bytes[4] = fdc.nec765_command_bytes[3]; /* H */
	    fdc.nec765_result_bytes[5] = fdc.nec765_command_bytes[4]; /* R */
	    fdc.nec765_result_bytes[6] = fdc.nec765_command_bytes[5]; /* N */
	
	    nec765_setup_result_phase(7);
	}
	
	
	static void     nec765_write_data()
	{
		/*TODO*/////if (!(floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY)))
		/*TODO*/////{
                /*TODO*/////    fdc.nec765_status[0] = 0x0c0 | (1<<4) | fdc.drive | (fdc.side<<2);
                /*TODO*/////    fdc.nec765_status[1] = 0x00;
                /*TODO*/////    fdc.nec765_status[2] = 0x00;
	
                /*TODO*/////    fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
                /*TODO*/////    fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
                /*TODO*/////    fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
                /*TODO*/////    fdc.nec765_result_bytes[3] = fdc.nec765_command_bytes[2]; /* C */
                /*TODO*/////    fdc.nec765_result_bytes[4] = fdc.nec765_command_bytes[3]; /* H */
                /*TODO*/////    fdc.nec765_result_bytes[5] = fdc.nec765_command_bytes[4]; /* R */
                /*TODO*/////    fdc.nec765_result_bytes[6] = fdc.nec765_command_bytes[5]; /* N */
                    
                /*TODO*/////    nec765_setup_result_phase(7);
			
                /*TODO*/////    return;
		/*TODO*/////}
	
		/* write protected? */
		/*TODO*/////if (floppy_drive_get_flag_state(fdc.drive,FLOPPY_DRIVE_DISK_WRITE_PROTECTED))
		/*TODO*/////{
		/*TODO*/////	fdc.nec765_status[1] |= NEC765_ST1_NOT_WRITEABLE;
	
		/*TODO*/////	nec765_write_complete();
		/*TODO*/////	return;
		/*TODO*/////}
	
		if ((nec765_get_matching_sector() != 0))
		{
                    int data_size;
	
                    data_size = nec765_n_to_bytes(fdc.nec765_command_bytes[5]);
	
                    /*TODO*/////nec765_setup_execution_phase_write(nec765_data_buffer, data_size);
		}
	    else
	    {
	        nec765_setup_result_phase(7);
	    }
	}
	
	
	/* return true if we have read all sectors, false if not */
	static int nec765_sector_count_complete()
	{
	/* this is not correct?? */
	//#if 1
		/* if terminal count has been set - yes */
		if ((fdc.nec765_flags != 0) & (NEC765_TC != 0))
		{
			/* completed */
			return 1;
		}
	
	
		
		/* multi-track? */
		if ((fdc.nec765_command_bytes[0] & 0x080) != 0)
		{
			/* it appears that in multi-track mode,
			the EOT parameter of the command is ignored!? -
			or is it ignored the first time and not the next, so that
			if it is started on side 0, it will end at EOT on side 1,
			but if started on side 1 it will end at end of track????
			
			PC driver requires this to end at last sector on side 1, and
			ignore EOT parameter.
			
			To be checked!!!!
			*/
	
			/* if just read last sector and on side 1 - finish */
			if ((nec765_just_read_last_sector_on_track()==1) &&
				(fdc.side==1))
			{
				return 1;
			}
	
			/* if not on second side then we haven't finished yet */
			if (fdc.side!=1)
			{
				/* haven't finished yet */
				return 0;
			}
		}
		else
		{
			/* sector id == EOT? */
			if ((fdc.nec765_command_bytes[4]==fdc.nec765_command_bytes[6]))
			{
	
				/* completed */
				return 1;
			}
		}
	//#else
	
		/* if terminal count has been set - yes */
		if ((fdc.nec765_flags != 0)& (NEC765_TC != 0))
		{
			/* completed */
			return 1;
		}
		
		/* Multi-Track operation:
	
		Verified on Amstrad CPC.
	
			disc format used:
				9 sectors per track
				2 sides
				Sector IDs: &01, &02, &03, &04, &05, &06, &07, &08, &09
	
			Command specified: 
				SIDE = 0,
				C = 0,H = 0,R = 1, N = 2, EOT = 1
			Sectors read:
				Sector 1 side 0
				Sector 1 side 1
	
			Command specified: 
				SIDE = 0,
				C = 0,H = 0,R = 1, N = 2, EOT = 3
			Sectors read:
				Sector 1 side 0
				Sector 2 side 0
				Sector 3 side 0
				Sector 1 side 1
				Sector 2 side 1
				Sector 3 side 1
	
				
			Command specified:
				SIDE = 0,
				C = 0, H = 0, R = 7, N = 2, EOT = 3
			Sectors read:
				Sector 7 side 0
				Sector 8 side 0
				Sector 9 side 0
				Sector 10 not found. Error "No Data"
	
			Command specified:
				SIDE = 1,
				C = 0, H = 1, R = 1, N = 2, EOT = 1
			Sectors read:
				Sector 1 side 1
	
			Command specified:
				SIDE = 1,
				C = 0, H = 1, R = 1, N = 2, EOT = 2
			Sectors read:
				Sector 1 side 1
				Sector 1 side 2
	
	  */
	
		/* sector id == EOT? */
		if ((fdc.nec765_command_bytes[4]==fdc.nec765_command_bytes[6]))
		{
			/* multi-track? */
			if ((fdc.nec765_command_bytes[0] & 0x080) != 0)
			{
				/* if we have reached EOT (fdc.nec765_command_bytes[6]) 
				on side 1, then read is complete */
				if (fdc.side==1)
					return 1;
	
				return 0;
	
			}
	
			/* completed */
			return 1;
		}
	//#endif
		/* not complete */
		return 0;
	}
	
	static void	nec765_increment_sector()
	{
		/* multi-track? */
		if ((fdc.nec765_command_bytes[0] & 0x080) != 0)
		{
			/* reached EOT? */
	                /* if (fdc.nec765_command_bytes[4]==fdc.nec765_command_bytes[6])*/
	                if (nec765_just_read_last_sector_on_track() != 0)
	                {
				/* yes */
	
				/* reached EOT */
				/* change side to 1 */
				fdc.side = 1;
				/* reset sector id to 1 */
				fdc.nec765_command_bytes[4] = 1;
				/* set head to 1 for get next sector test */
				fdc.nec765_command_bytes[3] = 1;
			}
			else
			{
				/* increment */
				fdc.nec765_command_bytes[4]++;
			}
	
		}
		else
		{
		
			fdc.nec765_command_bytes[4]++;
		}
	}
	
	/* control mark handling code */
	
	/* if SK==0, and we are executing a read data command, and a deleted data sector is found,
	the data is not skipped. The data is read, but the control mark is set and the read is stopped */
	/* if SK==0, and we are executing a read deleted data command, and a data sector is found,
	the data is not skipped. The data is read, but the control mark is set and the read is stopped */
	static int nec765_read_data_stop()
	{
		/* skip not set? */
		if ((fdc.nec765_command_bytes[0] & (1<<5))==0)
		{
			/* read data? */
			if (fdc.command == 0x06)
			{
				/* did we just read a sector with deleted data? */
				/*TODO*/////if (fdc.data_type == NEC765_DAM_DELETED_DATA)
				/*TODO*/////{
					/* set control mark */
				/*TODO*/////	fdc.nec765_status[2] |= NEC765_ST2_CONTROL_MARK;
	
					/* quit */
				/*TODO*/////	return 1;
				/*TODO*/////}
			}
			/* deleted data? */
			else 
			if (fdc.command == 0x0c)
			{
				/* did we just read a sector with data? */
				/*TODO*/////if (fdc.data_type == NEC765_DAM_DATA)
				/*TODO*/////{
					/* set control mark */
				/*TODO*/////	fdc.nec765_status[2] |= NEC765_ST2_CONTROL_MARK;
	
					/* quit */
				/*TODO*/////	return 1;
				/*TODO*/////}
			}
		}
	
		/* continue */
		return 0;
	}
	
	static void     nec765_continue_command()
	{
		/*TODO*/////if ((fdc.nec765_phase == NEC765_EXECUTION_PHASE_READ) ||
		/*TODO*/////	(fdc.nec765_phase == NEC765_EXECUTION_PHASE_WRITE))
		/*TODO*/////{
		/*TODO*/////	switch (fdc.command)
	        /*TODO*/////{
				/* read a track */
		/*TODO*/////		case 0x02:
		/*TODO*/////		{
		/*TODO*/////			fdc.sector_counter++;
	
					/* sector counter == EOT */
		/*TODO*/////			if (fdc.sector_counter==fdc.nec765_command_bytes[6])
		/*TODO*/////			{
						/* TODO: Add correct info here */
	
	        /*TODO*/////                                fdc.nec765_status[1] |= NEC765_ST1_END_OF_CYLINDER;
	
	        /*TODO*/////                                nec765_setup_st0();
	
	        /*TODO*/////                                fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	        /*TODO*/////                        fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	        /*TODO*/////                        fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
	        /*TODO*/////                        fdc.nec765_result_bytes[3] = fdc.nec765_command_bytes[2]; /* C */
	        /*TODO*/////                        fdc.nec765_result_bytes[4] = fdc.nec765_command_bytes[3]; /* H */
	        /*TODO*/////                        fdc.nec765_result_bytes[5] = fdc.nec765_command_bytes[4]; /* R */
	        /*TODO*/////                        fdc.nec765_result_bytes[6] = fdc.nec765_command_bytes[5]; /* N */
	
		/*TODO*/////			        nec765_setup_result_phase(7);
		/*TODO*/////			}
		/*TODO*/////			else
		/*TODO*/////			{
		/*TODO*/////				nec765_read_a_track();
		/*TODO*/////			}
		/*TODO*/////		}
		/*TODO*/////		break;
	
				/* format track */
		/*TODO*/////		case 0x0d:
		/*TODO*/////		{
		/*TODO*/////			floppy_drive_format_sector(fdc.drive, fdc.side, fdc.sector_counter,
		/*TODO*/////				fdc.format_data[0], fdc.format_data[1],
		/*TODO*/////				fdc.format_data[2], fdc.format_data[3],
		/*TODO*/////				fdc.nec765_command_bytes[5]);
	
		/*TODO*/////			fdc.sector_counter++;
	
					/* sector_counter = SC */
		/*TODO*/////			if (fdc.sector_counter == fdc.nec765_command_bytes[3])
		/*TODO*/////			{
						/* TODO: Check result is correct */
		/*TODO*/////			        fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	        /*TODO*/////                fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	        /*TODO*/////                fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
		/*TODO*/////					fdc.nec765_result_bytes[3] = fdc.format_data[0];
		/*TODO*/////					fdc.nec765_result_bytes[4] = fdc.format_data[1];
		/*TODO*/////					fdc.nec765_result_bytes[5] = fdc.format_data[2];
		/*TODO*/////					fdc.nec765_result_bytes[6] = fdc.format_data[3];
		/*TODO*/////			        nec765_setup_result_phase(7);
		/*TODO*/////			}
		/*TODO*/////			else
		/*TODO*/////			{
	
		/*TODO*/////				nec765_format_track();
		/*TODO*/////			}
		/*TODO*/////		}
		/*TODO*/////		break;
	
	
	
				/* write data, write deleted data */
		/*TODO*/////		case 0x09:
	        /*TODO*/////        case 0x05:
					/* sector id == EOT */
	
					/* write data to disc */
		/*TODO*/////			floppy_drive_write_sector_data(fdc.drive, fdc.side, fdc.sector_id,nec765_data_buffer,nec765_n_to_bytes(fdc.nec765_command_bytes[5]));
	
		/*TODO*/////			if (nec765_sector_count_complete())
	        /*TODO*/////         {
		/*TODO*/////				nec765_increment_sector();
	
		/*TODO*/////				nec765_write_complete();
		/*TODO*/////			}
		/*TODO*/////			else
		/*TODO*/////			{
		/*TODO*/////				nec765_increment_sector();
	
		/*TODO*/////				nec765_write_data();
		/*TODO*/////			}
		/*TODO*/////			break;
	
				/* read data, read deleted data */
		/*TODO*/////		case 0x0c:
	        /*TODO*/////       case 0x06:
	        /*TODO*/////        {
	
	                        /* read all sectors? */
	
					/* sector id == EOT */
		/*TODO*/////			if ((nec765_sector_count_complete() !=0) || (nec765_read_data_stop() != 0))
		/*TODO*/////		    {
					//		nec765_increment_sector();
	
		/*TODO*/////				nec765_read_complete();
	
	        /*TODO*/////                }
	        /*TODO*/////                else
	        /*TODO*/////                {
	        /*TODO*/////                        nec765_increment_sector();
	
	        /*TODO*/////                        nec765_read_data();
	        /*TODO*/////                }
	        /*TODO*/////        }
	        /*TODO*/////        break;
	
	
	        /*TODO*/////        default:
	        /*TODO*/////                break;
	       /*TODO*/////}
		/*TODO*/////}
	}
	
	
	static int nec765_get_command_byte_count()
	{
		fdc.command = fdc.nec765_command_bytes[0] & 0x01f;
	
		/*TODO*/////if (fdc.version==NEC765A)
		/*TODO*/////{
		/*TODO*/////	 return nec765_cmd_size[fdc.command];
                /*TODO*/////}
		/*TODO*/////else
		/*TODO*/////{
		/*TODO*/////	if (fdc.version==SMC37C78)
		/*TODO*/////	{
		/*TODO*/////		switch (fdc.command)
		/*TODO*/////		{
					/* version */
		/*TODO*/////			case 0x010:
		/*TODO*/////				return 1;
				
					/* verify */
		/*TODO*/////			case 0x016:
		/*TODO*/////				return 9;
	
					/* configure */
		/*TODO*/////			case 0x013:
		/*TODO*/////				return 3;
	
					/* dumpreg */
		/*TODO*/////			case 0x0e:
		/*TODO*/////				return 1;
				
					/* perpendicular mode */
		/*TODO*/////			case 0x012:
		/*TODO*/////				return 1;
	
					/* lock */
		/*TODO*/////			case 0x014:
		/*TODO*/////				return 1;
				
					/* seek/relative seek are together! */
	
		/*TODO*/////			default:
		/*TODO*/////				return nec765_cmd_size[fdc.command];
		/*TODO*/////		}
		/*TODO*/////	}
		/*TODO*/////}
	
		return nec765_cmd_size[fdc.command];
	}
	
	
	
	
	
	void	nec765_update_state()
	{
	    switch (fdc.nec765_phase)
	    {
	         case NEC765_RESULT_PHASE:
	         {
	             /* set data reg */
				/*TODO*///// fdc.nec765_data_reg = fdc.nec765_result_bytes[fdc.nec765_transfer_bytes_count];
	
				 if (fdc.nec765_transfer_bytes_count==0)
				 {
					/* clear int for specific commands */
					switch (fdc.command)
					{
						/* read a track */
						case 2:
						/* write data */
						case 5:
						/* read data */
						case 6:
						/* write deleted data */
						case 9:
						/* read id */
						case 10:
						/* read deleted data */
						case 12:
						/* format at track */
						case 13:
						/* scan equal */
						case 17:
						/* scan low or equal */
						case 19:
						/* scan high or equal */
						case 29:
						{
							nec765_set_int(0);
						}
						break;
	
						default:
							break;
					}
				 }
	
	//#ifdef VERBOSE
	             logerror("NEC765: RESULT: %02x\r\n", fdc.nec765_data_reg);
	//#endif
	
	             fdc.nec765_transfer_bytes_count++;
	             fdc.nec765_transfer_bytes_remaining--;
	
	            if (fdc.nec765_transfer_bytes_remaining==0)
	            {
					nec765_idle();
	            }
				else
				{
					nec765_set_data_request();
				}
			 }
			 break;
	
	         case NEC765_EXECUTION_PHASE_READ:
	         {
				 /* setup data register */
	            /*TODO*///// fdc.nec765_data_reg = fdc.execution_phase_data[fdc.nec765_transfer_bytes_count];
	             fdc.nec765_transfer_bytes_count++;
	             fdc.nec765_transfer_bytes_remaining--;
	
	//#ifdef SUPER_VERBOSE
				logerror("EXECUTION PHASE READ: %02x\r\n", fdc.nec765_data_reg);
	//#endif
	
	            if ((fdc.nec765_transfer_bytes_remaining==0) || ((fdc.nec765_flags != 0)& (NEC765_TC != 0)))
	            {
	                nec765_continue_command();
	            }
				else
				{
					// trigger int
					nec765_setup_timed_data_request(1);
				}
			 }
			 break;
	
		    case NEC765_COMMAND_PHASE_FIRST_BYTE:
	        {
	                fdc.FDC_main |= 0x10;                      /* set BUSY */
	//#ifdef VERBOSE
	                logerror("NEC765: COMMAND: %02x\r\n",fdc.nec765_data_reg);
	//#endif
					/* seek in progress? */
					if ((fdc.nec765_flags != 0) & (NEC765_SEEK_ACTIVE == 1))
					{
						/* any command results in a invalid - I think that seek, recalibrate and
						sense interrupt status may work*/
						fdc.nec765_data_reg = 0;
					}
	
					fdc.nec765_command_bytes[0] = fdc.nec765_data_reg;
	
					fdc.nec765_transfer_bytes_remaining = nec765_get_command_byte_count();
				
					fdc.nec765_transfer_bytes_count = 1;
	                fdc.nec765_transfer_bytes_remaining--;
	
	                if (fdc.nec765_transfer_bytes_remaining==0)
	                {
	                        nec765_setup_command();
	                }
	                else
	                {
							/* request more data */
							nec765_set_data_request();
	                    /*TODO*/////    fdc.nec765_phase = NEC765_COMMAND_PHASE_BYTES;
	                }
	        }
	        break;
	
	                case NEC765_COMMAND_PHASE_BYTES:
	                {
	//#ifdef VERBOSE
	                        logerror("NEC765: COMMAND: %02x\r\n",fdc.nec765_data_reg);
	//#endif
	                        fdc.nec765_command_bytes[fdc.nec765_transfer_bytes_count] = fdc.nec765_data_reg;
	                        fdc.nec765_transfer_bytes_count++;
	                        fdc.nec765_transfer_bytes_remaining--;
	
	                        if (fdc.nec765_transfer_bytes_remaining==0)
	                        {
	                                nec765_setup_command();
	                        }
							else
							{
								/* request more data */
								nec765_set_data_request();
							}
	
	                }
	                break;
	
	            case NEC765_EXECUTION_PHASE_WRITE:
	            {
	                /*TODO*/////fdc.execution_phase_data[fdc.nec765_transfer_bytes_count]=fdc.nec765_data_reg;
	                fdc.nec765_transfer_bytes_count++;
	                fdc.nec765_transfer_bytes_remaining--;
	
	                 if ((fdc.nec765_transfer_bytes_remaining==0) || ((fdc.nec765_flags != 0) & (NEC765_TC == 1)))
	                {
	
	                        nec765_continue_command();
	                }
					else
					{
						nec765_setup_timed_data_request(1);
					}
	            }
			    break;
	
		}
	}
	
	
	/*TODO*/////READ_HANDLER(nec765_data_r)
	/*TODO*/////{
	//	int data;
	
		/* get data we will return */
	//	data = fdc.nec765_data_reg;
	
	
	/*TODO*/////	if ((fdc.FDC_main & 0x0c0)==0x0c0)
	/*TODO*/////	{
	/*TODO*/////		if (
	/*TODO*/////			(fdc.nec765_phase == NEC765_EXECUTION_PHASE_READ) ||
	/*TODO*/////			(fdc.nec765_phase == NEC765_EXECUTION_PHASE_WRITE))
	/*TODO*/////		{
	
				/* reading the data byte clears the interrupt */
	/*TODO*/////			nec765_set_int(0);
	/*TODO*/////		}
	
			/* reset data request */
	/*TODO*/////		nec765_clear_data_request();
	
			/* update state */
	/*TODO*/////		nec765_update_state();
	/*TODO*/////	}
	
	//#ifdef SUPER_VERBOSE
	/*TODO*/////	logerror("DATA R: %02x\r\n", fdc.nec765_data_reg);
	//#endif
	
	/*TODO*/////	return fdc.nec765_data_reg;
	/*TODO*/////}
	
	/*TODO*/////WRITE_HANDLER(nec765_data_w)
	/*TODO*/////{
	//#ifdef SUPER_VERBOSE
	/*TODO*/////	logerror("DATA W: %02x\r\n", data);
	//#endif
	
		/* write data to data reg */
	/*TODO*/////	fdc.nec765_data_reg = data;
	
	/*TODO*/////	if ((fdc.FDC_main & 0x0c0)==0x080)
	/*TODO*/////	{
	/*TODO*/////		if (
	/*TODO*/////			(fdc.nec765_phase == NEC765_EXECUTION_PHASE_READ) ||
	/*TODO*/////			(fdc.nec765_phase == NEC765_EXECUTION_PHASE_WRITE))
	/*TODO*/////		{
	
				/* reading the data byte clears the interrupt */
	/*TODO*/////			nec765_set_int(0);
	/*TODO*/////		}
	
			/* reset data request */
	/*TODO*/////		nec765_clear_data_request();
	
			/* update state */
	/*TODO*/////		nec765_update_state();
	/*TODO*/////	}
	/*TODO*/////}
	
	static void nec765_setup_invalid()
	{
		fdc.command = 0;
		fdc.nec765_result_bytes[0] = 0x080;
		nec765_setup_result_phase(1);
	}
	
	static void     nec765_setup_command()
	{
	//	nec765_clear_data_request();
	
		/* if not in dma mode set execution phase bit */
		if (!((fdc.nec765_flags != 0) & (NEC765_DMA_MODE == 1)))
		{
	        fdc.FDC_main |= 0x020;              /* execution phase */
		}
	
	        switch (fdc.nec765_command_bytes[0] & 0x01f)
	        {
	            case 0x03:      /* specify */
				{
					/* setup step rate */
					fdc.srt_in_ms = 16-((fdc.nec765_command_bytes[1]>>4) & 0x0f);
	
					fdc.nec765_flags &= ~NEC765_DMA_MODE;
	
					if ((fdc.nec765_command_bytes[2] & 0x01)==0)
					{
						fdc.nec765_flags |= NEC765_DMA_MODE;
					}
	
	                nec765_idle();
	            }
				break;
	
	            case 0x04:  /* sense drive status */
				{
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[3] = fdc.drive | (fdc.side<<2);
	
				/*TODO*/////	if (floppy_drive_get_flag_state(fdc.drive,FLOPPY_DRIVE_DISK_WRITE_PROTECTED))
				/*TODO*/////	{
				/*TODO*/////		fdc.nec765_status[3] |= 0x040;
				/*TODO*/////	}
	
				/*TODO*/////	if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_READY))
				/*TODO*/////	{
				/*TODO*/////		fdc.nec765_status[3] |= 0x020;
				/*TODO*/////	}
	
				/*TODO*/////	if (floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_HEAD_AT_TRACK_0))
				/*TODO*/////	{
				/*TODO*/////		fdc.nec765_status[3] |= 0x010;
				/*TODO*/////	}
	
	                        /*TODO*/////        fdc.nec765_status[3] |= 0x08;
	                               
					/* two side and fault not set but should be? */
	
	                fdc.nec765_result_bytes[0] = fdc.nec765_status[3];
	
	                nec765_setup_result_phase(1);
				}
				break;
	
	            case 0x07:          /* recalibrate */
	                nec765_seek_setup(1);
	                break;
	            case 0x0f:          /* seek */
	
					nec765_seek_setup(0);
					break;
	            case 0x0a:      /* read id */
	            {
					/* improve so that unformatted discs are not recognised */
	                /*TODO*/////chrn_id id;
	
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
			/*TODO*/////floppy_drive_get_next_id(fdc.drive, fdc.side, &id);
	
	                fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	                fdc.nec765_result_bytes[1] = fdc.nec765_status[1];
	                fdc.nec765_result_bytes[2] = fdc.nec765_status[2];
	                /*TODO*/////fdc.nec765_result_bytes[3] = id.C; /* C */
	                /*TODO*/////fdc.nec765_result_bytes[4] = id.H; /* H */
	                /*TODO*/////fdc.nec765_result_bytes[5] = id.R; /* R */
	                /*TODO*/////fdc.nec765_result_bytes[6] = id.N; /* N */
	
	
	                 nec765_setup_result_phase(7);
	            }
	            break;
	
	
			case 0x08: /* sense interrupt status */
	  			/* interrupt pending? */
				if ((fdc.nec765_flags != 0) & (NEC765_INT == 1))
				{
					/* yes. Clear int */
					nec765_set_int(0);
	
					/* clear drive seek bits */
					fdc.FDC_main &= ~(1 | 2 | 4 | 8);
	
					/* return status */
					fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
	           		/* return pcn */
					fdc.nec765_result_bytes[1] = fdc.pcn[fdc.drive];
	
					/* return result */
					nec765_setup_result_phase(2);
				}
				else
				{
					/* no int */
					nec765_setup_invalid();
				}
	
	            break;
	
			  case 0x06:  /* read data */
	            {
	
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
	
	                nec765_read_data();
	            }
		    	break;
	
			/* read deleted data */
			case 0x0c:
			{
	
				nec765_setup_drive_and_side();
	
	            fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
				fdc.nec765_status[1] = 0;
				fdc.nec765_status[2] = 0;
	
	
				/* .. for now */
				nec765_read_data();
			}
			break;
	
			/* write deleted data */
			case 0x09:
			{
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
				/* ... for now */
	                nec765_write_data();
	            }
	            break;
	
			/* read a track */
			case 0x02:
			{
				/*TODO*/////	chrn_id id;
	
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
	                /*TODO*/////fdc.nec765_status[0] |= NEC765_ST1_NO_DATA;
	
					/* wait for index */
					/*TODO*/////do
					/*TODO*/////{
						/* get next id from disc */
					/*TODO*/////	floppy_drive_get_next_id(fdc.drive, fdc.side,&id);
					/*TODO*/////}
					/*TODO*/////while ((floppy_drive_get_flag_state(fdc.drive, FLOPPY_DRIVE_INDEX))==0);
	
	
					fdc.sector_counter = 0;
	
	                nec765_read_a_track();
	            }
	            break;
	
	            case 0x05:  /* write data */
	            {
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
	                nec765_write_data();
	            }
	            break;
	
			/* format a track */
			case 0x0d:
			{
					nec765_setup_drive_and_side();
	
	                fdc.nec765_status[0] = fdc.drive | (fdc.side<<2);
	                fdc.nec765_status[1] = 0;
	                fdc.nec765_status[2] = 0;
	
					fdc.sector_counter = 0;
	
					nec765_format_track();
			}
			break;
	
			/* invalid */
	        default:
			{	
				/*TODO*/////switch (fdc.version)
				/*TODO*/////{
				/*TODO*/////	case NEC765A:
				/*TODO*/////	{
				/*TODO*/////		nec765_setup_invalid();
				/*TODO*/////	}
				/*TODO*/////	break;
	
				/*TODO*/////	case NEC765B:
				/*TODO*/////	{
						/* from nec765b data sheet */
				/*TODO*/////		if ((fdc.nec765_command_bytes[0] & 0x01f)==0x010)
				/*TODO*/////		{
							/* version */
				/*TODO*/////			fdc.nec765_status[0] = 0x090;
				/*TODO*/////			fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
				/*TODO*/////			nec765_setup_result_phase(1);
				/*TODO*/////		}
				/*TODO*/////	}			
				/*TODO*/////	break;
	
				/*TODO*/////	case SMC37C78:
				/*TODO*/////	{
						/* TO BE COMPLETED!!! !*/
				/*TODO*/////		switch (fdc.nec765_command_bytes[0] & 0x01f)
				/*TODO*/////		{
							/* version */
				/*TODO*/////			case 0x010:
				/*TODO*/////			{
				/*TODO*/////				fdc.nec765_status[0] = 0x090;
				/*TODO*/////				fdc.nec765_result_bytes[0] = fdc.nec765_status[0];
				/*TODO*/////				nec765_setup_result_phase(1);
				/*TODO*/////			}
				/*TODO*/////			break;
	
							/* configure */
				/*TODO*/////			case 0x013:
				/*TODO*/////			{
							
				/*TODO*/////			}
				/*TODO*/////			break;
	
							/* dump reg */
				/*TODO*/////			case 0x0e:
				/*TODO*/////			{
				/*TODO*/////				fdc.nec765_result_bytes[0] = fdc.pcn[0];
				/*TODO*/////				fdc.nec765_result_bytes[1] = fdc.pcn[1];
				/*TODO*/////				fdc.nec765_result_bytes[2] = fdc.pcn[2];
				/*TODO*/////				fdc.nec765_result_bytes[3] = fdc.pcn[3];
								
				/*TODO*/////				nec765_setup_result_phase(10);
	
				/*TODO*/////			}
				/*TODO*/////			break;
	
	
							/* perpendicular mode */
				/*TODO*/////			case 0x012:
				/*TODO*/////			{
				/*TODO*/////				nec765_idle();
				/*TODO*/////			}
				/*TODO*/////			break;
	
							/* lock */
				/*TODO*/////			case 0x014:
				/*TODO*/////			{
				/*TODO*/////				nec765_setup_result_phase(1);
				/*TODO*/////			}
				/*TODO*/////			break;
	
				
				/*TODO*/////		}
				/*TODO*/////	}
	
	
	
				}
	        }
	        /*TODO*/////break;
		/*TODO*/////	}
	}
	
	
	/* dma acknowledge write */
	/*TODO*/////WRITE_HANDLER(nec765_dack_w)
	/*TODO*/////{
		/* clear request */
	/*TODO*/////	nec765_set_dma_drq(0);
		/* write data */
	/*TODO*/////	nec765_data_w(offset, data);
	/*TODO*/////}
	
	/*TODO*/////READ_HANDLER(nec765_dack_r)
	/*TODO*/////{
		/* clear data request */
	/*TODO*/////	nec765_set_dma_drq(0);
		/* read data */
	/*TODO*/////	return nec765_data_r(offset);	
	/*TODO*/////}
	
	
	void	nec765_reset(int offset)
	{
		/* nec765 in idle state - ready to accept commands */
		nec765_idle();
	
		/* set int low */
		nec765_set_int(0);
		/* set dma drq output */
		nec765_set_dma_drq(0);
	
		/* tandy 100hx assumes that after NEC is reset, it is in DMA mode */
		fdc.nec765_flags |= NEC765_DMA_MODE;
	
		/* if ready input is set during reset generate an int */
		if ((fdc.nec765_flags != 0) & (NEC765_FDD_READY ==1))
		{
			int i;
			int a_drive_is_ready;
	
			fdc.nec765_status[0] = 0x080 | 0x040;
		
			/* for the purpose of pc-xt. If any of the drives have a disk inserted,
			do not set not-ready - need to check with pc_fdc_hw.c whether all drives
			are checked or only the drive selected with the drive select bits?? */
	
			a_drive_is_ready = 0;
			for (i=0; i<4; i++)
			{
			/*TODO*/////	if (floppy_drive_get_flag_state(i, FLOPPY_DRIVE_DISK_PRESENT))
			/*TODO*/////	{
			/*TODO*/////		a_drive_is_ready = 1;
			/*TODO*/////		break;
			/*TODO*/////	}
	
			}
	
			if (a_drive_is_ready != 1)
			{
				fdc.nec765_status[0] |= 0x08;
			}
	
			nec765_set_int(1);	
		}
	}
	
	void	nec765_set_reset_state(int state)
	{
		int flags;
	
		/* get previous reset state */
		flags = fdc.nec765_flags;
	
		/* set new reset state */
		/* clear reset */
		fdc.nec765_flags &= ~NEC765_RESET;
	
		/* reset */
		if (state != 0)
		{
			fdc.nec765_flags |= NEC765_RESET;
	
			nec765_set_int(0);
		}
	
		/* reset changed state? */
		if (((flags^fdc.nec765_flags) & NEC765_RESET)!=0)
		{
			/* yes */
	
			/* no longer reset */
			if ((fdc.nec765_flags & NEC765_RESET)==0)
			{
				/* reset nec */
				nec765_reset(0);
			}
		}
	}
	
	
	void	nec765_set_ready_state(int state)
	{
		/* clear ready state */
		fdc.nec765_flags &= ~NEC765_FDD_READY;
	
		if (state != 0)
		{
			fdc.nec765_flags |= NEC765_FDD_READY;
		}
	}
}

