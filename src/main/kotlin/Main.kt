import net.minestom.server.MinecraftServer
import net.minestom.server.entity.*
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerLoginEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.DimensionType
import kotlin.experimental.and
import kotlin.random.Random

fun main()
{
    val minecraftServer = MinecraftServer.init()
    val instanceManager = MinecraftServer.getInstanceManager()
    val dimension = DimensionType.builder(NamespaceID.from("minestom:fullbright"))
        .ambientLight(2f)
        .build()
    MinecraftServer.getDimensionTypeManager().addDimension(dimension)
    val instance = instanceManager.createInstanceContainer(dimension)

    instance.setGenerator { unit ->
        val bottom = unit.absoluteStart().y().toInt()
        if (Random.nextBoolean())
            unit.modifier().fillHeight(bottom, bottom + 3, Block.PACKED_ICE)
        else
            unit.modifier().fillHeight(bottom, bottom + 1, Block.PACKED_ICE)
    }

    val globalEventHandler = MinecraftServer.getGlobalEventHandler()

    globalEventHandler.addListener(PlayerLoginEvent::class.java) { event ->
        event.setSpawningInstance(instance)
        event.player.gameMode = GameMode.CREATIVE
        event.player.inventory.setItemInHand(Player.Hand.MAIN, ItemStack.of(Material.OAK_BOAT))
    }

    globalEventHandler.addListener(PlayerBlockInteractEvent::class.java) { event ->
        if (event.player.inventory.itemInMainHand.material() != Material.OAK_BOAT)
            return@addListener

        val boat = Entity(EntityType.BOAT)
        boat.setInstance(instance, event.blockPosition.add(0.0, 1.0, 0.0))
        boat.addPassenger(event.player)
    }

    globalEventHandler.addListener(PlayerPacketEvent::class.java) { event ->
        val packet = event.packet

        if (packet !is ClientSteerVehiclePacket)
            return@addListener

        if (packet.flags and 0x2 == 0.toByte())
            return@addListener

        val player = event.player
        val vehicle = player.vehicle
        vehicle?.removePassenger(player)
        vehicle?.remove()
        player.teleport(player.position.add(0.0, 1.0, 0.0))
    }

    minecraftServer.start("localhost", 25565)
}
