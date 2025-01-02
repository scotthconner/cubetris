package com.scotthconner.cubetrisrebooted.lib.object.text;

import com.scotthconner.cubetrisrebooted.lib.render.core.Camera;
import com.scotthconner.cubetrisrebooted.lib.render.core.SceneObject;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.Font;
import com.scotthconner.cubetrisrebooted.lib.render.sprite.IBlendFunction;

/**
 * FontText
 *
 * Renderable object added to the screen that manages the state for rendering a string of text
 * to the screen. Uses the string, the justification, and knowledge of the font class structure
 * to render justified text with proper kerning.
 *
 * Created by scottc on 2/23/16.
 */
public class FontText extends SceneObject {
    private Font                   mFont;          // the font to use to render the text
    private IBlendFunction         mBlendFunction; // the blend function to render with
    private Font.TextJustification mJustification; // how the text justifies to the position
    private String                 mText;          // the text to render itself
    private int                    mTextWidth;     // calculated width

    // FONTTEXT METHODS /////////////////////////////////////////////
    public FontText(Font f) {
        super();
        mFont = f;
    }

    /**
     * Sets the text of the object and re-calculates the text width.
     *
     * @param t what you want to set the text to
     */
    public void setText(String t) {
        mText = t;
        mTextWidth = mFont.calculateTextWidth(mText);
    }
    // END FONTTEXT METHODS /////////////////////////////////////////

    // IRENDERABLE OVERRIDES ////////////////////////////////////////
    @Override
    public boolean update(long msDelta) {
        return true;
    }

    @Override
    public void render(Camera camera) {
        // calculate the true position based on the text
        // width and the justification
        float xCursor = mPosition.x;
        switch(mJustification) {
            case JUSTIFY_CENTER:
                xCursor -= mTextWidth / 2.0f;
                break;
            case JUSTIFY_RIGHT:
                xCursor -= mTextWidth;
                break;
            default:
            case JUSTIFY_LEFT:
                break;
        }

        // get each sprint, and render it at the offset, added, and kerned position
        int textLength = mText.length();
        mBlendFunction.enable();
        for(int x = 0; x < textLength; x++) {
            char c = mText.charAt(x);

            // grab each sprite, and render it at the location using the camera
            mFont.getLetterSprite(c).render(camera,
                    xCursor + mFont.getCharacterXOffset(c),
                    mPosition.y - mFont.getCharacterYOffset(c),
                    mPosition.z);

            // push the x cursor forward by the width and the differential based on the kerning
            // of the next character if there is one
            xCursor += mFont.getCharacterAdvance(c);
            if (x + 1 < textLength) {
                xCursor += mFont.getKerningValue(c, mText.charAt(x+1));
            }
        }
        mBlendFunction.disable();
    }

    public void cleanup() {}
    // IRENDERABLE OVERRIDES ////////////////////////////////////////

    // BUILDER //////////////////////////////////////////////////////
    public FontText withText(String s) {
        setText(s);
        return this;
    }

    public FontText withJustification(Font.TextJustification j) {
        mJustification = j;
        return this;
    }

    public FontText withBlendFunction(IBlendFunction b) {
        mBlendFunction = b;
        return this;
    }
    /////////////////////////////////////////////////////////////////
}
