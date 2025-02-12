package com.example.kahootmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JuegoActivity extends AppCompatActivity {

    private TextView timerTextView;
    private TextView questionTextView;
    private TextView answer1TextView;
    private TextView answer2TextView;
    private TextView answer3TextView;
    private TextView answer4TextView;
    private TextView questionCounterTextView;
    private Button buttonDeleteGame;
    private Button buttonStartGame;
    private DatabaseReference databaseReference;
    private String partidaId;
    private String dificultad;
    private List<Player> playersList = new ArrayList<>();
    private PlayersAdapter playersAdapter;
    private ScoresAdapter scoresAdapter;
    private List<Question> questionsList = new ArrayList<>();
    private Set<Integer> shownQuestions = new HashSet<>();
    private int currentQuestionIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("JuegoActivity", "onCreate called");
        setContentView(R.layout.activity_juego);

        timerTextView = findViewById(R.id.timerTextView);
        questionTextView = findViewById(R.id.questionTextView);
        answer1TextView = findViewById(R.id.answer1TextView);
        answer2TextView = findViewById(R.id.answer2TextView);
        answer3TextView = findViewById(R.id.answer3TextView);
        answer4TextView = findViewById(R.id.answer4TextView);
        questionCounterTextView = findViewById(R.id.questionCounterTextView);
        buttonDeleteGame = findViewById(R.id.buttonDeleteGame);
        buttonStartGame = findViewById(R.id.buttonStartGame);

        RecyclerView playersRecyclerView = findViewById(R.id.playersRecyclerView);
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playersAdapter = new PlayersAdapter(playersList);
        playersRecyclerView.setAdapter(playersAdapter);

        RecyclerView scoresRecyclerView = findViewById(R.id.scoresRecyclerView);
        scoresRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scoresAdapter = new ScoresAdapter(playersList);
        scoresRecyclerView.setAdapter(scoresAdapter);

        partidaId = getIntent().getStringExtra("partidaId");
        dificultad = getIntent().getStringExtra("dificultad");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        Log.d("JuegoActivity", "partidaId: " + partidaId + ", dificultad: " + dificultad);

        loadPlayers();

        buttonDeleteGame.setVisibility(View.GONE);
        buttonDeleteGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("JuegoActivity", "buttonDeleteGame clicked");
                deleteGame(partidaId);
            }
        });

        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("JuegoActivity", "buttonStartGame clicked");
                startGame();
            }
        });
    }

    private void loadPlayers() {
        Log.d("JuegoActivity", "loadPlayers called");
        databaseReference.child("salas").child(partidaId).child("jugadores").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("JuegoActivity", "loadPlayers onDataChange called");
                playersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.getKey();
                    Long score = snapshot.child("puntos").getValue(Long.class);
                    if (name != null && score != null) {
                        Log.d("JuegoActivity", "Player loaded: " + name);
                        playersList.add(new Player(name, score.intValue()));
                    }
                }
                playersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("JuegoActivity", "Error al cargar los jugadores", databaseError.toException());
            }
        });
    }

    private void startTimer(long duration, TextView timerTextView, Runnable onFinish) {
        Log.d("JuegoActivity", "startTimer called with duration: " + duration);
        new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));
                Log.d("JuegoActivity", "Timer tick: " + minutes + ":" + seconds);
            }

            public void onFinish() {
                Log.d("JuegoActivity", "Timer finished");
                onFinish.run();
            }
        }.start();
    }

    private void startGame() {
        Log.d("JuegoActivity", "startGame called");
        buttonStartGame.setVisibility(View.GONE);
        questionCounterTextView.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.VISIBLE);
        findViewById(R.id.playersRecyclerView).setVisibility(View.GONE); // Ocultar la lista de jugadores
        findViewById(R.id.scoresRecyclerView).setVisibility(View.GONE); // Ocultar la lista de puntuaciones
        findViewById(R.id.questionLayout).setVisibility(View.VISIBLE); // Mostrar el layout de preguntas
        loadQuestions();
    }

    private void loadQuestions() {
        Log.d("JuegoActivity", "loadQuestions called");
        databaseReference.child("preguntas").child(dificultad).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("JuegoActivity", "loadQuestions onDataChange called");
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Question question = snapshot.getValue(Question.class);
                        if (question != null) {
                            Log.d("JuegoActivity", "Question loaded: " + question.getTexto());
                            questionsList.add(question);
                        }
                    }
                    if (!questionsList.isEmpty()) {
                        Log.d("JuegoActivity", "Preguntas cargadas: " + questionsList.size());
                        showQuestion();
                    } else {
                        Log.e("JuegoActivity", "No se encontraron preguntas en la lista");
                    }
                } else {
                    Log.e("JuegoActivity", "No se encontraron preguntas para el id de partida: " + partidaId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("JuegoActivity", "Error al cargar las preguntas", databaseError.toException());
            }
        });
    }

    private void showQuestion() {
        Log.d("JuegoActivity", "showQuestion called");
        if (shownQuestions.size() < questionsList.size()) {
            do {
                currentQuestionIndex = (int) (Math.random() * questionsList.size());
            } while (shownQuestions.contains(currentQuestionIndex));

            shownQuestions.add(currentQuestionIndex);
            Question question = questionsList.get(currentQuestionIndex);
            question.setCanAnswer(true); // Permitir que los jugadores respondan
            questionTextView.setText(question.getTexto());
            Log.d("JuegoActivity", "Mostrando pregunta: " + question.getTexto());

            List<String> respuestas = question.getRespuestas();
            answer1TextView.setText(respuestas.get(1));
            answer2TextView.setText(respuestas.get(2));
            answer3TextView.setText(respuestas.get(3));
            answer4TextView.setText(respuestas.get(4));

            // Guardar la pregunta actual y el contador en la base de datos
            databaseReference.child("salas").child(partidaId).child("currentQuestion").setValue(question);
            databaseReference.child("salas").child(partidaId).child("currentQuestion").child("currentQuestionNumber").setValue(shownQuestions.size());
            databaseReference.child("salas").child(partidaId).child("currentQuestion").child("totalQuestions").setValue(questionsList.size());

            findViewById(R.id.scoresRecyclerView).setVisibility(View.GONE); // Ocultar la lista de puntuaciones
            findViewById(R.id.questionLayout).setVisibility(View.VISIBLE); // Mostrar el layout de preguntas
            timerTextView.setVisibility(View.VISIBLE);
            questionCounterTextView.setText(String.format("Pregunta %d/%d", shownQuestions.size(), questionsList.size()));
            startTimer(30000, timerTextView, JuegoActivity.this::showScores);
        } else {
            showFinalScores();
        }
    }

    private void showScores() {
        Log.d("JuegoActivity", "showScores called");
        findViewById(R.id.questionLayout).setVisibility(View.GONE); // Ocultar el layout de preguntas
        RecyclerView scoresRecyclerView = findViewById(R.id.scoresRecyclerView);
        scoresRecyclerView.setVisibility(View.VISIBLE); // Mostrar la lista de puntuaciones
        scoresRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scoresRecyclerView.setAdapter(scoresAdapter);
        buttonDeleteGame.setVisibility(View.GONE);
        timerTextView.setVisibility(View.VISIBLE);
        questionCounterTextView.setText(String.format("Pregunta %d/%d", shownQuestions.size(), questionsList.size()));

        // Actualizar canAnswer a false para que los jugadores no puedan responder
        databaseReference.child("salas").child(partidaId).child("currentQuestion").child("canAnswer").setValue(false);

        databaseReference.child("salas").child(partidaId).child("jugadores").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("JuegoActivity", "showScores onDataChange called");
                boolean playersFound = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    playersFound = true;
                    String playerName = snapshot.getKey();
                    Long score = snapshot.child("puntos").getValue(Long.class);
                    if (playerName != null && score != null) {
                        for (Player player : playersList) {
                            if (player.getName().equals(playerName)) {
                                player.setScore(score.intValue());
                                Log.d("JuegoActivity", "Player score updated: " + playerName + " - " + player.getScore());
                                break;
                            }
                        }
                    }
                }
                if (!playersFound) {
                    playersList.add(new Player("TESTDATA", 0));
                    Log.d("JuegoActivity", "No players found, added TESTDATA with 0 points");
                }
                scoresAdapter.notifyDataSetChanged();
                if (shownQuestions.size() < questionsList.size()) {
                    startTimer(30000, timerTextView, JuegoActivity.this::showQuestion);
                } else {
                    showFinalScores();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("JuegoActivity", "Error al cargar las puntuaciones", databaseError.toException());
            }
        });
    }

    private void showFinalScores() {
        Log.d("JuegoActivity", "showFinalScores called");
        findViewById(R.id.questionLayout).setVisibility(View.GONE); // Ocultar el layout de preguntas
        findViewById(R.id.scoresRecyclerView).setVisibility(View.VISIBLE); // Mostrar la lista de puntuaciones
        buttonDeleteGame.setVisibility(View.VISIBLE);
        timerTextView.setVisibility(View.GONE);
        questionCounterTextView.setVisibility(View.GONE);
    }

    private void deleteGame(String id) {
        Log.d("JuegoActivity", "deleteGame called with id: " + id);
        databaseReference.child("salas").child(id).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("JuegoActivity", "Juego eliminado con éxito");
                // Navigate back to MainActivity
                Intent intent = new Intent(JuegoActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Log.e("JuegoActivity", "Error al eliminar el juego", task.getException());
            }
        });
    }
}