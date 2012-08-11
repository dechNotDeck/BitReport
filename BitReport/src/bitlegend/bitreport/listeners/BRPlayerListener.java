package bitlegend.bitreport.listeners;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import bitlegend.bitreport.BitReport;

public class BRPlayerListener implements Listener {
	@SuppressWarnings("unused")
	private BitReport instance;
	
	// Database info
	private String user = "";
	private String pass = "";
	private String url = "";
	private String reports = "";
	
	public BRPlayerListener(BitReport instance) {
		this.instance = instance;
		user = instance.config.readString("DB_User");
		pass = instance.config.readString("DB_Pass");
		url = "jdbc:mysql://" + instance.config.readString("DB_Host") + 
				"/" + instance.config.readString("DB_Name");
		reports = instance.config.readString("DB_Table_reports");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		if (player.hasPermission("bitreport.claim")) {
			try {
				Connection conn = DriverManager.getConnection(url, user, pass);
				Statement statement = conn.createStatement();
				ResultSet result = statement.executeQuery("SELECT * FROM `"
						+ reports + "` WHERE `status` = '0'");
				int count = 0;
				while (result.next()) {
					count++;
				}
				if (count > 0)
					player.sendMessage(ChatColor.RED + "There are currently "
							+ count + " unresolved tickets.");
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
	}
}
