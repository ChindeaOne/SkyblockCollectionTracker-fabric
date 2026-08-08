package io.github.chindeaone.collectiontracker.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommands
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object ConfigCommandRegistry {

    fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>, sct: LiteralArgumentBuilder<FabricClientCommandSource>) {
        // Mining commands
        sct.then(
            ClientCommands.literal("config")
                .then(
                    ClientCommands.literal("toggleMiningStats")
                        .executes {
                            val enabled = ConfigHelper.toggleMiningStats()
                            ChatUtils.sendMessage("§eMining stats overlay " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleMiningStatsOnlyOnMiningIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleMiningStatsOnlyOnMiningIslands()
                            ChatUtils.sendMessage("§eMining stats overlay in mining islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("togglePickaxeAbility")
                        .executes {
                            val enabled = ConfigHelper.togglePickaxeAbility()
                            ChatUtils.sendMessage("§ePickaxe ability display " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("togglePickaxeAbilityOnlyOnMiningIslands")
                        .executes {
                            val enabled = ConfigHelper.togglePickaxeAbilityOnlyOnMiningIslands()
                            ChatUtils.sendMessage("§ePickaxe ability display in mining islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("togglePickaxeAbilityReadyTitle")
                        .executes {
                            val enabled = ConfigHelper.togglePickaxeAbilityReadyTitle()
                            ChatUtils.sendMessage("§ePickaxe ability ready title "+ if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("togglePickaxeAbilityExpiredTitle")
                        .executes {
                            val enabled = ConfigHelper.togglePickaxeAbilityExpiredTitle()
                            ChatUtils.sendMessage("§ePickaxe ability expired title "+ if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleSkyMall")
                        .executes {
                            val enabled = ConfigHelper.toggleSkyMall()
                            ChatUtils.sendMessage("§eSky Mall " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleSkyMallOnlyOnMiningIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleSkyMallOnlyOnMiningIslands()
                            ChatUtils.sendMessage("§eSky Mall in mining islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleSkyMallChatMessages")
                        .executes {
                            val enabled = ConfigHelper.toggleSkyMallChatMessages()
                            ChatUtils.sendMessage("§eSky Mall chat messages " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleCommissionsOverlay")
                        .executes {
                            val enabled = ConfigHelper.toggleCommissionsOverlay()
                            ChatUtils.sendMessage("§eCommissions overlay " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleCommissionsTracking")
                        .executes {
                            val enabled = ConfigHelper.toggleCommissionsTracking()
                            ChatUtils.sendMessage("§eCommissions tracking sub-overlay " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleCommissionsKeybinds")
                        .executes {
                            val enabled = ConfigHelper.toggleCommissionsKeybinds()
                            ChatUtils.sendMessage("§eCommissions keybinds " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleTempBuffTracker")
                        .executes {
                            val enabled = ConfigHelper.toggleTempBuffTracker()
                            ChatUtils.sendMessage("§eTemporary buff tracker " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleTempBuffExpiredTitle")
                        .executes {
                            val enabled = ConfigHelper.toggleTempBuffExpiredTitle()
                            ChatUtils.sendMessage("§eTemporary buff expired title " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )

                // Foraging commands
                .then(
                    ClientCommands.literal("toggleForagingStats")
                        .executes {
                            val enabled = ConfigHelper.toggleForagingStats()
                            ChatUtils.sendMessage("§eForaging stats overlay " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleForagingStatsOnlyOnForagingIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleForagingStatsOnlyOnForagingIslands()
                            ChatUtils.sendMessage("§eForaging stats overlay in foraging islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleAxeAbility")
                        .executes {
                            val enabled = ConfigHelper.toggleAxeAbility()
                            ChatUtils.sendMessage("§eAxe ability display " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleAxeAbilityOnlyOnForagingIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleAxeAbilityOnlyOnForagingIslands()
                            ChatUtils.sendMessage("§eAxe ability display in foraging islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleAxeAbilityReadyTitle")
                        .executes {
                            val enabled = ConfigHelper.toggleAxeAbilityReadyTitle()
                            ChatUtils.sendMessage("§eAxe ability ready title " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleAxeAbilityExpiredTitle")
                        .executes {
                            val enabled = ConfigHelper.toggleAxeAbilityExpiredTitle()
                            ChatUtils.sendMessage("§eAxe ability expired title " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleLottery")
                        .executes {
                            val enabled = ConfigHelper.toggleLottery()
                            ChatUtils.sendMessage("§eLottery " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleLotteryOnlyOnForagingIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleLotteryOnlyOnForagingIslands()
                            ChatUtils.sendMessage("§eLottery in foraging islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleLotteryChatMessages")
                        .executes {
                            val enabled = ConfigHelper.toggleLotteryChatMessages()
                            ChatUtils.sendMessage("§eLottery chat messages " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleBeekeeper")
                        .executes {
                            val enabled = ConfigHelper.toggleBeekeeper()
                            ChatUtils.sendMessage("§eBeekeeper " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleBeekeeperOnlyOnForagingIslands")
                        .executes {
                            val enabled = ConfigHelper.toggleBeekeeperOnlyOnForagingIslands()
                            ChatUtils.sendMessage("§eBeekeeper in foraging islands only " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
                .then(
                    ClientCommands.literal("toggleBeekeeperChatMessages")
                        .executes {
                            val enabled = ConfigHelper.toggleBeekeeperChatMessages()
                            ChatUtils.sendMessage("§eBeekeeper chat messages " + if (enabled) "§aenabled." else "§cdisabled.", true)
                            SkyblockCollectionTracker.configManager.save()
                            1
                        }
                )
        )

        dispatcher.register(sct)
    }
}
