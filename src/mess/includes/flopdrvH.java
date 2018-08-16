package mess.includes;

import static old.mame.timer.*;
import static old.mame.timerH.*;

public interface flopdrvH {
    
    public static enum DENSITY {
            //DEN_FM_LO = 0,
            DEN_FM_LO,
            DEN_FM_HI,
            DEN_MFM_LO,
            DEN_MFM_HI
    };

    /* sector has a deleted data address mark */
    public static final int ID_FLAG_DELETED_DATA = 0x0001;
    /* CRC error in id field */
    public static final int ID_FLAG_CRC_ERROR_IN_ID_FIELD = 0x0002;
    /* CRC error in data field */
    public static final int ID_FLAG_CRC_ERROR_IN_DATA_FIELD =  0x0004;

    //int 	floppy_status(int id, int new_status);

    public static class chrn_id
    {
            public char C;
            public char H;
            public char R;
            public char N;
            public int data_id;			// id for read/write data command
            public int flags;
    };

    /* set if drive is present */
    public static final int FLOPPY_DRIVE_CONNECTED = 0x0008;
    /* set if disc is in drive */
    public static final int FLOPPY_DRIVE_DISK_INSERTED = 0x0001;
    /* set if disc is write protected - also set if drive is present but no disc in drive */
    public static final int FLOPPY_DRIVE_DISK_WRITE_PROTECTED = 0x0002;
    /* set if drive is connected and head is positioned over track 0 */
    public static final int FLOPPY_DRIVE_HEAD_AT_TRACK_0 = 0x0004;
    /* set if drive is ready */
    public static final int FLOPPY_DRIVE_READY = 0x0010;
    /* set if index has just occured */
    public static final int FLOPPY_DRIVE_INDEX = 0x0020;
    /* motor state */
    public static final int FLOPPY_DRIVE_MOTOR_ON = 0x0040;
    /* set if disk image is read only */
    public static final int FLOPPY_DRIVE_DISK_IMAGE_READ_ONLY = 0x0100;


    public static interface floppy_interface
    {
            /* seek to physical track */
            void seek_callback(int drive, int physical_track);

            /* the following are not strictly floppy drive operations, but are used by the
            nec765 to get data from the track - really the whole track should be constructed
            into the raw format the nec765 would normally see and this would be totally accurate */
            /* the disc image would then have to re-interpret this back and update the image
            with the data */

            /* get number of sectors per track on side specified */
            int get_sectors_per_track(int drive, int physical_side);
            /* get id from current track and specified side */
            void get_id_callback(int drive, chrn_id chrn, int id_index, int physical_side);

            /* read sector data into buffer, length = number of bytes to read */
            void	read_sector_data_into_buffer(int drive, int side,int data_id,char[] data, int length);
            /* write sector data from buffer, length = number of bytes to read  */
            void	write_sector_data_from_buffer(int drive, int side,int data_id, char[] data, int length, int ddam);
            /* Read track in buffer, length = number of bytes to read */
            void	read_track_data_info_buffer(int drive, int side, char[] ptr, int length );
            /* format */
            void format_sector(int drive, int side, int sector_index,int c, int h, int r, int n, int filler);
    };

    public static class floppy_drive
    {
            /* flags */
            public int flags;
            /* maximum track allowed */
            public int max_track;
            /* num sides */
            public int num_sides;
            /* current track - this may or may not relate to the present cylinder number
            stored by the fdc */
            public int current_track;

            /* index pulse timer */
            public timer_entry index_timer;
            /* index pulse callback */
            public timer_callback index_pulse_callback;

            /* physical real drive unit */
            public int fdd_unit;

            public char[] id_buffer= new char[4];

            public int id_index;
            public chrn_id[] ids=new chrn_id[32];

            public floppy_interface f_interface;
    };


    /* floppy drive types */
    public static enum floppy_type
    {
            FLOPPY_DRIVE_SS_40,
            FLOPPY_DRIVE_DS_80
    };

    //void	floppy_drive_set_index_pulse_callback(int drive, void (*callback)(int id));

    /* set flag state */
    //int floppy_drive_get_flag_state(int drive, int flag);
    /* get flag state */
    //void floppy_drive_set_flag_state(int drive, int flag, int state);
    /* get current physical track drive is on */
    //int floppy_drive_get_current_track(int drive);

    //void	floppy_drive_set_geometry(int,floppy_type type);

    //void	floppy_drives_init(void);
    //void	floppy_drives_exit(void);

    /* get next id from track, 1 if got a id, 0 if no id was got */
    //int floppy_drive_get_next_id(int drive, int side, chrn_id *);
    /* set ready state of drive. If flag == 1, set ready state only if drive present,
    disk is in drive, and motor is on. Otherwise set ready state to the state passed */
    //void	floppy_drive_set_ready_state(int drive, int state, int flag);

    //void	floppy_drive_set_motor_state(int drive, int state);

    /* set interface for disk image functions */
    //void	floppy_drive_set_disk_image_interface(int, floppy_interface *);

    /* set real fdd unit */
    //void	floppy_drive_set_real_fdd_unit(int, unsigned char);

    /* seek up or down */
    //void floppy_drive_seek(int drive, signed int signed_tracks);


    //void	floppy_drive_read_track_data_info_buffer(int drive, int side, char *ptr, int *length );
    //void	floppy_drive_format_sector(int drive, int side, int sector_index, int c, int h, int r, int n, int filler);
    //void    floppy_drive_read_sector_data(int drive, int side, int index1, char *pBuffer, int length);
    //void    floppy_drive_write_sector_data(int drive, int side, int index1, char *pBuffer, int length, int ddam);
    //int		floppy_drive_get_datarate_in_us(DENSITY density);

};