package by.grodno.zagart.studies.serial_receiver.classes;

import by.grodno.zagart.studies.serial_receiver.entities.Module;
import by.grodno.zagart.studies.serial_receiver.entities.Stand;
import by.grodno.zagart.studies.serial_receiver.services.impl.ModuleServiceImpl;
import by.grodno.zagart.studies.serial_receiver.services.impl.StandServiceImpl;

import java.io.Serializable;

/**
 * Класс инкапсулирует данные, передаваемые между единицами
 * проекта Observer посредством сетевых соединений. Пакет
 * TCP-данных проекта Observer.
 */
public class ObserverNetworkPackage implements Serializable {

    private static final long serialVersionUID = 3L;

    private Module module;
    private Stand stand;

    /**
     * Для создания объекта класса необходимы объекты, указанные
     * в параметрах конструктора. Объект module должен быть
     * элементом объекта stand, так как при сохранении в базу
     * данных между ними устанавливается связь!
     *
     * @param module Объект класса Module.
     * @param stand Объект класса Stand.
     */
    public ObserverNetworkPackage(Module module, Stand stand) {
        this.module = module;
        this.stand = stand;
    }

    public Module getModule() {
        return module;
    }
    public Stand getStand() {
        return stand;
    }

    /**
     * Сохраняем данные из данного класс в базу данных, используя
     * сервисы соответствующих типов.
     *
     * @param standService Сервис класса Stand.
     * @param moduleService Сервис класс Module.
     */
    public void persist(StandServiceImpl standService, ModuleServiceImpl moduleService) {
        standService.save(stand);
        moduleService.save(module);
        stand.addModule(module);
        standService.update(stand);
        moduleService.update(module);
    }

}
