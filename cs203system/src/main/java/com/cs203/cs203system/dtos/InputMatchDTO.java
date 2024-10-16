package com.cs203.cs203system.dtos;

import com.cs203.cs203system.enums.MatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.cs203.cs203system.model.Match}
 */
@Value
public class InputMatchDTO implements Serializable {
    @NotNull
    Long id;
    Integer durationInMinutes;
    @NotNull
    MatchStatus status;
    @NotNull
    Integer player1Score;
    @NotNull
    Integer player2Score;
    LocalDate matchDate;

    // New fields for punches and dodges
    Integer punchesPlayer1;
    Integer punchesPlayer2;
    Integer dodgesPlayer1;
    Integer dodgesPlayer2;

    // New fields for KO information
    boolean koByPlayer1;   // Whether Player 1 performed a KO
    boolean koByPlayer2;   // Whether Player 2 performed a KO

}