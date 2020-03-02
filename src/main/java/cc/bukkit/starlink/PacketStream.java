package cc.bukkit.starlink;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import com.google.common.collect.Lists;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet;

public class PacketStream implements Iterable<Packet<?>> {
  private final NetworkManager manager;
  private final Queue<Packet<?>> packets = Lists.newLinkedList();
  private boolean flows;
  
  public PacketStream(NetworkManager networkManager) {
      manager = networkManager;
  }
  
  public static PacketStream create(NetworkManager networkManager) {
      return new PacketStream(networkManager);
  }
  
  public static PacketStream from(EntityPlayer player) {
      return player.playerConnection.networkManager.stream();
  }
  
  public PacketStream flows() {
      flows = true;
      return this;
  }
  
  public PacketStream write(Packet<?> packet) {
      packets.add(packet);
      return this;
  }
  
  public PacketStream writeAndFlush(Packet<?> packet) {
      packets.add(packet);
      return flows ? this : flush();
  }
  
  public PacketStream flush() {
      manager.sendPackets(packets.toArray());
      packets.clear();
      flows = false; // close anyway
      return this;
  }
  
  @Override
  public Iterator<Packet<?>> iterator() {
      return packets.iterator();
  }
}
