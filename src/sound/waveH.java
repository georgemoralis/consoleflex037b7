/**
 * ported to v0.37b7
 *
 */
package sound;

public class waveH {

    public static final int MAX_WAVE = 4;

    /**
     * ***************************************************************************
     * CassetteWave interface
     * ***************************************************************************
     */
    public static class Wave_interface {

        public Wave_interface(int num, int[] mixing_level) {
            this.num = num;
            this.mixing_level = mixing_level;
        }

        public int num;
        public int[] mixing_level;//[MAX_WAVE];
    };

    /*TODO*////*****************************************************************************
/*TODO*/// * functions for the IODevice entry IO_CASSETTE. Example for the macro
/*TODO*/// * IO_CASSETTE_WAVE(1,"wav\0cas\0",mycas_id,mycas_init,mycas_exit)
/*TODO*/// *****************************************************************************/
/*TODO*///#define IO_CASSETTE_WAVE(count,fileext,id,init,exit)	\
/*TODO*///{														\
/*TODO*///	IO_CASSETTE,		/* type */						\
/*TODO*///	count,				/* count */ 					\
/*TODO*///	fileext,			/* file extensions */			\
/*TODO*///	IO_RESET_NONE,		/* reset depth */				\
/*TODO*///	id, 				/* id */						\
/*TODO*///	init,				/* init */						\
/*TODO*///	exit,				/* exit */						\
/*TODO*///	wave_info,			/* info */						\
/*TODO*///	wave_open,			/* open */						\
/*TODO*///	wave_close, 		/* close */ 					\
/*TODO*///	wave_status,		/* status */					\
/*TODO*///	wave_seek,			/* seek */						\
/*TODO*///	wave_tell,			/* tell */						\
/*TODO*///	wave_input, 		/* input */ 					\
/*TODO*///	wave_output,		/* output */					\
/*TODO*///	wave_input_chunk,	/* input_chunk */				\
/*TODO*///	wave_output_chunk	/* output_chunk */				\
/*TODO*///}
/*TODO*///
/*TODO*////*****************************************************************************
/*TODO*/// * Use this structure for the "void *args" argument of device_open()
/*TODO*/// * file
/*TODO*/// *	  file handle returned by osd_fopen() (mandatory)
/*TODO*/// * display
/*TODO*/// *	  display cassette icon, playing time and total time on screen
/*TODO*/// * fill_wave
/*TODO*/// *	  callback to fill in samples (optional)
/*TODO*/// * smpfreq
/*TODO*/// *	  sample frequency when the wave is generated (optional)
/*TODO*/// *	  used for fill_wave() and for writing (creating) wave files
/*TODO*/// * header_samples
/*TODO*/// *	  number of samples for a cassette header (optional)
/*TODO*/// * trailer_samples
/*TODO*/// *	  number of samples for a cassette trailer (optional)
/*TODO*/// * chunk_size
/*TODO*/// *	  number of bytes to convert at once (optional)
/*TODO*/// * chunk_samples
/*TODO*/// *	  number of samples produced for a data chunk (optional)
/*TODO*/// *****************************************************************************/
    public static class wave_args {

        public wave_args(Object file) {
            this.file = file;
        }

        public Object file;
        public int display;
        /*TODO*///	int (*fill_wave)(INT16 *buffer, int length, UINT8 *bytes);
        public int smpfreq;
        /*TODO*///    int header_samples;
/*TODO*///    int trailer_samples;
/*TODO*///    int chunk_size;
/*TODO*///	int chunk_samples;
    };
    /*TODO*///
/*TODO*////*****************************************************************************
/*TODO*/// * Your (optional) fill_wave callback will be called with "UINT8 *bytes" set
/*TODO*/// * to one of these values if you should fill in the (optional) header or
/*TODO*/// * trailer samples into the buffer.
/*TODO*/// * Otherwise 'bytes' is a pointer to the chunk of data
/*TODO*/// *****************************************************************************/
/*TODO*///#define CODE_HEADER 	((UINT8*)-1)
/*TODO*///#define CODE_TRAILER	((UINT8*)-2)
/*TODO*///
    public static final int WAVE_STATUS_MOTOR_ENABLE = 1;
    public static final int WAVE_STATUS_MUTED = 2;
    public static final int WAVE_STATUS_MOTOR_INHIBIT = 4;

}
