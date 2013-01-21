package me.FurH.server.FAntiXRay;

import java.lang.reflect.Field;
import net.minecraft.server.Block;

/**
 *
 * @author FurmigaHumana
 */
public class FUtils {

    /* get a private field */
    public static Object getPrivateField(Object obj, String x) {
        try {
            Field f = obj.getClass().getDeclaredField(x);
            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /* return true if the id is a transparent block */
    public static boolean isTransparent(int id) {
        if (id == 0) {
            return true;
        }
        
        if (id == 1) {
            return false;
        }
        
        return Block.i(id);
    }
}