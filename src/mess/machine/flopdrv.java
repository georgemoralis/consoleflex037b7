/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

/*
	This code handles the floppy drives.
	All FDD actions should be performed using these functions.

	The functions are emulated and a disk image is used.

  Disk image operation:
  - set disk image functions using floppy_drive_set_disk_image_interface

  Real disk operation:
  - set unit id

  TODO:
	- Disk change handling.
	- Override write protect if disk image has been opened in read mode
*/

import static consoleflex.funcPtr.*;
import static mess.includes.flopdrvH.*;
import static old.arcadeflex.osdepend.logerror;
import static mess.messH.*;
import static mess.mess.*;

import static old.mame.timer.*;
import static old.mame.timerH.*;

import static WIP.arcadeflex.fucPtr.*;

public class flopdrv
{
	
	public static final int MAX_DRIVES=4;
	
	public static floppy_drive[] drives = new floppy_drive[MAX_DRIVES];
        
        /*static {
            for (int i=0 ; i<MAX_DRIVES ; i++)
                drives[i] = new floppy_drive();
        };*/
	
	/* this is called once in init_devices */
        /* initialise all floppy drives */
        /* and initialise real disc access */
        public static void floppy_drives_init()
        {
                int i;

                /* if no floppies, no point setting this up */
                if (device_count(IO_FLOPPY)==0)
                        return;

                /* ensure first drive is present, all other drives are marked
                as not present - override in driver if more are to be made available */
                for (i=0; i<MAX_DRIVES; i++)
                {
                        floppy_drive pDrive = drives[i];

                        /* initialise flags */
                        pDrive.flags = FLOPPY_DRIVE_HEAD_AT_TRACK_0;
                        pDrive.index_pulse_callback = null;
                        pDrive.index_timer = null;

                        if (i==0)
                        {
                                /* set first drive connected */
                                floppy_drive_set_flag_state(i, FLOPPY_DRIVE_CONNECTED, 1);
                        }
                        else
                        {
                                /* all remaining drives are not connected - can be overriden in driver */
                                floppy_drive_set_flag_state(i, FLOPPY_DRIVE_CONNECTED, 0);
                        }

                        /* all drives are double-sided 80 track - can be overriden in driver! */
                        floppy_drive_set_geometry(i, floppy_type.FLOPPY_DRIVE_DS_80);

                        pDrive.fdd_unit = i;

                        /* initialise id index - not so important */
                        pDrive.id_index = 0;
                        /* initialise track */
                        pDrive.current_track = 0;
                        
                        drives[i]=pDrive;
                }
        }

        void floppy_drives_exit()
        {
                int i;

                /* if no floppies, no point cleaning up*/
                if (device_count(IO_FLOPPY)==0)
                        return;

                for (i=0; i<MAX_DRIVES; i++)
                {
                        floppy_drive pDrive;

                        pDrive = drives[i];

                        /* remove timer for index pulse */
                        if (pDrive.index_timer != null)
                        {
                                timer_remove(pDrive.index_timer);
                                pDrive.index_timer = null;
                        }
                        
                        drives[i]=pDrive;
                }

        }

        /* this callback is executed every 300 times a second to emulate the index
        pulse. What is the length of the index pulse?? */
        public static timer_callback floppy_drive_index_callback = new timer_callback() {
            public void handler(int id) {
                /* check it's in range */
                    if ((id>=0) && (id<MAX_DRIVES))
                    {
                            floppy_drive pDrive;

                            pDrive = drives[id];

                            if (pDrive.index_pulse_callback != null)
                                    pDrive.index_pulse_callback.handler(id);
                            
                            drives[id]=pDrive;
                    }
            }
        };
        
        /* set the callback for the index pulse */
        void floppy_drive_set_index_pulse_callback(int id, timer_callback callback)
        {
                floppy_drive pDrive;

                /* check it's in range */
                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                pDrive = drives[id];

                pDrive.index_pulse_callback = callback;
                
                drives[id]=pDrive;
        }

        /*************************************************************************/
        /* IO_FLOPPY device functions */

        /* return and set current status
          use for setting:-
          1) write protect/enable
          2) drive present/missing
        */

        //public static int	floppy_status(int id, int new_status)
        public static io_statusPtr floppy_status = new io_statusPtr(){
     
            public int handler(int id, int new_status){
                System.out.println("floppy_status");
                floppy_drive pDrive;

                /* check it's in range */
                if ((id<0) || (id>=MAX_DRIVES))
                        return 0;

                pDrive = drives[id];

                /* return current status only? */
                if (new_status!=-1)
                {
                        /* we don't set the flags directly.
                        The flags are "cooked" when we do a floppy_drive_get_flag_state depending on
                        if drive is connected etc. So if we wrote the flags back it would
                        corrupt this information. Therefore we update the flags depending on new_status */

                        floppy_drive_set_flag_state(id, FLOPPY_DRIVE_CONNECTED, (new_status & FLOPPY_DRIVE_CONNECTED));
                        floppy_drive_set_flag_state(id, FLOPPY_DRIVE_DISK_WRITE_PROTECTED, (new_status & FLOPPY_DRIVE_DISK_WRITE_PROTECTED));
                }
                
                drives[id]=pDrive;

                /* return current status */
                return floppy_drive_get_flag_state(id,0x0ff);
        }};
                
        

        void floppy_drive_set_real_fdd_unit(int id, int unit_id)
        {
                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                drives[id].fdd_unit = unit_id;
        }

        /* set interface for image interface */
        public static void floppy_drive_set_disk_image_interface(int id, floppy_interface iface)
        {
                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                if (iface==null)
                        return;
                
                if (drives[id]==null)
                    drives[id] = new floppy_drive();

                //memcpy(&drives[id].interface, iface, sizeof(floppy_interface));
                drives[id].f_interface = iface;
                
        }

        /* set flag state */
        public static void floppy_drive_set_flag_state(int id, int flag, int state)
        {
                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                drives[id].flags &= ~flag;

                if (state != 0)
                        drives[id].flags |= flag;
                
                floppy_drive_seek(0, 0);
        }

        public static void floppy_drive_set_motor_state(int drive, int state)
        {
            //System.out.println("floppy_drive_set_motor_state "+state);
                int new_motor_state = 0;
                int previous_state = 0;

                /* previous state */
                if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_MOTOR_ON) != 0)
                        previous_state = 1;

                /* calc new state */

                /* drive present? */
                if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_CONNECTED) != 0)
                {
                        /* disk inserted? */
                        if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_DISK_INSERTED) != 0)
                        {
                                /* drive present and disc inserted */

                                /* state of motor is same as the programmed state */
                                if (state != 0)
                                {
                                        new_motor_state = 1;
                                }
                        }
                }

                if ((new_motor_state^previous_state)!=0)
                {
                        /* if timer already setup remove it */
                        if ((drive>=0) && (drive<MAX_DRIVES))
                        {
                                floppy_drive pDrive;

                                pDrive = drives[drive];

                                if (pDrive.index_timer!=null)
                                {
                                        timer_remove(pDrive.index_timer);
                                        pDrive.index_timer = null;
                                }

                                if (new_motor_state != 0)
                                {
                                        /* off->on */
                                        /* check it's in range */

                                        /* setup timer to trigger at 300 times a second = 300rpm */
                                        pDrive.index_timer = timer_pulse(TIME_IN_HZ(300), drive, floppy_drive_index_callback);
                                }
                                else
                                {
                                        /* on->off */
                                }
                                
                                drives[drive]=pDrive;
                        }
                }

                floppy_drive_set_flag_state(drive, FLOPPY_DRIVE_MOTOR_ON, new_motor_state);

        }

        /* for pc, drive is always ready, for amstrad,pcw,spectrum it is only ready under
        a fixed set of circumstances */
        /* use this to set ready state of drive */
        public static void floppy_drive_set_ready_state(int drive, int state, int flag)
        {
                if (flag != 0)
                {
                        /* set ready only if drive is present, disk is in the drive,
                        and disk motor is on - for Amstrad, Spectrum and PCW*/

                        /* drive present? */
                        if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_CONNECTED) != 0)
                        {
                                /* disk inserted? */
                                if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_DISK_INSERTED) != 0)
                                {
                                        if (floppy_drive_get_flag_state(drive, FLOPPY_DRIVE_MOTOR_ON) != 0)
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
        public static int	floppy_drive_get_flag_state(int id, int flag)
        {
                int drive_flags;
                int flags;

                flags = 0;

                /* check it is within range */
                if ((id<0) || (id>=MAX_DRIVES))
                        return flags;

                drive_flags = drives[id].flags;

                /* these flags are independant of a real drive/disk image */
            flags |= drive_flags & (FLOPPY_DRIVE_CONNECTED | FLOPPY_DRIVE_READY | FLOPPY_DRIVE_MOTOR_ON | FLOPPY_DRIVE_INDEX);

                flags |= drive_flags & FLOPPY_DRIVE_DISK_INSERTED;

                /* if disk image is read-only return write protected all the time */
                if ((drive_flags & FLOPPY_DRIVE_DISK_IMAGE_READ_ONLY) != 0)
                {
                        flags |= FLOPPY_DRIVE_DISK_WRITE_PROTECTED;
                }
                else
                {
                        /* return real state of write protected flag */
                        flags |= drive_flags & FLOPPY_DRIVE_DISK_WRITE_PROTECTED;
                }

                /* drive present not */
                if ((drive_flags & FLOPPY_DRIVE_CONNECTED) == 0)
                {
                        /* adjust some flags if drive is not present */
                        flags &= ~FLOPPY_DRIVE_HEAD_AT_TRACK_0;
                        flags |= FLOPPY_DRIVE_DISK_WRITE_PROTECTED;
                        flags &= ~FLOPPY_DRIVE_DISK_INSERTED;
                }

                flags &= flag;

                return flags;
        }


        public static void	floppy_drive_set_geometry(int id, floppy_type type)
        {
                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                switch (type)
                {
                        /* single sided, 40 track drive e.g. Amstrad CPC internal 3" drive */
                        case FLOPPY_DRIVE_SS_40:
                        {
                                if (drives[id]==null){
                                    drives[id] = new floppy_drive();
                                }
                                
                                drives[id].max_track = 42;
                                drives[id].num_sides = 1;                                
                        }
                        break;

                        case FLOPPY_DRIVE_DS_80:
                        {
                                if (drives[id]==null)
                                    drives[id] = new floppy_drive();
                                drives[id].max_track = 83;
                                drives[id].num_sides = 2;
                        }
                        break;
                }
        }

        public static void    floppy_drive_seek(int id, int signed_tracks)
        {
                floppy_drive pDrive;

                if ((id<0) || (id>=MAX_DRIVES))
                        return;

                pDrive = drives[id];

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

                /* inform disk image of step operation so it can cache information */
                //if (pDrive.interface.seek_callback)
                        pDrive.f_interface.seek_callback(id, pDrive.current_track);
                
                drives[id]=pDrive;
        }


        /* this is not accurate. But it will do for now */
        public static int	floppy_drive_get_next_id(int drive, int side, chrn_id id)
        {
                int spt;

                /* get sectors per track */
                spt = 0;
                //if (drives[drive].interface.get_sectors_per_track)
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
                if (spt!=0)
                {
                        //if (drives[drive].f_interface.get_id_callback)
                        //{
                                drives[drive].f_interface.get_id_callback(drive, id, drives[drive].id_index, side);
                        //}
                }

                drives[drive].id_index++;
                if (spt!=0)
                {
                        drives[drive].id_index %= spt;
                }
                else
                {
                        drives[drive].id_index = 0;
                }

                if (spt==0)
                        return 0;

                return 1;
        }

        public static int	floppy_drive_get_current_track(int drive)
        {
                return drives[drive].current_track;
        }

        void	floppy_drive_read_track_data_info_buffer(int drive, int side, char[] ptr, int length )
        {
                //if (drives[drive].f_interface.read_track_data_info_buffer)
                        drives[drive].f_interface.read_track_data_info_buffer(drive, side, ptr, length);
        }

        public static void	floppy_drive_format_sector(int drive, int side, int sector_index,int c,int h, int r, int n, int filler)
        {
                //if (drives[drive].f_interface.format_sector)
                        drives[drive].f_interface.format_sector(drive, side, sector_index,c, h, r, n, filler);
        }

        public static void    floppy_drive_read_sector_data(int drive, int side, int index1, char[] pBuffer, int length)
        {
                //if (drives[drive].interface.read_sector_data_into_buffer)
                        drives[drive].f_interface.read_sector_data_into_buffer(drive, side, index1, pBuffer,length);
        }

        public static void    floppy_drive_write_sector_data(int drive, int side, int index1, char[] pBuffer,int length, int ddam)
        {
                //if (drives[drive].interface.write_sector_data_from_buffer)
                        drives[drive].f_interface.write_sector_data_from_buffer(drive, side, index1, pBuffer,length,ddam);
        }

        int floppy_drive_get_datarate_in_us(DENSITY density)
        {
                int usecs;
                /* 64 for single density */
                switch (density)
                {
                        case DEN_FM_LO:
                        {
                                usecs = 128;
                        }
                        break;

                        case DEN_FM_HI:
                        {
                                usecs = 64;
                        }
                        break;

                        default:
                        case DEN_MFM_LO:
                        {
                                usecs = 32;
                        }
                        break;

                        case DEN_MFM_HI:
                        {
                                usecs = 16;
                        }
                        break;
                }

                return usecs;
        }
	
}
