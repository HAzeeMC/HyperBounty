package com.hazee.hyperbounty.command;

import com.hazee.hyperbounty.HyperBounty;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BountyTabCompleter implements TabCompleter {
    
    private final HyperBounty plugin;
    
    public BountyTabCompleter(HyperBounty plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!(sender instanceof Player)) {
            return completions;
        }
        
        if (args.length == 1) {
            completions.add("set");
            completions.add("check");
            completions.add("list");
            completions.add("help");
            
            if (sender.hasPermission("hyperbounty.admin")) {
                completions.add("remove");
                completions.add("reload");
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "set":
                case "check":
                case "remove":
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (!player.getName().equalsIgnoreCase(sender.getName())) {
                            completions.add(player.getName());
                        }
                    }
                    break;
            }
        }
        
        return filterCompletions(completions, args[args.length - 1]);
    }
    
    private List<String> filterCompletions(List<String> completions, String input) {
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(input.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}