package by.grodno.zagart.studies.serial_receiver;

import by.grodno.zagart.studies.serial_receiver.classes.SerialReceiverRunner;
import by.grodno.zagart.studies.serial_receiver.network.protocols.ObserverSerialProtocol;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public class SerialReceiverMain {

    public static final Logger logger = Logger.getLogger(SerialReceiverMain.class);

    /**
     * Точка входа приложения.
     * Пример входных параметров: COM1 UTF-8 ru
     * Первый параметр - имя последовательного порта для подключения. Второй
     * параметр - кодировка для вывода сообщений (кодировка файлов ресурсов - UTF-8,
     * системная кодировка Windows: windows-1251, но ее перекрывает кодировка консоли: cp866).
     * Третий параметр - локаль. Доступны русская и английская (ru/en).
     *
     * @param args Массив входных данных.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String portName = args[0];
        String encoding = args[1];
        String localeName = args[2];
        Locale locale = new Locale(localeName);
        System.setOut(new PrintStream(System.out, true, encoding));
        ObserverSerialProtocol protocol = new ObserverSerialProtocol(locale);
        protocol.setOutput(System.out);
        protocol.addFilter(ObserverSerialProtocol.Constant.LCD_DISPLAY);
        (new SerialReceiverRunner(portName, protocol, locale)).start();
    }

}
