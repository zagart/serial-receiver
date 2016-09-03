package by.grodno.zagart.studies.serial_receiver;

import by.grodno.zagart.studies.serial_receiver.classes.SerialReceiverRunner;
import by.grodno.zagart.studies.serial_receiver.network.protocols.ObserverSerialProtocol;
import org.apache.log4j.Logger;

public class SerialReceiverMain {

    public static final Logger logger = Logger.getLogger(SerialReceiverMain.class);

    public static void main(String[] args) {
        ObserverSerialProtocol protocol = new ObserverSerialProtocol();
        protocol.setOutput(System.out);
        protocol.addFilter(ObserverSerialProtocol.Constant.LCD_DISPLAY);
        (new SerialReceiverRunner("COM1", protocol)).start();
    }

}
