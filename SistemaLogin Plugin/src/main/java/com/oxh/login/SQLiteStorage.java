package com.oxh.login;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class SQLiteStorage {
    private final Main plugin;
    private Connection conn;
    private File dbFile;

    public SQLiteStorage(Main plugin){
        this.plugin = plugin;
        try{
            dbFile = new File(plugin.getDataFolder(), "sajuilogin.db");
            if (!dbFile.exists()) dbFile.getParentFile().mkdirs();
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement st = conn.createStatement()){
                st.executeUpdate("CREATE TABLE IF NOT EXISTS users(uuid TEXT PRIMARY KEY, pass TEXT, premium INTEGER)");
            }
        } catch (Exception ex){ plugin.getLogger().severe("Error SQLite: " + ex.getMessage()); }
    }

    public void saveUser(UUID uuid, String hashed, boolean premium){
        try (PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO users(uuid, pass, premium) VALUES(?,?,?)")){
            ps.setString(1, uuid.toString()); ps.setString(2, hashed); ps.setInt(3, premium?1:0); ps.executeUpdate();
        } catch (SQLException e){ e.printStackTrace(); }
    }

    public boolean userExists(UUID uuid){
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE uuid = ?")){
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()){ return rs.next(); }
        } catch (SQLException e){ e.printStackTrace(); }
        return false;
    }

    public String getHash(UUID uuid){
        try (PreparedStatement ps = conn.prepareStatement("SELECT pass FROM users WHERE uuid=?")){
            ps.setString(1, uuid.toString()); try (ResultSet rs = ps.executeQuery()){ if (rs.next()) return rs.getString(1); }
        } catch (SQLException e){ e.printStackTrace(); }
        return null;
    }

    public boolean isPremium(UUID uuid){
        try (PreparedStatement ps = conn.prepareStatement("SELECT premium FROM users WHERE uuid=?")){
            ps.setString(1, uuid.toString()); try (ResultSet rs = ps.executeQuery()){ if (rs.next()) return rs.getInt(1)==1; }
        } catch (SQLException e){ e.printStackTrace(); }
        return false;
    }
}
