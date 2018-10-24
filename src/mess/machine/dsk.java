/***************************************************************************

  dsk.c

 CPCEMU standard and extended disk image support.
 Used on Amstrad CPC and Spectrum +3 drivers.

 KT - 27/2/00 - Moved Disk Image handling code into this file
							- Fixed a few bugs
							- Cleaned code up a bit
***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package mess.machine;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.mame.osdependH.*;
import static consoleflex.funcPtr.*;
import static mess.includes.flopdrvH.*;
import static mess.mess.*;
import static mess.messH.*;
import static WIP.mame.osdependH.*;
import static mess.osdepend.fileio.*;
import static old.arcadeflex.fileio.osd_fwrite;
import static arcadeflex.libc.cstring.memset;
import static mess.machine.flopdrv.*;
import static WIP.arcadeflex.libc.memcpy.*;


public class dsk {
    //* TODO include in MESSH these constants
        public static final int INIT_PASS = 0;
        public static final int INIT_FAIL = 1;
        public static final int IMAGE_VERIFY_PASS = 0;
        public static final int IMAGE_VERIFY_FAIL = 1;
	
        /* disk image and extended disk image support code */
	/* supports up to 84 tracks and 2 sides */
	
	public static final int dsk_MAX_TRACKS = 84;
	public static final int dsk_MAX_SIDES = 2;
	public static final int dsk_NUM_DRIVES = 4;
	public static final int dsk_SECTORS_PER_TRACK = 20;
        
        public static UBytePtr diskimage_data;

        public static class dsk_drive
	{
		UBytePtr data; /* the whole image data */
		int[] track_offsets = new int[dsk_MAX_TRACKS*dsk_MAX_SIDES]; /* offset within data for each track */
		int[] sector_offsets = new int[dsk_SECTORS_PER_TRACK]; /* offset within current track for sector data */
		int current_track=0;		/* current track */
		int disk_image_type;  /* image type: standard or extended */
	};

        public static floppy_interface dsk_floppy_interface=new floppy_interface(){
            @Override
            public void seek_callback(int drive, int physical_track) {
                //System.out.println("seek_callback ["+drive+"]/"+physical_track);
                dsk_seek_callback(drive, physical_track);
                
            }

            @Override
            public int get_sectors_per_track(int drive, int physical_side) {                
                int res=dsk_get_sectors_per_track(drive, physical_side);
                System.out.println("get_sectors_per_track="+res);
                return res;
            }

            @Override
            public void get_id_callback(int drive, chrn_id chrn, int id_index, int physical_side) {
                System.out.println("get_id_callback "+chrn);
                dsk_get_id_callback(drive, chrn, id_index, physical_side);
            }

            @Override
            public void read_sector_data_into_buffer(int drive, int side, int data_id, char[] data, int length) {
                System.out.println("read_sector_data_into_buffer");
                dsk_read_sector_data_into_buffer(drive, side, data_id, data, length);
            }

            @Override
            public void write_sector_data_from_buffer(int drive, int side, int data_id, char[] data, int length, int ddam) {
                System.out.println("write_sector_data_from_buffer");
                dsk_write_sector_data_from_buffer(drive, side, data_id, data, length, ddam);
            }

            @Override
            public void read_track_data_info_buffer(int drive, int side, char[] ptr, int length) {
                // nothing to do
                System.out.println("read_track_data_info_buffer");
            }

            @Override
            public void format_sector(int drive, int side, int sector_index, int c, int h, int r, int n, int filler) {
                // nothing to do
            }          
            
        };

        /*TODO*/////static void dsk_disk_image_init(dsk_drive *);

        public static dsk_drive[] drives = new dsk_drive[dsk_NUM_DRIVES]; /* the drives */
        
        /*static { 
            for (int i=0;i<dsk_NUM_DRIVES;i++) 
                drives[i]=new dsk_drive(); 
        };*/ 


    public static io_initPtr dsk_load = new io_initPtr() {
            public int handler(int id) {
                 System.out.println("dsk_load");
		//void *file;
                Object file=null;
	
		//file = image_fopen(IO_SNAPSHOT, id, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
                file = image_fopen(IO_FLOPPY, id, OSD_FILETYPE_IMAGE_R, 0);
                
                System.out.println("file: "+file);
                
		//if (file != 0)
                if (file != null)
		{
			int datasize;
			UBytePtr data;
	
			/* get file size */
			datasize = osd_fsize(file);
                        
                        System.out.println("size: "+datasize);
	
			if (datasize!=0)
			{
				/* malloc memory for this data */
				data = new UBytePtr(datasize);
	
				if (data!=null)
				{
					/* read whole file */
					osd_fread(file, data, datasize);
	
					//ptr = data;
                                        diskimage_data = new UBytePtr(data);
                                        
                                        System.out.println("data: "+diskimage_data);
	
					/* close file */
					osd_fclose(file);
                                        
                                        System.out.println("Sale");
	
					/* ok! */
					//return INIT_OK;
                                        if (drives[id]==null)
                                        	drives[id]=new dsk_drive();
                                        
                                        drives[id].data=new UBytePtr(diskimage_data);
                                        //flopdrv.drives[id]=new floppy_drive();
                                        flopdrv.drives[id].f_interface=dsk_floppy_interface;
                                        //flopdrv.drives[id].id_buffer=new UBytePtr(data).memory;
                                        
                                        return 1;
				}
				osd_fclose(file);
	
			}
		}
	
		//return INIT_FAILED;
                return 0;
	}};
	
	public static int dsk_floppy_verify(UBytePtr disk_data)
        {
                if ( (memcmp(disk_data.memory, 0, "MV - CPC", 8)==0) || 	/* standard disk image? */
                         (memcmp(disk_data.memory, 0, "EXTENDED", 8)==0))	/* extended disk image? */
                {
                        return IMAGE_VERIFY_PASS;
                }
                return IMAGE_VERIFY_FAIL;
        }


        /* load floppy */
        public static io_initPtr dsk_floppy_load = new io_initPtr() {
            public int handler(int id) {
                System.out.println("dsk_floppy_load");
            //dsk_drive thedrive = drives[id];

            /* load disk image */
            if (dsk_load.handler(id)==1)
            {
                    dsk_drive thedrive = drives[id];
                    
                    if (thedrive == null){
                        thedrive=new dsk_drive();
                        drives[id]=thedrive;
                    }
                    if (thedrive.data != null)
                    {
                        dsk_disk_image_init(thedrive); /* initialise dsk */
                        drives[id]=thedrive;
                        floppy_drive_set_disk_image_interface(id,dsk_floppy_interface);
                        floppy_drive_set_flag_state(id, FLOPPY_DRIVE_READY, 1);
			
                        drives[id]=thedrive;
                                
                        if(dsk_floppy_verify(thedrive.data) == IMAGE_VERIFY_PASS){
                            System.out.println("Devuelvo 0");
                            return 0;
                        }else{
                            System.out.println("Devuelvo 1");
                            return 1;
                        }
                        
                        
				
                    }
            }

            return 1;
        }};

        public static int dsk_save(int type, int id, UBytePtr ptr)
	{
		System.out.println("dsk_save");
                Object file = image_fopen(type, id, OSD_FILETYPE_IMAGE_RW, 0);
	
		if (file != null)
		{
			int datasize;
			UBytePtr data;
	
			/* get file size */
			datasize = osd_fsize(file);
	
			if (datasize!=0)
			{
				data = ptr;
				if (data!=null)
				{
					osd_fwrite(file, data, datasize);
	
					/* close file */
					osd_fclose(file);
	
					/* ok! */
					return 1;
				}
				osd_fclose(file);
	
			}
		};
	
		return 0;
	}


        //void dsk_floppy_exit(int id)
        public static io_exitPtr dsk_floppy_exit = new io_exitPtr() {
            public int handler(int id) {
                dsk_drive thedrive = drives[id];

                if (thedrive.data!=null)
                {
                        dsk_save(IO_FLOPPY,id,thedrive.data);
                        //free(thedrive->data);
                }
                thedrive.data = null;
                
                drives[id]=thedrive;
                
                return 1;
        }};



        public static void dsk_dsk_init_track_offsets(dsk_drive thedrive)
        {
            System.out.println("dsk_dsk_init_track_offsets---------------->>>>>>>>>>>>>");
            int track_offset;
            int i;
            int track_size;
            int tracks, sides;
            int skip, length,offs;
            UBytePtr file_loaded = thedrive.data;


            /* get size of each track from main header. Size of each
            track includes a 0x0100 byte header, and the actual sector data for
            all sectors on the track */
            track_size = file_loaded.read(0x032) | (file_loaded.read(0x033)<<8);

            /* main header is 0x0100 in size */
            track_offset = 0x0100;

            sides = file_loaded.read(0x031);
            tracks = file_loaded.read(0x030);


            /* single sided? */
            if (sides==1)
            {
                    skip = 2;
                    length = tracks;
            }
            else
            {
                    skip = 1;
                    length = tracks*sides;
            }

            offs = 0;
            for (i=0; i<length; i++)
            {
                    thedrive.track_offsets[offs]=track_offset;
                    track_offset+=track_size;
                    offs+=skip;
            }

        }

        public static void dsk_dsk_init_sector_offsets(dsk_drive thedrive,int track,int side)
        {
            int track_offset;

            side = side & 0x01;

            /* get offset to track header in image */
            track_offset = thedrive.track_offsets[(track<<1) + side];

            if (track_offset!=0)
            {
                    int spt;
                    int sector_offset;
                    int sector_size;
                    int i;

                    UBytePtr track_header;

                    track_header = new UBytePtr(thedrive.data, track_offset);

                    /* sectors per track as specified in nec765 format command */
                    /* sectors on this track */
                    spt = track_header.read(0x015);

                    sector_size = (1<<(track_header.read(0x014)+7));

                    /* track header is 0x0100 bytes in size */
                    sector_offset = 0x0100;

                    for (i=0; i<spt; i++)
                    {
                            thedrive.sector_offsets[i] = sector_offset;
                            sector_offset+=sector_size;
                    }
            }
        }

        public static void dsk_extended_dsk_init_track_offsets(dsk_drive thedrive)
        {
            System.out.println("EXTENDED!!!!!!!!!!!!!!!!!!!!");
            int track_offset;

	
            int i;
            int track_size;
            int tracks, sides;
            int offs, skip, length;
            //UBytePtr file_loaded = thedrive.data;

            sides = thedrive.data.read(0x031);
            tracks = thedrive.data.read(0x030);
            
            System.out.println("SIDES="+sides);
            System.out.println("TRACKS="+tracks);

            if (sides==1)
            {
                    skip = 2;
                    length = tracks;
            }
            else
            {
                    skip = 1;
                    length = tracks*sides;
            }

            /* main header is 0x0100 in size */
            track_offset = 0x0100;
            offs = 0;
            
            for (i=0; i<length; i++)
            {
                    int track_size_high_byte;

                    /* track size is specified as a byte, and is multiplied
                    by 256 to get size in bytes. If 0, track doesn't exist and
                    is unformatted, otherwise it exists. Track size includes 0x0100
                    header */
                    track_size_high_byte = thedrive.data.read(0x034 + i);
                    //System.out.println("track_size_high_byte="+track_size_high_byte);

                    if (track_size_high_byte != 0)
                    {
                            /* formatted track */
                            track_size = track_size_high_byte<<8;

                            thedrive.track_offsets[offs] = track_offset;
                            //System.out.println("Escribo "+track_offset+" en "+offs);
                            track_offset+=track_size;
                    }

                    offs+=skip;
            }
            //System.out.println("FIN EXTENDED-A- "+(int)thedrive.track_offsets[0]);
        }


        public static void dsk_extended_dsk_init_sector_offsets(dsk_drive thedrive,int track,int side)
        {
            int track_offset=0;

            side = side & 0x01;

            /* get offset to track header in image */
            track_offset = thedrive.track_offsets[(track<<1) + side];
            
            System.out.println(track+" dsk_extended_dsk_init_sector_offsets!!!!!!!!"+track_offset);

            if (track_offset!=0)
            {
                    int spt;
                    int sector_offset;
                    int sector_size;
                    int i;
                    UBytePtr id_info;
                    UBytePtr track_header;

                    track_header= new UBytePtr(thedrive.data, track_offset);

                    /* sectors per track as specified in nec765 format command */
                    /* sectors on this track */
                    spt = track_header.read(0x015);

                    id_info = new UBytePtr(track_header, 0x018);

                    /* track header is 0x0100 bytes in size */
                    sector_offset = 0x0100;

                    for (i=0; i<spt; i++)
                    {
                            sector_size = id_info.read((i<<3) + 6) + (id_info.read((i<<3) + 7)<<8);
                            

                            thedrive.sector_offsets[i] = sector_offset;
                            sector_offset+=sector_size;
                    }
            }
        }



        public static void dsk_disk_image_init(dsk_drive thedrive)
        {
            System.out.println("dsk_disk_image_init");
            /*-----------------27/02/00 11:26-------------------
             clear offsets
            --------------------------------------------------*/
            memset(thedrive.track_offsets, 0, dsk_MAX_TRACKS*dsk_MAX_SIDES);
            memset(thedrive.sector_offsets, 0, 20);

            if (memcmp(thedrive.data.memory,0,"MV - CPC",8)==0)
            {
                    thedrive.disk_image_type = 0;

                    /* standard disk image */
                    dsk_dsk_init_track_offsets(thedrive);

            }
            else
            if (memcmp(thedrive.data.memory,0,"EXTENDED",8)==0)
            {
                    thedrive.disk_image_type = 1;

                    /* extended disk image */
                    dsk_extended_dsk_init_track_offsets(thedrive);
                    //System.out.println("FIN EXTENDED "+(int)thedrive.track_offsets[0]);
            }
            
        }


        public static void dsk_seek_callback(int drive, int physical_track)
        {
            drive = drive & 0x03;
            flopdrv.drives[drive].current_track = physical_track;
        }

        static int get_track_offset(int drive, int side)
        {
            dsk_drive thedrive;

            drive = drive & 0x03;
            side = side & 0x01;

            thedrive = drives[drive];
            System.out.println("thedrive: "+thedrive+" drive: "+drive+" side: "+side);
            System.out.println(thedrive.track_offsets[(thedrive.current_track<<1) + side]);

            return thedrive.track_offsets[(thedrive.current_track<<1) + side];
        }

        static UBytePtr get_floppy_data(int drive)
        {
            System.out.println("dsk get_floppy_data");
            drive = drive & 0x03;
            //System.out.println(drives[drive].data);
            return drives[drive].data;
        }

        public static void dsk_get_id_callback(int drive, chrn_id id, int id_index, int side)
        {
            int id_offset;
	int track_offset;
	UBytePtr track_header;
	UBytePtr data;

	drive = drive & 0x03;
	side = side & 0x01;

	/* get offset to track header in image */
	track_offset = get_track_offset(drive, side);

	/* track exists? */
	if (track_offset==0)
		return;

	/* yes */
	data = get_floppy_data(drive);
        
        System.out.println("DataNULL: "+data);

	if (data==null)
		return;

	track_header = new UBytePtr(data, track_offset);

	id_offset = 0x018 + (id_index<<3);

	id.C = track_header.read(id_offset + 0);
	id.H = track_header.read(id_offset + 1);
        id.R = track_header.read(id_offset + 2);
	id.N = track_header.read(id_offset + 3);
	id.flags = 0;
	id.data_id = id_index;
        
        System.out.println((int)id.C);
        System.out.println((int)id.H);
        System.out.println((int)id.R);
        System.out.println((int)id.N);
        

        System.out.println("VALOR: ----->"+(int)track_header.read((id_offset + 5) & 0x040));
	if (track_header.read((id_offset + 5) & 0x040) != 0)
	{
		id.flags |= ID_FLAG_DELETED_DATA;
	}

        System.out.println((int)id.flags);
        System.out.println((int)id.data_id);


//	id->ST0 = track_header[id_offset + 4];
//	id->ST1 = track_header[id_offset + 5];

        }


        static void dsk_set_ddam(int drive, int id_index, int side, int ddam)
        {
            int id_offset;
            int track_offset;
            UBytePtr track_header;
            UBytePtr data;

            drive = drive & 0x03;
            side = side & 0x01;

            /* get offset to track header in image */
            track_offset = get_track_offset(drive, side);

            /* track exists? */
            if (track_offset==0)
                    return;

            /* yes */
            data = get_floppy_data(drive);

            if (data==null)
                    return;

            track_header = new UBytePtr(data, track_offset);

            id_offset = 0x018 + (id_index<<3);

            track_header.write(id_offset + 5, track_header.read(id_offset + 5)& ~0x040);

            if (ddam != 0)
            {
                    track_header.write(id_offset + 5, track_header.read(id_offset + 5)| 0x040);
            }
        }


        public static UBytePtr dsk_get_sector_ptr_callback(int drive, int sector_index, int side)
        {
            System.out.println("dsk_get_sector_ptr_callback-------!!!!!!!");
            int track_offset;
            int sector_offset;
            int track;
            dsk_drive thedrive;
            UBytePtr data;

            drive = drive & 0x03;
            side = side & 0x01;

            thedrive = drives[drive];

            track = thedrive.current_track;

            /* offset to track header in image */
            track_offset = get_track_offset(drive, side);

            /* track exists? */
            if (track_offset==0)
                    return null;


            /* setup sector offsets */
            switch (thedrive.disk_image_type)
            {
            case 0:
                    dsk_dsk_init_sector_offsets(thedrive,track, side);
                    break;


            case 1:
                    dsk_extended_dsk_init_sector_offsets(thedrive, track, side);
                    break;

            default:
                    break;
            }

            sector_offset = thedrive.sector_offsets[sector_index];

            data = get_floppy_data(drive);
            
            drives[drive]=thedrive;

            if (data==null)
                    return null;

            return new UBytePtr(data, track_offset + sector_offset);
        }

        public static void dsk_write_sector_data_from_buffer(int drive, int side, int index1, char[] ptr, int length, int ddam)
        {
            UBytePtr pSectorData;

            pSectorData = dsk_get_sector_ptr_callback(drive, index1, side);

            if (pSectorData!=null)
            {
                    memcpy(pSectorData, ptr, length);
            }

            /* set ddam */
            dsk_set_ddam(drive, index1, side,ddam);
        }

        public static void dsk_read_sector_data_into_buffer(int drive, int side, int index1, char[] ptr, int length)
        {
            UBytePtr pSectorData;
            
            System.out.println(index1);

            pSectorData = dsk_get_sector_ptr_callback(drive, index1, side);
            
            System.out.println(pSectorData);

            if (pSectorData!=null)
            {
                    //memcpy(ptr, pSectorData.memory, length);
                    
                    for (int i=0 ; i<length ; i++){
                        System.out.print(pSectorData.read(i)+"-");
                        ptr[i]=pSectorData.read(i);
                    }
                    System.out.println("END");
                    for (int i=0 ; i<length ; i++)
                        System.out.print(ptr[i]+"-");
                    System.out.println("END");
            }
        }

        public static int    dsk_get_sectors_per_track(int drive, int side)
        {
            int track_offset;
            UBytePtr track_header;
            UBytePtr data;

            drive = drive & 0x03;
            side = side & 0x01;

            /* get offset to track header in image */
            track_offset = get_track_offset(drive, side);
            
            System.out.println("track_offset -------- "+track_offset);

            /* track exists? */
            if (track_offset==0)
                    return 0;

            data = get_floppy_data(drive);

            if (data==null)
                    return 0;

            /* yes, get sectors per track */
            track_header = new UBytePtr(data, track_offset);

            return track_header.read(0x015);
        }


}
