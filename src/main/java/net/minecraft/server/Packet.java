package net.minecraft.server;

import java.io.IOException;

public interface Packet<T extends PacketListener> {

    void a(PacketDataSerializer packetdataserializer) throws IOException;

    void b(PacketDataSerializer packetdataserializer) throws IOException;

    void a(T t0);

    default boolean a() {
        return false;
    }
    
    // StarLink start - add protocol
    default EnumProtocol protocol() {
	return EnumProtocol.PLAY; // trick to avoid so much imports
    }
    // StarLink end
}
