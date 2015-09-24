package org.helioviewer.jhv.gui;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import org.helioviewer.base.FileUtils;
import org.helioviewer.base.logging.Log;

public class UIGlobals {

    private static UIGlobals instance;

    private UIGlobals() {
    }

    // always use getSingletonInstance
    public static UIGlobals getSingletonInstance() {
        if (instance == null) {
            instance = new UIGlobals();

            InputStream is = FileUtils.getResourceInputStream("/fonts/RobotoCondensed-Regular.ttf");
            try {
                UIFontRoboto = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (Exception e) {
                Log.warn("Font not loaded correctly, fallback to default");
                UIFontRoboto = new Font("SansSerif", Font.PLAIN, 20);
            }

            if (System.getProperty("jhv.os").equals("mac")) {
                Hashtable<TextAttribute, Object> map = new Hashtable<TextAttribute, Object>();
                map.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
                map.put(TextAttribute.FAMILY, "HelveticaNeue");

                map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
                map.put(TextAttribute.SIZE, 12);

                Font font = new Font(map);
                if (font == null)
                    return instance;

                UIFont = font;

                map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                map.put(TextAttribute.SIZE, 11);
                UIFontBold = font.deriveFont(map);

                map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
                map.put(TextAttribute.SIZE, 10);
                UIFontSmall = font.deriveFont(map);

                map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
                map.put(TextAttribute.SIZE, 9);
                UIFontSmallBold = font.deriveFont(map);
            }
        }
        return instance;
    }

    public void setUIFont(Font font) {
        FontUIResource f = new FontUIResource(font);
        Enumeration<?> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value != null && value instanceof FontUIResource)
                UIManager.put(key, f);
        }
    }

    public static Font UIFont = new Font("SansSerif", Font.PLAIN, 12);
    public static Font UIFontBold = new Font("SansSerif", Font.BOLD, 11);

    public static Font UIFontSmall = new Font("SansSerif", Font.PLAIN, 10);
    public static Font UIFontSmallBold = new Font("SansSerif", Font.BOLD, 9);

    public static Font UIFontMono = new Font("Courier", Font.PLAIN, 12);

    public static Font UIFontRoboto;

}
