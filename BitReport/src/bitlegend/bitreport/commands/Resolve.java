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
import org.bukkit.entity.Player;

import bitlegend.bitreport.BitReport;

public class Resolve implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Resolve(BitReport instance) {
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
			if (player.hasPermission("bitreport.resolve")) {
				if (split.length >= 1) {
					int id = Integer.parseInt(split[0]);
					String notes = "";
					
					if (split.length > 1) {
						for (int i = 1; i < split.length; i++)
							notes += " " + split[i];
						notes = notes.trim();
					}
					
					try {
						Connection conn = DriverManager.getConnection(url, user, pass);
						
						Statement check = conn.createStatement();
						ResultSet checkset = check
								.executeQuery("SELECT * FROM `" + reports
										+ "` WHERE `uid`='" + id + "'");

						boolean resolved = false;
						int statuscheck = 0;
						while (checkset.next()) {
							statuscheck = checkset.getInt(3);
							if (statuscheck == 2)
								resolved = true;
						}
						
						if (resolved == false && statuscheck == 1) {
							Statement update = conn.createStatement();
							update.executeUpdate("UPDATE `" + reports
									+ "` SET `status` = '2' WHERE `uid` = '" + id
									+ "'");

							if (!notes.equals(""))
								update.executeUpdate("UPDATE `" + reports
										+ "` SET `notes` = '" + notes
										+ "' WHERE `uid` = '" + id + "'");
							
							Statement select = conn.createStatement();
							ResultSet result = select
									.executeQuery("SELECT * FROM `" + reports
											+ "` WHERE `uid`='" + id + "'");
							while (result.next()) {
								String username = result.getString(2);
								for (Player p : instance.getServer()
										.getOnlinePlayers()) {
									if (p.getName().equals(username))
										p.sendMessage(ChatColor.GREEN
												+ "Your ticket (id number "
												+ ChatColor.YELLOW
												+ id
												+ ChatColor.GREEN
												+ ") has been marked as resolved by "
												+ ChatColor.YELLOW
												+ player.getName());
								}
							}
							
							// Clean up
							result.close();
							select.close();
							update.close();
						}
						else if (resolved == true)
							player.sendMessage(ChatColor.YELLOW + 
									"This ticket has already been resolved");
						else if (statuscheck != 1)
							player.sendMessage(ChatColor.YELLOW + 
									"This ticket has not yet been claimed");
						
						// Clean up
						checkset.close();
						check.close();
						conn.close();
					} catch (SQLException se) {
						se.printStackTrace();
					}
					
					r = true;
				}
			}
			else {
				player.sendMessage(ChatColor.YELLOW + "You do not have access to this feature");
				r = true;
			}
		}
		
		return r;
	}

}
