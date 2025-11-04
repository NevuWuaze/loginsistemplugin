package com.oxh.login;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityManager {
    private final Main plugin;
    private final Map<UUID, Integer> attempts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lockedUntil = new ConcurrentHashMap<>();

    public SecurityManager(Main plugin){ this.plugin = plugin; }

    public boolean isLocked(UUID uuid){
        Long until = lockedUntil.get(uuid);
        if (until==null) return false;
        if (System.currentTimeMillis() > until){ lockedUntil.remove(uuid); return false; }
        return true;
    }

    public void recordAttempt(Player p){
        UUID u = p.getUniqueId();
        int max = plugin.cfg().getInt("messages.max-captcha-attempts", 3);
        int cur = attempts.getOrDefault(u, 0) + 1;
        attempts.put(u, cur);
        if (cur >= max){
            long lockMillis = plugin.cfg().getLong("messages.captcha-lock-seconds", 60) * 1000L;
            lockedUntil.put(u, System.currentTimeMillis() + lockMillis);
            if (plugin.cfg().getBoolean("messages.captcha-failed-kick", true)){
                p.kickPlayer("Has fallado demasiadas veces el captcha. Intenta más tarde.");
            }
            new BukkitRunnable(){ public void run(){ attempts.remove(u); lockedUntil.remove(u); } }.runTaskLater(plugin, plugin.cfg().getLong("messages.captcha-lock-seconds",60)*20L);
        }
    }

    public void resetAttempts(UUID uuid){ attempts.remove(uuid); }
}
