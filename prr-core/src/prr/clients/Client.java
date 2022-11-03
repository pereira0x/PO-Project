package prr.clients;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import prr.Notifications.Notifiable;
import prr.Notifications.Notification;
import prr.Notifications.NotificationDeliveryMethod;
import prr.TarrifPlans.BaseTarrifPlan;
import prr.TarrifPlans.TarrifPlan;
import prr.communications.Communication;
import prr.terminals.Terminal;

public class Client implements Serializable, Notifiable {

    /** Serial number for serialization. */
    private static final long serialVersionUID = 202210121030L;

    /** The client's key. */
    private String _key;

    /** The client's name. */
    private String _name;

    /** The client's tax id number. */
    private int _taxId;

    /** Notifications setting. */
    private boolean _notifiable;

    /** Client level */
    private Level _level;

    /** Client terminals */
    private Map<String, Terminal> _terminals;

    private TarrifPlan _tarrifPlan;

    private final Queue<Notification> inAppNotifications = new LinkedList<>();
    private NotificationDeliveryMethod notificationDeliveryMethod = inAppNotifications::add;

    /**
     * Constructor.
     * 
     * @param key        the client's key
     * @param name       the client's name
     * @param taxId      the client's tax id number
     * @param payments   total payments made by this client
     * @param debts      total debts of this client
     * @param notifiable notifications setting
     */
    public Client(String key, String name, int taxId) {
        _key = key;
        _name = name;
        _taxId = taxId;
        _level = new NormalLevelClient(this);
        _notifiable = true;
        _terminals = new TreeMap<>();
        _tarrifPlan = new BaseTarrifPlan();
    }

    /**
     * @return the client's key
     */
    public String getKey() {
        return _key;
    }

    public TarrifPlan getTarrifPlan() {
        return _tarrifPlan;
    }

    /**
     * @return the client's name
     */
    public String getName() {
        return _name;
    }

    /**
     * @return the client's tax id number
     */
    public int getTaxId() {
        return _taxId;
    }

    /**
     * @return the total payments made by this client
     */
    public float getPayments() {
        float payments = 0;
        for (Terminal terminal : _terminals.values())
            payments += terminal.getPayments();
        return payments;
    }

    /**
     * @return the total debts of this client
     */
    public float getDebts() {
        // get the debt of all terminals
        float debt = 0;
        for (Terminal terminal : _terminals.values())
            debt += terminal.getDebts();
        return debt;
    }

    /**
     * @return the notifications setting
     */
    public boolean getNotifiable() {
        return _notifiable;
    }

    /**
     * disable client notifications
     */
    public void disableNotifiable() {
        _notifiable = false;
    }

    /**
     * enable client notifications
     */
    public void enableNotifiable() {
        _notifiable = true;
    }

    /**
     * @return the client's number of terminals
     */
    public int numberOfTerminals() {
        return _terminals.size();
    }

    /**
     * Adds a terminal to the client's terminals
     */
    public void addTerminal(Terminal terminal) {
        _terminals.put(terminal.getKey(), terminal);
    }

    /**
     * Gets a terminal from the client's terminals
     */
    public Terminal getTerminal(String key) {
        return _terminals.get(key);
    }

    public Level getLevel() {
        return _level;
    }

    public void setLevel(Level level) {
        _level = level;
    }

    public Collection<Communication> getSentCommunications() {
        Map<Integer, Communication> communications = new TreeMap<>();
        for (Terminal terminal : _terminals.values()) {
            communications.putAll(terminal.getSentCommunications());
        }
        return communications.values();
    }

    public Collection<Communication> getReceivedCommunications() {
        Map<Integer, Communication> communications = new TreeMap<>();
        for (Terminal terminal : _terminals.values()) {
            communications.putAll(terminal.getReceivedCommunications());
        }
        return communications.values();
    }

    public void tryLevelUp() {
        _level.tryForPromotion();
    }

    public void tryLevelDown() {
        _level.tryForDemotion();
    }

    public abstract class Level implements Serializable {
        private static final long serialVersionUID = 202210121157L;

        public abstract String getLevelName();

        public Client getClient() {
            return Client.this;
        }

        public float getBalance() {
            return Client.this.getPayments() - Client.this.getDebts();
        }

        protected void setLevel(Level level) {
            Client.this._level = level;
        }

        abstract public void tryForPromotion();

        abstract public void tryForDemotion();

    }

    // get inAppNotifications
    public Queue<Notification> getInAppNotifications() {
        return inAppNotifications;
    }

    @Override
    public String toString() {
        return String.format("CLIENT|%s|%s|%d|%s|%s|%d|%d|%d", _key, _name, _taxId,
                _level.getLevelName(), _notifiable ? "YES" : "NO",
                numberOfTerminals(), Math.round(getPayments()),
                Math.round(getDebts()));
    }

    public Collection<Notification> readInAppNotifications() {
        Collection<Notification> notifications = new LinkedList<>(
                this.inAppNotifications);
        this.inAppNotifications.clear();
        return notifications;
    }

    @Override
    public void notify(Notification notification) {
        notificationDeliveryMethod.deliver(notification);
    }

    @Override
    public void setNotificationDeliveryMethod(
            NotificationDeliveryMethod deliveryMethod) {
        this.notificationDeliveryMethod = deliveryMethod;
    }

}
