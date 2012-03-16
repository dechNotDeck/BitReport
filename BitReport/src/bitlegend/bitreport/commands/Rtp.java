package bitlegend.bitreport.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import bitlegend.bitreport.BitReport;

public class Rtp implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Rtp(BitReport instance) {
		this.instance = instance;
		user = instance.config.readString("DB_User");
		pass = instance.config.readString("DB_Pass");
		url = "jdbc:mysql://" + instance.config.readString("DB_Host") + "/"
				+ instance.config.readString("DB_Name");
		reports = instance.config.readString("DB_Table_reports");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] split) {
		boolean r = false;
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			if (player.hasPermission("bitreport.tp")) {
				if (split.length >= 1) {
					int id = Integer.parseInt(split[0]);
					try {
						Connection conn = DriverManager.getConnection(url, user, pass);
						Statement select = conn.createStatement();
						String query = "SELECT * FROM `" + reports + "` " +
								"WHERE `uid` = '" + id + "'";
						ResultSet result = select.executeQuery(query);
						while (result.next()) {
							String locraw = result.getString(7);
							int status = result.getInt(3);
							String[] locsplit = locraw.split(",");
							Location loc = new Location(player.getWorld(), 
									Double.parseDouble(locsplit[0]), 
									Double.parseDouble(locsplit[1]), 
									Double.parseDouble(locsplit[2]));
							if (split.length == 2 && split[1].equals("override") && 
									player.hasPermission("bitreport.tpoverride") && status == 2){
								player.teleport(loc);
							}
							if (status == 2 && player.hasPermission("bitreport.tpoverride") 
									&& split.length == 1) {
								player.sendMessage(ChatColor.YELLOW +
										"This ticket has already been resolved, to " +
										"rtp to its location, use /rtp <ticket id> override");
							}
							if (status == 2 && !player.hasPermission("bitreport.tpoverride")) {
								player.sendMessage(ChatColor.YELLOW + 
										"You do not have permission to use rtp override");
							}
							if (status == 1 || status == 0)
								player.teleport(loc);
						}
						r = true;
					} catch (SQLException se) {
						se.printStackTrace();
					}
				}
			}
			else {
				player.sendMessage(ChatColor.YELLOW + 
						"You to not have access to this feature");
				r = true;
			}
		}
		
		return r;
	}

}
