package com.oxh.login;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PremiumConfirmCommand implements CommandExecutor {
    private final PlayerData pd; private final GUIManager gm;
    public PremiumConfirmCommand(PlayerData pd, GUIManager gm){ this.pd = pd; this.gm = gm; }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Solo jugadores."); return true; }
        Player p = (Player) sender;
        pd.setPremium(p.getUniqueId(), true);
        p.sendMessage("§6Premium activado. Gracias.");
        return true;
    }
}
