package com.sismics.util;

import com.sismics.BaseTest;
import com.sismics.util.mime.MimeType;
import com.sismics.util.mime.MimeTypeUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test of the utilities to check MIME types.
 * 
 * @author bgamard
 */
public class TestMimeTypeUtil extends BaseTest {
    @Test
    public void test() throws Exception {
        // Detect ODT files
        Path path = Paths.get(getResource(FILE_ODT).toURI());
        Assert.assertEquals(MimeType.OPEN_DOCUMENT_TEXT, MimeTypeUtil.guessMimeType(path, FILE_ODT));

        // Detect DOCX files
        path = Paths.get(getResource(FILE_DOCX).toURI());
        Assert.assertEquals(MimeType.OFFICE_DOCUMENT, MimeTypeUtil.guessMimeType(path, FILE_ODT));

        // Detect PPTX files
        path = Paths.get(getResource(FILE_PPTX).toURI());
        Assert.assertEquals(MimeType.OFFICE_PRESENTATION, MimeTypeUtil.guessMimeType(path, FILE_PPTX));

        // Detect XLSX files
        path = Paths.get(getResource(FILE_XLSX).toURI());
        Assert.assertEquals(MimeType.OFFICE_SHEET, MimeTypeUtil.guessMimeType(path, FILE_XLSX));

        // Detect TXT files
        path = Paths.get(getResource(FILE_TXT).toURI());
        Assert.assertEquals(MimeType.TEXT_PLAIN, MimeTypeUtil.guessMimeType(path, FILE_TXT));

        // Detect CSV files
        path = Paths.get(getResource(FILE_CSV).toURI());
        Assert.assertEquals(MimeType.TEXT_CSV, MimeTypeUtil.guessMimeType(path, FILE_CSV));

        // Detect PDF files
        path = Paths.get(getResource(FILE_PDF).toURI());
        Assert.assertEquals(MimeType.APPLICATION_PDF, MimeTypeUtil.guessMimeType(path, FILE_PDF));

        // Detect JPEG files
        path = Paths.get(getResource(FILE_JPG).toURI());
        Assert.assertEquals(MimeType.IMAGE_JPEG, MimeTypeUtil.guessMimeType(path, FILE_JPG));

        // Detect GIF files
        path = Paths.get(getResource(FILE_GIF).toURI());
        Assert.assertEquals(MimeType.IMAGE_GIF, MimeTypeUtil.guessMimeType(path, FILE_GIF));

        // Detect PNG files
        path = Paths.get(getResource(FILE_PNG).toURI());
        Assert.assertEquals(MimeType.IMAGE_PNG, MimeTypeUtil.guessMimeType(path, FILE_PNG));

        // Detect ZIP files
        path = Paths.get(getResource(FILE_ZIP).toURI());
        Assert.assertEquals(MimeType.APPLICATION_ZIP, MimeTypeUtil.guessMimeType(path, FILE_ZIP));

        // Detect WEBM files
        path = Paths.get(getResource(FILE_WEBM).toURI());
        Assert.assertEquals(MimeType.VIDEO_WEBM, MimeTypeUtil.guessMimeType(path, FILE_WEBM));

        // Detect MP4 files
        path = Paths.get(getResource(FILE_MP4).toURI());
        Assert.assertEquals(MimeType.VIDEO_MP4, MimeTypeUtil.guessMimeType(path, FILE_MP4));
    }

    @Test
    public void testGetFileExtension() {
        Assert.assertEquals("zip", MimeTypeUtil.getFileExtension(MimeType.APPLICATION_ZIP));
        Assert.assertEquals("jpg", MimeTypeUtil.getFileExtension(MimeType.IMAGE_JPEG));
        Assert.assertEquals("png", MimeTypeUtil.getFileExtension(MimeType.IMAGE_PNG));
        Assert.assertEquals("pdf", MimeTypeUtil.getFileExtension(MimeType.APPLICATION_PDF));
        Assert.assertEquals("odt", MimeTypeUtil.getFileExtension(MimeType.OPEN_DOCUMENT_TEXT));
        Assert.assertEquals("docx", MimeTypeUtil.getFileExtension(MimeType.OFFICE_DOCUMENT));
        Assert.assertEquals("txt", MimeTypeUtil.getFileExtension(MimeType.TEXT_PLAIN));
        Assert.assertEquals("csv", MimeTypeUtil.getFileExtension(MimeType.TEXT_CSV));
        Assert.assertEquals("webm", MimeTypeUtil.getFileExtension(MimeType.VIDEO_WEBM));
        Assert.assertEquals("mp4", MimeTypeUtil.getFileExtension(MimeType.VIDEO_MP4));
        Assert.assertEquals("bin", MimeTypeUtil.getFileExtension("application/unknown"));
    }

    @Test
    public void testGetFileExtensionDefaultMimeType() {
        Assert.assertEquals("bin", MimeTypeUtil.getFileExtension(MimeType.DEFAULT));
    }

    @Test
    public void testGuessMimeTypeDefaultWhenUnknown() throws Exception {
        Path path = Files.createTempFile("teedy", ".unknownext");
        try {
            Assert.assertEquals(MimeType.DEFAULT, MimeTypeUtil.guessMimeType(path, "file.unknownext"));
        } finally {
            Files.deleteIfExists(path);
        }
    }
}
