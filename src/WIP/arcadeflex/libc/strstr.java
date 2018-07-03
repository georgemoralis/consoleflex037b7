/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WIP.arcadeflex.libc;

/**
 *
 * const char * strstr ( const char * str1, const char * str2 ); char * strstr
 * (char * str1, const char * str2 );
 *
 * Locate substring Returns a pointer to the first occurrence of str2 in str1,
 * or a null pointer if str2 is not part of str1.
 *
 * The matching process does not include the terminating null-characters, but it
 * stops there.
 */
public class strstr {

    public static String strstr(String str1, String str2) {
        int found = str1.indexOf(str2);
        if (found == -1)//not found
        {
            return null;
        } else {
            return str1.substring(found, str1.length());
        }
    }
}
