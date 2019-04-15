//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

// package com.woosim.printer;
package io.github.edufolly.flutterbluetoothserial;

import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class WoosimCmd {
    private static final String TAG = "WoosimCmd";
    private static final boolean D = false;
    private static final byte ESC = 27;
    private static final byte FS = 28;
    private static final byte GS = 29;
    public static final int MCU_M16C = 1;
    public static final int MCU_ARM = 2;
    public static final int MCU_RX = 3;
    public static final int CUT_FULL = 0;
    public static final int CUT_PARTIAL = 1;
    public static final int CT_CP437 = 0;
    public static final int CT_KATAKANA = 1;
    public static final int CT_CP850 = 2;
    public static final int CT_CP860 = 3;
    public static final int CT_CP863 = 4;
    public static final int CT_CP865 = 5;
    public static final int CT_CP852 = 6;
    public static final int CT_CP857 = 7;
    public static final int CT_CP737 = 8;
    public static final int CT_CP866 = 9;
    public static final int CT_CP862 = 10;
    public static final int CT_CP775 = 11;
    public static final int CT_POLISH = 12;
    public static final int CT_ISO8859_15 = 13;
    public static final int CT_WIN1252 = 14;
    public static final int CT_CP858 = 15;
    public static final int CT_CP855 = 16;
    public static final int CT_WIN1251 = 17;
    public static final int CT_WIN1250 = 18;
    public static final int CT_WIN1253 = 19;
    public static final int CT_WIN1254 = 20;
    public static final int CT_WIN1255 = 21;
    public static final int CT_WIN1258 = 22;
    public static final int CT_WIN1257 = 23;
    public static final int CT_AZERBAIJANI = 24;
    public static final int CT_WIN874 = 30;
    public static final int CT_CP720 = 40;
    public static final int CT_IRAN_SYSTEM = 41;
    public static final int CT_WIN1256 = 41;
    public static final int CT_ARABIC_FARSI = 42;
    public static final int CT_ARABIC_FORMS_B = 43;
    public static final int CT_HINDI_DEVANAGARI = 50;
    public static final int CT_DBCS = 255;
    public static final int FONT_LARGE = 0;
    public static final int FONT_MEDIUM = 1;
    public static final int FONT_SMALL = 2;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    public WoosimCmd() {
    }

    public static byte[] initPrinter() {
        return new byte[]{27, 64};
    }

    public static byte[] printData() {
        return new byte[]{10};
    }

    public static byte[] printDotFeed(int var0) {
        return new byte[]{27, 74, (byte)var0};
    }

    public static byte[] printLineFeed(int var0) {
        return new byte[]{27, 100, (byte)var0};
    }

    public static byte[] setPageMode() {
        return new byte[]{27, 76};
    }

    public static byte[] cutPaper(int var0) {
        if (var0 >= 0 && var0 <= 1) {
            return new byte[]{29, 86, (byte)var0};
        } else {
            Log.e("WoosimCmd", "Invalid paper cutting mode: " + var0);
            return null;
        }
    }

    public static byte[] setPositionFromMark(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{27, 80, var1, var2};
    }

    public static byte[] feedToMark() {
        return new byte[]{27, 122, 27, 121};
    }

    public static byte[] openCashDrawer() {
        return new byte[]{27, 112, 0, 50, 50};
    }

    public static byte[] queryStatus() {
        return new byte[]{27, 118};
    }

    public static byte[] setMacro(byte[] var0) {
        byte[] var1 = new byte[var0.length + 7];
        var1[0] = 27;
        var1[1] = 28;
        var1[2] = 43;
        var1[3] = 1;
        var1[4] = 0;
        var1[5] = (byte)(var0.length & 255);
        var1[6] = (byte)(var0.length >> 8 & 255);
        System.arraycopy(var0, 0, var1, 7, var0.length);
        return var1;
    }

    public static byte[] resetMacro() {
        return new byte[]{27, 28, 43, 1, 0, 0, 0};
    }

    public static byte[] queryModelName() {
        return new byte[]{27, 89, -1};
    }

    public static byte[] queryDeviceVersion() {
        return new byte[]{27, 0, 1};
    }

    public static byte[] MSR_1stTrackMode() {
        return new byte[]{27, 77, 67};
    }

    public static byte[] MSR_2ndTrackMode() {
        return new byte[]{27, 77, 68};
    }

    public static byte[] MSR_3rdTrackMode() {
        return new byte[]{27, 77, 71};
    }

    public static byte[] MSR_doubleTrackMode() {
        return new byte[]{27, 77, 69};
    }

    public static byte[] MSR_tripleTrackMode() {
        return new byte[]{27, 77, 70};
    }

    public static byte[] MSR_exit() {
        return new byte[]{4};
    }

    public static byte[] SMSR_enter() {
        return new byte[]{27, 77, 67};
    }

    public static byte[] SMSR_exit() {
        return new byte[]{27, 77, 88};
    }

    public static byte[] SMSR_writeData(byte[] var0, int var1) {
        byte var2 = (byte)(var1 & 255);
        byte var3 = (byte)(var1 >> 8 & 255);
        byte[] var4 = new byte[var1 + 5];
        var4[0] = 27;
        var4[1] = 77;
        var4[2] = 83;
        var4[3] = var3;
        var4[4] = var2;
        System.arraycopy(var0, 0, var4, 5, var1);
        return var4;
    }

    public static byte[] SCR_enter() {
        return new byte[]{27, 78};
    }

    public static byte[] SCR_exit() {
        return new byte[]{126, 4, 126};
    }

    public static byte[] SCR_enterNonSecureMode() {
        return new byte[]{27, 110, 0};
    }

    public static byte[] SCR_exitNonSecureMode() {
        return new byte[]{27, 110, 27};
    }

    public static byte[] SCR_enterPaymentMode() {
        return new byte[]{27, 108, 0};
    }

    public static byte[] SCR_checkIntegrity() {
        return new byte[]{27, 108, 1};
    }

    public static byte[] SCR_enterKeyDownloadMode() {
        return new byte[]{27, 108, 2};
    }

    public static byte[] SCR_checkReaderStatus() {
        return new byte[]{27, 108, 3};
    }

    public static byte[] SCR_checkKeyValidation() {
        return new byte[]{27, 108, 4};
    }

    public static byte[] setCodeTable(int var0, int var1, int var2) {
        int var3 = var1;
        int var4 = var2;
        if (var0 >= 1 && var0 <= 3) {
            if ((var1 < 0 || var1 > 50) && var1 != 255) {
                Log.e("WoosimCmd", "Invalid code table: " + var1);
                return null;
            } else {
                if (var2 < 0 || var2 > 2) {
                    var4 = 0;
                }

                if (var0 == 1 || var0 == 2) {
                    if (var1 > 3 && var1 != 13 && var1 != 12 && var1 != 255) {
                        Log.e("WoosimCmd", "Invalid code table for specific MCU: " + var1);
                        return null;
                    }

                    if (var1 == 13) {
                        var3 = 4;
                    }

                    if (var1 == 12) {
                        var3 = 5;
                    }

                    if (var2 == 2) {
                        var4 = 1;
                    }
                }

                return new byte[]{27, 116, (byte)var3, 27, 33, (byte)var4};
            }
        } else {
            Log.e("WoosimCmd", "Invalid MCU type: " + var0);
            return null;
        }
    }

    public static byte[] setTextStyle(boolean var0, boolean var1, boolean var2, int var3, int var4) {
        int var5 = var3 - 1;
        int var6 = var4 - 1;
        if (var5 < 0) {
            var5 = 0;
        }

        if (var5 > 7) {
            var5 = 7;
        }

        if (var6 < 0) {
            var6 = 0;
        }

        if (var6 > 7) {
            var6 = 7;
        }

        byte var7 = (byte)(var5 & 15 | var6 << 4 & 240);
        return new byte[]{27, 69, (byte)(var0 ? 1 : 0), 27, 45, (byte)(var1 ? 1 : 0), 29, 66, (byte)(var2 ? 1 : 0), 29, 33, var7};
    }

    public static byte[] setCharacterSpace(int var0) {
        return new byte[]{27, 32, (byte)var0};
    }

    public static byte[] setLineSpace(int var0) {
        return new byte[]{27, 51, (byte)var0};
    }

    public static byte[] resetLineSpace() {
        return new byte[]{27, 50};
    }

    public static byte[] setUpsideDown(boolean var0) {
        int var1 = var0 ? 1 : 0;
        return new byte[]{27, 123, (byte)var1};
    }

    public static byte[] setTextAlign(int var0) {
        return new byte[]{27, 97, (byte)var0};
    }

    public static byte[] setAlignment(int var0) {
        switch(var0) {
        case 0:
            return new byte[]{27, 124, 108, 65};
        case 1:
            return new byte[]{27, 124, 99, 65};
        case 2:
            return new byte[]{27, 124, 114, 65};
        default:
            return null;
        }
    }

    public static byte[] moveAbsPosition(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{27, 36, var1, var2};
    }

    public static byte[] moveRelPosition(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{27, 92, var1, var2};
    }

    public static byte[] setTabPosition(int[] var0) {
        int var1 = var0.length;
        if (var1 > 32) {
            var1 = 32;
        }

        byte var2 = 0;
        byte[] var3 = new byte[var1 + 3];
        int var5 = var2 + 1;
        var3[var2] = 27;
        var3[var5++] = 68;

        for(int var4 = 0; var4 < var1; ++var4) {
            var3[var5++] = (byte)var0[var4];
        }

        var3[var5] = 0;
        return var3;
    }

    public static byte[] setLeftMargin(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{29, 76, var1, var2};
    }

    public static byte[] setPrintingWidth(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{29, 87, var1, var2};
    }

    public static byte[] selectTTF(String var0) {
        byte[] var1 = new byte[]{27, 103, 70};
        ByteBuffer var2 = ByteBuffer.allocate(var1.length + var0.getBytes().length + 1);
        var2.put(var1);
        var2.put(var0.getBytes());
        var2.put((byte)0);
        return var2.array();
    }

    public static byte[] getTTFcode(int var0, int var1, String var2) {
        byte[] var3 = new byte[]{27, 103, 85};
        byte[] var4 = null;

        try {
            var4 = var2.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException var6) {
            var6.printStackTrace();
        }

        if (var4 == null) {
            return null;
        } else {
            ByteBuffer var5 = ByteBuffer.allocate(var3.length + var4.length + 4);
            var5.put(var3);
            if (var0 > 255) {
                var5.put((byte)-1);
            } else {
                var5.put((byte)var0);
            }

            if (var1 > 255) {
                var5.put((byte)-1);
            } else {
                var5.put((byte)var1);
            }

            var5.put(var4);
            var5.put((byte)0);
            var5.put((byte)0);
            return var5.array();
        }
    }

    // public static byte[] getTTFcodeArabic(int var0, int var1, String var2) {
    //     byte[] var3 = new byte[]{27, 103, 85};
    //     byte[] var4 = WoosimArabic.encodeTTF(var2);
    //     ByteBuffer var5 = ByteBuffer.allocate(var3.length + var4.length + 4);
    //     var5.put(var3);
    //     if (var0 > 255) {
    //         var5.put((byte)-1);
    //     } else {
    //         var5.put((byte)var0);
    //     }

    //     if (var1 > 255) {
    //         var5.put((byte)-1);
    //     } else {
    //         var5.put((byte)var1);
    //     }

    //     var5.put(var4);
    //     var5.put((byte)0);
    //     var5.put((byte)0);
    //     return var5.array();
    // }

    public static byte[] queryTTFWidth(int var0, int var1, String var2) {
        byte[] var3 = new byte[]{27, 103, 78, 85};
        byte[] var4 = null;

        try {
            var4 = var2.getBytes("UTF-16BE");
        } catch (UnsupportedEncodingException var6) {
            var6.printStackTrace();
        }

        if (var4 == null) {
            return null;
        } else {
            ByteBuffer var5 = ByteBuffer.allocate(var3.length + var4.length + 4);
            var5.put(var3);
            if (var0 > 255) {
                var5.put((byte)-1);
            } else {
                var5.put((byte)var0);
            }

            if (var1 > 255) {
                var5.put((byte)-1);
            } else {
                var5.put((byte)var1);
            }

            var5.put(var4);
            var5.put((byte)0);
            var5.put((byte)0);
            return var5.array();
        }
    }

    public static byte[] PM_setStdMode() {
        return new byte[]{27, 83};
    }

    public static byte[] PM_printData() {
        return new byte[]{27, 12};
    }

    public static byte[] PM_printStdMode() {
        return new byte[]{12};
    }

    public static byte[] PM_deleteData() {
        return new byte[]{24};
    }

    public static byte[] PM_setArea(int var0, int var1, int var2, int var3) {
        if (var2 > 0 && var3 > 0) {
            byte var4 = (byte)(var0 & 255);
            byte var5 = (byte)(var0 >> 8 & 255);
            byte var6 = (byte)(var1 & 255);
            byte var7 = (byte)(var1 >> 8 & 255);
            byte var8 = (byte)(var2 & 255);
            byte var9 = (byte)(var2 >> 8 & 255);
            byte var10 = (byte)(var3 & 255);
            byte var11 = (byte)(var3 >> 8 & 255);
            return new byte[]{27, 87, var4, var5, var6, var7, var8, var9, var10, var11};
        } else {
            Log.e("WoosimCmd", "Invalid area");
            return null;
        }
    }

    public static byte[] PM_setDirection(int var0) {
        if (var0 >= 0 && var0 <= 3) {
            return new byte[]{27, 84, (byte)var0};
        } else {
            Log.e("WoosimCmd", "Invalid direction");
            return null;
        }
    }

    public static byte[] PM_setPosition(int var0, int var1) {
        byte var2 = (byte)(var0 & 255);
        byte var3 = (byte)(var0 >> 8 & 255);
        byte var4 = (byte)(var1 & 255);
        byte var5 = (byte)(var1 >> 8 & 255);
        return new byte[]{27, 79, var2, var3, var4, var5};
    }

    public static byte[] PM_moveAbsVertical(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{29, 36, var1, var2};
    }

    public static byte[] PM_moveRelVertical(int var0) {
        byte var1 = (byte)(var0 & 255);
        byte var2 = (byte)(var0 >> 8 & 255);
        return new byte[]{29, 92, var1, var2};
    }

    // public static byte[] Crypto_saveBasekey(int var0, byte[] var1) {
    //     return WoosimCryptography.saveBasekey(var0, var1);
    // }

    // public static byte[] Crypto_saveBasekeyList(int var0, byte[] var1) {
    //     return WoosimCryptography.saveBasekeyList(var0, var1);
    // }

    // public static byte[] Crypto_setBasekeyIndex(int var0) {
    //     return WoosimCryptography.setBasekeyIndex(var0);
    // }

    // public static byte[] Crypto_queryBasekeyIndex() {
    //     return WoosimCryptography.queryBasekeyIndex();
    // }

    // public static byte[] Crypto_setEncryptionMode(int var0) {
    //     return WoosimCryptography.setEncryptionMode(var0);
    // }

    // public static byte[] EMSR_1stTrackMode() {
    //     return WoosimCryptography.MSR_1stTrackMode();
    // }

    // public static byte[] EMSR_2ndTrackMode() {
    //     return WoosimCryptography.MSR_2ndTrackMode();
    // }

    // public static byte[] EMSR_3rdTrackMode() {
    //     return WoosimCryptography.MSR_3rdTrackMode();
    // }

    // public static byte[] EMSR_doubleTrackMode() {
    //     return WoosimCryptography.MSR_doubleTrackMode();
    // }

    // public static byte[] EMSR_tripleTrackMode() {
    //     return WoosimCryptography.MSR_tripleTrackMode();
    // }

    // public static byte[] EMSR_JISMode() {
    //     return WoosimCryptography.MSR_JISMode();
    // }
}
