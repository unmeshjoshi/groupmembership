package org.distrib.patterns.net;

import org.distrib.patterns.common.Message;
import org.distrib.patterns.common.RequestOrResponse;

public interface RequestConsumer {
    default void close(ClientConnection connection) {}
    void accept(Message<RequestOrResponse> request);
}
