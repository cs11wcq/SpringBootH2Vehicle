package mitchell.SpringBootH2Vehicle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
public class InvalidYearException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidYearException(String message){
        super(message);
    }
}
