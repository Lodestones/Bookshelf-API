package to.lodestone.bookshelfapi.api.util;

public class EnumUtil {
    public static <T extends Enum<T>> T fetchEnum(Class<T> enumClass, String value) {
        try {
            if (value == null)
                return null;
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static <T extends Enum<T>> T fetchEnum(Class<T> enumClass, String value, T defaultEnum) {
        try {
            if (value == null)
                return defaultEnum;
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            return defaultEnum;
        }
    }

}
