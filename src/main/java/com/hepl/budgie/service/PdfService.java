package com.hepl.budgie.service;

import java.io.IOException;

public interface PdfService {

    byte[] generatePdf(String html) throws IOException;

    // byte[] generatePdfFromDocText(String text) throws DocumentException;

}
