package com.bekvon.bukkit.residence.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.currency.Currency;

public class GemsEconomyAdapter implements EconomyInterface {

	GemsEconomyAPI api = new GemsEconomyAPI();
	GemsEconomy p = null;
	Currency t = null;
	String currencyName = null;

    public GemsEconomyAdapter(GemsEconomy p, String currency) {
    	this.p = p;
    	this.api = new GemsEconomyAPI();
    	this.t = api.getCurrency(currency);
    	this.currencyName = currency;
    }

    @Override
    public double getBalance(Player player) {
	return api.getBalance(player.identity().uuid(),this.t);
    }
    
    @Override
    public double getBalance(String playerName) {
    OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
    return api.getBalance(p.getPlayer().getUniqueId(),this.t);
    }

    @Override
    public boolean canAfford(String playerName, double amount) {
	double holdings = this.getBalance(playerName);
	if (holdings >= amount) {
	    return true;
	}
	return false;
    }

    @Override
    public boolean add(String playerName, double amount) {
    OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
	api.deposit(p.getPlayer().getUniqueId(), amount, t);
	return true;
    }

    @Override
    public boolean subtract(String playerName, double amount) {
	if (this.canAfford(playerName, amount)) {
	    OfflinePlayer p = Bukkit.getOfflinePlayer(playerName);
	    api.withdraw(p.getPlayer().getUniqueId(), amount, t);
	    return true;
	}
	return false;
    }

    @Override
    public boolean transfer(String playerFrom, String playerTo, double amount) {
	if (this.canAfford(playerFrom, amount)) {
	    subtract(playerFrom,amount);
	    add(playerTo,amount);
	    return true;
	}
	return false;
    }

    private static void checkExist(String playerName) {

    }

    @Override
    public String getName() {
	return this.currencyName;
    }
    

    @Override
    public String format(double amount) {
	return Double.toString(amount);
    }

}
