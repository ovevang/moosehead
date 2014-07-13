package no.java.moosehead.api;

public class ParticipantActionResult {
    public static enum Status {
        OK,WRONG_CAPTCHA,ERROR;
    }
    private final Status status;
    private final String errormessage;

    private ParticipantActionResult(Status status, String errormessage) {
        this.status = status;
        this.errormessage = errormessage;
    }

    public static ParticipantActionResult ok() {
        return new ParticipantActionResult(Status.OK,null);
    }

    public static ParticipantActionResult error(String message) {
        return new ParticipantActionResult(Status.ERROR,message);
    }

    public static ParticipantActionResult wrongCaptcha() {
        return new ParticipantActionResult(Status.WRONG_CAPTCHA,null);
    }


    public Status getStatus() {
        return status;
    }

    public String getErrormessage() {
        return errormessage;
    }
}
