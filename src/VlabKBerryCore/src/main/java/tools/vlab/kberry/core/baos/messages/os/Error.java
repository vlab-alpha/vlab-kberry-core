package tools.vlab.kberry.core.baos.messages.os;

import lombok.Getter;

import java.util.Arrays;

public enum Error {
    OK(0, "No Error"),
    INTERNAL_ERROR(1, "Internal Error"),
    NO_ELEMENT_FOUND(2, "Server Item or DataPoint not found!"),
    BUFFER_TO_SMALL(3, "Buffer for description string or parameters  too small!"),
    ITEMS_NOT_WRITEABLE(4, "Server item is not writable!"),
    SERVICE_NOT_SUPPORTED(5, "BAOS Modules do not support Description String!"),
    BAD_SERVICE_PARAMETER(6, "Server item, Datapoint or Parameter ID out of range!"),
    BAD_ID(7, "Writing of item failed! Server item ID or Datapoint out of range or datapoint is not configured by ETS!"),
    BAD_COMMAND(8, "Writing Server item failed (internal error) Or Writing Datapoint failed (illegal command)!"),
    BAD_LENGTH(9, "Wrong length writing Server item or Datapoint or Parameter bytes!"),
    MESSAGE_INCONSISTENT(10, "Count in writing request is wrong."),
    OBJECT_SERVER_IS_BUSY(11, "Object server is busy!"),
    UNKNOWN_CODE(99,"Unknown Error Code!"),;

    @Getter
    private final int code;
    @Getter
    private final String description;

    Error(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static Error withCode(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code).findFirst().orElse(UNKNOWN_CODE);
    }


}
