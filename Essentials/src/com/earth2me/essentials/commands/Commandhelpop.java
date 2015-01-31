package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import static com.earth2me.essentials.I18n.tl;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.entity.Player;


public class Commandhelpop extends EssentialsCommand
{
	public Commandhelpop()
	{
		super("helpop");
	}

	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
		user.setDisplayNick();
		final String message = sendMessage(server, user.getSource(), user.getName(), args);
		if (!user.isAuthorized("essentials.helpop.receive"))
		{
			user.sendMessage(message);
		}
	}

	@Override
	public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception
	{
		//Meri start
		//sendMessage(server, sender, Console.NAME, args);
		//Player p = Bukkit.getOnlinePlayers().[0];
		Player[] pl;
		pl = ess.getOnlinePlayers().toArray(new Player[ess.getOnlinePlayers().size()]);
		sendMessageConsola(server, pl[0], Console.NAME, args);
	}

	private String sendMessage(final Server server, final CommandSource sender, final String from, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		final String message = getFinalArg(args, 0);
		server.getLogger().log(Level.INFO, message);
		//Meri start
		//ess.broadcastMessage("essentials.helpop.receive", message);
		Player p = sender.getPlayer();
		enviarHelpOP(p, p.getName(), message);
		//Meri end
		return message;
	}
	
	//Meri start
	private String sendMessageConsola(final Server server, final Player p, final String from, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}
		final String message = tl("helpOp", from, FormatUtil.stripFormat(getFinalArg(args, 0)));
		server.getLogger().log(Level.INFO, message);
		enviarHelpOP(p, "Consola", message);
		return message;
	}
	
    public void enviarHelpOP(Player p, String nombre, String mensaje) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("CoreHelpOp");
            out.writeUTF(nombre);
            out.writeUTF(mensaje);
            p.sendPluginMessage(ess, "MeriCore", b.toByteArray());
		} catch(IOException e) {
			if (ess.getSettings().isDebug()) {
				ess.getLogger().severe("Error enviando un mensaje: ");
				ess.getLogger().severe(e.getMessage());
			}
		}
    }
	//Meri end
}
