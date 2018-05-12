import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbInterface;

public class Main {
    public static void main(String[] args) {
        tryToFind();
    }

    public static void tryToFind() {
        String answer = null;
        try {
           UsbDevice device = Connector.findDevice((short) (0x2341), (short) (0x0043));
            if (device != null) {
                answer = "Device found.";
            } else
                answer = "Device NOT found.";
        } catch (UsbException e) {
            e.printStackTrace();
        }
        System.out.println(answer);
    }

    public static void readData(UsbDevice device){
        UsbInterface usbInterface = Connector.getDeviceInterface(device,0);
        Connector.readMessageAsynch(usbInterface, 0);
    }
}
