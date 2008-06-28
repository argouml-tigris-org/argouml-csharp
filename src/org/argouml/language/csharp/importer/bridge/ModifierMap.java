package org.argouml.language.csharp.importer.bridge;

/**
 * Created by IntelliJ IDEA.
 * User: THILINAH
 * Date: Jun 23, 2008
 * Time: 9:22:46 AM
 */
public class ModifierMap {
//    public static final short ACC_PUBLIC    = 0x0001;
//    public static final short ACC_PRIVATE   = 0x0002;
//    public static final short ACC_PROTECTED = 0x0004;
//    public static final short ACC_STATIC    = 0x0008;
//    public static final short ACC_FINAL     = 0x0010;
//    public static final short ACC_SUPER     = 0x0020;
//    public static final short ACC_VOLATILE  = 0x0040;
//    public static final short ACC_TRANSIENT = 0x0080;
//    public static final short ACC_NATIVE    = 0x0100;
//    public static final short ACC_INTERFACE = 0x0200;
//    public static final short ACC_ABSTRACT  = 0x0400;
//
//
//   public static long Empty		= 0x0000000;
//        public static long New			= 0x0000001;
//        public static long Public		= 0x0000002;
//        public static long Protected	= 0x0000004;
//        public static long Internal	= 0x0000008;
//        public static long Private		= 0x0000010;
//        public static long Abstract	= 0x0000020;
//        public static long Sealed		= 0x0000040;
//        public static long Partial	= 0x0000080;
//
//        public static long Static		= 0x0000100;
//        public static long Virtual		= 0x0000200;
//        public static long Override	= 0x0000400;
//        public static long Extern		= 0x0000800;
//        public static long Readonly	= 0x0001000;
//        public static long Volatile	= 0x0002000;
//
//        public static long Ref			= 0x0008000;
//        public static long Out			= 0x0010000;
//        public static long Params		= 0x0020000;
//
//        public static long Assembly	= 0x0040000;
//        public static long Field		= 0x0080000;
//        public static long Event		= 0x0100000;
//        public static long Method		= 0x0200000;
//        public static long Param		= 0x0400000;
//        public static long Property	= 0x0800000;
//        public static long Return		= 0x1000000;
//        public static long Type		= 0x2000000;
//        public static long Module		= 0x4000000;

    public static short getUmlModifierForVisibility(long mod){
        short smod=0x0000;
        if((mod & 0x0000002) >0){
            smod += 0x0001;
        }
        else if((mod & 0x0000010)>0){
            smod += 0x0002;
        }
        else if((mod & 0x0000004)>0){
            smod += 0x0004;
        }
        else if((mod &0x0000100)>0){
            smod += 0x0008;
        }
        else if((mod & 0x0000040)>0){
            smod += 0x0010;
        }
        else{
            smod += 0x0000;
        }

        return smod;

    }
}
