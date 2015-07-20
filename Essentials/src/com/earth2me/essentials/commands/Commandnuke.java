package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import org.bukkit.Server;


public class Commandnuke extends EssentialsCommand
{
	public Commandnuke()
	{
		super("nuke");
	}

	@Override
	protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws NoSuchFieldException, NotEnoughArgumentsException
	{
		sender.sendMessage("El comando /nuke ha sido deshabilitado por seguridad a los usuarios");
	}
}
