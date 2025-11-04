package com.oxh.login;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MusicManager {
    private final Main plugin;
    private final Map<UUID, Integer> playingTask = new ConcurrentHashMap<>();

    public MusicManager(Main plugin){ this.plugin = plugin; }

    public void startBackgroundFor(Player p){
        if (!plugin.cfg().getBoolean("music.enabled", true)) return;
        if (playingTask.containsKey(p.getUniqueId())) return;

        if (plugin.cfg().getBoolean("music.use-resourcepack", false)){
            String url = plugin.cfg().getString("music.resourcepack-url", "");
            if (!url.isEmpty()) p.setResourcePack(url);
        }

        String soundName = plugin.cfg().getString("music.sound", "MUSIC_DISC_BLOCKS");
        Sound sound = null;
        try { sound = Sound.valueOf(soundName); } catch (Exception ex) { sound = Sound.MUSIC_DISC_BLOCKS; }
        float volume = (float) plugin.cfg().getDouble("music.volume", 1.0);
        float pitch = (float) plugin.cfg().getDouble("music.pitch", 1.0);
        long intervalTicks = plugin.cfg().getLong("music.interval-ticks", 20L*6);

        int taskId = new BukkitRunnable(){
            public void run(){
                if (!p.isOnline()) { stopFor(p); return; }
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        }.runTaskTimer(plugin, 0L, intervalTicks).getTaskId();

        playingTask.put(p.getUniqueId(), taskId);
    }

    public void stopFor(Player p){ Integer id = playingTask.remove(p.getUniqueId()); if (id!=null) plugin.getServer().getScheduler().cancelTask(id); }

    public void stopAll(){ for (UUID u : playingTask.keySet()) plugin.getServer().getScheduler().cancelTask(playingTask.get(u)); playingTask.clear(); }
}
