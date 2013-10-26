package com.darktidegames.celeo;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;

import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.CalculableType;

/**
 * Moderators and Admins are granted an additional permissions group. Guides
 * simply get their guide title.
 * 
 * @author Celeo
 */
public class DarkRoles extends JavaPlugin implements Listener
{

	private Map<Player, StaffType> staff = new HashMap<Player, StaffType>();
	private final String world = "world";
	private final CalculableType type = CalculableType.USER;
	private Essentials ess;

	@Override
	public void onEnable()
	{
		Plugin test = getServer().getPluginManager().getPlugin("Essentials");
		if (test != null)
			ess = (Essentials) test;
		else
			getLogger().warning("Cannot hook into Essentials!");
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("Enabled");
	}

	@Override
	public void onDisable()
	{
		for (Player player : staff.keySet())
			deStaff(player);
		getLogger().info("Disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (!(sender instanceof Player))
			return false;
		Player player = (Player) sender;
		if (args == null || args.length == 0)
		{
			toggleStaff(player);
			return true;
		}
		String s = args[0].toLowerCase();
		if (s.equals("staff") || s.equals("on"))
			staff(player);
		else if (s.equals("normal") || s.equals("off"))
			deStaff(player);
		return true;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		deStaff(event.getPlayer());
	}

	@EventHandler
	public void onStaffKillOther(PlayerDeathEvent event)
	{
		Player dead = event.getEntity();
		if (dead == null)
			return;
		if (dead.getLastDamageCause() == null)
			return;
		if (dead.getLastDamageCause().getEntity() == null)
			return;
		if (dead.getLastDamageCause() instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent eveEvent = (EntityDamageByEntityEvent) dead.getLastDamageCause();
			if (eveEvent.getDamager() instanceof Player)
			{
				Player killer = (Player) ((EntityDamageByEntityEvent) dead.getLastDamageCause()).getDamager();
				if (isStaff(killer) && getStaffType(dead) != null
						&& !getStaffType(dead).equals(StaffType.GUIDE))
					getLogger().info(killer.getName() + " killed "
							+ dead.getName() + " while having staff powers on!");
			}
		}
		if (isStaff(dead) && !getStaffType(dead).equals(StaffType.GUIDE))
			getLogger().warning(dead.getName()
					+ " died while having staff powers on!");
	}

	private void toggleStaff(Player player)
	{
		if (isStaff(player))
			deStaff(player);
		else
			staff(player);
	}

	private void staff(Player player)
	{
		if (player.hasPermission("sudo.admin"))
		{
			ApiLayer.addGroup(world, type, player.getName(), "admin");
			player.sendMessage("§6Entered administrator group");
			staff.put(player, StaffType.ADMIN);
			player.setPlayerListName("§4" + player.getName());
		}
		else if (player.hasPermission("sudo.mod"))
		{
			ApiLayer.addGroup(world, type, player.getName(), "moderator");
			player.sendMessage("§6Entered moderator group");
			staff.put(player, StaffType.MODERATOR);
			player.setPlayerListName("§2" + player.getName());
		}
		else if (player.hasPermission("sudo.guide"))
		{
			ApiLayer.addGroup(world, type, player.getName(), "guide");
			player.sendMessage("§6Entered guide group");
			staff.put(player, StaffType.GUIDE);
			player.setPlayerListName("§3" + player.getName());
		}
		else
			player.sendMessage("§cYou cannot use that");
		ApiLayer.update();
	}

	private void deStaff(Player player)
	{
		if (!isStaff(player))
			return;
		if (ess != null)
		{
			if (ess.getVanishedPlayers().contains(player.getName()))
				player.chat("/vanish");
			if (ess.getUserMap().getUser(player.getName()).isGodModeEnabled())
				player.chat("/tgm");
		}
		player.setPlayerListName(player.getName());
		ApiLayer.removeGroup(world, type, player.getName(), getStaffType(player).toString().toLowerCase());
		ApiLayer.update();
		staff.remove(player);
		player.sendMessage("§6You have left your staff group");
	}

	/**
	 * GUIDE, MODERATOR, ADMIN
	 * 
	 * @author Celeo
	 */
	public enum StaffType
	{
		GUIDE, MODERATOR, ADMIN;
	}

	public boolean isStaff(Player player)
	{
		return staff.keySet().contains(player);
	}

	/**
	 * 
	 * @param player
	 *            Player
	 * @return <b>StaffType</b> of player: GUIDE, MODERATOR, ADMIN, or null
	 */
	public StaffType getStaffType(Player player)
	{
		return staff.get(player);
	}

	public Map<Player, StaffType> getAllStaff()
	{
		return staff;
	}

}