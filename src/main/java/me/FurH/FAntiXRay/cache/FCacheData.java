package me.FurH.FAntiXRay.cache;

import java.io.Serializable;
import java.util.Arrays;

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

    @Override
    public int hashCode() {
        int hashc = 5;

        hashc = 97 * hashc + (this.world != null ? this.world.hashCode() : 0);
        hashc = 97 * hashc + this.x;
        hashc = 97 * hashc + this.z;
        hashc = 97 * hashc + (int) (this.hash ^ (this.hash >>> 32));
        hashc = 97 * hashc + this.engine;

        return hashc;
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

        if (this.x != other.x) {
            return false;
        }
        
        if (this.z != other.z) {
            return false;
        }

        if (this.hash != other.hash) {
            return false;
        }
        
        if (this.engine != other.engine) {
            return false;
        }
        
        if ((this.world == null) ? (other.world != null) : !this.world.equals(other.world)) {
            return false;
        }
        
        if (!Arrays.equals(this.obfuscated, other.obfuscated)) {
            return false;
        }
        
        return true;
    }
}
