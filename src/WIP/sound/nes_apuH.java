/*
 * ported to v0.37b7
 * modifications for consoleflex
 */
package WIP.sound;

import static WIP.arcadeflex.fucPtr.*;

public class nes_apuH {

    public static final int MAX_NESPSG = 2;

    /* AN EXPLANATION
     *
     * The NES APU is actually integrated into the Nintendo processor.
     * You must supply the same number of APUs as you do processors.
     * Also make sure to correspond the memory regions to those used in the
     * processor, as each is shared.
     */
    public static class NESinterface {

        public NESinterface(int num, int[] region, int[] volume,int baseclock, WriteHandlerPtr[] apu_callback_w, ReadHandlerPtr[] apu_callback_r) {
            this.num = num;
            this.region = region;
            this.volume = volume;
            this.baseclock=baseclock;
            this.apu_callback_r = apu_callback_r;
            this.apu_callback_w = apu_callback_w;
        }
        public int num;
        /* total number of chips in the machine */

        public int[] region;//[MAX_NESPSG];  /* DMC regions */
        public int[] volume;//[MAX_NESPSG];
        public int baseclock;
        public WriteHandlerPtr[] apu_callback_w;//[MAX_NESPSG]; /* LBO */
        public ReadHandlerPtr[] apu_callback_r;//[MAX_NESPSG]; /* LBO */
    };
}
