package com.earth2me.essentials;

import static com.earth2me.essentials.I18n.tl;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import net.ess3.api.IEssentials;
import net.ess3.api.InvalidWorldException;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public abstract class UserData extends PlayerExtension implements IConf
{
	protected final transient IEssentials ess;
	private final EssentialsUserConf config;
	private final File folder;

	protected UserData(Player base, IEssentials ess)
	{
		super(base);
		this.ess = ess;
		folder = new File(ess.getDataFolder(), "userdata");
		if (!folder.exists())
		{
			folder.mkdirs();
		}

		String filename;
		try
		{
			filename = base.getUniqueId().toString();
		}
		catch (Throwable ex)
		{
			ess.getLogger().warning("Falling back to old username system for " + base.getName());
			filename = base.getName();
		}

		config = new EssentialsUserConf(base.getName(), base.getUniqueId(), new File(folder, filename + ".yml"));
		reloadConfig();
	}

	public final void reset()
	{
		config.forceSave();
		config.getFile().delete();
		if (config.username != null)
		{
			ess.getUserMap().removeUser(config.username);
		}
	}

	public final void cleanup()
	{
		config.cleanup();
	}

	@Override
	public final void reloadConfig()
	{
		config.load();
		money = _getMoney();
		unlimited = _getUnlimited();
		powertools = _getPowertools();
		homes = _getHomes();
		lastLocation = _getLastLocation();
		lastTeleportTimestamp = _getLastTeleportTimestamp();
		lastHealTimestamp = _getLastHealTimestamp();
		jail = _getJail();
		mails = _getMails();
		teleportEnabled = _getTeleportEnabled();
		godmode = _getGodModeEnabled();
		muted = _getMuted();
		muteTimeout = _getMuteTimeout();
		jailed = _getJailed();
		jailTimeout = _getJailTimeout();
		lastLogin = _getLastLogin();
		lastLogout = _getLastLogout();
		lastLoginAddress = _getLastLoginAddress();
		afk = _getAfk();
		geolocation = _getGeoLocation();
		isSocialSpyEnabled = _isSocialSpyEnabled();
		isNPC = _isNPC();
		arePowerToolsEnabled = _arePowerToolsEnabled();
		kitTimestamps = _getKitTimestamps();
		nickname = _getNickname();
		ignoredPlayers = _getIgnoredPlayers();
		logoutLocation = _getLogoutLocation();
		lastAccountName = _getLastAccountName();
		destroyMinecart = _getDestroyMinecartOnQuit(); //Meriland: Destruir minecarts al bajarte de ellas
	}
	private BigDecimal money;

	private BigDecimal _getMoney()
	{
		BigDecimal result = ess.getSettings().getStartingBalance();
		BigDecimal maxMoney = ess.getSettings().getMaxMoney();
		BigDecimal minMoney = ess.getSettings().getMinMoney();

		if (config.hasProperty("money"))
		{
			result = config.getBigDecimal("money", result);
		}
		if (result.compareTo(maxMoney) > 0)
		{
			result = maxMoney;
		}
		if (result.compareTo(minMoney) < 0)
		{
			result = minMoney;
		}
		return result;
	}

	public BigDecimal getMoney()
	{
		return money;
	}

	public void setMoney(BigDecimal value, boolean throwError) throws MaxMoneyException
	{
		BigDecimal maxMoney = ess.getSettings().getMaxMoney();
		BigDecimal minMoney = ess.getSettings().getMinMoney();
		if (value.compareTo(maxMoney) > 0)
		{
			if (throwError)
			{
				throw new MaxMoneyException();
			}
			money = maxMoney;
		}
		else
		{
			money = value;
		}
		if (money.compareTo(minMoney) < 0)
		{
			money = minMoney;
		}
		config.setProperty("money", money);
		stopTransaction();
	}
	private Map<String, Object> homes;

	private Map<String, Object> _getHomes()
	{
		if (config.isConfigurationSection("homes"))
		{
			return config.getConfigurationSection("homes").getValues(false);
		}
		return new HashMap<String, Object>();
	}

	private String getHomeName(String search)
	{
		if (NumberUtil.isInt(search))
		{
			try
			{
				search = getHomes().get(Integer.parseInt(search) - 1);
			}
			catch (NumberFormatException e)
			{
			}
			catch (IndexOutOfBoundsException e)
			{
			}
		}
		return search;
	}

	public Location getHome(String name) throws Exception
	{
		String search = getHomeName(name);
		return config.getLocation("homes." + search, this.getBase().getServer());
	}

	public Location getHome(final Location world)
	{
		try
		{
			if (getHomes().isEmpty())
			{
				return null;
			}
			Location loc;
			for (String home : getHomes())
			{
				loc = config.getLocation("homes." + home, this.getBase().getServer());
				if (world.getWorld() == loc.getWorld())
				{
					return loc;
				}

			}
			loc = config.getLocation("homes." + getHomes().get(0), this.getBase().getServer());
			return loc;
		}
		catch (InvalidWorldException ex)
		{
			return null;
		}
	}

	public List<String> getHomes()
	{
		return new ArrayList<String>(homes.keySet());
	}

	public void setHome(String name, Location loc)
	{
		//Invalid names will corrupt the yaml
		name = StringUtil.safeString(name);
		homes.put(name, loc);
		config.setProperty("homes." + name, loc);
		config.save();
	}

	public void delHome(String name) throws Exception
	{
		String search = getHomeName(name);
		if (!homes.containsKey(search))
		{
			search = StringUtil.safeString(search);
		}
		if (homes.containsKey(search))
		{
			homes.remove(search);
			config.removeProperty("homes." + search);
			config.save();
		}
		else
		{
			throw new Exception(tl("invalidHome", search));
		}
	}

	public boolean hasHome()
	{
		return config.hasProperty("home");
	}
	private String nickname;

	public String _getNickname()
	{
		return config.getString("nickname");
	}

	public String getNickname()
	{
		return nickname;
	}

	public void setNickname(String nick)
	{
		nickname = nick;
		config.setProperty("nickname", nick);
		config.save();
	}
	private List<Integer> unlimited;

	private List<Integer> _getUnlimited()
	{
		return config.getIntegerList("unlimited");
	}

	public List<Integer> getUnlimited()
	{
		return unlimited;
	}

	public boolean hasUnlimited(ItemStack stack)
	{
		return unlimited.contains(stack.getTypeId());
	}

	public void setUnlimited(ItemStack stack, boolean state)
	{
		if (unlimited.contains(stack.getTypeId()))
		{
			unlimited.remove(Integer.valueOf(stack.getTypeId()));
		}
		if (state)
		{
			unlimited.add(stack.getTypeId());
		}
		config.setProperty("unlimited", unlimited);
		config.save();
	}
	private Map<String, Object> powertools;

	private Map<String, Object> _getPowertools()
	{
		if (config.isConfigurationSection("powertools"))
		{
			return config.getConfigurationSection("powertools").getValues(false);
		}
		return new HashMap<String, Object>();
	}

	public void clearAllPowertools()
	{
		powertools.clear();
		config.setProperty("powertools", powertools);
		config.save();
	}

	@SuppressWarnings("unchecked")
	public List<String> getPowertool(ItemStack stack)
	{
		return (List<String>)powertools.get("" + stack.getTypeId());
	}

	@SuppressWarnings("unchecked")
	public List<String> getPowertool(int id)
	{
		return (List<String>)powertools.get("" + id);
	}

	public void setPowertool(ItemStack stack, List<String> commandList)
	{
		if (commandList == null || commandList.isEmpty())
		{
			powertools.remove("" + stack.getTypeId());
		}
		else
		{
			powertools.put("" + stack.getTypeId(), commandList);
		}
		config.setProperty("powertools", powertools);
		config.save();
	}

	public boolean hasPowerTools()
	{
		return !powertools.isEmpty();
	}
	private Location lastLocation;

	private Location _getLastLocation()
	{
		try
		{
			return config.getLocation("lastlocation", this.getBase().getServer());
		}
		catch (InvalidWorldException e)
		{
			return null;
		}
	}

	public Location getLastLocation()
	{
		return lastLocation;
	}

	public void setLastLocation(Location loc)
	{
		if (loc == null || loc.getWorld() == null)
		{
			return;
		}
		lastLocation = loc;
		config.setProperty("lastlocation", loc);
		config.save();
	}
	private Location logoutLocation;

	private Location _getLogoutLocation()
	{
		try
		{
			return config.getLocation("logoutlocation", this.getBase().getServer());
		}
		catch (InvalidWorldException e)
		{
			return null;
		}
	}

	public Location getLogoutLocation()
	{
		return logoutLocation;
	}

	public void setLogoutLocation(Location loc)
	{
		if (loc == null || loc.getWorld() == null)
		{
			return;
		}
		logoutLocation = loc;
		config.setProperty("logoutlocation", loc);
		config.save();
	}
	private long lastTeleportTimestamp;

	private long _getLastTeleportTimestamp()
	{
		return config.getLong("timestamps.lastteleport", 0);
	}

	public long getLastTeleportTimestamp()
	{
		return lastTeleportTimestamp;
	}

	public void setLastTeleportTimestamp(long time)
	{
		lastTeleportTimestamp = time;
		config.setProperty("timestamps.lastteleport", time);
		config.save();
	}
	private long lastHealTimestamp;

	private long _getLastHealTimestamp()
	{
		return config.getLong("timestamps.lastheal", 0);
	}

	public long getLastHealTimestamp()
	{
		return lastHealTimestamp;
	}

	public void setLastHealTimestamp(long time)
	{
		lastHealTimestamp = time;
		config.setProperty("timestamps.lastheal", time);
		config.save();
	}
	private String jail;

	private String _getJail()
	{
		return config.getString("jail");
	}

	public String getJail()
	{
		return jail;
	}

	public void setJail(String jail)
	{
		if (jail == null || jail.isEmpty())
		{
			this.jail = null;
			config.removeProperty("jail");
		}
		else
		{
			this.jail = jail;
			config.setProperty("jail", jail);
		}
		config.save();
	}
	private List<String> mails;

	private List<String> _getMails()
	{
		return config.getStringList("mail");
	}

	public List<String> getMails()
	{
		return mails;
	}

	public void setMails(List<String> mails)
	{
		if (mails == null)
		{
			config.removeProperty("mail");
			mails = _getMails();
		}
		else
		{
			config.setProperty("mail", mails);
		}
		this.mails = mails;
		config.save();
	}

	public void addMail(String mail)
	{
		mails.add(mail);
		setMails(mails);
	}
	private boolean teleportEnabled;

	private boolean _getTeleportEnabled()
	{
		return config.getBoolean("teleportenabled", true);
	}

	public boolean isTeleportEnabled()
	{
		return teleportEnabled;
	}

	public void setTeleportEnabled(boolean set)
	{
		teleportEnabled = set;
		config.setProperty("teleportenabled", set);
		config.save();
	}
	private List<String> ignoredPlayers;

	public List<String> _getIgnoredPlayers()
	{
		return Collections.synchronizedList(config.getStringList("ignore"));
	}

	public void setIgnoredPlayers(List<String> players)
	{
		if (players == null || players.isEmpty())
		{
			ignoredPlayers = Collections.synchronizedList(new ArrayList<String>());
			config.removeProperty("ignore");
		}
		else
		{
			ignoredPlayers = players;
			config.setProperty("ignore", players);
		}
		config.save();
	}

	@Deprecated
	public boolean isIgnoredPlayer(final String userName)
	{
		final IUser user = ess.getUser(userName);
		if (user == null || !user.getBase().isOnline())
		{
			return false;
		}
		return isIgnoredPlayer(user);
	}

	public boolean isIgnoredPlayer(IUser user)
	{
		return (ignoredPlayers.contains(user.getName().toLowerCase(Locale.ENGLISH)) && !user.isIgnoreExempt());
	}

	public void setIgnoredPlayer(IUser user, boolean set)
	{
		if (set)
		{
			ignoredPlayers.add(user.getName().toLowerCase(Locale.ENGLISH));
		}
		else
		{
			ignoredPlayers.remove(user.getName().toLowerCase(Locale.ENGLISH));
		}
		setIgnoredPlayers(ignoredPlayers);
	}
	private boolean godmode;

	private boolean _getGodModeEnabled()
	{
		return config.getBoolean("godmode", false);
	}

	public boolean isGodModeEnabled()
	{
		return godmode;
	}

	public void setGodModeEnabled(boolean set)
	{
		godmode = set;
		config.setProperty("godmode", set);
		config.save();
	}
	private boolean muted;

	public boolean _getMuted()
	{
		return config.getBoolean("muted", false);
	}

	public boolean getMuted()
	{
		return muted;
	}

	public boolean isMuted()
	{
		return muted;
	}

	public void setMuted(boolean set)
	{
		muted = set;
		config.setProperty("muted", set);
		config.save();
	}
	private long muteTimeout;

	private long _getMuteTimeout()
	{
		return config.getLong("timestamps.mute", 0);
	}

	public long getMuteTimeout()
	{
		return muteTimeout;
	}

	public void setMuteTimeout(long time)
	{
		muteTimeout = time;
		config.setProperty("timestamps.mute", time);
		config.save();
	}
	private boolean jailed;

	private boolean _getJailed()
	{
		return config.getBoolean("jailed", false);
	}

	public boolean isJailed()
	{
		return jailed;
	}

	public void setJailed(boolean set)
	{
		jailed = set;
		config.setProperty("jailed", set);
		config.save();
	}

	public boolean toggleJailed()
	{
		boolean ret = !isJailed();
		setJailed(ret);
		return ret;
	}
	private long jailTimeout;

	private long _getJailTimeout()
	{
		return config.getLong("timestamps.jail", 0);
	}

	public long getJailTimeout()
	{
		return jailTimeout;
	}

	public void setJailTimeout(long time)
	{
		jailTimeout = time;
		config.setProperty("timestamps.jail", time);
		config.save();
	}

	private long lastLogin;

	private long _getLastLogin()
	{
		return config.getLong("timestamps.login", 0);
	}

	public long getLastLogin()
	{
		return lastLogin;
	}

	private void _setLastLogin(long time)
	{
		lastLogin = time;
		config.setProperty("timestamps.login", time);
	}

	public void setLastLogin(long time)
	{
		_setLastLogin(time);
		if (base.getAddress() != null && base.getAddress().getAddress() != null)
		{
			_setLastLoginAddress(base.getAddress().getAddress().getHostAddress());
		}
		config.save();
	}
	private long lastLogout;

	private long _getLastLogout()
	{
		return config.getLong("timestamps.logout", 0);
	}

	public long getLastLogout()
	{
		return lastLogout;
	}

	public void setLastLogout(long time)
	{
		lastLogout = time;
		config.setProperty("timestamps.logout", time);
		config.save();
	}
	private String lastLoginAddress;

	private String _getLastLoginAddress()
	{
		return config.getString("ipAddress", "");
	}

	public String getLastLoginAddress()
	{
		return lastLoginAddress;
	}

	private void _setLastLoginAddress(String address)
	{
		lastLoginAddress = address;
		config.setProperty("ipAddress", address);
	}
	private boolean afk;

	private boolean _getAfk()
	{
		return config.getBoolean("afk", false);
	}

	public boolean isAfk()
	{
		return afk;
	}

	public void _setAfk(boolean set)
	{
		afk = set;
		config.setProperty("afk", set);
		config.save();
	}
	private boolean newplayer;
	private String geolocation;

	private String _getGeoLocation()
	{
		return config.getString("geolocation");
	}

	public String getGeoLocation()
	{
		return geolocation;
	}

	public void setGeoLocation(String geolocation)
	{
		if (geolocation == null || geolocation.isEmpty())
		{
			this.geolocation = null;
			config.removeProperty("geolocation");
		}
		else
		{
			this.geolocation = geolocation;
			config.setProperty("geolocation", geolocation);
		}
		config.save();
	}
	private boolean isSocialSpyEnabled;

	private boolean _isSocialSpyEnabled()
	{
		return config.getBoolean("socialspy", false);
	}

	public boolean isSocialSpyEnabled()
	{
		return isSocialSpyEnabled;
	}

	public void setSocialSpyEnabled(boolean status)
	{
		isSocialSpyEnabled = status;
		config.setProperty("socialspy", status);
		config.save();
	}
	private boolean isNPC;

	private boolean _isNPC()
	{
		return config.getBoolean("npc", false);
	}

	public boolean isNPC()
	{
		return isNPC;
	}
	private String lastAccountName = null;

	public String getLastAccountName()
	{
		return lastAccountName;
	}

	public String _getLastAccountName()
	{
		return config.getString("lastAccountName", null);
	}

	public void setLastAccountName(String lastAccountName)
	{
		this.lastAccountName = lastAccountName;
		config.setProperty("lastAccountName", lastAccountName);
		config.save();
		ess.getUserMap().trackUUID(getConfigUUID(), lastAccountName, true);
	}

	public void setNPC(boolean set)
	{
		isNPC = set;
		config.setProperty("npc", set);
		config.save();
	}
	private boolean arePowerToolsEnabled;

	public boolean arePowerToolsEnabled()
	{
		return arePowerToolsEnabled;
	}

	public void setPowerToolsEnabled(boolean set)
	{
		arePowerToolsEnabled = set;
		config.setProperty("powertoolsenabled", set);
		config.save();
	}

	public boolean togglePowerToolsEnabled()
	{
		boolean ret = !arePowerToolsEnabled();
		setPowerToolsEnabled(ret);
		return ret;
	}

	private boolean _arePowerToolsEnabled()
	{
		return config.getBoolean("powertoolsenabled", true);
	}
	private Map<String, Long> kitTimestamps;

	private Map<String, Long> _getKitTimestamps()
	{

		if (config.isConfigurationSection("timestamps.kits"))
		{
			final ConfigurationSection section = config.getConfigurationSection("timestamps.kits");
			final Map<String, Long> timestamps = new HashMap<String, Long>();
			for (String command : section.getKeys(false))
			{
				if (section.isLong(command))
				{
					timestamps.put(command.toLowerCase(Locale.ENGLISH), section.getLong(command));
				}
				else if (section.isInt(command))
				{
					timestamps.put(command.toLowerCase(Locale.ENGLISH), (long)section.getInt(command));
				}
			}
			return timestamps;
		}
		return new HashMap<String, Long>();
	}

	public long getKitTimestamp(String name)
	{
		name = name.replace('.', '_').replace('/', '_');
		if (kitTimestamps != null && kitTimestamps.containsKey(name))
		{
			return kitTimestamps.get(name);
		}
		return 0l;
	}

	public void setKitTimestamp(final String name, final long time)
	{
		kitTimestamps.put(name.toLowerCase(Locale.ENGLISH), time);
		config.setProperty("timestamps.kits", kitTimestamps);
		config.save();
	}
	
	//Meri start
	private boolean destroyMinecart;

	private boolean _getDestroyMinecartOnQuit()
	{
		return config.getBoolean("destroyminecart", true);
	}
	
	public boolean destroyMinecartOnQuit()
	{
		return destroyMinecart;
	}

	public void setDestroyMinecartOnQuit(boolean set)
	{
		destroyMinecart = set;
		config.setProperty("destroyminecart", set);
		config.save();
	}
	//Meri end

	public void setConfigProperty(String node, Object object)
	{
		final String prefix = "info.";
		node = prefix + node;
		if (object instanceof Map)
		{
			config.setProperty(node, (Map)object);
		}
		else if (object instanceof List)
		{
			config.setProperty(node, (List<String>)object);
		}
		else if (object instanceof Location)
		{
			config.setProperty(node, (Location)object);
		}
		else if (object instanceof ItemStack)
		{
			config.setProperty(node, (ItemStack)object);
		}
		else
		{
			config.setProperty(node, object);
		}
		config.save();
	}

	public Set<String> getConfigKeys()
	{
		if (config.isConfigurationSection("info"))
		{
			return config.getConfigurationSection("info").getKeys(true);
		}
		return new HashSet<String>();
	}

	public Map<String, Object> getConfigMap()
	{
		if (config.isConfigurationSection("info"))
		{
			return config.getConfigurationSection("info").getValues(true);
		}
		return new HashMap<String, Object>();
	}

	public Map<String, Object> getConfigMap(String node)
	{
		if (config.isConfigurationSection("info." + node))
		{
			return config.getConfigurationSection("info." + node).getValues(true);
		}
		return new HashMap<String, Object>();
	}

	public UUID getConfigUUID()
	{
		return config.uuid;
	}

	public void save()
	{
		config.save();
	}

	public void startTransaction()
	{
		config.startTransaction();
	}

	public void stopTransaction()
	{
		config.stopTransaction();
	}
}
