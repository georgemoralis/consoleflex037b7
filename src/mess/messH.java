/** *
 * Ported to mess 0.37b6
 */
package mess;

import static old.mame.inptportH.*;

public class messH {

    /*TODO*////* Endian macros */
/*TODO*///#define FLIPENDIAN_INT16(x)	((((x) >> 8) | ((x) << 8)) & 0xffff)
/*TODO*///#define FLIPENDIAN_INT32(x)	((((x) << 24) | (((UINT32) (x)) >> 24) | \
/*TODO*///                       (( (x) & 0x0000ff00) << 8) | (( (x) & 0x00ff0000) >> 8)))
/*TODO*///
/*TODO*///#ifdef LSB_FIRST
/*TODO*///#define BIG_ENDIANIZE_INT16(x)		(FLIPENDIAN_INT16(x))
/*TODO*///#define BIG_ENDIANIZE_INT32(x)		(FLIPENDIAN_INT32(x))
/*TODO*///#define LITTLE_ENDIANIZE_INT16(x)	(x)
/*TODO*///#define LITTLE_ENDIANIZE_INT32(x)	(x)
/*TODO*///#else
/*TODO*///#define BIG_ENDIANIZE_INT16(x)		(x)
/*TODO*///#define BIG_ENDIANIZE_INT32(x)		(x)
/*TODO*///#define LITTLE_ENDIANIZE_INT16(x)	(FLIPENDIAN_INT16(x))
/*TODO*///#define LITTLE_ENDIANIZE_INT32(x)	(FLIPENDIAN_INT32(x))
/*TODO*///#endif /* LSB_FIRST */
/*TODO*///
/*TODO*////* Win32 defines this for vararg functions */
/*TODO*///#ifndef DECL_SPEC
/*TODO*///#define DECL_SPEC
/*TODO*///#endif
/*TODO*///
/*TODO*///int DECL_SPEC mess_printf(char *fmt, ...);
/*TODO*///
/*TODO*///extern void showmessinfo(void);
/*TODO*///extern int displayimageinfo(struct osd_bitmap *bitmap, int selected);
/*TODO*///extern int filemanager(struct osd_bitmap *bitmap, int selected);
/*TODO*///extern int tapecontrol(struct osd_bitmap *bitmap, int selected);
/*TODO*///
/* driver.h - begin */
    public static int IPT_SELECT1 = IPT_COIN1;
    public static int IPT_SELECT2 = IPT_COIN2;
    public static int IPT_SELECT3 = IPT_COIN3;
    public static int IPT_SELECT4 = IPT_COIN4;
    public static int IPT_KEYBOARD = IPT_TILT;
    /* driver.h - end */
 /*TODO*///
/*TODO*///
/*TODO*////* The wrapper for osd_fopen() */
/*TODO*///void *image_fopen(int type, int id, int filetype, int read_or_write);
/*TODO*///
/*TODO*////* IODevice Initialisation return values.  Use these to determine if */
/*TODO*////* the emulation can continue if IODevice initialisation fails */
/*TODO*///enum { INIT_OK, INIT_FAILED, INIT_UNKNOWN };
/*TODO*///
/*TODO*///
/*TODO*////* IODevice ID return values.  Use these to determine if */
/*TODO*////* the emulation can continue if image cannot be positively IDed */
/*TODO*///enum { ID_FAILED, ID_OK, ID_UNKNOWN };
/*TODO*///
/*TODO*///
/*TODO*////* fileio.c */
/*TODO*///typedef struct
/*TODO*///{
/*TODO*///	int crc;
/*TODO*///	int length;
/*TODO*///	char * name;
/*TODO*///} image_details;
/*TODO*///
/*TODO*////* possible values for osd_fopen() last argument
/*TODO*/// * OSD_FOPEN_READ
/*TODO*/// *	open existing file in read only mode.
/*TODO*/// *	ZIP images can be opened only in this mode, unless
/*TODO*/// *	we add support for writing into ZIP files.
/*TODO*/// * OSD_FOPEN_WRITE
/*TODO*/// *	open new file in write only mode (truncate existing file).
/*TODO*/// *	used for output images (eg. a cassette being written).
/*TODO*/// * OSD_FOPEN_RW
/*TODO*/// *	open existing(!) file in read/write mode.
/*TODO*/// *	used for floppy/harddisk images. if it fails, a driver
/*TODO*/// *	might try to open the image with OSD_FOPEN_READ and set
/*TODO*/// *	an internal 'write protect' flag for the FDC/HDC emulation.
/*TODO*/// * OSD_FOPEN_RW_CREATE
/*TODO*/// *	open existing file or create new file in read/write mode.
/*TODO*/// *	used for floppy/harddisk images. if a file doesn't exist,
/*TODO*/// *	it shall be created. Used to 'format' new floppy or harddisk
/*TODO*/// *	images from within the emulation. A driver might use this
/*TODO*/// *	if both, OSD_FOPEN_RW and OSD_FOPEN_READ modes, failed.
/*TODO*/// */
/*TODO*///enum { OSD_FOPEN_READ, OSD_FOPEN_WRITE, OSD_FOPEN_RW, OSD_FOPEN_RW_CREATE };
/*TODO*///
/*TODO*///
/*TODO*////* mess.c functions [for external use] */
/*TODO*///int parse_image_types(char *arg);
/*TODO*///
/*TODO*////******************************************************************************
/*TODO*/// *	floppy disc controller direct access
/*TODO*/// *	osd_fdc_init
/*TODO*/// *		initialize the needed hardware & structures;
/*TODO*/// *		returns 0 on success
/*TODO*/// *	osd_fdc_exit
/*TODO*/// *		shut down
/*TODO*/// *	osd_fdc_motors
/*TODO*/// *		start motors for <unit> number (0 = A:, 1 = B:)
/*TODO*/// *	osd_fdc_density
/*TODO*/// *		set type of drive from bios info (1: 360K, 2: 1.2M, 3: 720K, 4: 1.44M)
/*TODO*/// *		set density (0:FM,LO 1:FM,HI 2:MFM,LO 3:MFM,HI) ( FM doesn't work )
/*TODO*/// *		tracks, sectors per track and sector length code are given to
/*TODO*/// *		calculate the appropriate double step and GAP II, GAP III values
/*TODO*/// *	osd_fdc_interrupt
/*TODO*/// *		stop motors and interrupt the current command
/*TODO*/// *	osd_fdc_recal
/*TODO*/// *		recalibrate the current drive and update *track if not NULL
/*TODO*/// *	osd_fdc_seek
/*TODO*/// *		seek to a given track number and update *track if not NULL
/*TODO*/// *	osd_fdc_step
/*TODO*/// *		step into a direction (+1/-1) and update *track if not NULL
/*TODO*/// *	osd_fdc_format
/*TODO*/// *		format track t, head h, spt sectors per track
/*TODO*/// *		sector map at *fmt
/*TODO*/// *	osd_fdc_put_sector
/*TODO*/// *		put a sector from memory *buff to track 'track', side 'side',
/*TODO*/// *		head number 'head', sector number 'sector';
/*TODO*/// *		write deleted data address mark if ddam is non zero
/*TODO*/// *	osd_fdc_get_sector
/*TODO*/// *		read a sector to memory *buff from track 'track', side 'side',
/*TODO*/// *		head number 'head', sector number 'sector'
/*TODO*/// *
/*TODO*/// * NOTE: side and head
/*TODO*/// * the terms are used here in the following way:
/*TODO*/// * side = physical side of the floppy disk
/*TODO*/// * head = logical head number (can be 0 though side 1 is to be accessed)
/*TODO*/// *****************************************************************************/
/*TODO*///
/*TODO*///int  osd_fdc_init(void);
/*TODO*///void osd_fdc_exit(void);
/*TODO*///void osd_fdc_motors(unsigned char unit);
/*TODO*///void osd_fdc_density(unsigned char unit, unsigned char density, unsigned char tracks, unsigned char spt, unsigned char eot, unsigned char secl);
/*TODO*///void osd_fdc_interrupt(int param);
/*TODO*///unsigned char osd_fdc_recal(unsigned char *track);
/*TODO*///unsigned char osd_fdc_seek(unsigned char t, unsigned char *track);
/*TODO*///unsigned char osd_fdc_step(int dir, unsigned char *track);
/*TODO*///unsigned char osd_fdc_format(unsigned char t, unsigned char h, unsigned char spt, unsigned char *fmt);
/*TODO*///unsigned char osd_fdc_put_sector(unsigned char track, unsigned char side, unsigned char head, unsigned char sector, unsigned char *buff, unsigned char ddam);
/*TODO*///unsigned char osd_fdc_get_sector(unsigned char track, unsigned char side, unsigned char head, unsigned char sector, unsigned char *buff);
/*TODO*///
/*TODO*///#ifdef MAX_KEYS
/*TODO*/// #undef MAX_KEYS
/*TODO*/// #define MAX_KEYS	128 /* for MESS but already done in INPUT.C*/
/*TODO*///#endif
/*TODO*///
/*TODO*////******************************************************************************
/*TODO*/// * This is a start at the proposed peripheral structure.
/*TODO*/// * It will be filled with live starting with the next release (I hope).
/*TODO*/// * For now it gets us rid of the several lines MESS specific code
/*TODO*/// * in the GameDriver struct and replaces it by only one pointer.
/*TODO*/// *	type				type of device (from above enum)
/*TODO*/// *	count				maximum number of instances
/*TODO*/// *	file_extensions 	supported file extensions
/*TODO*/// *	_private			to be used by the peripheral driver code
/*TODO*/// *	id					identify file
/*TODO*/// *	init				initialize device
/*TODO*/// *	exit				shutdown device
/*TODO*/// *	info				get info for device instance
/*TODO*/// *	open				open device (with specific args)
/*TODO*/// *	close				close device
/*TODO*/// *	status				(set a device status and) get the previous status
/*TODO*/// *	seek				seek to file position
/*TODO*/// *	tell				tell current file position
/*TODO*/// *	input				input character or code
/*TODO*/// *	output				output character or code
/*TODO*/// *	input_chunk 		input chunk of data (eg. sector or track)
/*TODO*/// *	output_chunk		output chunk of data (eg. sector or track)
/*TODO*/// ******************************************************************************/
/*TODO*///
/*TODO*///enum {
/*TODO*///	IO_END = 0,
/*TODO*///	IO_CARTSLOT,
/*TODO*///	IO_FLOPPY,
/*TODO*///	IO_HARDDISK,
/*TODO*///	IO_CASSETTE,
/*TODO*///	IO_PRINTER,
/*TODO*///	IO_SERIAL,
/*TODO*///	IO_SNAPSHOT,
/*TODO*///	IO_QUICKLOAD,
/*TODO*///	IO_ALIAS,  /* dummy type for alias names from mess.cfg */
/*TODO*///	IO_COUNT
/*TODO*///};
/*TODO*///
/*TODO*///enum {
/*TODO*///	IO_RESET_NONE,		/* changing the device file doesn't reset anything */
/*TODO*///	IO_RESET_CPU,		/* only reset the CPU */
/*TODO*///	IO_RESET_ALL		/* restart the driver including audio/video */
/*TODO*///};
/*TODO*///
/*TODO*///struct IODevice {
/*TODO*///	int type;
/*TODO*///	int count;
/*TODO*///	const char *file_extensions;
/*TODO*///	int reset_depth;
/*TODO*///	int (*id)(int id);
/*TODO*///	int (*init)(int id);
/*TODO*///	void (*exit)(int id);
/*TODO*///	const void *(*info)(int id, int whatinfo);
/*TODO*///	int (*open)(int id, int mode, void *args);
/*TODO*///	void (*close)(int id);
/*TODO*///	int (*status)(int id, int newstatus);
/*TODO*///    int (*seek)(int id, int offset, int whence);
/*TODO*///    int (*tell)(int id);
/*TODO*///	int (*input)(int id);
/*TODO*///	void (*output)(int id, int data);
/*TODO*///	int (*input_chunk)(int id, void *dst, int chunks);
/*TODO*///	int (*output_chunk)(int id, void *src, int chunks);
/*TODO*///	UINT32 (*partialcrc)(const unsigned char *buf, unsigned int size);
/*TODO*///
/*TODO*///};
/*TODO*///
/*TODO*////* these are called from mame.c run_game() */
/*TODO*///
/*TODO*///extern int get_filenames(void);
/*TODO*///extern int init_devices(const void *game);
/*TODO*///extern void exit_devices(void);
/*TODO*///
/*TODO*////* access mess.c internal fields for a device type (instance id) */
/*TODO*///
/*TODO*///extern int device_count(int type);
/*TODO*///extern const char *device_typename(int type);
/*TODO*///extern const char *briefdevice_typename(int type);
/*TODO*///extern const char *device_typename_id(int type, int id);
/*TODO*///extern const char *device_filename(int type, int id);
/*TODO*///extern unsigned int device_length(int type, int id);
/*TODO*///extern unsigned int device_crc(int type, int id);
/*TODO*///extern void device_set_crc(int type, int id, UINT32 new_crc);
/*TODO*///extern const char *device_longname(int type, int id);
/*TODO*///extern const char *device_manufacturer(int type, int id);
/*TODO*///extern const char *device_year(int type, int id);
/*TODO*///extern const char *device_playable(int type, int id);
/*TODO*///extern const char *device_extrainfo(int type, int id);
/*TODO*///
/*TODO*///extern const char *device_file_extension(int type, int extnum);
/*TODO*///extern int device_filename_change(int type, int id, const char *name);
/*TODO*///
/*TODO*////* access functions from the struct IODevice arrays of a driver */
/*TODO*///
/*TODO*///extern const void *device_info(int type, int id);
/*TODO*///extern int device_open(int type, int id, int mode, void *args);
/*TODO*///extern void device_close(int type, int id);
/*TODO*///extern int device_seek(int type, int id, int offset, int whence);
/*TODO*///extern int device_tell(int type, int id);
/*TODO*///extern int device_status(int type, int id, int newstatus);
/*TODO*///extern int device_input(int type, int id);
/*TODO*///extern void device_output(int type, int id, int data);
/*TODO*///extern int device_input_chunk(int type, int id, void *dst, int chunks);
/*TODO*///extern void device_output_chunk(int type, int id, void *src, int chunks);
/*TODO*///
/*TODO*////* This is the dummy GameDriver with flag NOT_A_DRIVER set
/*TODO*///   It allows us to use an empty PARENT field in the macros. */
/*TODO*///extern struct GameDriver driver_0;
/*TODO*///
/*TODO*////* Flag is used to bail out in mame.c/run_game() and cpuintrf.c/run_cpu()
/*TODO*/// * but keep the program going. It will be set eg. if the filename for a
/*TODO*/// * device which has IO_RESET_ALL flag set is changed
/*TODO*/// */
/*TODO*///extern int mess_keep_going;
/*TODO*///
/*TODO*////******************************************************************************
/*TODO*/// * MESS' version of the GAME() and GAMEX() macros of MAME
/*TODO*/// * CONS and CONSX are for consoles
/*TODO*/// * COMP and COMPX are for computers
/*TODO*/// ******************************************************************************/
/*TODO*///#define CONS(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,COMPANY,FULLNAME)	\
/*TODO*///extern struct GameDriver driver_##PARENT;	\
/*TODO*///struct GameDriver driver_##NAME =			\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	&machine_driver_##MACHINE,				\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	io_##NAME, 								\
/*TODO*///	ROT0									\
/*TODO*///};
/*TODO*///
/*TODO*///#define CONSX(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,COMPANY,FULLNAME,FLAGS)	\
/*TODO*///extern struct GameDriver driver_##PARENT;	\
/*TODO*///struct GameDriver driver_##NAME =			\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	&machine_driver_##MACHINE,				\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	io_##NAME, 								\
/*TODO*///	ROT0|(FLAGS)							\
/*TODO*///};
/*TODO*///
/*TODO*///#define COMP(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,COMPANY,FULLNAME)	\
/*TODO*///extern struct GameDriver driver_##PARENT;	\
/*TODO*///struct GameDriver driver_##NAME =			\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	&machine_driver_##MACHINE,				\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	io_##NAME, 								\
/*TODO*///	ROT0|GAME_COMPUTER 						\
/*TODO*///};
/*TODO*///
/*TODO*///#define COMPX(YEAR,NAME,PARENT,MACHINE,INPUT,INIT,COMPANY,FULLNAME,FLAGS)	\
/*TODO*///extern struct GameDriver driver_##PARENT;	\
/*TODO*///struct GameDriver driver_##NAME =			\
/*TODO*///{											\
/*TODO*///	__FILE__,								\
/*TODO*///	&driver_##PARENT,						\
/*TODO*///	#NAME,									\
/*TODO*///	FULLNAME,								\
/*TODO*///	#YEAR,									\
/*TODO*///	COMPANY,								\
/*TODO*///	&machine_driver_##MACHINE,				\
/*TODO*///	input_ports_##INPUT,					\
/*TODO*///	init_##INIT,							\
/*TODO*///	rom_##NAME,								\
/*TODO*///	io_##NAME, 								\
/*TODO*///	ROT0|GAME_COMPUTER|(FLAGS)	 			\
/*TODO*///};
/*TODO*///
/*TODO*///#ifdef __cplusplus
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///#endif
/*TODO*///    
}
