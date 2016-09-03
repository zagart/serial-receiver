package by.grodno.zagart.studies.serial_receiver.interfaces;

import java.io.Serializable;

/**
 * Реализвация этого интерфейса классом указывает на то, что
 * этот класс имеет уникальный идентификатор. Делает логирование
 * DAO уровней более информативным.
 */
public interface Identifiable<PK extends Serializable> {

    PK getId();

}
