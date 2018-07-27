/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import static mess.includes.flopdrvH.*;
import static old.arcadeflex.osdepend.logerror;

public class flopdrv
{
	
	public static final int MAX_DRIVES=4;
	
	public static floppy_drive[] drives = new floppy_drive[MAX_DRIVES];
	
	/* init all floppy drives */
	public static void floppy_drives_init()
	{
	logerror("floppy_drives_init");
        /* KT - caused problems with disk missing etc */
	//        memset(&drives[0], 0, sizeof(floppy_drive));
            drives[0]=new floppy_drive();
	//        memset(&drives[1], 0, sizeof(floppy_drive));
            drives[1]=new floppy_drive();
	//        memset(&drives[2], 0, sizeof(floppy_drive));
            drives[2]=new floppy_drive();
	//        memset(&drives[3], 0, sizeof(floppy_drive));
            drives[3]=new floppy_drive();
	        drives[0].current_track = 10;
	        drives[1].current_track = 10;
	        drives[2].current_track = 10;
	        drives[3].current_track = 10;
	}
	
	public static void floppy_drive_set_interface(int index1, floppy_interface iface)
	{
		if ((index1<0) || (index1>=MAX_DRIVES))
			return;
	
		//memcpy(drives[index1].f_interface, iface, sizeof(floppy_interface));
                drives[index1].f_interface = iface;
	}
	
	/* set flag state */
	public static void floppy_drive_set_flag_state(int drive, int flag, int state)
	{
		drives[drive].flags &= ~flag;
	
		if (state != 0)
		{
			drives[drive].flags |= flag;
		}
	
		if (flag==FLOPPY_DRIVE_DISK_PRESENT)
		{
			floppy_drive_seek(drive, 0);
		}
	
	}
	
	public static void floppy_drive_set_motor_state(int drive, int state)
	{
		floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_MOTOR_ON, state);
	}
	
	
	public static void floppy_drive_set_ready_state(int drive, int state, int flag)
	{
		if (flag != 0)
		{
			/* set ready only if drive is present, disk is in the drive,
			and disk motor is on - for Amstrad, Spectrum and PCW*/
		
			/* drive present? */
			if ((drives[drive].flags & FLOPPY_DRIVE_PRESENT) != 0)
			{
				/* disk inserted? */
				if ((drives[drive].flags & FLOPPY_DRIVE_DISK_PRESENT) != 0)
				{
					if ((drives[drive].flags & FLOPPY_DRIVE_MOTOR_ON) != 0)
					{
						/* set state */
						floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_READY, state);
	                                        return;
	                                }
				}
			}
	
	                floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_READY, 0);
	
	
		}
		else
		{
			/* force ready state - for PC driver */
			floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_READY, state);
		}
	
	}
	
	
	/* get flag state */
	public static int floppy_drive_get_flag_state(int drive, int flag)
	{
		switch (flag)
		{
			case FLOPPY_DRIVE_HEAD_AT_TRACK_0:
			{
				/* drive present */
				if ((drives[drive].flags & FLOPPY_DRIVE_PRESENT) != 0)
				{
					/* return real state of track 0 flag */
					return drives[drive].flags & FLOPPY_DRIVE_HEAD_AT_TRACK_0;
				}
		
				/* return not at track 0 */
				return 0;
			}
	
			case FLOPPY_DRIVE_PRESENT:
				return drives[drive].flags & flag;
		
	
			/* return ready state - in case of CPC drive will not be ready if it is not present */
			/* in case of PC drive will be ready even if it is not present */
			case FLOPPY_DRIVE_READY:
				return drives[drive].flags & flag;
                                //return 0;
		
			case FLOPPY_DRIVE_DISK_WRITE_PROTECTED:
			{
				/* drive present */
				if ((drives[drive].flags & FLOPPY_DRIVE_PRESENT) != 0)
				{
					/* return real state of write protected flag */
					return drives[drive].flags & FLOPPY_DRIVE_DISK_WRITE_PROTECTED;
				}
		
				/* drive not present. return write protected  */
				return FLOPPY_DRIVE_DISK_WRITE_PROTECTED;
			}
	
			case FLOPPY_DRIVE_INDEX:
				return drives[drive].flags & flag;
	
			case FLOPPY_DRIVE_DISK_PRESENT:
				return drives[drive].flags & flag;
	
			default:
				break;
		}
	
		return 0;
	}
	
	
	
	
	//static int floppy_drive_motor_state;
	//
	//void	floppy_drive_set_motor_state(int state)
	//{
	//	floppy_drive_motor_state = state;
	//
	//}
	
	//#if 0
	public void floppy_drive_init()
	{
		int i;
	
		for (i=0; i<4; i++)
		{
			//floppy_drive pDrive = get_floppy_drive_ptr(i);
                        floppy_drive pDrive = drives[i];
	
			pDrive.id_index = 0;
			pDrive.current_track = 0;
			pDrive.flags = FLOPPY_DRIVE_HEAD_AT_TRACK_0;
	
			/* 	When Drive is accessed but not fitted,
			Drive Ready and Write Protected status signals are false. */
	
			/* When Drive is  fitted and accessed with no disk inserted,
			Drive Ready status from Drive is false and Write Protected status
			from Drive 1 is true. */
	
			floppy_drive_set_geometry(i, floppy_type.FLOPPY_DRIVE_SS_40);
		}
	}
	//#endif
	
	public static void floppy_drive_set_geometry(int drive, floppy_type type)
	{
		switch (type)
		{
			/* single sided, 40 track drive e.g. Amstrad CPC internal 3" drive */
			case FLOPPY_DRIVE_SS_40:
			{                            
				drives[drive].max_track = 42;
				drives[drive].num_sides = 1;
			}
			break;
	
			case FLOPPY_DRIVE_DS_80:
			{
				drives[drive].max_track = 83;
				drives[drive].num_sides = 2;
			}
			break;
		}
	
		drives[drive].id_index = 0;
		drives[drive].current_track = 0;
		drives[drive].flags |= FLOPPY_DRIVE_HEAD_AT_TRACK_0;
	
	}
	
	
	public static void floppy_drive_seek(int drive, int signed_tracks)
	{
		floppy_drive pDrive = drives[drive];
	
		/* update position */
		pDrive.current_track+=signed_tracks;
	
		if (pDrive.current_track<0)
		{
			pDrive.current_track = 0;
		}
		else
		if (pDrive.current_track>=pDrive.max_track)
		{
			pDrive.current_track = pDrive.max_track-1;
		}
	
		/* set track 0 flag */
		pDrive.flags &= ~FLOPPY_DRIVE_HEAD_AT_TRACK_0;
	
		if (pDrive.current_track==0)
		{
			pDrive.flags |= FLOPPY_DRIVE_HEAD_AT_TRACK_0;
		}
	
		if ((floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_PRESENT)) != 0)
		{
			//if ((drives[drive].f_interface.seek_callback)
	
				drives[drive].f_interface.seek_callback(drive, pDrive.current_track);
		}
	}
	
	/* this is not accurate. But it will do for now */
	public static void floppy_drive_get_next_id(int drive, int side, chrn_id id)
	{
		int spt;
	
		/* get sectors per track */
		spt = 0;
		//if ((drives[drive].f_interface.get_sectors_per_track) != 0)
			spt = drives[drive].f_interface.get_sectors_per_track(drive, side);
	
		/* set index */
		if ((drives[drive].id_index==(spt-1)) || (spt==0))
		{
			floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_INDEX, 1);
		}
		else
		{
			floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_INDEX, 0);
		}
	
		/* get id */
		//if (drives[drive].interface.get_id_callback)
		//{
			drives[drive].f_interface.get_id_callback(drive, id, drives[drive].id_index, side);
		//}
	
		id.data_id = drives[drive].id_index;
	
		drives[drive].id_index++;
		if (spt!=0)
		{
			drives[drive].id_index %= spt;
		}
		else
		{
			drives[drive].id_index = 0;
		}
	
	}
	
	public static int floppy_drive_get_current_track(int drive)
	{
		return drives[drive].current_track;
	}
	
	public static void floppy_drive_format_sector(int drive, int side, int sector_index,int c,int h, int r, int n, int filler)
	{
		//if (drives[drive].interface.format_sector)
			drives[drive].f_interface.format_sector(drive, side, sector_index,c, h, r, n, filler);
	}
	
	public static void floppy_drive_read_sector_data(int drive, int side, int index1, char[] pBuffer, int length)
	{
		//if (drives[drive].f_interface.read_sector_data_into_buffer)
	                drives[drive].f_interface.read_sector_data_into_buffer(drive, side, index1, pBuffer,length);
		
	}
	
	public static void floppy_drive_write_sector_data(int drive, int side, int index1, char[] pBuffer,int length)
	{
		//if (drives[drive].f_interface.write_sector_data_from_buffer)
	                drives[drive].f_interface.write_sector_data_from_buffer(drive, side, index1, pBuffer,length);
		
	}
	
}
