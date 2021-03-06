package uk.gov.justice.digital.hmpps.keyworker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@ApiModel(description = "Released prisoner")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrisonerCustodyStatusDto {

    @ApiModelProperty(required = true, value = "Identifies prisoner.")
    @NotBlank
    private String offenderNo;

    @ApiModelProperty(required = true, value = "Date and time the prisoner movement was created.")
    @NotNull
    private LocalDateTime createDateTime;
}