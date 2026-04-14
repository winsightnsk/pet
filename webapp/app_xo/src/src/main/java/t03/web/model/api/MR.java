package t03.web.model.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@NoArgsConstructor
public class MR<T> {

    @Getter
    private T data = null;
    @Getter
    private String error = "";
    private HttpStatus status = HttpStatus.OK;

    public MR(T data) {
        this.data = data;
    }

    public MR<T> data(T data) {
        this.data = data;
        return this;
    }

    public MR<T> error(String err) {
        this.error = err;
        return this;
    }

    public MR<T> status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public ResponseEntity<MR<T>> response() {
        return new ResponseEntity<>(this, this.status);
    }

}
