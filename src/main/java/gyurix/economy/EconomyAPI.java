package gyurix.economy;

import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class EconomyAPI {
    public static HashMap<String, BalanceData> balanceTypes = new HashMap();
    @ConfigSerialization.ConfigOptions(comment = "Migrate all the Economy data through Vault from an other Economy plugin, i.e. Essentials.")
    public static boolean migrate;
    @ConfigSerialization.ConfigOptions(comment = "The type of SpigotLibs vault hook, available options:\n" +
            "NONE - do NOT hook to Vault at all (not suggested)\n" +
            "USER - hook to Vault as an Economy user (suggested if you don't want to use SpigotLibs Economy management)\n" +
            "PROVIDER - hook to Vault as an Economy provider (override other Economy plugins, like Essentials)")
    public static VaultHookType vaultHookType;
    @ConfigSerialization.ConfigOptions(serialize = false)
    public static Economy oldEconomy;

    public static BigDecimal getBankBalance(String bank) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return new BigDecimal(oldEconomy.bankBalance(bank).balance);
            return (BigDecimal) SU.pf.get("bankbalance." + bank + ".default", BigDecimal.class);
        } catch (Throwable e) {
            System.err.println("Error on getting default balance of bank " + bank);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBankBalance(String bank, String balanceType) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return new BigDecimal(oldEconomy.bankBalance(bank).balance);
            return (BigDecimal) SU.pf.get("bankbalance." + bank + "." + balanceType, BigDecimal.class);
        } catch (Throwable e) {
            System.err.println("Error on getting " + balanceType + " balance of bank " + bank);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBalance(UUID plr) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return new BigDecimal(oldEconomy.getBalance(Bukkit.getOfflinePlayer(plr)));
            return (BigDecimal) SU.getPlayerConfig(plr).get("balance.default", BigDecimal.class);
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBalance(UUID plr, String balanceType) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return new BigDecimal(oldEconomy.getBalance(Bukkit.getOfflinePlayer(plr)));
            BigDecimal bal = (BigDecimal) SU.getPlayerConfig(plr).get("balance." + balanceType, BigDecimal.class);
            if (bal == null) {
                bal = EconomyAPI.balanceTypes.get(balanceType).defaultValue;
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, bal);
            }
            return bal;
        } catch (Throwable e) {
            System.err.println("Error on getting " + balanceType + " balance of player " + plr);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    private static boolean vaultSet(UUID id, BigDecimal bal) {
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(id);
            double now = oldEconomy.getBalance(p);
            double dif = bal.doubleValue() - now;
            if (dif > 0) {
                return oldEconomy.depositPlayer(p, dif).transactionSuccess();
            } else if (dif < 0) {
                return oldEconomy.withdrawPlayer(p, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            System.err.println("Error on setting default balance of player " + id + " to " + bal + " in economy " + oldEconomy.getName() + ".");
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    private static boolean vaultSetBank(String bank, BigDecimal bal) {
        try {
            double now = getBankBalance(bank).doubleValue();
            double dif = bal.doubleValue() - now;
            if (dif > 0) {
                return oldEconomy.depositPlayer(bank, dif).transactionSuccess();
            } else if (dif < 0) {
                return oldEconomy.withdrawPlayer(bank, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            System.err.println("Error on setting default balance of bank " + bank + " to " + bal + " in economy " + oldEconomy.getName() + ".");
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean setBalance(UUID plr, BigDecimal balance) {
        try {
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, EconomyAPI.getBalance(plr), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (vaultHookType == VaultHookType.USER)
                    return vaultSet(plr, balance);
                SU.getPlayerConfig(plr).setObject("balance.default", balance);
                return true;
            }
        } catch (Throwable e) {
            System.err.println("Error on setting default balance of player " + plr + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
        }
        return false;
    }

    public static boolean setBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, EconomyAPI.getBalance(plr), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (vaultHookType == VaultHookType.USER)
                    return vaultSet(plr, balance);
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, balance);
                return true;
            }
        } catch (Throwable e) {
            System.err.println("Error on setting " + balanceType + " balance of player " + plr + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
        }
        return false;
    }

    public static boolean setBankBalance(String bank, BigDecimal balance) {
        try {
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, EconomyAPI.getBankBalance(bank), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (vaultHookType == VaultHookType.USER)
                    return vaultSetBank(bank, balance);
                SU.pf.setObject("bankbalance." + bank + ".default", balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            System.err.println("Error on setting default balance of bank " + bank + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean setBankBalance(String bank, String balanceType, BigDecimal balance) {
        try {
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, EconomyAPI.getBankBalance(bank), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (vaultHookType == VaultHookType.USER)
                    return vaultSetBank(bank, balance);
                SU.pf.setObject("bankbalance." + bank + "." + balanceType, balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            System.err.println("Error on setting " + balanceType + " balance of bank " + bank + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean addBalance(UUID plr, BigDecimal balance) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return oldEconomy.depositPlayer(Bukkit.getOfflinePlayer(plr), balance.doubleValue()).transactionSuccess();
            BigDecimal bd = EconomyAPI.getBalance(plr).add(balance);
            if (bd.compareTo(new BigDecimal(0)) < 0) {
                return false;
            }
            return EconomyAPI.setBalance(plr, bd);
        } catch (Throwable e) {
            System.err.println("Error on adding " + balance + " default balance to player " + plr + ".");
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean addBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            if (vaultHookType == VaultHookType.USER)
                return oldEconomy.depositPlayer(Bukkit.getOfflinePlayer(plr), balance.doubleValue()).transactionSuccess();
            BigDecimal bd = EconomyAPI.getBalance(plr, balanceType).add(balance);
            if (bd.compareTo(new BigDecimal(0)) < 0) {
                return false;
            }
            return EconomyAPI.setBalance(plr, balanceType, bd);
        } catch (Throwable e) {
            System.err.println("Error on adding " + balance + " " + balanceType + " balance to player " + plr + ".");
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean addBankBalance(String bank, BigDecimal balance) {
        return EconomyAPI.setBankBalance(bank, EconomyAPI.getBankBalance(bank).add(balance));
    }

    public static boolean addBankBalance(String bank, String balanceType, BigDecimal balance) {
        return EconomyAPI.setBankBalance(bank, balanceType, EconomyAPI.getBankBalance(bank, balanceType).add(balance));
    }

    public static boolean sendBalance(UUID sender, UUID receiver, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!EconomyAPI.addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        EconomyAPI.addBalance(receiver, balance);
        return true;
    }

    public static boolean sendBalance(UUID sender, UUID receiver, String balanceType, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!EconomyAPI.addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        EconomyAPI.addBalance(receiver, balanceType, balance);
        return true;
    }

    public static boolean sendBalanceToBank(UUID sender, String bank, BigDecimal balance) {
        if (!EconomyAPI.addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        EconomyAPI.addBankBalance(bank, balance);
        return true;
    }

    public static boolean sendBalanceToBank(UUID sender, String bank, String balanceType, BigDecimal balance) {
        if (!EconomyAPI.addBalance(sender, balanceType, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        EconomyAPI.addBankBalance(bank, balanceType, balance);
        return true;
    }

    public enum VaultHookType {NONE, USER, PROVIDER}

    public static class BalanceData {
        public String prefix = "";
        public String name = "";
        public String suffix = "";
        public BigDecimal defaultValue;

        public BalanceData() {
        }

        public BalanceData(String prefix, String name, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.name = name;
        }

        public BalanceData(String prefix, String name, String suffix, BigDecimal defaultValue) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public String format(BigDecimal amount) {
            return this.prefix + amount + this.suffix;
        }
    }

}

