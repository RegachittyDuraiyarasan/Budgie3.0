package com.hepl.budgie.config.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.ibm.icu.text.MessageFormat;

@Component
public class Translator {

    private final MessageSource messageSource;

    public Translator(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String toLocale(String msgCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, null, locale);
    }

    public String toLocale(String msgCode, Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", locale);
        String format = bundle.getString(msgCode);
        MessageFormat formatter = new MessageFormat(format, locale);
        return formatter.format(args);
    }

}
