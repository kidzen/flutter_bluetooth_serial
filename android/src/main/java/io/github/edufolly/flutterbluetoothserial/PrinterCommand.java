package io.github.edufolly.flutterbluetoothserial;

/* compiled from: PrintHandler */
class PrinterCommands {
    public static byte[] CANCEL_PRINT = new byte[]{(byte) 24};
    public static int CENTER_JUSTIFICATION = 0;
    public static byte[] DEFAULT_LINE_SPACING = new byte[]{(byte) 27, (byte) 50};
    public static byte[] DEFINE_USER_BIT_IMAGE = new byte[]{(byte) 27, (byte) 88, (byte) 52};
    public static byte[] EMPHASIZE_MODE = new byte[]{(byte) 27, (byte) 69};
    // public static byte[] INITIALIZE_PRINTER = new byte[]{(byte) 27, (byte) 64};
    public static byte[] INITIALIZE_PRINTER = {0x1B, 0x40};
    public static int LEFT_JUSTIFICATION = -1;
    public static byte[] PRINT = new byte[]{(byte) 27, (byte) 74, (byte) 0};
    public static byte[] PRINT_BARCODE_CODE128 = new byte[]{(byte) 29, (byte) 107, (byte) 73};
    public static byte[] PRINT_BARCODE_CODE39 = new byte[]{(byte) 29, (byte) 107, (byte) 69};
    public static byte[] PRINT_FEED_LINE = new byte[]{(byte) 10};
    public static byte[] PRINT_FEED_STANDARD = new byte[]{(byte) 12};
    public static byte[] PRINT_LINE_AND_BOX = new byte[]{(byte) 29, (byte) 105};
    public static byte[] PRINT_PAGE_MODE = new byte[]{(byte) 27, (byte) 12};
    public static byte[] RIGHT_CHARACTER_SPACING = new byte[]{(byte) 27, (byte) 32, (byte) 2};
    public static int RIGHT_JUSTIFICATION = 1;
    public static byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 27, (byte) 42};
    public static byte[] SELECT_BIT_IMAGE_MODE_COMPLETE = new byte[]{(byte) 27, (byte) 42, (byte) 33, (byte) 64, (byte) 3};
    public static byte[] SELECT_CHARACTER_CODE = new byte[]{(byte) 29, (byte) 116};
    public static byte[] SELECT_CHARACTER_SIZE = new byte[]{(byte) 29, (byte) 33};
    public static byte[] SELECT_INTERNATIONAL_CHARACTER_SET = new byte[]{(byte) 29, (byte) 82};
    public static byte[] SELECT_PAGE_MODE = new byte[]{(byte) 27, (byte) 76};
    public static byte[] SELECT_PRINT_DIRECTION = new byte[]{(byte) 27, (byte) 84};
    public static byte[] SELECT_PRINT_MODE = new byte[]{(byte) 27, (byte) 33, (byte) 1};
    public static byte[] SELECT_STANDARD_MODE = new byte[]{(byte) 27, (byte) 83};
    public static byte[] SET_ABSOLUTE_HORIZONTAL_POSITION = new byte[]{(byte) 27, (byte) 36};
    public static byte[] SET_ABSOLUTE_VERTICAL_POSITION = new byte[]{(byte) 29, (byte) 36};
    public static byte[] SET_BARCODE_HEIGHT = new byte[]{(byte) 29, (byte) 104};
    public static byte[] SET_BARCODE_WIDTH = new byte[]{(byte) 29, (byte) 119};
    public static byte[] SET_LINE_SPACING = new byte[]{(byte) 27, (byte) 51};
    public static byte[] SET_LINE_SPACING_0 = new byte[]{(byte) 27, (byte) 51, (byte) 0};
    public static byte[] SET_LINE_SPACING_24 = new byte[]{(byte) 27, (byte) 51, (byte) 24};
    public static byte[] SET_LINE_SPACING_30 = new byte[]{(byte) 27, (byte) 51, (byte) 30};
    public static byte[] SET_PRINTING_AREA = new byte[]{(byte) 27, (byte) 87};
    public static byte[] SET_PRINT_STARTING_POSITION = new byte[]{(byte) 27, (byte) 79};
    public static byte[] SET_RELATIVE_HORIZONTAL_POSITION = new byte[]{(byte) 27, (byte) 92};
    public static byte[] SET_RELATIVE_VERTICAL_POSITION = new byte[]{(byte) 29, (byte) 92};
    public static byte[] SET_RIGHT_SIDE_SPACING = new byte[]{(byte) 27, (byte) 32};

    PrinterCommands() {
    }
}
