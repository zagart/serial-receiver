package by.grodno.zagart.studies.serial_receiver.interfaces;

import java.io.IOException;
import java.util.List;

/**
 * Имплементации этого интерфейса предназначены для описания свода
 * правил и реализации методов, с помощью которых будет выполняться
 * обмен данными через последовательный порт List<Integer>.
 */
public interface SerialProtocol {

    int getMessageLength();

    int getSpeed();

    String process(List<Integer> data) throws IOException;

}
