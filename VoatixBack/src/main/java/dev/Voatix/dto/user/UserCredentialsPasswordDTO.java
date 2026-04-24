package dev.Voatix.dto.user;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentialsPasswordDTO {
    @NotBlank()
    @Size(min = 6, max = 30)
    private String nickname;
    @NotBlank()
    @Size(min = 6, max = 30)
    private String password;
    @NotBlank()
    @Email()
    @JsonProperty("eMail")
    private String eMail;
}
