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
package WIP.arcadeflex;

import static WIP.arcadeflex.libc_v2.*;
import static WIP.mame.sndintrfH.*;
import static WIP.mame.osdependH.*;

public class fucPtr {

    public static abstract interface ReadHandlerPtr {

        public abstract int handler(int offset);
    }

    public static abstract interface WriteHandlerPtr {

        public abstract void handler(int offset, int data);
    }

    public static abstract interface InitMachinePtr {

        public abstract void handler();
    }

    public static abstract interface InitDriverPtr {

        public abstract void handler();
    }

    public static abstract interface InterruptPtr {

        public abstract int handler();
    }

    public static abstract interface VhConvertColorPromPtr {

        public abstract void handler(char[] palette, char[] colortable, UBytePtr color_prom);
    }

    public static abstract interface VhEofCallbackPtr {

        public abstract void handler();
    }

    public static abstract interface VhStartPtr {

        public abstract int handler();
    }

    public static abstract interface VhStopPtr {

        public abstract void handler();
    }

    public static abstract interface VhUpdatePtr {

        public abstract void handler(osd_bitmap bitmap, int full_refresh);
    }

    public static abstract interface ShStartPtr {

        public abstract int handler(MachineSound msound);
    }

    public static abstract interface ShStopPtr {

        public abstract void handler();
    }

    public static abstract interface ShUpdatePtr {

        public abstract void handler();
    }

    public static abstract interface RomLoadPtr {

        public abstract void handler();
    }

    public static abstract interface InputPortPtr {

        public abstract void handler();
    }

    public static abstract interface nvramPtr {

        public abstract void handler(Object file, int read_or_write);
    };

    public static abstract interface WriteYmHandlerPtr {

        public abstract void handler(int linestate);
    }
}
