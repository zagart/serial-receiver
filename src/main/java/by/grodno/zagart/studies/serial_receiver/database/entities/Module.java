package by.grodno.zagart.studies.serial_receiver.database.entities;

import by.grodno.zagart.studies.serial_receiver.interfaces.Identifiable;
import by.grodno.zagart.studies.serial_receiver.utils.DataUtil;
import org.apache.log4j.Logger;

import javax.persistence.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

/**
 * Класс описывает объекты типа "модуль" и их свойства.
 * Также предоставляет доступ к полям.
 */
@Entity
@Table(name = "MODULE")
public class Module implements Identifiable<Long>, Serializable {

    public static final Logger logger = Logger.getLogger(Module.class);

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String statusInfo;
    private Date statusChangeDate;
    private Stand stand;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @Column(name = "NAME")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Column(name = "STATUS")
    public String getStatusInfo() { return statusInfo; }
    public void setStatusInfo(String statusInfo) { this.statusInfo = statusInfo; }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "STATUS_DATE")
    public Date getStatusChangeDate() { return statusChangeDate; }
    public void setStatusChangeDate(Date statusChangeDate) { this.statusChangeDate = statusChangeDate; }

    @ManyToOne
    @JoinColumn(name = "STAND_ID")
    public Stand getStand() { return stand; }
    public void setStand(Stand stand) { this.stand = stand; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        if (!id.equals(module.id)) return false;
        if (name != null ? !name.equals(module.name) : module.name != null) return false;
        if (statusInfo != null ? !statusInfo.equals(module.statusInfo) : module.statusInfo != null) return false;
        if (statusChangeDate != null ? !statusChangeDate.equals(module.statusChangeDate) : module.statusChangeDate != null)
            return false;
        return stand != null ? stand.equals(module.stand) : module.stand == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (statusInfo != null ? statusInfo.hashCode() : 0);
        result = 31 * result + (statusChangeDate != null ? statusChangeDate.hashCode() : 0);
        result = 31 * result + (stand != null ? stand.hashCode() : 0);
        return result;
    }

    public static Module parseTcpString(String tcpData) throws NoClassDefFoundError {
        Module module = new Module();
        try {
            Properties properties = DataUtil.convertStringToProperties(tcpData);
            module.setName(properties.getProperty("name"));
            module.setStatusInfo(properties.getProperty("status"));
            module.setStatusChangeDate(new Date());
        } catch (IOException ex) {
            logger.error("Module class. Convertion (string-to-properties) error: " + ex.getStackTrace());
        }
        if (module.getName() == null || module.getStatusInfo() == null) {
            throw new NoClassDefFoundError();
        }
        return module;
    }

    public static Module parseSerialString(String serialData) {
        Module module = new Module();
        try {
            Properties properties = DataUtil.convertStringToProperties(serialData);
            module.setName(properties.getProperty("module"));
            module.setStatusInfo(String.format("%s Новое значение -> %s.",
                    properties.getProperty("event"),
                    properties.getProperty("value")));
            module.setStatusChangeDate(new Date());
        } catch (IOException ex) {
            logger.error("Module class. Convertion (string-to-properties) error: " + ex.getStackTrace());
        }
        if (module.getName() == null || module.getStatusInfo() == null) {
            throw new NoClassDefFoundError();
        }
        return module;
    }

}
