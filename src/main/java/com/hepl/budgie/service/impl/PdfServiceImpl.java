package com.hepl.budgie.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.hepl.budgie.service.PdfService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfServiceImpl implements PdfService {

    @Override
    public byte[] generatePdf(String html) throws IOException {
        log.info("Generate pdf from html");

        ByteArrayOutputStream target = new ByteArrayOutputStream();
        String baseUri = new ClassPathResource("/static/").getURL().toExternalForm();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.toStream(target);
        builder.withW3cDocument(new W3CDom().fromJsoup(createWellFormedHtml(html)), baseUri);
        builder.run();

        return target.toByteArray();
    }

    private org.jsoup.nodes.Document createWellFormedHtml(String inputHTML) throws IOException {
        org.jsoup.nodes.Document document = Jsoup.parse(inputHTML, "UTF-8");
        document.outputSettings()
                .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        return document;
    }

}
