package by.grodno.zagart.studies.serial_receiver.services.impl;

import by.grodno.zagart.studies.serial_receiver.dataaccess.impl.StandDaoImpl;
import by.grodno.zagart.studies.serial_receiver.entities.Stand;
import by.grodno.zagart.studies.serial_receiver.services.AbstractHibernateService;

/**
 * Наследник абстрактного класса service, отвечает за использование
 * классов слоя dataccess типа Stand, управляет сессиями и транзакциями Hibernate
 * и выполняет логирование.
 * Благодаря механизму рефлексии реализация методов будет взята из
 * абстрактного класса-родителя на основе указанных при наследовании
 * параметров.
 */
public class StandServiceImpl extends AbstractHibernateService<Stand, Long, StandDaoImpl> { }