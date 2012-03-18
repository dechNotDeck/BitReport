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

public class Rnext implements CommandExecutor {
	private BitReport instance;

	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";

	public Rnext(BitReport instance) {
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
			if (instance.pex.has(player, "bitreport.info")) {
				try {
					Connection conn = DriverManager.getConnection(url, user, pass);
					Statement select = conn.createStatement();
					ResultSet result = select.executeQuery("SELECT * FROM `"
							+ reports
							+ "` WHERE `status` = '0' ORDER BY `uid` LIMIT 1");
					while (result.next()) {
						int id = result.getInt(1);
						instance.getServer().dispatchCommand(player, "rinfo " + id);
					}
					result.close();
					select.close();
					conn.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}
				r = true;
			}
			else {
				player.sendMessage(ChatColor.YELLOW + 
						"You do not have access to this feature");
				r = true;
			}
		}
		
		return r;
	}

}
