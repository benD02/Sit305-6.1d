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

    public void setQuizzes(List<Quiz> newQuizzes) {
        this.quizList = newQuizzes;
        notifyDataSetChanged();  // Notify any registered observers that the data set has changed.
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
        holder.quizDescription.setText(quiz.getQuizDescription());

        if (quiz.isCompleted()) {
            holder.quizStatus.setVisibility(View.VISIBLE);
            holder.quizStatus.setText("Completed");
        } else {
            holder.quizStatus.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return quizList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView quizName;
        public TextView quizDescription;
        public Button startQuizButton;
        OnQuizListener onQuizListener;
        public TextView quizStatus;


        public ViewHolder(View itemView, OnQuizListener onQuizListener) {
            super(itemView);
            quizName = itemView.findViewById(R.id.quizName);
            quizDescription = itemView.findViewById(R.id.quizDescription);
            startQuizButton = itemView.findViewById(R.id.startQuizButton);
            quizStatus = itemView.findViewById(R.id.quizStatus);
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
