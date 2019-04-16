package io.github.edufolly.flutterbluetoothserial;

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.EventChannel.EventSink;
import io.flutter.plugin.common.EventChannel.StreamHandler;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;


public class FlutterBluetoothSerialPlugin implements MethodCallHandler,
RequestPermissionsResultListener {

    private static final String TAG = "FlutterBluePlugin";
    private static final String NAMESPACE = "flutter_bluetooth_serial";
    private static final int REQUEST_COARSE_LOCATION_PERMISSIONS = 1451;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static ConnectedThread THREAD = null;
    private final Registrar registrar;
    private BluetoothAdapter mBluetoothAdapter;

    private static double PosY = 0.0d;
    private static Bitmap b = Bitmap.createBitmap(832, 2000, Config.ARGB_8888);
    private static Canvas c = new Canvas(b);
    public static ArrayList<byte[]> printData = new ArrayList();
    public static NotificationManager NotificationManagerInstance = null;

    private Result pendingResult;

    private EventSink readSink;
    private EventSink statusSink;
    private Runnable doPrint;

    public static void registerWith(Registrar registrar) {
        final FlutterBluetoothSerialPlugin instance = new FlutterBluetoothSerialPlugin(registrar);
        registrar.addRequestPermissionsResultListener(instance);
    }

    FlutterBluetoothSerialPlugin(Registrar registrar) {
        this.registrar = registrar;
        MethodChannel channel = new MethodChannel(registrar.messenger(), NAMESPACE + "/methods");
        EventChannel stateChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/state");
        EventChannel readChannel = new EventChannel(registrar.messenger(), NAMESPACE + "/read");
        BluetoothManager mBluetoothManager = (BluetoothManager) registrar.activity()
        .getSystemService(Context.BLUETOOTH_SERVICE);
        assert mBluetoothManager != null;
        this.mBluetoothAdapter = mBluetoothManager.getAdapter();
        channel.setMethodCallHandler(this);
        stateChannel.setStreamHandler(stateStreamHandler);
        readChannel.setStreamHandler(readResultsHandler);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (mBluetoothAdapter == null && !"isAvailable".equals(call.method)) {
            result.error("bluetooth_unavailable", "the device does not have bluetooth", null);
            return;
        }

        final Map<String, Object> arguments = call.arguments();

        switch (call.method) {

            case "isAvailable":
            result.success(mBluetoothAdapter != null);
            break;
            
            case "deviceSerial":
            String serial =  GetDeviceSerialNo();
            result.success(serial);
            break;

            case "isOn":
            try {
                assert mBluetoothAdapter != null;
                result.success(mBluetoothAdapter.isEnabled());
            } catch (Exception ex) {
                result.error("Error", ex.getMessage(), exceptionToString(ex));
            }
            break;

            case "isConnected":
            result.success(THREAD != null);
            break;

            case "openSettings":
            ContextCompat.startActivity(registrar.activity(),
                new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS),
                null);
            result.success(true);
            break;

            case "getBondedDevices":
            try {

                if (ContextCompat.checkSelfPermission(registrar.activity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(registrar.activity(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION_PERMISSIONS);

                pendingResult = result;
                break;
            }

            getBondedDevices(result);

        } catch (Exception ex) {
            result.error("Error", ex.getMessage(), exceptionToString(ex));
        }

        break;

        case "connect":
        if (arguments.containsKey("address")) {
            String address = (String) arguments.get("address");
            connect(result, address);
        } else {
            result.error("invalid_argument", "argument 'address' not found", null);
        }
        break;

        case "disconnect":
        disconnect(result);
        break;

        case "write":
        if (arguments.containsKey("message")) {
            String message = (String) arguments.get("message");
            write(result, message);
        } else {
            result.error("invalid_argument", "argument 'message' not found", null);
        }
        break;

        case "writeBytes":
        if (arguments.containsKey("message")) {
            byte[] bytes = (byte[]) arguments.get("message");
            writeBytes(result, bytes);
        } else {
            result.error("invalid_argument", "argument 'bytes' not found", null);
        }
        break;

        case "testPrint":
        if (arguments.containsKey("message")) {
            String message = (String) arguments.get("message");
            testPrint(result, message);
        } else {
            result.error("invalid_argument", "argument 'message' not found", null);
        }
        break;

        case "printJob":
        // Notice notice = new Notice();
        if (arguments.containsKey("notice_no")) {
            // String message = (String) arguments.get("message");
            // print(arguments);
            // Notice notice = Notice();
            // notice.NoticeNo = arguments;
            printJob(result, arguments);
            // testPrint(result, message);
                    // printJob(result, message);
                    // printJob(result, message);
                    // testPrint(result, message);
        } else {
            result.error("invalid_argument", "argument 'message' not found", null);
        }
        break;

        default:
        result.notImplemented();
        break;
    }
}

    /**
     * @param requestCode  requestCode
     * @param permissions  permissions
     * @param grantResults grantResults
     * @return boolean
     */
    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {

        if (requestCode == REQUEST_COARSE_LOCATION_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getBondedDevices(pendingResult);
            } else {
                pendingResult.error("no_permissions",
                    "this plugin requires location permissions for scanning", null);
                pendingResult = null;
            }
            return true;
        }
        return false;
    }

    /**
     * @param result result
     */
    private void getBondedDevices(Result result) {

        List<Map<String, Object>> list = new ArrayList<>();

        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("address", device.getAddress());
            ret.put("name", device.getName());
            ret.put("type", device.getType());
            ret.put("status", "bonded");
            list.add(ret);
        }

        result.success(list);
    }

    public static String GetDeviceSerialNo()
    {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

        // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = null;
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = null;
        }

        return serialNumber.toUpperCase();
    }

    // private void getNotice(String[] args) {

    //     List<Map<String, Object>> list = new ArrayList<>();

    //     for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
    //         Map<String, Object> ret = new HashMap<>();
    //         ret.put("address", device.getAddress());
    //         ret.put("name", device.getName());
    //         ret.put("type", device.getType());
    //         ret.put("status", "bonded");
    //         list.add(ret);
    //     }

    //     result.success(list);
    // }

    private String exceptionToString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * @param result  result
     * @param address address
     */
    private void connect(Result result, String address) {

        if (THREAD != null) {
            result.error("connect_error", "already connected", null);
            return;
        }
        AsyncTask.execute(() -> {
            try {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                if (device == null) {
                    result.error("connect_error", "device not found", null);
                    return;
                }

                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);

                if (socket == null) {
                    result.error("connect_error", "socket connection not established", null);
                    return;
                }

                socket.connect();

                THREAD = new ConnectedThread(socket);
                THREAD.start();

                result.success(true);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                result.error("connect_error", ex.getMessage(), exceptionToString(ex));
            }
        });
    }

    /**
     * @param result result
     */
    private void disconnect(Result result) {

        if (THREAD == null) {
            result.error("disconnection_error", "not connected", null);
            return;
        }
        AsyncTask.execute(() -> {
            try {
                THREAD.cancel();
                THREAD = null;
                result.success(true);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage(), ex);
                result.error("disconnection_error", ex.getMessage(), exceptionToString(ex));
            }
        });
    }


    /**
     * @param result  result
     * @param message message
     */
    private void write(Result result, String message) {
        if (THREAD == null) {
            result.error("write_error", "not connected", null);
            return;
        }

        try {
            THREAD.write(message.getBytes());
            result.success(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            result.error("write_error", ex.getMessage(), exceptionToString(ex));
        }
    }

    private void writeBytes(Result result, byte[] bytes) {
        if (THREAD == null) {
            result.error("write_error", "not connected", null);
            return;
        }

        try {
            THREAD.write(bytes);

            result.success(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            result.error("write_error", ex.getMessage(), exceptionToString(ex));
        }
    }

    /*
     * START CUSTOM SECTION
     *
     *
     */
    // private static void InitPrinterTraffic() {
    //     Bitmap b = Bitmap.createBitmap(832, 1800, Config.ARGB_8888);
    //     // b = Bitmap.createBitmap(832, 100, Config.ARGB_8888);
    //     c = new Canvas(b);
    //     c.drawColor(Color.WHITE);
    //     ArrayList<byte[]> printData = new ArrayList<byte[]>();
    //     printData.add(PrinterCommands.INITIALIZE_PRINTER);
    //     PosY = 0.0d;
    // }


    private static void InitPrinterTraffic() {
        b = Bitmap.createBitmap(832, 1700, Bitmap.Config.ARGB_8888);
        c = new Canvas(b);

        c.drawColor(Color.WHITE);

        printData = new ArrayList<byte[]>();

        printData.add(PrinterCommands.INITIALIZE_PRINTER);

        PosY = 0;
    }

    private static void feedToBlack() {
        printData = new ArrayList<byte[]>();
        printData.add(PrinterCommands.PRINT_TO_BLACK_MARK);
    }


    private void printJob(Result result, Map notice) {
        String officerDetails = String.valueOf(notice.get("officer"));
        InitPrinterTraffic();
        // PrintText(PosX, IncY, FontType, FontSize, FontBold, strVariable, nLimit, RightJustified);

        //        // PrintBarCode(1.5d, 0.3d, Notice.NoticeSerialNo);
        PrintText(0.1d, 1.3d, "Arial", 9, false, "No. :", -1);
        PrintText(0.4d, 0.0d, "Arial", 9, true, String.valueOf(notice.get("noticeNo")), -1);
        // PrintText(0.95d, 0.2d, "Arial", 10, true, "NOTIS KESALAHAN SERTA TAWARAN KOMPAUN", -1);
        // PrintText(2.5d, 0.0d, "Arial", 9, false, "NO. KENDERAAN", -1);
        // PrintText(2.85d, 0.0d, "Arial", 9, true, " : " + Notice.VehicleNo, -1);
        PrintText(2.1d, 0.0d, "Arial", 9, false, "No Cukai Jalan", -1);
        PrintText(3.2d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("vehicleNo")), -1);
        // PrintText(4.0d, 0.0d, "Arial", 9, false, "NO. CUKAI JALAN", -1);
        // PrintText(2.85d, 0.0d, "Arial", 9, true, " : " + Notice.RoadTaxNo, -1);
        PrintText(0.1d, 0.25d, "Arial", 9, false, "JENAMA / MODEL", -1);
        String makeModel = String.valueOf(notice.get("vehicleMakeModel"));
        PrintText(0.95d, 0.0d, "Arial", 9, true, " : " + makeModel, -1);
        PrintText(0.1d, 0.15d, "Arial", 9, false, "WARNA", -1);
        PrintText(0.95d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("vehicleColor")), -1);
        PrintText(0.1d, 0.15d, "Arial", 9, false, "JENIS BADAN", -1);
        PrintText(0.95d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("vehicleType")), -1);
        PrintText(0.1d, 0.15d, "Arial", 9, false, "LOKASI / JALAN", -1);
        PrintText(0.95d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("location")), -1);

        if (String.valueOf(notice.get("locationDetail")).length() > 0) {
            PrintText(0.95d, 0.15d, "Arial", 9, true, " : (" + String.valueOf(notice.get("locationDetail")) + ")", -1);
        } else {
            PrintText(0.95d, 0.15d, "Arial", 9, true, " : -", -1);
        }
        PrintText(0.95d, 0.15d, "Arial", 9, true, " : " + String.valueOf(notice.get("area")), -1);
        PrintText(0.95d, 0.15d, "Arial", 9, true, " : W.P. PUTRAJAYA", -1);
        PrintText(0.1d, 0.15d, "Arial", 9, false, "TARIKH", -1);
        PrintText(2.5d, 0.0d, "Arial", 9, false, "WAKTU", -1);
        PrintText(0.95d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("date")), -1);
        PrintText(2.85d, 0.0d, "Arial", 9, true, " : " + String.valueOf(notice.get("time")), -1);
        // PrintTextFlow("KEPADA PEMUNYA / PEMANDU KENDERAAN TERSEBUT DI ATAS, TUAN / PUAN DI DAPATI TELAH MELAKUKAN KESALAHAN SEPERTI BERIKUT :", 0.1d, 0.3d);
        PrintText(0.1d, 0.9d, "Arial", 9, true, "PERUNTUKAN UNDANG-UNDANG:", -1);
        PrintTextFlow(Notice.OffenceAct, 0.3d, 0.15d);
        PrintText(0.1d, 0.15d, "Arial", 9, true, "SEKSYEN / KAEDAH / PERENGGAN :", -1);
        PrintTextFlow(Notice.OffenceSection, 0.3d, 0.15d);
        PrintText(0.1d, 0.15d, "Arial", 9, true, "KESALAHAN :", -1);
        PrintTextFlow(Notice.Offence, 0.3d, 0.15d);
        PrintText(0.1d, 0.4d, "Arial", 9, false, "DIKELUARKAN OLEH :", -1);
        PrintText(1.1d, 0.0d, "Arial", 9, true, officerDetails, -1);
        PrintText(1.1d, 0.1d, "Arial", 9, false, "WARDEN LALULINTAS", -1);
        PrintText(3.0d, 0.0d, "Arial", 9, false, "TARIKH :", -1);
        PrintText(3.5d, 0.0d, "Arial", 9, true, Notice.OffenceDateTime, -1);

        PrintText(1.92d, 2.1d, "Arial", 9, true, "RM 30", -1);
        PrintText(2.5d, 0.0d, "Arial", 9, true, "RM 50", -1);
        PrintText(3.18d, 0.0d, "Arial", 9, true, "RM 100", -1);
        //         // PrintImage("logo.bmp", 0.15d, 0.05d, 0.0d, 0.0d, false);
        // PrintText(0.7d, 0.1d, "Arial", 9, true, "PERBADANAN PUTRAJAYA", -1);
        // PrintText(0.7d, 0.2d, "Arial", 8, false, "NO. KEND.", -1);
        // PrintText(1.55d, 0.0d, "Arial", 8, true, " : " + Notice.VehicleNo, -1);
        // PrintText(0.7d, 0.1d, "Arial", 8, false, "PERUNTUKAN", -1);
        // PrintText(1.55d, 0.0d, "Arial", 8, true, " : " + Notice.OffenceAct, 50);
        // PrintText(0.7d, 0.1d, "Arial", 8, false, "SEKSYEN/KAEDAH", -1);
        // PrintText(1.55d, 0.0d, "Arial", 8, true, " : " + Notice.OffenceSection, -1);
        // PrintText(0.7d, 0.1d, "Arial", 8, false, "TARIKH", -1);
        // PrintText(1.55d, 0.0d, "Arial", 8, true, " : " + Notice.OffenceDateTime, -1);
                // PrintRect(0.1d, 0.2d, 4.0d, 0.4d);
        // PrintText(2.7d, 0.4d, "Arial", 8, true, "KERATAN UNTUK CATATAN PEMBAYARAN", -1, true);
        // PrintText(2.2d, 0.1d, "Arial", 8, false, "TERIMA KASIH", -1, true);
        PrintText(3.2d, 1.1d, "Arial", 9, false, "No. :", -1, true);
        PrintText(4.1d, 0.0d, "Arial", 9, true, String.valueOf(notice.get("noticeNo")), -1, true);
        PrintText(0.1d, 0.1d, "Arial", 9, false, "(AHMAD HILMI BIN HARUN)", -1);

        // printData.add(PrinterCommands.PRINT_TO_BLACK_MARK);
        c.save();


        try {

            doPrint = new Runnable() {
                @Override
                public void run() {
                    // Looper.prepare();
                    PrintImage(b);
                    // Looper.loop();
                    // Looper.myLooper().quit();
                }
            };
            Thread gthread = new Thread(doPrint);
            gthread.start();
            // PrintImage(b);
            result.success(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            result.error("write_error", ex.getMessage(), exceptionToString(ex));
        }
    }

    private void testPrint(Result result, String message) {
        // Bitmap b = Bitmap.createBitmap(832, 100, Config.ARGB_8888);

        b = Bitmap.createBitmap(832, 100, Config.ARGB_8888);
        c = new Canvas(b);
        c.drawColor(Color.WHITE);
        printData = new ArrayList<byte[]>();
        printData.add(PrinterCommands.INITIALIZE_PRINTER);
        PosY = 0.0;
        printData.add(PrinterCommands.PRINT_TO_BLACK_MARK);
        PrintText(0.2, 0.2, "Arial", 20, true, "message", -1);

        c.save();

        try {
            PrintImage(b);
            result.success(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            result.error("write_error", ex.getMessage(), exceptionToString(ex));
        }

    }

    private static void PrintImage(Bitmap bmp) {

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[(height * width)];
        byte pW = (byte) (width / 8);
        int pL = 100 * pW;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] bitArray = ConvertBitArray(pixels, width, height);


        THREAD.write(PrinterCommands.SET_LINE_SPACING_0);
        for (int r = 0; r < height / 100; r++) {
            byte[] send = new byte[(pL + 5)];
            send[0] = (byte) 27;
            send[1] = (byte) 88;
            send[2] = (byte) 52;
            send[3] = pW;
            send[4] = (byte) 100;

            System.arraycopy(bitArray, r * pL, send, 5, pL);
            // print(send);
            // THREAD.write(String.valueOf(r).getBytes());
            // THREAD.write(String.valueOf(r).getBytes());
            // THREAD.write(PrinterCommands.PRINT_FEED_LINE);
            // THREAD.write(String.valueOf(height).getBytes());
            // THREAD.write(PrinterCommands.PRINT_FEED_LINE);
            THREAD.write(send);
            THREAD.write(PrinterCommands.PRINT_FEED_LINE);                
        }
        THREAD.write(PrinterCommands.PRINT_TO_BLACK_MARK);

    }

    private static void PrintText(double PosX, double IncY, String FontType, int FontSize, boolean FontBold, String strVariable, int nLimit) {
        PrintText(PosX, IncY, FontType, FontSize, FontBold, strVariable, nLimit, false);
    }

    private static void PrintText(double PosX, double IncY, String FontType, int FontSize, boolean FontBold, String strVariable, int nLimit, boolean RightJustified) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize((float) (((double) FontSize) * 2.25d));
        if (FontBold) {
            p.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (RightJustified) {
            p.setTextAlign(Align.RIGHT);
        }
        if (nLimit == 0) {
            p.setTextAlign(Align.CENTER);
        }
        PosY += IncY;
        float xPos = (float) PosX * 200;
        float yPos = (float) PosY * 200;

        c.drawText(strVariable, xPos, yPos, p);
        c.save();
    }

    private static void PrintTextFlow(String strVariable, double PosX, double IncY) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(20.25f);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        PosY += IncY;
        float xPos = ((float) PosX) * 200.0f;
        float yPos = ((float) PosY) * 200.0f;
        String strTemp = "";
        while (strVariable.length() != 0) {
            if (p.measureText(strVariable) < 760.0f) {
                c.drawText(strVariable, xPos, yPos, p);
                c.save();
                yPos += 20.0f;
                strVariable = strTemp.trim();
                strTemp = "";
            }
            if (strVariable.length() != 0) {
                if (p.measureText(strVariable) < 760.0f) {
                    c.drawText(strVariable, xPos, yPos, p);
                    c.save();
                    yPos += 20.0f;
                    strVariable = "";
                    strTemp = "";
                } else {
                    strTemp = strVariable.substring(strVariable.lastIndexOf(32)) + strTemp;
                    strVariable = strVariable.substring(0, strVariable.lastIndexOf(32));
                }
            }
        }
    }

    private static void PrintRect(double left, double top, double right, double bottom) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        PosY += top;
        float lPos = ((float) left) * 200.0f;
        float rPos = ((float) right) * 200.0f;
        float tPos = ((float) PosY) * 200.0f;
        PosY += bottom;
        float bPos = ((float) PosY) * 200.0f;
        c.drawLine(lPos, tPos, rPos, tPos, p);
        c.drawLine(lPos, tPos, lPos, bPos, p);
        c.drawLine(lPos, bPos, rPos, bPos, p);
        c.drawLine(rPos, tPos, rPos, bPos, p);
        c.save();
    }

    private static byte[] ConvertBitArray(int[] src, int width, int height) {
        int w = (width / 8) + (width % 8 > 0 ? 1 : 0);
        byte[] ret = new byte[(w * height)];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < w; x++) {
                for (int b = 0; b < 8; b++) {
                    int pos = ((y * width) + (x * 8)) + b;
                    int monoPos = (y * w) + x;
                    if (((x * 8) + b) + 1 > width) {
                        ret[monoPos] = (byte) ((ret[monoPos] << 1) + 1);
                    } else if (src[pos] == -1) {
                        ret[monoPos] = (byte) (ret[monoPos] << 1);
                    } else {
                        ret[monoPos] = (byte) ((ret[monoPos] << 1) + 1);
                    }
                }
            }
        }
        return ret;
    }


    /*private static byte[] ConvertBitArray(int[] src, int width, int height) {
        int w = width / 8;
        w = w + ((width % 8 > 0) ? 1 : 0);
        byte[] ret = new byte[w * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < w; x++) {
                for (int b = 0; b < 8; b++) {
                    int pos = y * width + x * 8 + b;
                    int col = x * 8 + b + 1;
                    int monoPos = y * w + x;

                    if (col > width) {
                        ret[monoPos] = (byte) ((ret[monoPos] << 1) + 1);
                    } else {
                        if (src[pos] == -1)
                            ret[monoPos] = (byte) ((ret[monoPos] << 1));
                        else
                            ret[monoPos] = (byte) ((ret[monoPos] << 1) + 1);
                    }
                }
            }
        }
        return ret;
    }*/

    /*
     * END CUSTOM SECTION
     */

    /*private void printTest(Result result, byte[] bytes) {
        if (THREAD == null) {
            result.error("write_error", "not connected", null);
            return;
        }


        b = Bitmap.createBitmap(832, 100, Config.ARGB_8888);
        c = new Canvas(b);
        c.drawColor(Color.WHITE);
        // printData = new ArrayList();
        // printData.add(PrinterCommands.INITIALIZE_PRINTER);
        PosY = 0.0d;
        PosY = 0.0d;

        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize((float) (((double) 20) * 2.25d));
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextAlign(Align.CENTER);
        PosY += IncY;
        c.drawText("MACAddress", ((float) 0.2d) * 200.0f, ((float) 0.2d) * 200.0f, p);
        c.save();

        // PrintImage(DrawBmp());

        try {
            THREAD.write(bytes);
            
            result.success(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage(), ex);
            result.error("write_error", ex.getMessage(), exceptionToString(ex));
        }
    }*/

    /**
     *
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    readSink.success(new String(buffer, 0, bytes));
                } catch (NullPointerException e) {
                    break;
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmOutStream.flush();
                mmOutStream.close();

                mmInStream.close();

                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     */
    private final StreamHandler stateStreamHandler = new StreamHandler() {

        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                Log.d(TAG, action);

                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    THREAD = null;
                    statusSink.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1));
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    statusSink.success(1);
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    THREAD = null;
                    statusSink.success(0);
                }
            }
        };

        @Override
        public void onListen(Object o, EventSink eventSink) {
            statusSink = eventSink;
            registrar.activity().registerReceiver(mReceiver,
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

            registrar.activeContext().registerReceiver(mReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));

            registrar.activeContext().registerReceiver(mReceiver,
                new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        }

        @Override
        public void onCancel(Object o) {
            statusSink = null;
            registrar.activity().unregisterReceiver(mReceiver);
        }
    };

    /**
     *
     */
    private final StreamHandler readResultsHandler = new StreamHandler() {
        @Override
        public void onListen(Object o, EventSink eventSink) {
            readSink = eventSink;
        }

        @Override
        public void onCancel(Object o) {
            readSink = null;
        }
    };
}

class Notice {
    public static double CompoundAmount1 = 50.00;
    public static double CompoundAmount2 = 80.00;
    public static double CompoundAmount3 = 100.00;
    public static String CompoundDate = "2019/01/11";
    public static String CompoundExpiryDateString = "Test";
    public static String HandheldCode = "AC7";
    public static List<String> ImageLocation;
    public static String ImageLocation1 = "AC745679187Pic0.jpg";
    public static String ImageLocation2 = "AC745679187Pic1.jpg";
    public static String ImageLocation3 = "AC745679187Pic2.jpg";
    public static String NoticeId = "1234";
    public static String NoticeNo = "AC745679187";
    public static String Offence = "MENYEBABKAN/MEMBENARKAN KENDERAAN DIHENTIKAN DI JALAN DALAM KEDUDUKAN /KEADAAN YG MUNGKIN MENYEBABKAN BAHAYA/HALANGAN/KESUSAHAN KEPADA PENGGUNA/LALULINTAS";
    public static String OffenceAct = "AKTA PENGANGKUTAN JALAN 1987";
    public static String OffenceActCode = "04";
    public static String OffenceDateTime = "2019/01/11";
    public static String OffenceDate = "2019/01/11";
    public static String OffenceTime = "14:02:10";
    public static String OffenceLocation = "TAMAN WAWASAN";
    public static String OffenceLocationArea = "PRESINT 2";
    public static String OffenceLocationDetails = "IKN";
    public static String OffenceSection = "SEKSYEN 48";
    public static String RoadTaxNo = "42718384";
    public static String VehicleColor = "Test";
    public static String VehicleMake = "MERCEDES";
    public static String VehicleMakeModel = "MERCEDES BENZ";
    public static String VehicleNo = "RH7907";
    public static String VehicleType = "MOTOKAR";
    public static String Witness = "ZULFADZLI BIN ANWAR";

    public static void main(String[] args) {
        Notice myObj = new Notice(); // Create an object of class MyClass (This will call the constructor)
        // System.out.println(myObj.x); // Print the value of x
    }
}