package t03.web.model.api;

import lombok.Data;

@Data
public class SignUpRequest {
    private String login;
    private String password;
}
