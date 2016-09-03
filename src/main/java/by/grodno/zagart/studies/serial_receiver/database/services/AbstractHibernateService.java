package by.grodno.zagart.studies.serial_receiver.database.services;

import by.grodno.zagart.studies.serial_receiver.database.dataaccess.GenericDao;
import by.grodno.zagart.studies.serial_receiver.interfaces.Identifiable;
import by.grodno.zagart.studies.serial_receiver.interfaces.Reflective;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static by.grodno.zagart.studies.serial_receiver.utils.HibernateUtil.*;

/**
 * Абстрактный класс, аналогичный классу слоя dataaccess. Выполняет
 * задачи уровня service, используя уровень dataaccess.
 *
 * @param <T>
 * @param <PK>
 * @param <DAO>
 */
public abstract class AbstractHibernateService
        <T extends Identifiable,
                PK extends Serializable,
                DAO extends GenericDao>
        implements GenericService<T, PK>, Reflective {

    private GenericDao dao = (GenericDao) getGenericObject(2);
    public final Logger logger = Logger.getLogger(dao.getClass());
    private final T entityObj;

    { entityObj = (T) getGenericObject(0); }

    @Override
    public PK save(T obj) {
        openCurrentSessionWithTransaction();
        dao.save(obj);
        closeCurrentSessionWithTransaction();
        logger.info(String.format("%s object saved with id = %d.",
                entityObj.getClass().getSimpleName(),
                obj.getId()));
        return (PK) obj.getId();
    }

    @Override
    public void update(T obj) {
        openCurrentSessionWithTransaction();
        dao.update(obj);
        closeCurrentSessionWithTransaction();
        logger.info(String.format("%s object with id = %d updated.",
                entityObj.getClass().getSimpleName(),
                obj.getId()));
    }

    @Override
    public List<T> getAll() {
        openCurrentSession();
        List<T> daoAll = dao.getAll();
        closeCurrentSession();
        logger.info(String.format("All %s objects pulled from database(%d).",
                entityObj.getClass().getSimpleName(),
                daoAll.size()));
        return daoAll;
    }

    @Override
    public List<T> getListByQuery(String hql) {
        openCurrentSession();
        List<T> daoListByQuery = dao.getListByQuery(hql);
        closeCurrentSession();
        logger.info(String.format("%s object(s) pulled from database by query(%d).",
                entityObj.getClass().getSimpleName(),
                daoListByQuery.size()));
        return daoListByQuery;
    }

    @Override
    public Set<PK> getPkSetByQuery(String hql) {
        openCurrentSession();
        Set<PK> daoPkSetByQuery = dao.getPkSetByQuery(hql);
        closeCurrentSession();
        logger.info(String.format("%s id(s) pulled from database by query(%d).",
                entityObj.getClass().getSimpleName(),
                daoPkSetByQuery.size()));
        return daoPkSetByQuery;
    }

    @Override
    public int executeQuery(String hql, Map<String, Object> parameters) {
        openCurrentSessionWithTransaction();
        int affected = dao.executeQuery(hql, parameters);
        closeCurrentSessionWithTransaction();
        logger.info(String.format("%s query executed. %d element(s) affected.",
                entityObj.getClass().getSimpleName(),
                affected));
        return affected;
    }

    @Override
    public T getById(PK id) {
        openCurrentSession();
        T obj = (T) dao.getById(id);
        closeCurrentSession();
        logger.info(String.format("%s object pulled from database by id = %d.",
                entityObj.getClass().getSimpleName(),
                obj.getId()));
        return obj;
    }

    @Override
    public void delete(PK id) {
        openCurrentSessionWithTransaction();
        dao.delete(id);
        closeCurrentSessionWithTransaction();
        logger.info(String.format("%s object deleted from database by id = %d.",
                entityObj.getClass().getSimpleName(), id));
    }

    @Override
    public void delete(T obj) {
        openCurrentSessionWithTransaction();
        dao.delete(obj);
        closeCurrentSessionWithTransaction();
        logger.info(String.format("%s object with id = %d deleted from database.",
                entityObj.getClass().getSimpleName(),
                obj.getId()));
    }

}
