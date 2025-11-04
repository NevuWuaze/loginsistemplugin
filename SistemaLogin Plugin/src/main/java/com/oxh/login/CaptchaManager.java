package com.oxh.login;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CaptchaManager {
    private final Map<UUID, Integer> pending = new ConcurrentHashMap<>();
    private final SecureRandom rnd = new SecureRandom();
    private final Main plugin;

    public CaptchaManager(Main plugin) {
        this.plugin = plugin;
        new BukkitRunnable(){public void run(){ pending.clear(); }}.runTaskTimerAsynchronously(plugin,20*60,20*60);
    }

    public String generarOperacion(Player p) {
        int a = rnd.nextInt(10)+1;
        int b = rnd.nextInt(10)+1;
        int op = rnd.nextInt(3); // 0:+ 1:- 2:*
        String s;
        int res;
        if (op==0){ s = a+" + "+b; res = a+b; }
        else if (op==1){ s = a+" - "+b; res = a-b; }
        else { s = a+" * "+b; res = a*b; }
        pending.put(p.getUniqueId(), res);
        return s;
    }

    public boolean validar(Player p, int intento){
        Integer real = pending.remove(p.getUniqueId());
        if (real==null) return false;
        return real == intento;
    }

    public boolean tienePendiente(Player p){ return pending.containsKey(p.getUniqueId()); }
}
