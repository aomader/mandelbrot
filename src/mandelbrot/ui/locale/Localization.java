package mandelbrot.ui.locale;

import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A simple helper class to provide convenience methods for localization.
 */
public class Localization {

    private static ResourceBundle bundle =
        ResourceBundle.getBundle("mandelbrot.ui.locale.Localization");

    /**
     * Tries to find a localization within the Localization resource bundle.
     * @param key The key to lookup.
     * @return Either the maybe correctly localized string or the key.
     */
    public static String get(String key) {
        String result = null;

        try {
            result = bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }

        if (result == null || result.length() == 0) {
            return key;
        }

        // the properties file is saved as UTF-8, but it is read as
        // ISO-8859-1, therefore we need to convert the results
        try {
            return new String(result.getBytes("ISO-8859-1"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return key;
        }
    }

}
