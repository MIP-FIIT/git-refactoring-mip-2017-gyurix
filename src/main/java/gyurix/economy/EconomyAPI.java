package gyurix.economy;

import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.*;
import org.bukkit.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

public class EconomyAPI {
    public static HashMap<String, BalanceData> balanceTypes = new HashMap<>();
    @ConfigOptions(comment = "Migrate all the Economy data through Vault from an other Economy plugin, i.e. Essentials.")
    public static boolean migrate;
    @ConfigOptions(comment = "The type of SpigotLibs vault hook, available options:\n" +
            "NONE - do NOT hook to Vault at all (not suggested)\n" +
            "USER - hook to Vault as an Economy user (suggested if you don't want to use SpigotLibs Economy management)\n" +
            "PROVIDER - hook to Vault as an Economy provider (override other Economy plugins, like Essentials)")
    public static VaultHookType vaultHookType;

    public static boolean addBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return addBalance(plr, balance);
            BigDecimal bd = getBalance(plr, balanceType).add(balance);
            if (bd.compareTo(new BigDecimal(0)) < 0)
                return false;
            return setBalance(plr, balanceType, bd);
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on adding " + balance + ' ' + balanceType + " balance to player " + plr + '.');
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean addBankBalance(String bank, BigDecimal balance) {
        return setBankBalance(bank, getBankBalance(bank).add(balance));
    }

    public static boolean addBankBalance(String bank, String balanceType, BigDecimal balance) {
        return setBankBalance(bank, balanceType, getBankBalance(bank, balanceType).add(balance));
    }

    public static BigDecimal getBalance(UUID plr, String balanceType) {
        try {
            if (balanceType.equals("default"))
                return getBalance(plr);
            BigDecimal bal = SU.getPlayerConfig(plr).get("balance." + balanceType, BigDecimal.class);
            if (bal == null) {
                bal = balanceTypes.get(balanceType).defaultValue;
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, bal);
            }
            return bal;
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on getting " + balanceType + " balance of player " + plr);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBankBalance(String bank) {
        try {
            if (useVault())
                return new BigDecimal(SU.econ.bankBalance(bank).balance);
            return SU.pf.get("bankbalance." + bank + ".default", BigDecimal.class);
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on getting default balance of bank " + bank);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBankBalance(String bank, String balanceType) {
        try {
            if (useVault())
                return new BigDecimal(SU.econ.bankBalance(bank).balance);
            return SU.pf.get("bankbalance." + bank + '.' + balanceType, BigDecimal.class);
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on getting " + balanceType + " balance of bank " + bank);
            if (Config.debug)
                e.printStackTrace();
            return new BigDecimal(0);
        }
    }

    public static boolean sendBalance(UUID sender, UUID receiver, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBalance(receiver, balance);
        return true;
    }

    public static boolean addBalance(UUID plr, BigDecimal balance) {
        try {
            if (useVault()) {
                if (balance.compareTo(new BigDecimal(0)) < 0)
                    return SU.econ.withdrawPlayer(Bukkit.getOfflinePlayer(plr), -balance.doubleValue()).transactionSuccess();
                return SU.econ.depositPlayer(Bukkit.getOfflinePlayer(plr), balance.doubleValue()).transactionSuccess();
            }
            BigDecimal bd = getBalance(plr).add(balance);
            if (bd.compareTo(new BigDecimal(0)) < 0)
                return false;
            return setBalance(plr, bd);
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on adding " + balance + " default balance to player " + plr + '.');
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean useVault() {
        return vaultHookType == VaultHookType.USER && SU.vault && SU.econ != null;
    }

    public static BigDecimal getBalance(UUID plr) {
        try {
            if (useVault())
                return new BigDecimal(SU.econ.getBalance(Bukkit.getOfflinePlayer(plr)));
            return SU.getPlayerConfig(plr).get("balance.default", BigDecimal.class);
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static boolean setBalance(UUID plr, BigDecimal balance) {
        try {
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, getBalance(plr), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (useVault())
                    return vaultSet(plr, balance);
                SU.getPlayerConfig(plr).setObject("balance.default", balance);
                return true;
            }
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting default balance of player " + plr + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
        }
        return false;
    }

    private static boolean vaultSet(UUID id, BigDecimal bal) {
        try {
            OfflinePlayer p = Bukkit.getOfflinePlayer(id);
            double now = SU.econ.getBalance(p);
            double dif = bal.doubleValue() - now;
            if (dif > 0) {
                return SU.econ.depositPlayer(p, dif).transactionSuccess();
            } else if (dif < 0) {
                return SU.econ.withdrawPlayer(p, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting default balance of player " + id + " to " + bal + " in economy " + SU.econ.getName() + '.');
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean sendBalance(UUID sender, UUID receiver, String balanceType, BigDecimal balance) {
        if (balance.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBalance(receiver, balanceType, balance);
        return true;
    }

    public static boolean sendBalanceToBank(UUID sender, String bank, BigDecimal balance) {
        if (!addBalance(sender, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBankBalance(bank, balance);
        return true;
    }

    public static boolean sendBalanceToBank(UUID sender, String bank, String balanceType, BigDecimal balance) {
        if (!addBalance(sender, balanceType, new BigDecimal(0).subtract(balance))) {
            return false;
        }
        addBankBalance(bank, balanceType, balance);
        return true;
    }

    public static boolean setBalance(UUID plr, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return setBalance(plr, balance);
            BalanceUpdateEvent e = new BalanceUpdateEvent(plr, getBalance(plr, balanceType), balance, balanceTypes.get(balanceType));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                SU.getPlayerConfig(plr).setObject("balance." + balanceType, balance);
                return true;
            }
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting " + balanceType + " balance of player " + plr + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
        }
        return false;
    }

    public static boolean setBankBalance(String bank, BigDecimal balance) {
        try {
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, getBankBalance(bank), balance, balanceTypes.get("default"));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                if (useVault())
                    return vaultSetBank(bank, balance);
                SU.pf.setObject("bankbalance." + bank + ".default", balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting default balance of bank " + bank + " to " + balance);
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public static boolean setBankBalance(String bank, String balanceType, BigDecimal balance) {
        try {
            if (balanceType.equals("default"))
                return setBankBalance(bank, balance);
            BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, getBankBalance(bank), balance, balanceTypes.get(balanceType));
            SU.pm.callEvent(e);
            if (!e.isCancelled()) {
                SU.pf.setObject("bankbalance." + bank + '.' + balanceType, balance);
                return true;
            }
            return false;
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting " + balanceType + " balance of bank " + bank + " to " + balance);
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
                return SU.econ.bankDeposit(bank, dif).transactionSuccess();
            } else if (dif < 0) {
                return SU.econ.bankWithdraw(bank, 0 - dif).transactionSuccess();
            }
            return true;
        } catch (Throwable e) {
            SU.cs.sendMessage("§a[EconomyAPI] §cError on setting default balance of bank " + bank + " to " + bal + " in economy " + SU.econ.getName() + '.');
            if (Config.debug)
                e.printStackTrace();
            return false;
        }
    }

    public enum VaultHookType {NONE, USER, PROVIDER}

    public static class BalanceData {
        public BigDecimal defaultValue;
        public String name;
        public String prefix = "", prefixPlural;
        public String suffix = "", format, suffixPlural;
        public boolean useKMBT;

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
            if (amount == null)
                amount = new BigDecimal(0);
            boolean pl = !(amount.compareTo(new BigDecimal(-1)) >= 0 && amount.compareTo(new BigDecimal(1)) <= 0);
            String pf = prefixPlural == null ? prefix : pl ? prefixPlural : prefix;
            String sf = suffixPlural == null ? suffix : pl ? suffixPlural : suffix;
            String f = useKMBT ? getKMBT(amount) : format == null ? amount.toString() : new DecimalFormat(format).format(amount);
            return pf + f + sf;
        }

        private String getKMBT(BigDecimal amount) {
            BigDecimal t = new BigDecimal(1000_000_000_000L);
            BigDecimal b = new BigDecimal(1000_000_000L);
            BigDecimal m = new BigDecimal(1000_000L);
            BigDecimal k = new BigDecimal(1000L);
            if (amount.compareTo(t) > -1) {
                return amount.divide(t, BigDecimal.ROUND_DOWN).longValue() + "T";
            }
            if (amount.compareTo(b) > -1) {
                return amount.divide(b, BigDecimal.ROUND_DOWN).longValue() + "B";
            }
            if (amount.compareTo(m) > -1) {
                return amount.divide(m, BigDecimal.ROUND_DOWN).longValue() + "M";
            }
            if (amount.compareTo(k) > -1) {
                return amount.divide(k, BigDecimal.ROUND_DOWN).longValue() + "K";
            }
            return amount.toString();
        }
    }

}

