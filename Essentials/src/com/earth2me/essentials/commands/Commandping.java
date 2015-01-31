package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import static com.earth2me.essentials.I18n.tl;
import org.bukkit.Server;

// This command can be used to echo messages to the users screen, mostly useless but also an #EasterEgg
public class Commandping extends EssentialsCommand
{
	public Commandping()
	{
		super("ping");
	}

	@Override
	public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		//Meri end
		sender.sendMessage(tl("pong"));
		sender.sendMessage("Usa /tps para ver el estado de lag de Meriland");
	}
}
