/*
 *  this file should be fully compatible for 0.36
 */
package old.sound;

import WIP.mame.sndintrf.snd_interface;

import static old.arcadeflex.libc_old.sprintf;
import static WIP.arcadeflex.libc_v2.ShortPtr;
import static WIP.arcadeflex.fucPtr.WriteHandlerPtr;
import static WIP.mame.mame.Machine;
import static WIP.mame.sndintrfH.MachineSound;
import static WIP.mame.sndintrfH.SOUND_DAC;
import static old.sound.dacH.DACinterface;
import static old.sound.dacH.MAX_DAC;
import static old.sound.streams.*;
public class dac extends snd_interface {

    static int[] channel = new int[MAX_DAC];
    static int[] output = new int[MAX_DAC];
    static int[] UnsignedVolTable = new int[256];
    static int[] SignedVolTable = new int[256];

    public dac() {
        sound_num = SOUND_DAC;
        name = "DAC";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((DACinterface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;//NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void stop() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void update() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    @Override
    public void reset() {
        //NO FUNCTIONAL CODE IS NECCESARY
    }

    public static StreamInitPtr DAC_update = new StreamInitPtr() {
        public void handler(int num, ShortPtr buffer, int length) {
            int out = output[num];
            int bi = 0;
            while (length-- != 0) {
                buffer.write(bi++, (short) out);//while (length--) *(buffer++) = out;
            }
        }
    };

    public static void DAC_data_w(int num, int data) {
            int out = UnsignedVolTable[data];

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }


    public static void DAC_signed_data_w(int num, int data) {
            int out = SignedVolTable[data];

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }


    public static void DAC_data_16_w(int num, int data) {
            int out = data >> 1;		/* range      0..32767 */

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }


    public static void DAC_signed_data_16_w(int num, int data) {
            int out = data - 0x8000;	/* range -32768..32767 */

            if (output[num] != out) {
                /* update the output buffer before changing the registers */
                stream_update(channel[num], 0);
                output[num] = out;
            }
        }


    static void DAC_build_voltable() {
        int i;

        /* build volume table (linear) */
        for (i = 0; i < 256; i++) {
            UnsignedVolTable[i] = i * 0x101 / 2;	/* range      0..32767 */

            SignedVolTable[i] = i * 0x101 - 0x8000;	/* range -32768..32767 */

        }
    }

    @Override
    public int start(MachineSound msound) {
        int i;

        DACinterface intf = ((DACinterface) msound.sound_interface);

        DAC_build_voltable();

        for (i = 0; i < intf.num; i++) {
            String name;

            name = sprintf("DAC #%d", i);
            channel[i] = stream_init(name, intf.mixing_level[i], Machine.sample_rate, i, DAC_update);

            if (channel[i] == -1) {
                return 1;
            }

            output[i] = 0;
        }

        return 0;
    }
    public static WriteHandlerPtr DAC_0_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
    {
        DAC_data_w(0,data);
    } };

    public static WriteHandlerPtr DAC_1_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
    {
        DAC_data_w(1,data);
    } };

    public static WriteHandlerPtr DAC_0_signed_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
    {
        DAC_signed_data_w(0,data);
    } };

    public static WriteHandlerPtr DAC_1_signed_data_w = new WriteHandlerPtr() {public void handler(int offset, int data)
    {
        DAC_signed_data_w(1,data);
    } };
}
