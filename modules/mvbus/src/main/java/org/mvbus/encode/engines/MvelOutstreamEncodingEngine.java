package org.mvbus.encode.engines;

import org.mvbus.util.OutputAppender;

import java.io.OutputStream;
import java.io.PrintWriter;

public class MvelOutstreamEncodingEngine extends MvelEncodingEngine {
    private OutputStream outStream;
    private OutputAppender<PrintWriter> output;

    public MvelOutstreamEncodingEngine(final OutputStream outStream) {
        this.outStream = outStream;
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
