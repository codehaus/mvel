package org.mvbus.encode.engines.mvel;

import org.mvel2.util.StringAppender;
import org.mvbus.util.OutputAppender;
import org.mvbus.encode.engines.mvel.MvelEncodingEngine;

public class MvelSimpleEncodingEngine extends MvelEncodingEngine {
    private OutputAppender<StringAppender> output = new OutputAppender<StringAppender>() {
        private StringAppender appender = new StringAppender();

        public OutputAppender append(String str) {
            appender.append(str);
            return this;
        }

        public StringAppender getTarget() {
            return appender;
        }
    };

    public String getEncoded() {
        return output.getTarget().toString();
    }

    public OutputAppender append(String str) {
        return output.append(str);
    }

    public void flush() {
    }
}
