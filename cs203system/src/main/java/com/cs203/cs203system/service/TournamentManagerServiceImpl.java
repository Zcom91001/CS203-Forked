//package com.cs203.cs203system.utility;
//
//import com.cs203.cs203system.dtos.TournamentCreateDto;
//import com.cs203.cs203system.enums.TournamentFormat;
//import com.cs203.cs203system.enums.TournamentStatus;
//import com.cs203.cs203system.model.Player;
//import com.cs203.cs203system.model.Tournament;
//import com.cs203.cs203system.repository.PlayerRepository;
//import com.cs203.cs203system.repository.TournamentRepository;
//import com.cs203.cs203system.service.TournamentService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Random;
//
//@Service
//public class TournamentManagerImpl implements TournamentManager {
//    private static final Logger logger = LoggerFactory.getLogger(TournamentManagerImpl.class);
//
//    @Autowired
//    private SwissRoundManager swissRoundManager;
//
//    @Autowired
//    private DoubleEliminationManager doubleEliminationManager;
//
//    @Autowired
//    private TournamentRepository tournamentRepository;
//
//    @Autowired
//    private PlayerRepository playerRepository;
//
//    @Autowired
//    private TournamentService tournamentService;
//
//    private Tournament tournament;
//
//
//    private final Random random = new Random();
//
//    public List<Player> getPlayersForTournament(Tournament tournament) {
//        return playerRepository.findByTournaments_Id(tournament.getId());
//    }
//
//    @Override
//    @Transactional
//    public Tournament initializeTournament(Tournament tournament) {
//        setTournamentDetails(tournament);
//        startTournament(tournament);
//
//        List<Player> players = getPlayersForTournament(tournament);
//        Tournament output = tournamentRepository.save(tournament);
//        switch (tournament.getFormat()) {
//            case SWISS:
//                logger.debug("Initializing Swiss tournament...");
//                swissRoundManager.initializeRounds(tournament);
//                break;
//            case DOUBLE_ELIMINATION:
//                logger.debug("Initializing Double Elimination tournament...");
//                doubleEliminationManager.initializeDoubleElimination(tournament, players);
//                break;
//            case HYBRID:
//                logger.debug("Initializing Hybrid tournament with Swiss rounds...");
//                swissRoundManager.initializeRounds(tournament);
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
//        }
//
//        return output;
//    }
//
//    @Override
//    @Transactional
//    public Tournament initializeTournament(TournamentCreateDto tournamentCreateDto) {
//        Tournament tournament = new Tournament();
//        tournamentService.addPlayerToTournament(tournament.getId(),tournamentCreateDto.getPlayerIds());
//        setTournamentDetails(tournament);
//        startTournament(tournament);
//
//        List<Player> players = getPlayersForTournament(tournament);
//        Tournament output = tournamentRepository.save(tournament);
//        switch (tournament.getFormat()) {
//            case SWISS:
//                logger.debug("Initializing Swiss tournament...");
//                swissRoundManager.initializeRounds(tournament);
//                break;
//            case DOUBLE_ELIMINATION:
//                logger.debug("Initializing Double Elimination tournament...");
//                doubleEliminationManager.initializeDoubleElimination(tournament, players);
//                break;
//            case HYBRID:
//                logger.debug("Initializing Hybrid tournament with Swiss rounds...");
//                swissRoundManager.initializeRounds(tournament);
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
//        }
//
//        return output;
//    }
//
//
//    @Override
//    @Transactional
//    public void progressTournament(Tournament tournament) {
//        logger.debug("Progressing tournament: {} with format: {}", tournament.getName(), tournament.getFormat());
//
//        // Check if the tournament is already complete
//        if (isTournamentComplete(tournament)) {
//            logger.debug("Tournament already complete. Skipping further progression.");
//            return; // Exit if the tournament is already complete
//        }
//
//        // Handle progression based on the tournament format
//        switch (tournament.getFormat()) {
//            case SWISS:
//                logger.debug("Progressing Swiss format...");
//                if (!swissRoundManager.isSwissPhaseComplete(tournament)) {
//                    swissRoundManager.updateStandings(tournament); // Update standings if not complete
//                } else {
//                    // Transitioning to the next phase or marking the tournament as completed
//                    logger.debug("Swiss phase complete. Marking the tournament as complete.");
//                    tournament.setStatus(TournamentStatus.COMPLETED);
//                    tournamentRepository.save(tournament);
//                }
//                break;
//
//            case DOUBLE_ELIMINATION:
//                logger.debug("Progressing Double Elimination format...");
//                if (!doubleEliminationManager.isDoubleEliminationComplete(tournament)) {
//                    // Progress the double elimination
//                    logger.debug("Progressing Double Elimination...");
//                    // Uncomment the method to update standings for Double Elimination
//                    // doubleEliminationManager.updateStandings(tournament);
//                } else {
//                    logger.debug("Double Elimination complete. Marking the tournament as complete.");
//                    tournament.setStatus(TournamentStatus.COMPLETED);
//                    tournamentRepository.save(tournament);
//                }
//                break;
//
//            case HYBRID:
//                logger.debug("Handling Hybrid progression...");
//                handleHybridProgression(tournament); // Utilize existing logic for hybrid handling
//                break;
//
//            default:
//                throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
//        }
//
//        tournamentRepository.save(tournament);
//    }
//
//    @Transactional
//    public void handleHybridProgression(Tournament tournament) {
//        logger.debug("Handling Hybrid progression...");
//
//        // Step 1: Progress Swiss rounds if not completed
//        if (!swissRoundManager.isSwissPhaseComplete(tournament)) {
//            logger.debug("Continuing Swiss rounds...");
//            swissRoundManager.updateStandings(tournament);
//        }
//        // Step 2: If Swiss is complete and Double Elimination has not started, transition
//        else if (!tournament.isDoubleEliminationStarted()) {
//
//            logger.debug("Swiss rounds are complete. Determining Swiss winner...");
//
//            // Determine the Swiss winner and log the result
//            Player swissWinner = swissRoundManager.determineSwissWinner(tournament);
//            if (swissWinner != null) {
//                logger.debug("Swiss winner is: {}", swissWinner.getName());
//            }
//            logger.debug("Swiss rounds are complete. Transitioning to Double Elimination...");
//            List<Player> topPlayers = swissRoundManager.getTopPlayers(tournament);
//            doubleEliminationManager.initializeDoubleElimination(tournament, topPlayers);
//
//            // Mark that Double Elimination has started to avoid re-transitioning
//            tournament.setDoubleEliminationStarted(true);
//            tournamentRepository.save(tournament);
//        }
//        // Step 3: If Double Elimination has started, progress through it
//        else {
//            if (!doubleEliminationManager.isDoubleEliminationComplete(tournament)) {
//                logger.debug("Progressing Double Elimination rounds...");
//                // Add logic here to progress the Double Elimination if needed.
//                // Uncomment this when you implement standings updates for Double Elimination
//                // doubleEliminationManager.updateStandings(tournament);
//            } else {
//                logger.debug("Double Elimination is complete. Marking tournament as complete.");
//                tournament.setStatus(TournamentStatus.COMPLETED);
//                tournamentRepository.save(tournament); // Mark tournament as complete
//            }
//        }
//    }
//
//    @Override
//    public boolean isTournamentComplete(Tournament tournament) {
//        switch (tournament.getFormat()) {
//            case SWISS:
//                return swissRoundManager.isSwissPhaseComplete(tournament);
//            case DOUBLE_ELIMINATION:
//                return doubleEliminationManager.isDoubleEliminationComplete(tournament);
//            case HYBRID:
//                // Ensure this checks both Swiss and Double Elimination completion properly
//                return swissRoundManager.isSwissPhaseComplete(tournament) &&
//                        doubleEliminationManager.isDoubleEliminationComplete(tournament);
//            default:
//                throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
//        }
//    }
//
//    @Override
//    @Transactional
//    public Player determineWinner(Tournament tournament) {
//        if (isTournamentComplete(tournament)) {
//            completeTournament(tournament);
//            switch (tournament.getFormat()) {
//                case SWISS:
//                    logger.debug("Determining Swiss winner...");
//                    return swissRoundManager.determineSwissWinner(tournament);
//                case DOUBLE_ELIMINATION:
//                    logger.debug("Determining Double Elimination winner...");
//                    return doubleEliminationManager.determineWinner(tournament);
//                case HYBRID:
//                    logger.debug("Determining Hybrid winner from Double Elimination phase...");
//                    return doubleEliminationManager.determineWinner(tournament); // In Hybrid, the winner is from Double Elimination phase
//                default:
//                    throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
//            }
//        }
//        logger.debug("Tournament not complete; no winner determined.");
//        return null;
//    }
//
//    //todo:
//    @Override
//    public void setTournamentDetails(Tournament tournament) {
//        LocalDate startDate = LocalDate.now().plusDays(random.nextInt(10)); // Start in 0-9 days
//        LocalDate endDate = startDate.plusDays(5 + random.nextInt(5)); // Duration of 5-9 days
//        tournament.setStartDate(startDate);
//        tournament.setEndDate(endDate);
//
//        List<String> locations = Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix");
//        tournament.setLocation(locations.get(random.nextInt(locations.size())));
//
//        tournament.setStatus(TournamentStatus.SCHEDULED);
////        TournamentFormat[] formats = TournamentFormat.values();
////        tournament.setFormat(formats[random.nextInt(formats.length)]);
//        tournament.setFormat(TournamentFormat.DOUBLE_ELIMINATION);
//
//        tournamentRepository.save(tournament);
//    }
//
//    @Override
//    public void startTournament(Tournament tournament) {
//        tournament.setStatus(TournamentStatus.ONGOING);
//        tournamentRepository.save(tournament);
//    }
//
//    @Override
//    public void completeTournament(Tournament tournament) {
//        tournament.setStatus(TournamentStatus.COMPLETED);
//        tournamentRepository.save(tournament);
//        //resetTournamentDataForPlayers
//    }
//}


package com.cs203.cs203system.service;

import com.cs203.cs203system.enums.TournamentStatus;
import com.cs203.cs203system.exceptions.NotFoundException;
import com.cs203.cs203system.model.Match;
import com.cs203.cs203system.model.Player;
import com.cs203.cs203system.model.Round;
import com.cs203.cs203system.model.Tournament;
import com.cs203.cs203system.repository.PlayerRepository;
import com.cs203.cs203system.repository.TournamentRepository;
import com.cs203.cs203system.utilities2.DoubleEliminationManager;
import com.cs203.cs203system.utility.SwissRoundManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TournamentManagerServiceImpl implements TournamentManagerService {
    private static final Logger logger = LoggerFactory.getLogger(TournamentManagerServiceImpl.class);

    @Autowired
    private SwissRoundManager swissRoundManager;

    @Autowired
    private DoubleEliminationManager doubleEliminationManager;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Override
    public Optional<Tournament> findTournamentById(Long id) {
        Optional<Tournament> tournament = tournamentRepository.findById(id);
        if (tournament.isEmpty()) {
            throw new NotFoundException("Tournament id of " + id + " does not exist");
        }
        return tournament; // Simply return the found tournament Optional
    }

    @Override
    public List<Tournament> findAllTournaments() {
        return tournamentRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteTournamentById(Long id) {
        // Ensure the tournament exists before deletion
        if (!tournamentRepository.existsById(id)) {
            throw new NotFoundException("Tournament id of " + id + " does not exist");
        }
        tournamentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Tournament createTournament(Tournament tournament, List<Long> playerIds) {
        tournament.setId(null);
        List<Player> players = playerIds
                .stream()
                .map(playerId -> playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new NotFoundException("Player " + playerId + " not found")))
                .collect(Collectors.toList());
        tournament.setPlayers(players);
        tournament.setStatus(TournamentStatus.ONGOING);

        tournament = tournamentRepository.save(tournament);
        logger.error("Tournament format: {}", tournament.getFormat());
        switch (tournament.getFormat()) {
            case SWISS:
                logger.debug("Initializing Swiss tournament...");
                swissRoundManager.initializeRounds(tournament);
                break;
            case DOUBLE_ELIMINATION:
                logger.debug("Initializing Double Elimination tournament...");
                doubleEliminationRunner(tournament);
                break;
            case HYBRID:
                logger.debug("Initializing Hybrid tournament with Swiss rounds...");
                swissRoundManager.initializeRounds(tournament);
                break;
            default:
                throw new IllegalArgumentException("Unsupported tournament format: " + tournament.getFormat());
        }
        // Pairing
        return tournament;
    }

    public Tournament doubleEliminationRunner(Tournament tournament) {
        //Stream players into respective brackets
        //Eliminate players who have lost more than two
        boolean is_final = false;
        if (tournament.getPlayers().size() == 2) {
            is_final = true;
        }
        if (tournament.getRoundsCompleted() > 0) {
            doubleEliminationManager.receiveResult(tournament, tournament.getRoundsCompleted()+1, is_final);
        }
        doubleEliminationManager.initializeDoubleElimination(tournament);
        Round round = doubleEliminationManager.initializeRound(tournament, tournament.getPlayers().stream().toList());
        List<Pair<Player, Player>> pairedPlayer = doubleEliminationManager.pairPlayers(tournament.getPlayers());
        return doubleEliminationManager.createBracketMatches(tournament, round);
    }
}