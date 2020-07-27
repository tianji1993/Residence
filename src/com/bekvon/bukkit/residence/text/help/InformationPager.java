package com.bekvon.bukkit.residence.text.help;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.CMIChatColor;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.rent.RentableLand;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
import com.bekvon.bukkit.residence.utils.GetTime;

public class InformationPager {
    Residence plugin;

    public InformationPager(Residence plugin) {
	this.plugin = plugin;
    }

    public void printInfo(CommandSender sender, String command, String title, String[] lines, int page) {
	printInfo(sender, command, title, Arrays.asList(lines), page);
    }

    public void printInfo(CommandSender sender, String command, String title, List<String> lines, int page) {

	PageInfo pi = new PageInfo(6, lines.size(), page);

	if (!pi.isPageOk()) {
	    sender.sendMessage(ChatColor.RED + plugin.msg(lm.Invalid_Page));
	    return;
	}
	plugin.msg(sender, lm.InformationPage_TopSingle, title);
	plugin.msg(sender, lm.InformationPage_Page, plugin.msg(lm.General_GenericPages, String.format("%d", page),
	    pi.getTotalPages(), lines.size()));
	for (int i = pi.getStart(); i <= pi.getEnd(); i++) {
	    if (lines.size() > i)
		sender.sendMessage(ChatColor.GREEN + lines.get(i));
	}

	plugin.getInfoPageManager().ShowPagination(sender, pi, command);
    }

    public void printListInfo(CommandSender sender, String targetPlayer, TreeMap<String, ClaimedResidence> ownedResidences, int page, boolean resadmin) {

	int perPage = 20;
	if (sender instanceof Player)
	    perPage = 6;

	if (ownedResidences.isEmpty()) {
	    plugin.msg(sender, lm.Residence_DontOwn, targetPlayer);
	    return;
	}

	PageInfo pi = new PageInfo(perPage, ownedResidences.size(), page);

	if (!(sender instanceof Player) && page == -1) {
	    printListWithDelay(sender, ownedResidences, 0, resadmin);
	    return;
	}
	if (!(sender instanceof Player) && page == -2) {
	    printListToFile(ownedResidences, resadmin);
	    return;
	}

	if (!pi.isPageOk()) {
	    sender.sendMessage(ChatColor.RED + plugin.msg(lm.Invalid_Page));
	    return;
	}

	if (targetPlayer != null)
	    plugin.msg(sender, lm.InformationPage_Top, plugin.msg(lm.General_Residences), targetPlayer);
//	plugin.msg(sender, lm.InformationPage_Page, plugin.msg(lm.General_GenericPages, String.format("%d", page),  pi.getTotalPages(), ownedResidences.size()));

	String cmd = "res";
	if (resadmin)
	    cmd = "resadmin";

	int y = -1;

	for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
	    y++;
	    if (y > pi.getEnd())
		break;
	    if (!pi.isInRange(y))
		continue;

	    ClaimedResidence res = resT.getValue();
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(plugin.msg(lm.General_Owner, res.getOwner()));

	    if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
		StringB.append("\n");
		CuboidArea area = res.getAreaArray()[0];
		String cord1 = plugin.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
		String cord2 = plugin.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
		String worldInfo = CMIChatColor.translate(plugin.msg(lm.General_CoordsLiner, cord1, cord2));
		StringB.append(worldInfo);
	    }

	    StringB.append("\n").append(plugin.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + plugin.msg(lm.Residence_IsRented);
		    StringB.append("\n").append(plugin.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + plugin.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n").append(plugin.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n").append(plugin.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n").append(plugin.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n").append(plugin.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + plugin.msg(lm.Residence_IsForSale);
		StringB.append("\n " + plugin.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String tpFlag = "";
	    String moveFlag = "";
	    if (sender instanceof Player && !res.isOwner(sender)) {
		tpFlag = res.getPermissions().playerHas((Player) sender, Flags.tp, true) ? plugin.msg(lm.General_AllowedTeleportIcon) : plugin.msg(lm.General_BlockedTeleportIcon);
		moveFlag = res.getPermissions().playerHas(sender.getName(), Flags.move, true) ? plugin.msg(lm.General_AllowedMovementIcon) : plugin.msg(lm.General_BlockedMovementIcon);
	    }

	    String msg = plugin.msg(lm.Residence_ResList, y + 1, res.getName(), res.getWorld(), tpFlag + moveFlag, ExtraString);

	    RawMessage rm = new RawMessage();
	    if (sender instanceof Player)
		rm.addText(msg).addHover(StringB.toString()).addCommand(cmd + " tp " + res.getName());
	    else
		rm.addText(msg + " " + StringB.toString().replace("\n", ""));

	    rm.show(sender);
	}

	if (targetPlayer != null)
	    ShowPagination(sender, pi, cmd + " list " + targetPlayer);
	else
	    ShowPagination(sender, pi, cmd + " listall");
    }

    private void printListWithDelay(final CommandSender sender, final TreeMap<String, ClaimedResidence> ownedResidences, final int start, final boolean resadmin) {

	int i = start;
	int y = 0;
	for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
	    y++;
	    if (y < i)
		continue;
	    i++;
	    if (i >= start + 100)
		break;
	    if (ownedResidences.size() < i)
		break;

	    ClaimedResidence res = resT.getValue();
	    StringBuilder StringB = new StringBuilder();
	    StringB.append(plugin.msg(lm.General_Owner, res.getOwner()));

	    if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
		CuboidArea area = res.getAreaArray()[0];
		String cord1 = plugin.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
		String cord2 = plugin.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
		String worldInfo = CMIChatColor.translate(plugin.msg(lm.General_CoordsLiner, cord1, cord2));
		StringB.append("\n" + worldInfo);
	    }

	    StringB.append("\n" + plugin.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

	    String ExtraString = "";
	    if (res.isForRent()) {
		if (res.isRented()) {
		    ExtraString = " " + plugin.msg(lm.Residence_IsRented);
		    StringB.append("\n " + plugin.msg(lm.Residence_RentedBy, res.getRentedLand().player));
		} else {
		    ExtraString = " " + plugin.msg(lm.Residence_IsForRent);
		}
		RentableLand rentable = res.getRentable();
		StringB.append("\n" + plugin.msg(lm.General_Cost, rentable.cost, rentable.days));
		StringB.append("\n" + plugin.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
		StringB.append("\n" + plugin.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
		StringB.append("\n" + plugin.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
	    }

	    if (res.isForSell()) {
		ExtraString = " " + plugin.msg(lm.Residence_IsForSale);
		StringB.append("\n" + plugin.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
	    }

	    String msg = plugin.msg(lm.Residence_ResList, i, res.getName(), res.getWorld(), "", ExtraString);

	    msg = CMIChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
	    msg = msg.replaceAll("\\s{2}", " ");
	    sender.sendMessage(msg);
	}

	if (ownedResidences.isEmpty()) {
	    return;
	}

	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    @Override
	    public void run() {
		printListWithDelay(sender, ownedResidences, start + 100, resadmin);
		return;
	    }
	}, 5L);

    }

    private void printListToFile(final TreeMap<String, ClaimedResidence> ownedResidences, final boolean resadmin) {

	Bukkit.getConsoleSender().sendMessage("Saving");
	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
	    @Override
	    public void run() {
		int y = 0;
		final StringBuilder sb = new StringBuilder();
		for (Entry<String, ClaimedResidence> resT : ownedResidences.entrySet()) {
		    y++;
		    if (ownedResidences.size() < y)
			break;

		    ClaimedResidence res = resT.getValue();
		    StringBuilder StringB = new StringBuilder();
		    StringB.append(" " + plugin.msg(lm.General_Owner, res.getOwner()));

		    if (res.getAreaArray().length > 0 && (res.getPermissions().has(Flags.hidden, FlagCombo.FalseOrNone) && res.getPermissions().has(Flags.coords, FlagCombo.TrueOrNone) || resadmin)) {
			CuboidArea area = res.getAreaArray()[0];
			String cord1 = plugin.msg(lm.General_CoordsTop, area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
			String cord2 = plugin.msg(lm.General_CoordsBottom, area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
			String worldInfo = CMIChatColor.translate(plugin.msg(lm.General_CoordsLiner, cord1, cord2));
			StringB.append("\n" + worldInfo);
		    }

		    StringB.append("\n " + plugin.msg(lm.General_CreatedOn, GetTime.getTime(res.getCreateTime())));

		    String ExtraString = "";
		    if (res.isForRent()) {
			if (res.isRented()) {
			    ExtraString = " " + plugin.msg(lm.Residence_IsRented);
			    StringB.append("\n " + plugin.msg(lm.Residence_RentedBy, res.getRentedLand().player));
			} else {
			    ExtraString = " " + plugin.msg(lm.Residence_IsForRent);
			}
			RentableLand rentable = res.getRentable();
			StringB.append("\n " + plugin.msg(lm.General_Cost, rentable.cost, rentable.days));
			StringB.append("\n " + plugin.msg(lm.Rentable_AllowRenewing, rentable.AllowRenewing));
			StringB.append("\n " + plugin.msg(lm.Rentable_StayInMarket, rentable.StayInMarket));
			StringB.append("\n " + plugin.msg(lm.Rentable_AllowAutoPay, rentable.AllowAutoPay));
		    }

		    if (res.isForSell()) {
			ExtraString = " " + plugin.msg(lm.Residence_IsForSale);
			StringB.append("\n " + plugin.msg(lm.Economy_LandForSale) + " " + res.getSellPrice());
		    }

		    String msg = plugin.msg(lm.Residence_ResList, y, res.getName(), res.getWorld(), "", ExtraString);

		    msg = CMIChatColor.stripColor(msg + " " + StringB.toString().replace("\n", ""));
		    msg = msg.replaceAll("\\s{2}", " ");

		    sb.append(msg);
		    sb.append(" \n");
//	    sender.sendMessage(msg);
		}

		File BackupDir = new File(Residence.getInstance().getDataLocation(), "FullLists");
		if (!BackupDir.isDirectory())
		    BackupDir.mkdir();
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		File file = new File(BackupDir, dateFormat.format(date) + ".txt");
		try {
		    FileUtils.writeStringToFile(file, sb.toString(), "UTF-8");
		} catch (IOException e) {
		    e.printStackTrace();
		}
		Bukkit.getConsoleSender().sendMessage("Saved file to FullLists folder with " + file.getName() + " name");
		return;
	    }
	});
    }

    public void ShowPagination(CommandSender sender, PageInfo pi, String cmd) {
	ShowPagination(sender, pi, cmd, null);
    }

    public void ShowPagination(CommandSender sender, PageInfo pi, Object cmd, String pagePref) {
	ShowPagination(sender, pi.getTotalPages(), pi.getCurrentPage(), pi.getTotalEntries(), plugin.getCommandManager().getLabel() + " " + cmd.getClass().getSimpleName(), pagePref);
    }

    public void ShowPagination(CommandSender sender, PageInfo pi, String cmd, String pagePref) {
	ShowPagination(sender, pi.getTotalPages(), pi.getCurrentPage(), pi.getTotalEntries(), cmd, pagePref);
    }

    public void ShowPagination(CommandSender sender, int pageCount, int CurrentPage, int totalEntries, String cmd, String pagePref) {
	if (!(sender instanceof Player))
	    return;
	if (!cmd.startsWith("/"))
	    cmd = "/" + cmd;

	if (pageCount == 1)
	    return;

	String pagePrefix = pagePref == null ? "" : pagePref;

	int NextPage = CurrentPage + 1;
	NextPage = CurrentPage < pageCount ? NextPage : CurrentPage;
	int Prevpage = CurrentPage - 1;
	Prevpage = CurrentPage > 1 ? Prevpage : CurrentPage;

	RawMessage rm = new RawMessage();
	rm.addText((CurrentPage > 1 ? plugin.msg(lm.General_prevPage) : plugin.msg(lm.General_prevPageOff))).addHover(
	    CurrentPage > 1 ? plugin.msg(lm.General_prevPageHover) : plugin.msg(lm.General_lastPageHover)).addCommand(
		CurrentPage > 1 ? cmd + " " + pagePrefix + Prevpage : cmd + " " + pagePrefix + pageCount);
	rm.addText(plugin.msg(lm.General_pageCount, CurrentPage, pageCount)).addHover(plugin.msg(lm.General_pageCountHover, totalEntries));
	rm.addText(plugin.msg(pageCount > CurrentPage ? lm.General_nextPage : lm.General_nextPageOff)).addHover(
	    pageCount > CurrentPage ? plugin.msg(lm.General_nextPageHover) : plugin.msg(lm.General_firstPageHover)).addCommand(
	    pageCount > CurrentPage ? cmd + " " + pagePrefix + NextPage : cmd + " " + pagePrefix + 1);
	if (pageCount != 0)
	    rm.show(sender);
    }
}
