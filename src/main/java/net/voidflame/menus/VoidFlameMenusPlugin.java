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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoidFlameMenusPlugin extends JavaPlugin implements Listener {
    private static final String MAIN = "§8VoidFlame";
    private static final String DUELS = "§8VoidFlame • Duels";
    private static final String STATS = "§8VoidFlame • Stats";
    private static final String SETTINGS = "§8VoidFlame • Settings";
    private final Map<UUID, Boolean> settingBusy = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Boolean>> settingsCache = new ConcurrentHashMap<>();
    private StorageService storage;

    private record Setting(String key, Material material, String label, boolean defaultValue, String on, String off) {}

    private static final List<Setting> SETTINGS_LIST = List.of(
            new Setting("duel_requests", Material.IRON_SWORD, "طلبات المبارزة", true, "للجميع", "متوقف"),
            new Setting("party_invites", Material.CAKE, "دعوات الحفلة", true, "للجميع", "متوقف"),
            new Setting("explosion_effects", Material.WIND_CHARGE, "تأثيرات الانفجار", false, "مفعل", "متوقف"),
            new Setting("kit_profile", Material.BOOK, "ملف الكيت", false, "ظاهر", "مخفي"),
            new Setting("personal_level", Material.NAME_TAG, "إظهار المستوى الشخصي", false, "مفعل", "متوقف"),
            new Setting("friend_requests", Material.PLAYER_HEAD, "طلبات الصداقة", false, "للجميع", "متوقف"),
            new Setting("private_messages", Material.WRITABLE_BOOK, "الرسائل الخاصة", false, "للأصدقاء", "متوقف"),
            new Setting("friend_join_notifications", Material.BELL, "إشعار دخول الأصدقاء", true, "مفعل", "متوقف"),
            new Setting("scoreboard", Material.DARK_OAK_HANGING_SIGN, "لوحة النقاط", true, "مفعل", "متوقف"),
            new Setting("show_players", Material.ENDER_EYE, "إظهار اللاعبين", true, "مفعل", "متوقف")
    );

    @Override public void onEnable() {
        saveDefaultConfig();
        var r = getServer().getServicesManager().getRegistration(StorageService.class);
        if (r == null || (storage = r.getProvider()) == null) {
            getLogger().severe("VoidFlame-Core storage unavailable.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("VoidFlame-Menus enabled.");
    }

    public void open(Player p) { open(p, MAIN); }

    private void open(Player p, String title) {
        Inventory inv = Bukkit.createInventory(null, 45, title);
        fill(inv);
        if (title.equals(MAIN)) {
            button(inv, 11, Material.DIAMOND_SWORD, "§bDuels", "§7Queue, request and spectate.");
            button(inv, 13, Material.CHEST, "§aKits", "§7Browse and equip kits.");
            button(inv, 15, Material.NETHERITE_HELMET, "§eStats", "§7Your profile and leaderboard.");
            button(inv, 29, Material.NETHER_STAR, "§dPractice", "§7Open practice features.");
            button(inv, 31, Material.BOOK, "§6Server", "§7Server information.");
            button(inv, 33, Material.BARRIER, "§cClose");
            button(inv, 20, Material.COMPARATOR, "§eSettings", "§7إعداداتك الشخصية");
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
        } else if (title.equals(SETTINGS)) {
            button(inv, 10, Material.LIME_DYE, "§aالاتصال", "§7متصل بالسيرفر", "§7نشاطك: §aمفعل");
            for (int i = 0; i < SETTINGS_LIST.size(); i++) {
                Setting s = SETTINGS_LIST.get(i);
                boolean value = getSetting(p, s);
                button(inv, 11 + i, s.material(), "§e" + s.label(), "§7الحالة: " + (value ? "§a" + s.on() : "§c" + s.off()), "§8اضغط للتبديل");
            }
            button(inv, 31, Material.ARROW, "§7رجوع");
            button(inv, 33, Material.BARRIER, "§cإغلاق");
        }
        p.openInventory(inv);
    }

    private void fill(Inventory inv) {
        ItemStack pane = item(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inv.getSize(); i++) if (i / 9 == 0 || i / 9 == 4) inv.setItem(i, pane.clone());
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

    private boolean getSetting(Player p, Setting s) {
        Map<String, Boolean> values = settingsCache.get(p.getUniqueId());
        if (values == null) return s.defaultValue();
        return values.getOrDefault(s.key(), s.defaultValue());
    }

    private void loadSettings(Player player) {
        UUID id = player.getUniqueId();
        Map<String, Boolean> values = new ConcurrentHashMap<>();
        settingsCache.put(id, values);
        for (Setting setting : SETTINGS_LIST) {
            storage.get("settings:" + id, setting.key()).thenAccept(raw -> {
                values.put(setting.key(), raw == null ? setting.defaultValue() : Boolean.parseBoolean(raw));
            }).exceptionally(error -> {
                getLogger().warning("Could not load setting " + setting.key() + " for " + id + ": " + error.getMessage());
                return null;
            });
        }
    }

    private void toggle(Player p, Setting s) {
        if (settingBusy.putIfAbsent(p.getUniqueId(), true) != null) return;
        storage.get("settings:" + p.getUniqueId(), s.key()).thenAccept(current -> {
            boolean next = current == null ? s.defaultValue() : Boolean.parseBoolean(current);
            boolean value = !next;
            storage.put("settings:" + p.getUniqueId(), s.key(), Boolean.toString(value))
                    .whenComplete((ignored, error) -> Bukkit.getScheduler().runTask(this, () -> {
                        settingBusy.remove(p.getUniqueId());
                        if (error != null) {
                            p.sendMessage(ChatColor.RED + "تعذر حفظ الإعداد.");
                            return;
                        }
                        settingsCache.computeIfAbsent(p.getUniqueId(), ignoredId -> new ConcurrentHashMap<>()).put(s.key(), value);
                        open(p, SETTINGS);
                    }));
        });
    }

    private void command(Player p, String command) {
        p.closeInventory();
        Bukkit.dispatchCommand(p, command);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadSettings(event.getPlayer());
    }

    @EventHandler public void click(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!title.equals(MAIN) && !title.equals(DUELS) && !title.equals(STATS) && !title.equals(SETTINGS)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p) || e.getRawSlot() >= e.getInventory().getSize()) return;
        if (title.equals(MAIN)) {
            switch (e.getRawSlot()) {
                case 11 -> open(p, DUELS);
                case 13 -> command(p, "kits");
                case 15 -> open(p, STATS);
                case 20 -> open(p, SETTINGS);
                case 29 -> command(p, "practice");
                case 31 -> command(p, "help");
                case 33 -> p.closeInventory();
                default -> {}
            }
        } else if (title.equals(DUELS)) {
            switch (e.getRawSlot()) {
                case 10 -> command(p, "queue");
                case 13 -> command(p, "duel");
                case 16 -> command(p, "spectate");
                case 31 -> open(p, MAIN);
                case 33 -> p.closeInventory();
                default -> {}
            }
        } else if (title.equals(STATS)) {
            switch (e.getRawSlot()) {
                case 11 -> command(p, "stats");
                case 15 -> command(p, "stats top");
                case 31 -> open(p, MAIN);
                case 33 -> p.closeInventory();
                default -> {}
            }
        } else {
            if (e.getRawSlot() >= 11 && e.getRawSlot() < 11 + SETTINGS_LIST.size()) {
                toggle(p, SETTINGS_LIST.get(e.getRawSlot() - 11));
            } else if (e.getRawSlot() == 31) {
                open(p, MAIN);
            } else if (e.getRawSlot() == 33) {
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        settingsCache.remove(event.getPlayer().getUniqueId());
        settingBusy.remove(event.getPlayer().getUniqueId());
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) { sender.sendMessage("Players only."); return true; }
        open(p);
        return true;
    }
}
