/*
 * ported to v0.37b7
 * 
 */
package sound;

import WIP.arcadeflex.libc_v2.*;

public class YM_DELTAT {

    public YM_DELTAT() {
        u8_reg = new int[16];
    }
    public UBytePtr memory;
    public int memory_size;
    public double freqbase;
    public int[] output_pointer;
    /* pointer of output pointers */
    public int output_range;

    public /*UINT8*/ int[] u8_reg;
    public /*UINT8*/ int u8_portstate, u8_portcontrol;
    public int portshift;

    public /*UINT8*/ int u8_flag;
    /* port state        */
    public /*UINT8*/ int u8_flagMask;
    /* arrived flag mask */
    public /*UINT8*/ int u8_now_data;
    public /*UINT32*/ long u32_now_addr;
    public /*UINT32*/ long u32_now_step;
    public /*UINT32*/ long u32_step;
    public /*UINT32*/ long u32_start;
    public /*UINT32*/ long u32_end;
    public /*UINT32*/ long u32_delta;
    public int volume;
    public IntSubArray pan;
    /* &output_pointer[pan] */
    public int /*adpcmm,*/ adpcmx, adpcmd;
    public int adpcml;
    /* hiro-shi!! */

 /* leveling and re-sampling state for DELTA-T */
    public int volume_w_step;
    /* volume with step rate */
    public int next_leveling;
    /* leveling value        */
    public int sample_step;
    /* step of re-sampling   */

    public /*UINT8*/ int u8_arrivedFlag;
    /* flag of arrived end address */
}
