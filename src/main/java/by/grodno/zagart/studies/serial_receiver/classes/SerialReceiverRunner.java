package by.grodno.zagart.studies.serial_receiver.classes;


import by.grodno.zagart.studies.serial_receiver.database.services.impl.ModuleServiceImpl;
import by.grodno.zagart.studies.serial_receiver.database.services.impl.StandServiceImpl;
import by.grodno.zagart.studies.serial_receiver.interfaces.SerialProtocol;
import by.grodno.zagart.studies.serial_receiver.network.SerialReceiver;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TooManyListenersException;

/**
 * Класс-обработчик для TCP-клиента.
 */
public class SerialReceiverRunner extends Thread {

    public static final Logger logger = Logger.getLogger(SerialReceiverRunner.class);
    private static ResourceBundle l10n;

    private String portName;
    private SerialProtocol protocol;

    public SerialReceiverRunner(String portName, SerialProtocol protocol, Locale locale) throws IOException {
        super("SerialReceiverRunner");
        if (portName == null || protocol == null || locale == null) {
            throw new IOException("Parameter cannot be null!");
        }
        this.portName = portName;
        this.protocol = protocol;
        this.l10n = ResourceBundle.getBundle("messages", locale);
    }

    @Override
    public void run() {
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
        boolean running = true;
        try {
            while (running) {
                running = serialReceiverRun(portName, protocol);
                if (running) {
                    System.out.println(l10n.getString("serialStartError"));
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
            System.out.println(String.format(l10n.getString("serialStart"), portName));
            SerialReceiver receiver = new SerialReceiver(portName, protocol);
            receiver.start();
            handleSerialData(receiver);
            System.out.println(l10n.getString("serialStartSuccess"));
            return false;
        } catch (NoSuchPortException ex) {
            logger.warn(String.format("Port %s not found -> %s",
                    portName,
                    ex.getMessage()));
            return true;
        } catch (PortInUseException ex1) {
            logger.warn(String.format("Port %s already in use -> %s",
                    portName,
                    ex1.getMessage()));
            return true;
        } catch (IOException ex2) {
            logger.error(String.format("Failed to open input stream -> %s",
                    ex2.getMessage()));
            return true;
        } catch (UnsupportedCommOperationException ex3) {
            logger.error(String.format("Error when configure port %s -> %s",
                    portName,
                    ex3.getMessage()));
            return true;
        } catch (TooManyListenersException ex4) {
            logger.error(String.format("Too many listeners for one port (%s) -> %s",
                    portName,
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
                        ObserverNetworkPackage networkPackage;
                        if ((networkPackage = (ObserverNetworkPackage) receiver.pullMessage()) != null) {
                            networkPackage.persist(standService, moduleService);
                            printPackageInfo(networkPackage);
                        }
                        this.wait(10);
                    }
                } catch (InterruptedException ex) {
                    logger.error(String.format("Illegal attempt to get monitor -> ", ex.getMessage()));
                }
            }

            private void printPackageInfo(ObserverNetworkPackage networkPackage) {
                System.out.println("*****************************************************************");
                System.out.printf(l10n.getString("printPackageInfo"),
                        networkPackage.getStand().getNumber(),
                        networkPackage.getModule().getName(),
                        networkPackage.getModule().getStatus(),
                        networkPackage.getModule().getValue());
                System.out.println("*****************************************************************");
            }
        }).start();
    }



}
