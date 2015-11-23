package gyurix.economy;

import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * A simple class for vault compatibility.
 */
public class EconomyVaultHook implements Economy{
    /**
     * Initializes Vault hook, DO NOT USE THIS METHOD.
     */
    public static void init(){
        Bukkit.getServicesManager().register(Economy.class, new EconomyVaultHook(), Main.pl, ServicePriority.Highest);
    }
    public boolean isEnabled() {
        return true;
    }

    public String getName() {
        return "SpigotLib - EconomyAPI";
    }

    public boolean hasBankSupport() {
        return true;
    }

    public int fractionalDigits() {
        return Integer.MAX_VALUE;
    }

    public String format(double v) {
        return EconomyAPI.balanceTypes.get("default").format(new BigDecimal(v));
    }

    public String currencyNamePlural() {
        return EconomyAPI.balanceTypes.get("default").suffix;
    }

    public String currencyNameSingular() {
        return EconomyAPI.balanceTypes.get("default").suffix;
    }

    public boolean hasAccount(String s) {
        return true;
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return true;
    }

    public boolean hasAccount(String s, String s1) {
        return true;
    }

    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return true;
    }

    public double getBalance(String s) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue();
    }

    public double getBalance(OfflinePlayer offlinePlayer) {
        UUID id=offlinePlayer.getUniqueId();
        if (id==null)
            id = SU.getUUID(offlinePlayer.getName());
        return EconomyAPI.getBalance(id).doubleValue();
    }

    public double getBalance(String s, String world) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue();
    }

    public double getBalance(OfflinePlayer offlinePlayer, String world) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue();
    }

    public boolean has(String s, double v) {
        return EconomyAPI.getBalance(SU.getUUID(s)).doubleValue()>=v;
    }

    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue()>=v;
    }

    public boolean has(String player, String world, double v) {
        return EconomyAPI.getBalance(SU.getUUID(player)).doubleValue()>=v;
    }

    public boolean has(OfflinePlayer offlinePlayer, String world, double v) {
        return EconomyAPI.getBalance(offlinePlayer.getUniqueId()).doubleValue()>=v;
    }

    public EconomyResponse withdrawPlayer(String player, double v) {
        UUID id=SU.getUUID(player);
        boolean success=EconomyAPI.addBalance(id,new BigDecimal(0-v));
        return new EconomyResponse(v,EconomyAPI.getBalance(id).doubleValue(),
                success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, success ? "Â§aSuccess." : "Â§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double v) {
        UUID id=offlinePlayer.getUniqueId();
        if (id==null)
            id=SU.getUUID(offlinePlayer.getName());
        boolean success=EconomyAPI.addBalance(id,new BigDecimal(0-v));
        return new EconomyResponse(v,EconomyAPI.getBalance(id).doubleValue(),
                success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, success ? "Â§aSuccess." : "Â§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(String player, String world, double v) {
        UUID id=SU.getUUID(player);
        boolean success=EconomyAPI.addBalance(id,new BigDecimal(0-v));
        return new EconomyResponse(v,EconomyAPI.getBalance(id).doubleValue(),
                success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, success ? "Â§aSuccess." : "Â§cNot enough money.");
    }

    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        UUID id=offlinePlayer.getUniqueId();
        if (id==null)
            id=SU.getUUID(offlinePlayer.getName());
        boolean success=EconomyAPI.addBalance(id,new BigDecimal(0-v));
        return new EconomyResponse(v,EconomyAPI.getBalance(id).doubleValue(),
                success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE, success ? "Â§aSuccess." : "Â§cNot enough money.");
    }

    public EconomyResponse depositPlayer(String player, double v) {
        return withdrawPlayer(player,0-v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double v) {
        return withdrawPlayer(offlinePlayer,0-v);
    }

    public EconomyResponse depositPlayer(String player, String world, double v) {
        return withdrawPlayer(player, v);
    }

    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String world, double v) {
        return withdrawPlayer(offlinePlayer, v);
    }

    public EconomyResponse createBank(String bankName, String s1) {
        if (EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Â§cBank Â§eÂ§l" + bankName + "Â§c already exists.");
        }
        EconomyAPI.banks.put(bankName,new HashMap<String, BigDecimal>());
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Â§2Bank Â§aÂ§l" + bankName + "Â§2 has been created successfully.");
    }

    public EconomyResponse createBank(String bankName, OfflinePlayer offlinePlayer) {
        if (EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Â§cBank Â§eÂ§l" + bankName + "Â§c already exists.");
        }
        EconomyAPI.banks.put(bankName, new HashMap<String, BigDecimal>());
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "Â§2Bank Â§aÂ§l" + bankName + "Â§2 has been created successfully.");
    }

    public EconomyResponse deleteBank(String bankName) {
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        double bal=EconomyAPI.getBankBalance(bankName).doubleValue();
        EconomyAPI.banks.remove(bankName);
        return new EconomyResponse(0, bal, EconomyResponse.ResponseType.SUCCESS, "Â§2Bank Â§aÂ§l" + bankName + "Â§2 has been removed successfully.");
    }

    public EconomyResponse bankBalance(String bankName) {
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        return new EconomyResponse(0, EconomyAPI.getBankBalance(bankName).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Â§aSuccess.");
    }

    public EconomyResponse bankHas(String bankName, double v) {
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        double bal=EconomyAPI.getBankBalance(bankName).doubleValue();
        if (v>bal){
            return new EconomyResponse(v, bal, EconomyResponse.ResponseType.FAILURE, "Â§cNot enough money.");
        }
        return new EconomyResponse(v, bal, EconomyResponse.ResponseType.SUCCESS, "Â§aSuccess.");
    }

    public EconomyResponse bankWithdraw(String bankName, double v) {
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        EconomyAPI.addBankBalance(bankName,new BigDecimal(0-v));
        return new EconomyResponse(v, EconomyAPI.getBankBalance(bankName).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Â§aSuccess.");
    }

    public EconomyResponse bankDeposit(String bankName, double v) {
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        EconomyAPI.addBankBalance(bankName,new BigDecimal(v));
        return new EconomyResponse(v, EconomyAPI.getBankBalance(bankName).doubleValue(), EconomyResponse.ResponseType.SUCCESS, "Â§aSuccess.");
    }
    public EconomyResponse bankMemberOwner(String bankName){
        if (!EconomyAPI.banks.containsKey(bankName)){
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Â§cBank Â§eÂ§l" + bankName + "Â§c doesn't exists.");
        }
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Â§cBanks doesn't have members and owners.");
    }
    public EconomyResponse isBankOwner(String bankName, String s1) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankOwner(String bankName, OfflinePlayer offlinePlayer) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankMember(String bankName, String s1) {
        return bankMemberOwner(bankName);
    }

    public EconomyResponse isBankMember(String bankName, OfflinePlayer offlinePlayer) {
        return bankMemberOwner(bankName);
    }

    public List<String> getBanks() {
        return new ArrayList<String>(EconomyAPI.banks.keySet());
    }

    public boolean createPlayerAccount(String s) {
        return false;
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
