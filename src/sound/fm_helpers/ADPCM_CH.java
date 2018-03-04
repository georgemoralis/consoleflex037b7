package sound.fm_helpers;
/*
 * ported to v0.37b7
 *
 */
import WIP.arcadeflex.libc_v2.IntSubArray;

public class ADPCM_CH {

    public ADPCM_CH() {

    }
    public int/*UINT8*/ u8_flag;
    /* port state        */
    public int/*UINT8*/ u8_flagMask;
    /* arrived flag mask */
    public int/*UINT8*/ u8_now_data;
    public long/*UINT32*/ u32_now_addr;
    public long/*UINT32*/ u32_now_step;
    public long/*UINT32*/ u32_step;
    public long/*UINT32*/ u32_start;
    public long/*UINT32*/ u32_end;
    public int IL;
    public int volume;
    /* calcrated mixing level */
    public IntSubArray pan;
    /* &out_ch[OPN_xxxx] */
    public int /*adpcmm,*/ adpcmx, adpcmd;
    public int adpcml;
    /* hiro-shi!! */
}
