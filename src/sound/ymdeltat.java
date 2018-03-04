/*
 * ported to v0.37b7
 * 
 */
package sound;

import WIP.arcadeflex.libc_v2.IntSubArray;
import WIP.arcadeflex.libc_v2.UBytePtr;

public class ymdeltat {

    /*from ymdeltat.h*/
    public static final int YM_DELTAT_SHIFT = (16);

    public static void YM_DELTAT_DECODE_PRESET(YM_DELTAT DELTAT) {
        ym_deltat_memory = DELTAT.memory;
    }

    static UBytePtr ym_deltat_memory;
    /* memory pointer */

 /* Forecast to next Forecast (rate = *8) */
 /* 1/8 , 3/8 , 5/8 , 7/8 , 9/8 , 11/8 , 13/8 , 15/8 */
    static int ym_deltat_decode_tableB1[] = {
        1, 3, 5, 7, 9, 11, 13, 15,
        -1, -3, -5, -7, -9, -11, -13, -15,};
    /* delta to next delta (rate= *64) */
 /* 0.9 , 0.9 , 0.9 , 0.9 , 1.2 , 1.6 , 2.0 , 2.4 */
    static int ym_deltat_decode_tableB2[] = {
        57, 57, 57, 57, 77, 102, 128, 153,
        57, 57, 57, 57, 77, 102, 128, 153
    };

    /* DELTA-T-ADPCM write register */
    public static void YM_DELTAT_ADPCM_Write(YM_DELTAT DELTAT, int r, int v) {
        if (r >= 0x10) {
            return;
        }
        DELTAT.u8_reg[r] = v & 0xFF;
        /* stock data */

        switch (r) {
            case 0x00:
                /* START,REC,MEMDATA,REPEAT,SPOFF,--,--,RESET */
                if ((v & 0x80) != 0) {
                    DELTAT.u8_portstate = (v & 0x90) & 0xFF;
                    /* start req,memory mode,repeat flag copy */
                    /**
                     * ** start ADPCM ***
                     */
                    DELTAT.volume_w_step = (int) ((double) DELTAT.volume * DELTAT.u32_step / (1 << YM_DELTAT_SHIFT));
                    DELTAT.u32_now_addr = ((DELTAT.u32_start) << 1) & 0xFFFFFFFFL;
                    DELTAT.u32_now_step = ((1 << YM_DELTAT_SHIFT) - DELTAT.u32_step) & 0xFFFFFFFFL;
                    /*adpcm.adpcmm   = 0;*/
                    DELTAT.adpcmx = 0;
                    DELTAT.adpcml = 0;
                    DELTAT.adpcmd = YM_DELTAT_DELTA_DEF;
                    DELTAT.next_leveling = 0;
                    DELTAT.u8_flag = 1;
                    /* start ADPCM */

                    if (DELTAT.u32_step == 0L) {
                        DELTAT.u8_flag = 0;
                        DELTAT.u8_portstate = 0x00;
                    }
                    /**
                     * ** PCM memory check & limit check ***
                     */
                    if (DELTAT.memory == null) {			// Check memory Mapped
                        //LOG(LOG_ERR,("YM Delta-T ADPCM rom not mapped\n"));
                        DELTAT.u8_flag = 0;
                        DELTAT.u8_portstate = 0x00;
                        //logerror("DELTAT memory 0\n");
                    } else {
                        if (DELTAT.u32_end >= DELTAT.memory_size) {		// Check End in Range
                            //LOG(LOG_ERR,("YM Delta-T ADPCM end out of range: $%08x\n",DELTAT.end));
                            DELTAT.u32_end = (DELTAT.memory_size - 1) & 0xFFFFFFFFL;
                            //logerror("DELTAT end over\n");
                        }
                        if (DELTAT.u32_start >= DELTAT.memory_size) {		// Check Start in Range
                            //LOG(LOG_ERR,("YM Delta-T ADPCM start out of range: $%08x\n",DELTAT.start));
                            DELTAT.u8_flag = 0;
                            DELTAT.u8_portstate = 0x00;
                            //logerror("DELTAT start under\n");
                        }
                    }
                } else if ((v & 0x01) != 0) {
                    DELTAT.u8_flag = 0;
                    DELTAT.u8_portstate = 0x00;
                }
                break;
            case 0x01:
                /* L,R,-,-,SAMPLE,DA/AD,RAMTYPE,ROM */
                DELTAT.u8_portcontrol = v & 0xff;
                DELTAT.pan = new IntSubArray(DELTAT.output_pointer, (v >> 6) & 0x03);//&DELTAT.output_pointer[(v>>6)&0x03];
                break;
            case 0x02:
            /* Start Address L */
            case 0x03:
                /* Start Address H */
                DELTAT.u32_start = ((DELTAT.u8_reg[0x3] * 0x0100 | DELTAT.u8_reg[0x2]) << DELTAT.portshift) & 0xFFFFFFFFL;
                break;
            case 0x04:
            /* Stop Address L */
            case 0x05:
                /* Stop Address H */
                DELTAT.u32_end = ((DELTAT.u8_reg[0x5] * 0x0100 | DELTAT.u8_reg[0x4]) << DELTAT.portshift) & 0xFFFFFFFFL;
                DELTAT.u32_end = (DELTAT.u32_end + (1 << DELTAT.portshift) - 1) & 0xFFFFFFFFL;
                break;
            case 0x06:
            /* Prescale L (PCM and Recoard frq) */
            case 0x07:
            /* Proscale H */
            case 0x08:
                /* ADPCM data */
                break;
            case 0x09:
            /* DELTA-N L (ADPCM Playback Prescaler) */
            case 0x0a:
                /* DELTA-N H */
                DELTAT.u32_delta = ((DELTAT.u8_reg[0xa] * 0x0100 | DELTAT.u8_reg[0x9])) & 0xFFFFFFFFL;
                DELTAT.u32_step = ((long) ((double) (DELTAT.u32_delta * (1 << (YM_DELTAT_SHIFT - 16))) * (DELTAT.freqbase))) & 0xFFFFFFFFL;
                DELTAT.volume_w_step = (int) ((double) DELTAT.volume * DELTAT.u32_step / (1 << YM_DELTAT_SHIFT));
                break;
            case 0x0b: /* Level control (volume , voltage flat) */ {
                int oldvol = DELTAT.volume;
                DELTAT.volume = (v & 0xff) * (DELTAT.output_range / 256) / YM_DELTAT_DECODE_RANGE;
                if (oldvol != 0) {
                    DELTAT.adpcml = (int) ((double) DELTAT.adpcml / (double) oldvol * (double) DELTAT.volume);
                    DELTAT.sample_step = (int) ((double) DELTAT.sample_step / (double) oldvol * (double) DELTAT.volume);
                }
                DELTAT.volume_w_step = (int) ((double) DELTAT.volume * (double) DELTAT.u32_step / (double) (1 << YM_DELTAT_SHIFT));
            }
            break;
        }
    }

    public static void YM_DELTAT_ADPCM_Reset(YM_DELTAT DELTAT, int pan) {
        DELTAT.u32_now_addr = 0L;
        DELTAT.u32_now_step = 0L;
        DELTAT.u32_step = 0L;
        DELTAT.u32_start = 0L;
        DELTAT.u32_end = 0L;
        /* F2610.adpcm[i].delta     = 21866; */
        DELTAT.volume = 0;
        DELTAT.pan = new IntSubArray(DELTAT.output_pointer, pan);
        /* DELTAT.flagMask  = 0; */
        DELTAT.u8_arrivedFlag = 0;
        DELTAT.u8_flag = 0;
        DELTAT.adpcmx = 0;
        DELTAT.adpcmd = 127;
        DELTAT.adpcml = 0;
        /*DELTAT.adpcmm    = 0;*/
        DELTAT.volume_w_step = 0;
        DELTAT.next_leveling = 0;
        DELTAT.u8_portstate = 0;
        /* DELTAT.portshift = 8; */
    }

    /* DELTA-T particle adjuster */
    public static final int YM_DELTAT_DELTA_MAX = (24576);
    public static final int YM_DELTAT_DELTA_MIN = (127);
    public static final int YM_DELTAT_DELTA_DEF = (127);

    public static final int YM_DELTAT_DECODE_RANGE = 32768;
    public static final int YM_DELTAT_DECODE_MIN = (-(YM_DELTAT_DECODE_RANGE));
    public static final int YM_DELTAT_DECODE_MAX = ((YM_DELTAT_DECODE_RANGE) - 1);

    /**
     * ** ADPCM B (Delta-T control type) ***
     */
    public static void YM_DELTAT_ADPCM_CALC(YM_DELTAT DELTAT) {
        long/*UINT32*/ u32_step;
        int data;
        int old_m;
        int now_leveling;
        int delta_next;

        DELTAT.u32_now_step = (DELTAT.u32_now_step + DELTAT.u32_step) & 0xFFFFFFFFL;
        if (DELTAT.u32_now_step >= (1 << YM_DELTAT_SHIFT)) {
            u32_step = (DELTAT.u32_now_step >> YM_DELTAT_SHIFT) & 0xFFFFFFFFL;
            DELTAT.u32_now_step = (DELTAT.u32_now_step & (1 << YM_DELTAT_SHIFT) - 1) & 0xFFFFFFFFL;
            do {
                if (DELTAT.u32_now_addr > (DELTAT.u32_end << 1)) {
                    if ((DELTAT.u8_portstate & 0x10) != 0) {
                        /**
                         * ** repeat start ***
                         */
                        DELTAT.u32_now_addr = (DELTAT.u32_start << 1) & 0xFFFFFFFFL;
                        DELTAT.adpcmx = 0;
                        DELTAT.adpcmd = YM_DELTAT_DELTA_DEF;
                        DELTAT.next_leveling = 0;
                        DELTAT.u8_flag = 1;
                    } else {
                        DELTAT.u8_arrivedFlag |= DELTAT.u8_flagMask;
                        DELTAT.u8_flag = 0;
                        DELTAT.adpcml = 0;
                        now_leveling = 0;
                        return;
                    }
                }
                if ((DELTAT.u32_now_addr & 1) != 0) {
                    data = DELTAT.u8_now_data & 0x0f;
                } else {
                    DELTAT.u8_now_data = ((ym_deltat_memory.read((int) (DELTAT.u32_now_addr >> 1)))) & 0xFF;
                    data = (DELTAT.u8_now_data >> 4) & 0xFF;
                }
                DELTAT.u32_now_addr = (DELTAT.u32_now_addr + 1) & 0xFFFFFFFFL;
                /* shift Measurement value */
                old_m = DELTAT.adpcmx/*adpcmm*/;
                /* ch.adpcmm = YM_DELTAT_Limit( ch.adpcmx + (decode_tableB3[data] * ch.adpcmd / 8) ,YM_DELTAT_DECODE_MAX, YM_DELTAT_DECODE_MIN ); */
 /* Forecast to next Forecast */
                DELTAT.adpcmx += (ym_deltat_decode_tableB1[data] * DELTAT.adpcmd / 8);
                if (DELTAT.adpcmx > YM_DELTAT_DECODE_MAX) {
                    DELTAT.adpcmx = YM_DELTAT_DECODE_MAX;
                } else if (DELTAT.adpcmx < YM_DELTAT_DECODE_MIN) {
                    DELTAT.adpcmx = YM_DELTAT_DECODE_MIN;
                }
                /* delta to next delta */
                DELTAT.adpcmd = (DELTAT.adpcmd * ym_deltat_decode_tableB2[data]) / 64;
                if (DELTAT.adpcmd > YM_DELTAT_DELTA_MAX) {
                    DELTAT.adpcmd = YM_DELTAT_DELTA_MAX;
                } else if (DELTAT.adpcmd < YM_DELTAT_DELTA_MIN) {
                    DELTAT.adpcmd = YM_DELTAT_DELTA_MIN;
                }
                /* shift leveling value */
                delta_next = DELTAT.adpcmx/*adpcmm*/ - old_m;
                now_leveling = DELTAT.next_leveling;
                DELTAT.next_leveling = old_m + (delta_next / 2);
            } while (--u32_step != 0);

            /* delta step of re-sampling */
            DELTAT.sample_step = (DELTAT.next_leveling - now_leveling) * DELTAT.volume_w_step;
            /* output of start point */
            DELTAT.adpcml = now_leveling * DELTAT.volume;
            /* adjust to now */
            DELTAT.adpcml += (int) ((double) DELTAT.sample_step * ((double) DELTAT.u32_now_step / (double) DELTAT.u32_step));
        }
        DELTAT.adpcml += DELTAT.sample_step;
        /* output for work of output channels (outd[OPNxxxx])*/
        DELTAT.pan.write(DELTAT.pan.read() + DELTAT.adpcml);
    }

}
