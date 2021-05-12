package com.github.guikeller.jtide.services;

import org.xml.sax.SAXException;

public class DoneWithSiteException extends SAXException {

    public final static long serialVersionUID = 1L;

    public DoneWithSiteException(String s) {
        super(s);
    }

}
