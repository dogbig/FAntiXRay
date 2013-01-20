package org.bukkit.craftbukkit;

import java.lang.reflect.Field;

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
        
        switch (id) {
            case 6:
            case 8:
            case 9:
            case 10:
            case 11:
            case 18:
            case 20:
            case 26:
            case 27:
            case 28:
            case 30:
            case 31:
            case 32:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 44:
            case 50:
            case 52:
            case 53:
            case 55:
            case 59:
            case 63:
            case 64:
            case 65:
            case 66:
            case 67:
            case 68: 
            case 69:
            case 70:
            case 71:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 81:
            case 83:
            case 85:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 101:
            case 102:
            case 104:
            case 105:
            case 106:
            case 107:
            case 111: 
            case 113:
            case 114:
            case 115:
            case 116:
            case 117:
            case 118:
            case 119:
            case 122:
            case 126:
            case 127:
            case 128:
            case 131:
            case 132:
            case 134:
            case 135:
            case 136:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 145:
                return true;
            default:
                return false;
        }
    }
}