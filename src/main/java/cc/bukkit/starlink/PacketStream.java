package cc.bukkit.starlink;

import java.util.Queue;

import com.google.common.collect.Lists;

import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class PacketStream {
  private final Queue<Packet<?>> packets = Lists.newLinkedList();
  
  public static PacketStream create() {
      return new PacketStream();
  }
  
  public PacketStream flow(Packet<?> packet) {
      packets.add(packet);
      return this;
  }
  
  public PacketStream send(NetworkManager networkManager) {
      networkManager.sendPackets(packets);
      return this;
  }
}
