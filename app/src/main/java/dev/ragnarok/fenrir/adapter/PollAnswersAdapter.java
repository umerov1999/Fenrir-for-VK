package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.settings.CurrentTheme;

public class PollAnswersAdapter extends RecyclerBindableAdapter<Poll.Answer, PollAnswersAdapter.ViewHolder> {

    private final Context context;
    private Set<Integer> checkedIds;
    private boolean checkable;
    private OnAnswerChangedCallback listener;
    private boolean multiple;

    public PollAnswersAdapter(Context context, @NonNull List<Poll.Answer> items) {
        super(items);
        this.context = context;
        checkedIds = new HashSet<>();
    }

    @Override
    protected void onBindItemViewHolder(ViewHolder holder, int position, int type) {
        Poll.Answer answer = getItem(position);

        holder.tvTitle.setText(answer.getText());
        holder.rbButton.setText(answer.getText());

        holder.tvCount.setText(String.valueOf(answer.getVoteCount()));
        holder.pbRate.setProgress((int) answer.getRate());
        holder.pbRate.setProgressTintList(ColorStateList.valueOf(CurrentTheme.getColorPrimary(context)));

        boolean isMyAnswer = checkedIds.contains(answer.getId());

        holder.rbButton.setOnCheckedChangeListener(null);
        holder.rbButton.setChecked(isMyAnswer);
        holder.rbButton.setOnCheckedChangeListener((compoundButton, checked) -> changeChecked(answer.getId(), checked));

        //holder.mVotedRoot.setVisibility(checkable ? View.GONE : View.VISIBLE);
        holder.tvTitle.setVisibility(checkable ? View.GONE : View.VISIBLE);
        holder.rbButton.setVisibility(checkable ? View.VISIBLE : View.GONE);
    }

    @Override
    protected ViewHolder viewHolder(View view, int type) {
        return new ViewHolder(view);
    }

    @Override
    protected int layoutId(int type) {
        return R.layout.item_poll_answer;
    }

    private void changeChecked(int id, boolean isChecked) {
        if (checkable) {
            if (isChecked) {
                if (!multiple) {
                    checkedIds.clear();
                }
                checkedIds.add(id);
            } else {
                checkedIds.remove(id);
            }

            if (listener != null) {
                listener.onAnswerChanged(checkedIds);
            }

            notifyDataSetChanged();
        }
    }

    public void setData(List<Poll.Answer> answers, boolean checkable, boolean multiple, Set<Integer> checkedIds) {
        setItems(answers, false);
        this.checkable = checkable;
        this.multiple = multiple;
        this.checkedIds = checkedIds;
        notifyDataSetChanged();
    }

    public void setListener(OnAnswerChangedCallback listener) {
        this.listener = listener;
    }

    public interface OnAnswerChangedCallback {
        void onAnswerChanged(Set<Integer> checked);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvCount;
        final MaterialCheckBox rbButton;
        final TextView tvTitle;
        final ProgressBar pbRate;

        public ViewHolder(View itemView) {
            super(itemView);
            rbButton = itemView.findViewById(R.id.item_poll_answer_radio);
            tvCount = itemView.findViewById(R.id.item_poll_answer_count);
            tvTitle = itemView.findViewById(R.id.item_poll_answer_title);
            pbRate = itemView.findViewById(R.id.item_poll_answer_progress);
        }
    }
}
