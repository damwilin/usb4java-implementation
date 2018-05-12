import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.util.List;

public class Connector {

    private static void dumpDevice(final UsbDevice device) {
        System.out.println(device);

        final UsbPort port = device.getParentUsbPort();

        for (UsbConfiguration configuration : (List<UsbConfiguration>) device.getUsbConfigurations()) {
            System.out.println(configuration.getUsbConfigurationDescriptor());

            for (UsbInterface usbInterface : (List<UsbInterface>) configuration.getUsbInterfaces())
            {
                System.out.println(usbInterface.getUsbInterfaceDescriptor());

                for (UsbEndpoint endpoint: (List<UsbEndpoint>) usbInterface.getUsbEndpoints()){
                    System.out.println(endpoint.getUsbEndpointDescriptor());
                }
            }
        }


        if (device.isUsbHub()) {
            final UsbHub hub = (UsbHub) device;
            for (UsbDevice child : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                dumpDevice(child);
            }
        }

    }

    public static void getDevicesList() throws UsbException {
        final UsbServices services = UsbHostManager.getUsbServices();
        dumpDevice(services.getRootUsbHub());
    }

    private static UsbDevice findDeviceHelper(UsbHub hub, short vendorId, short productId) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId)
                return device;
            if (device.isUsbHub()) {
                device = findDeviceHelper((UsbHub) device, vendorId, productId);
                if (device != null)
                    return device;
            }
        }
        return null;
    }

    public static UsbDevice findDevice(short vendorId, short productId) throws UsbException {
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub hub = services.getRootUsbHub();
        return findDeviceHelper(hub, vendorId, productId);
    }

    public static void readMessageAsynch(UsbInterface iface,
                                  int endPoint){

        UsbPipe pipe = null;

        try {
            iface.claim(new UsbInterfacePolicy() {
                @Override
                public boolean forceClaim(UsbInterface usbInterface) {
                    return true;
                }
            });

            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();

            pipe.open();

            pipe.addUsbPipeListener(new UsbPipeListener()
            {
                @Override
                public void errorEventOccurred(UsbPipeErrorEvent event)
                {
                    UsbException error = event.getUsbException();
                    error.printStackTrace();
                }

                @Override
                public void dataEventOccurred(UsbPipeDataEvent event)
                {
                    byte[] data = event.getData();

                    System.out.println(data + " bytes received");
                }
            });

//			pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbNotActiveException e) {
                e.printStackTrace();
            } catch (UsbDisconnectedException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            }
        }

    }

    public static void readMessage(UsbInterface iface,
                            int endPoint){

        UsbPipe pipe = null;

        try {
            iface.claim(usbInterface -> true);
            System.out.println("Endpoint list:");
            for (int i=0; i< iface.getUsbEndpoints().size(); i++){
                System.out.println(iface.getUsbEndpoints().get(i));
            }
            UsbEndpoint endpoint = (UsbEndpoint) iface.getUsbEndpoints().get(endPoint); // there can be more 1,2,3..
            pipe = endpoint.getUsbPipe();
            pipe.open();

            byte[] data = new byte[8];
            int received = pipe.syncSubmit(data);
            System.out.println(received + " bytes received");

            pipe.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                iface.release();
            } catch (UsbClaimException e) {
                e.printStackTrace();
            } catch (UsbNotActiveException e) {
                e.printStackTrace();
            } catch (UsbDisconnectedException e) {
                e.printStackTrace();
            } catch (UsbException e) {
                e.printStackTrace();
            }
        }

    }


    public static UsbInterface getDeviceInterface(UsbDevice device, int index) {

        UsbConfiguration configuration = device.getActiveUsbConfiguration();
        System.out.println("Usb interfaces:");
        for (int i=0; i< configuration.getUsbInterfaces().size(); i++){
            System.out.println(configuration.getUsbInterfaces().get(i).toString());
        }
        UsbInterface iface = (UsbInterface) configuration.getUsbInterfaces().get(index); // there can be more 1,2,3..

        return iface;
    }

}
