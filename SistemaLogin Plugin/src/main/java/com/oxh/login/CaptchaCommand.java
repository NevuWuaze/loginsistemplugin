package com.oxh.login;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CaptchaCommand implements CommandExecutor {
    private final CaptchaManager cm;
    private final GUIManager gm;
    private final PlayerData pd;
    private final SecurityManager sm;
    private final MusicManager mm;

    public CaptchaCommand(CaptchaManager cm, GUIManager gm, PlayerData pd, SecurityManager sm, MusicManager mm){ this.cm = cm; this.gm = gm; this.pd = pd; this.sm = sm; this.mm = mm; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Solo jugadores."); return true; }
        Player p = (Player) sender;
        if (sm.isLocked(p.getUniqueId())){ p.sendMessage("§cEstás temporalmente bloqueado por múltiples intentos fallidos."); return true; }
        if (args.length==0) { p.sendMessage("Uso: /captcha <resultado>"); return true; }
        try {
            int val = Integer.parseInt(args[0]);
            if (cm.validar(p, val)){
                p.sendMessage("§aCaptcha validado!");
                sm.resetAttempts(p.getUniqueId());
                mm.startBackgroundFor(p);
                gm.openRegisterPremiumMenu(p);
            } else {
                p.sendMessage("§cCaptcha incorrecto.");
                sm.recordAttempt(p);
                String op = cm.generarOperacion(p);
                p.sendTitle("§cCAPTCHA", "§eResuelve: §6"+op, 10, 20*10, 10);
            }
        } catch (NumberFormatException ex){ p.sendMessage("§cEso no es un número."); }
        return true;
    }
}
