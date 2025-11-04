package com.oxh.login;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterCommand implements CommandExecutor {
    private final PlayerData pd;
    private final MusicManager mm;

    public RegisterCommand(PlayerData pd, MusicManager mm){ this.pd = pd; this.mm = mm; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Solo jugadores."); return true; }
        Player p = (Player) sender;
        if (args.length < 1){ p.sendMessage("Uso: /register <contraseña>"); return true; }
        if (pd.isRegistered(p.getUniqueId())){ p.sendMessage("§cYa estás registrado."); return true; }
        String pass = args[0];
        if (pass.length() < 6){ p.sendMessage("§cLa contraseña debe tener al menos 6 caracteres."); return true; }
        String hashed = BCrypt.hashpw(pass, BCrypt.gensalt(12));
        pd.registerHashed(p.getUniqueId(), hashed);
        p.sendMessage("§aRegistro completado. Has iniciado sesión automáticamente.");
        mm.stopFor(p);
        pd.onSuccessfulLogin(p.getUniqueId(), p);
        return true;
    }
}
