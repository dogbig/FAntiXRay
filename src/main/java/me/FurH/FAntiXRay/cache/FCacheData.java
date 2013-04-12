package me.FurH.FAntiXRay.cache;

import java.io.Serializable;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class FCacheData implements Serializable {

    private static final long serialVersionUID = -12598846547156L;

    public String world;
    public int x;
    public int z;
    public byte[] obfuscated;
    public long hash;
    public int engine;

    public FCacheData(String world, int x, int z, byte[] obfuscated, long hash, int engine) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.obfuscated = obfuscated;
        this.hash = hash;
        this.engine = engine;
    }
}
