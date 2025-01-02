package com.scotthconner.cubetrisrebooted.lib.render.sprite;

import android.content.Context;
import android.util.Log;

import com.scotthconner.cubetrisrebooted.R;
import com.scotthconner.cubetrisrebooted.lib.core.AndroidUtils;
import com.scotthconner.cubetrisrebooted.lib.core.TextureManager;

import java.util.HashMap;

/**
 * Class that manages fonts, and their OpenGL resources. Based on the
 * uv coordinate file format from bmGlyph
 *
 * Created by scottc on 2/15/16.
 */
public class Font {
    // FONT MANAGER ////////////////////////////////////////////////////////////////
    //
    // The font class comes with an embedded manager. The only way to get a font is
    // to call the static method #create.
    ////////////////////////////////////////////////////////////////////////////////
    // we store this so the gamestate doesn't need access to the application context
    private static Context mContext = null;
    private static HashMap<String, Font> mFonts = null;

    /**
     * Called once when the main activity is created
     * @param cxt the application context from the main activity
     */
    public static void setContext(Context cxt) {
        mContext = cxt;
        if (null == mFonts) {
            mFonts = new HashMap<String, Font>();
        }
    }

    /**
     * Loads a font, or returns it if a label has already been loaded
     * @param resourceId the .fnt file you want to load
     * @param label the string name you want to give the font
     * @return the loaded Font object.
     */
    public static Font load(int resourceId, String label) {
        if (mFonts.containsKey(label)) {
            return mFonts.get(label);
        }

        Font newFont = new Font(resourceId);
        mFonts.put(label, newFont);
        return newFont;
    }

    /**
     * If you expect a font to already be loaded, grab a static
     * reference to it here.
     * @param label the string name you expect for the font you want.
     * @return the pre-loaded font object.
     */
    public static Font getFont(String label) {
        return mFonts.get(label);
    }
    ////////////////////////////////////////////////////////////////////////////////
    // END FONT MANAGER
    ////////////////////////////////////////////////////////////////////////////////

    // FONT LETTER /////////////////////////////////////////////////////////////////
    //
    // A font is made up of a dictionary of "letters". This includes all the data
    // needed to generate sprites, as well as holds kerning information.
    ////////////////////////////////////////////////////////////////////////////////
    private class Letter {
        public int mCharId;     // the ASCII of the character.
        public float uvStartX;  // the texture coordinate x start position
        public float uvStartY;  // the texture coordinate y start position
        public float uvEndX;    // the texture coordinate x end position
        public float uvEndY;    // the texture coordinate y end position
        public float mSizeX;    // the pixel width of the letter
        public float mSizeY;    // the pixel height of the letter
        public float mXOffset;  // the x offset of the letter within its bounding box
        public float mYOffset;  // the y offset of the letter within its bounding box
        public float mXAdvance; // how far the cursor needs to continue forward after printing

        public Sprite mSprite;  // initialized sprint instance

        // determines the kerning from this character to all others. an absense simply
        // means the kerning for the next letter is 0.
        private HashMap<Character, Integer> mKerningMap;

        public Letter() {
            mSprite = null;
            mKerningMap = new HashMap<Character, Integer>();
        }

        public void addKerningConfiguration(char c, int kerning) {
            mKerningMap.put(c, kerning);
        }

        public int getKerningValue(char c) {
            if (mKerningMap.containsKey(c)) {
                return mKerningMap.get(c).intValue();
            }

            return 0;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////
    // END FONT LETTER
    ////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////
    // FONT CONSTANTS and ENUMS
    ////////////////////////////////////////////////////////////////////////////////
    public static enum TextJustification {
        JUSTIFY_LEFT,
        JUSTIFY_CENTER,
        JUSTIFY_RIGHT
    }
    ////////////////////////////////////////////////////////////////////////////////
    // End FONT CONSTANTS and ENUMS
    ////////////////////////////////////////////////////////////////////////////////

    // Font Instance Properties/////////////////////////////////////////////////////
    private String mFontFace;   // an informative string of the font's origin
    private int    mFontSize;   // the pixel size of the font
    private int    mLineHeight; // the pixel height of a font's line
    private int    mBaseLine;   // the line's base Y offset from the print cursor

    // texture information
    private String mGLTextureLabel;     // the texture label for the texture manager
    private int    mTextureId;

    // holds all of the font's character definitions
    private HashMap<Character, Letter> mDictionary;
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Releases the resource for the font.
     */
    public void release() {
        TextureManager.getInstance().clearTexture(mGLTextureLabel);
        mDictionary = null;
    }

    /**
     *
     * Used to get the raw Sprite instance for a given character.
     *
     * @param letter the character you are looking for the sprite of.
     * @return the Sprite instance that is that letter
     */
    public Sprite getLetterSprite(char letter) {
        return mDictionary.get(letter).mSprite;
    }

    /**
     * Returns the advance width of the given font's character. will throw an exception
     * if it is not a character supported by the font.
     *
     * @param c the character you want the width of
     * @return a float containing the width of the character.
     */
    public float getCharacterAdvance(char c ) {
        return mDictionary.get(c).mXAdvance;
    }

    /**
     * Returns the x offset of the given font's character. This means if you want to
     * show this character at position X, you would want to also offset it with this amount.
     *
     * @param c the character
     * @return the additional x offset you should use when rendering this
     */
    public float getCharacterXOffset(char c) {
        return mDictionary.get(c).mXOffset;
    }

    /**
     * Returns the y offset of the given font's character. This means if you want to
     * show this character at position Y, you would want to also offset it with this amount.
     *
     * @param c the character
     * @return the additional y offset you should use when rendering this
     */
    public float getCharacterYOffset(char c) {
        return mDictionary.get(c).mYOffset;
    }

    /**
     * Retrieves the kerning value for a left and right character.
     *
     * @param left
     * @param right
     * @return the kerning value for those two characters
     */
    public float getKerningValue(char left, char right) {
        return mDictionary.get(left).getKerningValue(right);
    }

    /**
     * Calculates the width of a string with font kerning.
     *
     * @param text the text you want to measure
     * @return and integer describing the width of the string
     */
    public int calculateTextWidth(String text) {
        int textSize = text.length();
        int width = 0;
        for(int x = 0; x < textSize; x++) {
            char c = text.charAt(x);
            width += getCharacterAdvance(c);

            // if we are not at the end, subtract the next letter's kerning value
            if (x + 1 < textSize) {
                width += mDictionary.get(c).getKerningValue(text.charAt(x+1));
            }
        }

        return width;
    }

    /**
     * Uses the application context set in setContext.
     *
     * @param resourceId the raw resource ID of the .fnt file
     */
    private Font(int resourceId) {
        mDictionary = new HashMap<Character, Letter>();

        String fontFileContents = AndroidUtils.readRawTextFile(mContext, resourceId);
        String[] lines = fontFileContents.split("\n");

        // parse the font property lines, we assume there are 4, and we
        // fill in the properties as we know them.
        HashMap<String, String> fontProperties = parseFontLine(lines[0]);
        fontProperties.putAll(parseFontLine(lines[1]));
        fontProperties.putAll(parseFontLine(lines[2]));
        fontProperties.putAll(parseFontLine(lines[3]));

        mFontFace = fontProperties.get("fontFace");
        mFontSize = Integer.parseInt(fontProperties.get("fontSize"));
        mLineHeight = Integer.parseInt(fontProperties.get("lineHeight"));
        mBaseLine = Integer.parseInt(fontProperties.get("baseLine"));
        mGLTextureLabel = fontProperties.get("textureFile").replace("\"", "").replace(".png","");

        int charCount = Integer.parseInt(fontProperties.get("charsCount"));

        // use reflection to find the label of the texture in the resources, and load
        // the texture into the texture manager
        int fontTextureResourceId = AndroidUtils.getId(mGLTextureLabel, R.drawable.class);
        mTextureId = TextureManager.getInstance().loadTexture(fontTextureResourceId, mGLTextureLabel);

        // fill the dictionary
        for(int x = 4; x < 4 + charCount; x++) {
            // pull the letter properties out of the line in the file
            Letter l = parseLetterLine(lines[x]);

            // Generate the sprite's geometry and buffers for rendering and
            // store a reference of it in the letter dictionary
            Sprite.Definition spriteDef = new Sprite.Definition();
            spriteDef.mTextureId = mTextureId;
            spriteDef.mSizeX = l.mSizeX;
            spriteDef.mSizeY = l.mSizeY;
            spriteDef.uvStartX = l.uvStartX;
            spriteDef.uvStartY = l.uvStartY;
            spriteDef.uvEndX = l.uvEndX;
            spriteDef.uvEndY = l.uvEndY;
            spriteDef.mCentered = false;
            l.mSprite = new Sprite(spriteDef);

            mDictionary.put(new Character((char)l.mCharId), l);
        }

        int kerningCount = 0;
        // fill the kerning, skipping the kerning count line
        for(int x = 5 + charCount; x < lines.length; x++) {
            HashMap<String, String> kerningProperties = parseFontLine(lines[x]);
            char character = (char)Integer.parseInt(kerningProperties.get("first"));
            Letter l = mDictionary.get(character);
            char c = (char)Integer.parseInt(kerningProperties.get("second"));
            int amount = Integer.parseInt(kerningProperties.get("amount"));
            l.addKerningConfiguration(c, amount);
            kerningCount++;
        }

        Log.d("Font::load", "loaded font '" + this.mFontFace + "' with " + this.mDictionary.size() +
            " characters and " + kerningCount + " kerning definitions.");
    }

    /**
     * Parses a map of keys and values from a bmGlyph .fnt file
     * @param line a line read from a file
     * @return a map of strings and values
     */
    private HashMap<String,String> parseFontLine(String line) {
        HashMap<String,String> tupleMap = new HashMap<>();
        String[] tuples = line.replace(" ","").split("#");

        for(int x = 0; x < tuples.length; x++) {
            if (tuples[x].length() != 0 ) {
                String[] t = tuples[x].split("=");
                if (t.length == 2) { // could be a #kerning line
                    tupleMap.put(t[0], t[1]);
                }
            }
        }

        return tupleMap;
    }

    /**
     * Generates a letter from the fnt file line.
     *
     * @param line line from a fnt file
     * @return the fully formed letter object
     */
    private Letter parseLetterLine(String line) {
        HashMap<String, String> letterProperties = parseFontLine(line);
        Letter l = new Letter();
        l.mCharId = Integer.parseInt(letterProperties.get("charId"));

        float[] uvStart = parseCoord(letterProperties.get("uvStart"));
        l.uvStartX = uvStart[0];
        l.uvStartY = uvStart[1];

        float[] uvEnd = parseCoord(letterProperties.get("uvEnd"));
        l.uvEndX = uvEnd[0];
        l.uvEndY = uvEnd[1];

        float[] size = parseCoord(letterProperties.get("size"));
        l.mSizeX = size[0];
        l.mSizeY = size[1];

        l.mXOffset = Integer.parseInt(letterProperties.get("xOffset"));
        l.mYOffset = Integer.parseInt(letterProperties.get("yOffset"));
        l.mXAdvance = Integer.parseInt(letterProperties.get("xAdvance"));

        return l;
    }

    /**
     * Used for parsing strings like "(0.234,0.235)"
     *
     * @param coord the chopped coord from the font file
     * @return a tuple of floats
     */
    private float[] parseCoord(String coord) {
        String[] s = coord.replace(")","").replace("(","").split(",");
        float[]  f = {Float.parseFloat(s[0]), Float.parseFloat(s[1])};
        return f;
    }
}
