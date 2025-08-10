package br.com.joaoVitorRibeiro.jankenpon.player;

import io.github.guisso.jankenpon.Move;
import io.github.guisso.jankenpon.AbstractPlayer;

import java.util.*;

public class MeuJogador extends AbstractPlayer {
    private final Map<Move, Integer> globalFreq = new EnumMap<>(Move.class);
    private final LinkedList<Move> recentMoves = new LinkedList<>();
    private final Random rnd = new Random();

    private final int WINDOW_SIZE = 8;
    private int recentCorrect = 0, globalCorrect = 0;
    private Move lastPrediction = Move.ROCK;

    public MeuJogador() {
        for (Move m : Move.values()) {
            globalFreq.put(m, 0);
        }
    }

    @Override
    public String getDeveloperName() {
        return "João Botelho";
    }

    @Override
    public Move makeMyMove(Move opponentPreviousMove) {
        if (opponentPreviousMove == null || opponentPreviousMove == Move.NONE) {
            return Move.ROCK;
        }

        // Atualiza frequência global
        globalFreq.put(opponentPreviousMove, globalFreq.get(opponentPreviousMove) + 1);

        // Atualiza histórico de jogadas recentes
        recentMoves.add(opponentPreviousMove);
        if (recentMoves.size() > WINDOW_SIZE) {
            recentMoves.removeFirst();
        }

        // Avalia acerto do último palpite
        if (lastPrediction != null && lastPrediction != Move.NONE) {
            if (beats(lastPrediction, opponentPreviousMove)) {
                recentCorrect++;
                globalCorrect++;
            }
        }

        // Previsão via global
        Move globalMost = mostFrequent(globalFreq);

        // Previsão via janela curta
        Map<Move, Integer> windowFreq = new EnumMap<>(Move.class);
        windowFreq.put(Move.ROCK, 0);
        windowFreq.put(Move.PAPER, 0);
        windowFreq.put(Move.SCISSORS, 0);
        for (Move m : recentMoves) {
            windowFreq.put(m, windowFreq.get(m) + 1);
        }
        Move recentMost = mostFrequent(windowFreq);

        // Decide estratégia
        Move predicted;
        if (recentCorrect >= globalCorrect) {
            predicted = counterTo(recentMost); // adversário muda rápido
        } else {
            predicted = counterTo(globalMost); // adversário previsível
        }

        // Ruído inteligente: 15% de chance de aleatório diferente do último movimento
        if (rnd.nextDouble() < 0.15) {
            Move[] vals = {Move.ROCK, Move.PAPER, Move.SCISSORS};
            Move randMove;
            do {
                randMove = vals[rnd.nextInt(vals.length)];
            } while (randMove == lastPrediction);
            predicted = randMove;
        }

        lastPrediction = predicted;
        return predicted;
    }

    private boolean beats(Move a, Move b) {
        return (a == Move.ROCK && b == Move.SCISSORS) ||
               (a == Move.PAPER && b == Move.ROCK) ||
               (a == Move.SCISSORS && b == Move.PAPER);
    }

    private Move mostFrequent(Map<Move, Integer> freq) {
        Move most = Move.ROCK;
        int best = -1;
        for (Move m : new Move[]{Move.ROCK, Move.PAPER, Move.SCISSORS}) {
            if (freq.get(m) > best) {
                best = freq.get(m);
                most = m;
            }
        }
        return most;
    }

    private Move counterTo(Move m) {
        switch (m) {
            case ROCK: return Move.PAPER;
            case PAPER: return Move.SCISSORS;
            case SCISSORS: return Move.ROCK;
            default: return Move.ROCK;
        }
    }
}
