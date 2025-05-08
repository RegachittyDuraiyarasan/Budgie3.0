package com.hepl.budgie.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class AppUtils {

    private AppUtils() {
        throw new IllegalStateException("App utils");
    }

    public static String generateUniqueId(String lastSequence) {
        long count = 0;
        String[] part = lastSequence.split("(?<=\\D)(?=\\d)");
        count = Long.parseLong(part[1]);

        return part[0] + ("00000" + (count + 1)).substring(String.valueOf(count).length());
    }

    public static ZonedDateTime parseLocalDate(LocalDate localDate, String zoneId) {
        return localDate.atStartOfDay(ZoneId.of(zoneId));
    }

    public static ZonedDateTime parseZonedDate(String format, String date, String zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDate = LocalDateTime.parse(date, formatter);
        return ZonedDateTime.of(localDate, ZoneId.of(zoneId));
    }


    public static String parseZonedDate(String format, ZonedDateTime date, String zoneId) {
        ZonedDateTime zoneDate = date.toInstant()
                .atZone(ZoneId.of(zoneId));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return zoneDate.format(formatter);
    }

    public static String parseLocalDate(String format, LocalDateTime date, String zoneId) {
        if (date == null || format == null || zoneId == null) {
            throw new IllegalArgumentException("Format, date, and zoneId must not be null");
        }
    
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        ZonedDateTime zonedDateTime = date.atZone(ZoneId.of(zoneId));
    
        return formatter.format(zonedDateTime);
    }
    

    public static byte[] generateQRCode(String data, int width, int height) throws IOException, WriterException {
        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hintMap);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    public static String generateUniqueIdExpEdu(String lastSequence, int digits) {
        // Split into prefix (non-numeric) and numeric parts
        String[] part = lastSequence.split("(?<=\\D)(?=\\d)");
        long count = Long.parseLong(part[1]);

        // Increment count and format with leading zeros
        return part[0] + String.format("%0"+digits+"d", count + 1);
    }

    public static Object typeConversionValue(String value, String type) {
        if (type.equals("boolean")) {
            return Boolean.parseBoolean(value);
        } else if (type.equals("int")) {
            return Integer.parseInt(value);
        } else if (type.equals("double")) {
            return Double.parseDouble(value);
        }

        return value;
    }
    public static String formatZonedDate(String pattern, ZonedDateTime dateTime, String timeZoneId) {
        if (dateTime == null || pattern == null || timeZoneId == null) {
            return null;
        }
        return dateTime.withZoneSameInstant(ZoneId.of(timeZoneId))
                .format(DateTimeFormatter.ofPattern(pattern));
    }
    

}
