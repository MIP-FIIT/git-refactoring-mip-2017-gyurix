package gyurix.economy;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author GyuriX
 *         <p/>
 *         An API for managing the whole servers economy system.
 *         It includes the following features:
 *         - multiple balance type
 *         - making virtual banks
 *         - managing balances of offline players
 *         - built in balance transfer success calculations
 *         - balance prefixes names and suffixes
 */
public class EconomyAPI {
    /**
     * Sync the balance from other plugins if the player don't have the default money in the SpigotLibs EconomyAPI.
     */
    public static boolean liveSync = true;
    /**
     * The map of the available banks and their balances of each balancetype
     */
    public static HashMap<String, HashMap<String, BigDecimal>> banks = new HashMap<String, HashMap<String, BigDecimal>>();
    /**
     * The map of the balance types names and their data
     */
    public static HashMap<String, BalanceData> balanceTypes = new HashMap<String, BalanceData>();
    /**
     * Toggles the hook to the Vault plugin.
     */
    public static boolean vaultHook=true;

    /**
     * Get the default typed balance of a bank
     *
     * @param bank bank name
     * @return The amount of balance in the default balancetype in the given bank.
     */
    public static BigDecimal getBankBalance(String bank) {
        try {
            return banks.get(bank).get("default");
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    /**
     * Get the given typed balance of a bank
     *
     * @param bank        bank name
     * @param balanceType type of the balance
     * @return The amount of balance in the given balancetype in the given bank.
     */
    public static BigDecimal getBankBalance(String bank, String balanceType) {
        try {
            return banks.get(bank).get(balanceType);
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    /**
     * Get the default typed balance of an offline/online player
     *
     * @param plr UUID of the player
     * @return The amount of the given players default balance.
     */
    public static BigDecimal getBalance(UUID plr) {
        try {
            BigDecimal bal = (BigDecimal) SU.getPlayerConfig(plr).get("balance.default", BigDecimal.class);
            if (bal != null)
                return bal;
            if (liveSync) {
                liveSync=false;
                OfflinePlayer op = Bukkit.getOfflinePlayer(plr);
                for (RegisteredServiceProvider<Economy> p : Bukkit.getServicesManager().getRegistrations(Economy.class)) {
                    if (p.getPlugin() != Main.pl) {
                        SU.getPlayerConfig(plr).setObject("balance.default", bal=new BigDecimal(p.getProvider().getBalance(op)));
                    }
                }
                liveSync=true;
            }
            if (bal == null)
                EconomyAPI.setBalance(plr, bal = balanceTypes.get("default").defaultValue);
            return bal;
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    /**
     * Get the given typed balance of an offline/online player
     *
     * @param plr         UUID of the player
     * @param balanceType type of the balance
     * @return The amount of the given players balance of the given balanceType.
     */
    public static BigDecimal getBalance(UUID plr, String balanceType) {
        try {
            BigDecimal bal= (BigDecimal) SU.getPlayerConfig(plr).get("balance." + balanceType, BigDecimal.class);
            if (bal == null)
                SU.getPlayerConfig(plr).setObject("balance.default", bal = balanceTypes.get(balanceType).defaultValue);
            return bal;
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    /**
     * Set the default typed balance of an offline/online player
     *
     * @param plr     UUID of the player
     * @param balance amount of the players new default balance
     * @return The success of the transfer
     */
    public static boolean setBalance(UUID plr, BigDecimal balance) {
        BalanceUpdateEvent e=new BalanceUpdateEvent(plr,getBalance(plr),balance,balanceTypes.get("default"));
        SU.pm.callEvent(e);
        if (!e.isCancelled())
            SU.getPlayerConfig(plr).setObject("balance.default", balance);
        return !e.isCancelled();
    }

    /**
     * Set the given typed balance of an offline/online player
     *
     * @param plr         UUID of the player
     * @param balanceType type of the balance
     * @param balance     amount of the players new given typed balance
     * @return The success of the transfer
     */
    public static boolean setBalance(UUID plr, String balanceType, BigDecimal balance) {
        BalanceUpdateEvent e=new BalanceUpdateEvent(plr,getBalance(plr,balanceType),balance,balanceTypes.get(balanceType));
        SU.pm.callEvent(e);
        if (!e.isCancelled())
            SU.getPlayerConfig(plr).setObject("balance." + balanceType, balance);
        return !e.isCancelled();
    }

    /**
     * Set the default typed balance of a bank
     *
     * @param bank    Target bank
     * @param balance amount of the banks new default typed balance
     * @return The success of the transfer
     */
    public static boolean setBankBalance(String bank, BigDecimal balance) {
        BankBalanceUpdateEvent e=new BankBalanceUpdateEvent(bank,getBankBalance(bank),balance,balanceTypes.get("default"));
        SU.pm.callEvent(e);
        if (!e.isCancelled())
            banks.get(bank).put("default", balance);
        return !e.isCancelled();
    }

    /**
     * Set the given typed balance of a bank
     *
     * @param bank        target bank
     * @param balanceType type of the balance
     * @param balance     amount of the banks new given typed balance
     * @return The success of the transfer
     */
    public static boolean setBankBalance(String bank, String balanceType, BigDecimal balance) {
        BankBalanceUpdateEvent e=new BankBalanceUpdateEvent(bank,getBankBalance(bank,balanceType),balance,balanceTypes.get(balanceType));
        SU.pm.callEvent(e);
        if (!e.isCancelled())
            banks.get(bank).put(balanceType, balance);
        return !e.isCancelled();
    }

    /**
     * Add or take some of the default typed balance from the player
     *
     * @param plr     UUID of the target player
     * @param balance amount of the default type balance to give (balance&gt;0) or take (balance&lt;0) from the given player
     * @return The success of the transfer, it's false, if the player doesn't have enough default typed balance
     */
    public static boolean addBalance(UUID plr, BigDecimal balance) {
        BigDecimal bd = getBalance(plr).add(balance);
        if (bd.compareTo(new BigDecimal(0)) < 0)
            return false;
        return setBalance(plr, bd);
    }

    /**
     * Add or take some of the given typed balance from the player
     *
     * @param plr         UUID of the target player
     * @param balanceType type of the given balance
     * @param balance     amount of the given typed balance to give (balance&gt;0) or take (balance&lt;0) from the given player
     * @return The success of the transfer, it's false, if the player doesn't have enough given typed balance
     */
    public static boolean addBalance(UUID plr, String balanceType, BigDecimal balance) {
        BigDecimal bd = getBalance(plr, balanceType).add(balance);
        if (bd.compareTo(new BigDecimal(0)) < 0)
            return false;
        return setBalance(plr, balanceType, bd);
    }

    /**
     * Add or take some of the default typed balance of the bank
     *
     * @param bank    target bank
     * @param balance amount of the default balance type to be added (balance&gt;0) or taken (balance&lt;0) from the given bank
     * @return The success of the transfer
     */
    public static boolean addBankBalance(String bank, BigDecimal balance) {
        return setBankBalance(bank, getBankBalance(bank).add(balance));
    }

    /**
     * Add or take some of the given typed balance of the bank
     *
     * @param bank        target bank
     * @param balanceType type of the given balance
     * @param balance     amount of the given balance type to be added (balance&gt;0) or taken (balance&lt;0) from the given bank
     * @return The success of the transfer
     */
    public static boolean addBankBalance(String bank, String balanceType, BigDecimal balance) {
        return setBankBalance(bank, balanceType, getBankBalance(bank, balanceType).add(balance));
    }

    /**
     * Send (remove from sender and give to the receiver) some of the default typed balance
     *
     * @param sender   the UUID of the balance sender player
     * @param receiver the UUID of the balance receiver player
     * @param balance  the amount of the default typed balance to be transfered from the sender to the receiver
     * @return The success of the transfer, it's false, if the balance is negative or the sender doesn't have enough default typed balance
     */
    public static boolean sendBalance(UUID sender, UUID receiver, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0)
            return false;
        if (!addBalance(sender, new BigDecimal(0).subtract(balance)))
            return false;
        addBalance(receiver, balance);
        return true;
    }

    /**
     * Send (remove from sender and give to the receiver) some of the given typed balance
     *
     * @param sender      the UUID of the balance sender player
     * @param receiver    the UUID of the balance receiver player
     * @param balanceType the type of the transferable balance
     * @param balance     the amount of the given typed balance to be transfered from the sender to the receiver
     * @return The success of the transfer, it's false, if the balance is negative or the sender doesn't have enough default typed balance
     */
    public static boolean sendBalance(UUID sender, UUID receiver, String balanceType, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0)
            return false;
        if (!addBalance(sender, new BigDecimal(0).subtract(balance)))
            return false;
        addBalance(receiver, balanceType, balance);
        return true;
    }

    /**
     * Send (remove from sender and give to the receiver) some of the default typed balance
     *
     * @param sender  the UUID of the balance sender player
     * @param bank    the name of the balance receiver bank
     * @param balance the amount of the default typed transferable money, it can also be negative, for sending balance to player from the bank
     * @return The success of the transfer, it's false if the sender doesn't have enough default typed balance
     */
    public static boolean sendBalanceToBank(UUID sender, String bank, BigDecimal balance) {
        if (!addBalance(sender, new BigDecimal(0).subtract(balance)))
            return false;
        addBankBalance(bank, balance);
        return true;

    }

    /**
     * Send (remove from sender and give to the receiver) some of the given typed balance
     *
     * @param sender      the UUID of the balance sender player
     * @param bank        the name of the balance receiver bank
     * @param balanceType the type of the transferable balance
     * @param balance     the amount of the given typed transferable money, it can also be negative, for sending balance to player from the bank
     * @return The success of the transfer, it's false if the sender doesn't have enough given typed balance
     */
    public static boolean sendBalanceToBank(UUID sender, String bank, String balanceType, BigDecimal balance) {
        if (!addBalance(sender, balanceType, new BigDecimal(0).subtract(balance)))
            return false;
        addBankBalance(bank, balanceType, balance);
        return true;
    }

    /**
     * @author GyuriX
     *         <p/>
     *         A class for storing informations about a balance type
     */
    public static class BalanceData {
        public String prefix = "", name = "", suffix = "";
        public BigDecimal defaultValue;

        /**
         * Empty constructor
         */
        public BalanceData() {

        }

        /**
         * Constructor with every parameter, but defaultValue
         *
         * @param prefix prefix of the balance type
         * @param name   name of the balance type
         * @param suffix suffix of the balance type
         */
        public BalanceData(String prefix, String name, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.name = name;
        }

        /**
         * Constructor with every parameter
         *
         * @param prefix       prefix of the balance type
         * @param name         name of the balance type
         * @param suffix       suffix of the balance type
         * @param defaultValue default value of the balance type
         */
        public BalanceData(String prefix, String name, String suffix, BigDecimal defaultValue) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.name = name;
            this.defaultValue = defaultValue;
        }

        /**
         * Formats the given amount of balance to a string containing the prefix and the suffix of the balance
         *
         * @param amount amount of the balance to be formatted
         * @return the formatted string
         */
        public String format(BigDecimal amount) {
            return prefix + amount + suffix;
        }
    }
}
