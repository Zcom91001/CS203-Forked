package com.cs203.cs203system.dtos;

import com.cs203.cs203system.model.Tournament;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

/**
 * DTO for {@link com.cs203.cs203system.model.Tournament}
 *
 * Optional datatypes if to allow for dynamic request
 */
@Data
public class TournamentUpdateRequest implements Serializable {

//    @NotNull(message = "ID cannot be null")
    Integer id;

    Optional<String> name = Optional.empty();

//    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in the format YYYY-MM-DD")
    Optional<LocalDate> startDate = Optional.empty();

//    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in the format YYYY-MM-DD")
    Optional<LocalDate> endDate = Optional.empty();

    Optional<String> venue = Optional.empty();
}