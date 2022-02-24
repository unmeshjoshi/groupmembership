package org.distrib.patterns.net;

import org.distrib.patterns.common.RequestOrResponse;

//<codeFragment name="clientConnection">
public interface ClientConnection {
    void write(RequestOrResponse response);
    void close();
}
//</codeFragment>