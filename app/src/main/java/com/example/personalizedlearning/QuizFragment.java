package com.example.personalizedlearning;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class QuizFragment extends Fragment {

    private static final String ARG_QUIZ_TITLE = "quizTitle";
    private static final String ARG_QUIZ_DESCRIPTION = "quizDescription";

    private String quizTitle;
    private String quizDescription;

    public QuizFragment() {
    }

    public static QuizFragment newInstance(String serializedQuiz) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putString("quiz", serializedQuiz);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String serializedQuiz = getArguments().getString("quiz");
            Gson gson = new Gson();
            Quiz quiz = gson.fromJson(serializedQuiz, new TypeToken<Quiz>(){}.getType());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);
        TextView tvTitle = view.findViewById(R.id.quiz_fragment_title);
        TextView tvDescription = view.findViewById(R.id.quiz_fragment_description);

        tvTitle.setText(quizTitle);
        tvDescription.setText(quizDescription);

        return view;
    }
}
