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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

public final class VoidFlameMenusPlugin extends JavaPlugin implements Listener {
    private Object storage; private Method put,get;
    @Override public void onEnable(){saveDefaultConfig(); if(!connectStorage()){getLogger().severe("VoidFlame-Core storage unavailable.");getServer().getPluginManager().disablePlugin(this);return;} getServer().getPluginManager().registerEvents(this,this);getLogger().info("VoidFlame-Menus enabled.");}
    private boolean connectStorage(){try{Class<?> t=Class.forName("net.voidflame.core.storage.StorageService");RegisteredServiceProvider<?> r=getServer().getServicesManager().getRegistration(t);if(r==null)return false;storage=r.getProvider();put=t.getMethod("put",String.class,String.class,String.class);get=t.getMethod("get",String.class,String.class);return true;}catch(ReflectiveOperationException e){return false;}}
    public CompletableFuture<Void> put(String k,String v){try{return (CompletableFuture<Void>)put.invoke(storage,"menus",k,v);}catch(ReflectiveOperationException e){return CompletableFuture.failedFuture(e);}}
    public CompletableFuture<String> get(String k){try{return (CompletableFuture<String>)get.invoke(storage,"menus",k);}catch(ReflectiveOperationException e){return CompletableFuture.failedFuture(e);}}
    public void open(Player p){Inventory inv=Bukkit.createInventory(null,27,"VoidFlame"); item(inv,11,Material.DIAMOND_SWORD,"Duels"); item(inv,13,Material.CHEST,"Kits"); item(inv,15,Material.NETHERITE_HELMET,"Stats"); p.openInventory(inv);}
    private void item(Inventory i,int slot,Material m,String name){ItemStack x=new ItemStack(m);ItemMeta meta=x.getItemMeta();meta.setDisplayName(name);x.setItemMeta(meta);i.setItem(slot,x);}
    @EventHandler public void click(InventoryClickEvent e){if(!e.getView().getTitle().equals("VoidFlame"))return;e.setCancelled(true);}
    @Override public boolean onCommand(CommandSender s,Command c,String l,String[] a){if(!(s instanceof Player p))return true;open(p);return true;}
}
