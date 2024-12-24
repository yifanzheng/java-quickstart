package top.yifan.pdf;

import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.DefaultResourceCache;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.shading.PDShading;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.io.IOException;

/**
 * @author zhengyifan
 */
public class PdfResourceCache extends DefaultResourceCache {
    public PdfResourceCache() {
        super();
    }

    @Override
    public PDFont getFont(COSObject indirect) throws IOException {
        return super.getFont(indirect);
    }

    @Override
    public void put(COSObject indirect, PDFont font) throws IOException {
        super.put(indirect, font);
    }

    @Override
    public PDColorSpace getColorSpace(COSObject indirect) throws IOException {
        return super.getColorSpace(indirect);
    }

    @Override
    public void put(COSObject indirect, PDColorSpace colorSpace) throws IOException {
        super.put(indirect, colorSpace);
    }

    @Override
    public PDExtendedGraphicsState getExtGState(COSObject indirect) {
        return super.getExtGState(indirect);
    }

    @Override
    public void put(COSObject indirect, PDExtendedGraphicsState extGState) {
        super.put(indirect, extGState);
    }

    @Override
    public PDShading getShading(COSObject indirect) throws IOException {
        return super.getShading(indirect);
    }

    @Override
    public void put(COSObject indirect, PDShading shading) throws IOException {
        super.put(indirect, shading);
    }

    @Override
    public PDAbstractPattern getPattern(COSObject indirect) throws IOException {
        return super.getPattern(indirect);
    }

    @Override
    public void put(COSObject indirect, PDAbstractPattern pattern) throws IOException {
        super.put(indirect, pattern);
    }

    @Override
    public PDPropertyList getProperties(COSObject indirect) {
        return super.getProperties(indirect);
    }

    @Override
    public void put(COSObject indirect, PDPropertyList propertyList) {
        super.put(indirect, propertyList);
    }

    @Override
    public PDXObject getXObject(COSObject indirect) throws IOException {
        return super.getXObject(indirect);
    }

    @Override
    public void put(COSObject indirect, PDXObject xobject) throws IOException {
        super.put(indirect, xobject);
    }
}
