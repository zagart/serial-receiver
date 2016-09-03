package by.grodno.zagart.studies.serial_receiver.services.impl;

import by.grodno.zagart.studies.serial_receiver.dataaccess.impl.ModuleDaoImpl;
import by.grodno.zagart.studies.serial_receiver.entities.Module;
import by.grodno.zagart.studies.serial_receiver.services.AbstractHibernateService;

/**
 * Наследник абстрактного класса service, отвечает за использование
 * классов слоя dataaccess типа Module, управляет сессиями и транзакциями Hibernate
 * и выполняет логирование.
 * Благодаря механизму рефлексии реализация методов будет взята из
 * абстрактного класса-родителя на основе указанных при наследовании
 * параметров.
 */
public class ModuleServiceImpl extends AbstractHibernateService<Module, Long, ModuleDaoImpl> { }
