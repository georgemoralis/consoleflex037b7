

package old.cpu.hd6309;

import old.cpu.m6809.m6809;
import static old.mame.cpuintrf.*;
import static old.mame.cpuintrfH.*;
import static old.mame.driverH.*;
import static WIP.mame.memoryH.*;
import static old.cpu.m6809.m6809H.*;
import static WIP.mame.memory.*;
import static WIP.mame.mame.*;

public class hd6309 extends m6809
{
    public hd6309()
    {
        cpu_num = CPU_HD6309;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = HD6309_INT_NONE;
        irq_int = HD6309_INT_IRQ;
        nmi_int = HD6309_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6809_ICount;
    } 
    public String cpu_info(Object context, int regnum) {
        switch( regnum )
        {
            case CPU_INFO_NAME: return "HD6309";
        }
        return super.cpu_info(context, regnum);
    }
}
