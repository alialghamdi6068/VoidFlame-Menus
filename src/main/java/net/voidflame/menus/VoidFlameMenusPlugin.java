package net.voidflame.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;
import net.voidflame.core.storage.StorageService;

public final class VoidFlameMenusPlugin extends JavaPlugin implements Listener {
    private StorageService storage;
    @Override public void onEnable(){saveDefaultConfig(); if(!connectStorage()){getLogger().severe("VoidFlame-Core storage unavailable.");getServer().getPluginManager().disablePlugin(this);return;} getServer().getPluginManager().registerEvents(this,this);getLogger().info("VoidFlame-Menus enabled.");}
    private boolean connectStorage(){ var r=getServer().getServicesManager().getRegistration(StorageService.class); if(r==null)return false; storage=r.getProvider(); return storage!=null; }
    public CompletableFuture<Void> put(String k,String v){ return storage.put("menus",k,v); }
    public CompletableFuture<String> get(String k){ return storage.get("menus",k); }
    public void open(Player p){
        Inventory inv=Bukkit.createInventory(null,27,"VoidFlame");
        item(inv,11,Material.DIAMOND_SWORD,"§bDuels","§7Open the practice system.");
        item(inv,13,Material.CHEST,"§aKits","§7Open kit and queue options.");
        item(inv,15,Material.NETHERITE_HELMET,"§eStats","§7View your practice statistics.");
        item(inv,22,Material.BARRIER,"§cClose");
        p.openInventory(inv);
    }
    private void item(Inventory i,int slot,Material m,String name,String... lore){
        ItemStack x=new ItemStack(m);ItemMeta meta=x.getItemMeta();meta.setDisplayName(name);meta.setLore(java.util.Arrays.asList(lore));x.setItemMeta(meta);i.setItem(slot,x);
    }
    @EventHandler public void click(InventoryClickEvent e){
        if(!e.getView().getTitle().equals("VoidFlame"))return;
        e.setCancelled(true);
        if(!(e.getWhoClicked() instanceof Player p))return;
        switch(e.getRawSlot()){
            case 11,13 -> {p.closeInventory();p.performCommand("duel");}
            case 15 -> {p.closeInventory();p.performCommand("stats");}
            case 22 -> p.closeInventory();
            default -> {}
        }
    }
    @Override public boolean onCommand(CommandSender s,Command c,String l,String[] a){if(!(s instanceof Player p))return true;open(p);return true;}
}
