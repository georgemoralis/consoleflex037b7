/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consoleflex;

import static WIP.arcadeflex.libc_v2.ConvertArguments;
import static WIP.arcadeflex.libc_v2.argc;
import static WIP.arcadeflex.libc_v2.argv;
import old.arcadeflex.osdepend;

/**
 *
 * @author shadow-laptop
 */
public class mainapp {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ConvertArguments("consoleflex", args);
        args = null;
        System.exit(osdepend.main(argc, argv));
    }

}
