/**
 * ported to v0.37b7
 *
 */
package sound;

import WIP.arcadeflex.libc_v2.ShortPtr;
import static WIP.arcadeflex.libc_v2.sprintf;
import static WIP.mame.mame.Machine;
import static WIP.mame.sndintrf.*;
import static WIP.mame.sndintrfH.*;
import static consoleflex.funcPtr.*;
import static old.sound.streams.*;
import static sound.waveH.*;

public class wave extends snd_interface {

    public wave() {
        sound_num = SOUND_WAVE;
        name = "Cassette";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((Wave_interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    /* Our private wave file structure */
    public static class struct_wave_file {

        int channel;/* channel for playback */
 /*TODO*///	void *file; 			/* osd file handle */
/*TODO*///	int mode;				/* write mode? */
/*TODO*///	int (*fill_wave)(INT16 *,int,UINT8*);
/*TODO*///	void *timer;			/* timer (TIME_NEVER) for reading sample values */
/*TODO*///	INT16 play_sample;		/* current sample value for playback */
/*TODO*///	INT16 record_sample;	/* current sample value for playback */
/*TODO*///	int display;			/* display tape status on screen */
/*TODO*///	int offset; 			/* offset set by device_seek function */
/*TODO*///	int play_pos;			/* sample position for playback */
/*TODO*///	int record_pos; 		/* sample position for recording */
/*TODO*///	int counter;			/* sample fraction counter for playback */
/*TODO*///	int smpfreq;			/* sample frequency from the WAV header */
/*TODO*///	int resolution; 		/* sample resolution in bits/sample (8 or 16) */
/*TODO*///	int samples;			/* number of samples (length * resolution / 8) */
/*TODO*///	int length; 			/* length in bytes */
/*TODO*///	void *data; 			/* sample data */
/*TODO*///	int status;				/* other status (mute, motor inhibit) */
    };

    static Wave_interface intf;
    static struct_wave_file[] wave = new struct_wave_file[MAX_WAVE];// = {{-1,},{-1,}};

    /*TODO*///#ifdef LSB_FIRST
/*TODO*///#define intelLong(x) (x)
/*TODO*///#else
/*TODO*///#define intelLong(x) (((x << 24) | (((unsigned long) x) >> 24) | \
/*TODO*///                       (( x & 0x0000ff00) << 8) | (( x & 0x00ff0000) >> 8)))
/*TODO*///#endif
    public static final int WAVE_OK = 0;
    public static final int WAVE_ERR = 1;
    public static final int WAVE_FMT = 2;

    /*TODO*////*****************************************************************************
/*TODO*/// * helper functions
/*TODO*/// *****************************************************************************/
/*TODO*///static int wave_read(int id)
/*TODO*///{
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///    UINT32 offset = 0;
/*TODO*///	UINT32 filesize, temp32;
/*TODO*///	UINT16 channels, bits, temp16;
/*TODO*///	char buf[32];
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return WAVE_ERR;
/*TODO*///
/*TODO*///    /* read the core header and make sure it's a WAVE file */
/*TODO*///	offset += osd_fread(w->file, buf, 4);
/*TODO*///	if( offset < 4 )
/*TODO*///	{
/*TODO*///		logerror("WAVE read error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///	}
/*TODO*///	if( memcmp (&buf[0], "RIFF", 4) != 0 )
/*TODO*///	{
/*TODO*///		logerror("WAVE header not 'RIFF'\n");
/*TODO*///		return WAVE_FMT;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* get the total size */
/*TODO*///	offset += osd_fread(w->file, &temp32, 4);
/*TODO*///	if( offset < 8 )
/*TODO*///	{
/*TODO*///		logerror("WAVE read error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///	}
/*TODO*///	filesize = intelLong(temp32);
/*TODO*///	logerror("WAVE filesize %u bytes\n", filesize);
/*TODO*///
/*TODO*///	/* read the RIFF file type and make sure it's a WAVE file */
/*TODO*///	offset += osd_fread(w->file, buf, 4);
/*TODO*///	if( offset < 12 )
/*TODO*///	{
/*TODO*///		logerror("WAVE read error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///	}
/*TODO*///	if( memcmp (&buf[0], "WAVE", 4) != 0 )
/*TODO*///	{
/*TODO*///		logerror("WAVE RIFF type not 'WAVE'\n");
/*TODO*///		return WAVE_FMT;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* seek until we find a format tag */
/*TODO*///	while( 1 )
/*TODO*///	{
/*TODO*///		offset += osd_fread(w->file, buf, 4);
/*TODO*///		offset += osd_fread(w->file, &temp32, 4);
/*TODO*///		w->length = intelLong(temp32);
/*TODO*///		if( memcmp(&buf[0], "fmt ", 4) == 0 )
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* seek to the next block */
/*TODO*///		osd_fseek(w->file, w->length, SEEK_CUR);
/*TODO*///		offset += w->length;
/*TODO*///		if( offset >= filesize )
/*TODO*///		{
/*TODO*///			logerror("WAVE no 'fmt ' tag found\n");
/*TODO*///			return WAVE_ERR;
/*TODO*///        }
/*TODO*///	}
/*TODO*///
/*TODO*///	/* read the format -- make sure it is PCM */
/*TODO*///	offset += osd_fread_lsbfirst(w->file, &temp16, 2);
/*TODO*///	if( temp16 != 1 )
/*TODO*///	{
/*TODO*///		logerror("WAVE format %d not supported (not = 1 PCM)\n", temp16);
/*TODO*///			return WAVE_ERR;
/*TODO*///    }
/*TODO*///	logerror("WAVE format %d (PCM)\n", temp16);
/*TODO*///
/*TODO*///	/* number of channels -- only mono is supported */
/*TODO*///	offset += osd_fread_lsbfirst(w->file, &channels, 2);
/*TODO*///	if( channels != 1 && channels != 2 )
/*TODO*///	{
/*TODO*///		logerror("WAVE channels %d not supported (only 1 mono or 2 stereo)\n", channels);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///	logerror("WAVE channels %d\n", channels);
/*TODO*///
/*TODO*///	/* sample rate */
/*TODO*///	offset += osd_fread(w->file, &temp32, 4);
/*TODO*///	w->smpfreq = intelLong(temp32);
/*TODO*///	logerror("WAVE sample rate %d Hz\n", w->smpfreq);
/*TODO*///
/*TODO*///	/* bytes/second and block alignment are ignored */
/*TODO*///	offset += osd_fread(w->file, buf, 6);
/*TODO*///
/*TODO*///	/* bits/sample */
/*TODO*///	offset += osd_fread_lsbfirst(w->file, &bits, 2);
/*TODO*///	if( bits != 8 && bits != 16 )
/*TODO*///	{
/*TODO*///		logerror("WAVE %d bits/sample not supported (only 8/16)\n", bits);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///	logerror("WAVE bits/sample %d\n", bits);
/*TODO*///	w->resolution = bits;
/*TODO*///
/*TODO*///	/* seek past any extra data */
/*TODO*///	osd_fseek(w->file, w->length - 16, SEEK_CUR);
/*TODO*///	offset += w->length - 16;
/*TODO*///
/*TODO*///	/* seek until we find a data tag */
/*TODO*///	while( 1 )
/*TODO*///	{
/*TODO*///		offset += osd_fread(w->file, buf, 4);
/*TODO*///		offset += osd_fread(w->file, &temp32, 4);
/*TODO*///		w->length = intelLong(temp32);
/*TODO*///		if( memcmp(&buf[0], "data", 4) == 0 )
/*TODO*///			break;
/*TODO*///
/*TODO*///		/* seek to the next block */
/*TODO*///		osd_fseek(w->file, w->length, SEEK_CUR);
/*TODO*///		offset += w->length;
/*TODO*///		if( offset >= filesize )
/*TODO*///		{
/*TODO*///			logerror("WAVE not 'data' tag found\n");
/*TODO*///			return WAVE_ERR;
/*TODO*///        }
/*TODO*///	}
/*TODO*///
/*TODO*///	/* allocate the game sample */
/*TODO*///	w->data = malloc(w->length);
/*TODO*///
/*TODO*///	if( w->data == NULL )
/*TODO*///	{
/*TODO*///		logerror("WAVE failed to malloc %d bytes\n", w->length);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* read the data in */
/*TODO*///	if( w->resolution == 8 )
/*TODO*///	{
/*TODO*///		if( osd_fread(w->file, w->data, w->length) != w->length )
/*TODO*///		{
/*TODO*///			logerror("WAVE failed read %d data bytes\n", w->length);
/*TODO*///			free(w->data);
/*TODO*///			return WAVE_ERR;
/*TODO*///		}
/*TODO*///		if( channels == 2 )
/*TODO*///		{
/*TODO*///			UINT8 *src = w->data;
/*TODO*///			INT8 *dst = w->data;
/*TODO*///			logerror("WAVE mixing 8-bit unsigned stereo to 8-bit signed mono\n");
/*TODO*///            /* convert stereo 8-bit data to mono signed samples */
/*TODO*///			for( temp32 = 0; temp32 < w->length/2; temp32++ )
/*TODO*///			{
/*TODO*///				*dst = ((src[0] + src[1]) / 2) ^ 0x80;
/*TODO*///				dst += 1;
/*TODO*///				src += 2;
/*TODO*///			}
/*TODO*///			w->length /= 2;
/*TODO*///            w->data = realloc(w->data, w->length);
/*TODO*///			if( w->data == NULL )
/*TODO*///			{
/*TODO*///				logerror("WAVE failed to malloc %d bytes\n", w->length);
/*TODO*///				return WAVE_ERR;
/*TODO*///			}
/*TODO*///        }
/*TODO*///		else
/*TODO*///		{
/*TODO*///			UINT8 *src = w->data;
/*TODO*///			INT8 *dst = w->data;
/*TODO*///            logerror("WAVE converting 8-bit unsigned to 8-bit signed\n");
/*TODO*///            /* convert 8-bit data to signed samples */
/*TODO*///			for( temp32 = 0; temp32 < w->length; temp32++ )
/*TODO*///				*dst++ = *src++ ^ 0x80;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* 16-bit data is fine as-is */
/*TODO*///		if( osd_fread_lsbfirst(w->file, w->data, w->length) != w->length )
/*TODO*///		{
/*TODO*///			logerror("WAVE failed read %d data bytes\n", w->length);
/*TODO*///			free(w->data);
/*TODO*///			return WAVE_ERR;
/*TODO*///        }
/*TODO*///        if( channels == 2 )
/*TODO*///        {
/*TODO*///			INT16 *src = w->data;
/*TODO*///			INT16 *dst = w->data;
/*TODO*///            logerror("WAVE mixing 16-bit stereo to 16-bit mono\n");
/*TODO*///            /* convert stereo 16-bit data to mono */
/*TODO*///			for( temp32 = 0; temp32 < w->length/2; temp32++ )
/*TODO*///			{
/*TODO*///				*dst = ((INT32)src[0] + (INT32)src[1]) / 2;
/*TODO*///				dst += 1;
/*TODO*///				src += 2;
/*TODO*///			}
/*TODO*///			w->length /= 2;
/*TODO*///			w->data = realloc(w->data, w->length);
/*TODO*///			if( w->data == NULL )
/*TODO*///			{
/*TODO*///				logerror("WAVE failed to malloc %d bytes\n", w->length);
/*TODO*///				return WAVE_ERR;
/*TODO*///            }
/*TODO*///        }
/*TODO*///		else
/*TODO*///		{
/*TODO*///			logerror("WAVE using 16-bit signed samples as is\n");
/*TODO*///        }
/*TODO*///	}
/*TODO*///	w->samples = w->length * 8 / w->resolution;
/*TODO*///	logerror("WAVE %d samples - %d:%02d\n", w->samples, (w->samples/w->smpfreq)/60, (w->samples/w->smpfreq)%60);
/*TODO*///
/*TODO*///	return WAVE_OK;
/*TODO*///}
/*TODO*///
/*TODO*///static int wave_write(int id)
/*TODO*///{
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	UINT32 filesize, offset = 0, temp32;
/*TODO*///	UINT16 temp16;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///        return WAVE_ERR;
/*TODO*///
/*TODO*///	while( w->play_pos < w->samples )
/*TODO*///    {
/*TODO*///		*((INT16 *)w->data + w->play_pos) = 0;
/*TODO*///		w->play_pos++;
/*TODO*///	}
/*TODO*///
/*TODO*///    filesize =
/*TODO*///		4 + 	/* 'RIFF' */
/*TODO*///		4 + 	/* size of entire file */
/*TODO*///		8 + 	/* 'WAVEfmt ' */
/*TODO*///		20 +	/* WAVE tag  (including size -- 0x10 in dword) */
/*TODO*///		4 + 	/* 'data' */
/*TODO*///		4 + 	/* size of data */
/*TODO*///		w->length;
/*TODO*///
/*TODO*///    /* write the core header for a WAVE file */
/*TODO*///	offset += osd_fwrite(w->file, "RIFF", 4);
/*TODO*///    if( offset < 4 )
/*TODO*///    {
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	temp32 = intelLong(filesize) - 8;
/*TODO*///	offset += osd_fwrite(w->file, &temp32, 4);
/*TODO*///
/*TODO*///	/* read the RIFF file type and make sure it's a WAVE file */
/*TODO*///	offset += osd_fwrite(w->file, "WAVE", 4);
/*TODO*///	if( offset < 12 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* write a format tag */
/*TODO*///	offset += osd_fwrite(w->file, "fmt ", 4);
/*TODO*///    if( offset < 12 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///    /* size of the following 'fmt ' fields */
/*TODO*///    offset += osd_fwrite(w->file, "\x10\x00\x00\x00", 4);
/*TODO*///	if( offset < 16 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* format: PCM */
/*TODO*///	temp16 = 1;
/*TODO*///	offset += osd_fwrite_lsbfirst(w->file, &temp16, 2);
/*TODO*///	if( offset < 18 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* channels: 1 (mono) */
/*TODO*///	temp16 = 1;
/*TODO*///    offset += osd_fwrite_lsbfirst(w->file, &temp16, 2);
/*TODO*///	if( offset < 20 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* sample rate */
/*TODO*///	temp32 = intelLong(w->smpfreq);
/*TODO*///	offset += osd_fwrite(w->file, &temp32, 4);
/*TODO*///	if( offset < 24 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* byte rate */
/*TODO*///	temp32 = intelLong(w->smpfreq * w->resolution / 8);
/*TODO*///	offset += osd_fwrite(w->file, &temp32, 4);
/*TODO*///	if( offset < 28 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* block align (size of one `sample') */
/*TODO*///	temp16 = w->resolution / 8;
/*TODO*///	offset += osd_fwrite_lsbfirst(w->file, &temp16, 2);
/*TODO*///	if( offset < 30 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* block align */
/*TODO*///	temp16 = w->resolution;
/*TODO*///	offset += osd_fwrite_lsbfirst(w->file, &temp16, 2);
/*TODO*///	if( offset < 32 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* 'data' tag */
/*TODO*///	offset += osd_fwrite(w->file, "data", 4);
/*TODO*///	if( offset < 36 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	/* data size */
/*TODO*///	temp32 = intelLong(w->length);
/*TODO*///	offset += osd_fwrite(w->file, &temp32, 4);
/*TODO*///	if( offset < 40 )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	if( osd_fwrite_lsbfirst(w->file, w->data, w->length) != w->length )
/*TODO*///	{
/*TODO*///		logerror("WAVE write error at offs %d\n", offset);
/*TODO*///		return WAVE_ERR;
/*TODO*///    }
/*TODO*///
/*TODO*///	return WAVE_OK;
/*TODO*///}
/*TODO*///
/*TODO*///static void wave_display(int id)
/*TODO*///{
/*TODO*///	static int tape_pos = 0;
/*TODO*///    struct wave_file *w = &wave[id];
/*TODO*///
/*TODO*///	if( abs(w->play_pos - tape_pos) > w->smpfreq / 4 )
/*TODO*///	{
/*TODO*///        char buf[32];
/*TODO*///		int x, y, n, t0, t1;
/*TODO*///
/*TODO*///        x = Machine->uixmin + id * Machine->uifontwidth * 16 + 1;
/*TODO*///		y = Machine->uiymin + Machine->uiheight - 9;
/*TODO*///		n = (w->play_pos * 4 / w->smpfreq) & 3;
/*TODO*///		t0 = w->play_pos / w->smpfreq;
/*TODO*///		t1 = w->samples / w->smpfreq;
/*TODO*///		sprintf(buf, "%c%c %2d:%02d [%2d:%02d]", n*2+2,n*2+3, t0/60,t0%60, t1/60,t1%60);
/*TODO*///		ui_text(Machine->scrbitmap,buf, x, y);
/*TODO*///		tape_pos = w->play_pos;
/*TODO*///    }
/*TODO*///}
/*TODO*///
    public static StreamInitPtr wave_sound_update = new StreamInitPtr() {
        public void handler(int param, ShortPtr buffer, int length) {
            /*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	int pos = w->play_pos;
/*TODO*///	int count = w->counter;
/*TODO*///	INT16 sample = w->play_sample;
/*TODO*///
/*TODO*///	if( !w->timer || (w->status & WAVE_STATUS_MUTED) )
/*TODO*///	{
/*TODO*///		while( length-- > 0 )
/*TODO*///			*buffer++ = sample;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///    while (length--)
/*TODO*///	{
/*TODO*///		count -= w->smpfreq;
/*TODO*///		while (count <= 0)
/*TODO*///		{
/*TODO*///			count += Machine->sample_rate;
/*TODO*///			if (w->resolution == 16)
/*TODO*///				sample = *((INT16 *)w->data + pos);
/*TODO*///			else
/*TODO*///				sample = *((INT8 *)w->data + pos)*256;
/*TODO*///			if (++pos >= w->samples)
/*TODO*///			{
/*TODO*///				pos = w->samples - 1;
/*TODO*///				if (pos < 0)
/*TODO*///					pos = 0;
/*TODO*///			}
/*TODO*///        }
/*TODO*///		*buffer++ = sample;
/*TODO*///	}
/*TODO*///	w->counter = count;
/*TODO*///	w->play_pos = pos;
/*TODO*///	w->play_sample = sample;
/*TODO*///		
/*TODO*///	if( w->display )
/*TODO*///		wave_display(id);
        }
    };

    /**
     * ***************************************************************************
     * WaveSound interface
     * ***************************************************************************
     */
    @Override
    public int start(MachineSound msound) {
        int i;

        intf = (Wave_interface) msound.sound_interface;

        for (i = 0; i < intf.num; i++) {
            //struct wave_file *w = &wave[i];
            wave[i] = new struct_wave_file();
            wave[i].channel = -1; //channel inits to -1
            String buf = "";

            if (intf.num > 1) {
                buf = sprintf("Cassette #%d", i + 1);
            } else {
                buf += "Cassette";
            }

            wave[i].channel = stream_init(buf, intf.mixing_level[i], Machine.sample_rate, i, wave_sound_update);

            if (wave[i].channel == -1) {
                return 1;
            }
        }

        return 0;
    }

    @Override
    public void stop() {
        int i;

        for (i = 0; i < intf.num; i++) {
            wave[i].channel = -1;
        }
    }

    @Override
    public void update() {

        int i;

        for (i = 0; i < intf.num; i++) {
            if (wave[i].channel != -1) {
                stream_update(wave[i].channel, 0);
            }
        }
    }

    @Override
    public void reset() {
        //no action required
    }

    /**
     * ***************************************************************************
     * IODevice interface functions
     * ***************************************************************************
     */

    /*
     * return info about a wave device
     */
    public static io_infoPtr wave_info = new io_infoPtr() {
        public void handler(int id, int whatinfo) {
            //return NULL;
        }
    };
    /*TODO*////*
/*TODO*/// * You can use this default handler if you don't want
/*TODO*/// * to support your own file types with the fill_wave()
/*TODO*/// * extension
/*TODO*/// */
/*TODO*///int wave_init(int id, const char *name)
/*TODO*///{
/*TODO*///	void *file;
/*TODO*///	if( !name || strlen(name) == 0 )
/*TODO*///		return INIT_OK;
/*TODO*///	file = osd_fopen(Machine->gamedrv->name, name, OSD_FILETYPE_IMAGE_RW, OSD_FOPEN_READ);
/*TODO*///	if( file )
/*TODO*///	{
/*TODO*///		struct wave_args wa = {0,};
/*TODO*///		wa.file = file;
/*TODO*///		wa.display = 1;
/*TODO*///		if( device_open(IO_CASSETTE,id,0,&wa) )
/*TODO*///			return INIT_FAILED;
/*TODO*///		return INIT_OK;
/*TODO*///    }
/*TODO*///	return INIT_FAILED;
/*TODO*///}
/*TODO*///
/*TODO*///void wave_exit(int id)
/*TODO*///{
/*TODO*///	wave_close(id);
/*TODO*///}
/*TODO*///
/*TODO*///int wave_status(int id, int newstatus)
/*TODO*///{
/*TODO*///	/* wave status has the following bitfields:
/*TODO*///	 *
/*TODO*///	 *  Bit 2:  Inhibit Motor (1=inhibit 0=noinhibit)
/*TODO*///	 *	Bit 1:	Mute (1=mute 0=nomute)
/*TODO*///	 *	Bit 0:	Motor (1=on 0=off)
/*TODO*///	 *
/*TODO*///	 *  Bit 0 is usually set by the tape control, and bit 2 is usually set by
/*TODO*///	 *  the driver
/*TODO*///	 *
/*TODO*///	 *	Also, you can pass -1 to have it simply return the status
/*TODO*///	 */
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return 0;
/*TODO*///
/*TODO*///    if( newstatus != -1 )
/*TODO*///	{
/*TODO*///		w->status = newstatus;
/*TODO*///
/*TODO*///		if (newstatus & WAVE_STATUS_MOTOR_INHIBIT)
/*TODO*///			newstatus = 0;
/*TODO*///		else
/*TODO*///			newstatus &= WAVE_STATUS_MOTOR_ENABLE;
/*TODO*///
/*TODO*///		if( newstatus && !w->timer )
/*TODO*///		{
/*TODO*///			w->timer = timer_set(TIME_NEVER, 0, NULL);
/*TODO*///		}
/*TODO*///		else
/*TODO*///		if( !newstatus && w->timer )
/*TODO*///		{
/*TODO*///			if( w->timer )
/*TODO*///				w->offset += (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///			timer_remove(w->timer);
/*TODO*///			w->timer = NULL;
/*TODO*///			schedule_full_refresh();
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return (w->timer ? WAVE_STATUS_MOTOR_ENABLE : 0) |
/*TODO*///		(w->status & WAVE_STATUS_MOTOR_INHIBIT ? w->status : w->status & ~WAVE_STATUS_MOTOR_ENABLE);
/*TODO*///}
/*TODO*///
    public static io_openPtr wave_open = new io_openPtr() {

        public int handler(int id, int mode, Object args) {
            System.out.println("Unimplemented wave_open function");//TODO REMOVE IT
            /*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///    struct wave_args *wa = args;
/*TODO*///	int result;
/*TODO*///
/*TODO*///    /* wave already opened? */
/*TODO*///	if( w->file )
/*TODO*///		wave_close(id);
/*TODO*///
/*TODO*///    w->file = wa->file;
/*TODO*///	w->mode = mode;
/*TODO*///	w->fill_wave = wa->fill_wave;
/*TODO*///	w->smpfreq = wa->smpfreq;
/*TODO*///	w->display = wa->display;
/*TODO*///
/*TODO*///	if( w->mode )
/*TODO*///	{
/*TODO*///        w->resolution = 16;
/*TODO*///		w->samples = w->smpfreq;
/*TODO*///		w->length = w->samples * w->resolution / 8;
/*TODO*///		w->data = malloc(w->length);
/*TODO*///		if( !w->data )
/*TODO*///		{
/*TODO*///			logerror("WAVE malloc(%d) failed\n", w->length);
/*TODO*///			memset(w, 0, sizeof(struct wave_file));
/*TODO*///			return WAVE_ERR;
/*TODO*///		}
/*TODO*///		return INIT_OK;
/*TODO*///    }
/*TODO*///	else
/*TODO*///	{
/*TODO*///		result = wave_read(id);
/*TODO*///		if( result == WAVE_OK )
/*TODO*///		{
/*TODO*///			/* return sample frequency in the user supplied structure */
/*TODO*///			wa->smpfreq = w->smpfreq;
/*TODO*///			w->offset = 0;
/*TODO*///			return INIT_OK;
/*TODO*///		}
/*TODO*///
/*TODO*///		if( result == WAVE_FMT )
/*TODO*///		{
/*TODO*///			UINT8 *data;
/*TODO*///			int bytes, pos, length;
/*TODO*///
/*TODO*///			/* User supplied fill_wave function? */
/*TODO*///			if( w->fill_wave == NULL )
/*TODO*///			{
/*TODO*///				logerror("WAVE no fill_wave callback, failing now\n");
/*TODO*///				return WAVE_ERR;
/*TODO*///			}
/*TODO*///
/*TODO*///			logerror("WAVE creating wave using fill_wave() callback\n");
/*TODO*///
/*TODO*///			/* sanity check: default chunk size is one byte */
/*TODO*///			if( wa->chunk_size == 0 )
/*TODO*///			{
/*TODO*///				wa->chunk_size = 1;
/*TODO*///				logerror("WAVE chunk_size defaults to %d\n", wa->chunk_size);
/*TODO*///			}
/*TODO*///			if( wa->smpfreq == 0 )
/*TODO*///			{
/*TODO*///				wa->smpfreq = 11025;
/*TODO*///				logerror("WAVE smpfreq defaults to %d\n", w->smpfreq);
/*TODO*///			}
/*TODO*///
/*TODO*///			/* allocate a buffer for the binary data */
/*TODO*///			data = malloc(wa->chunk_size);
/*TODO*///			if( !data )
/*TODO*///			{
/*TODO*///				free(w->data);
/*TODO*///				/* zap the wave structure */
/*TODO*///				memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///				return WAVE_ERR;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* determine number of samples */
/*TODO*///			length =
/*TODO*///				wa->header_samples +
/*TODO*///				((osd_fsize(w->file) + wa->chunk_size - 1) / wa->chunk_size) * wa->chunk_samples +
/*TODO*///				wa->trailer_samples;
/*TODO*///
/*TODO*///			w->smpfreq = wa->smpfreq;
/*TODO*///			w->resolution = 16;
/*TODO*///			w->samples = length;
/*TODO*///			w->length = length * 2;   /* 16 bits per sample */
/*TODO*///
/*TODO*///			w->data = malloc(w->length);
/*TODO*///			if( !w->data )
/*TODO*///			{
/*TODO*///				logerror("WAVE failed to malloc %d bytes\n", w->length);
/*TODO*///				/* zap the wave structure */
/*TODO*///				memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///				return WAVE_ERR;
/*TODO*///			}
/*TODO*///			logerror("WAVE creating max %d:%02d samples (%d) at %d Hz\n", (w->samples/w->smpfreq)/60, (w->samples/w->smpfreq)%60, w->samples, w->smpfreq);
/*TODO*///
/*TODO*///			pos = 0;
/*TODO*///			/* if there has to be a header */
/*TODO*///			if( wa->header_samples > 0 )
/*TODO*///			{
/*TODO*///				length = (*w->fill_wave)((INT16 *)w->data + pos, w->samples - pos, CODE_HEADER);
/*TODO*///				if( length < 0 )
/*TODO*///				{
/*TODO*///					logerror("WAVE conversion aborted at header\n");
/*TODO*///					free(w->data);
/*TODO*///					/* zap the wave structure */
/*TODO*///					memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///					return WAVE_ERR;
/*TODO*///				}
/*TODO*///				logerror("WAVE header %d samples\n", length);
/*TODO*///				pos += length;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* convert the file data to samples */
/*TODO*///			bytes = 0;
/*TODO*///			osd_fseek(w->file, 0, SEEK_SET);
/*TODO*///			while( pos < w->samples )
/*TODO*///			{
/*TODO*///				length = osd_fread(w->file, data, wa->chunk_size);
/*TODO*///				if( length == 0 )
/*TODO*///					break;
/*TODO*///				bytes += length;
/*TODO*///				length = (*w->fill_wave)((INT16 *)w->data + pos, w->samples - pos, data);
/*TODO*///				if( length < 0 )
/*TODO*///				{
/*TODO*///					logerror("WAVE conversion aborted at %d bytes (%d samples)\n", bytes, pos);
/*TODO*///					free(w->data);
/*TODO*///					/* zap the wave structure */
/*TODO*///					memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///					return WAVE_ERR;
/*TODO*///				}
/*TODO*///				pos += length;
/*TODO*///			}
/*TODO*///			logerror("WAVE converted %d data bytes to %d samples\n", bytes, pos);
/*TODO*///
/*TODO*///			/* if there has to be a trailer */
/*TODO*///			if( wa->trailer_samples )
/*TODO*///			{
/*TODO*///				if( pos < w->samples )
/*TODO*///				{
/*TODO*///					length = (*w->fill_wave)((INT16 *)w->data + pos, w->samples - pos, CODE_TRAILER);
/*TODO*///					if( length < 0 )
/*TODO*///					{
/*TODO*///						logerror("WAVE conversion aborted at trailer\n");
/*TODO*///						free(w->data);
/*TODO*///						/* zap the wave structure */
/*TODO*///						memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///						return WAVE_ERR;
/*TODO*///					}
/*TODO*///					logerror("WAVE trailer %d samples\n", length);
/*TODO*///					pos += length;
/*TODO*///				}
/*TODO*///			}
/*TODO*///
/*TODO*///			if( pos < w->samples )
/*TODO*///			{
/*TODO*///				/* what did the fill_wave() calls really fill into the buffer? */
/*TODO*///				w->samples = pos;
/*TODO*///				w->length = pos * 2;   /* 16 bits per sample */
/*TODO*///				w->data = realloc(w->data, w->length);
/*TODO*///				/* failure in the last step? how sad... */
/*TODO*///				if( !w->data )
/*TODO*///				{
/*TODO*///					logerror("WAVE realloc(%d) failed\n", w->length);
/*TODO*///					/* zap the wave structure */
/*TODO*///					memset(&wave[id], 0, sizeof(struct wave_file));
/*TODO*///					return WAVE_ERR;
/*TODO*///				}
/*TODO*///			}
/*TODO*///			logerror("WAVE %d samples - %d:%02d\n", w->samples, (w->samples/w->smpfreq)/60, (w->samples/w->smpfreq)%60);
/*TODO*///			/* hooray! :-) */
/*TODO*///			return INIT_OK;
/*TODO*///		}
/*TODO*///	}
            return WAVE_ERR;
        }
    };
    public static io_closePtr wave_close = new io_closePtr() {

        public void handler(int id) {
            System.out.println("Unimplemented wave_close function!");//TODO REMOVE
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///
/*TODO*///    if( !w->file )
/*TODO*///		return;
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///	{
/*TODO*///		if( w->channel != -1 )
/*TODO*///			stream_update(w->channel, 0);
/*TODO*///		w->samples = w->play_pos;
/*TODO*///		w->length = w->samples * w->resolution / 8;
/*TODO*///		timer_remove(w->timer);
/*TODO*///		w->timer = NULL;
/*TODO*///	}
/*TODO*///
/*TODO*///    if( w->mode )
/*TODO*///	{
/*TODO*///		wave_output(id,0);
/*TODO*///		wave_write(id);
/*TODO*///		w->mode = 0;
/*TODO*///	}
/*TODO*///
/*TODO*///    if( w->data )
/*TODO*///		free(w->data);
/*TODO*///    w->data = NULL;
/*TODO*///
/*TODO*///	if (w->file) {
/*TODO*///		osd_fclose(w->file);
/*TODO*///		w->file = NULL;
/*TODO*///	}
/*TODO*///	w->offset = 0;
/*TODO*///	w->play_pos = 0;
/*TODO*///	w->record_pos = 0;
/*TODO*///	w->counter = 0;
/*TODO*///	w->smpfreq = 0;
/*TODO*///	w->resolution = 0;
/*TODO*///	w->samples = 0;
/*TODO*///	w->length = 0;
        }
    };
    public static io_seekPtr wave_seek = new io_seekPtr() {
        public int handler(int id, int offset, int whence) {
            System.out.println("Unimplemented wave_seek function");//TODO remove
            return 0; // Dummy to be removed
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///    UINT32 pos = 0;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return pos;
/*TODO*///
/*TODO*///    switch( whence )
/*TODO*///	{
/*TODO*///	case SEEK_SET:
/*TODO*///		w->offset = offset;
/*TODO*///		break;
/*TODO*///	case SEEK_END:
/*TODO*///		w->offset = w->samples - 1;
/*TODO*///		break;
/*TODO*///	case SEEK_CUR:
/*TODO*///		if( w->timer )
/*TODO*///			pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///		w->offset = pos + offset;
/*TODO*///		if( w->offset < 0 )
/*TODO*///			w->offset = 0;
/*TODO*///		if( w->offset >= w->length )
/*TODO*///			w->offset = w->length - 1;
/*TODO*///	}
/*TODO*///	w->play_pos = w->record_pos = w->offset;
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///	{
/*TODO*///		timer_remove(w->timer);
/*TODO*///		w->timer = timer_set(TIME_NEVER, 0, NULL);
/*TODO*///	}
/*TODO*///
/*TODO*///    return w->offset;
        }
    };
    public static io_tellPtr wave_tell = new io_tellPtr() {
        public int handler(int id) {
            System.out.println("Unimplemented wave_tell function");//TODO Remove
            return 0;//todo remove!!
            /*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///    UINT32 pos = 0;
/*TODO*///	if( w->timer )
/*TODO*///		pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///	if( pos >= w->samples )
/*TODO*///		pos = w->samples -1;
/*TODO*///    return pos;
        }
    };
    public static io_inputPtr wave_input = new io_inputPtr() {
        public int handler(int id) {
            System.out.println("Unimplemented wave_input function");//TODO Remove
            return 0;//todo remove!!
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	UINT32 pos = 0;
/*TODO*///    int level = 0;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return level;
/*TODO*///
/*TODO*///    if( w->channel != -1 )
/*TODO*///		stream_update(w->channel, 0);
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///	{
/*TODO*///		pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///		if( pos >= w->samples )
/*TODO*///			pos = w->samples - 1;
/*TODO*///		if( pos >= 0 )
/*TODO*///		{
/*TODO*///			if( w->resolution == 16 )
/*TODO*///				level = *((INT16 *)w->data + pos);
/*TODO*///			else
/*TODO*///				level = 256 * *((INT8 *)w->data + pos);
/*TODO*///		}
/*TODO*///    }
/*TODO*///	if( w->display )
/*TODO*///		wave_display(id);
/*TODO*///    return level;
        }
    };
    public static io_outputPtr wave_output = new io_outputPtr() {
        public void handler(int id, int data) {
            System.out.println("Unimplemented wave_output function");//TODO Remove
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	UINT32 pos = 0;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return;
/*TODO*///
/*TODO*///    if( !w->mode )
/*TODO*///		return;
/*TODO*///
/*TODO*///	if( data == w->record_sample )
/*TODO*///		return;
/*TODO*///
/*TODO*///	if( w->channel != -1 )
/*TODO*///		stream_update(w->channel, 0);
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///    {
/*TODO*///		pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///		if( pos >= w->samples )
/*TODO*///        {
/*TODO*///			/* add at least one second of data */
/*TODO*///			if( pos - w->samples < w->smpfreq )
/*TODO*///				w->samples += w->smpfreq;
/*TODO*///			else
/*TODO*///				w->samples = pos;	/* more than one second */
/*TODO*///            w->length = w->samples * w->resolution / 8;
/*TODO*///            w->data = realloc(w->data, w->length);
/*TODO*///            if( !w->data )
/*TODO*///            {
/*TODO*///                logerror("WAVE realloc(%d) failed\n", w->length);
/*TODO*///                memset(w, 0, sizeof(struct wave_file));
/*TODO*///                return;
/*TODO*///            }
/*TODO*///        }
/*TODO*///		while( w->record_pos < pos )
/*TODO*///        {
/*TODO*///			if( w->resolution == 16 )
/*TODO*///				*((INT16 *)w->data + w->record_pos) = w->record_sample;
/*TODO*///			else
/*TODO*///				*((INT8 *)w->data + w->record_pos) = w->record_sample / 256;
/*TODO*///			w->record_pos++;
/*TODO*///        }
/*TODO*///    }
/*TODO*///
/*TODO*///    if( w->display )
/*TODO*///        wave_display(id);
/*TODO*///
/*TODO*///    w->record_sample = data;
        }
    };
    public static io_input_chunkPtr wave_input_chunk = new io_input_chunkPtr() {
        public int handler(int id, Object dst, int chunks) {
            System.out.println("Unimplemented wave_input_chunk function");//TODO Remove
            return 0;//todo remove!!
/*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	UINT32 pos = 0;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return 0;
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///	{
/*TODO*///		pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///		if( pos >= w->samples )
/*TODO*///			pos = w->samples - 1;
/*TODO*///	}
/*TODO*///
/*TODO*///    if( pos + count >= w->samples )
/*TODO*///		count = w->samples - pos - 1;
/*TODO*///
/*TODO*///    if( count > 0 )
/*TODO*///	{
/*TODO*///		if( w->resolution == 16 )
/*TODO*///			memcpy(dst, (INT16 *)w->data + pos, count * sizeof(INT16));
/*TODO*///		else
/*TODO*///			memcpy(dst, (INT8 *)w->data + pos, count * sizeof(INT8));
/*TODO*///	}
/*TODO*///
/*TODO*///    return count;
        }
    };
    public static io_output_chunkPtr wave_output_chunk = new io_output_chunkPtr() {
        public void handler(int id, Object dst, int chunks) {
            System.out.println("Unimplemented wave_output_chunk function");//TODO Remove
            /*TODO*///	struct wave_file *w = &wave[id];
/*TODO*///	UINT32 pos = 0;
/*TODO*///
/*TODO*///	if( !w->file )
/*TODO*///		return 0;
/*TODO*///
/*TODO*///    if( w->timer )
/*TODO*///	{
/*TODO*///		pos = w->offset + (timer_timeelapsed(w->timer) * w->smpfreq + 0.5);
/*TODO*///		if( pos >= w->length )
/*TODO*///			pos = w->length - 1;
/*TODO*///	}
/*TODO*///
/*TODO*///    if( pos + count >= w->length )
/*TODO*///	{
/*TODO*///		/* add space for new data */
/*TODO*///		w->samples += count - pos;
/*TODO*///		w->length = w->samples * w->resolution / 8;
/*TODO*///		w->data = realloc(w->data, w->length);
/*TODO*///		if( !w->data )
/*TODO*///		{
/*TODO*///			logerror("WAVE realloc(%d) failed\n", w->length);
/*TODO*///			memset(w, 0, sizeof(struct wave_file));
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///    if( count > 0 )
/*TODO*///	{
/*TODO*///		if( w->resolution == 16 )
/*TODO*///			memcpy((INT16 *)w->data + pos, src, count * sizeof(INT16));
/*TODO*///		else
/*TODO*///			memcpy((INT8 *)w->data + pos, src, count * sizeof(INT8));
/*TODO*///	}
/*TODO*///
/*TODO*///    return count;
        }
    };
}
