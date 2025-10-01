package gg.lode.bookshelfapi.api.util;

public class ReflectionHelper {

    public static boolean hasClass(final String className) {
        try {
            Class.forName(className);
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

}
