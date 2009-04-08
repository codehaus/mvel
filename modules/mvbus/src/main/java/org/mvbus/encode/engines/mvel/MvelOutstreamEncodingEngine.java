package org.mvbus.encode.engines.mvel;

import org.mvbus.util.OutputAppender;
import org.mvbus.encode.engines.mvel.MvelEncodingEngine;

import java.io.OutputStream;
import java.io.PrintWriter;

public class MvelOutstreamEncodingEngine extends MvelEncodingEngine {
    private OutputAppender<PrintWriter> output;

    public MvelOutstreamEncodingEngine(final OutputStream outStream) {
        output = new OutputAppender<PrintWriter>() {
            private PrintWriter writer = new PrintWriter(outStream);

            public OutputAppender append(String str) {
                writer.append(str);
                return this;
            }

            public PrintWriter getTarget() {
                return writer;
            }
        };
    }

    public String getEncoded() {
        return null;
    }

    public OutputAppender append(String str) {
        return output.append(str);
    }

    public void flush() {
        output.getTarget().flush();
    }
}
