package net.voidflame.menus;

import net.voidflame.core.storage.StorageService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public final class VoidFlameMenusPlugin extends JavaPlugin implements Listener {
    private static final String MAIN = "§8VoidFlame";
    private static final String DUELS = "§8VoidFlame • Duels";
    private static final String STATS = "§8VoidFlame • Stats";
    private final Deque<String> history = new ArrayDeque<>();
    private StorageService storage;

    @Override public void onEnable() {
        saveDefaultConfig();
        var r = getServer().getServicesManager().getRegistration(StorageService.class);
        if (r == null || (storage = r.getProvider()) == null) { getLogger().severe("VoidFlame-Core storage unavailable."); getServer().getPluginManager().disablePlugin(this); return; }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("VoidFlame-Menus enabled.");
    }

    public void open(Player p) { open(p, MAIN, false); }

    private void open(Player p, String title, boolean push) {
        if (push) history.push(p.getUniqueId()+":"+p.getOpenInventory().getTitle());
        Inventory inv = Bukkit.createInventory(null, 45, title);
        fill(inv);
        if (title.equals(MAIN)) {
            button(inv, 11, Material.DIAMOND_SWORD, "§bDuels", "§7Queue, request and spectate.");
            button(inv, 13, Material.CHEST, "§aKits", "§7Browse and equip kits.");
            button(inv, 15, Material.NETHERITE_HELMET, "§eStats", "§7Your profile and leaderboard.");
            button(inv, 29, Material.NETHER_STAR, "§dPractice", "§7Open practice features.");
            button(inv, 31, Material.BOOK, "§6Server", "§7Server information.");
            button(inv, 33, Material.BARRIER, "§cClose");
        } else if (title.equals(DUELS)) {
            button(inv, 10, Material.DIAMOND_SWORD, "§bJoin Queue", "§7Uses the default queue.");
            button(inv, 13, Material.PAPER, "§fDuel Player", "§7Use /duel <player>.");
            button(inv, 16, Material.ENDER_EYE, "§dSpectate", "§7Use /spectate <player>.");
            button(inv, 31, Material.ARROW, "§7Back");
            button(inv, 33, Material.BARRIER, "§cClose");
        } else if (title.equals(STATS)) {
            button(inv, 11, Material.PLAYER_HEAD, "§eMy Stats", "§7Use /stats.");
            button(inv, 15, Material.GOLD_INGOT, "§6Leaderboard", "§7Use /stats top.");
            button(inv, 31, Material.ARROW, "§7Back");
            button(inv, 33, Material.BARRIER, "§cClose");
        }
        p.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack pane = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i=0;i<inv.getSize();i++) if (i/9==0 || i/9==4) inv.setItem(i, pane.clone());
    }

    private void button(Inventory inv, int slot, Material material, String name, String... lore) {
        inv.setItem(slot, item(material, name, lore));
    }

    private ItemStack item(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) { meta.setDisplayName(name); meta.setLore(List.of(lore)); stack.setItemMeta(meta); }
        return stack;
    }

    private void command(Player p, String command) {
        p.closeInventory();
        Bukkit.dispatchCommand(p, command);
    }

    @EventHandler public void click(InventoryClickEvent e) {
        String title=e.getView().getTitle();
        if (!title.equals(MAIN) && !title.equals(DUELS) && !title.equals(STATS)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p) || e.getRawSlot() >= e.getInventory().getSize()) return;
        if (title.equals(MAIN)) { switch(e.getRawSlot()) {
            case 11 -> open(p, DUELS, true);
            case 13 -> command(p, "kits");
            case 15 -> open(p, STATS, true);
            case 29 -> command(p, "practice");
            case 31 -> command(p, "help");
            case 33 -> p.closeInventory();
            default -> {}
        };
        else if (title.equals(DUELS)) switch(e.getRawSlot()) {
            case 10 -> command(p, "queue");
            case 13 -> command(p, "duel");
            case 16 -> command(p, "spectate");
            case 31 -> open(p, MAIN, false);
            case 33 -> p.closeInventory();
            default -> {}
        };
        else switch(e.getRawSlot()) {
            case 11 -> command(p, "stats");
            case 15 -> command(p, "stats top");
            case 31 -> open(p, MAIN, false);
            case 33 -> p.closeInventory();
            default -> {}
        };
    }

    @EventHandler public void close(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(MAIN)) history.removeIf(s -> s.startsWith(e.getPlayer().getUniqueId()+":"));
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        open(p); return true;
    }
}