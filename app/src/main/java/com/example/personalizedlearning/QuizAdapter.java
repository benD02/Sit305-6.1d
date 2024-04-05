package com.example.personalizedlearning;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {

    private List<Quiz> quizList;
    private OnQuizListener onQuizListener;

    public QuizAdapter(List<Quiz> quizList, OnQuizListener onQuizListener) {
        this.quizList = quizList;
        this.onQuizListener = onQuizListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_item, parent, false);
        return new ViewHolder(view, onQuizListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Quiz quiz = quizList.get(position);
        holder.quizName.setText(quiz.getQuizName());
    }

    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView quizName;
        public Button startQuizButton;
        OnQuizListener onQuizListener;

        public ViewHolder(View itemView, OnQuizListener onQuizListener) {
            super(itemView);
            quizName = itemView.findViewById(R.id.quizName);
            startQuizButton = itemView.findViewById(R.id.startQuizButton);
            this.onQuizListener = onQuizListener;
            startQuizButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onQuizListener.onQuizClick(getAdapterPosition());
        }
    }

    public interface OnQuizListener {
        void onQuizClick(int position);
    }
}
