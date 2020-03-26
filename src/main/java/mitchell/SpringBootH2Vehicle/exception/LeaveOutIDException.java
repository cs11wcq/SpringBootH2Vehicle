package mitchell.SpringBootH2Vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class LeaveOutIDException extends Exception {
    private static final long serialVersionUID = 1L;

    public LeaveOutIDException(String message){
        super(message);
    }
}
