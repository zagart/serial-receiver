package by.grodno.zagart.studies.serial_receiver.classes;


import by.grodno.zagart.studies.serial_receiver.database.entities.Module;
import by.grodno.zagart.studies.serial_receiver.database.entities.Stand;
import by.grodno.zagart.studies.serial_receiver.interfaces.SerialProtocol;
import by.grodno.zagart.studies.serial_receiver.network.SerialReceiver;
import by.grodno.zagart.studies.serial_receiver.database.services.impl.ModuleServiceImpl;
import by.grodno.zagart.studies.serial_receiver.database.services.impl.StandServiceImpl;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

/**
 * Класс-обработчик для TCP-клиента.
 */
public class SerialReceiverRunner extends Thread {

    private String portName;
    private SerialProtocol protocol;
    public static final Logger logger = Logger.getLogger(SerialReceiverRunner.class);
    
    public SerialReceiverRunner(String portName, SerialProtocol protocol) {
        super("SerialReceiverRunner");
        this.portName = portName;
        this.protocol = protocol;
    }

    @Override
    public void run() {
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;
        try {
            while (running) {
                running = serialReceiverRun(portName, protocol);
                if (running) {
                    System.out.println("Failed to start serial receiver. Press enter to retry.");
                    systemIn.readLine();
                }
            }
        }  catch (IOException ex) {
            logger.error(String.format("I/O exception -> ", ex.getMessage()));
        }

    }

    /**
     * Метод пробует создать новый объект класса SerialReceiver. В случае успеха
     * вызывает метод handleSerialData для обработки данных, которые будут периодически
     * обновляться внута объекта класса SerialReceiver.
     *
     * @return false, в случае успешного создания объекта (false в значении, что
     * сервер запущен и пытаться создать его снова больше не нужно), и true, если
     * создания объекта не произошло.
     */
    private synchronized boolean serialReceiverRun(String portName, SerialProtocol protocol) {
        try {
            System.out.println("Serial receiver trying to start...");
            SerialReceiver receiver = new SerialReceiver(portName, protocol);
            receiver.start();
            handleSerialData(receiver);
            System.out.println("Success. Waiting for input data.\n");
            return false;
        } catch (NoSuchPortException ex) {
            logger.warn(String.format("%s: Port not found -> %s",
                    this.getName(),
                    ex.getMessage()));
            return true;
        } catch (PortInUseException ex1) {
            logger.warn(String.format("%s: Port %s already in use -> %s",
                    this.getName(),
                    portName,
                    ex1.getMessage()));
            return true;
        } catch (IOException ex2) {
            logger.error(String.format("%s: Failed to open input stream -> %s",
                    this.getName(),
                    ex2.getMessage()));
            return true;
        } catch (UnsupportedCommOperationException ex3) {
            logger.error(String.format("%s: Error when configure port -> %s",
                    this.getName(),
                    ex3.getMessage()));
            return true;
        } catch (TooManyListenersException ex4) {
            logger.error(String.format("%s: Too many listeners for one port -> %s",
                    this.getName(),
                    ex4.getMessage()));
            return true;
        }
    }

    /**
     * Метод создает новый поток для обработки данных, хранящихся в объекте
     * класса SerialReceiver. Если в объекте найдены новые данные, то метод
     * вытягивает их и обрабатывает.
     *
     * @param receiver Объект класса SerialReceiver.
     */
    private void handleSerialData(SerialReceiver receiver) {
        (new Thread() {
            private final ModuleServiceImpl moduleService = new ModuleServiceImpl();
            private final StandServiceImpl standService = new StandServiceImpl();

            @Override
            public synchronized void run() {
                try {
                    while (receiver != null) {
                        String message;
                        if (!(message = receiver.pullMessage()).isEmpty()) {
                            Module module = Module.parseSerialString(message);
                            Stand stand = Stand.parseSerialString(message);
                            ObserverNetworkPackage observerPackage = new ObserverNetworkPackage(module, stand);
                            observerPackage.persist(standService, moduleService);
                        }
                        this.wait(10);
                    }
                } catch (InterruptedException ex) {
                    logger.error(String.format("Illegal attempt to get monitor -> ", ex.getMessage()));
                }
            }
        }).start();
    }

}
