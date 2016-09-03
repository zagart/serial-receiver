package by.grodno.zagart.studies.serial_receiver;

import by.grodno.zagart.studies.serial_receiver.classes.SerialReceiverRunner;
import by.grodno.zagart.studies.serial_receiver.network.protocols.ObserverSerialProtocol;

public class SerialReceiverMain {

    public static void main(String[] args) {
        (new SerialReceiverRunner("COM1", new ObserverSerialProtocol())).start();
    }

}
