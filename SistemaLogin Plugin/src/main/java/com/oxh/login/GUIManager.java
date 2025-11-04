package com.oxh.login;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class GUIManager {
    private final Main plugin;
    private final CaptchaManager captchaManager;
    private final PlayerData playerData;

    public GUIManager(Main plugin, CaptchaManager cm, PlayerData pd){
        this.plugin = plugin; this.captchaManager = cm; this.playerData = pd;
        startAnimationTask();
    }

    private ItemStack makeHead(String ownerName, String display){
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (ownerName != null && !ownerName.isEmpty()){
            try { sm.setOwningPlayer(Bukkit.getOfflinePlayer(ownerName)); } catch (Exception ignored) {}
        }
        sm.setDisplayName(display);
        head.setItemMeta(sm);
        return head;
    }

    public void openRegisterPremiumMenu(Player p){
        Inventory inv = Bukkit.createInventory(null, plugin.cfg().getInt("menu.size", 9), plugin.cfg().getString("menu.title-register", "§8Registro » Selecciona"));

        String regOwner = plugin.cfg().getString("menu.heads.register-owner", p.getName());
        String premOwner = plugin.cfg().getString("menu.heads.premium-owner", "Mojang");

        ItemStack headRegister = makeHead(regOwner, "§aRegistrarse");
        ItemStack headPremium = makeHead(premOwner, "§6Activar Premium");

        inv.setItem(3, headRegister);
        inv.setItem(5, headPremium);

        for (int i=0;i<inv.getSize();i++) if (i!=3 && i!=5) inv.setItem(i, glassPane(7));

        p.openInventory(inv);
    }

    public void openLoginPremiumMenu(Player p){
        Inventory inv = Bukkit.createInventory(null, plugin.cfg().getInt("menu.size",9), plugin.cfg().getString("menu.title-login", "§8Login » Selecciona"));

        String loginOwner = plugin.cfg().getString("menu.heads.login-owner", p.getName());
        String premOwner = plugin.cfg().getString("menu.heads.premium-owner", "Mojang");

        ItemStack headLogin = makeHead(loginOwner, "§aLogin");
        ItemStack headPremium = makeHead(premOwner, "§6Activar Premium");

        List<String> premiumNames = plugin.cfg().getStringList("menu.premium-player-names");
        if (premiumNames != null && !premiumNames.isEmpty()){
            String owner = premiumNames.get(0);
            headPremium = makeHead(owner, "§6Activar Premium") ;
        }

        inv.setItem(3, headLogin); inv.setItem(5, headPremium);
        for (int i=0;i<inv.getSize();i++) if (i!=3 && i!=5) inv.setItem(i, glassPane(7));
        p.openInventory(inv);
    }

    private ItemStack glassPane(int color){
        ItemStack it = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta m = it.getItemMeta(); m.setDisplayName(" "); it.setItemMeta(m);
        return it;
    }

    private void startAnimationTask(){
        new org.bukkit.scheduler.BukkitRunnable(){
            int tick = 0;
            public void run(){
                tick = (tick+1)%6;
                for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()){
                    org.bukkit.inventory.Inventory inv = p.getOpenInventory().getTopInventory();
                    if (inv==null) continue;
                    String title = inv.getView().getTitle();
                    if (title==null) continue;
                    if (!title.contains("Registro") && !title.contains("Login")) continue;
                    for (int i=0;i<inv.getSize();i++){
                        if (i==3||i==5) continue;
                        inv.setItem(i, glassPane((tick%2==0)?7:8));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, plugin.cfg().getLong("animation.tick-interval", 6L));
    }

    public void handleMenuClick(org.bukkit.event.inventory.InventoryClickEvent e){
        org.bukkit.entity.HumanEntity he = e.getWhoClicked();
        if (!(he instanceof Player)) return;
        Player p = (Player) he;
        org.bukkit.inventory.Inventory top = e.getView().getTopInventory();
        if (top==null) return;
        String title = top.getView().getTitle();
        if (title==null) return;
        e.setCancelled(true);
        if (title.contains("Registro")){
            if (e.getRawSlot()==3) {
                p.closeInventory();
                p.sendMessage("§aEscribe en chat: /register <contraseña>");
            } else if (e.getRawSlot()==5){
                p.closeInventory(); p.sendMessage("§6Para confirmar premium ejecuta /premiumconfirmar");
            }
        } else if (title.contains("Login")){
            if (e.getRawSlot()==3){ p.closeInventory(); p.sendMessage("§aUsa /login <contraseña> en el chat"); }
            else if (e.getRawSlot()==5){ p.closeInventory(); p.sendMessage("§6Para confirmar premium ejecuta /premiumconfirmar"); }
        }
    }
}
