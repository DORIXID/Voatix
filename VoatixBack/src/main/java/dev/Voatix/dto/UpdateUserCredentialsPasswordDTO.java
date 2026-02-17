package dev.Voatix.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserCredentialsPasswordDTO {
    @Size(min = 6, max = 30)
    private String nickname;
    @Size(min = 6, max = 30)
    private String password;
    @Email()
    @JsonProperty("eMail")
    private String eMail;
}
