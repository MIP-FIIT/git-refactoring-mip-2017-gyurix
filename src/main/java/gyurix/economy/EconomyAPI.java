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

public class EconomyAPI {
    public static boolean liveSync = true;
    public static HashMap<String, HashMap<String, BigDecimal>> banks = new HashMap();
    public static HashMap<String, BalanceData> balanceTypes = new HashMap();
    public static boolean vaultHook = true;

    public static BigDecimal getBankBalance(String bank) {
        try {
            return banks.get(bank).get("default");
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBankBalance(String bank, String balanceType) {
        try {
            return banks.get(bank).get(balanceType);
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBalance(UUID plr) {
        try {
            BigDecimal bal = (BigDecimal) SU.getPlayerConfig(plr).get("balance.default", BigDecimal.class);
            if (bal != null) {
                return bal;
            }
            if (liveSync) {
                liveSync = false;
                OfflinePlayer op = Bukkit.getOfflinePlayer(plr);
                for (RegisteredServiceProvider p : Bukkit.getServicesManager().getRegistrations(Economy.class)) {
                    if (p.getPlugin() == Main.pl) continue;
                    bal = new BigDecimal(((Economy) p.getProvider()).getBalance(op));
                    SU.getPlayerConfig(plr).setObject("balance.default", bal);
                }
                liveSync = true;
            }
            if (bal == null) {
                bal = EconomyAPI.balanceTypes.get("default").defaultValue;
                EconomyAPI.setBalance(plr, bal);
            }
            return bal;
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static BigDecimal getBalance(UUID plr, String balanceType) {
        try {
            BigDecimal bal = (BigDecimal) SU.getPlayerConfig(plr).get("balance." + balanceType, BigDecimal.class);
            if (bal == null) {
                bal = EconomyAPI.balanceTypes.get(balanceType).defaultValue;
                SU.getPlayerConfig(plr).setObject("balance.default", bal);
            }
            return bal;
        } catch (Throwable e) {
            return new BigDecimal(0);
        }
    }

    public static boolean setBalance(UUID plr, BigDecimal balance) {
        BalanceUpdateEvent e = new BalanceUpdateEvent(plr, EconomyAPI.getBalance(plr), balance, balanceTypes.get("default"));
        SU.pm.callEvent(e);
        if (!e.isCancelled()) {
            SU.getPlayerConfig(plr).setObject("balance.default", balance);
        }
        return !e.isCancelled();
    }

    public static boolean setBalance(UUID plr, String balanceType, BigDecimal balance) {
        BalanceUpdateEvent e = new BalanceUpdateEvent(plr, EconomyAPI.getBalance(plr, balanceType), balance, balanceTypes.get(balanceType));
        SU.pm.callEvent(e);
        if (!e.isCancelled()) {
            SU.getPlayerConfig(plr).setObject("balance." + balanceType, balance);
        }
        return !e.isCancelled();
    }

    public static boolean setBankBalance(String bank, BigDecimal balance) {
        BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, EconomyAPI.getBankBalance(bank), balance, balanceTypes.get("default"));
        SU.pm.callEvent(e);
        if (!e.isCancelled()) {
            banks.get(bank).put("default", balance);
        }
        return !e.isCancelled();
    }

    public static boolean setBankBalance(String bank, String balanceType, BigDecimal balance) {
        BankBalanceUpdateEvent e = new BankBalanceUpdateEvent(bank, EconomyAPI.getBankBalance(bank, balanceType), balance, balanceTypes.get(balanceType));
        SU.pm.callEvent(e);
        if (!e.isCancelled()) {
            banks.get(bank).put(balanceType, balance);
        }
        return !e.isCancelled();
    }

    public static boolean addBalance(UUID plr, BigDecimal balance) {
        BigDecimal bd = EconomyAPI.getBalance(plr).add(balance);
        if (bd.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        return EconomyAPI.setBalance(plr, bd);
    }

    public static boolean addBalance(UUID plr, String balanceType, BigDecimal balance) {
        BigDecimal bd = EconomyAPI.getBalance(plr, balanceType).add(balance);
        if (bd.compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        return EconomyAPI.setBalance(plr, balanceType, bd);
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

