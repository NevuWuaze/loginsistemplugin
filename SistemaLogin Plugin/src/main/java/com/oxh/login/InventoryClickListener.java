package com.oxh.login;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    private final GUIManager gui;

    public InventoryClickListener(GUIManager gui){ this.gui = gui; }

    @EventHandler
    public void onClick(InventoryClickEvent e){ gui.handleMenuClick(e); }
}
