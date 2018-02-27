/** *
 * Ported to mess 0.37b6
 */
package mess;

import static mess.messH.IO_COUNT;

public class mess {

    /*TODO*///extern struct GameOptions options;
/*TODO*///
/*TODO*////* CRC database file for this driver, supplied by the OS specific code */
/*TODO*///extern const char *crcfile;
/*TODO*///extern const char *pcrcfile;
/*TODO*///
/*TODO*////* used to tell updatescreen() to clear the bitmap */
/*TODO*///extern int need_to_clear_bitmap;
/*TODO*///
/*TODO*////* Globals */
/*TODO*///int mess_keep_going;
/*TODO*///
/*TODO*///struct image_info {
/*TODO*///	char *name;
/*TODO*///	UINT32 crc;
/*TODO*///	UINT32 length;
/*TODO*///	char *longname;
/*TODO*///	char *manufacturer;
/*TODO*///	char *year;
/*TODO*///	char *playable;
/*TODO*///	char *extrainfo;
/*TODO*///};
/*TODO*///
/*TODO*///static struct image_info *images[IO_COUNT] = {NULL,};
/*TODO*///static int count[IO_COUNT] = {0,};
    static String typename[] = {
        "NONE",
        "Cartridge ",
        "Floppydisk",
        "Harddisk  ",
        "Cassette  ",
        "Printer   ",
        "Serial    ",
        "Snapshot  ",
        "Quickload "
    };

    /*TODO*///static const char *brieftypename[IO_COUNT] = {
/*TODO*///	"NONE",
/*TODO*///	"Cart",
/*TODO*///	"Flop",
/*TODO*///	"Hard",
/*TODO*///	"Cass",
/*TODO*///	"Prin",
/*TODO*///	"Serl",
/*TODO*///	"Dump",
/*TODO*///	"Quik"
/*TODO*///};
/*TODO*///
/*TODO*///static char *mess_alpha = "";
/*TODO*///
/*TODO*///static char* dupe(const char *src)
/*TODO*///{
/*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		char *dst = malloc(strlen(src) + 1);
/*TODO*///		if( dst )
/*TODO*///			strcpy(dst,src);
/*TODO*///		return dst;
/*TODO*///	}
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*///static char* stripspace(const char *src)
/*TODO*///{
/*TODO*///	static char buff[512];
/*TODO*///	if( src )
/*TODO*///	{
/*TODO*///		char *dst;
/*TODO*///		while( *src && isspace(*src) )
/*TODO*///			src++;
/*TODO*///		strcpy(buff, src);
/*TODO*///		dst = buff + strlen(buff);
/*TODO*///		while( dst >= buff && isspace(*--dst) )
/*TODO*///			*dst = '\0';
/*TODO*///		return buff;
/*TODO*///	}
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*///static void free_image_info(struct image_info *img)
/*TODO*///{
/*TODO*///	if( !img )
/*TODO*///		return;
/*TODO*///	if( img->longname )
/*TODO*///		free(img->longname);
/*TODO*///	img->longname = NULL;
/*TODO*///	if( img->manufacturer )
/*TODO*///		free(img->manufacturer );
/*TODO*///	img->manufacturer = NULL;
/*TODO*///	if( img->year )
/*TODO*///		free(img->year );
/*TODO*///	img->year = NULL;
/*TODO*///	if( img->playable )
/*TODO*///		free(img->playable);
/*TODO*///	img->playable = NULL;
/*TODO*///	if( img->extrainfo )
/*TODO*///		free(img->extrainfo);
/*TODO*///	img->extrainfo = NULL;
/*TODO*///}
/*TODO*///
/*TODO*///int DECL_SPEC mess_printf(char *fmt, ...)
/*TODO*///{
/*TODO*///	va_list arg;
/*TODO*///	int length = 0;
/*TODO*///
/*TODO*///	if( !options.gui_host )
/*TODO*///	{
/*TODO*///		va_start(arg,fmt);
/*TODO*///		length = vprintf(fmt, arg);
/*TODO*///		va_end(arg);
/*TODO*///	}
/*TODO*///
/*TODO*///	return length;
/*TODO*///}
/*TODO*///
/*TODO*///static int read_crc_config (const char *, struct image_info *, const char*);
/*TODO*///
/*TODO*///void *image_fopen(int type, int id, int filetype, int read_or_write)
/*TODO*///{
/*TODO*///	struct image_info *img = &images[type][id];
/*TODO*///	const char *sysname;
/*TODO*///	void *file;
/*TODO*///	int extnum;
/*TODO*///
/*TODO*///	if( type >= IO_COUNT )
/*TODO*///	{
/*TODO*///		logerror("image_fopen: type out of range (%d)\n", type);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( id >= count[type] )
/*TODO*///	{
/*TODO*///		logerror("image_fopen: id out of range (%d)\n", id);
/*TODO*///		return NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///	if( img->name == NULL )
/*TODO*///		return NULL;
/*TODO*///
/*TODO*///	/* try the supported extensions */
/*TODO*///	extnum = 0;
/*TODO*///	for( ;; )
/*TODO*///	{
/*TODO*///		const char *ext;
/*TODO*///		char *p;
/*TODO*///		int l;
/*TODO*///
/*TODO*///		sysname = Machine->gamedrv->name;
/*TODO*///		logerror("image_fopen: trying %s for system %s\n", img->name, sysname);
/*TODO*///		file = osd_fopen(sysname, img->name, filetype, read_or_write);
/*TODO*///		/* file found, break out */
/*TODO*///		if( file )
/*TODO*///			break;
/*TODO*///		if( Machine->gamedrv->clone_of &&
/*TODO*///			Machine->gamedrv->clone_of != &driver_0 )
/*TODO*///		{
/*TODO*///			sysname = Machine->gamedrv->clone_of->name;
/*TODO*///			logerror("image_fopen: now trying %s for system %s\n", img->name, sysname);
/*TODO*///			file = osd_fopen(sysname, img->name, filetype, read_or_write);
/*TODO*///		}
/*TODO*///		if( file )
/*TODO*///			break;
/*TODO*///
/*TODO*///		ext = device_file_extension(type,extnum);
/*TODO*///		extnum++;
/*TODO*///
/*TODO*///		/* no (more) extensions, break out */
/*TODO*///		if( !ext )
/*TODO*///			break;
/*TODO*///
/*TODO*///		l = strlen(img->name);
/*TODO*///		p = strrchr(img->name, '.');
/*TODO*///		/* does the current name already have an extension? */
/*TODO*///		if( p )
/*TODO*///		{
/*TODO*///			++p; /* skip the dot */
/*TODO*///			/* new extension won't fit? */
/*TODO*///			if( strlen(p) < strlen(ext) )
/*TODO*///			{
/*TODO*///				img->name = realloc(img->name, l - strlen(p) + strlen(ext) + 1);
/*TODO*///				if( !img->name )
/*TODO*///				{
/*TODO*///					logerror("image_fopen: realloc failed.. damn it!\n");
/*TODO*///					return NULL;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			strcpy(p, ext);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			img->name = realloc(img->name, l + 1 + strlen(ext) + 1);
/*TODO*///			if( !img->name )
/*TODO*///			{
/*TODO*///				logerror("image_fopen: realloc failed.. damn it!\n");
/*TODO*///				return NULL;
/*TODO*///			}
/*TODO*///			sprintf(img->name + l, ".%s", ext);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if( file )
/*TODO*///	{
/*TODO*///		void *config;
/*TODO*///		const struct IODevice *pc_dev = Machine->gamedrv->dev;
/*TODO*///
/*TODO*///		logerror("image_fopen: found image %s for system %s\n", img->name, sysname);
/*TODO*///		img->length = osd_fsize(file);
/*TODO*////* Cowering, partial crcs for NES/A7800/others */
/*TODO*///		img->crc = 0;
/*TODO*///		while( pc_dev && pc_dev->count && !img->crc)
/*TODO*///		{
/*TODO*///			logerror("partialcrc() -> %08lx\n",pc_dev->partialcrc);
/*TODO*///			if( type == pc_dev->type && pc_dev->partialcrc )
/*TODO*///			{
/*TODO*///				unsigned char *pc_buf = (unsigned char *)malloc(img->length);
/*TODO*///				if( pc_buf )
/*TODO*///				{
/*TODO*///					osd_fseek(file,0,SEEK_SET);
/*TODO*///					osd_fread(file,pc_buf,img->length);
/*TODO*///					osd_fseek(file,0,SEEK_SET);
/*TODO*///					logerror("Calling partialcrc()\n");
/*TODO*///					img->crc = (*pc_dev->partialcrc)(pc_buf,img->length);
/*TODO*///					free(pc_buf);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					logerror("failed to malloc(%d)\n", img->length);
/*TODO*///				}
/*TODO*///			}
/*TODO*///			pc_dev++;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!img->crc) img->crc = osd_fcrc(file);
/*TODO*///		if( img->crc == 0 && img->length < 0x100000 )
/*TODO*///		{
/*TODO*///			logerror("image_fopen: calling osd_fchecksum() for %d bytes\n", img->length);
/*TODO*///			osd_fchecksum(sysname, img->name, &img->length, &img->crc);
/*TODO*///			logerror("image_fopen: CRC is %08x\n", img->crc);
/*TODO*///		}
/*TODO*///		free_image_info(img);
/*TODO*///
/*TODO*///		if (read_crc_config (crcfile, img, sysname) && Machine->gamedrv->clone_of->name)
/*TODO*///			read_crc_config (pcrcfile, img, Machine->gamedrv->clone_of->name);
/*TODO*///
/*TODO*///		config = config_open(crcfile);
/*TODO*///	}
/*TODO*///
/*TODO*///	return file;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///static int read_crc_config (const char *file, struct image_info *img, const char* sysname)
/*TODO*///{
/*TODO*///	int retval;
/*TODO*///	void *config = config_open (file);
/*TODO*///
/*TODO*///	retval = 1;
/*TODO*///	if( config )
/*TODO*///	{
/*TODO*///		char line[1024];
/*TODO*///		char crc[9+1];
/*TODO*///
/*TODO*///		sprintf(crc, "%08x", img->crc);
/*TODO*///		config_load_string(config,sysname,0,crc,line,sizeof(line));
/*TODO*///		if( line[0] )
/*TODO*///		{
/*TODO*///			logerror("found CRC %s= %s\n", crc, line);
/*TODO*///			img->longname = dupe(stripspace(strtok(line, "|")));
/*TODO*///			img->manufacturer = dupe(stripspace(strtok(NULL, "|")));
/*TODO*///			img->year = dupe(stripspace(strtok(NULL, "|")));
/*TODO*///			img->playable = dupe(stripspace(strtok(NULL, "|")));
/*TODO*///			img->extrainfo = dupe(stripspace(strtok(NULL, "|")));
/*TODO*///			retval = 0;
/*TODO*///		}
/*TODO*///		config_close(config);
/*TODO*///	}
/*TODO*///	return retval;
/*TODO*///}
/*TODO*///

    /*
    * Return a name for the device type (to be used for UI functions)
     */
    public static String device_typename(int type) {
        if (type < IO_COUNT) {
            return typename[type];
        }
        return "UNKNOWN";
    }
    /*TODO*///
/*TODO*///const char *briefdevice_typename(int type)
/*TODO*///{
/*TODO*///	if (type < IO_COUNT)
/*TODO*///		return brieftypename[type];
/*TODO*///	return "UNKNOWN";
/*TODO*///}
/*TODO*///
/*TODO*///const char *device_brieftypename(int type)
/*TODO*///{
/*TODO*///	if (type < IO_COUNT)
/*TODO*///		return brieftypename[type];
/*TODO*///	return "UNKNOWN";
/*TODO*///}
/*TODO*///
/*TODO*////* Return a name for a device of type 'type' with id 'id' */
/*TODO*///const char *device_typename_id(int type, int id)
/*TODO*///{
/*TODO*///	static char typename_id[40][31+1];
/*TODO*///	static int which = 0;
/*TODO*///	if (type < IO_COUNT)
/*TODO*///	{
/*TODO*///		which = ++which % 40;
/*TODO*///		/* for the average user counting starts at #1 ;-) */
/*TODO*///		sprintf(typename_id[which], "%s #%d", typename[type], id+1);
/*TODO*///		return typename_id[which];
/*TODO*///	}
/*TODO*///	return "UNKNOWN";
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the number of filenames for a device of type 'type'.
/*TODO*/// */
/*TODO*///int device_count(int type)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return 0;
/*TODO*///	return count[type];
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th filename for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_filename(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].name;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'num'th file extension for a device of type 'type',
/*TODO*/// * NULL if no file extensions of that type are available.
/*TODO*/// */
/*TODO*///const char *device_file_extension(int type, int extnum)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	const char *ext;
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	while( dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type )
/*TODO*///		{
/*TODO*///			ext = dev->file_extensions;
/*TODO*///			while( ext && *ext && extnum-- > 0 )
/*TODO*///				ext = ext + strlen(ext) + 1;
/*TODO*///			if( ext && !*ext )
/*TODO*///				ext = NULL;
/*TODO*///			return ext;
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th crc for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///unsigned int device_crc(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return 0;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].crc;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Set the 'id'th crc for a device of type 'type',
/*TODO*/// * this is to be used if only a 'partial crc' shall be used.
/*TODO*/// */
/*TODO*///void device_set_crc(int type, int id, UINT32 new_crc)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///	{
/*TODO*///		logerror("device_set_crc: type out of bounds (%d)\n", type);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	if (id < count[type])
/*TODO*///	{
/*TODO*///		images[type][id].crc = new_crc;
/*TODO*///		logerror("device_set_crc: new_crc %08x\n", new_crc);
/*TODO*///	}
/*TODO*///	else
/*TODO*///		logerror("device_set_crc: id out of bounds (%d)\n", id);
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th length for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///unsigned int device_length(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return 0;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].length;
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th long name for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_longname(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].longname;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th manufacturer name for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_manufacturer(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].manufacturer;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th release year for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_year(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].year;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th playable info for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_playable(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].playable;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*
/*TODO*/// * Return the 'id'th extrainfo info for a device of type 'type',
/*TODO*/// * NULL if not enough image names of that type are available.
/*TODO*/// */
/*TODO*///const char *device_extrainfo(int type, int id)
/*TODO*///{
/*TODO*///	if (type >= IO_COUNT)
/*TODO*///		return NULL;
/*TODO*///	if (id < count[type])
/*TODO*///		return images[type][id].extrainfo;
/*TODO*///	return NULL;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Copy the image names from options.image_files[] to
/*TODO*/// * the array of filenames we keep here, depending on the
/*TODO*/// * type identifier of each image.
/*TODO*/// */
/*TODO*///int get_filenames(void)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	int i;
/*TODO*///
/*TODO*///	for( i = 0; i < options.image_count; i++ )
/*TODO*///	{
/*TODO*///		int type = options.image_files[i].type;
/*TODO*///
/*TODO*///		if (type < IO_COUNT)
/*TODO*///		{
/*TODO*///			/* Add a filename to the arrays of names */
/*TODO*///			if( images[type] )
/*TODO*///				images[type] = realloc(images[type],(count[type]+1)*sizeof(struct image_info));
/*TODO*///			else
/*TODO*///				images[type] = malloc(sizeof(struct image_info));
/*TODO*///			if( !images[type] )
/*TODO*///				return 1;
/*TODO*///			memset(&images[type][count[type]], 0, sizeof(struct image_info));
/*TODO*///			if( options.image_files[i].name )
/*TODO*///			{
/*TODO*///				images[type][count[type]].name = dupe(options.image_files[i].name);
/*TODO*///				if( !images[type][count[type]].name )
/*TODO*///					return 1;
/*TODO*///			}
/*TODO*///			logerror("%s #%d: %s\n", typename[type], count[type]+1, images[type][count[type]].name);
/*TODO*///			count[type]++;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("Invalid IO_ type %d for %s\n", type, options.image_files[i].name);
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Does the driver have any IODevices defined? */
/*TODO*///	if( dev )
/*TODO*///	{
/*TODO*///		while( dev->count )
/*TODO*///		{
/*TODO*///			int type = dev->type;
/*TODO*///			while( count[type] < dev->count )
/*TODO*///			{
/*TODO*///				/* Add an empty slot name the arrays of names */
/*TODO*///				if( images[type] )
/*TODO*///					images[type] = realloc(images[type],(count[type]+1)*sizeof(struct image_info));
/*TODO*///				else
/*TODO*///					images[type] = malloc(sizeof(struct image_info));
/*TODO*///				if( !images[type] )
/*TODO*///					return 1;
/*TODO*///				memset(&images[type][count[type]], 0, sizeof(struct image_info));
/*TODO*///				count[type]++;
/*TODO*///			}
/*TODO*///			dev++;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	/* everything was fine */
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Call the init() functions for all devices of a driver
/*TODO*/// * with all user specified image names.
/*TODO*/// */
/*TODO*///int init_devices(const void *game)
/*TODO*///{
/*TODO*///	const struct GameDriver *gamedrv = game;
/*TODO*///	const struct IODevice *dev = gamedrv->dev;
/*TODO*///	int id;
/*TODO*///
/*TODO*///	/* initialize all devices */
/*TODO*///	while( dev->count )
/*TODO*///	{
/*TODO*///
/*TODO*///		/* try and check for valid image and compute 'partial' CRC
/*TODO*///		   for imageinfo if possible */
/*TODO*///		if( dev->id )
/*TODO*///		{
/*TODO*///			for( id = 0; id < dev->count; id++ )
/*TODO*///			{
/*TODO*///				int result;
/*TODO*///
/*TODO*///				/* initialize */
/*TODO*///				logerror("%s id (%s)\n", device_typename_id(dev->type,id), device_filename(dev->type,id) ? device_filename(dev->type,id) : "NULL");
/*TODO*///				result = (*dev->id)(id);
/*TODO*///				logerror("%s id returns %d\n", device_typename_id(dev->type,id), result);
/*TODO*///
/*TODO*///				if( result != ID_OK && device_filename(dev->type,id) )
/*TODO*///				{
/*TODO*///					mess_printf("%s id failed (%s)\n", device_typename_id(dev->type,id), device_filename(dev->type,id) );
/*TODO*////* HJB: I think we can't abort if a device->id function fails _yet_, because
/*TODO*/// * we first would have to clean up every driver to use the correct return values.
/*TODO*/// * device->init will fail if a file really can't be loaded.
/*TODO*/// */
/*TODO*////*					return 1; */
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("%s does not support id!\n", device_typename(dev->type));
/*TODO*///		}
/*TODO*///
/*TODO*///		/* if this device supports initialize (it should!) */
/*TODO*///		if( dev->init )
/*TODO*///		{
/*TODO*///			/* all instances */
/*TODO*///			for( id = 0; id < dev->count; id++ )
/*TODO*///			{
/*TODO*///				int result;
/*TODO*///
/*TODO*///				/* initialize */
/*TODO*///				logerror("%s init (%s)\n", device_typename_id(dev->type,id), device_filename(dev->type,id) ? device_filename(dev->type,id) : "NULL");
/*TODO*///				result = (*dev->init)(id);
/*TODO*///				logerror("%s init returns %d\n", device_typename_id(dev->type,id), result);
/*TODO*///
/*TODO*///				if( result != INIT_OK && device_filename(dev->type,id) )
/*TODO*///				{
/*TODO*///					mess_printf("%s init failed (%s)\n", device_typename_id(dev->type,id), device_filename(dev->type,id) );
/*TODO*///					return 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("%s does not support init!\n", device_typename(dev->type));
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Call the exit() functions for all devices of a
/*TODO*/// * driver for all images.
/*TODO*/// */
/*TODO*///void exit_devices(void)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	int type, id;
/*TODO*///
/*TODO*///	/* shutdown all devices */
/*TODO*///	while( dev->count )
/*TODO*///	{
/*TODO*///		/* all instances */
/*TODO*///		if( dev->exit)
/*TODO*///		{
/*TODO*///			/* shutdown */
/*TODO*///			for( id = 0; id < device_count(dev->type); id++ )
/*TODO*///				(*dev->exit)(id);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("%s does not support exit!\n", device_typename(dev->type));
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	for( type = 0; type < IO_COUNT; type++ )
/*TODO*///	{
/*TODO*///		if( images[type] )
/*TODO*///		{
/*TODO*///			for( id = 0; id < device_count(dev->type); id++ )
/*TODO*///			{
/*TODO*///				if( images[type][id].name )
/*TODO*///					free(images[type][id].name);
/*TODO*///				images[type][id].name = NULL;
/*TODO*///			}
/*TODO*///			free(images[type]);
/*TODO*///		}
/*TODO*///		images[type] = NULL;
/*TODO*///		count[type] = 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*/// * Change the associated image filename for a device.
/*TODO*/// * Returns 0 if successful.
/*TODO*/// */
/*TODO*///int device_filename_change(int type, int id, const char *name)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	struct image_info *img = &images[type][id];
/*TODO*///
/*TODO*///	if( type >= IO_COUNT )
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	while( dev->count && dev->type != type )
/*TODO*///		dev++;
/*TODO*///
/*TODO*///	if( id >= dev->count )
/*TODO*///		return 1;
/*TODO*///
/*TODO*///	if( dev->exit )
/*TODO*///		dev->exit(id);
/*TODO*///
/*TODO*///	if( dev->init )
/*TODO*///	{
/*TODO*///		int result;
/*TODO*///		/*
/*TODO*///		 * set the new filename and reset all addition info, it will
/*TODO*///		 * be inserted by osd_fopen() and the crc handling
/*TODO*///		 */
/*TODO*///		if( img->name )
/*TODO*///			free(img->name);
/*TODO*///		img->name = NULL;
/*TODO*///		img->length = 0;
/*TODO*///		img->crc = 0;
/*TODO*///		free_image_info(img);
/*TODO*///		if( name )
/*TODO*///		{
/*TODO*///			img->name = dupe(name);
/*TODO*///			if( !img->name )
/*TODO*///				return 1;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( dev->reset_depth == IO_RESET_CPU )
/*TODO*///			machine_reset();
/*TODO*///		else
/*TODO*///		if( dev->reset_depth == IO_RESET_ALL )
/*TODO*///		{
/*TODO*///			mess_keep_going = 1;
/*TODO*///
/*TODO*///		}
/*TODO*///
/*TODO*///		result = (*dev->init)(id);
/*TODO*///		if( result != INIT_OK && name )
/*TODO*///			return 1;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int device_open(int type, int id, int mode, void *args)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->open )
/*TODO*///			return (*dev->open)(id,mode,args);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///void device_close(int type, int id)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->close )
/*TODO*///		{
/*TODO*///			(*dev->close)(id);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int device_seek(int type, int id, int offset, int whence)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->seek )
/*TODO*///			return (*dev->seek)(id,offset,whence);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int device_tell(int type, int id)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->tell )
/*TODO*///			return (*dev->tell)(id);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int device_status(int type, int id, int newstatus)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->status )
/*TODO*///			return (*dev->status)(id,newstatus);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///int device_input(int type, int id)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->input )
/*TODO*///			return (*dev->input)(id);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void device_output(int type, int id, int data)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->output )
/*TODO*///		{
/*TODO*///			(*dev->output)(id,data);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int device_input_chunk(int type, int id, void *dst, int chunks)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->input_chunk )
/*TODO*///			return (*dev->input_chunk)(id,dst,chunks);
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///void device_output_chunk(int type, int id, void *src, int chunks)
/*TODO*///{
/*TODO*///	const struct IODevice *dev = Machine->gamedrv->dev;
/*TODO*///	while( dev && dev->count )
/*TODO*///	{
/*TODO*///		if( type == dev->type && dev->output )
/*TODO*///		{
/*TODO*///			(*dev->output_chunk)(id,src,chunks);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		dev++;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///int displayimageinfo(struct osd_bitmap *bitmap, int selected)
/*TODO*///{
/*TODO*///	char buf[2048], *dst = buf;
/*TODO*///	int type, id, sel = selected - 1;
/*TODO*///
/*TODO*///	dst += sprintf(dst,"%s\n\n",Machine->gamedrv->description);
/*TODO*///
/*TODO*///	for (type = 0; type < IO_COUNT; type++)
/*TODO*///	{
/*TODO*///		for( id = 0; id < device_count(type); id++ )
/*TODO*///		{
/*TODO*///			const char *name = device_filename(type,id);
/*TODO*///			if( name )
/*TODO*///			{
/*TODO*///				const char *info;
/*TODO*///				dst += sprintf(dst,"%s: %s\n", device_typename_id(type,id), device_filename(type,id));
/*TODO*///				info = device_longname(type,id);
/*TODO*///				if( info )
/*TODO*///					dst += sprintf(dst,"%s\n", info);
/*TODO*///				info = device_manufacturer(type,id);
/*TODO*///				if( info )
/*TODO*///				{
/*TODO*///					dst += sprintf(dst,"%s", info);
/*TODO*///					info = stripspace(device_year(type,id));
/*TODO*///					if( info && strlen(info))
/*TODO*///						dst += sprintf(dst,", %s", info);
/*TODO*///					dst += sprintf(dst,"\n");
/*TODO*///				}
/*TODO*///				info = device_playable(type,id);
/*TODO*///				if( info )
/*TODO*///					dst += sprintf(dst,"%s\n", info);
/*TODO*///// why is extrainfo printed? only MSX and NES use it that i know of ... Cowering
/*TODO*/////				info = device_extrainfo(type,id);
/*TODO*/////				if( info )
/*TODO*/////					dst += sprintf(dst,"%s\n", info);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				dst += sprintf(dst,"%s: ---\n", device_typename_id(type,id));
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel == -1)
/*TODO*///	{
/*TODO*///		/* startup info, print MAME version and ask for any key */
/*TODO*///
/*TODO*///		strcat(buf,"\n\tPress any key to Begin");
/*TODO*///		ui_drawbox(bitmap,0,0,Machine->uiwidth,Machine->uiheight);
/*TODO*///		ui_displaymessagewindow(bitmap, buf);
/*TODO*///
/*TODO*///		sel = 0;
/*TODO*///		if (code_read_async() != KEYCODE_NONE ||
/*TODO*///			code_read_async() != JOYCODE_NONE)
/*TODO*///			sel = -1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* menu system, use the normal menu keys */
/*TODO*///		strcat(buf,"\n\t\x1a Return to Main Menu \x1b");
/*TODO*///
/*TODO*///		ui_displaymessagewindow(bitmap,buf);
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_SELECT))
/*TODO*///			sel = -1;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CANCEL))
/*TODO*///			sel = -1;
/*TODO*///
/*TODO*///		if (input_ui_pressed(IPT_UI_CONFIGURE))
/*TODO*///			sel = -2;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (sel == -1 || sel == -2)
/*TODO*///	{
/*TODO*///		/* tell updatescreen() to clean after us */
/*TODO*///		need_to_clear_bitmap = 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	return sel + 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void showmessdisclaimer(void)
/*TODO*///{
/*TODO*///	mess_printf(
/*TODO*///		"MESS is an emulator: it reproduces, more or less faithfully, the behaviour of\n"
/*TODO*///		"several computer and console systems. But hardware is useless without software\n"
/*TODO*///		"so a file dump of the ROMs, cartridges, discs, and cassettes which run on that\n"
/*TODO*///		"hardware is required. Such files, like any other commercial software, are\n"
/*TODO*///		"copyrighted material and it is therefore illegal to use them if you don't own\n"
/*TODO*///		"the original media from which the files are derived. Needless to say, these\n"
/*TODO*///		"files are not distributed together with MESS. Distribution of MESS together\n"
/*TODO*///		"with these files is a violation of copyright law and should be promptly\n"
/*TODO*///		"reported to the authors so that appropriate legal action can be taken.\n\n");
/*TODO*///}
/*TODO*///
/*TODO*///void showmessinfo(void)
/*TODO*///{
/*TODO*///	mess_printf(
/*TODO*///		"M.E.S.S. v%s %s\n"
/*TODO*///		"Multiple Emulation Super System - Copyright (C) 1997-2000 by the MESS Team\n"
/*TODO*///		"M.E.S.S. is based on the excellent M.A.M.E. Source code\n"
/*TODO*///		"Copyright (C) 1997-2000 by Nicola Salmoria and the MAME Team\n\n",
/*TODO*///		build_version, mess_alpha);
/*TODO*///	showmessdisclaimer();
/*TODO*///	mess_printf(
/*TODO*///		"Usage:  MESS <system> <device> <software> <options>\n\n"
/*TODO*///		"        MESS -list        for a brief list of supported systems\n"
/*TODO*///		"        MESS -listfull    for a full list of supported systems\n"
/*TODO*///		"        MESS -listdevices for a full list of supported devices\n"
/*TODO*///		"See mess.txt for help, readme.txt for options.\n");
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///    
}
