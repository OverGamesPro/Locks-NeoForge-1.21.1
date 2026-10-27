package melonslise.locks.common.network.toserver;

import melonslise.locks.Locks;
import melonslise.locks.common.container.LockPickingContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TryPinPacket(byte pin) implements CustomPacketPayload
{
	public static final CustomPacketPayload.Type<TryPinPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Locks.ID, "try_pin"));

	public static final StreamCodec<RegistryFriendlyByteBuf, TryPinPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BYTE, TryPinPacket::pin, TryPinPacket::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type()
	{
		return TYPE;
	}

	public static void handle(TryPinPacket pkt, IPayloadContext ctx)
	{
		if(ctx.player().containerMenu instanceof LockPickingContainer menu)
			menu.tryPin(pkt.pin);
	}
}
