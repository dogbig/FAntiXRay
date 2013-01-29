package me.FurH.FAntiXRay.hook;

import me.FurH.FAntiXRay.FAntiXRay;
import me.FurH.FAntiXRay.obfuscation.FObfuscator;
import me.FurH.FAntiXRay.threads.FPacketData;
import net.minecraft.server.v1_4_R1.EntityPlayer;
import net.minecraft.server.v1_4_R1.INetworkManager;
import net.minecraft.server.v1_4_R1.MinecraftServer;
import net.minecraft.server.v1_4_R1.Packet;
import net.minecraft.server.v1_4_R1.Packet0KeepAlive;
import net.minecraft.server.v1_4_R1.Packet101CloseWindow;
import net.minecraft.server.v1_4_R1.Packet102WindowClick;
import net.minecraft.server.v1_4_R1.Packet106Transaction;
import net.minecraft.server.v1_4_R1.Packet107SetCreativeSlot;
import net.minecraft.server.v1_4_R1.Packet108ButtonClick;
import net.minecraft.server.v1_4_R1.Packet10Flying;
import net.minecraft.server.v1_4_R1.Packet130UpdateSign;
import net.minecraft.server.v1_4_R1.Packet14BlockDig;
import net.minecraft.server.v1_4_R1.Packet15Place;
import net.minecraft.server.v1_4_R1.Packet16BlockItemSwitch;
import net.minecraft.server.v1_4_R1.Packet18ArmAnimation;
import net.minecraft.server.v1_4_R1.Packet19EntityAction;
import net.minecraft.server.v1_4_R1.Packet202Abilities;
import net.minecraft.server.v1_4_R1.Packet203TabComplete;
import net.minecraft.server.v1_4_R1.Packet204LocaleAndViewDistance;
import net.minecraft.server.v1_4_R1.Packet205ClientCommand;
import net.minecraft.server.v1_4_R1.Packet250CustomPayload;
import net.minecraft.server.v1_4_R1.Packet255KickDisconnect;
import net.minecraft.server.v1_4_R1.Packet3Chat;
import net.minecraft.server.v1_4_R1.Packet51MapChunk;
import net.minecraft.server.v1_4_R1.Packet56MapChunkBulk;
import net.minecraft.server.v1_4_R1.Packet7UseEntity;
import net.minecraft.server.v1_4_R1.Packet9Respawn;
import net.minecraft.server.v1_4_R1.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftPlayer;

/**
 *
 * @author FurmigaHumana
 */
public class FPlayerConnection extends PlayerConnection {
    
    public FPlayerConnection(MinecraftServer minecraftserver, INetworkManager inetworkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, inetworkmanager, entityplayer);
    }

    @Override
    public void sendPacket(Packet packet) {

        if (packet instanceof Packet56MapChunkBulk || packet instanceof Packet56MapChunkBulk) {
            if (FAntiXRay.getConfiguration().thread_enabled || FAntiXRay.getConfiguration().thread_player) {
                FAntiXRay.getThreadManager().add(new FPacketData(player, packet));
                return;
            }
        }
        
        if (packet instanceof Packet56MapChunkBulk) {
            Packet56MapChunkBulk p56 = (Packet56MapChunkBulk)packet;
            packet = FObfuscator.obfuscate(player, p56, false);
        } else
        if (packet instanceof Packet51MapChunk) {
            Packet51MapChunk p51 = (Packet51MapChunk)packet;
            packet = FObfuscator.obfuscate(player, p51, false);
        }

        super.sendPacket(packet);
    }
    
    @Override
    public CraftPlayer getPlayer() {
        return super.getPlayer();
    }

    @Override
    public void d() {
        super.d();
    }

    @Override
    public void disconnect(String s) {
        super.disconnect(s);
    }

    @Override
    public void a(Packet10Flying packet10flying) {
        super.a(packet10flying);
    }

    @Override
    public void a(double d0, double d1, double d2, float f, float f1) {
        super.a(d0, d1, d2, f, f1);
    }

    @Override
    public void teleport(Location dest) {
        super.teleport(dest);
    }

    @Override
    public void a(Packet14BlockDig packet14blockdig) {
        super.a(packet14blockdig);
    }

    @Override
    public void a(Packet15Place packet15place) {
        super.a(packet15place);
    }

    @Override
    public void a(String s, Object[] aobject) {
        super.a(s, aobject);
    }

    @Override
    public void onUnhandledPacket(Packet packet) {
        super.onUnhandledPacket(packet);
    }

    @Override
    public void a(Packet16BlockItemSwitch packet16blockitemswitch) {
        super.a(packet16blockitemswitch);
    }

    @Override
    public void a(Packet3Chat packet3chat) {
        super.a(packet3chat);
    }

    @Override
    public void chat(String s, boolean async) {
        super.chat(s, async);
    }

    @Override
    public void a(Packet18ArmAnimation packet18armanimation) {
        super.a(packet18armanimation);
    }

    @Override
    public void a(Packet19EntityAction packet19entityaction) {
        super.a(packet19entityaction);
    }

    @Override
    public void a(Packet255KickDisconnect packet255kickdisconnect) {
        super.a(packet255kickdisconnect);
    }

    @Override
    public int lowPriorityCount() {
        return super.lowPriorityCount();
    }

    @Override
    public void a(Packet7UseEntity packet7useentity) {
        super.a(packet7useentity);
    }

    @Override
    public void a(Packet205ClientCommand packet205clientcommand) {
        super.a(packet205clientcommand);
    }

    @Override
    public boolean b() {
        return super.b();
    }

    @Override
    public void a(Packet9Respawn packet9respawn) {
        super.a(packet9respawn);
    }

    @Override
    public void handleContainerClose(Packet101CloseWindow packet101closewindow) {
        super.handleContainerClose(packet101closewindow);
    }

    @Override
    public void a(Packet102WindowClick packet102windowclick) {
        super.a(packet102windowclick);
    }

    @Override
    public void a(Packet108ButtonClick packet108buttonclick) {
        super.a(packet108buttonclick);
    }

    @Override
    public void a(Packet107SetCreativeSlot packet107setcreativeslot) {
        super.a(packet107setcreativeslot);
    }

    @Override
    public void a(Packet106Transaction packet106transaction) {
        super.a(packet106transaction);
    }

    @Override
    public void a(Packet130UpdateSign packet130updatesign) {
        super.a(packet130updatesign);
    }

    @Override
    public void a(Packet0KeepAlive packet0keepalive) {
        super.a(packet0keepalive);
    }

    @Override
    public boolean a() {
        return super.a();
    }

    @Override
    public void a(Packet202Abilities packet202abilities) {
        super.a(packet202abilities);
    }

    @Override
    public void a(Packet203TabComplete packet203tabcomplete) {
        super.a(packet203tabcomplete);
    }

    @Override
    public void a(Packet204LocaleAndViewDistance packet204localeandviewdistance) {
        super.a(packet204localeandviewdistance);
    }

    @Override
    public void a(Packet250CustomPayload packet250custompayload) {
        super.a(packet250custompayload);
    }
}