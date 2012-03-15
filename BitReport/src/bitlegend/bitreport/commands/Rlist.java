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

public class Rlist implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Rlist(BitReport instance) {
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
			if (player.hasPermission("bitreport.claim")) {
				try {
					// Create connection, statement and result set with query
					Connection conn = DriverManager.getConnection(url, user, pass);
					Statement statement = conn.createStatement();
					ResultSet result = statement.executeQuery("SELECT * FROM `"
							+ reports + "` WHERE `status` = '0'");
					// Start the list with a header
					player.sendMessage(ChatColor.RED + "== Tickets ==");
					
					// Loop through results
					while (result.next()) {
						String[] data = result.getString(5).split(" ");
						int i = (data.length < 10) ? data.length : 10;
						String truncate = "";
						
						for (int j = 0; j < i; j++)
							truncate += " " + data[j];
						truncate = truncate.trim();
						if (data.length > 10)
							truncate += "...";
						
						player.sendMessage(ChatColor.RED + "["
								+ result.getInt(1) + "] " + ChatColor.YELLOW
								+ result.getString(2) + ": " + ChatColor.WHITE
								+ truncate);
					}
					
					// Clean up
					result.close();
					statement.close();
					conn.close();
					
					r = true;
				} catch (SQLException se) {
					se.printStackTrace();
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
