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

public class Rclaim implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Rclaim(BitReport instance) {
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
			if (instance.pex.has(player, "bitreport.claim")) {
				if (split.length == 1) {
					int id = Integer.parseInt(split[0]);
					try {
						Connection conn = DriverManager.getConnection(url, user, pass);
						Statement select = conn.createStatement();
						ResultSet result = select
								.executeQuery("SELECT * FROM `" + reports
										+ "` WHERE" + "`uid` = '" + id + "'");
						while (result.next()) {
							int status = result.getInt(3);
							if (status == 0) { // Ticket is unclaimed
								String username = result.getString(2);
								String staff = player.getName();
								
								String query = "UPDATE `" + reports
										+ "` SET `status` = '1', `staff` = '"
										+ staff + "' WHERE `uid` = '" + id
										+ "'";
								Statement update = conn.createStatement();
								update.executeUpdate(query);
								
								player.sendMessage(ChatColor.GREEN
										+ "You have claimed ticket number "
										+ ChatColor.YELLOW + id
										+ ChatColor.GREEN
										+ ", it is now your responsibility");
								
								// Check if the player who filed the ticket is
								// online and inform them that their ticket has been claimed
								for (Player p : instance.getServer().getOnlinePlayers()) {
									if (p.getName().equals(username))
										p.sendMessage(ChatColor.GREEN
												+ "Your ticket (id number "
												+ ChatColor.YELLOW + id
												+ ChatColor.GREEN
												+ ") has been claimed by "
												+ ChatColor.YELLOW + staff);
								}
								
								// Clean up
								update.close();
							}
							else if (status == 1) {
								player.sendMessage(ChatColor.YELLOW + "This ticket has already been claimed.");
							}
							else if (status == 2) {
								player.sendMessage(ChatColor.YELLOW + "This ticket has already been resolved.");
							}
							r = true;
						}
						
						// Clean up
						result.close();
						select.close();
						conn.close();
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
