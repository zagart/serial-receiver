package by.grodno.zagart.studies.serial_receiver.network;


import by.grodno.zagart.studies.serial_receiver.interfaces.SerialProtocol;
import gnu.io.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Класс предназначен для обработки данных, поступающих
 * на COM-порт.
 *
 * !!! gnu.io -> сторонняя библиотека для работы с последовательным
 * и параллельным портами данных, требует ручной установки и
 * добавить Maven зависимость в pom.xml недостаточно.
 * Oracle имеет свою имплемантацию этой библиотеки, однако она
 * не поддерживает ОС Windows.
 */
public class SerialReceiver extends Thread {

    public static Logger logger = Logger.getLogger(SerialReceiver.class);

    private SerialPort port;
    private CommPortIdentifier identifier;
    private final InputStream input; //Используем не буферизированный поток, так как только он корректно читает байт с
                               // последовательного порта (значение от 0 до 255). Вероятно, это связано с
                               // тем, что единственый байтовый тип Java - byte - знаковый и имеет границы [-128; 127].
    private final SerialProtocol protocol;
    private final int bufferSize;
    private final int speed;
    private Queue<String> inbox = new ArrayBlockingQueue<>(Byte.MAX_VALUE);

    /**
     * Конструктор класса создает новый объект, ассоциированный с последовательным
     * портом в соответствии с указанным именем порта и протоколом.
     *
     * @param portName Имя COM-порта.
     * @param protocol Протокол передачи данных через последовательный порт.
     * @throws NoSuchPortException
     * @throws PortInUseException
     * @throws IOException
     * @throws UnsupportedCommOperationException
     * @throws TooManyListenersException
     */
    public SerialReceiver(String portName, SerialProtocol protocol) throws NoSuchPortException,
            PortInUseException,
            IOException,
            UnsupportedCommOperationException,
            TooManyListenersException {
        super("SerialReceiver");
        if (portName == null || protocol == null) {
            throw new IOException("Parameter cannot be null!");
        }
        findPort(portName);
        portInit();
        this.input = this.port.getInputStream();
        this.protocol = protocol;
        this.bufferSize = protocol.getMessageLength();
        this.speed = protocol.getSpeed();
    }

    @Override
    public void run() {
        waitData();
    }

    /**
     * Метод читает данные с последовательного порта и обрабатывает их
     * в соответствии с протоколом и складывает в объект этого класса ArrayBlockingQueue.
     */
    private synchronized void waitData() {
        try {
            List<Integer> data;
            while (input != null) {
                data = readBytes();
                String result = protocol.process(data);
                if (!result.isEmpty()) {
                    inbox.offer(result);
                }
                this.wait(10);
            }
            this.port.close();
        } catch (IOException ex) {
            if (!ex.getMessage().contains("No error")) {
                logger.error(String.format("%s: Error when reading stream -> %s",
                        this.getName(),
                        ex.getMessage()));
            }
        } catch (InterruptedException ex1) {
            logger.error(String.format("%s: Attempt to get monitor when thread waiting -> %s",
                    this.getName(),
                    ex1.getMessage()));
        }
    }

    /**
     * Метод читает байты из потока, связанного с последовательным портом,
     * в том случае, если в потоке есть байты в количестве значения поля
     * bufferSize, доступные для чтения.
     *
     * @return Возвращает List считанных байтов.
     * @throws IOException
     */
    private List<Integer> readBytes() throws IOException {
        List<Integer> bytes = new ArrayList<>();
        int value;
        int counter = 0;
        if (input.available() >= bufferSize) {
            while (counter++ < bufferSize && ((value = readByte()) != -1)) {
                bytes.add(value);
            }
        }
        return bytes;
    }

    /**
     * Метод читает один байт из потока, связанного с последовательным портом.
     *
     * @return Считанный байт (значение от 0 до 255) либо -1, если произошла
     * исключительная ситуация ввода/вывода.
     * @throws IOException
     */
    private int readByte() throws IOException {
        if (input != null) {
            try {
                return input.read();
            } catch (IOException ex) {
                if (ex.getMessage().contains("Stream closed")) {
                    throw ex;
                }
                return -1;
            }
        }
        return -1;
    }

    /**
     * Метод конфигурирует COM-порт.
     *
     * @throws UnsupportedCommOperationException
     */
    private void portInit() throws UnsupportedCommOperationException {
        port.setSerialPortParams(speed,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        port.setDTR(false);
        port.setRTS(true);
    }


    /**
     * Метод извлекает список доступных последовательных портов и ищет порт,
     * указанный в параметрах и, если находит - устанавливает его портом этого
     * объекта.
     *
     * @param portName Имя искомого порта.
     * @throws NoSuchPortException
     * @throws PortInUseException
     */
    private void findPort(String portName) throws NoSuchPortException, PortInUseException {
        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier identifier = (CommPortIdentifier)ports.nextElement();
            if (identifier.getName().equals(portName)) {
                this.identifier = identifier;
                this.port = (SerialPort) this.identifier.open("SerialReceiver", 2000);
            }
        }
        if (identifier == null) {
            throw new NoSuchPortException();
        }
    }

    /**
     * Метод возвращает одно из полученных и обработанных сообщений (head), но при этом
     * удаляет его из хранилища.
     *
     * @return Обработанное сообщение COM-порта.
     */
    public String pullMessage() {
        if (!inbox.isEmpty()) {
            return inbox.poll();
        }
        return "";
    }

}
