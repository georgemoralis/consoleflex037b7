/*
 * ported to v0.37b6
 * 
 */
package mess.osdepend;

import WIP.arcadeflex.libc_v2.UBytePtr;
import static WIP.mame.osdependH.*;
import arcadeflex.javaspecific.CRC;
import static arcadeflex.libc.cstring.strchr;
import static arcadeflex.libc.cstring.strrchr;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static old.arcadeflex.libc_old.*;
import old.arcadeflex.osdepend;
import static old.arcadeflex.osdepend.logerror;

public class fileio {

    /*TODO*///#include "mamalleg.h"
/*TODO*///#include "driver.h"
/*TODO*///#include "unzip.h"
/*TODO*///#include <sys/stat.h>
/*TODO*///#include <unistd.h>
/*TODO*///#include <signal.h>
/*TODO*///#include "msdos.h"
/*TODO*///
/*TODO*////* Use the file cache ? */
/*TODO*///#define FILE_CACHE	1
/*TODO*///
/*TODO*///char *roms = NULL;
/*TODO*///char **rompathv = NULL;
/*TODO*///int rompathc = 0;
/*TODO*///
/*TODO*///char *samples = NULL;
/*TODO*///char **samplepathv = NULL;
/*TODO*///int samplepathc = 0;
/*TODO*///
/*TODO*///char *cfgdir, *nvdir, *hidir, *inpdir, *stadir;
/*TODO*///char *memcarddir, *artworkdir, *screenshotdir;
/*TODO*///char *cheatdir;							/* Steph */
/*TODO*///
/*TODO*///char *soft = NULL;
/*TODO*///char **softpathv = NULL;
/*TODO*///int softpathc = 0;
/*TODO*///
/*TODO*///char *alternate_name;                   /* for "-romdir" */
/*TODO*///
    public static final int kPlainFile = 1;
    public static final int kRAMFile = 2;
    public static final int kZippedFile = 3;

    public static class FakeFileHandle {

        public FILE file;
        public char[] data = new char[1];
        public /*unsigned*/ int offset;
        public /*unsigned*/ int length;
        public int type;
        public /*unsigned*/ int crc;
    }

    /*TODO*////* This function can be called several times with different parameters,
/*TODO*/// * for example by "mame -verifyroms *". */
/*TODO*///void decompose_rom_sample_path(char *rompath, char *samplepath)
/*TODO*///{
/*TODO*///	char *token;
/*TODO*///
/*TODO*///	/* start with zero path components */
/*TODO*///	rompathc = samplepathc = 0;
/*TODO*///
/*TODO*///	if (!roms)
/*TODO*///		roms = malloc(strlen(rompath) + 1);
/*TODO*///	else
/*TODO*///		roms = realloc(roms, strlen(rompath) + 1);
/*TODO*///
/*TODO*///	if (!samples)
/*TODO*///		samples = malloc(strlen(samplepath) + 1);
/*TODO*///	else
/*TODO*///		samples = realloc(samples, strlen(samplepath) + 1);
/*TODO*///
/*TODO*///	if (!roms || !samples)
/*TODO*///	{
/*TODO*///		logerror("decompose_rom_sample_path: failed to malloc!\n");
/*TODO*///		raise(SIGABRT);
/*TODO*///	}
/*TODO*///
/*TODO*///	strcpy(roms, rompath);
/*TODO*///	token = strtok(roms, ";");
/*TODO*///	while (token)
/*TODO*///	{
/*TODO*///		if (rompathc)
/*TODO*///			rompathv = realloc(rompathv, (rompathc + 1) * sizeof (char *));
/*TODO*///		else
/*TODO*///			rompathv = malloc(sizeof (char *));
/*TODO*///
/*TODO*///		if (!rompathv)
/*TODO*///			break;
/*TODO*///		rompathv[rompathc++] = token;
/*TODO*///		token = strtok(NULL, ";");
/*TODO*///	}
/*TODO*///
/*TODO*///	strcpy(samples, samplepath);
/*TODO*///	token = strtok(samples, ";");
/*TODO*///	while (token)
/*TODO*///	{
/*TODO*///		if (samplepathc)
/*TODO*///			samplepathv = realloc(samplepathv, (samplepathc + 1) * sizeof (char *));
/*TODO*///		else
/*TODO*///			samplepathv = malloc(sizeof (char *));
/*TODO*///
/*TODO*///		if (!samplepathv)
/*TODO*///			break;
/*TODO*///		samplepathv[samplepathc++] = token;
/*TODO*///		token = strtok(NULL, ";");
/*TODO*///	}
/*TODO*///
/*TODO*///#if FILE_CACHE
/*TODO*///	/* AM 980919 */
/*TODO*///	if (file_cache_max == 0)
/*TODO*///	{
/*TODO*///		/* (rom path directories + 1 buffer)==rompathc+1 */
/*TODO*///		/* (dir + .zip + .zif)==3 */
/*TODO*///		/* (clone+parent)==2 */
/*TODO*///		cache_allocate((rompathc + 1) * 3 * 2 + (softpathc + 1) * 3 * 2);
/*TODO*///	}
/*TODO*///#endif
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void decompose_software_path(char *softwarepath)
/*TODO*///{
/*TODO*///	char *token;
/*TODO*///
/*TODO*///	/* start with zero path components */
/*TODO*///	softpathc = 0;
/*TODO*///
/*TODO*///	if (!soft)
/*TODO*///		soft = malloc(strlen(softwarepath) + 1);
/*TODO*///	else
/*TODO*///		soft = realloc(soft, strlen(softwarepath) + 1);
/*TODO*///
/*TODO*///	if (!soft)
/*TODO*///	{
/*TODO*///		logerror("decompose_software_path: failed to malloc!\n");
/*TODO*///		raise(SIGABRT);
/*TODO*///	}
/*TODO*///
/*TODO*///	strcpy(soft, softwarepath);
/*TODO*///	token = strtok(soft, ";");
/*TODO*///	while (token)
/*TODO*///	{
/*TODO*///		if (softpathc)
/*TODO*///			softpathv = realloc(softpathv, (softpathc + 1) * sizeof (char *));
/*TODO*///		else
/*TODO*///			softpathv = malloc(sizeof (char *));
/*TODO*///
/*TODO*///		if (!softpathv)
/*TODO*///			break;
/*TODO*///		softpathv[softpathc++] = token;
/*TODO*///		token = strtok(NULL, ";");
/*TODO*///	}
/*TODO*///
/*TODO*///	logerror("Number of software paths is %d\n", softpathc);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*
/*TODO*/// * file handling routines
/*TODO*/// *
/*TODO*/// * gamename holds the driver name, filename is only used for ROMs and samples.
/*TODO*/// * if 'write' is not 0, the file is opened for write. Otherwise it is opened
/*TODO*/// * for read.
/*TODO*/// */
/*TODO*///
/*TODO*////*
/*TODO*/// * check if roms/samples for a game exist at all
/*TODO*/// * return index+1 of the path vector component on success, otherwise 0
/*TODO*/// */
/*TODO*///int osd_faccess(const char *newfilename, int filetype)
/*TODO*///{
/*TODO*///	static int indx;
/*TODO*///	static const char *filename;
/*TODO*///	char name[256];
/*TODO*///	char **pathv;
/*TODO*///	int pathc;
/*TODO*///	char *dir_name;
/*TODO*///
/*TODO*///	/* if filename == NULL, continue the search */
/*TODO*///	if (newfilename != NULL)
/*TODO*///	{
/*TODO*///		indx = 0;
/*TODO*///		filename = newfilename;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		indx++;
/*TODO*///
/*TODO*///	if (filetype == OSD_FILETYPE_ROM)
/*TODO*///	{
/*TODO*///		pathv = rompathv;
/*TODO*///		pathc = rompathc;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	if (filetype == OSD_FILETYPE_SAMPLE)
/*TODO*///	{
/*TODO*///		pathv = samplepathv;
/*TODO*///		pathc = samplepathc;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	if (filetype == OSD_FILETYPE_IMAGE_R ||
/*TODO*///		filetype == OSD_FILETYPE_IMAGE_RW)
/*TODO*///	{
/*TODO*///		pathv = softpathv;
/*TODO*///		pathc = softpathc;
/*TODO*///    }
/*TODO*///    else if (filetype == OSD_FILETYPE_SCREENSHOT)
/*TODO*///	{
/*TODO*///		void *f;
/*TODO*///
/*TODO*///		sprintf(name, "%s/%s.png", screenshotdir, newfilename);
/*TODO*///		f = fopen(name, "rb");
/*TODO*///		if (f)
/*TODO*///		{
/*TODO*///			fclose(f);
/*TODO*///			return 1;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			return 0;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	for (; indx < pathc; indx++)
/*TODO*///	{
/*TODO*///		struct stat stat_buffer;
/*TODO*///
/*TODO*///		dir_name = pathv[indx];
/*TODO*///
/*TODO*///		/* does such a directory (or file) exist? */
/*TODO*///		sprintf(name, "%s/%s", dir_name, filename);
/*TODO*///		if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			return indx + 1;
/*TODO*///
/*TODO*///		/* try again with a .zip extension */
/*TODO*///		sprintf(name, "%s/%s.zip", dir_name, filename);
/*TODO*///		if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			return indx + 1;
/*TODO*///
/*TODO*///		/* try again with a .zif extension */
/*TODO*///		sprintf(name, "%s/%s.zif", dir_name, filename);
/*TODO*///		if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			return indx + 1;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* no match */
/*TODO*///	return 0;
/*TODO*///}

    /* JB 980920 update */
 /* AM 980919 update */
    public static Object osd_fopen(String game, String filename, int filetype, int _write) {
        String name = "";
        String gamename;
        int found = 0;
        int indx;
        FakeFileHandle f;
        int pathc = 0;
        String[] pathv = null;

        f = new FakeFileHandle();
        gamename = game;
        /*TODO*///
/*TODO*///	/* Support "-romdir" yuck. */
/*TODO*///	if (alternate_name)
/*TODO*///	{
/*TODO*///		logerror("osd_fopen: -romdir overrides '%s' by '%s'\n", gamename, alternate_name);
/*TODO*///		gamename = alternate_name;
/*TODO*///	}

        switch (filetype) {
            case OSD_FILETYPE_ROM:
            case OSD_FILETYPE_SAMPLE:

                /* only for reading */
                if (_write != 0) {
                    logerror("osd_fopen: type %02x write not supported\n", filetype);
                    break;
                }

                if (filetype == OSD_FILETYPE_SAMPLE) {
                    logerror("osd_fopen: using samplepath\n");
//hacked used predefined paths
                    /*TODO*///			pathc = samplepathc;
/*TODO*///			pathv = samplepathv;
                    pathc = 1;
                    pathv = new String[1];
                    pathv[0] = "samples";
                } else {
                    logerror("osd_fopen: using rompath\n");
//hacked used predefined paths
                    /*TODO*///			pathc = rompathc;
/*TODO*///			pathv = rompathv;
                    pathc = 1;
                    pathv = new String[1];
                    pathv[0] = "bios";
                }
                for (indx = 0; indx < pathc && found == 0; ++indx) {
                    String dir_name = pathv[indx];
                    if (found == 0) {
                        name = sprintf("%s/%s", dir_name, gamename);
                        logerror("Trying %s\n", name);
                        //java code to emulate stat command (shadow)
                        osdepend.dlprogress.setFileName("loading file: " + name);
                        //case where file exists in rom folder
                        if (new File(name).isDirectory() && new File(name).exists()) // if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )               
                        {
                            name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                            if (filetype == OSD_FILETYPE_ROM) {
                                //java issue since there is no way to pass by reference the data table 
                                //get it here
                                f.file = fopen(name, "rb");
                                long size = ftell(f.file);
                                f.data = new char[(int) size];
                                fclose(f.file);
                                int tlen[] = new int[1];
                                int tcrc[] = new int[1];
                                if (checksum_file(name, f.data, tlen, tcrc) == 0) {
                                    f.type = kRAMFile;
                                    f.offset = 0;
                                    found = 1;
                                }
                                //copy values where they belong
                                f.length = tlen[0];
                                f.crc = tcrc[0];
                            } else {
                                f.type = kPlainFile;
                                f.file = fopen(name, "rb");
                                found = (f.file != null) ? 1 : 0; //found = f.file !=0;
                            }
                        }
                    }

                    if (found == 0) {
                        /* try with a .zip extension */
                        name = sprintf("%s/%s.zip", dir_name, gamename);
                        logerror("Trying %s\n", name);
                        if (new File(name).exists())//if (cache_stat(name, &stat_buffer) == 0)
                        {
                            byte[] bytes = unZipIt3(name, filename);
                            f.file = fopen(bytes, filename, "rb");
                            long size = ftell(f.file);
                            f.data = new char[(int) size];
                            fclose(f.file);
                            int tlen[] = new int[1];
                            int tcrc[] = new int[1];
                            checksum_file_zipped(bytes, filename, f.data, tlen, tcrc);
                            f.type = kZippedFile;
                            f.offset = 0;
                            f.length = tlen[0];
                            f.crc = tcrc[0];
                            found = 1;
                        }
                    }
                }
                break;

            case OSD_FILETYPE_IMAGE_R:
                /* only for reading */
                if (_write != 0) {
                    logerror("osd_fopen: type %02x write not supported\n", filetype);
                    break;
                }
//hacked predefined software directories
                /*TODO*///		pathc = softpathc;
/*TODO*///		pathv = softpathv;
                pathc = 1;
                pathv = new String[1];
                pathv[0] = "software";
                logerror("osd_fopen: using softwarepath (%d directories)\n", pathc);

                logerror("Open IMAGE_R '%s' for %s\n", filename, game);
                for (indx = 0; indx < pathc && found == 0; ++indx) {
                    String file = "";
                    String extension;
                    String dir_name = pathv[indx];

                    file = filename;//strcpy(file, filename);

                    /* load from exact path specified in mess.cfg */
                    if (found == 0) {
                        name = sprintf("%s", dir_name);
                        if (new File(name).isDirectory() && new File(name).exists())//if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
                        {
                            /*TODO*///					sprintf(name, "%s/%s", dir_name, filename);
/*TODO*///					if (checksum_file(name, &f->data, &f->length, &f->crc) == 0)
/*TODO*///					{
/*TODO*///						logerror("Found '%s'\n", name);
/*TODO*///						f->type = kRAMFile;
/*TODO*///						f->offset = 0;
/*TODO*///						found = 1;
/*TODO*///					}
                        }
                    }

                    /* load from path specified in mess.cfg with system name appended */
                    if (found == 0) {
                        name = sprintf("%s/%s", dir_name, game);
                        if (new File(name).isDirectory() && new File(name).exists())//if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
                        {
                            /*TODO*///					sprintf(name, "%s/%s/%s", dir_name, game, filename);
/*TODO*///					if (checksum_file(name, &f->data, &f->length, &f->crc) == 0)
/*TODO*///					{
/*TODO*///						logerror("Found '%s'\n", name);
/*TODO*///						f->type = kRAMFile;
/*TODO*///						f->offset = 0;
/*TODO*///						found = 1;
/*TODO*///					}
                        }
                    }

                    /* load zipped image from exact path specified in mess.cfg */
 /*TODO*///			if (found==0)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s", dir_name, filename);
/*TODO*///				extension = strrchr(name, '.');		/* find extension */
/*TODO*///				if (extension)
/*TODO*///					strcpy(extension, ".zip");
/*TODO*///				else
/*TODO*///					strcat(name, ".zip");
/*TODO*///				logerror("Trying %s\n", name);
/*TODO*///				if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///				{
/*TODO*///					if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///					{
/*TODO*///						logerror("Found '%s'\n", name);
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32(0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			/* load zipped image from path specified in mess.cfg with system name appended */
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s/%s", dir_name, game, filename);
/*TODO*///				extension = strrchr(name, '.');		/* find extension */
/*TODO*///				if (extension)
/*TODO*///					strcpy(extension, ".zip");
/*TODO*///				else
/*TODO*///					strcat(name, ".zip");
/*TODO*///				logerror("Trying %s\n", name);
/*TODO*///				if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///				{
/*TODO*///					if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///					{
/*TODO*///						logerror("Found '%s'\n", name);
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32(0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			/* load image from exact path specified in mess.cfg and try system.zip */
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s.zip", dir_name, game);
/*TODO*///				logerror("Trying %s\n", name);
/*TODO*///				if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///				{
/*TODO*///					if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///					{
/*TODO*///						logerror("Found '%s'\n", name);
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						f->crc = crc32(0L, f->data, f->length);
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///            }

//the above code is custom to read file with extension (shadow)
                    extension = strrchr(file, '.');
                    if (extension != null && strchr(extension, '/') == null && strchr(extension, '\\') == null) {
                        /* strip extension */
                        //extension = file.substring(Integer.parseInt(extension), file.length());//*extension++ = '\0';
                        if (found == 0) {
                            name = sprintf("%s/%s/%s", dir_name, game, filename);
                            logerror("Trying %s\n", name);
                            //java code to emulate stat command (shadow)
                            osdepend.dlprogress.setFileName("loading file: " + name);
                            //case where file exists in rom folder
                            if (new File(name).exists()) // if( cache_stat (name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR) )               
                            {
                                //name = sprintf("%s/%s/%s", dir_name, gamename, filename);
                                //java issue since there is no way to pass by reference the data table 
                                //get it here
                                f.file = fopen(name, "rb");
                                long size = ftell(f.file);
                                f.data = new char[(int) size];
                                fclose(f.file);
                                int tlen[] = new int[1];
                                int tcrc[] = new int[1];
                                if (checksum_file(name, f.data, tlen, tcrc) == 0) {
                                    f.type = kRAMFile;
                                    f.offset = 0;
                                    found = 1;
                                }
                                //copy values where they belong
                                f.length = tlen[0];
                                f.crc = tcrc[0];

                            }
                        }
                        if (found == 0) {
                            /* try with a .zip extension */
                            name = sprintf("%s/%s/%s.zip", dir_name, game,file.substring(0, Integer.parseInt(extension)));
                            logerror("Trying %s\n", name);
                            if (new File(name).exists())//if (cache_stat(name, &stat_buffer) == 0)
                            {
                                byte[] bytes = unZipIt3(name, filename);
                                f.file = fopen(bytes, filename, "rb");
                                long size = ftell(f.file);
                                f.data = new char[(int) size];
                                fclose(f.file);
                                int tlen[] = new int[1];
                                int tcrc[] = new int[1];
                                checksum_file_zipped(bytes, filename, f.data, tlen, tcrc);
                                f.type = kZippedFile;
                                f.offset = 0;
                                f.length = tlen[0];
                                f.crc = tcrc[0];
                                found = 1;
                            }
                        }

                        /* load from path specified in mess.cfg but append extension */
 /*TODO*///				if (!found)
/*TODO*///				{
/*TODO*///					sprintf(name, "%s/%s", dir_name, extension);
/*TODO*///					if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///					{
/*TODO*///						sprintf(name, "%s/%s/%s", dir_name, extension, filename);
/*TODO*///						if (checksum_file(name, &f->data, &f->length, &f->crc) == 0)
/*TODO*///						{
/*TODO*///							logerror("Found '%s'\n", name);
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
                        /* load from path specified in mess.cfg with system name and extension appended */
 /*TODO*///				if (found==0)
/*TODO*///				{
/*TODO*///					sprintf(name, "%s/%s/%s", dir_name, game, extension);
/*TODO*///					if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///					{
/*TODO*///						sprintf(name, "%s/%s/%s/%s", dir_name, game, extension, filename);
/*TODO*///						if (checksum_file(name, &f->data, &f->length, &f->crc) == 0)
/*TODO*///						{
/*TODO*///							logerror("Found '%s'\n", name);
/*TODO*///							f->type = kRAMFile;
/*TODO*///							f->offset = 0;
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* load zipped image from path specified in mess.cfg with extension appended */
/*TODO*///				if (!found)
/*TODO*///				{
/*TODO*///					sprintf(name, "%s/%s/%s.zip", dir_name, extension, file);
/*TODO*///					logerror("Trying %s\n", name);
/*TODO*///					if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///					{
/*TODO*///						if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///						{
/*TODO*///							logerror("Found '%s'\n", name);
/*TODO*///							f->type = kZippedFile;
/*TODO*///							f->offset = 0;
/*TODO*///							f->crc = crc32(0L, f->data, f->length);
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* load zipped image from path specified in mess.cfg with system name and extension appended */
/*TODO*///				if (!found)
/*TODO*///				{
/*TODO*///					sprintf(name, "%s/%s/%s/%s.zip", dir_name, game, extension, file);
/*TODO*///					logerror("Trying %s\n", name);
/*TODO*///					if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///					{
/*TODO*///						if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///						{
/*TODO*///							logerror("Found '%s'\n", name);
/*TODO*///							f->type = kZippedFile;
/*TODO*///							f->offset = 0;
/*TODO*///							f->crc = crc32(0L, f->data, f->length);
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				/* load image from path specified in mess.cfg and try appended extension/system.zip */
/*TODO*///				if (!found)
/*TODO*///				{
/*TODO*///					sprintf(name, "%s/%s/%s.zip", dir_name, extension, game);
/*TODO*///					logerror("Trying %s\n", name);
/*TODO*///					if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///					{
/*TODO*///						if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///						{
/*TODO*///							logerror("Found '%s'\n", name);
/*TODO*///							f->type = kZippedFile;
/*TODO*///							f->offset = 0;
/*TODO*///							f->crc = crc32(0L, f->data, f->length);
/*TODO*///							found = 1;
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
                    }

                    if (found != 0) {
                        logerror("IMAGE_R %s FOUND in %s!\n", filename, name);
                    }
                }
                break;
            /* end of IMAGE_R */

 /*TODO*///	case OSD_FILETYPE_IMAGE_RW:
/*TODO*///		{
/*TODO*///			static char *write_modes[] = {"rb", "wb", "r+b", "r+b", "w+b"};
/*TODO*///			char file[256];
/*TODO*///			char *extension;
/*TODO*///
/*TODO*///            logerror("Open IMAGE_RW '%s' for %s mode '%s'\n", filename, game, write_modes[_write]);
/*TODO*///			strcpy(file, filename);
/*TODO*///
/*TODO*///            pathc = softpathc;
/*TODO*///            pathv = softpathv;
/*TODO*///			logerror("osd_fopen: using softwarepath (%d directories)\n", pathc);
/*TODO*///
/*TODO*///            do
/*TODO*///			{
/*TODO*///				for (indx = 0; indx < pathc; indx++)
/*TODO*///				{
/*TODO*///					const char *dir_name = pathv[indx];
/*TODO*///
/*TODO*///					/* Exact path support */
/*TODO*///					if (!found)
/*TODO*///					{
/*TODO*///						sprintf(name, "%s/%s", dir_name, gamename);
/*TODO*///						logerror("Trying %s directory\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///						{
/*TODO*///							sprintf(name, "%s/%s/%s", dir_name, gamename, file);
/*TODO*///							logerror("Trying %s\n", name);
/*TODO*///							f->file = fopen(name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if (!found && _write == 3)
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					/* Steph - Zip disk images support for MESS */
/*TODO*///					if (!found && !_write)
/*TODO*///					{
/*TODO*///						extension = strrchr(name, '.');		/* find extension */
/*TODO*///						/* add .zip for zipfile */
/*TODO*///						if (extension)
/*TODO*///							strcpy(extension, ".zip");
/*TODO*///						else
/*TODO*///							strcat(extension, ".zip");
/*TODO*///						logerror("Trying %s\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///						{
/*TODO*///							if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///							{
/*TODO*///								logerror("Using (osd_fopen) zip file for %s\n", filename);
/*TODO*///								f->type = kZippedFile;
/*TODO*///								f->offset = 0;
/*TODO*///								f->crc = crc32(0L, f->data, f->length);
/*TODO*///								found = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (!found)
/*TODO*///					{
/*TODO*///						sprintf(name, "%s", dir_name);
/*TODO*///						logerror("Trying %s directory\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///						{
/*TODO*///							sprintf(name, "%s/%s", dir_name, file);
/*TODO*///							logerror("Trying %s\n", name);
/*TODO*///							f->file = fopen(name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if (!found && _write == 3)
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (!found && !_write)
/*TODO*///					{
/*TODO*///						extension = strrchr(name, '.');		/* find extension */
/*TODO*///						/* add .zip for zipfile */
/*TODO*///						if (extension)
/*TODO*///							strcpy(extension, ".zip");
/*TODO*///						else
/*TODO*///							strcat(extension, ".zip");
/*TODO*///						logerror("Trying %s\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///						{
/*TODO*///							if (load_zipped_file(name, filename, &f->data, &f->length) == 0)
/*TODO*///							{
/*TODO*///								logerror("Using (osd_fopen) zip file for %s\n", filename);
/*TODO*///								f->type = kZippedFile;
/*TODO*///								f->offset = 0;
/*TODO*///								f->crc = crc32(0L, f->data, f->length);
/*TODO*///								found = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (!found && !_write)
/*TODO*///					{
/*TODO*///						/* try with a .zip extension */
/*TODO*///						sprintf(name, "%s/%s.zip", dir_name, gamename);
/*TODO*///						logerror("Trying %s\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///						{
/*TODO*///							if (load_zipped_file(name, file, &f->data, &f->length) == 0)
/*TODO*///							{
/*TODO*///								logerror("Using (osd_fopen) zip file for %s\n", filename);
/*TODO*///								f->type = kZippedFile;
/*TODO*///								f->offset = 0;
/*TODO*///								f->crc = crc32(0L, f->data, f->length);
/*TODO*///								found = 1;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///
/*TODO*///					if (!found)
/*TODO*///					{
/*TODO*///						/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///						sprintf(name, "%s/%s.zip", dir_name, gamename);
/*TODO*///						logerror("Trying %s ZipMagic directory\n", name);
/*TODO*///						if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///						{
/*TODO*///							sprintf(name, "%s/%s.zip/%s", dir_name, gamename, file);
/*TODO*///							logerror("Trying %s\n", name);
/*TODO*///							f->file = fopen(name, write_modes[_write]);
/*TODO*///							found = f->file != 0;
/*TODO*///							if (!found && _write == 3)
/*TODO*///							{
/*TODO*///								f->file = fopen(name, write_modes[4]);
/*TODO*///								found = f->file != 0;
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					if (found)
/*TODO*///						logerror("IMAGE_RW %s FOUND in %s!\n", file, name);
/*TODO*///				}
/*TODO*///
/*TODO*///				extension = strrchr(file, '.');
/*TODO*///				if (extension)
/*TODO*///					*extension = '\0';
/*TODO*///			} while (!found && extension);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_NVRAM:
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			sprintf(name, "%s/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf(name, "%s.zip/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s.zif/%s.nv", nvdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_HIGHSCORE:
/*TODO*///		if (mame_highscore_enabled())
/*TODO*///		{
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///				sprintf(name, "%s.zip/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///				sprintf(name, "%s.zif/%s.hi", hidir, gamename);
/*TODO*///				f->type = kPlainFile;
/*TODO*///				f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///				found = f->file != 0;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_CONFIG:
/*TODO*///		sprintf(name, "%s/%s.cfg", cfgdir, gamename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf(name, "%s.zip/%s.cfg", cfgdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s.zif/%s.cfg", cfgdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_INPUTLOG:
/*TODO*///		sprintf(name, "%s/%s.inp", inpdir, gamename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf(name, "%s.zip/%s.cfg", inpdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s.zif/%s.cfg", inpdir, gamename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!_write)
/*TODO*///		{
/*TODO*///			char file[256];
/*TODO*///
/*TODO*///			sprintf(file, "%s.inp", gamename);
/*TODO*///			sprintf(name, "%s/%s.zip", inpdir, gamename);
/*TODO*///			logerror("Trying %s in %s\n", file, name);
/*TODO*///			if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			{
/*TODO*///				if (load_zipped_file(name, file, &f->data, &f->length) == 0)
/*TODO*///				{
/*TODO*///					logerror("Using (osd_fopen) zip file %s for %s\n", name, file);
/*TODO*///					f->type = kZippedFile;
/*TODO*///					f->offset = 0;
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_STATE:
/*TODO*///		sprintf(name, "%s/%s.sta", stadir, gamename);
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = !(f->file == 0);
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf(name, "%s.zip/%s.sta", stadir, gamename);
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = !(f->file == 0);
/*TODO*///		}
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s.zif/%s.sta", stadir, gamename);
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = !(f->file == 0);
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_ARTWORK:
/*TODO*///		/* only for reading */
/*TODO*///		if (_write)
/*TODO*///		{
/*TODO*///			logerror("osd_fopen: type %02x write not supported\n", filetype);
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		sprintf(name, "%s/%s", artworkdir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip directory (if ZipMagic is installed) */
/*TODO*///			sprintf(name, "%s.zip/%s.png", artworkdir, filename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s.zif/%s.png", artworkdir, filename);
/*TODO*///			f->type = kPlainFile;
/*TODO*///			f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///			found = f->file != 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			char file[256], *extension;
/*TODO*///
/*TODO*///			sprintf(file, "%s", filename);
/*TODO*///			sprintf(name, "%s/%s", artworkdir, filename);
/*TODO*///			extension = strrchr(name, '.');
/*TODO*///			if (extension)
/*TODO*///				strcpy(extension, ".zip");
/*TODO*///			else
/*TODO*///				strcat(name, ".zip");
/*TODO*///			logerror("Trying %s in %s\n", file, name);
/*TODO*///			if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			{
/*TODO*///				if (load_zipped_file(name, file, &f->data, &f->length) == 0)
/*TODO*///				{
/*TODO*///					logerror("Using (osd_fopen) zip file %s\n", name);
/*TODO*///					f->type = kZippedFile;
/*TODO*///					f->offset = 0;
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			if (!found)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s.zip", artworkdir, game);
/*TODO*///				logerror("Trying %s in %s\n", file, name);
/*TODO*///				if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///				{
/*TODO*///					if (load_zipped_file(name, file, &f->data, &f->length) == 0)
/*TODO*///					{
/*TODO*///						logerror("Using (osd_fopen) zip file %s\n", name);
/*TODO*///						f->type = kZippedFile;
/*TODO*///						f->offset = 0;
/*TODO*///						found = 1;
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_MEMCARD:
/*TODO*///		sprintf(name, "%s/%s", memcarddir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_SCREENSHOT:
/*TODO*///		/* only for writing */
/*TODO*///		if (!_write)
/*TODO*///		{
/*TODO*///			logerror("osd_fopen: type %02x read not supported\n", filetype);
/*TODO*///			break;
/*TODO*///		}
/*TODO*///
/*TODO*///		sprintf(name, "%s/%s.png", screenshotdir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		f->file = fopen(name, _write ? "wb" : "rb");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_HIGHSCORE_DB:
/*TODO*///	case OSD_FILETYPE_HISTORY:
/*TODO*///		/* only for reading */
/*TODO*///		if (_write)
/*TODO*///		{
/*TODO*///			logerror("osd_fopen: type %02x write not supported\n", filetype);
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		f->type = kPlainFile;
/*TODO*///		/* open as ASCII files, not binary like the others */
/*TODO*///		f->file = fopen(filename, _write ? "w" : "r");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///
/*TODO*///		/* Steph */
/*TODO*///	case OSD_FILETYPE_CHEAT:
/*TODO*///		sprintf(name, "%s/%s", cheatdir, filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		/* open as ASCII files, not binary like the others */
/*TODO*///		f->file = fopen(filename, _write ? "a" : "r");
/*TODO*///		found = f->file != 0;
/*TODO*///		break;
/*TODO*///
/*TODO*///	case OSD_FILETYPE_LANGUAGE:
/*TODO*///		/* only for reading */
/*TODO*///		if (_write)
/*TODO*///		{
/*TODO*///			logerror("osd_fopen: type %02x write not supported\n", filetype);
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		sprintf(name, "%s.lng", filename);
/*TODO*///		f->type = kPlainFile;
/*TODO*///		/* open as ASCII files, not binary like the others */
/*TODO*///		f->file = fopen(name, _write ? "w" : "r");
/*TODO*///		found = f->file != 0;
/*TODO*///		logerror("fopen %s = %08x\n", name, (int) f->file);
/*TODO*///		break;
/*TODO*///
            default:
                logerror("osd_fopen(): unknown filetype %02x\n", filetype);
        }

        if (found == 0) {
            f = null;
            return null;
        }

        return f;
    }

    /* JB 980920 update */
    public static int osd_fread(Object file, char[] buffer, int offset, int length) {
        FakeFileHandle f = (FakeFileHandle) file;

        switch (f.type) {
            case kPlainFile:
                return fread(buffer, offset, 1, length, f.file);
            //break;
            case kZippedFile:
            case kRAMFile:
                /* reading from the RAM image of a file */
                if (f.data != null) {
                    if (length + f.offset > f.length) {
                        length = f.length - f.offset;
                    }
                    memcpy(buffer, offset, f.data, f.offset, length);//memcpy(buffer, f->offset + f->data, length);
                    f.offset += length;
                    return length;
                }
                break;
        }

        return 0;
    }

    public static int osd_fread(Object file, UBytePtr buffer, int length) {
        osd_fread(file, buffer.memory, buffer.offset, length);
        return 0;
    }

    public static int osd_fread(Object file, char[] buffer, int length) {
        return osd_fread(file, buffer, 0, length);
    }

    public static int osd_fread_lsbfirst(Object file, char[] buffer, int length) {
        return osd_fread(file, buffer, 0, length);
    }

    public static int osd_fread_lsbfirst(Object file, byte[] buffer, int length) {
        char[] buf = new char[length];
        int r = osd_fread(file, buf, 0, length);
        for (int i = 0; i < buf.length; i++) {
            buffer[i] = (byte) buf[i];
        }
        return r;
    }

    public static int osd_fread(Object file, byte[] buffer, int length) {
        char[] buf = new char[length];
        int r = osd_fread(file, buf, 0, length);
        for (int i = 0; i < buf.length; i++) {
            buffer[i] = (byte) buf[i];
        }
        return r;
    }
    public static int osd_fread(Object file, UBytePtr buffer, int offset, int length) {
        return osd_fread(file, buffer.memory, buffer.offset + offset, length);     
    }

    /*TODO*///int osd_fread_swap(void *file, void *buffer, int length)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	unsigned char *buf;
/*TODO*///	unsigned char temp;
/*TODO*///	int res;
/*TODO*///
/*TODO*///
/*TODO*///	res = osd_fread(file, buffer, length);
/*TODO*///
/*TODO*///	buf = buffer;
/*TODO*///	for (i = 0; i < length; i += 2)
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////* AM 980919 update */
/*TODO*///int osd_fwrite(void *file, const void *buffer, int length)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	switch (f->type)
/*TODO*///	{
/*TODO*///	case kPlainFile:
/*TODO*///		return fwrite(buffer, 1, length, ((FakeFileHandle *) file)->file);
/*TODO*///	default:
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///int osd_fwrite_swap(void *file, const void *buffer, int length)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	unsigned char *buf;
/*TODO*///	unsigned char temp;
/*TODO*///	int res;
/*TODO*///
/*TODO*///
/*TODO*///	buf = (unsigned char *) buffer;
/*TODO*///	for (i = 0; i < length; i += 2)
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	res = osd_fwrite(file, buffer, length);
/*TODO*///
/*TODO*///	for (i = 0; i < length; i += 2)
/*TODO*///	{
/*TODO*///		temp = buf[i];
/*TODO*///		buf[i] = buf[i + 1];
/*TODO*///		buf[i + 1] = temp;
/*TODO*///	}
/*TODO*///
/*TODO*///	return res;
/*TODO*///}
/*TODO*///
    public static int osd_fread_scatter(Object file, UBytePtr buffer, int length, int increment) {
        //unsigned char *buf = buffer;
        FakeFileHandle f = (FakeFileHandle) file;
        char[] tempbuf = new char[4096];
        int totread, r, i;
        int buf = 0;
        switch (f.type) {
            case kPlainFile:
                totread = 0;
                while (length != 0) {
                    r = length;
                    if (r > 4096) {
                        r = 4096;
                    }
                    r = fread(tempbuf, buffer.offset, 1, r, f.file);
                    if (r == 0) {
                        return totread;
                        /* error */
                    }
                    for (i = 0; i < r; i++) {
                        buffer.write(buf, tempbuf[i]);
                        buf += increment;
                    }
                    totread += r;
                    length -= r;
                }
                return totread;
            //break;
            case kZippedFile:
            case kRAMFile:
                /* reading from the RAM image of a file */
                if (f.data != null) {
                    if (length + f.offset > f.length) {
                        length = f.length - f.offset;
                    }
                    for (i = 0; i < length; i++) {
                        buffer.write(buf, f.data[f.offset + i]);
                        buf += increment;
                    }
                    f.offset += length;
                    return length;
                }
                break;
        }

        return 0;
    }

    /* JB 980920 update */
    public static int osd_fseek(Object file, int offset, int whence) {
        FakeFileHandle f = (FakeFileHandle) file;
        int err = 0;

        switch (f.type) {
            case kPlainFile:
                switch (whence) {
                    case SEEK_SET:
                        fseek(f.file, offset, SEEK_SET);
                        return 0;
                    case SEEK_CUR:
                        fseek(f.file, offset, SEEK_CUR);
                        return 0;
                    default:
                        throw new UnsupportedOperationException("FSEEK other than SEEK_SET NOT SUPPORTED.");
                }
            //break;
            case kZippedFile:
            case kRAMFile:
                /* seeking within the RAM image of a file */
                switch (whence) {
                    case SEEK_SET:
                        f.offset = offset;
                        break;
                    case SEEK_CUR:
                        f.offset += offset;
                        break;
                    case SEEK_END:
                        f.offset = f.length + offset;
                        break;
                }
                break;
        }

        return err;
    }

    /* JB 980920 update */
    public static void osd_fclose(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        switch (f.type) {
            case kPlainFile:
                fclose(f.file);
                break;
            case kZippedFile:
            case kRAMFile:
                if (f.data != null) {
                    f.data = null;
                }
                break;
        }
        f = null;
    }

    public static int checksum_file(String file, char[] p, int[] size, int[] crc) {
        FILE f;
        f = fopen(file, "rb");
        if (f == null) {
            return -1;
        }

        long length = ftell(f);

        if (fread(p, 1, (int) length, f) != length) {
            fclose(f);
            return -1;
        }
        size[0] = (int) length;
        crc[0] = (int) CRC.crc(p, size[0]);

        return 0;
    }

    /**
     * arcadeflex specific function for checking crc from zipped file
     */
    public static int checksum_file_zipped(byte[] bytes, String filename, char[] p, int[] size, int[] crc) {
        FILE f;
        f = fopen(bytes, filename, "rb");
        if (f == null) {
            return -1;
        }

        long length = ftell(f);

        if (fread(p, 0, 1, (int) length, f) != length) {
            fclose(f);
            return -1;
        }
        size[0] = (int) length;
        crc[0] = (int) CRC.crc(p, size[0]);
        return 0;
    }

    /*TODO*///
/*TODO*////* JB 980920 updated */
/*TODO*////* AM 980919 updated */
/*TODO*///int osd_fchecksum(const char *game, const char *filename, unsigned int *length, unsigned int *sum)
/*TODO*///{
/*TODO*///	char name[256];
/*TODO*///	int indx;
/*TODO*///	struct stat stat_buffer;
/*TODO*///	int found = 0;
/*TODO*///	const char *gamename = game;
/*TODO*///
/*TODO*///	/* Support "-romdir" yuck. */
/*TODO*///	if (alternate_name)
/*TODO*///		gamename = alternate_name;
/*TODO*///
/*TODO*///	for (indx = 0; indx < rompathc && !found; indx++)
/*TODO*///	{
/*TODO*///		const char *dir_name = rompathv[indx];
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			sprintf(name, "%s/%s", dir_name, gamename);
/*TODO*///			if (cache_stat(name, &stat_buffer) == 0 && (stat_buffer.st_mode & S_IFDIR))
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s/%s", dir_name, gamename, filename);
/*TODO*///				if (checksum_file(name, 0, length, sum) == 0)
/*TODO*///				{
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zip extension */
/*TODO*///			sprintf(name, "%s/%s.zip", dir_name, gamename);
/*TODO*///			if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			{
/*TODO*///				if (checksum_zipped_file(name, filename, length, sum) == 0)
/*TODO*///				{
/*TODO*///					logerror("Using (osd_fchecksum) zip file for %s\n", filename);
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///
/*TODO*///		if (!found)
/*TODO*///		{
/*TODO*///			/* try with a .zif directory (if ZipFolders is installed) */
/*TODO*///			sprintf(name, "%s/%s.zif", dir_name, gamename);
/*TODO*///			if (cache_stat(name, &stat_buffer) == 0)
/*TODO*///			{
/*TODO*///				sprintf(name, "%s/%s.zif/%s", dir_name, gamename, filename);
/*TODO*///				if (checksum_file(name, 0, length, sum) == 0)
/*TODO*///				{
/*TODO*///					found = 1;
/*TODO*///				}
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (!found)
/*TODO*///		return -1;
/*TODO*///
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
    public static int osd_fsize(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        if (f.type == kRAMFile || f.type == kZippedFile) {
            return f.length;
        }

        if (f.file != null) {
            int size;
            size = (int) ftell(f.file); //don't need the above just get the file size
            return size;
        }

        return 0;
    }

    public static int osd_fcrc(Object file) {
        FakeFileHandle f = (FakeFileHandle) file;

        return f.crc;
    }

    /*TODO*///int osd_fgetc(void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if (f->type == kPlainFile && f->file)
/*TODO*///		return fgetc(f->file);
/*TODO*///	else
/*TODO*///		return EOF;
/*TODO*///}
/*TODO*///
/*TODO*///int osd_ungetc(int c, void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if (f->type == kPlainFile && f->file)
/*TODO*///		return ungetc(c, f->file);
/*TODO*///	else
/*TODO*///		return EOF;
/*TODO*///}
/*TODO*///
/*TODO*///char *osd_fgets(char *s, int n, void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if (f->type == kPlainFile && f->file)
/*TODO*///		return fgets(s, n, f->file);
/*TODO*///	else
/*TODO*///		return NULL;
/*TODO*///}
/*TODO*///
/*TODO*///int osd_feof(void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if (f->type == kPlainFile && f->file)
/*TODO*///		return feof(f->file);
/*TODO*///	else
/*TODO*///		return 1;
/*TODO*///}
/*TODO*///
/*TODO*///int osd_ftell(void *file)
/*TODO*///{
/*TODO*///	FakeFileHandle *f = (FakeFileHandle *) file;
/*TODO*///
/*TODO*///	if (f->type == kPlainFile && f->file)
/*TODO*///		return ftell(f->file);
/*TODO*///	else
/*TODO*///		return -1L;
/*TODO*///}
/*TODO*///
/*TODO*///
    /* called while loading ROMs. It is called a last time with name == 0 to signal */
 /* that the ROM loading process is finished. */
 /* return non-zero to abort loading */
    public static int osd_display_loading_rom_message(String name, int current, int total) {
        if (name != null) {
            System.out.print("loading " + name + "\r");
        } else {
            System.out.print("                    \r");
        }
        /*TODO*///            if( keyboard_pressed (KEYCODE_LCONTROL) && keyboard_pressed (KEYCODE_C) )
/*TODO*///                    return 1;
        return 0;
    }

    /*TODO*///
/*TODO*///int osd_select_file(int sel, char *filename)
/*TODO*///{
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///    
    public static byte[] unZipIt3(String zipFile, String filename) {
        byte[] out = null;

        if (!new File(zipFile).exists()) {
            System.out.println("unzip failed");
        } else {
            //System.out.println("entered unzipit2 for "+filename);

            byte[] buffer = new byte[1024];

            try {
                //get the zip file content
                ZipInputStream zis
                        = new ZipInputStream(new FileInputStream(zipFile));
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while (ze != null) {

                    String fileName = ze.getName();
                    if (fileName.equalsIgnoreCase(filename)) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }

                        baos.close();
                        out = baos.toByteArray();
                    }
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();

                //System.out.println("Done ");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return out;
    }
}
