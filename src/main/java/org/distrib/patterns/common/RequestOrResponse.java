package org.distrib.patterns.common;

import com.google.common.base.Objects;

public class RequestOrResponse {
    private Integer requestId;
    private byte[] messageBodyJson;
    private Integer correlationId;
    private Integer groupId = -1;
    //for jackson
    private RequestOrResponse(){}

    public RequestOrResponse(Integer requestId, int correlationId) {
        this(requestId, "".getBytes(), correlationId);
    }

    public RequestOrResponse(Integer requestId, byte[] messageBodyJson) {
        this(requestId, messageBodyJson, 0);
    }
    public RequestOrResponse(Integer requestId, byte[] messageBodyJson, Integer correlationId) {
        this(-1, requestId, messageBodyJson, correlationId);
    }

    public RequestOrResponse(Integer groupId, Integer requestId, byte[] messageBodyJson, Integer correlationId) {
        this.groupId = groupId;
        this.requestId = requestId;
        this.messageBodyJson = messageBodyJson;
        this.correlationId = correlationId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public byte[] getMessageBodyJson() {
        return messageBodyJson;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestOrResponse that = (RequestOrResponse) o;
        return Objects.equal(requestId, that.requestId) && Objects.equal(messageBodyJson, that.messageBodyJson) && Objects.equal(correlationId, that.correlationId) && Objects.equal(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(requestId, messageBodyJson, correlationId, groupId);
    }

    @Override
    public String toString() {
        return "RequestOrResponse{" +
                "requestId=" + requestId +
                ", messageBodyJson='" + messageBodyJson + '\'' +
                ", correlationId=" + correlationId +
                ", groupId=" + groupId +
                '}';
    }
}

