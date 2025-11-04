package com.oxh.login;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileInputStream;

public class WorldEditSchematicPaster {
    private final Main plugin;
    public WorldEditSchematicPaster(Main plugin){ this.plugin = plugin; }

    public void pasteIfConfigured(){
        if (!plugin.cfg().getBoolean("login.use-schematic", false)) return;
        String path = plugin.cfg().getString("login.schematic-path", "login_map.schematic");
        File f = new File(plugin.getDataFolder(), path);
        if (!f.exists()) { plugin.getLogger().warning("Schematic no encontrada: " + f.getAbsolutePath()); return; }
        try (FileInputStream fis = new FileInputStream(f)){
            var format = ClipboardFormats.findByFile(f);
            var reader = format.getReader(fis);
            var clipboard = reader.read();
            BlockVector3 origin = BlockVector3.at(plugin.cfg().getDouble("login.x",0.5), plugin.cfg().getDouble("login.y",65), plugin.cfg().getDouble("login.z",0.5));
            com.sk89q.worldedit.world.World weWorld = new BukkitWorld(Bukkit.getWorld(plugin.cfg().getString("login.world","world")));
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)){
                ClipboardHolder holder = new ClipboardHolder(clipboard);
                holder.paste(editSession, origin, true);
            }
        } catch (Exception ex){ plugin.getLogger().warning("Error pegando schematic: " + ex.getMessage()); }
    }
}
