# HyperBounty Plugin

Advanced bounty system with killstreaks, hunter missions, and full customization.

## Features

- **Bounty System**: Set bounties on players with customizable amounts
- **Killstreak System**: Reward players for consecutive kills
- **Hunter Missions**: Track and complete bounty missions
- **Multi-Language**: Support for English and Vietnamese
- **Folia Support**: Compatible with Folia servers
- **PlaceholderAPI**: Extensive placeholder support
- **Customizable GUI**: Fully configurable user interface

## PlaceholderAPI

The plugin provides the following placeholders:

### Player Bounty Placeholders
- `%hyperbounty_bounty_amount%` - Bounty amount on player
- `%hyperbounty_bounty_setter%` - Who set the bounty
- `%hyperbounty_has_bounty%` - Whether player has a bounty (Yes/No)

### Player Stats Placeholders
- `%hyperbounty_killstreak%` - Current killstreak
- `%hyperbounty_hunter_missions%` - Number of active hunter missions

### Cooldown Placeholders
- `%hyperbounty_cooldown_kill%` - Kill cooldown remaining (seconds)
- `%hyperbounty_cooldown_reward%` - Reward cooldown remaining (seconds)
- `%hyperbounty_cooldown_bounty%` - Bounty cooldown remaining (seconds)

### Top Bounties Placeholders
- `%hyperbounty_top_bounty_1%` - Player with highest bounty
- `%hyperbounty_top_bounty_2%` - Player with 2nd highest bounty
- `%hyperbounty_top_bounty_3%` - Player with 3rd highest bounty
- `%hyperbounty_top_bounty_1_amount%` - Amount of highest bounty
- `%hyperbounty_top_bounty_2_amount%` - Amount of 2nd highest bounty
- `%hyperbounty_top_bounty_3_amount%` - Amount of 3rd highest bounty

### Global Placeholders
- `%hyperbounty_active_bounties%` - Total number of active bounties

## Folia Support

The plugin automatically detects Folia and uses appropriate schedulers:
- Global region scheduler for server-wide tasks
- Entity scheduler for player-specific tasks
- Async scheduler for background operations

## Commands

- `/bounty` - Open bounty menu
- `/bounty set <player> <amount>` - Set bounty on player
- `/bounty check [player]` - Check bounty status
- `/bounty list` - List all active bounties
- `/bounty remove <player>` - Remove bounty (Admin)
- `/bounty reload` - Reload configuration (Admin)

## Permissions

- `hyperbounty.use` - Use bounty commands (default: true)
- `hyperbounty.admin` - Admin commands (default: op)
- `hyperbounty.bypass` - Bypass cooldowns (default: op)
