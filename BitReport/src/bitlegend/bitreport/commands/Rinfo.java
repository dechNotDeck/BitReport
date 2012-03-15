package bitlegend.bitreport.commands;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import bitlegend.bitreport.BitReport;

public class Rinfo implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Rinfo(BitReport instance) {
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
			if (player.hasPermission("bitreport.info")) {
				if (split.length == 1) {
					int uid = Integer.parseInt(split[0]);
					try {
						Connection conn = DriverManager.getConnection(url, user, pass);
						Statement select = conn.createStatement();
						ResultSet result = select.executeQuery("SELECT * FROM `" + reports
								+ "` WHERE `uid` = '" + uid + "'");
						
						while (result.next()) {
							int id = result.getInt(1);
							String username = result.getString(2);
							int status = result.getInt(3);
							String claimed = (status == 0 ? "unclaimed" : (status == 1 ? "claimed" : "resolved"));
							String staff = result.getString(4) == "null" ? "N/A" : result.getString(4);
							String data = result.getString(5);
							data = ChatColor.YELLOW + "Data: " + ChatColor.WHITE + data;
							
							String[] dataparse = data.split("(?<=\\G.{67})");
							
							player.sendMessage(ChatColor.RED + "== Ticket "
									+ ChatColor.YELLOW + id + ChatColor.RED
									+ " submitted by " + ChatColor.YELLOW
									+ username + ChatColor.RED + " ==");
							
							player.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.WHITE + claimed);
							if (status == 2 || status == 1)
								player.sendMessage(ChatColor.YELLOW + "Staff: " + ChatColor.WHITE + staff);
							player.sendMessage(dataparse);
							
						}
						
						r = true;
					} catch (SQLException se) {
						se.printStackTrace();
					}
				}
			}
			else {
				player.sendMessage(ChatColor.YELLOW + 
						"You do not have access to this feature");
				r = true;
			}
		}
		if (sender instanceof ConsoleCommandSender) {
			instance.logInfo("This is an in-game only command.");
			r = true;
		}
		
		return r;
	}

}
