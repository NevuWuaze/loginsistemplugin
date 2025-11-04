package com.oxh.login;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {
    private final PlayerData pd;
    private final GUIManager gm;
    private final MusicManager mm;
    public LoginCommand(PlayerData pd, GUIManager gm, MusicManager mm){ this.pd = pd; this.gm = gm; this.mm = mm; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Solo jugadores."); return true; }
        Player p = (Player) sender;
        if (args.length<1){ p.sendMessage("Uso: /login <contraseña>"); return true; }
        String pass = args[0];
        if (pd.checkPasswordHashed(p.getUniqueId(), pass)){
            p.sendMessage("§aHas iniciado sesión correctamente.");
            mm.stopFor(p);
            pd.onSuccessfulLogin(p.getUniqueId(), p);
        } else {
            p.sendMessage("§cContraseña incorrecta.");
        }
        return true;
    }
}
