package me.FurH.FAntiXRay.cache;

import java.io.Serializable;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class FCacheData implements Serializable {

    private static final long serialVersionUID = -3881844511928889180L;

    public String world;
    public byte[] inflatedBuffer;
    public long hash;
    public long key;
    public int engine;

    public FCacheData(String world, long key, byte[] obfuscated, long hash, int engine) {
        this.key = key;
        this.inflatedBuffer = obfuscated;
        this.hash = hash;
        this.engine = engine;
        this.world = world;
    }

    @Override
    public int hashCode() {
        int hash1 = 7;
        
        hash1 = 53 * hash1 + (this.world != null ? this.world.hashCode() : 0);
        hash1 = 53 * hash1 + (int) (this.hash ^ (this.hash >>> 32));
        hash1 = 53 * hash1 + (int) (this.key ^ (this.key >>> 32));
        hash1 = 53 * hash1 + this.engine;
        
        return hash1;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final FCacheData other = (FCacheData) obj;
        if (this.engine != other.engine) {
            return false;
        }

        if (this.hash != other.hash) {
            return false;
        }

        if (this.key != other.key) {
            return false;
        }

        if ((this.world == null) ? (other.world != null) : !this.world.equals(other.world)) {
            return false;
        }

        return true;
    }
}