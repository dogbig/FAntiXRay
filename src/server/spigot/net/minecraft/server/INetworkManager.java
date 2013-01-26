package net.minecraft.server;

import java.net.SocketAddress;
import me.FurH.server.FAntiXRay.FObfuscationRequest;

/**
 *
 * @author FurmigaHumana
 */
public interface INetworkManager {
    
    public void a(FObfuscationRequest request);

    public void a(Connection cnctn);

    public void queue(Packet packet);

    public void a();

    public void b();

    public SocketAddress getSocketAddress();

    public void d();

    public int e();

    public void a(String string, Object[] os);
}
