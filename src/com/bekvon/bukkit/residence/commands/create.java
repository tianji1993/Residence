package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class create implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;
	if (args.length != 2) {
	    return false;
	}

	if (plugin.getWorldEdit() != null) {
	    if (plugin.getWorldEdit().getConfig().getInt("wand-item") == plugin.getConfigManager().getSelectionTooldID()) {
		plugin.getSelectionManager().worldEdit(player);
	    }
	}
	if (plugin.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (plugin.getWorldGuard() != null) {
		if (plugin.getWorldGuardUtil().isSelectionInRegion(player) == null) {
		    plugin.getResidenceManager().addResidence(player, args[1], plugin.getSelectionManager().getPlayerLoc1(player.getName()), plugin
			.getSelectionManager().getPlayerLoc2(player.getName()), resadmin);
		    return true;
		}
		ProtectedRegion Region = plugin.getWorldGuardUtil().isSelectionInRegion(player);
		plugin.msg(player, lm.Select_WorldGuardOverlap, Region.getId());

		Location lowLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMinimumPoint().getBlockX(),
		    Region.getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());

		Location highLoc = new Location(plugin.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMaximumPoint().getBlockX(),
		    Region.getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());

		Visualizer v = new Visualizer(player);
		v.setAreas(plugin.getSelectionManager().getSelectionCuboid(player));
		v.setErrorAreas(new CuboidArea(lowLoc, highLoc));

		plugin.getSelectionManager().showBounds(player, v);
		return true;
	    }
	    plugin.getResidenceManager().addResidence(player, args[1], plugin.getSelectionManager().getPlayerLoc1(player.getName()), plugin
		.getSelectionManager().getPlayerLoc2(player.getName()), resadmin);
	    return true;
	}
	plugin.msg(player, lm.Select_Points);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	// Main command
	c.get(path + "Description", "Create Residences");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res create <residence name>"));
    }
}
