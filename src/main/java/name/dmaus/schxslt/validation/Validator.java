/*
 * Copyright 2019 by David Maus <dmaus@dmaus.name>
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package name.dmaus.schxslt.validation;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.transform.dom.DOMResult;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import name.dmaus.schxslt.Schematron;
import name.dmaus.schxslt.Result;

public class Validator extends javax.xml.validation.Validator
{
    private ErrorHandler errors;
    private LSResourceResolver resources;

    private Schematron schematron;

    public Validator (Schematron schematron)
    {
        this.schematron = schematron;
    }

    public ErrorHandler getErrorHandler ()
    {
        return this.errors;
    }

    public void setErrorHandler (ErrorHandler errorHandler)
    {
        this.errors = errorHandler;
    }

    public LSResourceResolver getResourceResolver ()
    {
        return this.resources;
    }

    public void setResourceResolver (LSResourceResolver resourceResolver)
    {
        this.resources = resourceResolver;
    }

    public void reset ()
    {}

    public void validate (Source source, javax.xml.transform.Result result) throws SAXException
    {
        Result validity = this.schematron.validate(source);
        if (!validity.isValid()) {
            for (String message : validity.getValidationMessages()) {
                try {
                    this.signalError(message);
                } catch (Throwable t) {
                    throw new SAXException();
                }
            }
            throw new SAXException();
        }

        if (result != null) {
            try {
                TransformerFactory.newInstance().newTransformer().transform(source, result);
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void signalError (final String message) throws Throwable
    {
        if (this.errors != null) {
            this.errors.error(new SAXParseException(message, null));
        }
    }
}