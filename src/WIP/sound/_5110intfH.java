/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */
 /*
 * ported to v0.37b7
 *
 */
package WIP.sound;

import static WIP.sound.tms5110H.M0_callbackPtr;

public class _5110intfH {

    public static abstract interface IrqPtr {

        public abstract void handler(int state);
    }

    public static class TMS5110interface {

        public TMS5110interface(int baseclock, int mixing_level, IrqPtr irq, M0_callbackPtr M0_callback) {
            this.baseclock = baseclock;
            this.mixing_level = mixing_level;
            this.irq = irq;
            this.M0_callback = M0_callback;
        }

        int baseclock;
        /* clock rate = 80 * output sample rate,     */
 /* usually 640000 for 8000 Hz sample rate or */
 /* usually 800000 for 10000 Hz sample rate.  */
        int mixing_level;
        IrqPtr irq;
        /* IRQ callback function */
        M0_callbackPtr M0_callback;
        /* function to be called when chip requests another bit*/
    }

}
