/*
 *  Ported to 0.37b5
 */
package old.sound;


public class dacH {
    public static final int MAX_DAC = 4;

    public static class DACinterface {
        public DACinterface(int num, int[] mixing_level) {
            this.num = num;
            this.mixing_level = mixing_level;
        }

        public int num;	/* total number of DACs */
        public int[] mixing_level;//[MAX_DAC]
    }
}
