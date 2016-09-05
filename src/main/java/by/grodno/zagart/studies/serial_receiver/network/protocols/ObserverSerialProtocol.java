package by.grodno.zagart.studies.serial_receiver.network.protocols;


import by.grodno.zagart.studies.serial_receiver.interfaces.SerialProtocol;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static by.grodno.zagart.studies.serial_receiver.network.protocols.ObserverSerialProtocol.Constant.MSG_HEAD;
import static by.grodno.zagart.studies.serial_receiver.network.protocols.ObserverSerialProtocol.Constant.MSG_TAIL;

/**
 * Класс содержит набор констант и методы для обработки данных,
 * соответствующий правилам обмена данными через последовательный
 * порт единицами проекта Observer. Имплементация интерфейса
 * SerialProtocol.
 */
public class ObserverSerialProtocol implements SerialProtocol {

    public static final Logger logger = Logger.getLogger(ObserverSerialProtocol.class);

    private static ResourceBundle l10n;
    private static final int messageLength = 6;
    private static final int speed = 9600;
    private final MessageFormatChecker checker = new MessageFormatChecker();

    private PrintStream output;

    public ObserverSerialProtocol(Locale locale) throws IOException {
        if (locale == null) {
            throw new IOException("Parameter cannot be null!");
        }
        this.l10n = ResourceBundle.getBundle("messages", locale);
    }

    @Override
    public int getMessageLength() {
        return messageLength;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    /**
     * Метод получает объект List с численными значениями, полученными в результате
     * обработки данных с последовательного порта и обрабатывает их в соответствии с
     * требованиями протокола и списком фильтов.
     *
     * @param serialData Набор байт, полученных с последовательного порта, представленный
     *                   в виде объекта List типа Integer.
     * @return
     * @throws IOException
     */
    @Override
    public String process(List<Integer> serialData) throws IOException {
        if (!serialData.isEmpty() ) {
            if (serialData.size() == messageLength) {
                if (checker.isMessage(serialData)) {
                    if (checker.isValidArguments(serialData)) {
                        if (!checker.isInFilter(serialData)) {
                            String data = String.format(l10n.getString("observerProtocolNewData"),
                                    dataToString(serialData));
                            if (output != null) {
                                output.println(data);
                            }
                            return compilePropertiesString(serialData);
                        } else {
                            return "";
                        }
                    } else {
                        throw new IOException("Incorrect MODULE(3)/STATUS(4) arguments.");
                    }
                } else {
                    throw new IOException("HEAD/TAIL message marks are missing.");
                }
            } else {
                throw new IOException("Wrong message length.");
            }
        }
        return "";
    }

    /**
     * Метод позволяет установить для объекта класса поток, куда будут
     * выводиться данные. По умолчанию используется поток System.out.
     *
     * @param output Поток вывода.
     */
    public void setOutput(PrintStream output) {
        this.output = output;
    }

    /**
     * Метод добавляет в список фильтров класса константу протокола.
     *
     * @param filter Константа протокола.
     * @return true в случае успеха и false иначе.
     */
    public boolean addFilter(Constant filter) {
        return checker.addFilter(filter);
    }

    /**
     * Метод удаляет из списка фильтров класса константу протокола.
     *
     * @param filter Константа протокола.
     * @return true в случае успеха и false иначе.
     */
    public boolean removeFilter(Constant filter) {
        return checker.removeFilter(filter);
    }

    /**
     * Метод возвращает список констант, являющихся фильтрами для
     * данного объекта класса-протокола.
     *
     * @return Список констант из фильтра.
     */
    public Set<Constant> getFilterList() {
        return checker.getFilterList();
    }

    /**
     * Метод обрабатывает данные с последовательного порта в соответствии с протоколом
     * и собирает их в объект типа Properties, после чего вызывает у объекта метод
     * toString и возвращает результат.
     *
     * @param serialData
     * @return
     */
    private String compilePropertiesString(List<Integer> serialData) {
        int standNumber = 1;
        int moduleName = 2;
        int eventDescription = 3;
        int eventValue = 4;
        Properties properties = new Properties();
        properties.put("stand", serialData.get(standNumber));
        properties.put("module", getConstantDescriptionByValue(serialData.get(moduleName)));
        properties.put("event", getConstantDescriptionByValue(serialData.get(eventDescription)));
        properties.put("value", serialData.get(eventValue));
        return properties.toString();
    }

    /**
     * Преобразует данные с последовательного порта в упрощенном формате
     * в текстовую переменную String.
     *
     * @param serialData Набор байт, полученных с последовательного порта, представленный
     *                   в виде объекта List типа Integer.
     * @return
     */
    private String dataToString(List<Integer> serialData) {
        List<String> convertedData = new ArrayList<>();
        for (Integer dataByte : serialData) {
            String convertedByte = getConstantNameByValue(dataByte);
            if (!convertedByte.isEmpty()) {
                convertedData.add(convertedByte);
            } else {
                convertedData.add(dataByte.toString());
            }
        }
        return convertedData.toString();
    }

    /**
     * Возвращает текстовое описание константы протокола, соответствующее
     * значению параметра.
     *
     * @param value Числовое значение константы.
     * @return
     */
    private String getConstantDescriptionByValue(int value) {
        for (Constant c : Constant.values()) {
            if (c.value == value) {
                return c.description;
            }
        }
        return "";
    }

    /**
     * Возвращает текстовое имя константы протокола, соответствующее
     * значению параметра.
     *
     * @param value Числовое значение константы.
     * @return
     */
    private String getConstantNameByValue(int value) {
        for (Constant c : Constant.values()) {
            if (c.value == value) {
                return c.name();
            }
        }
        return "";
    }

    /**
     * Тип-перечисление, описывающий константы протокола.
     */
    public enum Constant {
        INIT(2, "Инициализация."),
        TEMP_CHANGE (3, "Изменение температуры."),
        LIGHT_CHANGE (4, "Изменение освещенности."),
        LCD_NEW_OUTPUT (5, "Изменение данных на LCD-дисплее."),
        STAND_MC (10, "Микро-контроллер."),
        LIGHT_SENSOR (11, "Датчик освещенности."),
        TEMP_SENSOR (12, "Датчик температуры."),
        LCD_DISPLAY (13, "LCD-дисплей."),
        NULL (200, "Данные отсутствуют или не предусмотрены для этого события/модуля."),
        OERR (201, "Критическая ошибка модуля USART."),
        FERR (202, "Ошибка стопового бита модуля USART."),
        NO_MSG (203, "Нет ответа."),
        SYSTEM_EXIT (204, "Аварийное отключение/прерывание/перезагрузка."),
        MSG_HEAD(205, "Метка начала сообщения."),
        MSG_TAIL(206, "Метка конца сообщения.");

        private final int value;
        private final String description;

        Constant(int value, String description) {
            this.value = value;
            this.description = description;
        }

    }

    /**
     * Внутренний класс протокола, описывающий методы для проверки корректности
     * данных, полученных с последовательного порта.
     */
    private class MessageFormatChecker {

        private Set<Constant> filterList = new HashSet<>();

        /**
         * Метод добавляет в список фильтров значение константы, если его
         * еще там нет.
         *
         * @param filter Новое значение для списка фильтров.
         */
        private boolean addFilter(Constant filter) {
            if (filter != null) {
                return filterList.add(filter);
            }
            return false;
        }

        /**
         * Метод удаляет из списка фильтров значение константы, если оно
         * там есть.
         *
         * @param filter Константа из списка фильтров.
         */
        private boolean removeFilter(Constant filter) {
            if (filter != null) {
                return filterList.remove(filter);
            }
            return false;
        }

        /**
         * @return Множество фильтров.
         */
        private Set<Constant> getFilterList() {
            return filterList;
        }

        /**
         * Метод проверяет, содержат ли данные из параметра значения,
         * содержащиеся в списке фильтров.
         *
         * @param serialData Список для фильтрации.
         * @return true, если содержат, и false - если нет.
         */
        private boolean isInFilter(List<Integer> serialData) {
            for (Constant filter : filterList) {
                if (serialData.contains(filter.value)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Метод проверяет наличие меток, указывающих на то, что набор
         * байт является сообщением последовательного порта проекта Observer.
         *
         * @param data Набор байт с последовательного порта.
         * @return
         * @throws IOException
         */
        private boolean isMessage(List<Integer> data) throws IOException {
            int firstElement = 0;
            int lastElement = messageLength - 1;
            if (data.get(firstElement) == MSG_HEAD.value && data.get(lastElement) == MSG_TAIL.value) {
                    return true;
            }
            return false;
        }

        /**
         * Метод проверяет корректность байтов из последовательности (байт имени модуля
         * и байт статуса).
         *
         * @param data Набор байт с последовательного порта.
         * @return
         * @throws IOException
         */
        private boolean isValidArguments(List<Integer> data) throws IOException {
            int module = 2;
            int status = 3;
            if (isConstant(data.get(module)) && isConstant(data.get(status))) {
                    return true;
            }
            return false;
        }

        /**
         * Метод проверяет, является ли значение в параметре константой
         * простокола Observer.
         *
         * @param value Обрабатываемое значение типа int.
         * @return
         */
        private boolean isConstant(int value) {
            for (Constant c : Constant.values()) {
                if (c.value == value) {
                    return true;
                }
            }
            return false;
        }

    }

}
